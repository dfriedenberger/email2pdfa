package de.frittenburger.mail2pdfa.impl;

import java.util.List;

import de.frittenburger.mail2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.mail2pdfa.interfaces.EmailService;

public class EmailServiceImpl implements EmailService {

	private int rangefrom = -1;
	private static int RANGE = 10;

	public void getMessages(EmailServiceAccountData emailServiceAccountData, String path) {

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

				reader.read(msgid, path + "/"+msgid+".eml");
				// save Messages and Attachments

			}

			reader.close();

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}

}
