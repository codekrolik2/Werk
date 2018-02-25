package org.werk.ui.controls.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

public class TextFieldCell<S, T> extends TableCell<S, T> {
	protected final TextField txt;
	
	public TextFieldCell() {
		txt = new TextField();
	}
	
	public String getFieldText() {
		return txt.getText();
	}
	
	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			setGraphic(txt);
			setText(null);
		}
	}
}