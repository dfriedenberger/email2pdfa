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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.frittenburger.email2pdfa.bo.FolderWrapper;
import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.bo.Range;
import de.frittenburger.email2pdfa.interfaces.EmailCache;
import de.frittenburger.email2pdfa.interfaces.EmailIndex;
import de.frittenburger.email2pdfa.interfaces.EmailIndexSyncService;
import de.frittenburger.email2pdfa.interfaces.EmailService;
import de.frittenburger.email2pdfa.interfaces.Logger;
import de.frittenburger.email2pdfa.interfaces.Sandbox;
import de.frittenburger.email2pdfa.interfaces.Sequence;

public class EmailServiceImpl implements EmailService {

	private final EmailCache emailCache;
	private final EmailIndex emailIndex;


	private final Logger logger = new LoggerImpl(this.getClass().getSimpleName());

	private EmailIndexSyncService emailIndexSyncService = new EmailIndexSyncServiceImpl();
	
	private EmailBoxReader reader = null;
	private String storeKey = null;


	private String folder = null;
	private Sequence sequence = null;

	public EmailServiceImpl(EmailCache emailCache, EmailIndex emailIndex) {
		this.emailCache = emailCache;
		this.emailIndex = emailIndex;
	}

	@Override
	public void open(EmailServiceAccountData emailServiceAccountData) throws IOException {
		reader = new EmailBoxReader(emailServiceAccountData);
		storeKey = String.format("%s/%s", emailServiceAccountData.mailserver,emailServiceAccountData.username);
		reader.open();
	}

	@Override
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		reader = null;
	}

	@Override
	public void openFolder(String folder,Sequence sequence) throws IOException {
		this.folder = folder;
		this.sequence = sequence;
	}

	@Override
	public List<String> getFolders() throws IOException {
		List<String> folderList = new ArrayList<String>();
		
		for(String fd : reader.listFolders())
		{
			//Todo: Filter konfigurierbar
			if(!fd.toLowerCase().startsWith("inbox")) 
			{
				System.out.println("Filter "+fd);
				continue;
			}
			folderList.add(fd);
		}
		return folderList;
		
	}

	@Override
	public Sequence getUnreadMessages(String folder) throws IOException {
		
		int cnt = reader.cnt(folder);
		return emailIndexSyncService.sync(emailIndex, storeKey, new FolderWrapper(reader,folder,cnt));
		
	}
	
	
	@Override
	public boolean getMessages(Sandbox sandbox) {

		if (!sequence.hasNext())
			return true; // ready

		Range range = sequence.next();

		logger.info("Read Range " + range);

		try {

			String[] msglst = reader.listMessages(folder, range);

			for (int i = 0; i < msglst.length; i++) {
				String msgid = msglst[i];
				int index = range.from + i;

				if(msgid == null) 
				{
					emailIndex.registerError(storeKey, folder, index);
					continue;
				}
					
				// Download Message ???
				if (!emailCache.exists(msgid)) {
					String path = sandbox.getInBoxPath() + "/" + msgid + ".eml";
					reader.read(folder, msgid, path);
					emailCache.add(msgid, msgid + ".eml");
				}
				emailIndex.register(storeKey, folder, msgid, index);

			}

		} catch (IOException e) {
			logger.error(e);
		}
		return false;

	}

	
}
