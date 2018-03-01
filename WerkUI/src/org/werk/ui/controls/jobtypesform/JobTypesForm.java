package org.werk.ui.controls.jobtypesform;

import java.io.IOException;
import java.util.Collection;

import org.werk.meta.JobType;
import org.werk.restclient.WerkCallback;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.mainapp.MainApp;
import org.werk.ui.guice.LoaderFactory;
import org.werk.ui.util.MessageBox;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class JobTypesForm extends VBox {
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
	@Inject
	MainApp mainApp;
	
	@FXML
    Button refreshButton;
	@FXML
	JobTypesTable table;
	
	public JobTypesForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("JobTypesForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	public void initTable() {
		table.setMainApp(mainApp);
	}
	
    public void refresh() {
		if (serverInfoManager.getPort() < 0)
			MessageBox.show(String.format("Server not assigned. Please set server."));
		else {
	    	try {
				String host = serverInfoManager.getHost();
				int port = serverInfoManager.getPort();
				
				refreshButton.setDisable(true);
				
				WerkCallback<Collection<JobType>> callback = new WerkCallback<Collection<JobType>>() {
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
					public void done(Collection<JobType> result) {
						Platform.runLater( () -> {
							refreshButton.setDisable(false);
							table.setItems(FXCollections.observableArrayList(result));
						});
					}
				};
				
				werkClient.getJobTypes(host, port, callback);
	    	} catch(Exception e) {
				MessageBox.show(String.format("Refresh error: [%s]", e));
				refreshButton.setDisable(false);
	    	}
		}
	}
}

