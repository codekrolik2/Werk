package org.werk.ui.controls.table;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

public abstract class ButtonCell<S, T> extends TableCell<S, T> {
	protected final Button btn;
	
	public ButtonCell(String buttonText) {
		btn = new Button(buttonText);
	}
	
	protected abstract void handle(ActionEvent event);
	
	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			btn.setOnAction(this::handle);
			setGraphic(btn);
			setText(null);
		}
	}
}