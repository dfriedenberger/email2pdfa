package de.frittenburger.email2pdfa.interfaces;

import javax.mail.internet.ContentType;

public interface BodyPartWrapper {

	String getContentId();
	String getFilename();
	String getDisposition();
	ContentType getContentType();
	
}
