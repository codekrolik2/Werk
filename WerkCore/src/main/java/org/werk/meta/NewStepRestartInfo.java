package org.werk.meta;

import java.util.List;
import java.util.Optional;

public interface NewStepRestartInfo {
	String getNewStepTypeName();
	boolean isNewStepRollback();
	Optional<List<Integer>> getStepsToRollback();
}
