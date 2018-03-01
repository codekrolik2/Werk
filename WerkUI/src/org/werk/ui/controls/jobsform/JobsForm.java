package org.werk.ui.controls.jobsform;

import java.io.IOException;

import org.werk.ui.guice.LoaderFactory;

import javafx.fxml.FXMLLoader;

public class JobsForm {
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
}
