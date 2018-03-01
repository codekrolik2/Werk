package org.werk.ui.controls.jobtypeinfoform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.rest.pojo.RESTJobType;
import org.werk.restclient.WerkCallback;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.mainapp.MainApp;
import org.werk.ui.controls.steptypesform.StepTypesTable;
import org.werk.ui.guice.LoaderFactory;
import org.werk.ui.util.MessageBox;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class JobTypeInfoForm extends VBox {
    @FXML Button createJobButton;
    @FXML Button showJobsButton;
    
    @FXML Label jobTypeLabel;
    @FXML Label firstStepLabel;
    @FXML Label acyclicLabel;
    @FXML Label historyLimitLabel;
    @FXML Label historyOverflowLabel;
    
    @FXML StepTypesTable stepsTable;
    
    @FXML TextArea initSignaturesTextArea;
    @FXML TextArea descriptionTextArea;
    @FXML TextArea jobConfigTextArea;
    @FXML TextArea jobJSONTextArea;
	
    @Inject
    MainApp mainApp;
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
    
    JobType jobType;
    
    public JobTypeInfoForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("JobTypeInfoForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    public void showCreateJobForm() {
		mainApp.createCreateJobTab(((RESTJobType)jobType).getFullName());
    }
    
	public void initTable() {
		stepsTable.hideJobs();
		stepsTable.setMainApp(mainApp);
	}
    
    public void setJobType(JobType jobType) {
    	this.jobType = jobType;

        jobTypeLabel.setText(
        	String.format("JobType/Version: %s", ((RESTJobType)jobType).getFullName())
        );
        firstStepLabel.setText(
        	String.format("First Step: %s", jobType.getFirstStepTypeName())
        );
        acyclicLabel.setText(
        	String.format("Acyclic: %b", jobType.isForceAcyclic())
        );
        historyLimitLabel.setText(
        	String.format("History Limit: %d", jobType.getHistoryLimit())
        );
        historyOverflowLabel.setText(
        	String.format("History Overflow Action: %s", jobType.getHistoryOverflowAction().toString())
        );

        descriptionTextArea.setText(jobType.getDescription());
        jobConfigTextArea.setText(jobType.getJobConfig());
        jobJSONTextArea.setText(((RESTJobType)jobType).getJson().toString(4));
        
        //Init signatures
        StringBuilder signatures = new StringBuilder();
        for (Entry<String, List<JobInputParameter>> ent : jobType.getInitParameters().entrySet()) {
        	StringBuilder signature = new StringBuilder();
        	signature.append(ent.getKey()).append("(");
        	boolean first = true;
        	for (JobInputParameter parameter : ent.getValue()) {
        		if (!first)
        			signature.append(",\n    ");
        		signature.append(parameter.toString());
        		first = false;
        	}
        	signature.append(");\n\n");
        	
        	signatures.append(signature.toString());
        }
        
        initSignaturesTextArea.setText(signatures.toString());
        
        //Steps
		if (serverInfoManager.getPort() < 0)
			MessageBox.show(String.format("Server not assigned. Can't load steps."));
		else {
			String host = serverInfoManager.getHost();
			int port = serverInfoManager.getPort();
			
			WerkCallback<Collection<StepType<Long>>> callback = new WerkCallback<Collection<StepType<Long>>>() {
				@Override
				public void error(Throwable cause) {
					Platform.runLater( () -> {
						MessageBox.show(
							String.format("Can't load steps %s:%d [%s]", host, port, cause.toString())
						);
					});
				}
				
				@Override
				public void done(Collection<StepType<Long>> result) {
					Platform.runLater( () -> {
						Map<String, StepType<Long>> stepMap = result.stream().
								collect(Collectors.toMap(a -> a.getStepTypeName(), a -> a));
						
						if (!stepMap.containsKey(jobType.getFirstStepTypeName()))
							stepsTable.setItems(FXCollections.observableArrayList(result));
						else {
							List<StepType<Long>> stepList = new ArrayList<>();
							Queue<StepType<Long>> queue = new LinkedList<>();
							queue.add(stepMap.remove(jobType.getFirstStepTypeName()));
							
							while (!queue.isEmpty()) {
								StepType<Long> step = queue.poll();
								for (String transition : step.getAllowedTransitions()) {
									StepType<Long> nextStep = stepMap.remove(transition);
									if (nextStep != null)
										queue.add(nextStep);
								}
								stepList.add(step);
							}
							
							stepList.addAll(stepMap.values());
							
							stepsTable.setItems(FXCollections.observableArrayList(stepList));
						}
					});
				}
			};
			
	        werkClient.getStepTypesForJob(host, port, callback, jobType.getJobTypeName(), Optional.of(jobType.getVersion()));
		}
    }
}
