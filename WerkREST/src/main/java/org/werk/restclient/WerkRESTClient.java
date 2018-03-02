package org.werk.restclient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pillar.time.LongTimeProvider;
import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.engine.JobIdSerializer;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.JobType;
import org.werk.meta.JobTypeSignature;
import org.werk.meta.StepType;
import org.werk.meta.VersionJobInitInfo;
import org.werk.processing.jobs.JobStatus;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.rest.JobFilters;
import org.werk.rest.serializers.JobFiltersSerializer;
import org.werk.rest.serializers.JobInitInfoSerializer;
import org.werk.rest.serializers.JobStepSerializer;
import org.werk.rest.serializers.JobStepTypeRESTSerializer;
import org.werk.rest.serializers.PageInfoSerializer;
import org.werk.service.JobCollection;
import org.werk.service.PageInfo;
import org.werk.util.JoinResultSerializer;
import org.werk.util.LongJobIdSerializer;
import org.werk.util.ParameterContextSerializer;
import org.werk.util.StepProcessingHistorySerializer;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.Getter;

public class WerkRESTClient {
	static final Logger logger = Logger.getLogger(WerkRESTClient.class);
	
	public static WerkRESTClient create(Vertx vertx) {
		TimeProvider timeProvider = new LongTimeProvider();
		JobIdSerializer<Long> jobIdSerializer = new LongJobIdSerializer();
		JoinResultSerializer<Long> joinResultSerializer = new JoinResultSerializer<>(jobIdSerializer);
		ParameterContextSerializer parameterContextSerializer = new ParameterContextSerializer();
		PageInfoSerializer pageInfoSerializer = new PageInfoSerializer();
		StepProcessingHistorySerializer stepProcessingHistorySerializer = new StepProcessingHistorySerializer(timeProvider);
		
		JobStepSerializer<Long> jobStepSerializer = new JobStepSerializer<Long>(parameterContextSerializer, joinResultSerializer, 
				jobIdSerializer, stepProcessingHistorySerializer, pageInfoSerializer, timeProvider);
		JobInitInfoSerializer jobInitInfoSerializer = new JobInitInfoSerializer(parameterContextSerializer, timeProvider);
		JobStepTypeRESTSerializer<Long> jobStepTypeRESTSerializer = new JobStepTypeRESTSerializer<Long>(parameterContextSerializer);
		
		JobFiltersSerializer<Long> jobFiltersSerializer =
				new JobFiltersSerializer<>(timeProvider, jobIdSerializer, pageInfoSerializer);
		
		return new WerkRESTClient(vertx,
				jobStepSerializer, jobInitInfoSerializer, jobStepTypeRESTSerializer, jobFiltersSerializer);
	}
	
	@Getter
	protected WebClient client;
	
	protected JobStepSerializer<Long> jobStepSerializer;
	protected JobInitInfoSerializer jobInitInfoSerializer;
	protected JobStepTypeRESTSerializer<Long> jobStepTypeRESTSerializer;
	protected JobFiltersSerializer<Long> jobFiltersSerializer;
	
	public WerkRESTClient(Vertx vertx,
			JobStepSerializer<Long> jobStepSerializer,
			JobInitInfoSerializer jobInitInfoSerializer,
			JobStepTypeRESTSerializer<Long> jobStepTypeRESTSerializer,
			JobFiltersSerializer<Long> jobFiltersSerializer) {
		this.jobStepSerializer = jobStepSerializer;
		this.jobInitInfoSerializer = jobInitInfoSerializer;
		this.jobStepTypeRESTSerializer = jobStepTypeRESTSerializer;
		this.jobFiltersSerializer = jobFiltersSerializer;
		
		WebClientOptions options = new WebClientOptions()
				  .setUserAgent("WerkRESTClient/0.0.1");
		options.setKeepAlive(false);
		client = WebClient.create(vertx);
	}
	
	public void createJob(String host, int port, WerkCallback<Long> callback, JobInitInfo init) throws Exception {
		String str = jobInitInfoSerializer.serializeJobInitInfo(init).toString();
		Buffer buffer = Buffer.buffer(str);
		
		sendCreateJobRequest(host, port, callback, buffer);
	}
	
	public void createJobOfVersion(String host, int port, WerkCallback<Long> callback, VersionJobInitInfo init) throws Exception {
		String str = jobInitInfoSerializer.serializeVersionJobInitInfo(init).toString();
		Buffer buffer = Buffer.buffer(str);
		
		sendCreateJobRequest(host, port, callback, buffer);
	}
	
	public void sendCreateJobRequest(String host, int port, WerkCallback<Long> callback, Buffer buffer) throws Exception {
		client.post(port, host, "/jobs").sendBuffer(buffer, ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();

					logger.info("createJob: received response with status code" + response.statusCode());

					String bodyStr = response.bodyAsString();
					JSONObject bodyJSON = new JSONObject(bodyStr);

					long jobId = bodyJSON.getLong("jobId");
					callback.done(jobId);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("createJob Exception ", e);
				callback.error(e);
			}
		});
	}

	public void restartJob(String host, int port, WerkCallback<Long> callback, JobRestartInfo<Long> jobRestartInfo) throws Exception {
		String str = jobStepSerializer.serializeJobRestartInfo(jobRestartInfo).toString();
		Buffer buffer = Buffer.buffer(str);
		
		client.patch(port, host, "/jobs").sendBuffer(buffer, ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("restartJob: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject bodyJSON = new JSONObject(bodyStr);
					
					long jobId = bodyJSON.getLong("jobId");
					callback.done(jobId);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("restartJob Exception ", e);
				callback.error(e);
			}
		});
	}

	public void getJobAndHistory(String host, int port, WerkCallback<ReadOnlyJob<Long>> callback, Long jobId) throws Exception {
		// Send a GET request
		client.get(port, host, "/jobs/"+jobId)
		.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getJobAndHistory: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject bodyJSON = new JSONObject(bodyStr);
					
					ReadOnlyJob<Long> job = jobStepSerializer.deserializeJobAndHistory(bodyJSON);
					callback.done(job);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getJobAndHistory Exception ", e);
				callback.error(e);
			}
		});
	}

	public void getJobs(String host, int port, WerkCallback<JobCollection<Long>> callback,
			Optional<Timestamp> from, Optional<Timestamp> to, 
			Optional<Timestamp> fromExec, Optional<Timestamp> toExec, 
			Optional<List<JobTypeSignature>> jobTypesAndVersions,
			Optional<Collection<Long>> parentJobIds, 
			Optional<Collection<Long>> jobIds,
			Optional<Set<String>> currentStepTypes, 
			Optional<Set<JobStatus>> jobStatuses,
			Optional<PageInfo> pageInfo) throws Exception {
		// Send a GET request
		HttpRequest<Buffer> r = client.get(port, host, "/jobs");
		
		if (from.isPresent() || to.isPresent() || fromExec.isPresent() || toExec.isPresent() ||
				jobTypesAndVersions.isPresent() || parentJobIds.isPresent() || jobIds.isPresent() ||
				currentStepTypes.isPresent() || pageInfo.isPresent()) {
			JobFilters<Long> jobFilters = new JobFilters<>(from, to, fromExec, toExec, jobTypesAndVersions,
					parentJobIds, jobIds, currentStepTypes, jobStatuses, pageInfo);
			
			JSONObject jobFiltersJSON = jobFiltersSerializer.serializeJobFilters(jobFilters);
			
			r.addQueryParam("filter", jobFiltersJSON.toString());
		}
		
		r.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getJobs: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject bodyJSON = new JSONObject(bodyStr);
					
					JobCollection<Long> jobCollection = jobStepSerializer.deserializeJobCollection(bodyJSON);
					callback.done(jobCollection);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getJobs Exception ", e);
				callback.error(e);
			}
		});
	}

	public void getJobTypes(String host, int port, WerkCallback<Collection<JobType>> callback) {
		// Send a GET request
		HttpRequest<Buffer> r = client.get(port, host, "/jobTypes");
		
		r.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getJobTypes: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject bodyJSON = new JSONObject(bodyStr);
					
					List<JobType> jobTypes = new ArrayList<>();
					JSONArray jobTypesArray = bodyJSON.getJSONArray("jobTypes");
					for (int i = 0; i < jobTypesArray.length(); i++) {
						JSONObject jobTypeObject = jobTypesArray.getJSONObject(i);
						JobType jobType = jobStepTypeRESTSerializer.deserializeJobType(jobTypeObject);
						jobTypes.add(jobType);
					}
					
					callback.done(jobTypes);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getJobTypes Exception ", e);
				callback.error(e);
			}
		});
	}

	public void getJobTypesForStep(String host, int port, WerkCallback<Collection<JobType>> callback, String stepType) {
		// Send a GET request
		HttpRequest<Buffer> r = client.get(port, host, "/jobTypesForStep/" + stepType);
		
		r.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getJobTypesForStep: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject bodyJSON = new JSONObject(bodyStr);
					
					List<JobType> jobTypes = new ArrayList<>();
					JSONArray jobTypesArray = bodyJSON.getJSONArray("jobTypes");
					for (int i = 0; i < jobTypesArray.length(); i++) {
						JSONObject jobTypeObject = jobTypesArray.getJSONObject(i);
						JobType jobType = jobStepTypeRESTSerializer.deserializeJobType(jobTypeObject);
						jobTypes.add(jobType);
					}
					
					callback.done(jobTypes);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getJobTypesForStep Exception ", e);
				callback.error(e);
			}
		});
	}

	public void getJobType(String host, int port, WerkCallback<JobType> callback, String jobTypeName, Optional<Long> version) {
		// Send a GET request
		String path = "/jobTypes/" + jobTypeName;
		if (version.isPresent())
			path += "/" + version.get();
		
		HttpRequest<Buffer> r = client.get(port, host, path);
		
		r.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getJobType: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject jobTypeObject = new JSONObject(bodyStr);
					JobType jobType = jobStepTypeRESTSerializer.deserializeJobType(jobTypeObject);
					
					callback.done(jobType);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getJobType Exception ", e);
				callback.error(e);
			}
		});
	}

	public void getStepTransitions(String host, int port, WerkCallback<Collection<StepType<Long>>> callback, String stepTypeName) {
		// Send a GET request
		client.get(port, host, "/stepTransitions/"+stepTypeName)
		.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getAllStepTypes: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject bodyJSON = new JSONObject(bodyStr);
					
					List<StepType<Long>> stepTypes = new ArrayList<>();
					JSONArray stepTypesArr = bodyJSON.getJSONArray("stepTypes");
					for (int i = 0; i < stepTypesArr.length(); i++) {
						JSONObject stepTypeObject = stepTypesArr.getJSONObject(i);
						StepType<Long> stepType = jobStepTypeRESTSerializer.deserializeStepType(stepTypeObject);
						stepTypes.add(stepType);
					}
					
					callback.done(stepTypes);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getAllStepTypes Exception ", e);
				callback.error(e);
			}
		});
	}

	public void getStepRollbackTransitions(String host, int port, WerkCallback<Collection<StepType<Long>>> callback, String stepTypeName) {
		// Send a GET request
		client.get(port, host, "/stepRollbackTransitions/" + stepTypeName)
		.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getAllStepTypes: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject bodyJSON = new JSONObject(bodyStr);
					
					List<StepType<Long>> stepTypes = new ArrayList<>();
					JSONArray stepTypesArr = bodyJSON.getJSONArray("stepTypes");
					for (int i = 0; i < stepTypesArr.length(); i++) {
						JSONObject stepTypeObject = stepTypesArr.getJSONObject(i);
						StepType<Long> stepType = jobStepTypeRESTSerializer.deserializeStepType(stepTypeObject);
						stepTypes.add(stepType);
					}
					
					callback.done(stepTypes);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getAllStepTypes Exception ", e);
				callback.error(e);
			}
		});
	}

	public void getAllStepTypes(String host, int port, WerkCallback<Collection<StepType<Long>>> callback) {
		// Send a GET request
		client.get(port, host, "/stepTypes")
		.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getAllStepTypes: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject bodyJSON = new JSONObject(bodyStr);
					
					List<StepType<Long>> stepTypes = new ArrayList<>();
					JSONArray stepTypesArr = bodyJSON.getJSONArray("stepTypes");
					for (int i = 0; i < stepTypesArr.length(); i++) {
						JSONObject stepTypeObject = stepTypesArr.getJSONObject(i);
						StepType<Long> stepType = jobStepTypeRESTSerializer.deserializeStepType(stepTypeObject);
						stepTypes.add(stepType);
					}
					
					callback.done(stepTypes);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getAllStepTypes Exception ", e);
				callback.error(e);
			}
		});
	}

	public void getStepTypesForJob(String host, int port, WerkCallback<Collection<StepType<Long>>> callback, 
			String jobTypeName, Optional<Long> version) {
		// Send a GET request
		String path = "/stepTypesForJob/" + jobTypeName;
		if (version.isPresent())
			path += "/" + version.get();
		
		client.get(port, host, path)
		.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getStepTypesForJob: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject bodyJSON = new JSONObject(bodyStr);
					
					List<StepType<Long>> stepTypes = new ArrayList<>();
					JSONArray stepTypesArr = bodyJSON.getJSONArray("stepTypes");
					for (int i = 0; i < stepTypesArr.length(); i++) {
						JSONObject stepTypeObject = stepTypesArr.getJSONObject(i);
						StepType<Long> stepType = jobStepTypeRESTSerializer.deserializeStepType(stepTypeObject);
						stepTypes.add(stepType);
					}
					
					callback.done(stepTypes);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getStepTypesForJob Exception ", e);
				callback.error(e);
			}
		});
	}

	public void getStepType(String host, int port, WerkCallback<StepType<Long>> callback, String stepTypeName) {
		// Send a GET request
		client.get(port, host, "/stepTypes/" + stepTypeName)
		.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getStepType: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject stepTypeObject = new JSONObject(bodyStr);
					
					StepType<Long> stepType = jobStepTypeRESTSerializer.deserializeStepType(stepTypeObject);
					callback.done(stepType);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getStepType Exception ", e);
				callback.error(e);
			}
		});
	}

	public void jobsAdded(String host, int port, WerkCallback<Object> callback) {
		// Send a GET request
		client.get(port, host, "/jobsAdded")
		.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("jobsAdded: received response with status code" + response.statusCode());
					callback.done(null);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("jobsAdded Exception ", e);
				callback.error(e);
			}
		});
	}
	
	public void getServerInfo(String host, int port, WerkCallback<JSONObject> callback) {
		// Send a GET request
		client.get(port, host, "/serverInfo")
		.send(ar -> {
			try {
				if (ar.succeeded()) {
					// Obtain response
					HttpResponse<Buffer> response = ar.result();
					
					logger.info("getServerInfo: received response with status code" + response.statusCode());
					
					String bodyStr = response.bodyAsString();
					JSONObject serverInfoObject = new JSONObject(bodyStr);
					
					callback.done(serverInfoObject);
				} else
					throw ar.cause();
			} catch(Throwable e) {
				logger.error("getServerInfo Exception ", e);
				callback.error(e);
			}
		});
	}
}
