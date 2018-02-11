package org.werk.meta.impl;

import java.util.List;
import java.util.Optional;

import org.werk.meta.NewStepRestartInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class NewStepRestartInfoImpl implements NewStepRestartInfo {
	@Getter
	String newStepTypeName;
	@Getter
	boolean isNewStepRollback;
	@Getter
	Optional<List<Integer>> stepsToRollback;
}