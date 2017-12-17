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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

	private static final String allowedfilenamechar = "[]#+-_=.~";
	
	private static String MIMETypes = "src/main/resources/mime/extensions.txt";
	static Map<String,String> extensions = new HashMap<String,String>();
	
	
	static {
		BufferedReader in = null;
		try {
			File fileDir = new File(MIMETypes);
			in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF8"));

			String str;

			while ((str = in.readLine()) != null) {
				str = str.trim();
				if (str.isEmpty())
					continue;
				if (str.startsWith("#"))
					continue;
				int i = str.indexOf("=");
				if (i < 0)
					continue;

				String ext = str.substring(0, i).trim();
				String contentType = str.substring(i + 1).trim();
				if (extensions.containsKey(contentType))
					throw new IOException("Contenttype also registered " + contentType);
				extensions.put(contentType, ext);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally
		{
			if(in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}




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
			
		String[] sender = message.getHeader("Sender");
		emailHeader.sender = new EmailHeaderFrom[0];
		if(sender != null && sender.length > 0 && !sender[0].trim().equals("")) //Empty Sender
		{
			if(sender.length != 1)
				throw new MessagingException("Invalid Sender-Header");
			
			emailHeader.sender = new EmailHeaderFrom[sender.length];
			for(int i = 0;i < from.length;i++)
			{
				emailHeader.sender[i] = new EmailHeaderFrom();
				emailHeader.sender[i].name = parseFromName(sender[i]);
				emailHeader.sender[i].address = parseFromAddress(sender[i]);
			}
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
		
		
		String keyAddress = emailHeader.from[0].address;
		if(emailHeader.sender.length > 0)
			keyAddress = emailHeader.sender[0].address;
		
		emailHeader.senderkey = keyAddress;
		emailHeader.mesgkey = keyAddress+"_"+parseDate(emailHeader.date[0])+"_"+hashtext.substring(0,6);
		
		return emailHeader;
	}

	
	public String parseFromAddress(String email) throws AddressException {
		 if(email.trim().equals("")) throw new AddressException("Empty Address");
		 InternetAddress emailAddr = new InternetAddress(email);
	      emailAddr.validate();		
	      return emailAddr.getAddress();
	}

	public String parseFromName(String email) throws AddressException {
		 if(email.trim().equals("")) throw new AddressException("Empty Address");

		 InternetAddress emailAddr = new InternetAddress(email);
	      emailAddr.validate();		
	      return emailAddr.getPersonal();
	}
	
	
	

	private static  DateFormat[] dateformats = new DateFormat[]{
				new MailDateFormat() , 
				new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH) //Sun Feb 28 13:17:24 GMT+01:00 2016
	};

	public String parseDate(String date) throws ParseException   {
       SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmm");
       Date result = null;
       for(DateFormat df : dateformats)
       {
    	   result =  df.parse(date); 
    	   if(result != null) break;
       }
       if(result == null)
			throw new ParseException("could not parse "+date,0);
		return formatter.format(result);
	}

	/*
		text		fuer Textdateien
		image		fuer Grafikdateien
		video		fuer Videodateien
		audio		fuer Sound-Dateien
		application	fuer Dateien, die an ein bestimmtes Programm gebunden sind
		multipart	fuer mehrteilige Daten
		message		fuer Nachrichten
		model		fuer Dateien, die mehrdimensionale Strukturen repräsentieren
		font    	fuer Schriften
    */
	
	private String getExtension(ContentType ct) {
		
		String baseType = ct.getBaseType();
		
		if(extensions.containsKey(baseType))
			return extensions.get(baseType);

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


	@Override
	public String parseValidFilename(String decodedFilename,ContentType contentType) {
		
		char c[] = decodedFilename.toCharArray();
		
		for(int i = 0;i < c.length;i++)
		{
			
			if('a' <= c[i] && c[i] <= 'z') continue;
			if('A' <= c[i] && c[i] <= 'Z') continue;
			if('0' <= c[i] && c[i] <= '9') continue;
			if(allowedfilenamechar.indexOf(c[i]) >= 0) continue;
			
			//replace
			c[i] = '_';
		}
		
		//check Extension
        String filename = new String(c);
		String extension = parseExtension(filename);
		if(extension == null || extension.length() > 4)
		{
			String ext = getExtension(contentType);
        	filename += ext;
		}	
		
		return filename;
	}


	@Override
	public String parseExtension(String fileName) {
		
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			return fileName.substring(i + 1).toUpperCase();
		}
		return null;
	}
	
}
