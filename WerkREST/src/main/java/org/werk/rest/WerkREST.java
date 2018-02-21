package org.werk.rest;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pillar.time.interfaces.TimeProvider;
import org.werk.engine.JobIdSerializer;
import org.werk.meta.JobInitInfo;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.meta.VersionJobInitInfo;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.rest.serializers.JobFiltersSerializer;
import org.werk.rest.serializers.JobInitInfoSerializer;
import org.werk.rest.serializers.JobStepSerializer;
import org.werk.rest.serializers.JobStepTypeRESTSerializer;
import org.werk.rest.serializers.PageInfoSerializer;
import org.werk.service.JobCollection;
import org.werk.service.WerkService;
import org.werk.util.JoinResultSerializer;
import org.werk.util.LongJobIdSerializer;
import org.werk.util.ParameterContextSerializer;
import org.werk.util.StepProcessingHistorySerializer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class WerkREST extends AbstractVerticle {
	final Logger logger = Logger.getLogger(WerkREST.class);
	
	protected WerkService<Long> werkService;
	protected JobIdSerializer<Long> jobIdSerializer; 
	
	protected JobStepTypeRESTSerializer<Long> jobStepTypeRESTSerializer;
	protected JobStepSerializer<Long> jobStepSerializer;
	protected JobFiltersSerializer<Long> jobFiltersSerializer;
	protected JobInitInfoSerializer jobInitInfoSerializer;
	
	protected boolean allowUnfilteredJobListRetrieval;
	
	public WerkREST(WerkService<Long> werkService, TimeProvider timeProvider, boolean allowUnfilteredJobListRetrieval) {
		this.werkService = werkService;
		this.allowUnfilteredJobListRetrieval = allowUnfilteredJobListRetrieval;
		
		jobIdSerializer = new LongJobIdSerializer();
		
		PageInfoSerializer pageInfoSerializer = new PageInfoSerializer();
		
		jobStepTypeRESTSerializer = new JobStepTypeRESTSerializer<Long>();
		
		ParameterContextSerializer contextSerializer = new ParameterContextSerializer();
		JoinResultSerializer<Long> joinResultSerializer = new JoinResultSerializer<>(jobIdSerializer);
		StepProcessingHistorySerializer stepProcessingHistorySerializer = 
				new StepProcessingHistorySerializer(timeProvider);

		jobStepSerializer = new JobStepSerializer<>(contextSerializer, joinResultSerializer, 
				jobIdSerializer, stepProcessingHistorySerializer, pageInfoSerializer, timeProvider);
		
		jobFiltersSerializer = new JobFiltersSerializer<Long>(timeProvider, jobIdSerializer, pageInfoSerializer);
		
		jobInitInfoSerializer = new JobInitInfoSerializer(contextSerializer, timeProvider);
	}
	
	@Override
	public void start() {
		Router router = Router.router(vertx);
		
		router.route().handler(BodyHandler.create());
		
		router.get("/jobTypes/:jobTypeName/:version").handler(this::handleGetJobType);
		router.get("/jobTypes/:jobTypeName").handler(this::handleGetJobType);
		router.get("/jobTypes").handler(this::handleListJobTypes);
		
		router.get("/stepTypes").handler(this::handleGetAllStepTypes);
		router.get("/stepTypes/:stepTypeName").handler(this::handleGetStepType);
		router.get("/stepTypesForJob/:jobTypeName/:version").handler(this::handleListStepTypes);
		router.get("/stepTypesForJob/:jobTypeName").handler(this::handleListStepTypes);
		
		router.get("/jobsAndHistory/:jobId").handler(this::handleJobsAndHistory);
		
		//Get jobs - JSON filter definition in request body
		router.get("/jobs").handler(this::handleGetJobs);
		//Create job (current or old version)
		router.post("/jobs").handler(this::handleCreateJob);
		//Restart job
		router.patch("/jobs/:jobId").handler(this::handleRestartJob);
		
		router.get("/jobsAdded").handler(this::handleJobsAdded);
		router.get("/serverInfo").handler(this::handleServerInfo);
		
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
	
	private void handleJobsAdded(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		try {
			werkService.jobsAdded();
			response.setStatusCode(200).end();
		} catch (Exception e) {
			logger.error(e, e);
			sendStatus(400, response);
			return;
		}
	}
	
	private void handleServerInfo(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		try {
			JSONObject info = werkService.getServerInfo();
			response.setStatusCode(200).end(info.toString());
		} catch (Exception e) {
			logger.error(e, e);
			sendStatus(400, response);
			return;
		}
	}
	
	private void handleRestartJob(RoutingContext routingContext) {
		String body = routingContext.getBodyAsString();
		HttpServerResponse response = routingContext.response();
		if ((body == null) || (body.trim().equals(""))) {
			sendStatus(400, response);
		} else {
			JSONObject jobRestartJSON = new JSONObject(body);
			JobRestartInfo<Long> jobRestartInfo = jobStepSerializer.deserializeJobRestartInfo(jobRestartJSON);
			try {
				werkService.restartJob(jobRestartInfo);
				
				JSONObject resp = new JSONObject();
				resp.put("jobId", jobRestartInfo.getJobId());
				routingContext.response().putHeader("content-type", "application/json").end(resp.toString());
			} catch (Exception e) {
				logger.error(e, e);
				sendStatus(400, response);
				return;
			}
		}
	}
	
	private void handleCreateJob(RoutingContext routingContext) {
		String body = routingContext.getBodyAsString();
		HttpServerResponse response = routingContext.response();
		if ((body == null) || (body.trim().equals(""))) {
			sendStatus(400, response);
		} else {
			try {
				Long jobId;
				JSONObject jobInitJSON = new JSONObject(body);
				if (jobInitJSON.has("jobVersion")) {
					VersionJobInitInfo versionJobInitInfo = jobInitInfoSerializer.deserializeVersionJobInitInfo(jobInitJSON);
					jobId = werkService.createJobOfVersion(versionJobInitInfo);
				} else {
					JobInitInfo jobInitInfo = jobInitInfoSerializer.deserializeJobInitInfo(jobInitJSON);
					jobId = werkService.createJob(jobInitInfo);
				}
				
				JSONObject resp = new JSONObject();
				resp.put("jobId", jobId);
				routingContext.response().putHeader("content-type", "application/json").end(resp.toString());
			} catch (Exception e) {
				logger.error(e, e);
				sendStatus(400, response);
				return;
			}
		}
	}
	
	private void handleGetJobs(RoutingContext routingContext) {
		String filter = routingContext.queryParams().get("filter");
		HttpServerResponse response = routingContext.response();
		if ((filter == null) || (filter.trim().equals(""))) {
			sendStatus(400, response);
		} else {
			JobFilters<Long> jobFilters = jobFiltersSerializer.deserializeJobFilters(new JSONObject(filter));
			
			if (!allowUnfilteredJobListRetrieval)
			if (!jobFilters.getFrom().isPresent() && !jobFilters.getTo().isPresent() &&
				!jobFilters.getJobTypesAndVersions().isPresent() && !jobFilters.getParentJobIds().isPresent() &&
				!jobFilters.getJobIds().isPresent()) {
				logger.error("Unfiltered JobList Retrieval is not allowed: specify filters");
				sendStatus(400, "Unfiltered JobList Retrieval is not allowed: specify filters", response);
				return;
			}

			try {
				JobCollection<Long> jobs = werkService.getJobs(jobFilters.getFrom(), jobFilters.getTo(), 
						jobFilters.getFromExec(), jobFilters.getToExec(),
						jobFilters.getJobTypesAndVersions(), jobFilters.getParentJobIds(), jobFilters.getJobIds(),
						jobFilters.getCurrentStepTypes(), jobFilters.getPageInfo());
				
				JSONObject jsonObject = jobStepSerializer.serializeJobCollection(jobs);
				
				routingContext.response().putHeader("content-type", "application/json").end(jsonObject.toString());
			} catch (Exception e) {
				logger.error(e, e);
				sendStatus(400, response);
				return;
			}
		}
	}
	
	private void handleJobsAndHistory(RoutingContext routingContext) {
		String jobIdStr = routingContext.request().getParam("jobId");
		
		HttpServerResponse response = routingContext.response();
		if (jobIdStr == null) {
			sendStatus(400, response);
		} else {
			long jobId = jobIdSerializer.deSerializeJobId(jobIdStr);
			try {
				ReadOnlyJob<Long> readOnlyJob = werkService.getJobAndHistory(jobId);
				
				if (readOnlyJob == null) {
					sendStatus(404, response);
				} else {
					routingContext.response().putHeader("content-type", "application/json").
						end(jobStepSerializer.serializeJobAndHistory(readOnlyJob).toString());
				}
			} catch (Exception e) {
				logger.error(e, e);
				sendStatus(400, response);
				return;
			}
		}
	}
	
	private void handleGetJobType(RoutingContext routingContext) {
		String jobTypeName = routingContext.request().getParam("jobTypeName");
		String versionStr = routingContext.request().getParam("version");
		
		HttpServerResponse response = routingContext.response();
		if (jobTypeName == null) {
			sendStatus(400, response);
		} else {
			Optional<Long> version = Optional.empty();
			if (versionStr != null) {
				try {
					long v = Long.parseLong(versionStr);
					version = Optional.of(v);
				} catch (NumberFormatException nfe) {
					logger.error(nfe, nfe);
					sendStatus(400, response);
					return;
				}
			}
			
			JobType jobType = werkService.getJobType(jobTypeName, version);
			if (jobType == null) {
				sendStatus(404, response);
			} else {
				response.putHeader("content-type", "application/json").
					end(jobStepTypeRESTSerializer.serializeJobType(jobType).toString());
			}
		}
	}

	private void handleListJobTypes(RoutingContext routingContext) {
		JSONArray arr = new JSONArray();
		for (JobType jobType : werkService.getJobTypes())
			arr.put(jobStepTypeRESTSerializer.serializeJobType(jobType));
		
		JSONObject obj = new JSONObject();
		obj.put("jobTypes", arr);
		
		routingContext.response().putHeader("content-type", "application/json").end(obj.toString());
	}

	private void handleGetStepType(RoutingContext routingContext) {
		String stepTypeName = routingContext.request().getParam("stepTypeName");
		
		HttpServerResponse response = routingContext.response();
		if (stepTypeName == null) {
			sendStatus(400, response);
		} else {
			StepType<?> stepType = werkService.getStepType(stepTypeName);
			
			routingContext.response().putHeader("content-type", "application/json").
				end(jobStepTypeRESTSerializer.serializeStepType(stepType).toString());
		}
	}

	private void handleListStepTypes(RoutingContext routingContext) {
		String jobTypeName = routingContext.request().getParam("jobTypeName");
		String versionStr = routingContext.request().getParam("version");
		
		HttpServerResponse response = routingContext.response();
		if (jobTypeName == null) {
			sendStatus(400, response);
		} else {
			Optional<Long> version = Optional.empty();
			if (versionStr != null) {
				try {
					long v = Long.parseLong(versionStr);
					version = Optional.of(v);
				} catch (NumberFormatException nfe) {
					logger.error(nfe, nfe);
					sendStatus(400, response);
					return;
				}
			}

			JSONArray arr = new JSONArray();
			for (StepType<?> stepType : werkService.getStepTypesForJob(jobTypeName, version))
				arr.put(jobStepTypeRESTSerializer.serializeStepType(stepType));
			
			JSONObject obj = new JSONObject();
			obj.put("stepTypes", arr);
			
			routingContext.response().putHeader("content-type", "application/json").end(obj.toString());
		}
	}
	
	private void handleGetAllStepTypes(RoutingContext routingContext) {
		JSONArray arr = new JSONArray();
		for (StepType<?> stepType : werkService.getAllStepTypes())
			arr.put(jobStepTypeRESTSerializer.serializeStepType(stepType));
		
		JSONObject obj = new JSONObject();
		obj.put("stepTypes", arr);
		
		routingContext.response().putHeader("content-type", "application/json").end(obj.toString());
	}
	
	//------------------------------------
	
	protected void sendStatus(int statusCode, HttpServerResponse response) {
		response.setStatusCode(statusCode).end();
	}
	
	protected void sendStatus(int statusCode, String msg, HttpServerResponse response) {
		response.setStatusCode(statusCode).end(msg);
	}
}