package org.werk.ui.util;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MessageBox {
	public static void show(Optional<String> title, Optional<String> header, Optional<String> content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		if (title.isPresent()) 
			alert.setTitle(title.get());
		if (header.isPresent())
			alert.setHeaderText(header.get());
		if (content.isPresent())
			alert.setContentText(content.get());
		
		alert.showAndWait();
	}
	
	public static void show(String message) {
		show(Optional.empty(), Optional.of(message), Optional.empty());
	}
}
