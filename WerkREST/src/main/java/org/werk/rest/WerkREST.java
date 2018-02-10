package org.werk.rest;

import java.util.Optional;

import org.json.JSONArray;
import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.rest.serializers.JobStepTypeRESTSerializer;
import org.werk.service.WerkService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class WerkREST extends AbstractVerticle {
	protected WerkService<Long> werkService;
	
	public WerkREST(WerkService<Long> werkService) {
		this.werkService = werkService;
	}
	
	@Override
	public void start() {
		Router router = Router.router(vertx);
		
		router.route().handler(BodyHandler.create());
		
		router.get("/jobTypes/:jobTypeName/:version").handler(this::handleGetJobType);
		router.get("/jobTypes/:jobTypeName").handler(this::handleGetJobType);
		router.get("/jobTypes").handler(this::handleListJobTypes);
		
		router.get("/stepTypes/:stepTypeName").handler(this::handleGetStepType);
		router.get("/stepTypesForJob/:jobTypeName/:version").handler(this::handleListStepTypes);
		router.get("/stepTypesForJob/:jobTypeName").handler(this::handleListStepTypes);
		
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
	
	private void handleGetJobType(RoutingContext routingContext) {
		String jobTypeName = routingContext.request().getParam("jobTypeName");
		String versionStr = routingContext.request().getParam("version");
		
		HttpServerResponse response = routingContext.response();
		if (jobTypeName == null) {
			sendError(400, response);
		} else {
			Optional<Long> version = Optional.empty();
			if (versionStr != null) {
				try {
					long v = Long.parseLong(versionStr);
					version = Optional.of(v);
				} catch (NumberFormatException nfe) {
					sendError(400, response);
					return;
				}
			}
			
			JobType jobType = werkService.getJobType(jobTypeName, version);
			if (jobType == null) {
				sendError(404, response);
			} else {
				response.putHeader("content-type", "application/json").
					end(JobStepTypeRESTSerializer.serializeJobType(jobType).toString());
			}
		}
	}

	private void handleListJobTypes(RoutingContext routingContext) {
		JSONArray arr = new JSONArray();
		for (JobType jobType : werkService.getJobTypes())
			arr.put(JobStepTypeRESTSerializer.serializeJobType(jobType));
		
		routingContext.response().putHeader("content-type", "application/json").end(arr.toString());
	}

	private void handleGetStepType(RoutingContext routingContext) {
		String stepTypeName = routingContext.request().getParam("stepTypeName");
		
		HttpServerResponse response = routingContext.response();
		if (stepTypeName == null) {
			sendError(400, response);
		} else {
			StepType<?> stepType = werkService.getStepType(stepTypeName);
			
			routingContext.response().putHeader("content-type", "application/json").
				end(JobStepTypeRESTSerializer.serializeStepType(stepType).toString());
		}
	}

	private void handleListStepTypes(RoutingContext routingContext) {
		String jobTypeName = routingContext.request().getParam("jobTypeName");
		String versionStr = routingContext.request().getParam("version");
		
		HttpServerResponse response = routingContext.response();
		if (jobTypeName == null) {
			sendError(400, response);
		} else {
			Optional<Long> version = Optional.empty();
			if (versionStr != null) {
				try {
					long v = Long.parseLong(versionStr);
					version = Optional.of(v);
				} catch (NumberFormatException nfe) {
					sendError(400, response);
					return;
				}
			}

			JSONArray arr = new JSONArray();
			for (StepType<?> stepType : werkService.getStepTypesForJob(jobTypeName, version))
				arr.put(JobStepTypeRESTSerializer.serializeStepType(stepType));
			
			routingContext.response().putHeader("content-type", "application/json").end(arr.toString());
		}
	}
	
	//------------------------------------
	
	protected void sendError(int statusCode, HttpServerResponse response) {
		response.setStatusCode(statusCode).end();
	}
}