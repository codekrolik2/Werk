package org.werk.ui.controls.jobdetailsform;

import java.io.IOException;

import org.werk.data.StepPOJO;
import org.werk.processing.readonly.ReadOnlyJob;
import org.werk.ui.guice.LoaderFactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class JobStepsForm extends ScrollPane {
	@FXML VBox internalVbox;
	
	public JobStepsForm() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("JobStepsForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	public void setJob(ReadOnlyJob<Long> job) throws Exception {
		internalVbox.getChildren().clear();
		for (StepPOJO step : job.getProcessingHistory()) {
			StepDetailsForm sdf = new StepDetailsForm();
			sdf.setStep(step);
			
			TitledPane pane = new TitledPane("#" + step.getStepNumber() + " " + step.getStepTypeName(), sdf);
			pane.setExpanded(false);
			internalVbox.getChildren().add(pane);
		}
	}
}
