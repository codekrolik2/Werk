package org.werk.rest.serializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.engine.JobIdSerializer;
import org.werk.rest.JobFilters;
import org.werk.service.PageInfo;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JobFiltersSerializer<J> {
	protected TimeProvider timeProvider;
	protected JobIdSerializer<J> jobIdSerializer; 
	
	public JobFilters<J> deserializeJobFilters(JSONObject filtersJSON) {
		Optional<Timestamp> from = Optional.empty();
		Optional<Timestamp> to = Optional.empty();
		Optional<Map<String, Long>> jobTypes = Optional.empty();
		Optional<Collection<J>> parentJobIds = Optional.empty();
		Optional<Collection<J>> jobIds = Optional.empty();
		Optional<Set<String>> currentStepTypes = Optional.empty();
		Optional<PageInfo> pageInfo = Optional.empty();
		
		if (filtersJSON.has("from"))
			from = Optional.of(timeProvider.createTimestamp(filtersJSON.getString("from")));
		if (filtersJSON.has("to"))
			to = Optional.of(timeProvider.createTimestamp(filtersJSON.getString("to")));
		if (filtersJSON.has("jobTypes")) {
			Map<String, Long> jobTypesMap = new HashMap<>();
			
			JSONObject jobTypesJSON = filtersJSON.getJSONObject("jobTypes");
			
			Iterator<?> keys = jobTypesJSON.keys();
			while (keys.hasNext()) {
			    String jobTypeName = (String)keys.next();
			    Long version  = jobTypesJSON.getLong(jobTypeName);
			    
			    jobTypesMap.put(jobTypeName, version);
			}
			
			jobTypes = Optional.of(jobTypesMap);
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
		if (filtersJSON.has("pageInfo")) {
			JSONObject pageInfoJSON = filtersJSON.getJSONObject("pageInfo");
			
			long itemsPerPage = pageInfoJSON.getLong("itemsPerPage");
			long pageNumber = pageInfoJSON.getLong("pageNumber");
			
			PageInfo pageInfoObj = new PageInfo(itemsPerPage, pageNumber);
			pageInfo = Optional.of(pageInfoObj);
		}
		
		return new JobFilters<>(from, to, jobTypes, parentJobIds, jobIds, currentStepTypes, pageInfo);
	}
}
