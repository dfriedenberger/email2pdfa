package de.frittenburger.email2pdfa;
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;


import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.bo.MimeMessageParserException;
import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;
import de.frittenburger.email2pdfa.impl.ContentConverterImpl;
import de.frittenburger.email2pdfa.impl.EmailServiceImpl;
import de.frittenburger.email2pdfa.impl.MimeMessageParserImpl;
import de.frittenburger.email2pdfa.impl.PDFACreatorImpl;
import de.frittenburger.email2pdfa.impl.SandboxImpl;
import de.frittenburger.email2pdfa.interfaces.ContentConverter;
import de.frittenburger.email2pdfa.interfaces.EmailService;
import de.frittenburger.email2pdfa.interfaces.MimeMessageParser;
import de.frittenburger.email2pdfa.interfaces.PDFACreator;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class Converter {

	public static void main(String[] args) throws Exception {

		// Create SandBox 
		Sandbox sandbox = new SandboxImpl();
		sandbox.init();
		
		if(args.length == 0)
		{
			printHelp();
			System.exit(0);
		}
		
		for(int i = 0;i < args.length;i++)
		{
			if(args[i].toLowerCase().startsWith("poll"))
			{
				//Download emails 
				String config = String.format("config/%s.properties",args[++i]);
				getMessages(config,sandbox);
			}
			else if(args[i].toLowerCase().startsWith("parse"))
			{
				parseMessages(sandbox);
			}	
			else if(args[i].toLowerCase().startsWith("convert"))
			{
				convertMessages(sandbox);
			}	
			else if(args[i].toLowerCase().startsWith("create"))
			{
				String config = String.format("config/%s.properties",args[++i]);
				createPdfs(config,sandbox);
			}	
			else
			{
				System.out.println("Unknown Argument "+args[i]);
				printHelp();
				System.exit(0);				
			}

		}	

	}

	

	private static void printHelp() {
		System.out.println("use following Arguments");
		System.out.println("poll [box] - for polling emails");
		System.out.println("parse - parse and extract emails");
		System.out.println("convert - create screenshots from html parts");
		System.out.println("create [sign] - create pdf/a files and sign them");
	}



	private static void getMessages(String propertyFile,Sandbox sandbox) throws IOException {
		EmailServiceAccountData emailServiceAccountData = new EmailServiceAccountData();

		Properties properties = new Properties();
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(propertyFile);
			properties.load(is);
		}
		finally {
			if(is != null)
				is.close();
		}
		
		emailServiceAccountData.provider = properties.getProperty("provider");
		emailServiceAccountData.mailserver = properties.getProperty("mailserver");
		emailServiceAccountData.username = properties.getProperty("username");
		emailServiceAccountData.password = properties.getProperty("password");

		EmailService service = new EmailServiceImpl();
		for(int i = 0;i < 100;i++)
		    service.getMessages(emailServiceAccountData, sandbox);

	}

	private static void parseMessages(Sandbox sandbox)  
			 {
		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl();
		for (File f : new File(sandbox.getInBoxPath()).listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".eml");
			}
		})) {
			try
			{
			mimeMessageParser.parse(f.getPath(), sandbox);
			}
			catch(MimeMessageParserException e)
			{
				e.printStackTrace();
			}
		}
	}

	private static void convertMessages(Sandbox sandbox) throws IOException {
		ContentConverter contentConverter = new ContentConverterImpl();

		for (File folder : new File(sandbox.getMessagePath()).listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})) {
				contentConverter.convert(folder.getPath(),sandbox);
		}

	}

	
	private static void createPdfs(String propertyFile, Sandbox sandbox) throws IOException, GeneralSecurityException {
		
		PdfCreatorSignatureData pdfCreatorSignatureData = new PdfCreatorSignatureData();
		
		Properties properties = new Properties();
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(propertyFile);
			properties.load(is);
		}
		finally {
			if(is != null)
				is.close();
		}
		
		pdfCreatorSignatureData.keyStorePath = properties.getProperty("keystorepath");
		pdfCreatorSignatureData.keyStorePassword = properties.getProperty("keystorepassword");
		pdfCreatorSignatureData.privateKeyPassword = properties.getProperty("privatekeypassword");

		PDFACreator pdfCreator = new PDFACreatorImpl();
		for (File folder : new File(sandbox.getContentPath()).listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})) {
			System.out.println("Convert " + folder.getPath());
			pdfCreator.convert(pdfCreatorSignatureData, folder.getPath(), sandbox);
		}
		
	}
}
