package org.werk.engine;

import org.werk.processing.jobs.Job;

public interface WerkEngine<J> {
	void addJob(Job<J> job);
	void shutdown();
}
