package org.werk.processing.steps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.werk.data.StepPOJO;

/**
 * PROCESING:
 * 	Success: Tries to find next step name in the following locations
 * 		1) StepParameter "BasicTransitioner_NextStep"
 * 		2) StepType's "transitionerConfig" field
 * 	If next step name found, transitions to the next step, otherwise finishes job.
 * 
 * 	Failure:
 * 		Switch to Rollback for current step.
 * 
 * ROLLBACK:
 * 	Success: run common rollback, i.e.
 * 		In processing history find the last step that wasn't rolled back and roll it back.
 * 		If such step wasn't found (all steps were rolled back), finish rollback.
 * 
 *  Failure:
 *  	Fail job
 * 
 * @param <J> JobId
 */
public class SimpleTransitioner<J> implements Transitioner<J> {
	@Override
	public Transition processingTransition(boolean isSuccess, Step<J> step) {
		//PROCESSING
		if (isSuccess) {
			String nextStep = step.getStringParameter("BasicTransitioner_NextStep");
			
			if ((nextStep == null) || (nextStep.trim().isEmpty()))
				nextStep = step.getStepType().getTransitionerConfig();
			
			if ((nextStep == null) || (nextStep.trim().isEmpty()))
				return Transition.nextStep(nextStep);
			else
				return Transition.finish();
		} else {
			List<Long> rollbackStepNumbers = new ArrayList<Long>();
			rollbackStepNumbers.add(step.getStepNumber());
			return Transition.rollback(step.getStepTypeName(), rollbackStepNumbers);
		}
	}
	
	@Override
	public Transition rollbackTransition(boolean isSuccess, Step<J> step) {
		//ROLLBACK
		if (isSuccess)
			return commonRollback(step);
		else
			return Transition.fail(); 
	}
	
	/**
	 * Common rollback routine
	 * 	In processing history find the last step that wasn't rolled back and roll it back.
	 * 	If such step wasn't found (all steps were rolled back), finish rollback.
	 * 
	 * @param step Current step
	 * @return Transition to rollback next step or finish rollback.
	 */
	public static <J> Transition commonRollback(Step<J> step) {
		List<StepPOJO> history = step.getJob().getProcessingHistory();
		
		Set<Long> stepNumbers = new HashSet<>();
		for (int i = history.size()-1; i >= 0; i--) {
			StepPOJO pojo = history.get(i);
			if (pojo.isRollback()) {
				if (pojo.getRollbackStepNumbers() != null)
					stepNumbers.addAll(pojo.getRollbackStepNumbers());
			} else {
				if (!stepNumbers.contains(pojo.getStepNumber())) {
					List<Long> rollbackStepNumbers = new ArrayList<Long>();
					rollbackStepNumbers.add(step.getStepNumber());
					return Transition.rollback(step.getStepTypeName(), rollbackStepNumbers);
				}
			}
		}
		
		return Transition.finishRollback();
	}
}
