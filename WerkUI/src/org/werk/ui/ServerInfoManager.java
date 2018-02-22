package org.werk.ui;

import org.json.JSONObject;
import org.werk.ui.controls.mainapp.MainApp;

import lombok.Getter;

public class ServerInfoManager {
	@Getter
	String host;
	@Getter
	int port;
	@Getter
	String serverInfo;
	
	public ServerInfoManager() {
		host = "";
		port = -1;
		serverInfo = "Server not assigned";
	}
	
	public void resetServerInfo(MainApp mainApp) {
		host = "";
		port = -1;
		serverInfo = "Server not assigned";
		
		mainApp.setStatusText("Server not assigned");
	}
	
	public void newServerInfo(MainApp mainApp, String host, int port, JSONObject serverInfo) {
		this.host = host;
		this.port = port;
		this.serverInfo = serverInfo.toString(4);
		
		mainApp.setStatusText(String.format("Server [%s:%d]", host, port));
		mainApp.createServerInfoTab();
		
	}
	
	public void newServerInfo(JSONObject serverInfo) {
		this.serverInfo = serverInfo.toString(4);
	}
}
