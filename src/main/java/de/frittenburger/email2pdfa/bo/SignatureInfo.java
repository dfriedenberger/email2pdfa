package de.frittenburger.email2pdfa.bo;

import java.util.HashMap;
import java.util.Map;

public class SignatureInfo {

	public SignatureInfo(boolean hasSignature) {
		this.hasSignature = hasSignature;
	}

	public boolean hasSignature = false;
	public Map<String,String> subject = new HashMap<String,String>();

}
