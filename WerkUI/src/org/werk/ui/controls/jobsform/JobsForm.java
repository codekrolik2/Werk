package org.werk.ui.controls.jobsform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pillar.time.interfaces.TimeProvider;
import org.pillar.time.interfaces.Timestamp;
import org.werk.exceptions.WerkException;
import org.werk.meta.JobType;
import org.werk.meta.JobTypeSignature;
import org.werk.meta.StepType;
import org.werk.processing.jobs.JobStatus;
import org.werk.restclient.WerkCallback;
import org.werk.restclient.WerkRESTClient;
import org.werk.service.JobCollection;
import org.werk.service.PageInfo;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.mainapp.MainApp;
import org.werk.ui.guice.LoaderFactory;
import org.werk.ui.util.MessageBox;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import jfxtras.scene.control.CalendarTextField;
import lombok.Getter;

public class JobsForm extends VBox {
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
	@Inject
	MainApp mainApp;
	@Inject
	TimeProvider tp;
	
	@FXML
	GridPane mainGrid;
	@FXML
	TitledPane titledPane;
	
	//--------------------------------------------------
	
	@FXML Button loadJobsButton;
	
	@FXML CalendarTextField fromDate;
	@FXML CalendarTextField toDate;
	
	@FXML CheckBox jobsPerPageCheckbox;
	@FXML TextField jobsPerPageText;
	
	//--------------------------------------------------
	
	@FXML Button refreshJobStepTypes;
	
	@FXML CheckBox nextExecutionCheckBox;
	@FXML CalendarTextField nextExecutionFromTime;
	@FXML CalendarTextField nextExecutionToTime;
	
	@FXML CheckBox jobTypesCheckBox;
	@FXML TextField jobTypesText;
	@FXML ComboBox<String> jobTypesCombo;
	@FXML Button jobTypesButton;
	@FXML Button jobTypesBackButton;

	@FXML CheckBox currentStepTypesCheckBox;
	@FXML TextField currentStepTypesText;
	@FXML ComboBox<String> currentStepTypesCombo;
	@FXML Button currentStepTypesButton;
	@FXML Button currentStepTypesBackButton;
	
	@FXML CheckBox jobIdsCheckBox;
	@FXML TextField jobIdsText;

	@FXML CheckBox jobParentIdsCheckBox;
	@FXML TextField jobParentIdsText;
	
	@FXML CheckBox jobStatusesCheckBox;
	@FXML TextField jobStatusesText;
	@FXML ComboBox<JobStatus> jobStatusesCombo;
	@FXML Button jobStatusesButton;
	@FXML Button jobStatusesBackButton;

	@FXML Pagination pagination;

	@FXML JobsTable jobsTable;
	
	@Getter
	Map<String, JobType> jobTypes;
	@Getter
	Map<String, StepType<Long>> stepTypes;
	
	boolean autoLoadJobs = false;
	
    public JobsForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("JobsForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
	public void initTable() {
		jobsTable.setMainApp(mainApp);
		jobsTable.setJobsForm(this);
	}
	
	protected void handleControlDisable(Boolean newValue, Node jobsPerPageText) {
		jobsPerPageText.setDisable(!newValue);
	}
	
	protected String removeLast(String text) {
		int index = text.lastIndexOf(",");
		if (index < 0)
			return "";
		else
			return text.substring(0, index);
	}
	
	public void moveJobStatusBack() {
		jobStatusesText.setText(removeLast(jobStatusesText.getText()));
	}

	public void moveJobTypeBack() {
		jobTypesText.setText(removeLast(jobTypesText.getText()));
	}

	public void moveCurrentStepTypeBack() {
		currentStepTypesText.setText(removeLast(currentStepTypesText.getText()));
	}
	
	public void moveJobType() {
		String newJobType = jobTypesCombo.getSelectionModel().getSelectedItem();
		if (newJobType == null)
			return;
		
		String jobTypesTxt = jobTypesText.getText();
		String[] parts = jobTypesText.getText().split(",");
		
		int partCount = 0;
		for (String part : parts) {
			if ((part != null) && (!part.trim().equals(""))) {
				partCount++;
				if (part.trim().equals(newJobType)) {
					return;
				}
			}
		}
		
		if (partCount > 0)
			jobTypesTxt += ", ";
		jobTypesTxt += newJobType;
		
		jobTypesText.setText(jobTypesTxt);
	}
	
	public void moveCurrentStepTypes() {
		String newStepType = currentStepTypesCombo.getSelectionModel().getSelectedItem();
		if (newStepType == null)
			return;
		
		String currentStepTypesTxt = currentStepTypesText.getText();
		String[] parts = currentStepTypesText.getText().split(",");
		
		int partCount = 0;
		for (String part : parts) {
			if ((part != null) && (!part.trim().equals(""))) {
				partCount++;
				if (part.trim().equals(newStepType)) {
					return;
				}
			}
		}
		
		if (partCount > 0)
			currentStepTypesTxt += ", ";
		currentStepTypesTxt += newStepType;
		
		currentStepTypesText.setText(currentStepTypesTxt);
	}
	
	public void moveJobStatus() {
		String newJobStatusStr = jobStatusesCombo.getSelectionModel().getSelectedItem().toString();
		if (newJobStatusStr == null)
			return;
		
		String jobStatusesTxt = jobStatusesText.getText();
		String[] parts = jobStatusesText.getText().split(",");
		
		int partCount = 0;
		for (String part : parts) {
			if ((part != null) && (!part.trim().equals(""))) {
				partCount++;
				if (part.trim().equals(newJobStatusStr)) {
					return;
				}
			}
		}
		
		if (partCount > 0)
			jobStatusesTxt += ", ";
		jobStatusesTxt += newJobStatusStr;
		
		jobStatusesText.setText(jobStatusesTxt);
	}
	
	public void initialize() {
		pagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				loadJobs();
			}
		});
		
		Calendar weekAgo = Calendar.getInstance();
		weekAgo.setTime(new Date());
		weekAgo.add(Calendar.DATE, -7);
		fromDate.calendarProperty().set(weekAgo);

		Calendar tomorrow = Calendar.getInstance();
		tomorrow.setTime(new Date());
		tomorrow.add(Calendar.DATE, 1);
		toDate.calendarProperty().set(tomorrow);

		jobStatusesCombo.setItems(
			FXCollections.observableArrayList(
				JobStatus.UNDEFINED,
				
				JobStatus.PROCESSING,
				JobStatus.ROLLING_BACK,
				
				JobStatus.JOINING,
				
				JobStatus.FINISHED,
				JobStatus.ROLLED_BACK,
				JobStatus.FAILED
			)
		);

		jobsPerPageText.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        if (!newValue.matches("\\d*"))
		        	jobsPerPageText.setText(newValue.replaceAll("[^\\d]", ""));
		    }
		});
		
		jobsPerPageCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	handleControlDisable(newValue, jobsPerPageText);
		    }
		});
		
		nextExecutionCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	handleControlDisable(newValue, nextExecutionFromTime);
		    	handleControlDisable(newValue, nextExecutionToTime);
		    }
		});
		
		jobTypesCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	handleControlDisable(newValue, jobTypesText);
		    	handleControlDisable(newValue, jobTypesCombo);
		    	handleControlDisable(newValue, jobTypesButton);
		    	handleControlDisable(newValue, jobTypesBackButton);
		    }
		});
		
		currentStepTypesCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	handleControlDisable(newValue, currentStepTypesText);
		    	handleControlDisable(newValue, currentStepTypesCombo);
		    	handleControlDisable(newValue, currentStepTypesButton);
		    	handleControlDisable(newValue, currentStepTypesBackButton);
		    }
		});
		
		jobIdsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	handleControlDisable(newValue, jobIdsText);
		    }
		});

		jobParentIdsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	handleControlDisable(newValue, jobParentIdsText);
		    }
		});
		
		jobStatusesCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	handleControlDisable(newValue, jobStatusesText);
		    	handleControlDisable(newValue, jobStatusesCombo);
		    	handleControlDisable(newValue, jobStatusesButton);
		    	handleControlDisable(newValue, jobStatusesBackButton);
		    }
		});
	}

    public void loadJobs() {
		if (serverInfoManager.getPort() < 0)
			MessageBox.show(String.format("Server not assigned. Please set server."));
		else {
	    	try {
				if (fromDate.getCalendar() == null)
					throw new WerkException("Time From not set");
				Optional<Timestamp> from = Optional.of(
					tp.createTimestamp(Long.toString(fromDate.getCalendar().getTime().getTime()))
				);
				
				if (toDate.getCalendar() == null)
					throw new WerkException("Time To not set");
				Optional<Timestamp> to = Optional.of(
					tp.createTimestamp(Long.toString(toDate.getCalendar().getTime().getTime()))
				);
				
				Optional<PageInfo> pageInfo = Optional.empty();
				if (jobsPerPageCheckbox.isSelected()) {
					long itemsPerPage = Long.parseLong(jobsPerPageText.getText());
					long pageNumber = pagination.getCurrentPageIndex();
					PageInfo pageInfoObj = new PageInfo(itemsPerPage, pageNumber);
					pageInfo = Optional.of(pageInfoObj);
				}
				
				Optional<Timestamp> fromExec = Optional.empty();
				Optional<Timestamp> toExec = Optional.empty();
				if (nextExecutionCheckBox.isSelected()) {
					if (nextExecutionFromTime.getCalendar() == null)
						throw new WerkException("Exec Time From not set");
					if (nextExecutionToTime.getCalendar() == null)
						throw new WerkException("Exec Time To not set");
					
					fromExec = Optional.of(
						tp.createTimestamp(Long.toString(nextExecutionFromTime.getCalendar().getTime().getTime()))
					);
					toExec = Optional.of(
						tp.createTimestamp(Long.toString(nextExecutionToTime.getCalendar().getTime().getTime()))
					);
				}
				
				Optional<Collection<Long>> parentJobIds = Optional.empty();
				if (jobParentIdsCheckBox.isSelected()) {
					List<Long> parentJobIdList = new ArrayList<>();
					String[] parts = jobParentIdsText.getText().split(",");
					
					for (String part : parts) {
						if ((part != null) && (!part.trim().equals(""))) {
							Long parentJobId;
							try {
								parentJobId = Long.parseLong(part);
							} catch(Exception e) {
								throw new WerkException(String.format("Can't parse Long ParentJobId [%s]", part));
							}
							parentJobIdList.add(parentJobId);
						}
					}
					
					if (parentJobIdList.isEmpty())
						throw new WerkException("ParentJobId List is empty");
					parentJobIds = Optional.of(parentJobIdList);
				}

				Optional<Collection<Long>> jobIds = Optional.empty();
				if (jobIdsCheckBox.isSelected()) {
					List<Long> jobIdsList = new ArrayList<>();
					String[] parts = jobIdsText.getText().split(",");
					
					for (String part : parts) {
						if ((part != null) && (!part.trim().equals(""))) {
							Long parentJobId;
							try {
								parentJobId = Long.parseLong(part);
							} catch(Exception e) {
								throw new WerkException(String.format("Can't parse Long ParentJobId [%s]", part));
							}
							jobIdsList.add(parentJobId);
						}
					}
					
					if (jobIdsList.isEmpty())
						throw new WerkException("JobId List is empty");
					jobIds = Optional.of(jobIdsList);
				}

				Optional<Set<String>> currentStepTypes = Optional.empty();
				if (currentStepTypesCheckBox.isSelected()) {
					Set<String> currentStepTypesSet = new HashSet<>();
					String[] parts = currentStepTypesText.getText().split(",");
					
					for (String part : parts) {
						if ((part != null) && (!part.trim().equals(""))) {
							if (!stepTypes.containsKey(part))
								throw new WerkException(String.format("StepType not found [%s]", part));
							currentStepTypesSet.add(part);
						}
					}
					
					if (currentStepTypesSet.isEmpty())
						throw new WerkException("StepType List is empty");
					currentStepTypes = Optional.of(currentStepTypesSet);
				}
				
				Optional<List<JobTypeSignature>> jobTypesAndVersions = Optional.empty();
				if (jobTypesCheckBox.isSelected()) {
					List<JobTypeSignature> jobTypesAndVersionsList = new ArrayList<>();
					String[] parts = jobTypesText.getText().split(",");
					
					for (String part : parts) {
						if ((part != null) && (!part.trim().equals(""))) {
							if (!jobTypes.containsKey(part))
								throw new WerkException(String.format("JobType not found [%s]", part));
							jobTypesAndVersionsList.add(jobTypes.get(part));
						}
					}
					
					if (jobTypesAndVersionsList.isEmpty())
						throw new WerkException("JobType List is empty");
					jobTypesAndVersions = Optional.of(jobTypesAndVersionsList);
				}
				
				Optional<Set<JobStatus>> jobStatuses = Optional.empty();
				if (jobStatusesCheckBox.isSelected()) {
					Set<JobStatus> jobStatusesSet = new HashSet<>();
					String[] parts = jobStatusesText.getText().split(",");
					
					for (String part : parts) {
						if ((part != null) && (!part.trim().equals(""))) {
							jobStatusesSet.add(JobStatus.valueOf(part));
						}
					}
					
					if (jobStatusesSet.isEmpty())
						throw new WerkException("JobStatus List is empty");
					jobStatuses = Optional.of(jobStatusesSet);
				}
				
				//----------------------------------------------------
				
				String host = serverInfoManager.getHost();
				int port = serverInfoManager.getPort();
				
				loadJobsButton.setDisable(true);
				pagination.setDisable(true);
				
				WerkCallback<JobCollection<Long>> callback = new WerkCallback<JobCollection<Long>>() {
					@Override
					public void error(Throwable cause) {
						Platform.runLater( () -> {
							loadJobsButton.setDisable(false);
							pagination.setDisable(false);
							MessageBox.show(
								String.format("Error processing request %s:%d [%s]", host, port, cause.toString())
							);
						});
					}
					
					@Override
					public void done(JobCollection<Long> result) {
						Platform.runLater( () -> {
							loadJobsButton.setDisable(false);
							pagination.setDisable(false);
							
							List<TableJobPOJO<Long>> filteredJobTypes = result.getJobs().stream().
									map(a -> new TableJobPOJO<Long>(a)).
									collect(Collectors.toList());
							
							jobsTable.setItems(FXCollections.observableArrayList(
								filteredJobTypes
							));
							
							if (result.getPageInfo().isPresent()) {
								PageInfo pageInfo = result.getPageInfo().get();
								
								int pageCount = (int)(result.getJobCount() / pageInfo.getItemsPerPage());
								if (result.getJobCount() % pageInfo.getItemsPerPage() > 0)
									pageCount++;
								if (pageCount == 0)
									pageCount = 1;
								
								pagination.setPageCount(pageCount);
								pagination.setCurrentPageIndex((int)pageInfo.getPageNumber());
							} else {
								pagination.setPageCount(1);
								pagination.setCurrentPageIndex(0);
							}
						});
					}
				};
				
				werkClient.getJobs(host, port, callback, 
						from, to, fromExec, toExec, jobTypesAndVersions, 
						parentJobIds, jobIds, currentStepTypes, jobStatuses, 
						pageInfo);
	    	} catch(Exception e) {
				MessageBox.show(String.format("Refresh error: [%s]", e));
				loadJobsButton.setDisable(false);
				pagination.setDisable(false);
	    	}
		}
	}
    
    public void refreshJobStepTypes() {
		if (serverInfoManager.getPort() < 0)
			MessageBox.show(String.format("Server not assigned. Please set server."));
		else {
	    	try {
				String host = serverInfoManager.getHost();
				int port = serverInfoManager.getPort();
				
				refreshJobStepTypes.setDisable(true);
				
				WerkCallback<Collection<JobType>> callback = new WerkCallback<Collection<JobType>>() {
					@Override
					public void error(Throwable cause) {
						Platform.runLater( () -> {
							refreshJobStepTypes.setDisable(false);
							MessageBox.show(
								String.format("Error processing request %s:%d [%s]", host, port, cause.toString())
							);
						});
					}
					
					@Override
					public void done(Collection<JobType> result) {
						Platform.runLater( () -> {
							jobTypes = result.stream().
								collect(Collectors.toMap(a -> JobTypeSignature.getJobTypeFullName(a), a -> a));
							refreshJobStepTypes.setDisable(false);
							jobTypesCombo.setItems(FXCollections.observableArrayList(jobTypes.keySet()));
						});

						try {
							WerkCallback<Collection<StepType<Long>>> callback = new WerkCallback<Collection<StepType<Long>>>() {
								@Override
								public void error(Throwable cause) {
									Platform.runLater( () -> {
										refreshJobStepTypes.setDisable(false);
										MessageBox.show(
											String.format("Error processing request %s:%d [%s]", host, port, cause.toString())
										);
									});
								}
								
								@Override
								public void done(Collection<StepType<Long>> result) {
									Platform.runLater( () -> {
										stepTypes = result.stream().
												collect(Collectors.toMap(a -> a.getStepTypeName(), a -> a));
										refreshJobStepTypes.setDisable(false);
										currentStepTypesCombo.setItems(FXCollections.observableArrayList(stepTypes.keySet()));
										
										if (autoLoadJobs) {
											autoLoadJobs = false;
											loadJobs();
										}
									});
								}
							};
							
							werkClient.getAllStepTypes(host, port, callback);
				    	} catch(Exception e) {
							MessageBox.show(String.format("Refresh error: [%s]", e));
							refreshJobStepTypes.setDisable(false);
				    	}
					}
				};
				
				werkClient.getJobTypes(host, port, callback);
	    	} catch(Exception e) {
				MessageBox.show(String.format("Refresh error: [%s]", e));
				refreshJobStepTypes.setDisable(false);
	    	}
		}
	}
    
	public void setJobTypes(String jobTypes) {
		jobTypesCheckBox.setSelected(true);
		jobTypesText.setText(jobTypes);
		titledPane.setExpanded(true);
		autoLoadJobs = true;
	}
    
	public void setParentJobId(Long parentJobId) {
		jobParentIdsCheckBox.setSelected(true);
		jobParentIdsText.setText(parentJobId.toString());
		titledPane.setExpanded(true);
		autoLoadJobs = true;
	}
}
