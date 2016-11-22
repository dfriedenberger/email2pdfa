package de.frittenburger.mail2pdfa.interfaces;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;

import de.frittenburger.mail2pdfa.bo.EmailHeader;
import de.frittenburger.mail2pdfa.bo.MessageContext;

public interface NameService {



	String parseContentId(String trim);

	String getFolder(MessageContext mesgContext, ContentType contentType);

	String getFilename(MessageContext mesgContext, ContentType contentType);


	EmailHeader getEmailHeader(Message message) throws GeneralSecurityException, MessagingException, ParseException, UnsupportedEncodingException;

}
