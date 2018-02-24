package org.werk.ui;

import org.werk.meta.JobType;
import org.werk.meta.StepType;
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
	
	@Inject
	public TabCreator(Provider<SetServerForm> setServerFormProvider,
			Provider<ServerInfoForm> serverInfoFormProvider,
			Provider<JobTypesForm> jobTypesFormProvider,
			Provider<StepTypesForm> stepTypesFormProvider,
			Provider<JobTypeInfoForm> jobTypeInfoFormProvider,
			Provider<StepTypeInfoForm> stepTypeInfoFormProvider) {
		this.setServerFormProvider = setServerFormProvider;
		this.serverInfoFormProvider = serverInfoFormProvider;
		this.jobTypesFormProvider = jobTypesFormProvider;
		this.stepTypesFormProvider = stepTypesFormProvider;
		this.jobTypeInfoFormProvider = jobTypeInfoFormProvider;
		this.stepTypeInfoFormProvider = stepTypeInfoFormProvider;
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
}
