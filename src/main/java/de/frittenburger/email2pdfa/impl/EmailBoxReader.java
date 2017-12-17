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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import de.frittenburger.email2pdfa.bo.EmailHeader;
import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.bo.Range;
import de.frittenburger.email2pdfa.interfaces.Logger;
import de.frittenburger.email2pdfa.interfaces.NameService;




public class EmailBoxReader  {

	Store store = null;
	private String provider;
	private String mailserver;
	private String username;
	private String password;
	private Map<String, Integer> cache = null;
	private NameService nameService = new NameServiceImpl();
	private Logger logger = new LoggerImpl(this.getClass().getSimpleName());

	public EmailBoxReader(EmailServiceAccountData config) {
		provider = config.provider;
		mailserver = config.mailserver;
		username = config.username;
		password = config.password;
	}

	public void open() throws IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		try {
			store = session.getStore(provider);
			cache = new HashMap<String, Integer>();
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		}
		try {
			store.connect(mailserver, username, password);
		} catch (MessagingException e) {
			store = null;
			throw new IOException(e);
		}

	}

	public void close() throws IOException {
		try {
			store.close();
		} catch (MessagingException e) {
			throw new IOException(e);
		} finally {
			store = null;
			cache = null;
		}
	}

	public int cnt(String folder) throws IOException {
		try {
			Folder inbox = store.getFolder(folder);

			inbox.open(Folder.READ_ONLY);
			int cnt = inbox.getMessageCount();
			inbox.close(false);
			return cnt;
		} catch (MessagingException e) {
			throw new IOException(e);
		}

	}

	
	public List<String> listFolders() throws IOException {
		try {
			List<String> folder = new ArrayList<String>();
			list(folder, store.getDefaultFolder());
			return folder;
		} catch (MessagingException e) {
			throw new IOException(e);
		}

	}

	private void list(List<String> folderList, Folder folder) throws MessagingException {
		
		for (Folder fd : folder.list()) {
			folderList.add(fd.getFullName());
			if(provider.equals("pop3")) continue;
			list(folderList, fd);
		}
	}
	
	public String listMessage(String folder, int ix) throws IOException {
		
		try {
			Folder inbox = store.getFolder(folder);
	
			inbox.open(Folder.READ_ONLY);
			
			javax.mail.Message message = inbox.getMessage(ix);
			EmailHeader emailheader = nameService.getEmailHeader(message);
			return emailheader.mesgkey;
		
		} catch (Exception e) {
			
			if(e instanceof MessagingException)
			{
				logger.errorFormat("Invalid email (%s) in folder=%s index=%d",e.getMessage(),folder ,ix);
				return null;
			}
			else
			{
				e.printStackTrace();
			}
			throw new IOException(e);

		}
		
	}

	public String[] listMessages(String folder,Range range) throws IOException {
	
		String[] mesgids = new String[range.to - range.from + 1];
		try {

			Folder inbox = store.getFolder(folder);

			inbox.open(Folder.READ_ONLY);
			
			javax.mail.Message[] message = inbox.getMessages(range.from,range.to);
			for (int i = 0; i < message.length; i++) {
				mesgids[i] = null;
				try {

					EmailHeader emailheader = nameService.getEmailHeader(message[i]);

					cache.put(emailheader.mesgkey, range.from + i);
					mesgids[i] = emailheader.mesgkey;
					
				} catch (Exception e) {
					
					if(e instanceof MessagingException)
					{
						message[i].writeTo(new FileOutputStream(new File("c:/temp/mailerror.eml")));
						logger.errorFormat("Invalid email (%s) in folder=%s index=%d",e.getMessage(),folder ,range.from + i);
					}
					else
						e.printStackTrace();
					
				} 
			}

			inbox.close(false);

		} catch (MessagingException e) {
			throw new IOException(e);
		}
		return mesgids;
	}

	public void read(String folder,String id,String filename) throws IOException {
		
		try {

			Folder inbox = store.getFolder(folder);

			inbox.open(Folder.READ_ONLY);
			
			int ix = cache.get(id).intValue();
			
			javax.mail.Message mail = inbox.getMessage(ix);
			
			//MetaData Lesen
			System.out.println("Subject " + mail.getSubject());
			
			//Content lesen
			FileOutputStream os = new FileOutputStream(filename);
			mail.writeTo(os);
			os.flush();
			

			inbox.close(false);

		} catch (MessagingException e) {
			throw new IOException(e);
		}
	}

	public void delete(int ix) throws IOException {
		// TODO Auto-generated method stub
		
	}

	
	

	
}
