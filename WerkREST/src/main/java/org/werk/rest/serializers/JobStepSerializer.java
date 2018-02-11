package org.werk.rest.serializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.werk.data.JobPOJO;
import org.werk.data.StepPOJO;
import org.werk.engine.JobIdSerializer;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.NewStepRestartInfo;
import org.werk.meta.impl.JobRestartInfoImpl;
import org.werk.meta.impl.NewStepRestartInfoImpl;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.util.JoinResultSerializer;
import org.werk.util.ParameterContextSerializer;
import org.werk.util.StepProcessingHistorySerializer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JobStepSerializer<J> {
	protected ParameterContextSerializer contextSerializer;
	protected JoinResultSerializer<J> joinResultSerializer;
	protected JobIdSerializer<J> jobIdSerializer;
	protected StepProcessingHistorySerializer stepProcessingHistorySerializer;
	
	public JSONObject serializeJobAndHistory(ReadOnlyJob<J> roJob) throws Exception {
		JSONObject job = serializeJob(roJob);
		
		Collection<StepPOJO> steps = roJob.getProcessingHistory();
		JSONArray history = new JSONArray();
		for (StepPOJO step : steps)
			history.put(serializeStep(step));
		
		job.put("history", history);
		
		return job;
	}
	
	public JSONObject serializeStep(StepPOJO stepPOJO) {
		JSONObject step = new JSONObject();
		
		step.put("stepTypeName", stepPOJO.getStepTypeName());
		step.put("isRollback", stepPOJO.isRollback());
		
		step.put("stepNumber", stepPOJO.getStepNumber());
		step.put("rollbackStepNumbers", stepPOJO.getRollbackStepNumbers());
		step.put("executionCount", stepPOJO.getExecutionCount());
		
		step.put("stepParameters", contextSerializer.serializeParameters(stepPOJO.getStepParameters()));
		step.put("processingLog", stepProcessingHistorySerializer.serializeLog(stepPOJO.getProcessingLog()));
		
		return step;
	}
	
	public JSONObject serializeJob(JobPOJO<J> jobPOJO) {
		JSONObject job = new JSONObject();
		
		job.put("jobTypeName", jobPOJO.getJobTypeName());
		job.put("version", jobPOJO.getVersion());
		
		job.put("jobId", jobIdSerializer.serializeJobId(jobPOJO.getJobId()));
		
		if (jobPOJO.getJobName().isPresent())
			job.put("jobName", jobPOJO.getJobName().get());
		if (jobPOJO.getParentJobId().isPresent())
			job.put("parentJobId", jobPOJO.getParentJobId().get());
		job.put("stepCount", jobPOJO.getStepCount());
		
		job.put("jobInitialParameters", contextSerializer.serializeParameters(jobPOJO.getJobInitialParameters()));
		
		job.put("status", jobPOJO.getStatus());
		job.put("nextExecutionTime", jobPOJO.getNextExecutionTime());
		job.put("jobParameters", contextSerializer.serializeParameters(jobPOJO.getJobParameters()));
		
		if (jobPOJO.getJoinStatusRecord().isPresent())
			job.put("joinStatusRecord", serializeJoinStatusRecord(jobPOJO.getJoinStatusRecord().get()));
			
		return job;
	}
	
	public JSONObject serializeJoinStatusRecord(JoinStatusRecord<J> rec) {
		JSONObject resultJSON = joinResultSerializer.serializeJoinResult(rec);
		resultJSON.put("waitForNJobs", rec.getWaitForNJobs());
		resultJSON.put("joinParameterName", rec.getJoinParameterName());
		return resultJSON;
	}
	
	public JobRestartInfo<J> deserializeJobRestartInfo(JSONObject jobRestartInfoJSON) {
		J jobId = jobIdSerializer.deSerializeJobId(jobRestartInfoJSON.getString("jobId"));
		//-------------------------------
		Map<String, Parameter> jobParametersUpdate = new HashMap<>();
		if (jobRestartInfoJSON.has("jobParametersUpdate"))
			jobParametersUpdate = contextSerializer.deserializeParameters(jobRestartInfoJSON.getJSONObject("jobParametersUpdate"));
		//-------------------------------
		List<String> jobParametersToRemove = new ArrayList<String>();
		if (jobRestartInfoJSON.has("jobParametersToRemove")) {
			JSONArray arr = jobRestartInfoJSON.getJSONArray("jobParametersToRemove");
			for (int i = 0; i < arr.length(); i++)
				jobParametersToRemove.add(arr.getString(i));
		}
		//-------------------------------
		Map<String, Parameter> stepParametersUpdate = new HashMap<>();
		if (jobRestartInfoJSON.has("stepParametersUpdate"))
			stepParametersUpdate = contextSerializer.deserializeParameters(jobRestartInfoJSON.getJSONObject("stepParametersUpdate"));
		//-------------------------------
		List<String> stepParametersToRemove = new ArrayList<String>();
		if (jobRestartInfoJSON.has("stepParametersToRemove")) {
			JSONArray arr = jobRestartInfoJSON.getJSONArray("stepParametersToRemove");
			for (int i = 0; i < arr.length(); i++)
				stepParametersToRemove.add(arr.getString(i));
		}
		//-------------------------------
		Optional<NewStepRestartInfo> newStepInfo = Optional.empty();
		if (jobRestartInfoJSON.has("newStepInfo"))
			newStepInfo = Optional.of(deserializeNewStepRestartInfo(jobRestartInfoJSON.getJSONObject("newStepInfo")));
		//-------------------------------
		Optional<JoinStatusRecord<J>> joinStatusRecord = Optional.empty();
		if (jobRestartInfoJSON.has("joinStatusRecord"))
			joinStatusRecord = Optional.of(deserializeJoinStatusRecord(jobRestartInfoJSON.getJSONObject("joinStatusRecord")));
		
		return new JobRestartInfoImpl<J>(jobId, jobParametersUpdate, jobParametersToRemove, stepParametersUpdate,
				stepParametersToRemove, newStepInfo, joinStatusRecord);
	}
	
	public JoinStatusRecord<J> deserializeJoinStatusRecord(JSONObject joinStatusRecordObj) {
		List<J> joinedJobIds = new ArrayList<J>();
		JSONArray arr = joinStatusRecordObj.getJSONArray("joinedJobIds");
		for (int i = 0; i < arr.length(); i++)
			joinedJobIds.add(jobIdSerializer.deSerializeJobId(arr.getString(i)));
		
		String joinParameterName = joinStatusRecordObj.getString("joinParameterName");
		int waitForNJobs = joinStatusRecordObj.getInt("waitForNJobs");
		
		return new RestartJoinStatusRecord<J>(joinedJobIds, joinParameterName, waitForNJobs);
	}
	
	public NewStepRestartInfo deserializeNewStepRestartInfo(JSONObject newStepRestartInfoObj) {
		String newStepTypeName = newStepRestartInfoObj.getString("newStepTypeName");
		boolean isNewStepRollback = newStepRestartInfoObj.getBoolean("isNewStepRollback");
		
		Optional<List<Integer>> stepsToRollback = Optional.empty();
		if (newStepRestartInfoObj.has("stepsToRollback")) {
			List<Integer> lst = new ArrayList<>();
			JSONArray stepsToRollbackJSON = newStepRestartInfoObj.getJSONArray("stepsToRollback");
			for (int i = 0; i < stepsToRollbackJSON.length(); i++) {
				lst.add(stepsToRollbackJSON.getInt(i));
			}
			stepsToRollback = Optional.of(lst);
		}

		NewStepRestartInfoImpl newStepRestartInfo = new NewStepRestartInfoImpl(newStepTypeName, 
				isNewStepRollback, stepsToRollback);
		return newStepRestartInfo;
	}
}
