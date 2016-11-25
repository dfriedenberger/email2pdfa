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
import java.util.List;

import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.interfaces.EmailService;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class EmailServiceImpl implements EmailService {

	private int rangefrom = -1;
	private static int RANGE = 10;

	public void getMessages(EmailServiceAccountData emailServiceAccountData, Sandbox sandbox) {

		EmailBoxReader reader = new EmailBoxReader(emailServiceAccountData);

		if (rangefrom == 1)
			return; // ready

		try {

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
				String path = sandbox.getInBoxPath() + "/"+msgid+".eml";
				if(new File(path).exists()) continue;
				reader.read(msgid,path);
			}

			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}

}
