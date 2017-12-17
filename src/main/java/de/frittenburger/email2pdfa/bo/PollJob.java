package de.frittenburger.email2pdfa.bo;

import de.frittenburger.email2pdfa.interfaces.Sequence;

public class PollJob {
	
	

	private final String account;
	private final String folder;
	private final Sequence sequence;
	
	public PollJob(String account, String folder, Sequence sequence) {
		
		this.account = account;
		this.folder = folder;
		this.sequence = sequence;
	
	}
	
	public String getAccount() {
		return account;
	}

	public String getFolder() {
		return folder;
	}

	public Sequence getSequence() {
		return sequence;
	}
}
