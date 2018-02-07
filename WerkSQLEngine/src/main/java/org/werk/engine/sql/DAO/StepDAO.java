package org.werk.engine.sql.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.pillar.db.interfaces.TransactionContext;
import org.pillar.db.jdbc.JDBCTransactionContext;
import org.pillar.db.jdbc.JDBCTransactionFactory;
import org.werk.data.StepPOJO;
import org.werk.engine.json.ParameterContextSerializer;
import org.werk.engine.json.StepProcessingHistorySerializer;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.steps.StepProcessingLogRecord;

public class StepDAO {
	protected ParameterContextSerializer parameterContextSerializer;
	protected StepProcessingHistorySerializer stepProcessingHistorySerializer;
	
	public StepDAO(ParameterContextSerializer parameterContextSerializer, 
			StepProcessingHistorySerializer stepProcessingHistorySerializer) {
		this.parameterContextSerializer = parameterContextSerializer;
		this.stepProcessingHistorySerializer = stepProcessingHistorySerializer;
	}
	
	protected void loadStepPOJO(ResultSet rs, Map<Long, StepPOJO> stepMap) throws SQLException {
		long stepId = rs.getLong(1);
		DBStepPOJO step;
		
		if (!stepMap.containsKey(stepId)) {
			long jobId = rs.getLong(2);
			String stepTypeName = rs.getString(3);
			boolean isRollback = rs.getBoolean(4);
			int stepNumber = rs.getInt(5);
			int executionCount = rs.getInt(6);
			
			String stepParametersStr = rs.getString(7);
			Map<String, Parameter> stepParameters;
			if (rs.wasNull())
				stepParameters = new HashMap<>();
			else
				stepParameters = 
					parameterContextSerializer.deserializeParameters(new JSONObject(stepParametersStr));
			
			List<StepProcessingLogRecord> processingLog;
			String processingLogStr = rs.getString(8);
			if (rs.wasNull())
				processingLog = new ArrayList<>();
			else
				processingLog = 
					stepProcessingHistorySerializer.deserializeLog(new JSONObject(processingLogStr));
			
			List<Integer> rollbackStepNumbers = new ArrayList<>();
			step = new DBStepPOJO(jobId, stepId, stepTypeName, isRollback, stepNumber, rollbackStepNumbers, executionCount, 
					stepParameters, processingLog);
			stepMap.put(stepId, step);
		} else {
			step = (DBStepPOJO)stepMap.get(stepId);
		}
		
		int rollbackStepNumber = rs.getInt(9);
		if (!rs.wasNull())
			step.getRollbackStepNumbers().add(rollbackStepNumber);
	}
	
	public StepPOJO getStep(TransactionContext tc, long jobId, long stepId) throws SQLException {
		Collection<StepPOJO> steps = getProcessingHistory(tc, jobId, Optional.empty(),
				Optional.empty(), Optional.of(stepId));
		
		if ((steps == null) || (steps.isEmpty()))
			return null;
		
		for (StepPOJO step : steps)
			return step;
		
		return null;
	}
	
	public Collection<StepPOJO> getProcessingHistory(TransactionContext tc, Long jobId, Optional<String> stepTypeName,
			Optional<Long> stepNumber, Optional<Long> stepId) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			StringBuilder sb = new StringBuilder("SELECT s1.id_step, s1.id_job, s1.step_type, s1.is_rollback, s1.step_number, "
					+ "s1.execution_count, s1.step_parameter_state, s1.step_processing_log, "
					+ "s2.step_number "
					+ "FROM steps s1 "
					+ "LEFT JOIN step_rollback sr "
					+ "	 INNER JOIN steps s2 "
					+ "  ON s2.id_step = sr.id_step_being_rolled_back "
					+ "ON s1.id_step = sr.id_rollback_step "
					+ "WHERE id_job = ?");
			
			if (stepTypeName.isPresent())
				sb.append(" AND s1.step_type = ?");
			if (stepNumber.isPresent())
				sb.append(" AND s1.step_number = ?");
			if (stepId.isPresent())
				sb.append(" AND s1.id_step = ?");
			sb.append(" ORDER BY s1.step_number");
			
			pst = connection.prepareStatement(sb.toString());
			
			pst.setLong(1, jobId);
			
			int cnt = 2;
			if (stepTypeName.isPresent())
				pst.setString(cnt++, stepTypeName.get());
			if (stepNumber.isPresent())
				pst.setLong(cnt++, stepNumber.get());
			if (stepId.isPresent())
				pst.setLong(cnt++, stepId.get());
			
			ResultSet rs = pst.executeQuery();
			
			Map<Long, StepPOJO> stepMap = new HashMap<>();
			while (rs.next())
				loadStepPOJO(rs, stepMap);
			
			return stepMap.values();
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public int updateStep(TransactionContext tc, long stepId, int executionCount, Map<String, Parameter> stepParameters,
			List<StepProcessingLogRecord> processingLog) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			pst = connection.prepareStatement("UPDATE steps SET execution_count = ?, step_parameter_state = ?, " + 
					"step_processing_log = ? " + 
					"WHERE id_step = ?");
			
			pst.setInt(1, executionCount);
			pst.setString(2, parameterContextSerializer.serializeParameters(stepParameters).toString());
			pst.setString(3, stepProcessingHistorySerializer.serializeLog(processingLog).toString());
			pst.setLong(4, stepId);
			
			return pst.executeUpdate();
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public long createRollbackStepRecord(TransactionContext tc, long jobId, long rollbackStepId, int stepBeingRolledBackNumber) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			pst = connection.prepareStatement("INSERT INTO step_rollback (id_rollback_step, id_step_being_rolled_back) " + 
					"SELECT ?, id_step FROM steps WHERE step_number = ? AND id_job = ?");
			
			pst.setLong(1, rollbackStepId);
			pst.setLong(2, stepBeingRolledBackNumber);
			pst.setLong(3, jobId);
			
			pst.executeUpdate();
			
			long serverId = JDBCTransactionFactory.getLastId(connection);
			return serverId;
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public long createStep(TransactionContext tc, long jobId, String stepType, boolean isRollback,
			int stepNumber, int executionCount, Optional<Map<String, Parameter>> stepParameters,
			List<StepProcessingLogRecord> processingLog) throws SQLException {
		Connection connection = ((JDBCTransactionContext)tc).getConnection();
		PreparedStatement pst = null;
		
		try {
			pst = connection.prepareStatement("INSERT INTO steps (id_job, step_type, is_rollback, " + 
					"step_number, execution_count, step_parameter_state, step_processing_log) " + 
					"VALUES (?, ?, ?, ?, ?, ?, ?)");

			pst.setLong(1, jobId);
			pst.setString(2, stepType);
			pst.setBoolean(3, isRollback);
			pst.setInt(4, stepNumber);
			pst.setInt(5, executionCount);
			pst.setString(6, stepParameters.isPresent() 
					? parameterContextSerializer.serializeParameters(stepParameters.get()).toString() 
					: new JSONObject().toString());
			pst.setString(7, stepProcessingHistorySerializer.serializeLog(processingLog).toString());
			
			pst.executeUpdate();
			
			long serverId = JDBCTransactionFactory.getLastId(connection);
			return serverId;
		} finally {
			if (pst != null) pst.close();
		}
	}
	
	public long createProcessingStep(TransactionContext tc, long jobId, String stepType, int stepNumber) throws SQLException {
		return createStep(tc, jobId, stepType, true, stepNumber, 0, Optional.empty(), new ArrayList<>());
	}
	
	public long createProcessingStep(TransactionContext tc, long jobId, String stepType, int stepNumber,
			Optional<Map<String, Parameter>> stepParameters) throws SQLException {
		return createStep(tc, jobId, stepType, true, stepNumber, 0, stepParameters, new ArrayList<>());
	}
	
	public long createRollbackStep(TransactionContext tc, long jobId, String stepType, int stepNumber,
			Optional<Map<String, Parameter>> stepParameters, Optional<List<Integer>> stepsToRollback) throws SQLException {
		long stepId = createStep(tc, jobId, stepType, true, stepNumber, 0, stepParameters, new ArrayList<>());
		
		if (stepsToRollback.isPresent())
		for (Integer stepToRollback : stepsToRollback.get())
			createRollbackStepRecord(tc, jobId, stepId, stepToRollback);
		
		return stepId;
	}
}
