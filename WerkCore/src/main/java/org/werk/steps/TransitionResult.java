package org.werk.steps;

import java.util.Optional;

public interface TransitionResult {
	TransitionStatus getTransitionStatus();
	Optional<String> stepName();
}
