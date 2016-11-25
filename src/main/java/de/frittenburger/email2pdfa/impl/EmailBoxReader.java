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
import de.frittenburger.email2pdfa.interfaces.NameService;




public class EmailBoxReader  {

	Store store = null;
	private String provider;
	private String mailserver;
	private String username;
	private String password;
	private Map<String, Integer> cache = null;
	private NameService nameService = new NameServiceImpl();
	
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

	public int cnt() throws IOException {
		try {
			Folder inbox = store.getFolder("Inbox");

			inbox.open(Folder.READ_ONLY);
			int cnt = inbox.getMessageCount();
			inbox.close(false);
			return cnt;
		} catch (MessagingException e) {
			throw new IOException(e);
		}

	}

	public List<String> list(int from, int to) throws IOException {
	
		List<String> mesgids = new ArrayList<String>();
		try {

			Folder inbox = store.getFolder("Inbox");

			inbox.open(Folder.READ_ONLY);
			
			javax.mail.Message[] message = inbox.getMessages(from,to);
			for (int i = 0; i < message.length; i++) {

				try {

					EmailHeader emailheader = nameService.getEmailHeader(message[i]);

					cache.put(emailheader.mesgkey, from + i);
					mesgids.add(emailheader.mesgkey);
					
				} catch (Exception e) {
					e.printStackTrace();
					// TODO: save to tmp-File
				} 
			}

			inbox.close(false);

		} catch (MessagingException e) {
			throw new IOException(e);
		}
		return mesgids;
	}

	public void read(String id,String filename) throws IOException {
		
		try {

			Folder inbox = store.getFolder("Inbox");

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
