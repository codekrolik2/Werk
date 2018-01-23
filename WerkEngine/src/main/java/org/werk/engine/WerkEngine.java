package org.werk.engine;

import org.pillar.commandline.CommandLineProcessor;

public class WerkEngine extends CommandLineProcessor {
	
	
	
	public static void main(String[] args) throws Exception {
		WerkEngine engine = new WerkEngine();
		engine.readInput();
	}

	@Override
	protected void processCommand(String command) throws Exception {
		System.out.println(command);
	}
}
