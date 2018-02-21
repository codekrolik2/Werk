package org.werk.ui.controls.connectform;

import java.io.IOException;

import org.json.JSONObject;
import org.werk.restclient.Callback;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.mainapp.MainAppControl;
import org.werk.ui.guice.FXMLLoaderFactory;
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

public class ConnectFormController extends GridPane {
	@FXML
    private TextField hostTextField;
	@FXML
    private TextField portTextField;
	@FXML
    private Button connectButton;
	@Setter
	private Stage dialogStage;
	
	@Inject
	WerkRESTClient werkClient;
	@Inject
	MainAppControl mainApp;
	@Inject
	ServerInfoManager serverInfoManager;
	
	@Inject
	public ConnectFormController(FXMLLoaderFactory loaderFactory) {
        FXMLLoader fxmlLoader = loaderFactory.loader(getClass().getResource("ConnectForm.fxml"));
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
    private void connect() {
		String host = hostTextField.getText();
		int port = Integer.parseInt(portTextField.getText());
		
		connectButton.setDisable(true);
		serverInfoManager.resetServerInfo(mainApp);
		
		Callback<JSONObject> callback = new Callback<JSONObject>() {
			@Override
			public void error(Throwable cause) {
				Platform.runLater( () -> {
						connectButton.setDisable(false);
						MessageBox.show(
							String.format("Error connecting to server %s:%d [%s]", host, port, cause.toString())
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
