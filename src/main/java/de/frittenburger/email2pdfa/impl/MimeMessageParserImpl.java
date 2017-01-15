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
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
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
import de.frittenburger.email2pdfa.bo.SignatureInfo;
import de.frittenburger.email2pdfa.interfaces.EmailCache;
import de.frittenburger.email2pdfa.interfaces.MimeMessageParser;
import de.frittenburger.email2pdfa.interfaces.NameService;
import de.frittenburger.email2pdfa.interfaces.Sandbox;
import de.frittenburger.email2pdfa.interfaces.SignatureEngine;

public class MimeMessageParserImpl implements MimeMessageParser {

	private final NameService nameService = new NameServiceImpl();
	private final SignatureEngine signaturEngine = new SignatureEngineImpl();
	private final EmailCache emailCache;

	private static final String emailheaderfile = "header.txt";
	private static final String emailheaderjsonfile = "emailheader.json";
	private static final String signaturejsonfile = "signature.json";
	private static final String mappingjsonfile = "mapping.json";
	private static final String orderjsonfile = "order.json";
	private static final String encodingjsonfile = "encoding.json";

	public MimeMessageParserImpl(EmailCache emailCache) {
		this.emailCache = emailCache;
	}



	@Override
	public String getTargetDir(String emlFile, Sandbox sandbox) throws MimeMessageParserException {
		
		try
		{
			String messageKey = emailCache.getMesgKey(new File(emlFile).getName());

			if(messageKey == null)
			{
				System.err.println("No Messagekey found for "+emlFile);
				MimeMessage mime = readMime(emlFile);
				EmailHeader header = nameService.getEmailHeader(mime);
				messageKey = header.mesgkey;
			}
		
			return getRootPath(sandbox,messageKey);
		
		} catch (IOException e) {
			throw new MimeMessageParserException(e);
		} catch (MessagingException e) {
			throw new MimeMessageParserException(e);
		} catch (GeneralSecurityException e) {
			throw new MimeMessageParserException(e);
		} catch (ParseException e) {
			throw new MimeMessageParserException(e);
		}
	}
	


	private String getRootPath(Sandbox sandbox, String messageKey) {
		return sandbox.getMessagePath() + "/" + messageKey;
	}



	@Override
	public void parse(String emlFile, Sandbox sandbox) throws MimeMessageParserException {

		
		println("read "+emlFile);
		try {
			
			List<String> rawheader = readHeader(emlFile);
			MimeMessage mime = readMime(emlFile);
			
			validateMime(mime,emlFile);
			
			EmailHeader header = nameService.getEmailHeader(mime);
			SignatureInfo signatureInfo = signaturEngine.check(mime);

			
			MessageContext messageContext = new MessageContext();
			messageContext.rootPath = getRootPath(sandbox,header.mesgkey);
			
			//Create Path
			File mesgDirectory = new File(messageContext.rootPath);
			if(!mesgDirectory.exists())
			{
				mesgDirectory.mkdir();
			}

			//Create headers.txt
			writeHeader(messageContext.rootPath + "/" + emailheaderfile,rawheader);
			
			
			//Create emailheader.json
			ObjectMapper mapper = new ObjectMapper();
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(messageContext.rootPath + "/" + emailheaderjsonfile),
					header);

			//Create signature.json
			if(signatureInfo.hasSignature)
			{
				mapper.writerWithDefaultPrettyPrinter().writeValue(new File(messageContext.rootPath + "/" + signaturejsonfile),
						signatureInfo);
			}
			//Parse content
			messageContext.encoding = mime.getEncoding();
			messageContext.contentFilename = null;
			messageContext.contentId = null;
						
			parseContent(messageContext, mime.getContent(), mime.getContentType(), mime.getDisposition());
			
			//Create order.json
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(messageContext.rootPath + "/" + orderjsonfile),
					messageContext.order);
			
			//Create mapping.json
			if(messageContext.contentIdMapping.size() > 0)
			{
				mapper.writerWithDefaultPrettyPrinter().writeValue(new File(messageContext.rootPath + "/" + mappingjsonfile),
						messageContext.contentIdMapping);
			}
			
			//Create encoding.json
			if(messageContext.contentEncoding.size() > 0)
			{
				mapper.writerWithDefaultPrettyPrinter().writeValue(new File(messageContext.rootPath + "/" + encodingjsonfile),
						messageContext.contentEncoding);
			}
			
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

	private void println(Object obj) {
		//Ignore
	}



	private void writeHeader(String filename, List<String> lines) throws IOException {

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filename));

			for (String line : lines) {
				bw.write(line);
				bw.newLine();
			}
		} finally {
			if (bw != null)

				bw.close();
		}
	}



	//Read and parse the given RFC822 message stream till the blank line separating the header from the body.
	private List<String> readHeader(String emlFile) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(emlFile));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().equals(""))
					break;
				lines.add(line);
			}
		} finally {
			if (reader != null)
				reader.close();
		}

		return lines;
	}


	private MimeMessage readMime(String emlFile) throws IOException, MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		InputStream is = null;
		MimeMessage mime = null;
		try {
			is = new FileInputStream(emlFile);
			mime = new MimeMessage(session, is);
			return mime;
		} finally {
			if (is != null)
				is.close();
			is = null;
		}
	}
	
	
	private void validateMime(MimeMessage mime,String emlFile) throws IOException, MessagingException {

		
		String[] transferEncoding = mime.getHeader("Content-Transfer-Encoding");
		if(transferEncoding != null)
		{
			for(String h : transferEncoding)
			{
				if(h.trim().equals("")) 
					throw new IOException("Empty Content-Transfer-Encoding Header in " +  emlFile);
			}
		}
		
	}



	private void parseContent(MessageContext mesgContext, Object msgContent, String contentType, String disposition)
			throws IOException, MessagingException {

		ContentType ct = new ContentType(contentType);
		Charset charset = null;
		try
		{
			String charsetStr = ct.getParameter("charset");
			if(charsetStr != null)
				charset = Charset.forName(charsetStr);
		}
		catch(Exception e)
		{
			throw new RuntimeException("Could not convert: " + ct.toString()+ " ex="+ e.getMessage());
		}
		
		println("Part " + msgContent.getClass() + " type=" + ct.getBaseType());
		println("disposition " + disposition);
		println("charset " + charset);

		if (msgContent instanceof Multipart) {
			Multipart multipart = (Multipart) msgContent;
			// System.out.println("MultiPartContentType
			// "+multipart.getContentType());

			// Pfad anlegen
			String saveMesgPath = mesgContext.relavtivePath;
			mesgContext.relavtivePath = nameService.getFolder(mesgContext, ct);
			mesgContext.order.add(mesgContext.relavtivePath.substring(1));
			new File(mesgContext.mesgPath()).mkdir();
			
			for (int j = 0; j < multipart.getCount(); j++) {

				BodyPart bodyPart = multipart.getBodyPart(j);

				mesgContext.contentFilename = null;
				mesgContext.contentId = null;

				
				
				// has Filename?
				String filename = bodyPart.getFileName();
				if (filename != null) {
					String decodedFilename = MimeUtility.decodeText(filename);
					mesgContext.contentFilename = nameService.parseValidFilename(decodedFilename,new ContentType(bodyPart.getContentType()));
				}

				String[] contentID = bodyPart.getHeader("Content-ID");
				if (contentID != null) {
					mesgContext.contentId = nameService.parseContentId(contentID[0].trim());
				}
				parseContent(mesgContext, bodyPart.getContent(), bodyPart.getContentType(), bodyPart.getDisposition());
			}
			
			
			// Ruecksetzen
			mesgContext.relavtivePath = saveMesgPath;

		} else {

			if (msgContent instanceof String) {
				String str = (String) msgContent;
				
				if(charset == null)
					charset = StandardCharsets.UTF_8;
				
				save(mesgContext, new ByteArrayInputStream(str.getBytes(charset)), ct, charset);
			} else if (msgContent instanceof InputStream) {
				// BASE64DecoderStream
				// QPDecoderStream
				// SharedByteArrayInputStream
				InputStream is = (InputStream) msgContent;
				save(mesgContext, is, ct, charset);
			} else if (msgContent instanceof MimeMessage) {
				MimeMessage mimeMessage = (MimeMessage) msgContent;
				save(mesgContext, mimeMessage.getRawInputStream(), new ContentType("message/rfc822"), charset);
			} else {
				throw new RuntimeException("Not implemented " + msgContent);
			}
		}

	}

	private void save(MessageContext mesgContext, InputStream in, ContentType contentType, Charset charset) throws IOException {

		if (mesgContext.contentFilename == null)
			mesgContext.contentFilename = nameService.getFilename(mesgContext, contentType);

		println("fileName " + mesgContext.contentFilename);

		FileOutputStream out = new FileOutputStream(mesgContext.mesgPath() + "/" + mesgContext.contentFilename);
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len != -1) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}

		out.close();
		
		String relpath = new String(mesgContext.relavtivePath + "/" + mesgContext.contentFilename).substring(1);
		mesgContext.order.add(relpath);
		
		if(charset != null)
			mesgContext.contentEncoding.put(relpath,charset.name());
		if(mesgContext.contentId != null)
		{
			//create mapping
			mesgContext.contentIdMapping.put(mesgContext.contentId,relpath);
		}
	}

	

}
