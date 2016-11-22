package de.frittenburger.mail2pdfa.bo;

import java.util.HashSet;
import java.util.Set;

public class MessageContext {

	public String mesgPath; //SandboxPath
	public String encoding;
	
	public String contentFilename;

	public Set<String> pathCache = new HashSet<String>();
	

}
