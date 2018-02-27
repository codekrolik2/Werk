package org.werk.ui.controls.table;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

public abstract class LabelCell<S, T> extends TableCell<S, T> {
	protected final Label label;
	
	public LabelCell() {
		label = new Label();
		label.setWrapText(true);
	}
	
	public abstract String getLabelText();
	
	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			label.setText(getLabelText());
			setGraphic(label);
			setText(null);
		}
	}
}