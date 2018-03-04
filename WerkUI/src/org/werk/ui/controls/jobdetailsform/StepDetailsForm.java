package org.werk.ui.controls.jobdetailsform;

import java.io.IOException;
import java.util.stream.Collectors;

import org.werk.data.StepPOJO;
import org.werk.processing.steps.StepProcessingLogRecord;
import org.werk.restclient.WerkRESTClient;
import org.werk.ui.ServerInfoManager;
import org.werk.ui.controls.parameters.DictionaryParameterInput;
import org.werk.ui.guice.LoaderFactory;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class StepDetailsForm extends VBox {
	@FXML
    Label stepNumberLabel;
	@FXML
	Label stepTypeLabel;
	@FXML
	Label isRollbackLabel;
	@FXML
	Label rollbackNumbersLabel;
	@FXML
	Label executionCountLabel;
	@FXML
    DictionaryParameterInput stepParameters;
	@FXML
    TableView<StepProcessingLogRecord> processingHistoryTable;
	
	@Inject
	WerkRESTClient werkClient;
	@Inject
	ServerInfoManager serverInfoManager;
	
	public StepDetailsForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("StepDetailsForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	public void setStep(StepPOJO step) {
		stepNumberLabel.setText("Step Number: " + Long.toString(step.getStepNumber()));
		stepTypeLabel.setText("Step Type Name: " + step.getStepTypeName());
		isRollbackLabel.setText("Is Rollback: " + Boolean.toString(step.isRollback()));
		rollbackNumbersLabel.setText("Rollback Step Numbers: " + String.join(", ",
				step.getRollbackStepNumbers().
				stream().map(a -> a.toString()).
				collect(Collectors.toList())));
		executionCountLabel.setText("Execution Count: " + Integer.toString(step.getExecutionCount()));
		
		//step.getStepParameters()
		//step.getProcessingLog()
	}
}
