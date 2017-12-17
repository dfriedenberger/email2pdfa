package de.frittenburger.email2pdfa.interfaces;

import java.io.IOException;

public interface EmailIndex {

	void init(String emailIndexFilePath) throws IOException;
	void commit();

	void register(String store, String folder, String messageKey, int index);
	void registerError(String store, String folder, int index);

	int getUpperMost(String store, String folder);

	int getIndex(String store, String folder, String messageKey);
	
	void clear(String storeKey, String folder, int s, int e);
	

 
	
	
}
