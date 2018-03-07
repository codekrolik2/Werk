package org.werk.ui.controls.parameters;

import java.io.IOException;

import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.ListParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.ui.controls.parameters.state.DictionaryParameterInit;
import org.werk.ui.controls.parameters.state.ListParameterInit;
import org.werk.ui.controls.parameters.state.ParameterInit;
import org.werk.ui.controls.parameters.state.ParameterStateException;
import org.werk.ui.controls.parameters.state.PrimitiveParameterInit;
import org.werk.ui.controls.table.ButtonCell;
import org.werk.ui.controls.table.ParameterInputCell;
import org.werk.ui.guice.LoaderFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import lombok.Getter;

public class ListParameterInput extends ParameterInput {
	@FXML
    protected Button addButton;
	@FXML
    protected ContextMenu contextMenu;
	@FXML
	protected TableView<ParameterInit> parametersTable;
	
	@FXML
    TableColumn<ParameterInit, String> disableRemoveParameter;
	@FXML
    TableColumn<ParameterInit, String> parameterValue;
	
	protected final ObservableList<ParameterInit> data = FXCollections.observableArrayList();
	
	@Getter
	ListParameterInit parameterInit;
	
	public ListParameterInput(ListParameterInit parameterInit) {
        this.parameterInit = parameterInit;
        parameterInit.setParameterInput(this);
        
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("ListParameterInput.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	public void initialize() {
		disableRemoveParameter.setCellFactory(new Callback<TableColumn<ParameterInit,String>, TableCell<ParameterInit,String>>() {
			@Override
			public TableCell<ParameterInit, String> call(TableColumn<ParameterInit, String> param) {
				return new ButtonCell<ParameterInit, String>("-") {
					@Override
					protected void handle(ActionEvent event) {
						int index = getIndex();
						
						parameterInit.getListParametersState().remove(index);
						data.remove(index);
					}
				};
			}
		});
		
		parameterValue.setCellFactory(new Callback<TableColumn<ParameterInit,String>, TableCell<ParameterInit,String>>() {
			@Override
			public TableCell<ParameterInit, String> call(TableColumn<ParameterInit, String> param) {
				return new ParameterInputCell<String>();
			}
		});
		
		if (!parameterInit.getListParametersState().isEmpty()) {
			restoreState();
		} else {
			resetValue();
		}
		
		parametersTable.setItems(data);
	}
	
	public void resetValue() {
		parameterInit.getListParametersState().clear();
		data.clear();
		updateDisabled();
		if (parameterInit.getOldParameter().isPresent()) {
			restoreState((ListParameter)parameterInit.getOldParameter().get());
		} else if (parameterInit.getJobInputParameter().isPresent()) {
			JobInputParameter jip = parameterInit.getJobInputParameter().get();
			if (jip instanceof DefaultValueJobInputParameter) {
				DefaultValueJobInputParameter defaultPrm = (DefaultValueJobInputParameter)jip;
				restoreState((ListParameter)defaultPrm.getDefaultValue());
			}
		}
	}
	
	protected void restoreState() {
		updateDisabled();
		for (ParameterInit pi : parameterInit.getListParametersState())
			data.add(pi);
	}
	
	protected void restoreState(ListParameter prm) {
		updateDisabled();
		if (prm.getValue() != null) {
			for (Parameter param : prm.getValue()) {
				if (param.getType() == ParameterType.DICTIONARY) {
					addParam(new DictionaryParameterInit((DictionaryParameter)param, false));				
				} else if (param.getType() == ParameterType.LIST) {
					addParam(new ListParameterInit(param));
				} else
					addParam(new PrimitiveParameterInit(param));
			}
		}
	}
	
	public void showParameterMenu() {
		contextMenu.show(addButton, Side.BOTTOM, 0, 0);
	}
	
	protected void addParam(ParameterInit prm) {
		if (getParameterInit().isImmutable())
			prm.setImmutable(true);
		parameterInit.getListParametersState().add(prm);
		data.add(prm);
	}
	
	//=================================================
	
	public void addNewLongParameter() {
		PrimitiveParameterInit prm = new PrimitiveParameterInit(ParameterType.LONG);
		addParam(prm);
	}
	
	public void addNewDoubleParameter() {
		PrimitiveParameterInit prm = new PrimitiveParameterInit(ParameterType.DOUBLE);
		addParam(prm);
	}
	
	public void addNewDictionaryParameter() {
		DictionaryParameterInit prm = new DictionaryParameterInit(false);
		addParam(prm);
	}
	
	public void addNewBoolParameter() {
		PrimitiveParameterInit prm = new PrimitiveParameterInit(ParameterType.BOOL);
		addParam(prm);
	}
	
	public void addNewStringParameter() {
		PrimitiveParameterInit prm = new PrimitiveParameterInit(ParameterType.STRING);
		addParam(prm);
	}
	
	public void addNewListParameter() {
		ListParameterInit prm = new ListParameterInit();
		addParam(prm);
	}
	
	//=================================================
	
	@Override
	public Parameter getParameter() throws ParameterStateException {
		return parameterInit.getState();
	}
}
