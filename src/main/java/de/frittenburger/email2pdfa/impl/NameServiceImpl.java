package de.frittenburger.email2pdfa.impl;
/*
 *  Copyright notice
 *
 *  (c) 2016 Dirk Friedenberger <projekte@frittenburger.de>
 *
 *  All rights reserved
 *
 *  This script is part of the Email2PDFA project. The Email2PDFA is
 *  free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The GNU General Public License can be found at
 *  http://www.gnu.org/copyleft/gpl.html.
 *
 *  This script is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  This copyright notice MUST APPEAR in all copies of the script!
 */

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;

import de.frittenburger.email2pdfa.bo.EmailHeader;
import de.frittenburger.email2pdfa.bo.EmailHeaderFrom;
import de.frittenburger.email2pdfa.bo.MessageContext;
import de.frittenburger.email2pdfa.interfaces.NameService;

public class NameServiceImpl implements NameService {

	public EmailHeader getEmailHeader(Message message) throws GeneralSecurityException, MessagingException, ParseException, UnsupportedEncodingException {
		
		EmailHeader emailHeader = new EmailHeader();
		
		emailHeader.date = message.getHeader("Date");
		if(emailHeader.date == null || emailHeader.date.length != 1)
			throw new MessagingException("Invalid Date-Header");
			
		String[] from = message.getHeader("From");
		if(from == null || from.length != 1)
			throw new MessagingException("Invalid From-Header");
		
		
		emailHeader.from = new EmailHeaderFrom[from.length];
		for(int i = 0;i < from.length;i++)
		{
			emailHeader.from[i] = new EmailHeaderFrom();
			emailHeader.from[i].name = parseFromName(from[i]);
			emailHeader.from[i].address = parseFromAddress(from[i]);
		}
						
		emailHeader.subject = message.getSubject();
		if(emailHeader.subject == null) emailHeader.subject = "";

		
		String key = "";
		for(String d : emailHeader.date) key += d;
		for(String f : from) key += f;
		key += emailHeader.subject;
		
		
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(key.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);		
		String hashtext = bigInt.toString(16);
		
		emailHeader.senderkey = parseFromAddress(from[0]);
		
		emailHeader.mesgkey = parseFromAddress(from[0])+"_"+parseDate(emailHeader.date[0])+"_"+hashtext.substring(0,6);
		
		return emailHeader;
	}

	
	private String parseFromAddress(String email) throws AddressException {
		 InternetAddress emailAddr = new InternetAddress(email);
	      emailAddr.validate();		
	      return emailAddr.getAddress();
	}

	private String parseFromName(String email) throws AddressException {
		 InternetAddress emailAddr = new InternetAddress(email);
	      emailAddr.validate();		
	      return emailAddr.getPersonal();
	}
	
	private String parseDate(String date) throws ParseException   {
       SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmm");
		DateFormat df = new MailDateFormat();
		Date result =  df.parse(date); 
		return formatter.format(result);
	}


	private String getExtension(ContentType ct) {
		
		String baseType = ct.getBaseType();
		
		if(baseType.equals("text/plain")) return ".txt";
		if(baseType.equals("text/html")) return ".html";
		if(baseType.equals("text/calendar")) return ".ics";

		if(baseType.equals("image/jpeg")) return ".jpg";
		if(baseType.equals("image/gif")) return ".gif";
		if(baseType.equals("image/png")) return ".png";

		if(baseType.equals("application/pdf")) return ".pdf";

		
		if(baseType.equals("application/octet-stream")) return ".bin"; //Keine Zuordnung
		
		// multipart/report
		if(baseType.equals("message/delivery-status")) return ".txt";
		if(baseType.equals("text/rfc822-headers")) return ".txt";
		if(baseType.equals("text/rfc822")) return ".txt";
		if(baseType.equals("message/rfc822")) return ".txt";

		throw new RuntimeException("Not implemented " + baseType);
	}

	private String getMultiPartType(ContentType ct) {
	
		String baseType = ct.getBaseType();
		
		if(baseType.equals("multipart/mixed")) return "mixed";
		if(baseType.equals("multipart/alternative")) return "alternative";
		if(baseType.equals("multipart/related")) return "related";
		if(baseType.equals("multipart/signed")) return "signed";
		if(baseType.equals("multipart/report")) return "report";
		throw new RuntimeException("Not implemented " + baseType);
	}



	public String parseContentId(String cid) {
		return cid.substring(1, cid.length()-1);
	}


	public String getFolder(MessageContext mesgContext, ContentType contentType) {
		
		String multipartType = getMultiPartType(contentType);
		
				for(int i = 1;i < 999;i++)
				{
					String name = mesgContext.relavtivePath + "/" + String.format("%s%03d", multipartType,i);
					if(mesgContext.pathCache.contains(name)) continue;
					mesgContext.pathCache.add(name);
					return name;			
				}
				throw new RuntimeException("could not create more than 999 folder names");
	}


	

	public String getFilename(MessageContext mesgContext, ContentType contentType) {
		
		String ext = getExtension(contentType);
		for(int i = 1;i < 999;i++)
		{
			String name = String.format("part%03d%s", i,ext);
			String fullname = mesgContext.mesgPath() + "/" + name;
			
			if(mesgContext.pathCache.contains(fullname)) continue;
			mesgContext.pathCache.add(fullname);
			return name;			
		}
		throw new RuntimeException("could not create more than 999 file names");
	}
	
}
