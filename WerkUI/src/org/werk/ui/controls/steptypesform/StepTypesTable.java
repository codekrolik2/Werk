package org.werk.ui.controls.steptypesform;

import java.io.IOException;

import org.werk.meta.StepType;
import org.werk.ui.controls.mainapp.MainApp;
import org.werk.ui.controls.table.ButtonCell;
import org.werk.ui.guice.LoaderFactory;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import lombok.Setter;

public class StepTypesTable extends TableView<StepType<Long>> {
	@Setter
	MainApp mainApp;
	@FXML
	TableColumn<StepType<Long>, String> detailsColumn;
	@FXML
	TableColumn<StepType<Long>, String> jobTypesColumn;
	
	public StepTypesTable() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("StepTypesTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	public void initialize() {
		detailsColumn.setCellFactory(new StepTypeDetailsCellFactory());
	}
	
	public void hideJobs() {
		jobTypesColumn.setVisible(false);
	}
	
	class StepTypeDetailsCellFactory implements Callback<TableColumn<StepType<Long>, String>, TableCell<StepType<Long>, String>> {
		@Override
		public TableCell<StepType<Long>, String> call(final TableColumn<StepType<Long>, String> param) {
			return new ButtonCell<StepType<Long>, String>("Details") {
				@Override
				protected void handle(ActionEvent event) {
					StepType<Long> stepType = getTableView().getItems().get(getIndex());
					mainApp.createStepTypeTab(stepType);
				}
			};
		}
	}
}
