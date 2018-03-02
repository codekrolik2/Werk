package org.werk.rest.serializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.engine.JobIdSerializer;
import org.werk.meta.JobTypeSignature;
import org.werk.processing.jobs.JobStatus;
import org.werk.rest.JobFilters;
import org.werk.service.PageInfo;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JobFiltersSerializer<J> {
	protected TimeProvider timeProvider;
	protected JobIdSerializer<J> jobIdSerializer;
	protected PageInfoSerializer pageInfoSerializer;
	
	public JobFilters<J> deserializeJobFilters(JSONObject filtersJSON) {
		Optional<Timestamp> from = Optional.empty();
		Optional<Timestamp> to = Optional.empty();
		Optional<Timestamp> fromExec = Optional.empty();
		Optional<Timestamp> toExec = Optional.empty();
		Optional<List<JobTypeSignature>> jobTypes = Optional.empty();
		Optional<Collection<J>> parentJobIds = Optional.empty();
		Optional<Collection<J>> jobIds = Optional.empty();
		Optional<Set<String>> currentStepTypes = Optional.empty();
		Optional<Set<JobStatus>> jobStatuses = Optional.empty();
		Optional<PageInfo> pageInfo = Optional.empty();
		
		if (filtersJSON.has("from"))
			from = Optional.of(timeProvider.createTimestamp(filtersJSON.getString("from")));
		if (filtersJSON.has("to"))
			to = Optional.of(timeProvider.createTimestamp(filtersJSON.getString("to")));
		if (filtersJSON.has("fromExec"))
			fromExec = Optional.of(timeProvider.createTimestamp(filtersJSON.getString("from")));
		if (filtersJSON.has("toExec"))
			toExec = Optional.of(timeProvider.createTimestamp(filtersJSON.getString("to")));
		if (filtersJSON.has("jobTypes")) {
			List<JobTypeSignature> jobTypesList = new ArrayList<>();
			
			JSONObject jobTypesJSON = filtersJSON.getJSONObject("jobTypes");
			
			Iterator<?> keys = jobTypesJSON.keys();
			while (keys.hasNext()) {
			    String jobTypeName = (String)keys.next();
			    Long version  = jobTypesJSON.getLong(jobTypeName);
			    
			    jobTypesList.add(new JobTypeSignature() {
					@Override public long getVersion() { return version; }
					@Override public String getJobTypeName() { return jobTypeName; }
				});
			}
			
			jobTypes = Optional.of(jobTypesList);
		}
		if (filtersJSON.has("parentJobIds")) {
			List<J> parentJobIdList = new ArrayList<>();
			
			JSONArray arr = filtersJSON.getJSONArray("parentJobIds");
			for (int i = 0; i < arr.length(); i++)
				parentJobIdList.add(jobIdSerializer.deSerializeJobId(arr.getString(i)));
			
			parentJobIds = Optional.of(parentJobIdList);
		}
		if (filtersJSON.has("jobIds")) {
			List<J> jobIdList = new ArrayList<>();
			
			JSONArray arr = filtersJSON.getJSONArray("jobIds");
			for (int i = 0; i < arr.length(); i++)
				jobIdList.add(jobIdSerializer.deSerializeJobId(arr.getString(i)));
			
			jobIds = Optional.of(jobIdList);
		}
		if (filtersJSON.has("currentStepTypes")) {
			Set<String> stepTypesSet = new HashSet<>();
			
			JSONArray arr = filtersJSON.getJSONArray("currentStepTypes");
			for (int i = 0; i < arr.length(); i++)
				stepTypesSet.add(arr.getString(i));
			
			currentStepTypes = Optional.of(stepTypesSet);
		}
		if (filtersJSON.has("jobStatuses")) {
			Set<JobStatus> jobStatusesSet = new HashSet<>();
			
			JSONArray arr = filtersJSON.getJSONArray("jobStatuses");
			for (int i = 0; i < arr.length(); i++)
				jobStatusesSet.add(JobStatus.valueOf(arr.getString(i)));
			
			jobStatuses = Optional.of(jobStatusesSet);
		}
		if (filtersJSON.has("pageInfo")) {
			JSONObject pageInfoJSON = filtersJSON.getJSONObject("pageInfo");
			
			long itemsPerPage = pageInfoJSON.getLong("itemsPerPage");
			long pageNumber = pageInfoJSON.getLong("pageNumber");
			
			PageInfo pageInfoObj = new PageInfo(itemsPerPage, pageNumber);
			pageInfo = Optional.of(pageInfoObj);
		}
		
		return new JobFilters<>(from, to, fromExec, toExec, jobTypes, parentJobIds, jobIds, 
				currentStepTypes, jobStatuses, pageInfo);
	}

	public JSONObject serializeJobFilters(JobFilters<J> filters) {
		JSONObject filtersJSON = new JSONObject();
		
		if (filters.getFrom().isPresent())
			filtersJSON.put("from", filters.getFrom().get().getRawTime());
		if (filters.getTo().isPresent())
			filtersJSON.put("to", filters.getTo().get().getRawTime());
		if (filters.getFromExec().isPresent())
			filtersJSON.put("fromExec", filters.getFromExec().get().getRawTime());
		if (filters.getToExec().isPresent())
			filtersJSON.put("toExec", filters.getToExec().get().getRawTime());
		if (filters.getJobTypesAndVersions().isPresent()) {
			List<JobTypeSignature> jobTypesMap = filters.getJobTypesAndVersions().get();
			JSONObject jobTypesJSON = new JSONObject();
			
			for (JobTypeSignature ent : jobTypesMap)
				jobTypesJSON.put(ent.getJobTypeName(), ent.getVersion());
			
			filtersJSON.put("jobTypes", jobTypesJSON);
		}
		if (filters.getParentJobIds().isPresent()) {
			JSONArray arr = new JSONArray();
			
			for(J j : filters.getParentJobIds().get())
				arr.put(jobIdSerializer.serializeJobId(j));
			
			filtersJSON.put("parentJobIds", arr);
		}
		if (filters.getJobIds().isPresent()) {
			JSONArray arr = new JSONArray();
			
			for(J j : filters.getJobIds().get())
				arr.put(jobIdSerializer.serializeJobId(j));
			
			filtersJSON.put("jobIds", arr);
		}
		if (filters.getCurrentStepTypes().isPresent()) {
			JSONArray arr = new JSONArray();
			
			for(String s : filters.getCurrentStepTypes().get())
				arr.put(s);
			
			filtersJSON.put("currentStepTypes", arr);
		}
		if (filters.getJobStatuses().isPresent()) {
			JSONArray arr = new JSONArray();
			
			for(JobStatus jobStatus : filters.getJobStatuses().get())
				arr.put(jobStatus.toString());
			
			filtersJSON.put("jobStatuses", arr);
		}
		if (filters.getPageInfo().isPresent()) {
			JSONObject pageInfoJSON = pageInfoSerializer.serializePageInfo(filters.getPageInfo().get());
			filtersJSON.put("pageInfo", pageInfoJSON);
		}
		
		return filtersJSON;
	}
}
