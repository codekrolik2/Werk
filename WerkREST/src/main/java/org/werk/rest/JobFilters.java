package org.werk.rest;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.pillar.time.interfaces.Timestamp;
import org.werk.service.PageInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JobFilters<J> {
	@Getter
	Optional<Timestamp> from;
	@Getter
	Optional<Timestamp> to; 
	@Getter
	Optional<Map<String, Long>> jobTypesAndVersions;
	@Getter
	Optional<Collection<J>> parentJobIds; 
	@Getter
	Optional<Collection<J>> jobIds;
	@Getter
	Optional<Set<String>> currentStepTypes;
	@Getter
	Optional<PageInfo> pageInfo;
}
