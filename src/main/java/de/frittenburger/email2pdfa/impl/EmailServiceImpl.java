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

import java.util.List;

import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.interfaces.EmailCache;
import de.frittenburger.email2pdfa.interfaces.EmailService;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class EmailServiceImpl implements EmailService {

	private int rangefrom = -1;
	private final EmailCache emailCache;
	private static int RANGE = 10;

	public EmailServiceImpl(EmailCache emailCache) {
		this.emailCache = emailCache;
	}

	public int getMessages(EmailServiceAccountData emailServiceAccountData, Sandbox sandbox) {

		EmailBoxReader reader = new EmailBoxReader(emailServiceAccountData);

		if (rangefrom == 1)
			return 0; // ready

		try {
			
			int readMessages = 0;

			reader.open();

			if (rangefrom == -1) {
				int cnt = reader.cnt();
				System.out.println(cnt + "Messages");
				rangefrom = cnt - RANGE;
			} else {
				rangefrom = rangefrom - RANGE - 1;

			}
			int rangeto = rangefrom + RANGE;

			if (rangefrom < 1)
				rangefrom = 1;

			System.out.println("Read Range " + rangefrom + " => " + rangeto);

			List<String> msglst = reader.list(rangefrom, rangeto);

			
			
			for (String msgid : msglst) {
			
				if(emailCache.exists(msgid)) continue;
				String path = sandbox.getInBoxPath() + "/"+msgid+".eml";
			
				emailCache.add(msgid,msgid+".eml");
				
				reader.read(msgid,path);
				readMessages++;
			}

			reader.close();
			
			return readMessages;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

	}

}
