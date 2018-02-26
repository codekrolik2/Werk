package org.werk.ui.controls.table;

import org.werk.ui.controls.parameters.state.DictionaryParameterAndName;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

public abstract class TextFieldCell<T> extends TableCell<DictionaryParameterAndName, T> {
	protected final TextField txt;
	
	public TextFieldCell() {
		txt = new TextField();
	}
	
	public String getFieldText() {
		return txt.getText();
	}
	
	protected abstract void textChanged(ObservableValue<? extends String> observable, String oldValue, String newValue);
	
	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			txt.textProperty().addListener(this::textChanged);
			
			DictionaryParameterAndName dpn = getTableView().getItems().get(getIndex());
			txt.setText(dpn.getName());
			
			if (dpn.getInit().getJobInputParameter().isPresent() || dpn.getInit().getOldParameter().isPresent())
				txt.setDisable(true);
			
			setGraphic(txt);
			setText(null);
		}
	}
}