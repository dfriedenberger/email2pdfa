package de.frittenburger.email2pdfa.bo;

import java.io.IOException;

import de.frittenburger.email2pdfa.impl.EmailBoxReader;

public class FolderWrapper {

	private final EmailBoxReader reader;
	private final String folder;
	private int cnt;

	public FolderWrapper(EmailBoxReader reader, String folder,int cnt) throws IOException {
		this.reader = reader;
		this.folder = folder;
		this.cnt = cnt;
	}

	public int getCount() {
		return cnt;
	}

	public String getName() {
		return folder;
	}

	
	
	public String listMessage(int u) throws IOException {
		if(u > cnt) 
			return null;
		if(u < 1)
			throw new IOException("Index "+u+" not allowed");
		
		return reader.listMessage(folder,u);
	}

	

}
