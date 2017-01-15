package de.frittenburger.email2pdfa.interfaces;


import java.io.IOException;

import javax.mail.internet.MimeMessage;
import de.frittenburger.email2pdfa.bo.SignatureInfo;

public interface SignatureEngine {

	public SignatureInfo check(MimeMessage message) throws IOException;
	

}
