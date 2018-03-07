package org.werk.ui.controls.parameters;

import java.io.IOException;
import java.util.List;
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
import org.werk.ui.controls.parameters.state.ParameterStateException;
import org.werk.ui.controls.parameters.state.PrimitiveParameterInit;
import org.werk.ui.controls.table.ButtonCell;
import org.werk.ui.controls.table.LabelCell;
import org.werk.ui.controls.table.MultiCell;
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
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import lombok.Getter;

public class DictionaryParameterInput extends ParameterInput {
	@FXML
	AnchorPane topPane;
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
    TableColumn<DictionaryParameterAndName, String> reset;
	@FXML
    TableColumn<ParameterInit, String> parameterValue;
	
	protected final ObservableList<DictionaryParameterAndName> data = FXCollections.observableArrayList();   
	
	@Getter
	DictionaryParameterInit parameterInit;
	
	protected DictionaryParameterInputType dictType;
	
	public DictionaryParameterInput() {
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("DictionaryParameterInput.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	public DictionaryParameterInput(DictionaryParameterInit parameterInit, DictionaryParameterInputType dictType) {
		this();
		setContext(parameterInit, dictType);
	}
    
	public void setContext(DictionaryParameterInit parameterInit, DictionaryParameterInputType dictType) {
		if (dictType == DictionaryParameterInputType.READ_ONLY) {
			topPane.setVisible(false);
			topPane.setManaged(false);
			disableRemoveParameter.setVisible(false);
		}
		
		this.parameterInit = parameterInit;
        parameterInit.setParameterInput(this);
        
		this.dictType = dictType;
		
        constraints.setVisible(false);
        reset.setVisible(false);
        
        if (dictType == DictionaryParameterInputType.JOB_CREATE)
        	constraints.setVisible(true);
        if (dictType == DictionaryParameterInputType.JOB_RESTART)
        	reset.setVisible(true);
        
        resetValue();
	}
	
	public void setInitState(List<DictionaryParameterAndName> mapParametersState) {
		parameterInit.setMapParametersState(mapParametersState);
	}
	
	public void initialize() {
		disableRemoveParameter.setCellFactory(
				new Callback<TableColumn<DictionaryParameterAndName,String>, TableCell<DictionaryParameterAndName,String>>() {
			@Override
			public TableCell<DictionaryParameterAndName, String> call(TableColumn<DictionaryParameterAndName, String> param) {
				return new MultiCell<DictionaryParameterAndName, String>(dictType != DictionaryParameterInputType.INNER) {
					@Override
					protected void handleRemove(ActionEvent event) {
						int index = getIndex();
						parameterInit.getMapParametersState().remove(index);
						data.remove(index);
					}

					@Override
					protected void handleDisable(Boolean newValue) {
						int index = getIndex();
						ParameterInit childParameterInit = parameterInit.getMapParametersState().get(index).getInit();
						ParameterInput childParameterInput = childParameterInit.getParameterInput();
						
						childParameterInit.setImmutable(!newValue);
						childParameterInput.updateDisabled();
					}

					@Override
					protected void handleDisableAndReset(Boolean newValue) {
						int index = getIndex();
						ParameterInit childParameterInit = parameterInit.getMapParametersState().get(index).getInit();
						ParameterInput childParameterInput = childParameterInit.getParameterInput();
						
						childParameterInit.setImmutable(!newValue);
						childParameterInput.updateDisabled();
						if (!newValue)
							childParameterInput.resetValue();
					}
				};
			}
		});
		
		constraints.setCellFactory(new Callback<TableColumn<DictionaryParameterAndName,String>, TableCell<DictionaryParameterAndName,String>>() {
			@Override
			public TableCell<DictionaryParameterAndName, String> call(final TableColumn<DictionaryParameterAndName, String> param) {
				return new LabelCell<DictionaryParameterAndName, String>() {
					@Override
					public String getLabelText() {
						int index = getIndex();
						ParameterInit prmInit = parameterInit.getMapParametersState().get(index).getInit();
						
						if (prmInit.getJobInputParameter().isPresent()) {
							JobInputParameter jip = prmInit.getJobInputParameter().get();
							return jip.getConstraints();
						} else
							return "";
					}
				};
			}
		});
		
		reset.setCellFactory(new Callback<TableColumn<DictionaryParameterAndName,String>, TableCell<DictionaryParameterAndName,String>>() {
			@Override
			public TableCell<DictionaryParameterAndName, String> call(final TableColumn<DictionaryParameterAndName, String> param) {
				return new ButtonCell<DictionaryParameterAndName, String>("Reset") {
					@Override
					protected void handle(ActionEvent event) {
						int index = getIndex();
						ParameterInput parameterInput = parameterInit.getMapParametersState().get(index).getInit().getParameterInput();
						parameterInput.resetValue();
					}
				};
			}
		});
		
		parameterName.setCellFactory(new Callback<TableColumn<DictionaryParameterAndName,String>, TableCell<DictionaryParameterAndName,String>>() {
			@Override
			public TableCell<DictionaryParameterAndName, String> call(TableColumn<DictionaryParameterAndName, String> param) {
				return new TextFieldCell<String>(dictType != DictionaryParameterInputType.INNER) {
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
		
		if (parameterInit != null)
			if (!parameterInit.getMapParametersState().isEmpty()) {
				restoreState();
			} else {
				resetValue();
			}
		parametersTable.setItems(data);
	}

	public void resetValue() {
		parameterInit.getMapParametersState().clear();
		data.clear();
		updateDisabled();
		if (parameterInit.getOldParameter().isPresent()) {
			restoreState((DictionaryParameter)parameterInit.getOldParameter().get());
		} else if (parameterInit.getJobInputParameter().isPresent()) {
			JobInputParameter jip = parameterInit.getJobInputParameter().get();
			if (jip instanceof DefaultValueJobInputParameter) {
				DefaultValueJobInputParameter defaultPrm = (DefaultValueJobInputParameter)jip;
				restoreState((DictionaryParameter)defaultPrm.getDefaultValue());
			}
		} else if (parameterInit.getInputParameters().isPresent()) {
			restoreState(parameterInit.getInputParameters().get());
		}
	}
	
	protected void restoreState(List<JobInputParameter> list) {
		updateDisabled();
		if (list != null) {
			for (JobInputParameter param : list) {
				if (param.getType() == ParameterType.DICTIONARY)
					addParam(param.getName(), new DictionaryParameterInit(param, false));				
				else if (param.getType() == ParameterType.LIST)
					addParam(param.getName(), new ListParameterInit(param));
				else
					addParam(param.getName(), new PrimitiveParameterInit(param));
			}
		}
	}

	protected void restoreState() {
		updateDisabled();
		for (DictionaryParameterAndName dpn : parameterInit.getMapParametersState())
			data.add(dpn);
	}
	
	protected void restoreState(DictionaryParameter prm) {
		updateDisabled();
		if (prm.getValue() != null) {
			for (Entry<String, Parameter> param : prm.getValue().entrySet()) {
				if (param.getValue().getType() == ParameterType.DICTIONARY)
					addParam(param.getKey(), new DictionaryParameterInit((DictionaryParameter)(param.getValue()), false));				
				else if (param.getValue().getType() == ParameterType.LIST)
					addParam(param.getKey(), new ListParameterInit(param.getValue()));
				else
					addParam(param.getKey(), new PrimitiveParameterInit(param.getValue()));
			}
		}
	}
	
	public void showParameterMenu() {
		contextMenu.show(addButton, Side.BOTTOM, 0, 0);
	}
	
	protected void addParam(String name, ParameterInit prm) {
		if (dictType == DictionaryParameterInputType.READ_ONLY) {
			prm.setImmutable(true);
			prm.getParameterInput().updateDisabled();
		}
		
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
		DictionaryParameterInit prm = new DictionaryParameterInit(false);
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
	public Parameter getParameter() throws ParameterStateException {
		return parameterInit.getState();
	}
}
