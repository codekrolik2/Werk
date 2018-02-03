package org.werk.engine;

import java.util.Optional;

import org.werk.processing.steps.callback.WerkCallback;

import lombok.Getter;

public class StepSwitchResult {
	@Getter
	protected SwitchStatus status;
	@Getter
	protected Optional<Long> delayMS;
	@Getter
	protected Optional<WerkCallback<String>> callback;
	@Getter
	protected Optional<String> parameterName;

	private StepSwitchResult(SwitchStatus status, Optional<WerkCallback<String>> callback,
			Optional<Long> delayMS, Optional<String> parameterName) {
		this.status = status;
		this.callback = callback;
		this.delayMS = delayMS;
		this.parameterName = parameterName;
	}
	
	public static StepSwitchResult process() {
		return new StepSwitchResult(SwitchStatus.PROCESS, Optional.empty(), Optional.empty(), Optional.empty());
	}
	
	public static StepSwitchResult process(Optional<Long> delayMS) {
		return new StepSwitchResult(SwitchStatus.PROCESS, Optional.empty(), delayMS, Optional.empty());
	}
	
	public static StepSwitchResult processWithDelay(long delayMS) {
		return new StepSwitchResult(SwitchStatus.PROCESS, Optional.empty(), Optional.of(delayMS), Optional.empty());
	}
	
	public static StepSwitchResult callback(WerkCallback<String> callback, String parameterName) {
		return new StepSwitchResult(SwitchStatus.CALLBACK, Optional.of(callback), Optional.empty(), Optional.of(parameterName));
	}
	
	public static StepSwitchResult callback(WerkCallback<String> callback, 
			Optional<Long> timeoutMs, String parameterName) {
		return new StepSwitchResult(SwitchStatus.CALLBACK, Optional.of(callback), 
				timeoutMs, Optional.of(parameterName));
	}
	
	public static StepSwitchResult callbackWithTimeout(WerkCallback<String> callback, 
			long timeoutMs, String parameterName) {
		return new StepSwitchResult(SwitchStatus.CALLBACK, Optional.of(callback), 
				Optional.of(timeoutMs), Optional.of(parameterName));
	}

	public static StepSwitchResult unload() {
		return new StepSwitchResult(SwitchStatus.UNLOAD, Optional.empty(), Optional.empty(), Optional.empty());
	}
}
