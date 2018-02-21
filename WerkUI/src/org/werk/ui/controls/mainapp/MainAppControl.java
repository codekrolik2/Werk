package org.werk.ui.controls.mainapp;

import java.io.IOException;

import org.werk.ui.TabCreator;
import org.werk.ui.controls.connectform.ConnectFormController;
import org.werk.ui.controls.serverinfoform.ServerInfoController;
import org.werk.ui.guice.FXMLLoaderFactory;
import org.werk.ui.util.ModalWindow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;

public class MainAppControl extends VBox {
	@Setter
	TabCreator tabCreator;
	
	@Setter
	Stage main;
	
	@FXML
	Label serverInfoLabel;
	
	@FXML
	TabPane tabs;
	
    public MainAppControl(FXMLLoaderFactory loaderFactory) {
        FXMLLoader fxmlLoader = loaderFactory.loader(getClass().getResource("MainApp.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
	
	public void showConnectDialog() {
		ConnectFormController connectForm = tabCreator.getConnectFormController();
		ModalWindow window = new ModalWindow(connectForm, "Connect");
		Stage dialogStage = window.showModal(main, this.getScene().getWindow());
		connectForm.setDialogStage(dialogStage);
	}
	
	public void setStatusText(String text) {
		serverInfoLabel.setText(text);
	}
	
	public void createServerInfoTab() {
		ServerInfoController serverInfoController = tabCreator.getServerInfoController();
		final Tab tab = new Tab("Server Info", serverInfoController);
		tab.setClosable(true);
        
		tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        
        serverInfoController.setServerInfo();
    }
	
	public void quit() {
		main.close();
	}
}
