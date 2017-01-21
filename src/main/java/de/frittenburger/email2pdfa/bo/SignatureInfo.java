package de.frittenburger.email2pdfa.bo;


public class SignatureInfo {

	
	public boolean hasSignature = false;
	public String email = null;
	public String info = null;
	
	public static SignatureInfo create(boolean signed) {
		SignatureInfo info = new SignatureInfo();
		info.hasSignature = signed;
		return info;
	}


}
