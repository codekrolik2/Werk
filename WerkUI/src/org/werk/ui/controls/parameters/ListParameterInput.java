package org.werk.ui.controls.parameters;

import java.io.IOException;

import org.werk.processing.parameters.Parameter;
import org.werk.ui.controls.parameters.state.ListParameterInit;
import org.werk.ui.guice.LoaderFactory;

import javafx.fxml.FXMLLoader;

public class ListParameterInput extends ParameterInput {
	ListParameterInit parameterInit;
	
	public ListParameterInput(ListParameterInit parameterInit) {
        this.parameterInit = parameterInit;
        
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("LongParameterInput.fxml"));
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
