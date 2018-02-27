package org.werk.ui.controls.parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.werk.meta.inputparameters.DefaultValueJobInputParameter;
import org.werk.meta.inputparameters.JobInputParameter;
import org.werk.meta.inputparameters.impl.DefaultValueJobInputParameterImpl;
import org.werk.meta.inputparameters.impl.EnumJobInputParameterImpl;
import org.werk.meta.inputparameters.impl.JobInputParameterImpl;
import org.werk.meta.inputparameters.impl.RangeJobInputParameterImpl;
import org.werk.processing.parameters.DictionaryParameter;
import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.processing.parameters.impl.BoolParameterImpl;
import org.werk.processing.parameters.impl.DictionaryParameterImpl;
import org.werk.processing.parameters.impl.DoubleParameterImpl;
import org.werk.processing.parameters.impl.ListParameterImpl;
import org.werk.processing.parameters.impl.LongParameterImpl;
import org.werk.processing.parameters.impl.StringParameterImpl;
import org.werk.ui.controls.parameters.state.DictionaryParameterAndName;
import org.werk.ui.controls.parameters.state.DictionaryParameterInit;
import org.werk.ui.controls.parameters.state.ListParameterInit;
import org.werk.ui.controls.parameters.state.ParameterInit;
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
import javafx.util.Callback;
import lombok.Getter;

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
    TableColumn<DictionaryParameterAndName, String> reset;
	@FXML
    TableColumn<ParameterInit, String> parameterValue;
	
	protected final ObservableList<DictionaryParameterAndName> data = FXCollections.observableArrayList();   
	
	@Getter
	DictionaryParameterInit parameterInit;
	
	protected DictionaryParameterInputType dictType;
	
	static ListParameterImpl createListParameterImpl() {
		List<Parameter> listValues = new ArrayList<>();
		listValues.add(new LongParameterImpl(123L));
		listValues.add(new BoolParameterImpl(true));
		listValues.add(new DoubleParameterImpl(123.45));
		listValues.add(new StringParameterImpl("TEST"));
		listValues.add(createDictionaryParameterImpl(false));
		
		return new ListParameterImpl(listValues);
	}
	
	static DictionaryParameterImpl createDictionaryParameterImpl(boolean topLevel) {
		Map<String, Parameter> values = new HashMap<>();
		
		values.put("Dprm11", new LongParameterImpl(123L));
		values.put("Dprm12", new BoolParameterImpl(true));
		values.put("Dprm13", new DoubleParameterImpl(123.45));
		values.put("Dprm14", new StringParameterImpl("TEST"));
		if (topLevel) {
			values.put("Dprm15", createListParameterImpl());
			values.put("Dprm16", createDictionaryParameterImpl(false));
		}
		
		return new DictionaryParameterImpl(values);
	}
	
	static DictionaryParameterInit createJobParameterInit() {
		JobInputParameter longDefImmPrm = new DefaultValueJobInputParameterImpl("prm11", 
				ParameterType.LONG, "", true, new LongParameterImpl(123L));
		JobInputParameter boolDefImmPrm = new DefaultValueJobInputParameterImpl("prm12", 
				ParameterType.BOOL, "", true, new BoolParameterImpl(true));
		JobInputParameter doubleDefImmPrm = new DefaultValueJobInputParameterImpl("prm13", 
				ParameterType.DOUBLE, "", true, new DoubleParameterImpl(123.45));
		JobInputParameter stringDefImmPrm = new DefaultValueJobInputParameterImpl("prm14", 
				ParameterType.STRING, "", true, new StringParameterImpl("TEST"));
		JobInputParameter listDefImmPrm = new DefaultValueJobInputParameterImpl("prm15", 
				ParameterType.LIST, "", true, createListParameterImpl());
		JobInputParameter dictDefImmPrm = new DefaultValueJobInputParameterImpl("prm16", 
				ParameterType.DICTIONARY, "", true, createDictionaryParameterImpl(true));
		
		JobInputParameter longDefPrm = new DefaultValueJobInputParameterImpl("prm21", 
				ParameterType.LONG, "", false, new LongParameterImpl(1234L));
		JobInputParameter boolDefPrm = new DefaultValueJobInputParameterImpl("prm22", 
				ParameterType.BOOL, "", false, new BoolParameterImpl(false));
		JobInputParameter doubleDefPrm = new DefaultValueJobInputParameterImpl("prm23", 
				ParameterType.DOUBLE, "", false, new DoubleParameterImpl(1234.56));
		JobInputParameter stringDefPrm = new DefaultValueJobInputParameterImpl("prm24", 
				ParameterType.STRING, "", false, new StringParameterImpl("TEST2"));
		JobInputParameter listDefPrm = new DefaultValueJobInputParameterImpl("prm25", 
				ParameterType.LIST, "", false, createListParameterImpl());
		JobInputParameter dictDefPrm = new DefaultValueJobInputParameterImpl("prm26", 
				ParameterType.DICTIONARY, "", false, createDictionaryParameterImpl(true));
		
		JobInputParameter longOptPrm = new JobInputParameterImpl("prm31", ParameterType.LONG, true, "");
		JobInputParameter boolOptPrm = new JobInputParameterImpl("prm32", ParameterType.BOOL, true, "");
		JobInputParameter doubleOptPrm = new JobInputParameterImpl("prm33", ParameterType.DOUBLE, true, "");
		JobInputParameter stringOptPrm = new JobInputParameterImpl("prm34", ParameterType.STRING, true, "");
		JobInputParameter listOptPrm = new JobInputParameterImpl("prm35", ParameterType.LIST, true, "");
		JobInputParameter dictOptPrm = new JobInputParameterImpl("prm36", ParameterType.DICTIONARY, true, "");
		
		/*JobInputParameter longPrm = new JobInputParameterImpl("prm41", ParameterType.LONG, false, "");
		JobInputParameter boolPrm = new JobInputParameterImpl("prm42", ParameterType.BOOL, false, "");
		JobInputParameter doublePrm = new JobInputParameterImpl("prm43", ParameterType.DOUBLE, false, "");
		JobInputParameter stringPrm = new JobInputParameterImpl("prm44", ParameterType.STRING, false, "");
		JobInputParameter listPrm = new JobInputParameterImpl("prm45", ParameterType.LIST, false, "");
		JobInputParameter dictPrm = new JobInputParameterImpl("prm46", ParameterType.DICTIONARY, false, "");*/

		JobInputParameter longPrm = new RangeJobInputParameterImpl("prm41", ParameterType.LONG, false, "",
				new LongParameterImpl(100L), new LongParameterImpl(200L), true, false, false);
		JobInputParameter boolPrm = new JobInputParameterImpl("prm42", ParameterType.BOOL, false, "");
		JobInputParameter doublePrm = new JobInputParameterImpl("prm43", ParameterType.DOUBLE, false, "");
		List<Parameter> enumVals = new ArrayList<>();
		enumVals.add(new StringParameterImpl("ENUM_VALUE1"));
		enumVals.add(new StringParameterImpl("ENUM_VALUE2"));
		enumVals.add(new StringParameterImpl("ENUM_VALUE3"));
		JobInputParameter stringPrm = new EnumJobInputParameterImpl("prm44", ParameterType.STRING, false, "",
				enumVals, false);
		JobInputParameter listPrm = new JobInputParameterImpl("prm45", ParameterType.LIST, false, "");
		JobInputParameter dictPrm = new JobInputParameterImpl("prm46", ParameterType.DICTIONARY, false, "");
		
		List<JobInputParameter> prms = new ArrayList<>();

		prms.add(longDefImmPrm);
		prms.add(boolDefImmPrm);
		prms.add(doubleDefImmPrm);
		prms.add(stringDefImmPrm);
		prms.add(listDefImmPrm);
		prms.add(dictDefImmPrm);
		
		prms.add(longDefPrm);
		prms.add(boolDefPrm);
		prms.add(doubleDefPrm);
		prms.add(stringDefPrm);
		prms.add(listDefPrm);
		prms.add(dictDefPrm);
		
		prms.add(longOptPrm); 
		prms.add(boolOptPrm); 
		prms.add(doubleOptPrm); 
		prms.add(stringOptPrm);
		prms.add(listOptPrm);
		prms.add(dictOptPrm);
		
		prms.add(longPrm);
		prms.add(boolPrm);
		prms.add(doublePrm);
		prms.add(stringPrm);
		prms.add(listPrm);
		prms.add(dictPrm);
		
		return new DictionaryParameterInit(Optional.of(prms), true);
	}
	
	static DictionaryParameterInit createParameterInit() {
		return new DictionaryParameterInit(createDictionaryParameterImpl(true), true);
	}
	
	public DictionaryParameterInput() {
		this(createJobParameterInit(), DictionaryParameterInputType.JOB_CREATE);
		//this(createParameterInit(), DictionaryParameterInputType.JOB_RESTART);
	}
	
	public DictionaryParameterInput(DictionaryParameterInit parameterInit, DictionaryParameterInputType dictType) {
		this.parameterInit = parameterInit;
		this.dictType = dictType;
        parameterInit.setParameterInput(this);
		
        FXMLLoader fxmlLoader = LoaderFactory.getInstance().loader(getClass().getResource("DictionaryParameterInput.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        constraints.setVisible(false);
        reset.setVisible(false);
        
        if (dictType == DictionaryParameterInputType.JOB_CREATE)
        	constraints.setVisible(true);
        if (dictType == DictionaryParameterInputType.JOB_RESTART)
        	reset.setVisible(true);
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
						ParameterInput parameterInput = parameterInit.getMapParametersState().get(index).getInit().getParameterInput();
						
						parameterInput.setImmutable(!newValue);
						parameterInput.updateDisabled();
					}

					@Override
					protected void handleDisableAndReset(Boolean newValue) {
						int index = getIndex();
						ParameterInput parameterInput = parameterInit.getMapParametersState().get(index).getInit().getParameterInput();
						
						parameterInput.setImmutable(!newValue);
						parameterInput.updateDisabled();
						if (!newValue)
							parameterInput.resetValue();
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
					addParam(param.getKey(), new DictionaryParameterInit(param.getValue(), false));				
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
	public Parameter getParameter() {
		return parameterInit.getState();
	}
}
