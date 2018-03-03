package org.werk.meta;

public interface JobTypeSignature {
	String getJobTypeName();
	long getVersion();
	
	static String getJobTypeFullName(JobTypeSignature jts) {
		return getJobTypeFullName(jts.getJobTypeName(), jts.getVersion());
	}

	static String getJobTypeFullName(String jobTypeName, long version) {
		return String.format("%s [v%d]", jobTypeName, version);
	}
}
