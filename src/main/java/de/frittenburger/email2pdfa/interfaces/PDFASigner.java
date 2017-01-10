package de.frittenburger.email2pdfa.interfaces;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;

public interface PDFASigner {
	
	void sign(PdfCreatorSignatureData pdfCreatorSignatureData, String senderkey, Sandbox sandbox) throws IOException, GeneralSecurityException;

	Map<String, String> getUnsignedPdfs(String senderkey, Sandbox sandbox);

}
