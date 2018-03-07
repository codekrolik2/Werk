package org.werk.ui.controls.jobrestartform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.werk.data.StepPOJO;
import org.werk.meta.JobRestartInfo;
import org.werk.meta.NewStepRestartInfo;
import org.werk.meta.StepType;
import org.werk.meta.impl.JobRestartInfoImpl;
import org.werk.meta.impl.NewStepRestartInfoImpl;
import org.werk.processing.jobs.JoinStatusRecord;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.impl.DictionaryParameterImpl;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.rest.pojo.RESTJob;
import org.werk.rest.serializers.RestartJoinStatusRecord;
import org.werk.restclient.WerkCallback;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.mainapp.MainApp;
import org.werk.ui.controls.parameters.DictionaryParameterInput;
import org.werk.ui.controls.parameters.DictionaryParameterInputType;
import org.werk.ui.controls.parameters.state.DictionaryParameterInit;
import org.werk.ui.controls.parameters.state.ParameterStateException;
import org.werk.ui.guice.LoaderFactory;
import org.werk.ui.util.MessageBox;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RestartJobForm extends VBox {
	@FXML TextField jobIdField;
	@FXML Button loadJobButton;
	@FXML Button restartJobButton;
	
	@FXML DictionaryParameterInput initJobParameters;
	@FXML DictionaryParameterInput jobParameters;
	@FXML DictionaryParameterInput stepParameters;
	
	@FXML TextArea jobJSONTextArea;
	
	@FXML CheckBox joinRecordCheckbox;
	@FXML TextField joinJobIdsField;
	@FXML TextField joinParameterNameField;
	@FXML TextField waitForNJobsField;
	
	@FXML CheckBox createNewStepCheckBox;
	@FXML ComboBox<String> newStepTypeNameCombo;
	@FXML ComboBox<Boolean> isNewStepRollbackCombo;
	@FXML TextField stepsToRollbackField;
	
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
	@Inject
	MainApp mainApp;
	
	ReadOnlyJob<Long> currentJob = null;
	StepPOJO lastStep = null;
	
    public RestartJobForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("RestartJobForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    public void initialize() {
    	isNewStepRollbackCombo.setItems(FXCollections.observableArrayList(true, false));
    	
    	joinRecordCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	joinJobIdsField.setDisable(!newValue);
		    	joinParameterNameField.setDisable(!newValue);
		    	waitForNJobsField.setDisable(!newValue);
		    }
		});
    	
    	createNewStepCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	newStepTypeNameCombo.setDisable(!newValue);
		    	isNewStepRollbackCombo.setDisable(!newValue);
		    	stepsToRollbackField.setDisable(!newValue);
		    	
		    	if (!newValue)
		    		setLastStepParameters(lastStep);
		    	else {
			    	DictionaryParameter stepPrmDictPrm = new DictionaryParameterImpl();
					DictionaryParameterInit stepPrmInit = new DictionaryParameterInit(stepPrmDictPrm, false);
			    	stepParameters.setContext(stepPrmInit, DictionaryParameterInputType.JOB_RESTART);
		    	}
		    }
		});
    }
    
    public void setJob(ReadOnlyJob<Long> readOnlyJob) throws Exception {
    	currentJob = readOnlyJob;
    	
		DictionaryParameter initDictPrm = new DictionaryParameterImpl(readOnlyJob.getJobInitialParameters());
		DictionaryParameterInit initDictParameterInit = new DictionaryParameterInit(initDictPrm, false);
    	initJobParameters.setContext(initDictParameterInit, DictionaryParameterInputType.JOB_RESTART);
    	
		DictionaryParameter jobPrmDictPrm = new DictionaryParameterImpl(readOnlyJob.getJobParameters());
		DictionaryParameterInit jobPrmInit = new DictionaryParameterInit(jobPrmDictPrm, false);
    	jobParameters.setContext(jobPrmInit, DictionaryParameterInputType.JOB_RESTART);
    	
    	lastStep = null;
    	Collection<StepPOJO> steps = readOnlyJob.getProcessingHistory();
    	for (StepPOJO step : steps)
    		if ((lastStep == null) || (lastStep.getStepNumber() < step.getStepNumber()))
    			lastStep = step;
    	
    	setLastStepParameters(lastStep);
    	
		jobJSONTextArea.setText(((RESTJob<Long>)readOnlyJob).getJson().toString(4));
    }
    
    protected void setLastStepParameters(StepPOJO lastStep) {
		DictionaryParameter stepPrmDictPrm = new DictionaryParameterImpl(lastStep.getStepParameters());
		DictionaryParameterInit stepPrmInit = new DictionaryParameterInit(stepPrmDictPrm, false);
    	stepParameters.setContext(stepPrmInit, DictionaryParameterInputType.JOB_RESTART);
    }
    
    protected void fillParameters(Map<String, Parameter> parametersUpdate, List<String> parametersToRemove,
    		Map<String, Parameter> originalParameters, Map<String, Parameter> newParameters) {
    	for (Entry<String, Parameter> ent : originalParameters.entrySet()) {
    		if (!newParameters.containsKey(ent.getKey()))
    			parametersToRemove.add(ent.getKey());
    	}
    	
    	for (Entry<String, Parameter> ent : newParameters.entrySet()) {
    		Parameter originalParameter = originalParameters.get(ent.getKey());
    		if (originalParameter == null)
    			parametersUpdate.put(ent.getKey(), ent.getValue());
    		else {
    			if (!originalParameter.equals(ent.getValue()))
    				parametersUpdate.put(ent.getKey(), ent.getValue());
    		}
    	}
    }
    
    public void restartJob() throws ParameterStateException {
		if (serverInfoManager.getPort() < 0) {
			MessageBox.show(String.format("Server not assigned. Please set server."));
			return;
		}
		
    	try {
			String host = serverInfoManager.getHost();
			int port = serverInfoManager.getPort();
	    	
	    	Long jobId = currentJob.getJobId();
	    	
	    	Optional<NewStepRestartInfo> newStepInfo = Optional.empty();
	    	if (joinRecordCheckbox.selectedProperty().get()) {
	        	String newStepTypeName = newStepTypeNameCombo.getValue();
	        	boolean isNewStepRollback = isNewStepRollbackCombo.getValue();
	        	
	        	Optional<List<Integer>> stepsToRollback = Optional.empty();
	        	
	    		String stepsToRollbackStr = stepsToRollbackField.getText().trim();
	        	if (!stepsToRollbackStr.equals("")) {
	        		List<Integer> stepsToRollbackList = new ArrayList<>();
	        		for (String s : stepsToRollbackStr.split(","))
	        			stepsToRollbackList.add(Integer.parseInt(s.trim()));
	        		stepsToRollback = Optional.of(stepsToRollbackList);
	        	}
	        	
	        	NewStepRestartInfo info = new NewStepRestartInfoImpl(newStepTypeName, isNewStepRollback, stepsToRollback);
	        	newStepInfo = Optional.of(info);
	    	}
	    	
	    	Optional<JoinStatusRecord<Long>> joinStatusRecord = Optional.empty();
	    	if (createNewStepCheckBox.selectedProperty().get()) {
	        	String joinParameterName = joinParameterNameField.getText();
	        	int waitForNJobs = -1;
	       		String jobsText = waitForNJobsField.getText().trim();
	       		if (!jobsText.equals(""))
	       			waitForNJobs = Integer.parseInt(jobsText);
	        	
	    		String joinJobIdsStr = joinJobIdsField.getText().trim();
	        	List<Long> joinedJobIds = new ArrayList<>();
	        	if (!joinJobIdsStr.equals(""))
	        		for (String s : joinJobIdsStr.split(","))
	        			joinedJobIds.add(Long.parseLong(s.trim()));
	        	
	        	JoinStatusRecord<Long> jsr = new RestartJoinStatusRecord<>(joinedJobIds, 
	        			joinParameterName, waitForNJobs);
	        	joinStatusRecord = Optional.of(jsr);
	    	}
	
	    	Map<String, Parameter> jobInitParametersUpdate = new HashMap<>();
	    	List<String> jobInitParametersToRemove = new ArrayList<>();
	    	fillParameters(jobInitParametersUpdate, jobInitParametersToRemove,
	    			currentJob.getJobInitialParameters(), 
	    			((DictionaryParameter)initJobParameters.getParameter()).getValue());
	    	
	    	Map<String, Parameter> jobParametersUpdate = new HashMap<>();
	    	List<String> jobParametersToRemove = new ArrayList<>();
	    	fillParameters(jobParametersUpdate, jobParametersToRemove,
	    			currentJob.getJobParameters(),
					((DictionaryParameter)jobParameters.getParameter()).getValue());
	    	
	    	Map<String, Parameter> stepParametersUpdate = new HashMap<>();
	    	List<String> stepParametersToRemove = new ArrayList<>();
	    	fillParameters(stepParametersUpdate, stepParametersToRemove,
	    			lastStep.getStepParameters(),
					((DictionaryParameter)stepParameters.getParameter()).getValue());
	    	
	    	JobRestartInfo<Long> info = new JobRestartInfoImpl<Long>(jobId, jobInitParametersUpdate,
	    		jobInitParametersToRemove, jobParametersUpdate, jobParametersToRemove, stepParametersUpdate,
	    		stepParametersToRemove, newStepInfo, joinStatusRecord);
			
	    	disableEnableControls(true);
	    	
	    	WerkCallback<Long> callback = new WerkCallback<Long>() {
				@Override
				public void error(Throwable cause) {
					Platform.runLater( () -> {
						disableEnableControls(false);
						MessageBox.show(
							String.format("Error processing request %s:%d [%s]", host, port, cause.toString())
						);
					});
				}

				@Override
				public void done(Long result) {
					Platform.runLater( () -> {
						disableEnableControls(false);
						MessageBox.show(
							String.format("Job restarted. JobId: [%d]", result)
						);
					});
				}
	    	};
	    	
			werkClient.restartJob(host, port, callback, info);
    	} catch(Exception e) {
			MessageBox.show(String.format("Refresh error: [%s]", e));
			disableEnableControls(false);
    	}
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
	
	protected void disableEnableControls(boolean disable) {
		loadJobButton.setDisable(disable);
    	restartJobButton.setDisable(disable);
    	joinRecordCheckbox.setDisable(disable);
    	createNewStepCheckBox.setDisable(disable);
	}
	
    public void setJobId(Long jobId) {
    	jobIdField.setText(jobId.toString());
		if (serverInfoManager.getPort() < 0)
			MessageBox.show(String.format("Server not assigned. Please set server."));
		else {
	    	try {
				String host = serverInfoManager.getHost();
				int port = serverInfoManager.getPort();
				
				disableEnableControls(true);
				currentJob = null;
				
				WerkCallback<ReadOnlyJob<Long>> callback = new WerkCallback<ReadOnlyJob<Long>>() {
					@Override
					public void error(Throwable cause) {
						Platform.runLater( () -> {
							disableEnableControls(false);
							MessageBox.show(
								String.format("Error processing request %s:%d [%s]", host, port, cause.toString())
							);
						});
					}
					
					@Override
					public void done(ReadOnlyJob<Long> result) {
						Platform.runLater( () -> {
							try {
								setJob(result);
								
								WerkCallback<Collection<StepType<Long>>> callback = new WerkCallback<Collection<StepType<Long>>>() {
									@Override
									public void error(Throwable cause) {
										Platform.runLater( () -> {
											disableEnableControls(false);
											MessageBox.show(
												String.format("Error processing request %s:%d [%s]", host, port, cause.toString())
											);
										});
									}
									
									@Override
									public void done(Collection<StepType<Long>> result) {
										try {
											List<String> stepTypes = result.stream().
													map(a -> a.getStepTypeName()).
													collect(Collectors.toList());
											
											newStepTypeNameCombo.setItems(FXCollections.observableArrayList(stepTypes));
										} catch (Exception e) {
											MessageBox.show(String.format("Set StepTypes For Job error: [%s]", e));
										} finally {
											disableEnableControls(false);
										}
									}
								};
								
								werkClient.getStepTypesForJob(host, port, callback, result.getJobTypeName(),
										Optional.of(result.getVersion()));
							} catch (Exception e) {
								MessageBox.show(String.format("Set job error: [%s]", e));
								disableEnableControls(false);
							}
						});
					}
				};
				
				werkClient.getJobAndHistory(host, port, callback, jobId);
	    	} catch(Exception e) {
				MessageBox.show(String.format("Refresh error: [%s]", e));
				disableEnableControls(false);
	    	}
		}
	}
}
