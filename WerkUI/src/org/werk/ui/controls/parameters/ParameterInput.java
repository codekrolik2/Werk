package org.werk.ui.controls.parameters;

import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.ui.controls.parameters.state.ParameterInit;

import javafx.scene.layout.VBox;
import lombok.Setter;

public abstract class ParameterInput extends VBox {
	public abstract Parameter getParameter();
	public abstract ParameterInit getParameterInit();
	public abstract void resetValue();

	@Setter
	protected Boolean immutable = null;
	
	public void updateDisabled() {
		setDisable(isImmutable());
	}

	public boolean isImmutable() {
		if (immutable != null)
			return immutable;
		if (getParameterInit().getJobInputParameter().isPresent()) {
			JobInputParameter jip = getParameterInit().getJobInputParameter().get();
			if (jip instanceof DefaultValueJobInputParameter)
				return true;
			else
				return jip.isOptional();
		}
		return false;
	}
}
