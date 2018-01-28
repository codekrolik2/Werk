package org.werk.engine;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StepSwitchResult {
	@Getter
	protected SwitchStatus status;
	@Getter
	protected Optional<Long> delayMS;
	
	public StepSwitchResult(SwitchStatus status) {
		this(status, Optional.empty());
	}
}
