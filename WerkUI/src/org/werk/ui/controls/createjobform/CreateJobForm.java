package org.werk.ui.controls.createjobform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.engine.json.JobParameterTool;
import org.werk.exceptions.WerkException;
import org.werk.meta.JobType;
import org.werk.meta.VersionJobInitInfo;
import org.werk.meta.impl.VersionJobInitInfoImpl;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.rest.pojo.RESTJobType;
import org.werk.restclient.WerkCallback;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.parameters.DictionaryParameterInput;
import org.werk.ui.controls.parameters.DictionaryParameterInputType;
import org.werk.ui.controls.parameters.state.DictionaryParameterInit;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import jfxtras.scene.control.CalendarTextField;

public class CreateJobForm extends VBox {
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
	@Inject
	JobParameterTool jpt;
	@Inject
	TimeProvider tp;
	
	@FXML
	ComboBox<String> jobTypeComboBox;
	@FXML
	ComboBox<String> initSignatureComboBox;

	@FXML
    CheckBox executionTimeCheckBox;
	@FXML
    CalendarTextField executionTime;
	@FXML
    CheckBox jobNameCheckBox;
	@FXML
    TextField jobName;
	
	@FXML
    Button createJobButton;
	@FXML
	Button refreshButton;
	@FXML
	DictionaryParameterInput parameterInput;
	
	String jobType;
	Map<String, JobType> jobTypes;
	
    public CreateJobForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("CreateJobForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
	
    public void setJobType(String jobType) {
    	this.jobType = jobType;
    }
    
    public void initialize() {
    	jobName.setDisable(true);
    	executionTime.setDisable(true);
    	
        executionTimeCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	executionTime.setDisable(!newValue);
		    }
		});
        
        jobNameCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	jobName.setDisable(!newValue);
		    }
		});
        
        jobTypeComboBox.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				JobType type = null;
				if (newValue != null)
					type = jobTypes.get(newValue);
				if (type != null) {
					List<String> initSignatures = type.getInitParameters().keySet().stream().sorted().collect(Collectors.toList());
					initSignatureComboBox.setItems(FXCollections.observableArrayList(initSignatures));
					if (!initSignatures.isEmpty())
						initSignatureComboBox.getSelectionModel().select(0);
					else {
						DictionaryParameterInit defaultInit = new DictionaryParameterInit(true);
						parameterInput.setContext(defaultInit, DictionaryParameterInputType.JOB_CREATE);
					}
				} else {
					List<JobInputParameter> initSignature = new ArrayList<>();
					DictionaryParameterInit defaultInit = new DictionaryParameterInit(Optional.of(initSignature), true);
					parameterInput.setContext(defaultInit, DictionaryParameterInputType.JOB_CREATE);
				}
			}
		});
        
        initSignatureComboBox.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				JobType type = jobTypes.get(jobTypeComboBox.valueProperty().get());
				
				List<JobInputParameter> initSignature = type.getInitParameters().get(newValue);
				DictionaryParameterInit dictInit = new DictionaryParameterInit(Optional.ofNullable(initSignature), true);
				parameterInput.setContext(dictInit, DictionaryParameterInputType.JOB_CREATE);
			}
        });
    }

    public void createJob() {
		if (serverInfoManager.getPort() < 0)
			MessageBox.show(String.format("Server not assigned. Please set server."));
		else {
	    	try {
				String jobTypeNameV = jobTypeComboBox.valueProperty().get();
				JobType type = jobTypes.get(jobTypeNameV);
				
				if (type == null)
					throw new WerkException(
						String.format("JobType [%s] not found", jobTypeNameV)
					);
				
				Optional<String> initSignatureName = Optional.empty(); 
				Map<String, Parameter> initParameters = null;
				Optional<String> jobName = Optional.empty();
				Optional<Timestamp> nextExecutionTime = Optional.empty();
				
	    		String initSignature = initSignatureComboBox.getValue();
	    		if (initSignature == null) {
	    			if (!jpt.emptyInitParameterSetAllowed(type))
	    				throw new WerkException("Empty InitParameterSet is not allowed");
	    			DictionaryParameter prm = (DictionaryParameter)parameterInput.getParameter();
	    			initParameters = prm.getValue();
	    		} else {
	    			initSignatureName = Optional.of(initSignature);
	        		DictionaryParameter prm = (DictionaryParameter)parameterInput.getParameter();
	        		List<JobInputParameter> prms = jpt.getParameterSet(type, initSignature);
	        		
	        		initParameters = prm.getValue();
	        		jpt.checkParameters(prms, initParameters);
	    		}
	    		
				if (jobNameCheckBox.isSelected()) {
					String jobNameTxt = this.jobName.getText();
					if ((jobNameTxt == null) || (jobNameTxt.trim().equals("")))
						throw new WerkException("Job Name not set");
					
					jobName = Optional.of(jobNameTxt);
				}
				if (executionTimeCheckBox.isSelected()) {
					if (this.executionTime.getCalendar() == null)
						throw new WerkException("ExecutionTime not set");
					
					Date nextExecutionTimeDt = this.executionTime.getCalendar().getTime();
					Timestamp lt = tp.createTimestamp(Long.toString(nextExecutionTimeDt.getTime()));
					nextExecutionTime = Optional.of(lt);
				}
				
				VersionJobInitInfo init = new VersionJobInitInfoImpl(type.getJobTypeName(), initSignatureName, initParameters, 
						type.getVersion(), jobName, nextExecutionTime);
	    		
				String host = serverInfoManager.getHost();
				int port = serverInfoManager.getPort();
				
	    		createJobButton.setDisable(true);
	    		
				WerkCallback<Long> callback = new WerkCallback<Long>() {
					@Override
					public void error(Throwable cause) {
						Platform.runLater( () -> {
							createJobButton.setDisable(false);
							MessageBox.show(
								String.format("Error processing request %s:%d [%s]", host, port, cause.toString())
							);
						});
					}
					
					@Override
					public void done(Long result) {
						Platform.runLater( () -> {
							createJobButton.setDisable(false);
							MessageBox.show(
								String.format("Job created: Id [%d]", result)
							);
						});
					}
				};
				
				werkClient.createJobOfVersion(host, port, callback, init);
	    	} catch(WerkException e) {
				MessageBox.show(String.format("Create job error: [%s]", e.getMessage()));
				createJobButton.setDisable(false);
	    	} catch(Exception e) {
				MessageBox.show(String.format("Create job error: [%s]", e));
				createJobButton.setDisable(false);
	    	}
		}
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
							
							jobTypes = result.stream().collect(Collectors.toMap(a -> ((RESTJobType)a).getFullName(), a -> a));
							
							jobTypeComboBox.setItems(
								FXCollections.observableArrayList(
									result.stream().map(a -> ((RESTJobType)a).getFullName()).sorted().collect(Collectors.toList())
								)
							);
							
							jobTypeComboBox.getSelectionModel().select(jobType);
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
