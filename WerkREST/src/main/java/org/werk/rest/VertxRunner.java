package org.werk.rest;

import java.util.function.Consumer;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class VertxRunner {
	public static void runVerticle(Verticle verticle) {
		//String verticleID = clazz.getName();
		VertxOptions options = new VertxOptions().setClustered(false);

		Consumer<Vertx> runner = vertx -> {
			try {
				vertx.deployVerticle(verticle);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		};
			
		Vertx vertx = Vertx.vertx(options);
		runner.accept(vertx);
	}
}