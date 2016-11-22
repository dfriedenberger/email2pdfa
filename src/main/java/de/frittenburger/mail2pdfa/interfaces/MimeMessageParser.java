package de.frittenburger.mail2pdfa.interfaces;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

import javax.mail.MessagingException;

public interface MimeMessageParser {

	void parse(String emlFile, String path) throws IOException, MessagingException, GeneralSecurityException, ParseException;

}
