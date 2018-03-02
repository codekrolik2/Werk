package org.werk.ui.controls.jobsform;

import java.io.IOException;

import org.werk.ui.guice.LoaderFactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class JobsForm extends VBox {
	@FXML
	TitledPane titledPane;
	
	@FXML
	GridPane mainGrid;
	
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
    
    public void initialize() {
    }
}
