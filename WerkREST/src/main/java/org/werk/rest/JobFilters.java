package org.werk.rest;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.pillar.time.interfaces.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JobFilters<J> {
	@Getter
	Optional<Timestamp> from;
	@Getter
	Optional<Timestamp> to; 
	@Getter
	Optional<Set<String>> jobTypes;
	@Getter
	Optional<Collection<J>> parentJobIds; 
	@Getter
	Optional<Collection<J>> jobIds;
}
