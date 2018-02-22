package org.werk.ui;

import org.werk.ui.controls.connectform.ConnectFormController;
import org.werk.ui.controls.jobtypesform.JobTypesFormControl;
import org.werk.ui.controls.serverinfoform.ServerInfo;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TabCreator {
	protected Provider<ConnectFormController> connectFormControllerProvider;
	protected Provider<ServerInfo> serverInfoControllerProvider;
	protected Provider<JobTypesFormControl> jobTypesFormControlProvider;
	
	@Inject
	public TabCreator(Provider<ConnectFormController> connectFormControllerProvider,
			Provider<ServerInfo> serverInfoControllerProvider,
			Provider<JobTypesFormControl> jobTypesFormControlProvider) {
		this.connectFormControllerProvider = connectFormControllerProvider;
		this.serverInfoControllerProvider = serverInfoControllerProvider;
		this.jobTypesFormControlProvider = jobTypesFormControlProvider;
	}
	
	public ConnectFormController getConnectFormController() {
		return connectFormControllerProvider.get();
	}
	
	public ServerInfo getServerInfoController() {
		return serverInfoControllerProvider.get();
	}
	
	public JobTypesFormControl getJobTypesFormControl() {
		return jobTypesFormControlProvider.get();
	}
}
