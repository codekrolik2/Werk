package org.werk.restclient;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
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
import org.werk.rest.serializers.JobStepSerializer;
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

	public Long createJob(JobInitInfo init) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Long createJobOfVersion(VersionJobInitInfo init) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void restartJob(JobRestartInfo<Long> jobRestartInfo) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public ReadOnlyJob<Long> getJobAndHistory(Long jobId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void getJobs(Optional<Timestamp> from, Optional<Timestamp> to, Optional<Timestamp> fromExec,
			Optional<Timestamp> toExec, Optional<Map<String, Long>> jobTypesAndVersions,
			Optional<Collection<Long>> parentJobIds, Optional<Collection<Long>> jobIds,
			Optional<Set<String>> currentStepTypes, Optional<PageInfo> pageInfo, 
			Callback<JobCollection<Long>> callback) throws Exception {
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
			if (ar.succeeded()) {
				// Obtain response
				HttpResponse<Buffer> response = ar.result();
				
				logger.info("Received response with status code" + response.statusCode());
				
				String bodyStr = response.bodyAsString();
				JSONObject bodyJSON = new JSONObject(bodyStr);
				
				JobCollection<Long> jobCollection = jobStepSerializer.deserializeJobCollection(bodyJSON);
				callback.done(jobCollection);
			} else {
				logger.error("Something went wrong " + ar.cause().getMessage());
				callback.error(ar.cause());
			}
		});
	}

	public Collection<JobType> getJobTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public JobType getJobType(String jobTypeName, Optional<Long> version) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<StepType<Long>> getAllStepTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<StepType<Long>> getStepTypesForJob(String jobTypeName, Optional<Long> version) {
		// TODO Auto-generated method stub
		return null;
	}

	public StepType<Long> getStepType(String stepTypeName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void jobsAdded() {
		// TODO Auto-generated method stub
	}
	
	public JSONObject getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}
}
