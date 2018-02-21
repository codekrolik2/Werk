package org.werk.ui.guice;

import java.net.URL;

import javafx.fxml.FXMLLoader;

public interface FXMLLoaderFactory {
	FXMLLoader loader(URL location);
}
