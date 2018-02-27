package org.werk.ui.controls.parameters;

import java.io.IOException;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.DoubleParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.impl.DoubleParameterImpl;
import org.werk.ui.controls.parameters.state.PrimitiveParameterInit;
import org.werk.ui.guice.LoaderFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import lombok.Getter;

public class DoubleParameterInput extends ParameterInput {
	@Getter
	PrimitiveParameterInit parameterInit;
	@FXML
	TextField textField;
	
	Pattern pattern = Pattern.compile("\\d*|\\d+\\.\\d*");
	
	public DoubleParameterInput(PrimitiveParameterInit parameterInit) {
        this.parameterInit = parameterInit;
        parameterInit.setParameterInput(this);
        
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("DoubleParameterInput.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	public void initialize() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		TextFormatter formatter = new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> {
		    return pattern.matcher(change.getControlNewText()).matches() ? change : null;
		});

		textField.setTextFormatter(formatter);
		
		textField.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        try {
		        	Double d = Double.parseDouble(textField.getText());
		        	if (parameterInit.getState() == null)
		        		parameterInit.setState(new DoubleParameterImpl(d));
		        	else
		        		((DoubleParameterImpl)parameterInit.getState()).setValue(d);
		        } catch(Exception e) { }
		    }
		});
		
		if (((DoubleParameter)parameterInit.getState()).getValue() != null) {
			restoreState((DoubleParameter)parameterInit.getState());
		} else {
			resetValue();
		}
	}
	
	public void resetValue() {
		updateDisabled();
		if (parameterInit.getOldParameter().isPresent()) {
			restoreState((DoubleParameter)parameterInit.getOldParameter().get());
		} else if (parameterInit.getJobInputParameter().isPresent()) {
			JobInputParameter jip = parameterInit.getJobInputParameter().get();
			if (jip instanceof DefaultValueJobInputParameter) {
				DefaultValueJobInputParameter defaultPrm = (DefaultValueJobInputParameter)jip;
				restoreState((DoubleParameter)defaultPrm.getDefaultValue());
			}
		} else
			parameterInit.setState(new DoubleParameterImpl(null));
	}
	
	protected void restoreState(DoubleParameter prm) {
		updateDisabled();
		if (prm.getValue() != null)
			textField.setText(prm.getValue().toString());
	}
	
	@Override
	public Parameter getParameter() {
		return parameterInit.getState();
	}
}
