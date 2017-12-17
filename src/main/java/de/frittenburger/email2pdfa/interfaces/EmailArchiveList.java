package de.frittenburger.email2pdfa.interfaces;

import java.io.IOException;

public interface EmailArchiveList {

	String getCkSum(String file);

	void putCkSum(String file, String md5);

	void init(String filename) throws IOException;

}
