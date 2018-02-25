package org.werk.ui.controls.parameters;

import java.io.IOException;

import org.werk.processing.parameters.Parameter;
import org.werk.ui.controls.parameters.state.PrimitiveParameterInit;
import org.werk.ui.guice.LoaderFactory;

import javafx.fxml.FXMLLoader;

public class BooleanParameterInput extends ParameterInput {
	PrimitiveParameterInit parameterInit;
	
	public BooleanParameterInput(PrimitiveParameterInit parameterInit) {
        this.parameterInit = parameterInit;
        
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("BooleanParameterInput.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}

	@Override
	public Parameter getParameter() {
		return parameterInit.getState();
	}
}
