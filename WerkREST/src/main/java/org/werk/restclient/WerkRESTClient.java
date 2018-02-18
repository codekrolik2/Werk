package org.werk.restclient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pillar.time.interfaces.Timestamp;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.meta.VersionJobInitInfo;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.rest.JobFilters;
import org.werk.rest.serializers.JobFiltersSerializer;
import org.werk.rest.serializers.JobInitInfoSerializer;
import org.werk.rest.serializers.JobStepSerializer;
import org.werk.rest.serializers.JobStepTypeRESTSerializer;
import org.werk.service.JobCollection;
import org.werk.service.PageInfo;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.Getter;

public class WerkRESTClient {
	final Logger logger = Logger.getLogger(WerkRESTClient.class);
	
	protected JobStepSerializer<Long> jobStepSerializer;
	protected JobInitInfoSerializer jobInitInfoSerializer;
	protected JobStepTypeRESTSerializer<Long> jobStepTypeRESTSerializer;
	
	@Getter
	protected WebClient client;
	
	@Getter
	protected String host;
	@Getter
	protected int port;
	
	protected JobFiltersSerializer<Long> jobFiltersSerializer;
	
	public WerkRESTClient(String host, int port, Vertx vertx) {
		this.host = host;
		this.port = port;
		
		WebClientOptions options = new WebClientOptions()
				  .setUserAgent("WerkRESTClient/0.0.1");
		options.setKeepAlive(false);
		client = WebClient.create(vertx);
	}
	
	public void createJob(Callback<Long> callback, JobInitInfo init) throws Exception {
		String str = jobInitInfoSerializer.deserializeJobInitInfo(init).toString();
		Buffer buffer = Buffer.buffer(str);
		
		sendCreateJobRequest(callback, buffer);
	}
	
	public void createJobOfVersion(Callback<Long> callback, VersionJobInitInfo init) throws Exception {
		String str = jobInitInfoSerializer.deserializeVersionJobInitInfo(init).toString();
		Buffer buffer = Buffer.buffer(str);
		
		sendCreateJobRequest(callback, buffer);
	}
	
	public void sendCreateJobRequest(Callback<Long> callback, Buffer buffer) throws Exception {
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
				logger.error("createJob Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}

	public void restartJob(Callback<Long> callback, JobRestartInfo<Long> jobRestartInfo) throws Exception {
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
				logger.error("restartJob Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}

	public void getJobAndHistory(Callback<ReadOnlyJob<Long>> callback, Long jobId) throws Exception {
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
				logger.error("getJobAndHistory Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}

	public void getJobs(Callback<JobCollection<Long>> callback,
			Optional<Timestamp> from, Optional<Timestamp> to, 
			Optional<Timestamp> fromExec, Optional<Timestamp> toExec, 
			Optional<Map<String, Long>> jobTypesAndVersions,
			Optional<Collection<Long>> parentJobIds, 
			Optional<Collection<Long>> jobIds,
			Optional<Set<String>> currentStepTypes, 
			Optional<PageInfo> pageInfo) throws Exception {
		// Send a GET request
		HttpRequest<Buffer> r = client.get(port, host, "/jobs");
		
		if (from.isPresent() || to.isPresent() || fromExec.isPresent() || toExec.isPresent() ||
				jobTypesAndVersions.isPresent() || parentJobIds.isPresent() || jobIds.isPresent() ||
				currentStepTypes.isPresent() || pageInfo.isPresent()) {
			JobFilters<Long> jobFilters = new JobFilters<>(from, to, fromExec, toExec, jobTypesAndVersions,
					parentJobIds, jobIds, currentStepTypes, pageInfo);
			
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
				logger.error("getJobs Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}

	public void getJobTypes(Callback<Collection<JobType>> callback) {
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
				logger.error("getJobTypes Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}

	public void getJobType(Callback<JobType> callback, String jobTypeName, Optional<Long> version) {
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
				logger.error("getJobType Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}

	public void getAllStepTypes(Callback<Collection<StepType<Long>>> callback) {
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
				logger.error("getAllStepTypes Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}

	public void getStepTypesForJob(Callback<Collection<StepType<Long>>> callback, String jobTypeName, Optional<Long> version) {
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
				logger.error("getStepTypesForJob Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}

	public void getStepType(Callback<StepType<Long>> callback, String stepTypeName) {
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
				logger.error("getStepType Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}

	public void jobsAdded(Callback<Object> callback) {
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
				logger.error("jobsAdded Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}
	
	public void getServerInfo(Callback<JSONObject> callback) {
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
				logger.error("getServerInfo Exception ", ar.cause());
				callback.error(ar.cause());
			}
		});
	}
}
