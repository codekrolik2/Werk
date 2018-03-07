package org.werk.ui;

import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.ui.controls.createjobform.CreateJobForm;
import org.werk.ui.controls.jobdetailsform.JobDetailsForm;
import org.werk.ui.controls.jobdetailsform.JobStepsForm;
import org.werk.ui.controls.jobrestartform.RestartJobForm;
import org.werk.ui.controls.jobsform.JobsForm;
import org.werk.ui.controls.jobtypeinfoform.JobTypeInfoForm;
import org.werk.ui.controls.jobtypesform.JobTypesForm;
import org.werk.ui.controls.serverinfoform.ServerInfoForm;
import org.werk.ui.controls.setserverform.SetServerForm;
import org.werk.ui.controls.steptypeinfoform.StepTypeInfoForm;
import org.werk.ui.controls.steptypesform.StepTypesForm;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TabCreator {
	protected Provider<SetServerForm> setServerFormProvider;
	protected Provider<ServerInfoForm> serverInfoFormProvider;
	protected Provider<JobTypesForm> jobTypesFormProvider;
	protected Provider<StepTypesForm> stepTypesFormProvider;
	protected Provider<JobTypeInfoForm> jobTypeInfoFormProvider;
	protected Provider<StepTypeInfoForm> stepTypeInfoFormProvider;
	protected Provider<CreateJobForm> createJobFormProvider;
	protected Provider<JobsForm> jobsFormProvider;
	protected Provider<JobDetailsForm> jobDetailsFormProvider;
	protected Provider<JobStepsForm> jobStepsFormProvider;
	protected Provider<RestartJobForm> restartJobFormProvider;
	
	@Inject
	public TabCreator(Provider<SetServerForm> setServerFormProvider,
			Provider<ServerInfoForm> serverInfoFormProvider,
			Provider<JobTypesForm> jobTypesFormProvider,
			Provider<StepTypesForm> stepTypesFormProvider,
			Provider<JobTypeInfoForm> jobTypeInfoFormProvider,
			Provider<StepTypeInfoForm> stepTypeInfoFormProvider,
			Provider<CreateJobForm> createJobFormProvider,
			Provider<JobsForm> jobsFormProvider,
			Provider<JobDetailsForm> jobDetailsFormProvider,
			Provider<JobStepsForm> jobStepsFormProvider,
			Provider<RestartJobForm> restartJobFormProvider) {
		this.setServerFormProvider = setServerFormProvider;
		this.serverInfoFormProvider = serverInfoFormProvider;
		this.jobTypesFormProvider = jobTypesFormProvider;
		this.stepTypesFormProvider = stepTypesFormProvider;
		this.jobTypeInfoFormProvider = jobTypeInfoFormProvider;
		this.stepTypeInfoFormProvider = stepTypeInfoFormProvider;
		this.createJobFormProvider = createJobFormProvider;
		this.jobsFormProvider = jobsFormProvider;
		this.jobDetailsFormProvider = jobDetailsFormProvider;
		this.jobStepsFormProvider = jobStepsFormProvider;
		this.restartJobFormProvider = restartJobFormProvider;
	}
	
	public SetServerForm getSetServerForm() {
		return setServerFormProvider.get();
	}
	
	public ServerInfoForm getServerInfoForm() {
		return serverInfoFormProvider.get();
	}
	
	public JobTypesForm getJobTypesForm() {
		JobTypesForm jobTypesForm = jobTypesFormProvider.get();
		jobTypesForm.initTable();
		return jobTypesForm;
	}
	
	public StepTypesForm getStepTypesForm() {
		StepTypesForm stepTypesForm = stepTypesFormProvider.get();
		stepTypesForm.initTable();
		return stepTypesForm;
	}
	
	public JobTypeInfoForm getJobTypeInfoForm(JobType jobType) {
		JobTypeInfoForm jobTypeInfoForm = jobTypeInfoFormProvider.get();
		jobTypeInfoForm.initTable();
		jobTypeInfoForm.setJobType(jobType);
		return jobTypeInfoForm;
	}
	
	public StepTypeInfoForm getStepTypeInfoForm(StepType<Long> stepType) {
		StepTypeInfoForm stepTypeInfoForm = stepTypeInfoFormProvider.get();
		stepTypeInfoForm.initTables();
		stepTypeInfoForm.setStepType(stepType);
		return stepTypeInfoForm;
	}
	
	public CreateJobForm getCreateJobForm(String jobType) {
		CreateJobForm createJobForm = createJobFormProvider.get();
		createJobForm.setJobType(jobType);
		return createJobForm;
	}
	
	public JobsForm getJobsForm() {
		JobsForm jobsForm = jobsFormProvider.get();
		jobsForm.initTable();
		return jobsForm;
	}
	
	public JobDetailsForm getJobDetailsForm(Long jobId) {
		JobDetailsForm jobDetailsForm = jobDetailsFormProvider.get();
		jobDetailsForm.setJobId(jobId);
		return jobDetailsForm;
	}
	
	public JobDetailsForm getJobDetailsForm() {
		JobDetailsForm jobDetailsForm = jobDetailsFormProvider.get();
		return jobDetailsForm;
	}
	
	public JobStepsForm getJobStepsForm() {
		JobStepsForm jobStepsForm = jobStepsFormProvider.get();
		return jobStepsForm;
	}

	public RestartJobForm getRestartJobForm(Long jobId) {
		RestartJobForm restartJobForm = restartJobFormProvider.get();
		restartJobForm.setJobId(jobId);
		return restartJobForm;
	}
}
