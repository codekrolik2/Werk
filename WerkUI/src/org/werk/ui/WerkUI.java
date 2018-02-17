package org.werk.ui;

import org.werk.ui.controls.connectform.ConnectFormController;
import org.werk.ui.controls.mainapp.MainAppController;
import org.werk.ui.modal.ModalWindow;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WerkUI extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		try {
			Parent mainApp = MainAppController.createControl();
			
			Scene scene = new Scene(mainApp, 800, 600);
			
			stage.setTitle("Werk UI");
			stage.setScene(scene);
			stage.show();
			
			Parent connectForm = ConnectFormController.createControl();
			ModalWindow window = new ModalWindow(connectForm, "Connect");
			window.clickShow(stage);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
