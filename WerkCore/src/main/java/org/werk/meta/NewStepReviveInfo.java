package org.werk.meta;

import java.util.List;
import java.util.Optional;

public interface NewStepReviveInfo {
	String getNewStepTypeName();
	Boolean isNewStepRollback();
	Optional<List<Integer>> getStepsToRollback();
}
