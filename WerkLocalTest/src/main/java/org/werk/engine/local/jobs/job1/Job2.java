package org.werk.engine.local.jobs.job1;

import org.werk.config.annotations.JobInit;
import org.werk.config.annotations.JobType;
import org.werk.config.annotations.inputparameters.DefaultBoolParameter;
import org.werk.config.annotations.inputparameters.DefaultLongParameter;
import org.werk.config.annotations.inputparameters.DefaultStringParameter;
import org.werk.config.annotations.inputparameters.EnumStringParameter;
import org.werk.config.annotations.inputparameters.JobInputParameter;
import org.werk.config.annotations.inputparameters.RangeDoubleParameter;

@JobType(name="Job2",
firstStepTypeName="Step2",
stepTypeNames={ "Step2", "Step3", "Step4" })
public class Job2 {
	@JobInit(initSignatureName="JOB2 Default")
	public void initJob(
		@DefaultBoolParameter(name="b21", isDefaultValueImmutable=false, defaultValue=true)
		Boolean b1,
		@RangeDoubleParameter(name="d21", start=3.01, end=7.05)
		Double d1,
		@DefaultLongParameter(name="l22", isDefaultValueImmutable=true, defaultValue=413250L)
		Long l1,
		@DefaultStringParameter(name="text", isDefaultValueImmutable=false, defaultValue="test 2 2")
		String text,
		@EnumStringParameter(name="text22", isOptional=true, values={ "a", "b", "c" })
		String text2,
		@JobInputParameter(name="text33", isOptional=false)
		String text3,
		@JobInputParameter(name="l24", isOptional=false)
		Long l2
	) {}

	@JobInit(initSignatureName="JOB2 Alternative")
	public void initJob2(
		@DefaultBoolParameter(name="b41", isDefaultValueImmutable=false, defaultValue=true)
		Boolean b1,
		@RangeDoubleParameter(name="d31", start=513.01, end=517.05)
		Double d1,
		@DefaultLongParameter(name="l31", isDefaultValueImmutable=true, defaultValue=555L)
		Long l1,
		@DefaultStringParameter(name="text", isDefaultValueImmutable=false, defaultValue="TEXT 2")
		String text,
		@EnumStringParameter(name="text12", isOptional=true, values={ "d", "e", "f" })
		String text2,
		@JobInputParameter(name="texrr3", isOptional=false)
		String text3,
		@JobInputParameter(name="l56", isOptional=false)
		Long l2
	) {}
}
