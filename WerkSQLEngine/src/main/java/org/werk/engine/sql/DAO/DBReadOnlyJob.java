package org.werk.engine.sql.DAO;

import java.util.Collection;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.pillar.db.interfaces.TransactionContext;
import org.pillar.db.interfaces.TransactionFactory;
import org.werk.data.StepPOJO;
import org.werk.engine.sql.SQLWerkJob;
import org.werk.processing.readonly.ReadOnlyJob;

public class DBReadOnlyJob extends DBJobPOJO implements ReadOnlyJob<Long> {
	final Logger logger = Logger.getLogger(SQLWerkJob.class);
	
	protected TransactionFactory transactionFactory;
	protected TransactionContext stepTransactionContext;
	protected StepDAO stepDAO;
	
	public DBReadOnlyJob(DBJobPOJO dbJobPOJO, TransactionFactory transactionFactory, 
			TransactionContext stepTransactionContext, StepDAO stepDAO) {
		super(dbJobPOJO.getJobTypeName(), dbJobPOJO.getVersion(), dbJobPOJO.getJobId(), dbJobPOJO.getJobName(), 
				dbJobPOJO.getParentJobId(), dbJobPOJO.getStepCount(), dbJobPOJO.getCurrentStepId(), 
				dbJobPOJO.getCurrentStepTypeName(), dbJobPOJO.getJobInitialParameters(), dbJobPOJO.getStatus(), 
				dbJobPOJO.getCreationTime(), dbJobPOJO.getNextExecutionTime(), dbJobPOJO.getJobParameters(), 
				dbJobPOJO.getJoinStatusRecord(), dbJobPOJO.getIdLocker());
		this.transactionFactory = transactionFactory;
		this.stepTransactionContext = stepTransactionContext;
		this.stepDAO = stepDAO;
	}
	
	@Override
	public Collection<StepPOJO> getProcessingHistory() throws Exception {
		TransactionContext tc = null;
		try {
			if (stepTransactionContext != null)
				tc = stepTransactionContext;
			else
				tc = transactionFactory.startTransaction();
			
			return stepDAO.getProcessingHistory(tc, jobId, Optional.empty(), Optional.empty(), Optional.empty());
		} catch(Exception e) {
			throw e;
		} finally {
			if (stepTransactionContext != null)
				try { tc.close(); } catch (Exception e) { logger.error("Transaction close error", e); }
		}
	}

	@Override
	public Collection<StepPOJO> getFilteredHistory(String stepTypeName) throws Exception {
		TransactionContext tc = null;
		try {
			if (stepTransactionContext != null)
				tc = stepTransactionContext;
			else
				tc = transactionFactory.startTransaction();
			
			return stepDAO.getProcessingHistory(tc, jobId, Optional.of(stepTypeName), Optional.empty(), Optional.empty());
		} catch(Exception e) {
			throw e;
		} finally {
			if (stepTransactionContext != null)
				try { tc.close(); } catch (Exception e) { logger.error("Transaction close error", e); }
		}
	}

	@Override
	public StepPOJO getStep(long stepNumber) throws Exception {
		TransactionContext tc = null;
		try {
			if (stepTransactionContext != null)
				tc = stepTransactionContext;
			else
				tc = transactionFactory.startTransaction();
			
			Collection<StepPOJO> steps = stepDAO.getProcessingHistory(tc, jobId, Optional.empty(), Optional.of(stepNumber), Optional.empty());
			if ((steps == null) || (steps.isEmpty()))
				return null;
			
			for (StepPOJO step : steps)
				return step;
			
			return null;
		} catch(Exception e) {
			throw e;
		} finally {
			if (stepTransactionContext != null)
				try { tc.close(); } catch (Exception e) { logger.error("Transaction close error", e); }
		}
	}
}
