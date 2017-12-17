package de.frittenburger.email2pdfa.interfaces;

import javax.mail.MessagingException;

import de.frittenburger.email2pdfa.bo.ParamHeader;

public interface HeaderParser {

	public ParamHeader parse(String[] header) throws MessagingException;

}
