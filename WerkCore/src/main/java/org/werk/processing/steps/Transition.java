package org.werk.processing.steps;

import java.util.Optional;

public interface Transition {
	TransitionStatus getTransitionStatus();
	Optional<String> stepName();
}
