package org.werk.ui.controls.parameters;

import java.io.IOException;
import java.util.Map.Entry;

import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.ui.controls.parameters.state.DictionaryParameterAndName;
import org.werk.ui.controls.parameters.state.DictionaryParameterInit;
import org.werk.ui.controls.parameters.state.ListParameterInit;
import org.werk.ui.controls.parameters.state.ParameterInit;
import org.werk.ui.controls.parameters.state.PrimitiveParameterInit;
import org.werk.ui.controls.table.ButtonCell;
import org.werk.ui.controls.table.ParameterInputCell;
import org.werk.ui.controls.table.TextFieldCell;
import org.werk.ui.guice.LoaderFactory;

import javafx.beans.value.ObservableValue;
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
import lombok.Setter;

public class DictionaryParameterInput extends ParameterInput {
	@FXML
    protected Button addButton;
	@FXML
    protected ContextMenu contextMenu;
	@FXML
	protected TableView<DictionaryParameterAndName> parametersTable;
	
	@FXML
    TableColumn<DictionaryParameterAndName, String> disableRemoveParameter;
	@FXML
    TableColumn<DictionaryParameterAndName, String> parameterName;
	@FXML
    TableColumn<DictionaryParameterAndName, String> constraints;
	@FXML
    TableColumn<ParameterInit, String> parameterValue;
	
	protected final ObservableList<DictionaryParameterAndName> data = FXCollections.observableArrayList();   
	
	@Setter
	DictionaryParameterInit parameterInit;
	
	protected boolean isImmutable = false;
	
	public DictionaryParameterInput() {
        parameterInit = new DictionaryParameterInit();
        
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("DictionaryParameterInput.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	public DictionaryParameterInput(DictionaryParameterInit parameterInit) {
		this.parameterInit = parameterInit;
		
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("DictionaryParameterInput.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	
		constraints.setVisible(false);
	}
	
	public void initialize() {
		disableRemoveParameter.setCellFactory(new Callback<TableColumn<DictionaryParameterAndName,String>, TableCell<DictionaryParameterAndName,String>>() {
			@Override
			public TableCell<DictionaryParameterAndName, String> call(TableColumn<DictionaryParameterAndName, String> param) {
				return new ButtonCell<DictionaryParameterAndName, String>("-") {
					@Override
					protected void handle(ActionEvent event) {
						int index = getIndex();
						
						parameterInit.getMapParametersState().remove(index);
						data.remove(index);
					}
				};
			}
		});
		
		parameterName.setCellFactory(new Callback<TableColumn<DictionaryParameterAndName,String>, TableCell<DictionaryParameterAndName,String>>() {
			@Override
			public TableCell<DictionaryParameterAndName, String> call(TableColumn<DictionaryParameterAndName, String> param) {
				return new TextFieldCell<String>() {
					@Override
					protected void textChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						int index = getIndex();
						parameterInit.getMapParametersState().get(index).setName(newValue);
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
		
		if (parameterInit.getMapParametersState() != null) {
			restoreState();
		} else {
			if (parameterInit.getOldParameter().isPresent()) {
				restoreState((DictionaryParameter)parameterInit.getOldParameter().get());
			} else if (parameterInit.getJobInputParameter().isPresent()) {
				JobInputParameter jip = parameterInit.getJobInputParameter().get();
				if (jip instanceof DefaultValueJobInputParameter) {
					DefaultValueJobInputParameter defaultPrm = (DefaultValueJobInputParameter)jip;
					restoreState((DictionaryParameter)defaultPrm.getDefaultValue());
				}
			}
		}
		
		if (isImmutable())
			addButton.setDisable(true);
		
		parametersTable.setItems(data);
	}

	protected void restoreState() {
		for (DictionaryParameterAndName dpn : parameterInit.getMapParametersState())
			data.add(dpn);
	}
	
	protected void restoreState(DictionaryParameter prm) {
		if (prm.getValue() != null) {
			for (Entry<String, Parameter> param : prm.getValue().entrySet()) {
				if (param.getValue().getType() == ParameterType.DICTIONARY)
					addParam(param.getKey(), new DictionaryParameterInit(param.getValue()));				
				else if (param.getValue().getType() == ParameterType.LIST)
					addParam(param.getKey(), new ListParameterInit(param.getValue()));
				else
					addParam(param.getKey(), new PrimitiveParameterInit(param.getValue()));
			}
		}
	}
	
	protected boolean isImmutable() {
		if (isImmutable)
			return true;
		if (parameterInit.getJobInputParameter().isPresent()) {
			JobInputParameter jip = parameterInit.getJobInputParameter().get();
			DefaultValueJobInputParameter defaultPrm = (DefaultValueJobInputParameter)jip;
			if (defaultPrm.isDefaultValueImmutable())
				isImmutable = true;
			return defaultPrm.isDefaultValueImmutable();
		}
		return false;
	}
	
	public void showParameterMenu() {
		contextMenu.show(addButton, Side.BOTTOM, 0, 0);
	}
	
	protected void addParam(String name, ParameterInit prm) {
		DictionaryParameterAndName dp = new DictionaryParameterAndName(name, prm);
		parameterInit.getMapParametersState().add(dp);
		data.add(dp);
	}
	
	//=================================================
	
	public void addNewLongParameter() {
		PrimitiveParameterInit prm = new PrimitiveParameterInit(ParameterType.LONG);
		addParam("", prm);
	}
	
	public void addNewDoubleParameter() {
		PrimitiveParameterInit prm = new PrimitiveParameterInit(ParameterType.DOUBLE);
		addParam("", prm);
	}
	
	public void addNewDictionaryParameter() {
		DictionaryParameterInit prm = new DictionaryParameterInit();
		addParam("", prm);
	}
	
	public void addNewBoolParameter() {
		PrimitiveParameterInit prm = new PrimitiveParameterInit(ParameterType.BOOL);
		addParam("", prm);
	}
	
	public void addNewStringParameter() {
		PrimitiveParameterInit prm = new PrimitiveParameterInit(ParameterType.STRING);
		addParam("", prm);
	}
	
	public void addNewListParameter() {
		ListParameterInit prm = new ListParameterInit();
		addParam("", prm);
	}
	
	//=================================================
	
	@Override
	public Parameter getParameter() {
		return parameterInit.getState();
	}

	@Override
	public void setImmutable() {
		isImmutable = true;
		//TODO: call setImmutable recursive
	}
}
