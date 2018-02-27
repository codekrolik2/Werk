package org.werk.ui.controls.table;

import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.ui.controls.parameters.state.DictionaryParameterAndName;
import org.werk.ui.controls.parameters.state.ParameterInit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;

public abstract class MultiCell<S, T> extends TableCell<S, T> {
	protected CheckBox checkbox;
	protected Button btn;
	protected boolean topLevel;

	public MultiCell(boolean topLevel) {
		this.topLevel = topLevel;
	}
	
	public boolean isChecked() {
		return checkbox.isSelected();
	}
	
	protected abstract void handleRemove(ActionEvent event);
	protected abstract void handleDisable(Boolean newValue);
	protected abstract void handleDisableAndReset(Boolean newValue);
	
	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			ParameterInit parameterInit;
			Object o = getTableView().getItems().get(getIndex());
			if (o instanceof DictionaryParameterAndName)
				parameterInit = ((DictionaryParameterAndName)o).getInit();
			else
				parameterInit = (ParameterInit)o;
			
			if ((topLevel) && (parameterInit.getJobInputParameter().isPresent())) {
				JobInputParameter jip = parameterInit.getJobInputParameter().get();
				if (jip instanceof DefaultValueJobInputParameter) {
					if (!((DefaultValueJobInputParameter)jip).isDefaultValueImmutable()) {
						checkbox = new CheckBox();
						checkbox.selectedProperty().set(!parameterInit.getParameterInput().isImmutable());
						checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
						    @Override
						    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
						    	handleDisableAndReset(newValue);
						    }
						});
						setGraphic(checkbox);
					} else
						setGraphic(null);
				} else if (jip.isOptional()) {
					checkbox = new CheckBox();
					checkbox.selectedProperty().set(!parameterInit.getParameterInput().isImmutable());
					checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
					    @Override
					    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					    	handleDisable(newValue);
					    }
					});						
					setGraphic(checkbox);
				} else
					setGraphic(null);
			} else if ((topLevel) && (parameterInit.getOldParameter().isPresent())) {
				checkbox = new CheckBox();
				checkbox.selectedProperty().set(!parameterInit.getParameterInput().isImmutable());
				checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				    @Override
				    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				    	handleDisable(newValue);
				    }
				});						
				setGraphic(checkbox);
			} else {
				btn = new Button("-");
				btn.setOnAction(this::handleRemove);
				setGraphic(btn);
			}	
			
			setText(null);
		}
	}
}