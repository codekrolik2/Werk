package org.werk.ui.controls.parameters;

import java.io.IOException;

import org.werk.processing.parameters.LongParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.ui.controls.parameters.state.PrimitiveParameterInit;
import org.werk.ui.guice.LoaderFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;

public class LongParameterInput extends ParameterInput {
	@FXML
	protected TextField textField;
	
	PrimitiveParameterInit parameterInit;
	
	public LongParameterInput(PrimitiveParameterInit parameterInit) {
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
	
	public void initialize() {
		textField.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        if (!newValue.matches("\\d*"))
		            textField.setText(newValue.replaceAll("[^\\d]", ""));
		        try {
		        	Long l = Long.parseLong(textField.getText());
		        	if (parameterInit.getState() == null)
		        		parameterInit.setState(new LongParameterImpl(l));
		        	else
		        		((LongParameterImpl)parameterInit.getState()).setValue(l);
		        } catch(Exception e) {
		        	parameterInit.setState(null);
		        }
		    }
		});
		if (parameterInit.getState() != null)
			textField.setText(((LongParameter)parameterInit.getState()).getValue().toString()); 
	}
	
	@Override
	public Parameter getParameter() {
		return parameterInit.getState();
	}
}
