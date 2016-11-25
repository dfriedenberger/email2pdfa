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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.frittenburger.email2pdfa.bo.EmailHeader;
import de.frittenburger.email2pdfa.bo.MessageContext;
import de.frittenburger.email2pdfa.bo.MimeMessageParserException;
import de.frittenburger.email2pdfa.interfaces.MimeMessageParser;
import de.frittenburger.email2pdfa.interfaces.NameService;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class MimeMessageParserImpl implements MimeMessageParser {

	private NameService nameService = new NameServiceImpl();

	private static final String emailheaderjsonfile = "emailheader.json";

	public void parse(String emlFile, Sandbox sandbox) throws MimeMessageParserException {

		
		System.out.println("read "+emlFile);
		try {
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			InputStream is = null;
			MimeMessage mime = null;
			try {
				is = new FileInputStream(emlFile);
				mime = new MimeMessage(session, is);
			} finally {
				if (is != null)
					is.close();
				is = null;
			}

			EmailHeader header = nameService.getEmailHeader(mime);
			MessageContext messageContext = new MessageContext();

			messageContext.mesgPath = sandbox.getMessagePath() + "/" + header.mesgkey;

			//Create Path
			File mesgDirectory = new File(messageContext.mesgPath);
			
			if(mesgDirectory.exists())
			{
				System.out.println(messageContext.mesgPath+" exists");
				return;
			}
			mesgDirectory.mkdir();

			//Create emailheader.json
			ObjectMapper mapper = new ObjectMapper();
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(messageContext.mesgPath + "/" + emailheaderjsonfile),
					header);

			//Parse content
			messageContext.encoding = mime.getEncoding();
			messageContext.contentFilename = null;
			parseContent(messageContext, mime.getContent(), mime.getContentType(), mime.getDisposition());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new MimeMessageParserException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new MimeMessageParserException(e);
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new MimeMessageParserException(e);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			throw new MimeMessageParserException(e);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new MimeMessageParserException(e);
		}

	}

	private void parseContent(MessageContext mesgContext, Object msgContent, String contentType, String disposition)
			throws IOException, MessagingException {

		ContentType ct = new ContentType(contentType);
		System.out.println("disposition " + disposition);
		System.out.println("Part " + msgContent.getClass() + " type=" + ct.getBaseType());

		if (msgContent instanceof Multipart) {
			Multipart multipart = (Multipart) msgContent;
			// System.out.println("MultiPartContentType
			// "+multipart.getContentType());

			// Pfad anlegen
			String saveMesgPath = mesgContext.mesgPath;
			mesgContext.mesgPath = nameService.getFolder(mesgContext, ct);
			new File(mesgContext.mesgPath).mkdir();
			
			for (int j = 0; j < multipart.getCount(); j++) {

				BodyPart bodyPart = multipart.getBodyPart(j);

				mesgContext.contentFilename = null;

				// has Filename?
				String filename = bodyPart.getFileName();
				if (filename != null) {
					mesgContext.contentFilename = MimeUtility.decodeText(filename);
				}

				String[] contentID = bodyPart.getHeader("Content-ID");
				if (contentID != null) {
					mesgContext.contentFilename = nameService.parseContentId(contentID[0].trim());
				}
				parseContent(mesgContext, bodyPart.getContent(), bodyPart.getContentType(), bodyPart.getDisposition());
			}
			// Ruecksetzen
			mesgContext.mesgPath = saveMesgPath;

		} else {

			if (msgContent instanceof String) {
				String str = (String) msgContent;
				save(mesgContext, new ByteArrayInputStream(str.getBytes()), ct);

			} else if (msgContent instanceof InputStream) {
				// BASE64DecoderStream
				// QPDecoderStream
				// SharedByteArrayInputStream
				InputStream is = (InputStream) msgContent;
				save(mesgContext, is, ct);
			} else if (msgContent instanceof MimeMessage) {
				MimeMessage mimeMessage = (MimeMessage) msgContent;
				save(mesgContext, mimeMessage.getRawInputStream(), new ContentType("message/rfc822"));
			} else {
				throw new RuntimeException("Not implemented " + msgContent);
			}
		}

	}

	private void save(MessageContext mesgContext, InputStream in, ContentType contentType) throws IOException {

		if (mesgContext.contentFilename == null)
			mesgContext.contentFilename = nameService.getFilename(mesgContext, contentType);

		System.out.println("fileName " + mesgContext.contentFilename);

		FileOutputStream out = new FileOutputStream(mesgContext.mesgPath + "/" + mesgContext.contentFilename);
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len != -1) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}

		out.close();

	}

}
