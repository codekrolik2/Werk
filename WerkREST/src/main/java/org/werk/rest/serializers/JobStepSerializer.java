package org.werk.rest.serializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.data.JobPOJO;
import org.werk.data.JobPOJOImpl;
import org.werk.data.StepPOJO;
import org.werk.data.StepPOJOImpl;
import org.werk.engine.JobIdSerializer;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.NewStepRestartInfo;
import org.werk.meta.impl.JobRestartInfoImpl;
import org.werk.meta.impl.NewStepRestartInfoImpl;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.jobs.MapJoinStatusRecord;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.processing.readonly.ReadOnlyJobImpl;
import org.werk.processing.steps.StepProcessingLogRecord;
import org.werk.service.JobCollection;
import org.werk.service.PageInfo;
import org.werk.util.JoinResultImpl;
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
	protected PageInfoSerializer pageInfoSerializer;
	protected TimeProvider timeProvider;
	
	public JSONObject serializeJobAndHistory(ReadOnlyJob<J> roJob) throws Exception {
		JSONObject job = serializeJob(roJob);
		
		Collection<StepPOJO> steps = roJob.getProcessingHistory();
		JSONArray history = new JSONArray();
		for (StepPOJO step : steps)
			history.put(serializeStep(step));
		
		job.put("history", history);
		
		return job;
	}
	
	public ReadOnlyJob<J> deserializeJobAndHistory(JSONObject roJobJSON) throws Exception {
		JobPOJO<J> job = deserializeJob(roJobJSON);
		
		List<StepPOJO> steps = new ArrayList<>();
		JSONArray history = roJobJSON.getJSONArray("history");
		for (int i = 0; i < history.length(); i++) {
			JSONObject stepJSON = history.getJSONObject(i);
			StepPOJO step = deserializeStep(stepJSON);
			
			steps.add(step);
		}
		
		String jobTypeName = job.getJobTypeName();
		long version = job.getVersion();
		J jobId = job.getJobId();
		Optional<String> jobName = job.getJobName();
		Optional<J> parentJobId = job.getParentJobId();
		int stepCount = job.getStepCount();
		String currentStepTypeName = job.getCurrentStepTypeName();
		Map<String, Parameter> jobInitialParameters = job.getJobInitialParameters();
		JobStatus status = job.getStatus();
		Timestamp creationTime = job.getCreationTime();
		Timestamp nextExecutionTime = job.getNextExecutionTime();
		Map<String, Parameter> jobParameters = job.getJobParameters();
		Optional<JoinStatusRecord<J>> joinStatusRecord = job.getJoinStatusRecord();
		
		return new ReadOnlyJobImpl<J>(jobTypeName, version, jobId, jobName, parentJobId, stepCount, currentStepTypeName, jobInitialParameters, status,
				creationTime, nextExecutionTime, jobParameters, joinStatusRecord, steps);
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
	
	public StepPOJO deserializeStep(JSONObject stepJSON) {
		String stepTypeName = stepJSON.getString("stepTypeName");
		boolean isRollback = stepJSON.getBoolean("isRollback");
		int stepNumber = stepJSON.getInt("stepNumber");
		
		JSONArray rollbackStepNumbersJSON = stepJSON.getJSONArray("rollbackStepNumbers");
		List<Integer> rollbackStepNumbers = new ArrayList<Integer>();
		for (int i = 0; i < rollbackStepNumbersJSON.length(); i++)
			rollbackStepNumbers.add(rollbackStepNumbersJSON.getInt(i));
		
		int executionCount = stepJSON.getInt("executionCount");
		Map<String, Parameter> stepParameters = 
				contextSerializer.deserializeParameters(stepJSON.getJSONObject("stepParameters"));
		List<StepProcessingLogRecord> processingLog = 
				stepProcessingHistorySerializer.deserializeLog(stepJSON.getJSONObject("processingLog"));
		
		return new StepPOJOImpl(stepTypeName, isRollback, stepNumber, rollbackStepNumbers, 
				executionCount, stepParameters, processingLog);
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
		job.put("currentStepTypeName", jobPOJO.getCurrentStepTypeName());
		
		job.put("jobInitialParameters", contextSerializer.serializeParameters(jobPOJO.getJobInitialParameters()));
		
		job.put("status", jobPOJO.getStatus().toString());
		job.put("creationTime", jobPOJO.getCreationTime().getRawTime());
		job.put("nextExecutionTime", jobPOJO.getNextExecutionTime().getRawTime());
		job.put("jobParameters", contextSerializer.serializeParameters(jobPOJO.getJobParameters()));
		
		if (jobPOJO.getJoinStatusRecord().isPresent())
			job.put("joinStatusRecord", serializeJoinStatusRecord(jobPOJO.getJoinStatusRecord().get()));
			
		return job;
	}
	
	public JobPOJO<J> deserializeJob(JSONObject job) {
		String jobTypeName;
		long version;
		J jobId;
		Optional<String> jobName = Optional.empty();
		Optional<J> parentJobId = Optional.empty();
		int stepCount;
		String currentStepTypeName;
		Map<String, Parameter> jobInitialParameters;
		JobStatus status;
		Timestamp creationTime;
		Timestamp nextExecutionTime;
		Map<String, Parameter> jobParameters;
		Optional<JoinStatusRecord<J>> joinStatusRecord = Optional.empty();
		
		jobTypeName = job.getString("jobTypeName");
		version = job.getLong("version");
		
		jobId = jobIdSerializer.deSerializeJobId(job.getString("jobId"));
		
		if (job.has("jobName"))
			jobName = Optional.of(job.getString("jobName"));
		if (job.has("parentJobId"))
			parentJobId = Optional.of(jobIdSerializer.deSerializeJobId(job.getString("parentJobId")));
		
		stepCount = job.getInt("stepCount");
		currentStepTypeName = job.getString("currentStepTypeName");
		
		jobInitialParameters = contextSerializer.deserializeParameters(job.getJSONObject("jobInitialParameters"));
		
		status = JobStatus.valueOf(job.getString("status"));
		
		creationTime = timeProvider.createTimestamp(job.getString("creationTime"));
		nextExecutionTime = timeProvider.createTimestamp(job.getString("nextExecutionTime"));
		
		jobParameters = contextSerializer.deserializeParameters(job.getJSONObject("jobParameters"));
		
		if (job.has("joinStatusRecord"))
			joinStatusRecord = Optional.of(deserializeMapJoinStatusRecord(job.getJSONObject("joinStatusRecord")));
		
		return new JobPOJOImpl<>(
				jobTypeName, version, jobId, jobName, parentJobId, stepCount, currentStepTypeName, jobInitialParameters, 
				status, creationTime, nextExecutionTime, jobParameters, joinStatusRecord
			);
	}
	
	public JSONObject serializeJoinStatusRecord(JoinStatusRecord<J> rec) {
		JSONObject resultJSON = joinResultSerializer.serializeJoinResult(rec);
		resultJSON.put("waitForNJobs", rec.getWaitForNJobs());
		resultJSON.put("joinParameterName", rec.getJoinParameterName());
		return resultJSON;
	}

	public JoinStatusRecord<J> deserializeMapJoinStatusRecord(JSONObject recJSON) {
		JoinResultImpl<J> joinResult = joinResultSerializer.deserializeJoinResult(recJSON);

		int waitForNJobs = recJSON.getInt("waitForNJobs");
		String joinParameterName = recJSON.getString("joinParameterName");
		
		return new MapJoinStatusRecord<J>(joinResult.getJoinedJobs(), joinParameterName, waitForNJobs);
	}
	
	public JSONObject serializeJobRestartInfo(JobRestartInfo<J> jobRestartInfo) throws Exception {
		JSONObject jobRestartInfoJSON = new JSONObject();
		jobRestartInfoJSON.put("jobId", jobIdSerializer.serializeJobId(jobRestartInfo.getJobId()));
		
		jobRestartInfoJSON.put("jobParametersUpdate", 
				contextSerializer.serializeParameters(jobRestartInfo.getJobParametersUpdate()));

		JSONArray jobParametersToRemoveArr = new JSONArray();
		for (String prmName : jobRestartInfo.getJobParametersToRemove())
			jobParametersToRemoveArr.put(prmName);
		jobRestartInfoJSON.put("jobParametersToRemove", jobParametersToRemoveArr);
		
		jobRestartInfoJSON.put("stepParametersUpdate",
				contextSerializer.serializeParameters(jobRestartInfo.getJobParametersUpdate()));
		
		JSONArray stepParametersToRemoveArr = new JSONArray();
		for (String prmName : jobRestartInfo.getStepParametersToRemove())
			stepParametersToRemoveArr.put(prmName);
		jobRestartInfoJSON.put("stepParametersToRemove", stepParametersToRemoveArr);
		
		if (jobRestartInfo.getNewStepInfo().isPresent())
			jobRestartInfoJSON.put("newStepInfo", serializeNewStepRestartInfo(jobRestartInfo.getNewStepInfo().get()));
		
		if (jobRestartInfo.getJoinStatusRecord().isPresent())
			jobRestartInfoJSON.put("joinStatusRecord", serializeJoinStatusRecord(jobRestartInfo.getJoinStatusRecord().get()));
		
		return jobRestartInfoJSON;
	}
	
	public JobRestartInfo<J> deserializeJobRestartInfo(JSONObject jobRestartInfoJSON) {
		J jobId = jobIdSerializer.deSerializeJobId(jobRestartInfoJSON.getString("jobId"));
		//-------------------------------
		Map<String, Parameter> jobInitParametersUpdate = new HashMap<>();
		if (jobRestartInfoJSON.has("jobInitParametersUpdate"))
			jobInitParametersUpdate = contextSerializer.deserializeParameters(jobRestartInfoJSON.getJSONObject("jobInitParametersUpdate"));
		//-------------------------------
		List<String> jobInitParametersToRemove = new ArrayList<String>();
		if (jobRestartInfoJSON.has("jobInitParametersToRemove")) {
			JSONArray arr = jobRestartInfoJSON.getJSONArray("jobInitParametersToRemove");
			for (int i = 0; i < arr.length(); i++)
				jobInitParametersToRemove.add(arr.getString(i));
		}
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
		
		return new JobRestartInfoImpl<J>(jobId, jobInitParametersUpdate, jobInitParametersToRemove, jobParametersUpdate, 
				jobParametersToRemove, stepParametersUpdate, stepParametersToRemove, newStepInfo, joinStatusRecord);
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
	
	public JSONObject serializeNewStepRestartInfo(NewStepRestartInfo newStepRestartInfo) {
		JSONObject newStepRestartInfoObj = new JSONObject();
		
		newStepRestartInfoObj.put("newStepTypeName", newStepRestartInfo.getNewStepTypeName());
		newStepRestartInfoObj.put("isNewStepRollback", newStepRestartInfo.isNewStepRollback());
		
		if (newStepRestartInfo.getStepsToRollback().isPresent()) {
			JSONArray stepsToRollbackJSON = new JSONArray();
			for (int step : newStepRestartInfo.getStepsToRollback().get())
				stepsToRollbackJSON.put(step);
			newStepRestartInfoObj.put("stepsToRollback", stepsToRollbackJSON);
		}
		
		return newStepRestartInfoObj;
	}
	
	public JSONObject serializeJobCollection(JobCollection<J> jobCollection) {
		JSONObject jobCollectionJSON = new JSONObject();
		
		JSONArray arr = new JSONArray();
		if ((jobCollection.getJobs() != null) && (!jobCollection.getJobs().isEmpty()))
			for (JobPOJO<J> job : jobCollection.getJobs())
				arr.put(serializeJob(job));
		jobCollectionJSON.put("jobs", arr);
		
		if (jobCollection.getPageInfo().isPresent())
			jobCollectionJSON.put("pageInfo", 
					pageInfoSerializer.serializePageInfo(jobCollection.getPageInfo().get()));
		
		jobCollectionJSON.put("jobCount", jobCollection.getJobCount());
		
		return jobCollectionJSON;
	}
	
	public JobCollection<J> deserializeJobCollection(JSONObject jobCollectionJSON) {
		Optional<PageInfo> pageInfo = Optional.empty();
		if (jobCollectionJSON.has("pageInfo"))
			pageInfo = Optional.of(
					pageInfoSerializer.deserializePageInfo(jobCollectionJSON.getJSONObject("pageInfo"))
				);
		
		List<JobPOJO<J>> jobs = new ArrayList<>();
		JSONArray jobsJSONArray = jobCollectionJSON.getJSONArray("jobs");
		for (int i = 0; i < jobsJSONArray.length(); i++) {
			JSONObject jobJSON = jobsJSONArray.getJSONObject(i);
			jobs.add(deserializeJob(jobJSON));
		}
		
		long jobCount = jobCollectionJSON.getLong("jobCount");
		
		return new JobCollection<J>(pageInfo, jobs, jobCount);
	}
}
