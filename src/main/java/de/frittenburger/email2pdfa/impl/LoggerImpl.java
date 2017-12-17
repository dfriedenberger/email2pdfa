package de.frittenburger.email2pdfa.impl;

import de.frittenburger.email2pdfa.interfaces.Logger;

public class LoggerImpl implements Logger {

	private final String name;

	public LoggerImpl(String name)
	{
		this.name = name;
	}

	@Override
	public void infoFormat(String format, Object... args) {
		System.out.println("[INFO] "+name+" "+String.format(format, args));		
	}
	
	@Override
	public void info(Object obj) {
		System.out.println("[INFO] "+name+" "+obj);		
	}
	
	@Override
	public void errorFormat(String format, Object... args) {
		System.err.println("[ERROR] "+name+" "+String.format(format, args));				
	}
	
	@Override
	public void error(Object obj) {
		System.err.println("[ERROR] "+name+" "+obj);				
	}

}
