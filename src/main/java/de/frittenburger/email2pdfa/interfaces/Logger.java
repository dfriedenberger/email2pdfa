package de.frittenburger.email2pdfa.interfaces;

public interface Logger {

	void infoFormat(String format, Object ... args);
	void info(Object obj);

	void errorFormat(String format, Object ... args);
	void error(Object obj);

}
