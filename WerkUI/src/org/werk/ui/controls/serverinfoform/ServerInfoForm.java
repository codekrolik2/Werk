package org.werk.ui.controls.serverinfoform;

import java.io.IOException;

import org.json.JSONObject;
import org.werk.restclient.WerkCallback;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.guice.LoaderFactory;
import org.werk.ui.util.MessageBox;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ServerInfoForm extends VBox {
	@FXML
    Button refreshButton;
	@FXML
    TextArea serverInfoText;

	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
	
	public ServerInfoForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("ServerInfoForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	public void setServerInfo() {
		serverInfoText.setText(serverInfoManager.getServerInfo());
	}
	
	@FXML
    private void refresh() {
		if (serverInfoManager.getPort() < 0)
			MessageBox.show(String.format("Server not assigned. Please set server."));
		else {
	    	try {
				String host = serverInfoManager.getHost();
				int port = serverInfoManager.getPort();
				
				refreshButton.setDisable(true);
				
				WerkCallback<JSONObject> callback = new WerkCallback<JSONObject>() {
					@Override
					public void error(Throwable cause) {
						Platform.runLater( () -> {
							refreshButton.setDisable(false);
							MessageBox.show(
								String.format("Error processing request %s:%d [%s]", host, port, cause.toString())
							);
						});
					}
					
					@Override
					public void done(JSONObject result) {
						Platform.runLater( () -> {
							refreshButton.setDisable(false);
							serverInfoManager.newServerInfo(result);
							setServerInfo();
						});
					}
				};
				
				werkClient.getServerInfo(host, port, callback);
	    	} catch(Exception e) {
				MessageBox.show(String.format("Refresh error: [%s]", e));
				refreshButton.setDisable(false);
	    	}
		}
	}
}
