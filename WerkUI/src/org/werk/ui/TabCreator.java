package org.werk.ui;

import org.werk.ui.controls.connectform.ConnectFormController;
import org.werk.ui.controls.serverinfoform.ServerInfoController;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TabCreator {
	protected Provider<ConnectFormController> connectFormControllerProvider;
	protected Provider<ServerInfoController> serverInfoControllerProvider;
	
	@Inject
	public TabCreator(Provider<ConnectFormController> connectFormControllerProvider,
			Provider<ServerInfoController> serverInfoControllerProvider) {
		this.connectFormControllerProvider = connectFormControllerProvider;
		this.serverInfoControllerProvider = serverInfoControllerProvider;
	}
	
	public ConnectFormController getConnectFormController() {
		return connectFormControllerProvider.get();
	}
	
	public ServerInfoController getServerInfoController() {
		return serverInfoControllerProvider.get();
	}
}
