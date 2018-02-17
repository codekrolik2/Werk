package org.werk.ui.controls.mainapp;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class MainAppController {
	public static Parent createControl() throws IOException {
		return FXMLLoader.load(MainAppController.class.getResource("MainApp.fxml"));
	}
	
/*    @FXML private Text actiontarget;
    
    @FXML protected void handleSubmitButtonAction(ActionEvent event) {
        actiontarget.setText("Sign in button pressed");
    }*/
}
