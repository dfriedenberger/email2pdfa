package de.frittenburger.email2pdfa.interfaces;

import java.io.IOException;

public interface EmailCache {

	void init(String emailCacheFile) throws IOException;

	String getMesgKey(String emlFile);

	boolean exists(String msgkey);

	void add(String msgkey, String emlFile);

	
}
