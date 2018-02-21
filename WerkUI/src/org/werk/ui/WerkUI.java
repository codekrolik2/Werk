package org.werk.ui;

import org.werk.ui.controls.mainapp.MainAppControl;
import org.werk.ui.guice.FXMLLoaderFactory;
import org.werk.ui.guice.LoaderFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Vertx;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class WerkUI extends Application {
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		try {
			WerkUIModule werkUIModule = new WerkUIModule();
			Injector injector = Guice.createInjector(werkUIModule);
			((LoaderFactory)injector.getInstance(FXMLLoaderFactory.class)).setInjector(injector);
			MainAppControl mainApp = injector.getInstance(MainAppControl.class);
			TabCreator tabCreator = injector.getInstance(TabCreator.class);
			mainApp.setTabCreator(tabCreator);
			
			//------------------------------
			
			Scene scene = new Scene(mainApp, 800, 600);
			
			stage.setTitle("Werk UI");
			stage.setScene(scene);
			stage.setResizable(true);
			stage.show();
			
			stage.setOnHidden(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					injector.getInstance(Vertx.class).close();
				}
			});
			
			mainApp.setMain(stage);
			mainApp.showConnectDialog();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
