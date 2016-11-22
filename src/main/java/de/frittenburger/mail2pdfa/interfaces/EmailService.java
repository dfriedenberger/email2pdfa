package de.frittenburger.mail2pdfa.interfaces;

import de.frittenburger.mail2pdfa.bo.EmailServiceAccountData;

public interface EmailService {

	void getMessages(EmailServiceAccountData emailServiceAccountData, String path);

}
