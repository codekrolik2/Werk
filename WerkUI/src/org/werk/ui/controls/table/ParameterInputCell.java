package org.werk.ui.controls.table;

import org.werk.processing.parameters.Parameter;
import org.werk.ui.controls.parameters.ParameterInput;
import org.werk.ui.controls.parameters.ParameterInputFactory;
import org.werk.ui.controls.parameters.state.DictionaryParameterAndName;
import org.werk.ui.controls.parameters.state.ParameterInit;

import javafx.scene.control.TableCell;

public class ParameterInputCell<T> extends TableCell<ParameterInit, T> {
	protected ParameterInput paramInput;
	protected ParameterInit parameterInit;
	protected boolean isImmutable;
	
	public ParameterInputCell(boolean isImmutable) {
		this.isImmutable = isImmutable;
	}
	
	public ParameterInputCell() { 
		this.isImmutable = false;
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
			Object o = getTableView().getItems().get(getIndex());
			if (o instanceof DictionaryParameterAndName)
				parameterInit = ((DictionaryParameterAndName)o).getInit();
			else
				parameterInit = (ParameterInit)o;
			paramInput = ParameterInputFactory.createParameterInput(parameterInit);
			if (isImmutable)
				paramInput.setImmutable();
			
			setGraphic(paramInput);
			setText(null);
		}
	}
}