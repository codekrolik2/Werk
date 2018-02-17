package org.werk.service;

import java.util.Collection;
import java.util.Optional;

import org.werk.data.JobPOJO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class JobCollection<J> {
	@Getter
	Optional<PageInfo> pageInfo;
	@Getter
	Collection<JobPOJO<J>> jobs;
	@Getter
	long jobCount;
}
