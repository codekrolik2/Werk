package org.werk.ui.controls.setserverform;

import java.io.IOException;

import org.json.JSONObject;
import org.werk.restclient.WerkCallback;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.mainapp.MainApp;
import org.werk.ui.guice.LoaderFactory;
import org.werk.ui.util.MessageBox;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Setter;

public class SetServerForm extends GridPane {
	@FXML
    private TextField hostTextField;
	@FXML
    private TextField portTextField;
	@FXML
    private Button setServerButton;
	@Setter
	private Stage dialogStage;
	
	@Inject
	WerkRESTClient werkClient;
	@Inject
	MainApp mainApp;
	@Inject
	ServerInfoManager serverInfoManager;
	
	public SetServerForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("SetServer.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	@FXML
    private void initialize() {
		portTextField.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        if (!newValue.matches("\\d*"))
		        	portTextField.setText(newValue.replaceAll("[^\\d]", ""));
		    }
		});
	}
	
	@FXML
    private void setServer() {
		String host = hostTextField.getText();
		int port = Integer.parseInt(portTextField.getText());
		
		setServerButton.setDisable(true);
		serverInfoManager.resetServerInfo(mainApp);
		
		WerkCallback<JSONObject> callback = new WerkCallback<JSONObject>() {
			@Override
			public void error(Throwable cause) {
				Platform.runLater( () -> {
					setServerButton.setDisable(false);
						MessageBox.show(
							String.format("Error setting to server %s:%d [%s]", host, port, cause.toString())
						);
					}
				);
			}
			
			@Override
			public void done(JSONObject result) {
				Platform.runLater(() -> {
					serverInfoManager.newServerInfo(mainApp, host, port, result);
					dialogStage.close(); 
				});
			}
		};
		
		werkClient.getServerInfo(host, port, callback);
	}
}
