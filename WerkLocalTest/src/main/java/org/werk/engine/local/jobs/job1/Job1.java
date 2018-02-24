package org.werk.engine.local.jobs.job1;

import org.werk.config.annotations.JobInit;
import org.werk.config.annotations.JobType;
import org.werk.config.annotations.StepType;
import org.werk.config.annotations.inputparameters.DefaultBoolParameter;
import org.werk.config.annotations.inputparameters.DefaultLongParameter;
import org.werk.config.annotations.inputparameters.DefaultStringParameter;
import org.werk.config.annotations.inputparameters.EnumStringParameter;
import org.werk.config.annotations.inputparameters.JobInputParameter;
import org.werk.config.annotations.inputparameters.RangeDoubleParameter;
import org.werk.engine.local.jobs.exec.SysoutExec;

@JobType(name="Job1",
firstStepTypeName="Step1",
stepTypeNames={ "Step1", "Step2", "Step3", "Step4" })
public class Job1 {
	@JobInit(parameterSetName="Default")
	public void initJob(
		@DefaultBoolParameter(name="b1", isDefaultValueImmutable=false, defaultValue=true)
		Boolean b1,
		//@DefaultDictionaryParameter
		//@DefaultListParameter
		@RangeDoubleParameter(name="d1", start=3.01, end=7.05)
		Double d1,
		@DefaultLongParameter(name="l1", isDefaultValueImmutable=true, defaultValue=123L)
		Long l1,
		@DefaultStringParameter(name="text", isDefaultValueImmutable=false, defaultValue="test")
		String text,
		@EnumStringParameter(name="text2", isOptional=true, values={ "a", "b", "c" })
		String text2,
		@JobInputParameter(name="text3", isOptional=false)
		String text3,
		@JobInputParameter(name="l2", isOptional=false)
		Long l2
	) {}
}

@StepType(name="Step1",
	stepExecClass=SysoutExec.class,
	transitions={"Step2"},
	rollbackTransitions={},
	execConfig="execConfig Step1",
	transitionerConfig="transitionerConfig Step1"
)
class Step1 { }

@StepType(name="Step2",
	stepExecClass=SysoutExec.class,
	transitions={"Step3"},
	rollbackTransitions={"Step1"},
	execConfig="execConfig Step2",
	transitionerConfig="transitionerConfig Step2"
)
class Step2 { }

@StepType(name="Step3",
	stepExecClass=SysoutExec.class,
	transitions={"Step4"},
	rollbackTransitions={"Step2"},
	execConfig="execConfig Step3",
	transitionerConfig="transitionerConfig Step3"
)
class Step3 { }

@StepType(name="Step4",
	stepExecClass=SysoutExec.class,
	transitions={},
	rollbackTransitions={"Step3"},
	execConfig="execConfig Step4",
	transitionerConfig="transitionerConfig Step4"
)
class Step4 { }
