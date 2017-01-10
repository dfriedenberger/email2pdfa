package de.frittenburger.email2pdfa.interfaces;

import java.util.Set;

import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;

public interface Configuration {

	Set<String> getEmailServiceAccountDataKeys();
	EmailServiceAccountData getEmailServiceAccountData(String key);
	
	Set<String> getPdfCreatorSignatureDataKeys();
	PdfCreatorSignatureData getPdfCreatorSignatureData(String key);

}
