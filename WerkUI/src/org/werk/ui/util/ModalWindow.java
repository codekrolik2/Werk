package org.werk.ui.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ModalWindow {
	protected Parent dialogue;
	protected String title;
	
	public ModalWindow(Parent dialogue, String title) {
		this.dialogue = dialogue;
		this.title = title;
	}
	
	public Stage showModal(Stage main, Window parentWindow) {
	    Stage stage = new Stage();
	    stage.setScene(new Scene(dialogue));
	    stage.setTitle(title);
	    
	    //XXX: Modality causes problems on Linux.
	    //stage.initModality(Modality.APPLICATION_MODAL);//Modality.WINDOW_MODAL
	    
	    //XXX: The following is required for Modality on Linux due to: https://bugs.openjdk.java.net/browse/JDK-8088811
	    //Results in main window disappearing and reappearing, but restores its resizeability.
	    /*stage.setOnHidden(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Platform.runLater(() -> {
					try {
					    main.hide();
					    main.show();
					} catch(Exception e) {
						
					}
				});
			}
		});*/
	    
	    stage.initOwner(parentWindow);
	    stage.setResizable(false);
	    stage.show();
	    
	    return stage;
	}
}
