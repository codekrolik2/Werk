package org.werk.ui.modal;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ModalWindow {
	protected Parent root;
	protected String title;
	
	public ModalWindow(Parent root, String title) {
		this.root = root;
		this.title = title;
	}
	
	public void clickShow(ActionEvent event) {
		clickShow(((Node)event.getSource()).getScene().getWindow());
	}
	
	public void clickShow(Window owner) {
	    Stage stage = new Stage();
	    stage.setScene(new Scene(root));
	    stage.setTitle(title);
		//You can also use Modality.APPLICATION_MODAL
	    stage.initModality(Modality.WINDOW_MODAL);
	    stage.initOwner(owner);
	    stage.setResizable(false);
	    stage.show();
	}
}
