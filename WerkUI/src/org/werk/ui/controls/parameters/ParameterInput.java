package org.werk.ui.controls.parameters;

import org.werk.processing.parameters.Parameter;
import org.werk.ui.controls.parameters.state.ParameterInit;
import org.werk.ui.controls.parameters.state.ParameterStateException;

import javafx.scene.layout.VBox;

public abstract class ParameterInput extends VBox {
	public abstract Parameter getParameter() throws ParameterStateException;
	public abstract ParameterInit getParameterInit();
	public abstract void resetValue();

	public void updateDisabled() {
		setDisable(getParameterInit().isImmutable());
	}
}
