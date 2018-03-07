package org.werk.ui.controls.parameters;

import java.io.IOException;

import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.BoolParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.impl.BoolParameterImpl;
import org.werk.ui.controls.parameters.state.PrimitiveParameterInit;
import org.werk.ui.guice.LoaderFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import lombok.Getter;

public class BooleanParameterInput extends ParameterInput {
	@Getter
	PrimitiveParameterInit parameterInit;
	
	@FXML
	ChoiceBox<Boolean> choice;
	
	public BooleanParameterInput(PrimitiveParameterInit parameterInit) {
        this.parameterInit = parameterInit;
        parameterInit.setParameterInput(this);
        
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("BooleanParameterInput.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        ObservableList<Boolean> list = FXCollections.observableArrayList(true, false);
        choice.setItems(list);
	}

	/*public void updateDisabled() {
		choice.setDisable(getParameterInit().isImmutable());
	}*/
	
	public void initialize() {
		choice.valueProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		        try {
		        	Boolean b = choice.getValue();
		        	if (parameterInit.getState() == null)
		        		parameterInit.setState(new BoolParameterImpl(b));
		        	else
		        		((BoolParameterImpl)parameterInit.getState()).setValue(b);
		        } catch(Exception e) { 
		        	if (parameterInit.getState() != null)
		        		((BoolParameterImpl)parameterInit.getState()).setValue(null);		        	
		        }
		    }
		});
		
		if (((BoolParameter)parameterInit.getState()).getValue() != null) {
			restoreState((BoolParameter)parameterInit.getState());
		} else {
			resetValue();
		}
	}
	
	public void resetValue() {
		updateDisabled();
		if (parameterInit.getOldParameter().isPresent()) {
			restoreState((BoolParameter)parameterInit.getOldParameter().get());
		} else if (parameterInit.getJobInputParameter().isPresent()) {
			JobInputParameter jip = parameterInit.getJobInputParameter().get();
			if (jip instanceof DefaultValueJobInputParameter) {
				DefaultValueJobInputParameter defaultPrm = (DefaultValueJobInputParameter)jip;
				restoreState((BoolParameter)defaultPrm.getDefaultValue());
			}
		} else
			parameterInit.setState(new BoolParameterImpl(null));
	}
	
	protected void restoreState(BoolParameter prm) {
		updateDisabled();
		if (prm.getValue() != null)
			choice.setValue(prm.getValue());
	}
	
	@Override
	public Parameter getParameter() {
		return parameterInit.getState();
	}
}
