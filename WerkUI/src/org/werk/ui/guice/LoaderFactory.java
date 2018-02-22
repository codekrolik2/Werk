package org.werk.ui.guice;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import com.google.inject.Injector;

import javafx.fxml.FXMLLoader;
import lombok.Setter;

public class LoaderFactory implements FXMLLoaderFactory {
	private LoaderFactory() {}
	
	static AtomicReference<LoaderFactory> instance = new AtomicReference<>();
	public static LoaderFactory getInstance() {
		instance.compareAndSet(null, new LoaderFactory());
		return instance.get();
	}
	
	@Setter
	Injector injector;
	
	public FXMLLoader loader(URL location) {
		FXMLLoader loader = new FXMLLoader(location);
		loader.setControllerFactory(injector::getInstance);
		return loader;
	}
}
