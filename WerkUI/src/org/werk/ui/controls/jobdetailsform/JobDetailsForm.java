package org.werk.ui.controls.jobdetailsform;

import java.io.IOException;
import java.util.Date;

import org.pillar.time.LongTimestamp;
import org.werk.meta.JobTypeSignature;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.rest.pojo.RESTJob;
import org.werk.restclient.WerkCallback;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.parameters.DictionaryParameterInput;
import org.werk.ui.guice.LoaderFactory;
import org.werk.ui.util.MessageBox;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class JobDetailsForm extends VBox {
	@FXML TextField jobIdField;
	@FXML Button loadJobButton;

	@FXML Label jobIdLabel;
	@FXML Label jobStatusLabel;
	@FXML Label jobNameLabel;
	@FXML Label jobTypeLabel;
	@FXML Label currentStepLabel;
	@FXML Label stepCountLabel;
	@FXML Label parentJobIdLabel;
	@FXML Label creationTimeLabel;
	@FXML Label nextExecutionTimeLabel;
	@FXML TextArea jobJSONTextArea;
	
	@FXML JoinStatusRecordForm joinStatusRecordForm;
	
	@FXML DictionaryParameterInput jobParameters;
	@FXML DictionaryParameterInput initJobParameters;
	@FXML JobStepsForm jobSteps;
	
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
	
	public JobDetailsForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("JobDetailsForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	public void initialize() {
		joinStatusRecordForm.managedProperty().bind(joinStatusRecordForm.visibleProperty());
	}
	
	public void loadJob() {
		try {
			Long jobId = Long.parseLong(jobIdField.getText());
			setJobId(jobId);
		} catch (Exception e) {
			MessageBox.show(
					String.format("JobId should be of type Long [%s]", jobIdField.getText())
				);
		}
	}
	
	public void setJob(ReadOnlyJob<Long> readOnlyJob) {
		jobIdLabel.setText("Job Id: " + Long.toString(readOnlyJob.getJobId()));
		jobStatusLabel.setText("Status: " + readOnlyJob.getStatus().toString());
		
		if (readOnlyJob.getJoinStatusRecord().isPresent()) {
			joinStatusRecordForm.setVisible(true);
			joinStatusRecordForm.setJoinStatusRecord(readOnlyJob.getJoinStatusRecord().get());
		} else 
			joinStatusRecordForm.setVisible(false);
		
		if (readOnlyJob.getJobName().isPresent())
			jobNameLabel.setText("Job Name: " + readOnlyJob.getJobName().get());
		else
			jobNameLabel.setText("Job Name:");
		
		jobTypeLabel.setText("JobType/Version: " +
				JobTypeSignature.getJobTypeFullName(readOnlyJob.getJobTypeName(), readOnlyJob.getVersion()));
		currentStepLabel.setText("Current Step Type Name: " + readOnlyJob.getCurrentStepTypeName());
		stepCountLabel.setText("Step Count: " + Long.toString(readOnlyJob.getStepCount()));
		
		if (readOnlyJob.getParentJobId().isPresent())
			parentJobIdLabel.setText("Parent Job Id: " + Long.toString(readOnlyJob.getParentJobId().get()));
		else
			parentJobIdLabel.setText("Parent Job Id:");
		
		creationTimeLabel.setText("Creation Time: " +
				new Date(((LongTimestamp)readOnlyJob.getCreationTime()).getUnixTime()).toString());
		nextExecutionTimeLabel.setText("Next Execution Time: " +
				new Date(((LongTimestamp)readOnlyJob.getNextExecutionTime()).getUnixTime()).toString());
		
		jobJSONTextArea.setText(((RESTJob<Long>)readOnlyJob).getJson().toString(4));
		
		/*jobParameters
		initJobParameters*/
		try {
			jobSteps.setJob(readOnlyJob);
		} catch (Exception e) {
			MessageBox.show(String.format("Job Steps error: [%s]", e));
			e.printStackTrace();
		}
	}
	
    public void setJobId(Long jobId) {
    	jobIdField.setText(jobId.toString());
		if (serverInfoManager.getPort() < 0)
			MessageBox.show(String.format("Server not assigned. Please set server."));
		else {
	    	try {
				String host = serverInfoManager.getHost();
				int port = serverInfoManager.getPort();
				
				loadJobButton.setDisable(true);
				
				WerkCallback<ReadOnlyJob<Long>> callback = new WerkCallback<ReadOnlyJob<Long>>() {
					@Override
					public void error(Throwable cause) {
						Platform.runLater( () -> {
							loadJobButton.setDisable(false);
							MessageBox.show(
								String.format("Error processing request %s:%d [%s]", host, port, cause.toString())
							);
						});
					}
					
					@Override
					public void done(ReadOnlyJob<Long> result) {
						Platform.runLater( () -> {
							setJob(result);
							loadJobButton.setDisable(false);
						});
					}
				};
				
				werkClient.getJobAndHistory(host, port, callback, jobId);
	    	} catch(Exception e) {
				MessageBox.show(String.format("Refresh error: [%s]", e));
				loadJobButton.setDisable(false);
	    	}
		}
	}
}
