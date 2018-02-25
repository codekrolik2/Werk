package org.werk.ui.controls.table;

import org.werk.processing.parameters.Parameter;
import org.werk.ui.controls.parameters.ParameterInput;
import org.werk.ui.controls.parameters.ParameterInputFactory;
import org.werk.ui.controls.parameters.state.ParameterInit;

import javafx.scene.control.TableCell;

public class ParameterInputCell<T> extends TableCell<ParameterInit, T> {
	protected ParameterInput paramInput;
	protected ParameterInit parameterInit;
	
	public ParameterInputCell() {
		System.out.println("NEW");
	}
	
	public Parameter getParameter() {
		return paramInput.getParameter();
	}
	
	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			parameterInit = getTableView().getItems().get(getIndex());
			paramInput = ParameterInputFactory.createParameterInput(parameterInit);
			
			setGraphic(paramInput);
			setText(null);
		}
	}
}