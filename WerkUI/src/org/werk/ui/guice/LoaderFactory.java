package org.werk.ui.guice;

import java.net.URL;

import com.google.inject.Injector;

import javafx.fxml.FXMLLoader;
import lombok.Setter;

public class LoaderFactory implements FXMLLoaderFactory {
	@Setter
	Injector injector;
	
	public FXMLLoader loader(URL location) {
		FXMLLoader loader = new FXMLLoader(location);
		loader.setControllerFactory(injector::getInstance);
		return loader;
	}
}
