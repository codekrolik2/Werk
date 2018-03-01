package org.werk.engine.local.jobs.job1;

import org.werk.config.annotations.JobType;

@JobType(name="Job3",
firstStepTypeName="Step3",
stepTypeNames={ "Step3", "Step4" })
public class Job3 {

}
