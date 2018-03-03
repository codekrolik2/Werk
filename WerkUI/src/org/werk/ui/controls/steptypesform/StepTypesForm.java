package org.werk.ui.controls.steptypesform;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.werk.meta.JobTypeSignature;
import org.werk.meta.StepType;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class StepTypesForm extends VBox {
	@FXML
    Button refreshButton;
	@FXML
	StepTypesTable table;
	@FXML
    TextField stepTypeFilter;
	@FXML
    TextField jobTypeFilter;
	
	@Inject
	MainApp mainApp;
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
	
	Collection<StepType<Long>> stepTypes;
	
	public StepTypesForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("StepTypesForm.fxml"));
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
    
	public void initialize() {
		jobTypeFilter.textProperty().addListener((observable, oldValue, newValue) -> {
			applyFilter();
		});		
		stepTypeFilter.textProperty().addListener((observable, oldValue, newValue) -> {
			applyFilter();
		});		
	}
	
	protected boolean stepTypeFilter(StepType<Long> stepType) {
		String stepFilter = stepTypeFilter.getText();
		if ((stepFilter != null) && (!stepFilter.trim().equals("")))
			if (!stepType.getStepTypeName().contains(stepFilter))
				return false;
		
		boolean pass = true;
		String jobFilter = jobTypeFilter.getText();
		if ((jobFilter != null) && (!jobFilter.trim().equals(""))) {
			pass = false;
			for (JobTypeSignature jobType : stepType.getJobTypes()) {
				if (JobTypeSignature.getJobTypeFullName(jobType).contains(jobFilter)) {
					pass = true;
					break;
				}
			}
		}
		
		return pass;
	}
	
	protected void applyFilter() {
		List<StepType<Long>> filteredStepTypes = stepTypes.stream().
			filter(a -> stepTypeFilter(a)).
			collect(Collectors.toList());
		
		table.setItems(FXCollections.observableArrayList(filteredStepTypes));
	}
	
	@FXML
    public void refresh() {
		if (serverInfoManager.getPort() < 0)
			MessageBox.show(String.format("Server not assigned. Please set server."));
		else {
	    	try {
				String host = serverInfoManager.getHost();
				int port = serverInfoManager.getPort();
				
				refreshButton.setDisable(true);
				
				WerkCallback<Collection<StepType<Long>>> callback = new WerkCallback<Collection<StepType<Long>>>() {
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
					public void done(Collection<StepType<Long>> result) {
						Platform.runLater( () -> {
							stepTypes = result;
							refreshButton.setDisable(false);
							applyFilter();
						});
					}
				};
				
				werkClient.getAllStepTypes(host, port, callback);
	    	} catch(Exception e) {
				MessageBox.show(String.format("Refresh error: [%s]", e));
				refreshButton.setDisable(false);
	    	}
		}
	}
}
