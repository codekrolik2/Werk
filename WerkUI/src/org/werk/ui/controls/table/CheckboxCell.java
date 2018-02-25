package org.werk.ui.controls.table;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;

public class CheckboxCell<S, T> extends TableCell<S, T> {
	protected final CheckBox checkbox;
	
	public CheckboxCell() {
		checkbox = new CheckBox();
	}
	
	public boolean isChecked() {
		return checkbox.isSelected();
	}
	
	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			setGraphic(checkbox);
			setText(null);
		}
	}
}