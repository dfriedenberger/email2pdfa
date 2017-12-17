package de.frittenburger.email2pdfa.interfaces;


public interface Index {

	void register(int index, String key);

	int getIndex(String key);

	int getUpperMost();

	void remove(int s, int e);


}
