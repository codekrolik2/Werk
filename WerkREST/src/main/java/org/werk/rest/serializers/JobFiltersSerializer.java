package org.werk.rest.serializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.engine.JobIdSerializer;
import org.werk.rest.JobFilters;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JobFiltersSerializer<J> {
	protected TimeProvider timeProvider;
	protected JobIdSerializer<J> jobIdSerializer; 
	
	public JobFilters<J> deserializeJobFilters(JSONObject filtersJSON) {
		Optional<Timestamp> from = Optional.empty();
		Optional<Timestamp> to = Optional.empty();
		Optional<Set<String>> jobTypes = Optional.empty();
		Optional<Collection<J>> parentJobIds = Optional.empty();
		Optional<Collection<J>> jobIds = Optional.empty();
		
		if (filtersJSON.has("from"))
			from = Optional.of(timeProvider.createTimestamp(filtersJSON.getString("from")));
		if (filtersJSON.has("to"))
			to = Optional.of(timeProvider.createTimestamp(filtersJSON.getString("to")));
		if (filtersJSON.has("jobTypes")) {
			Set<String> jobTypesSet = new HashSet<>();
			
			JSONArray arr = filtersJSON.getJSONArray("jobTypes");
			for (int i = 0; i < arr.length(); i++)
				jobTypesSet.add(arr.getString(i));
			
			jobTypes = Optional.of(jobTypesSet);
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
		
		return new JobFilters<>(from, to, jobTypes, parentJobIds, jobIds);
	}
}
