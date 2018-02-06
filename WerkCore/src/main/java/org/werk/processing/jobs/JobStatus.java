package org.werk.processing.jobs;

public enum JobStatus {
	UNDEFINED(1),
	
	PROCESSING(2),
	ROLLING_BACK(3),
	
	JOINING(4),
	
	FINISHED(5),
	ROLLED_BACK(6),
	FAILED(7);
	
	private int status;
	JobStatus(int status) {
		this.status = status;
	}
	
	public int getCode() {
		return status;
	}
	
	public static JobStatus fromCode(int code) {
		switch (code) {
			case 1: return UNDEFINED;
			case 2: return PROCESSING;
			case 3: return ROLLING_BACK;
			case 4: return JOINING;
			case 5: return FINISHED;
			case 6: return ROLLED_BACK;
			case 7: return FAILED;
			default: throw new IllegalArgumentException(
					String.format("Unknown JobStatus code [%d]", code)
				);
		}
	}
}
