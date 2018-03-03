package org.werk.ui.controls.jobtypesform;

import java.io.IOException;

import org.werk.meta.JobType;
import org.werk.rest.pojo.RESTJobType;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.mainapp.MainApp;
import org.werk.ui.controls.table.ButtonCell;
import org.werk.ui.guice.LoaderFactory;

import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import lombok.Setter;

public class JobTypesTable extends TableView<JobType> {
	@FXML
	TableColumn<JobType, String> createJobColumn;
	@FXML
	TableColumn<JobType, String> showJobsColumn;
	@FXML
	TableColumn<JobType, String> detailsColumn;

	@Setter
	MainApp mainApp;
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
	
	public JobTypesTable() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("JobTypesTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	public void initialize() {
		createJobColumn.setCellFactory(new CreateJobCellFactory());
		showJobsColumn.setCellFactory(new ShowJobsCellFactory());
		detailsColumn.setCellFactory(new JobTypeDetailsCellFactory());
	}
	
	public void hideCreateShowJobs() {
		createJobColumn.setVisible(false);
		showJobsColumn.setVisible(false);
	}
	
	class CreateJobCellFactory implements Callback<TableColumn<JobType, String>, TableCell<JobType, String>> {
		@Override
		public TableCell<JobType, String> call(final TableColumn<JobType, String> param) {
			return new ButtonCell<JobType, String>("Create Job") {
				@Override
				protected void handle(ActionEvent event) {
					JobType jobType = getTableView().getItems().get(getIndex());
					mainApp.createCreateJobTab(((RESTJobType)jobType).getFullName());
				}
			};
		}
	}

	class ShowJobsCellFactory implements Callback<TableColumn<JobType, String>, TableCell<JobType, String>> {
		@Override
		public TableCell<JobType, String> call(final TableColumn<JobType, String> param) {
			return new ButtonCell<JobType, String>("Show Jobs") {
				@Override
				protected void handle(ActionEvent event) {
					JobType jobType = getTableView().getItems().get(getIndex());
					mainApp.createJobsForm(((RESTJobType)jobType).getFullName());
				}
			};
		}
	}

	class JobTypeDetailsCellFactory implements Callback<TableColumn<JobType, String>, TableCell<JobType, String>> {
		@Override
		public TableCell<JobType, String> call(final TableColumn<JobType, String> param) {
			return new ButtonCell<JobType, String>("Details") {
				@Override
				protected void handle(ActionEvent event) {
					JobType jobType = getTableView().getItems().get(getIndex());
					mainApp.createJobTypeTab(jobType);
				}
			};
		}
	}
}
