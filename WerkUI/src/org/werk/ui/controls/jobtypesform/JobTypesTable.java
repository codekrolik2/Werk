package org.werk.ui.controls.jobtypesform;

import java.io.IOException;

import org.werk.ui.guice.LoaderFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;

public class JobTypesTable<S> extends TableView<S> {
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
}
