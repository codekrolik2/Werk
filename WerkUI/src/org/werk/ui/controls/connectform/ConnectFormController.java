package org.werk.ui.controls.connectform;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ConnectFormController {
	public static Parent createControl() throws IOException {
		return FXMLLoader.load(ConnectFormController.class.getResource("ConnectForm.fxml"));
	}

}
