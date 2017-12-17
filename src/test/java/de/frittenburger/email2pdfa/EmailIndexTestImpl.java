package de.frittenburger.email2pdfa;

import java.io.IOException;
import java.util.List;

import de.frittenburger.email2pdfa.interfaces.EmailIndex;

public class EmailIndexTestImpl implements EmailIndex {

	private final List<String> messages;

	public EmailIndexTestImpl(List<String> messages) {
		this.messages = messages;
	}

	@Override
	public void init(String emailIndexFilePath) throws IOException {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void commit() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void register(String store, String folder, String messageKey, int index) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void registerError(String store, String folder, int index) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public int getUpperMost(String store, String folder) {
		return messages.size();
	}

	@Override
	public int getIndex(String store, String folder, String messageKey) {
		for(int i = 0;i < messages.size();i++)
		{
			if(messages.get(i) == null) continue;
			if(messages.get(i).equals(messageKey)) return i + 1;
		}
		return -1;
	}

	@Override
	public void clear(String storeKey, String folder,int s,int e) {
		
		for(int i = s;i <=e;i++)
			messages.remove(s-1);
		
	}

}
