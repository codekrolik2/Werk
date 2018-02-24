package org.werk.ui.controls.steptypeinfoform;

import java.io.IOException;
import java.util.Collection;

import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.rest.pojo.RESTStepType;
import org.werk.restclient.WerkCallback;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.jobtypesform.JobTypesTable;
import org.werk.ui.controls.mainapp.MainApp;
import org.werk.ui.controls.steptypesform.StepTypesTable;
import org.werk.ui.guice.LoaderFactory;
import org.werk.ui.util.MessageBox;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class StepTypeInfoForm extends VBox {
	@FXML Label stepTypeLabel;
	@FXML Label stepExecLabel;
	@FXML Label transitionerLabel;
	@FXML Label logLimitLabel;
	@FXML Label logOverflowLabel;
	@FXML Label shortTransactionLabel;
	
	@FXML JobTypesTable jobTypesTable;
	@FXML StepTypesTable transitionsTable;
	@FXML StepTypesTable rollbackTransitionsTable;
	
	@FXML TextArea execConfigTextArea;
	@FXML TextArea transitionerConfigTextArea;
	@FXML TextArea processingDescriptionTextArea;
	@FXML TextArea rollbackDescriptionTextArea;
	@FXML TextArea stepJSONTextArea;
	
    @Inject
    MainApp mainApp;
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
    
    StepType<Long> stepType;
    
    public StepTypeInfoForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("StepTypeInfoForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
	public void initTables() {
		jobTypesTable.hideCreateShowJobs();
		jobTypesTable.setMainApp(mainApp);
		transitionsTable.setMainApp(mainApp);
		rollbackTransitionsTable.setMainApp(mainApp);
	}
    
    public void setStepType(StepType<Long> stepType) {
    	this.stepType = stepType;

    	stepTypeLabel.setText(
        	String.format("Step Type: %s", stepType.getStepTypeName())
        );
    	stepExecLabel.setText(
        	String.format("StepExec: %s", ((RESTStepType<Long>)stepType).getStepExecName())
        );
    	transitionerLabel.setText(
        	String.format("Transitioner: %s", ((RESTStepType<Long>)stepType).getTransitionerName())
        );
    	logLimitLabel.setText(
        	String.format("Log Limit: %d", stepType.getLogLimit())
        );
    	logOverflowLabel.setText(
        	String.format("Log Overflow Action: %s", stepType.getLogOverflowAction().toString())
        );
    	shortTransactionLabel.setText(
        	String.format("Short Transaction: %b", stepType.isShortTransaction())
        );

    	execConfigTextArea.setText(stepType.getExecConfig());
    	transitionerConfigTextArea.setText(stepType.getTransitionerConfig());
    	processingDescriptionTextArea.setText(stepType.getProcessingDescription());
    	rollbackDescriptionTextArea.setText(stepType.getRollbackDescription());
    	stepJSONTextArea.setText(((RESTStepType<Long>)stepType).getJsonObj().toString(4));
        
		if (serverInfoManager.getPort() < 0)
			MessageBox.show(String.format("Server not assigned. Can't load steps/jobs."));
		else {
			String host = serverInfoManager.getHost();
			int port = serverInfoManager.getPort();
			
			WerkCallback<Collection<JobType>> callbackJobTypes = new WerkCallback<Collection<JobType>>() {
				@Override
				public void error(Throwable cause) {
					Platform.runLater( () -> {
						MessageBox.show(
							String.format("Error loading JobTypeList %s:%d [%s]", host, port, cause.toString())
						);
					});
				}
				
				@Override
				public void done(Collection<JobType> result) {
					Platform.runLater( () -> {
						jobTypesTable.setItems(FXCollections.observableArrayList(result));
					});
				}
			};
			
			werkClient.getJobTypesForStep(host, port, callbackJobTypes, stepType.getStepTypeName());

			WerkCallback<Collection<StepType<Long>>> callbackTransitions = new WerkCallback<Collection<StepType<Long>>>() {
				@Override
				public void error(Throwable cause) {
					Platform.runLater( () -> {
						MessageBox.show(
							String.format("Error loading Transitions %s:%d [%s]", host, port, cause.toString())
						);
					});
				}
				
				@Override
				public void done(Collection<StepType<Long>> result) {
					Platform.runLater( () -> {
						transitionsTable.setItems(FXCollections.observableArrayList(result));
					});
				}
			};
			
			werkClient.getStepTransitions(host, port, callbackTransitions, stepType.getStepTypeName());

			WerkCallback<Collection<StepType<Long>>> callbackRollbackTransitions = new WerkCallback<Collection<StepType<Long>>>() {
				@Override
				public void error(Throwable cause) {
					Platform.runLater( () -> {
						MessageBox.show(
							String.format("Error loading Rollback Transitions %s:%d [%s]", host, port, cause.toString())
						);
					});
				}
				
				@Override
				public void done(Collection<StepType<Long>> result) {
					Platform.runLater( () -> {
						rollbackTransitionsTable.setItems(FXCollections.observableArrayList(result));
					});
				}
			};
			
			werkClient.getStepRollbackTransitions(host, port, callbackRollbackTransitions, stepType.getStepTypeName());
		}
    }
}
