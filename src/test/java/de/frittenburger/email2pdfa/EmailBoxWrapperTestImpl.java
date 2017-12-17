package de.frittenburger.email2pdfa;

import java.io.IOException;
import java.util.List;

import de.frittenburger.email2pdfa.bo.FolderWrapper;

public class EmailBoxWrapperTestImpl extends  FolderWrapper {

	private final List<String> inbox;
	private int readoperations = 0;


	public EmailBoxWrapperTestImpl(List<String> inbox,String folder) throws IOException {
		super(null,folder,inbox.size());
		this.inbox = inbox;
	}

	@Override	
	public String listMessage(int u) throws IOException {
		if(u > getCount()) 
			return null;
		readoperations++;
		return inbox.get(u-1);
	}

	public int getReadOperations() {
		return readoperations;
	}
}
