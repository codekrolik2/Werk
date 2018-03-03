package org.werk.ui.controls.jobsform;

import java.io.IOException;

import org.werk.meta.JobType;
import org.werk.meta.StepType;
import org.werk.processing.jobs.JobStatus;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.mainapp.MainApp;
import org.werk.ui.controls.table.ButtonCell;
import org.werk.ui.guice.LoaderFactory;
import org.werk.ui.util.MessageBox;

import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import lombok.Setter;

public class JobsTable extends TableView<TableJobPOJO<Long>> {
	@FXML TableColumn<TableJobPOJO<Long>, String> jobTypeColumn;
	@FXML TableColumn<TableJobPOJO<Long>, String> currentStepColumn;
	@FXML TableColumn<TableJobPOJO<Long>, String> parentJobIdColumn;
	@FXML TableColumn<TableJobPOJO<Long>, String> detailsColumn;
	@FXML TableColumn<TableJobPOJO<Long>, String> restartJobColumn;
	@FXML TableColumn<TableJobPOJO<Long>, String> childJobsColumn;
	
	@Setter
	MainApp mainApp;
	@Setter
	JobsForm jobsForm;
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
	
	public JobsTable() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("JobsTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	public void initialize() {
		currentStepColumn.setCellFactory(new StepTypeCellFactory());
		parentJobIdColumn.setCellFactory(new ParentJobIdCellFactory());
		jobTypeColumn.setCellFactory(new JobTypeCellFactory());
		detailsColumn.setCellFactory(new JobDetailsCellFactory());
		restartJobColumn.setCellFactory(new RestartJobCellFactory());
		childJobsColumn.setCellFactory(new ChildJobsCellFactory());
	}

	class RestartJobCellFactory implements Callback<TableColumn<TableJobPOJO<Long>, String>, 
			TableCell<TableJobPOJO<Long>, String>> {
		@Override
		public TableCell<TableJobPOJO<Long>, String> call(final TableColumn<TableJobPOJO<Long>, String> param) {
			return new RestartJobCell();
		}
	}

	class RestartJobCell extends TableCell<TableJobPOJO<Long>, String> {
		protected final Button btn;
		
		public RestartJobCell() {
			btn = new Button("Restart");
		}
		
		protected void handle(ActionEvent event) {
			TableJobPOJO<Long> jobPojo = getTableView().getItems().get(getIndex());
			System.out.println("Handle JobPOJO " + jobPojo.getJobId());
		}
		
		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setGraphic(null);
				setText(null);
			} else {
				TableJobPOJO<Long> jobPojo = getTableView().getItems().get(getIndex());
				if ((jobPojo.getStatus() == JobStatus.FINISHED) || (jobPojo.getStatus() == JobStatus.ROLLED_BACK)
						|| (jobPojo.getStatus() == JobStatus.FAILED)) {
					btn.setOnAction(this::handle);
					setGraphic(btn);
					setText(null);
				} else {
					setGraphic(null);
					setText(null);
				}
			}
		}
	}
	
	class ChildJobsCellFactory implements Callback<TableColumn<TableJobPOJO<Long>, String>, 
			TableCell<TableJobPOJO<Long>, String>> {
		@Override
		public TableCell<TableJobPOJO<Long>, String> call(final TableColumn<TableJobPOJO<Long>, String> param) {
			return new ButtonCell<TableJobPOJO<Long>, String>("Child Jobs") {
				@Override
				protected void handle(ActionEvent event) {
					TableJobPOJO<Long> jobPojo = getTableView().getItems().get(getIndex());
					mainApp.createJobsForm(jobPojo.getJobId());
				}
			};
		}
	}

	class JobDetailsCellFactory
			implements Callback<TableColumn<TableJobPOJO<Long>, String>, TableCell<TableJobPOJO<Long>, String>> {
		@Override
		public TableCell<TableJobPOJO<Long>, String> call(final TableColumn<TableJobPOJO<Long>, String> param) {
			return new ButtonCell<TableJobPOJO<Long>, String>("Details") {
				@Override
				protected void handle(ActionEvent event) {
					TableJobPOJO<Long> jobPojo = getTableView().getItems().get(getIndex());
					System.out.println("Handle JobPOJO " + jobPojo.getJobId());
				}
			};
		}
	}

	class ParentJobIdCellFactory implements Callback<TableColumn<TableJobPOJO<Long>, String>, 
			TableCell<TableJobPOJO<Long>, String>> {
		@Override
		public TableCell<TableJobPOJO<Long>, String> call(final TableColumn<TableJobPOJO<Long>, String> param) {
			return new ParentJobIdCell();
		}
	}
	
	class ParentJobIdCell extends TableCell<TableJobPOJO<Long>, String> {
		protected final Button btn;
		
		public ParentJobIdCell() {
			btn = new Button();
		}
		
		protected void handle(ActionEvent event) {
			TableJobPOJO<Long> jobPojo = getTableView().getItems().get(getIndex());
			System.out.println("Handle JobPOJO " + jobPojo.getJobId());
		}
		
		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setGraphic(null);
				setText(null);
			} else {
				TableJobPOJO<Long> jobPojo = getTableView().getItems().get(getIndex());
				
				if (jobPojo.getParentJobId().isPresent()) {
					btn.setOnAction(this::handle);
					btn.setText(jobPojo.getParentJobId().get().toString());
					setGraphic(btn);
					setText(null);
				} else {
					setGraphic(null);
					setText(null);
				}
			}
		}
	}
	
	class StepTypeCellFactory implements Callback<TableColumn<TableJobPOJO<Long>, String>, 
			TableCell<TableJobPOJO<Long>, String>> {
		@Override
		public TableCell<TableJobPOJO<Long>, String> call(final TableColumn<TableJobPOJO<Long>, String> param) {
			return new TableJobPOJOCell() {
				@Override
				protected String getBtnText(TableJobPOJO<Long> jobPojo) {
					return jobPojo.getStepType();
				}

				@Override
				protected void handle(TableJobPOJO<Long> jobPojo) {
					if (jobsForm.getStepTypes() != null) {
						StepType<Long> stepType = jobsForm.getStepTypes().get(jobPojo.getJobPOJO().getCurrentStepTypeName());
						if (stepType != null) {
							mainApp.createStepTypeTab(stepType);
							return;
						}
					}
					
					MessageBox.show(String.format("Step type not found: [%s].", jobPojo.getStepType()));
				}
			};
		}
	}

	class JobTypeCellFactory implements Callback<TableColumn<TableJobPOJO<Long>, String>, 
			TableCell<TableJobPOJO<Long>, String>> {
		@Override
		public TableCell<TableJobPOJO<Long>, String> call(final TableColumn<TableJobPOJO<Long>, String> param) {
			return new TableJobPOJOCell() {
				@Override
				protected String getBtnText(TableJobPOJO<Long> jobPojo) {
					return jobPojo.getJobType();
				}

				@Override
				protected void handle(TableJobPOJO<Long> jobPojo) {
					if (jobsForm.getJobTypes() != null) {
						JobType jobType = jobsForm.getJobTypes().get(jobPojo.getJobType());
						if (jobType != null) {
							mainApp.createJobTypeTab(jobType);
							return;
						}
					}
					
					MessageBox.show(String.format("Job type not found: [%s].", jobPojo.getJobType()));
				}
			};
		}
	}
	
	abstract class TableJobPOJOCell extends ButtonCell<TableJobPOJO<Long>, String> {
		public TableJobPOJOCell() {
			super("");
		}

		protected abstract String getBtnText(TableJobPOJO<Long> jobPojo);
		protected abstract void handle(TableJobPOJO<Long> jobPojo);
		
		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (!empty) {
				TableJobPOJO<Long> jobPojo = getTableView().getItems().get(getIndex());
				btn.setText(getBtnText(jobPojo));
			}
		}
		
		@Override
		protected void handle(ActionEvent event) {
			TableJobPOJO<Long> jobPojo = getTableView().getItems().get(getIndex());
			handle(jobPojo);
		}
	};
}
