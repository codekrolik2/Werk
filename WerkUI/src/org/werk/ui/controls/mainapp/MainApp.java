package org.werk.ui.controls.mainapp;

import java.io.IOException;

import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.rest.pojo.RESTJobType;
import org.werk.ui.TabCreator;
import org.werk.ui.controls.createjobform.CreateJobForm;
import org.werk.ui.controls.jobsform.JobsForm;
import org.werk.ui.controls.jobtypeinfoform.JobTypeInfoForm;
import org.werk.ui.controls.jobtypesform.JobTypesForm;
import org.werk.ui.controls.serverinfoform.ServerInfoForm;
import org.werk.ui.controls.setserverform.SetServerForm;
import org.werk.ui.controls.steptypeinfoform.StepTypeInfoForm;
import org.werk.ui.controls.steptypesform.StepTypesForm;
import org.werk.ui.guice.LoaderFactory;
import org.werk.ui.util.ModalWindow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;

public class MainApp extends VBox {
	@Setter
	TabCreator tabCreator;
	
	@Setter
	Stage main;
	
	@FXML
	Label serverInfoLabel;
	
	@FXML
	TabPane tabs;
	
    public MainApp() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("MainApp.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
	
	public void showSetServerDialog() {
		SetServerForm setServerForm = tabCreator.getSetServerForm();
		ModalWindow window = new ModalWindow(setServerForm, "Set Server");
		Stage dialogStage = window.showModal(main, this.getScene().getWindow());
		setServerForm.setDialogStage(dialogStage);
	}
	
	public void setStatusText(String text) {
		serverInfoLabel.setText(text);
	}
	
	public void createServerInfoTab() {
		ServerInfoForm serverInfoController = tabCreator.getServerInfoForm();
		final Tab tab = new Tab("Server Info", serverInfoController);
		tab.setClosable(true);
        
		tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        
        serverInfoController.setServerInfo();
    }
	
	public void createJobTypesTab() {
		JobTypesForm jobTypesFormControl = tabCreator.getJobTypesForm();
		final Tab tab = new Tab("Job Types", jobTypesFormControl);
		tab.setClosable(true);
        
		tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        
        jobTypesFormControl.refresh();
    }
	
	public void createStepTypesTab() {
		StepTypesForm stepTypesFormControl = tabCreator.getStepTypesForm();
		final Tab tab = new Tab("Step Types", stepTypesFormControl);
		tab.setClosable(true);
        
		tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        
        stepTypesFormControl.refresh();
    }
	
	public void createJobTypeTab(JobType jobType) {
		JobTypeInfoForm jobTypeInfoForm = tabCreator.getJobTypeInfoForm(jobType);
		final Tab tab = new Tab("JobType: " + ((RESTJobType)jobType).getFullName(), jobTypeInfoForm);
		tab.setClosable(true);
        
		tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
    }
	
	public void createStepTypeTab(StepType<Long> stepType) {
		StepTypeInfoForm stepTypeInfoForm = tabCreator.getStepTypeInfoForm(stepType);
		final Tab tab = new Tab("StepType: " + stepType.getStepTypeName(), stepTypeInfoForm);
		tab.setClosable(true);
        
		tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
    }
	
	public void createCreateJobTab() {
		createCreateJobTab(null);
	}
	
	public void createCreateJobTab(String jobType) {
		CreateJobForm createJobForm = tabCreator.getCreateJobForm(jobType);
		final Tab tab = new Tab("Create Job", createJobForm);
		tab.setClosable(true);
        
		tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        
        createJobForm.refresh();
	}
	
	public void createJobsForm(Long parentJobId) {
		JobsForm jobsForm = tabCreator.getJobsForm();
		final Tab tab = new Tab("Jobs", jobsForm);
		tab.setClosable(true);
        
		tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        
        jobsForm.refreshJobStepTypes();
        jobsForm.setParentJobId(parentJobId);
	}
	
	public void createJobsForm(String jobTypes) {
		JobsForm jobsForm = tabCreator.getJobsForm();
		final Tab tab = new Tab("Jobs", jobsForm);
		tab.setClosable(true);
        
		tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        
        jobsForm.refreshJobStepTypes();
        jobsForm.setJobTypes(jobTypes);
	}
	
	public void createJobsForm() {
		JobsForm jobsForm = tabCreator.getJobsForm();
		final Tab tab = new Tab("Jobs", jobsForm);
		tab.setClosable(true);
        
		tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        
        jobsForm.refreshJobStepTypes();
    }
	
	public void quit() {
		main.close();
	}
	
	public void closeAllTabs() {
		tabs.getTabs().clear();
	}
}
