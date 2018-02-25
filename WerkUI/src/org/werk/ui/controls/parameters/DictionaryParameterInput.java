package org.werk.ui.controls.parameters;

import java.io.IOException;

import org.werk.processing.parameters.Parameter;
import org.werk.processing.parameters.ParameterType;
import org.werk.ui.controls.parameters.state.DictionaryParameterInit;
import org.werk.ui.controls.parameters.state.ParameterInit;
import org.werk.ui.controls.parameters.state.PrimitiveParameterInit;
import org.werk.ui.controls.table.ParameterInputCell;
import org.werk.ui.guice.LoaderFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
	protected TableView<ParameterInit> parametersTable;
	
	@FXML
    TableColumn<ParameterInit, String> disableRemoveParameter;
	@FXML
    TableColumn<ParameterInit, String> parameterName;
	@FXML
    TableColumn<ParameterInit, String> constraints;
	@FXML
    TableColumn<ParameterInit, String> parameterValue;
	
	protected final ObservableList<ParameterInit> data = FXCollections.observableArrayList();   
	
	@Setter
	DictionaryParameterInit parameterInit;
	
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
	}
	
	public void initialize() {
		parameterValue.setCellFactory(new Callback<TableColumn<ParameterInit,String>, TableCell<ParameterInit,String>>() {
			@Override
			public TableCell<ParameterInit, String> call(TableColumn<ParameterInit, String> param) {
				return new ParameterInputCell<String>();
			}
		});
		
		System.out.println(System.identityHashCode(parametersTable.toString()));
		parametersTable.setItems(data);
	}
	
	public void showParameterMenu() {
		contextMenu.show(addButton, Side.BOTTOM, 0, 0);
	}
	
	public void addLongParameter() {
		//TODO: add PrimitiveParameterInit to init map
		data.add(new PrimitiveParameterInit(ParameterType.LONG));
	}
	
	public void addDictionaryParameter() {
		//TODO: add DictionaryParameterInit to init map
		data.add(new DictionaryParameterInit(ParameterType.DICTIONARY));
	}
	
	@Override
	public Parameter getParameter() {
		return parameterInit.getState();
	}
}
