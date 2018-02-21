package org.werk.ui;

import org.werk.restclient.WerkRESTClient;
import org.werk.ui.controls.mainapp.MainAppControl;
import org.werk.ui.guice.FXMLLoaderFactory;
import org.werk.ui.guice.LoaderFactory;

import com.google.inject.AbstractModule;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.VertxImpl;

public class WerkUIModule extends AbstractModule {
	@Override
	protected void configure() {
		VertxOptions opt = new VertxOptions().
				setEventLoopPoolSize(1).
				setWorkerPoolSize(1).
				setInternalBlockingPoolSize(1);
		Vertx vertx = VertxImpl.factory.vertx(opt);
		bind(Vertx.class).toInstance(vertx);
		
		WerkRESTClient werkClient = WerkRESTClient.create(vertx);
		bind(WerkRESTClient.class).toInstance(werkClient);
		
		LoaderFactory loaderFactory = new LoaderFactory();
		bind(FXMLLoaderFactory.class).toInstance(loaderFactory);
		
		MainAppControl mainAppControl = new MainAppControl(loaderFactory);
		bind(MainAppControl.class).toInstance(mainAppControl);
		
		ServerInfoManager serverInfoManager = new ServerInfoManager(); 
		bind(ServerInfoManager.class).toInstance(serverInfoManager);
	}
}