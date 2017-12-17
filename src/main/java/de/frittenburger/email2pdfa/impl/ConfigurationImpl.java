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
import java.util.Map.Entry;

import de.frittenburger.bo.AdminPanelException;
import de.frittenburger.core.Database;
import de.frittenburger.email2pdfa.bo.EmailDataFilePath;
import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.bo.GlobalConfig;
import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;
import de.frittenburger.email2pdfa.interfaces.Configuration;
import de.frittenburger.email2pdfa.interfaces.Sandbox;
import de.frittenburger.form.DataDirectory;
import de.frittenburger.form.EmailAccount;
import de.frittenburger.form.Signature;


public class ConfigurationImpl implements Configuration {

	private List<EmailServiceAccountData> emailaccountdata = new ArrayList<EmailServiceAccountData>();
	private List<EmailDataFilePath> emaildatafiles = new ArrayList<EmailDataFilePath>();

	private PdfCreatorSignatureData signaturedata = null;
	private Sandbox sandbox = null;
	private String name = null;
	
	private ConfigurationImpl() {}
	
	
	public static Configuration read() throws IOException, AdminPanelException {

		
		Database database = new Database();

		
	    List<Entry<String, GlobalConfig>> globals = database.getForms(GlobalConfig.class);
	    Entry<String, GlobalConfig> ge = globals.iterator().next();
	    String key = ge.getKey();
	    GlobalConfig global = ge.getValue();

	    ConfigurationImpl conf = new ConfigurationImpl();
		conf.name = key;
		conf.sandbox = new SandboxImpl(global.path.getValue());
	    
		
		List<Entry<String, EmailAccount>> accounts = database.getForms(EmailAccount.class);
		for (Entry<String, EmailAccount> e : accounts) {
		
			EmailAccount account = e.getValue();
			EmailServiceAccountData emailaccountdata = new EmailServiceAccountData();
			emailaccountdata.name = e.getKey();
			emailaccountdata.provider = account.provider.getValue();
			emailaccountdata.mailserver = account.server.getValue();
			emailaccountdata.username = account.username.getValue();
			emailaccountdata.password = account.password.getValue();
			
			conf.emailaccountdata.add(emailaccountdata);
		}
		
		List<Entry<String, DataDirectory>> directories = database.getForms(DataDirectory.class);
		for (Entry<String, DataDirectory> e : directories) {
		
			DataDirectory account = e.getValue();
			EmailDataFilePath emaildatafilepath = new EmailDataFilePath();
			emaildatafilepath.name = e.getKey();
			emaildatafilepath.path = account.directory.getValue();
			conf.emaildatafiles.add(emaildatafilepath);
		}
		
		if (database.countForm(Signature.class) > 0) {
			Signature signature = database.getForms(Signature.class).iterator().next().getValue();
			conf.signaturedata = new PdfCreatorSignatureData();
			conf.signaturedata.keyStorePath = signature.keystorepath.getValue();
			conf.signaturedata.keyStorePassword = signature.keystorepassword.getValue();
			conf.signaturedata.privateKeyPassword = signature.privatekeypassword.getValue();
			conf.signaturedata.location = signature.location.getValue();
			conf.signaturedata.reason = signature.reason.getValue();
		}
		
		
		return conf;
	}


	@Override
	public List<EmailServiceAccountData> getEmailServiceAccountData() {
		return emailaccountdata;
	}
	
	@Override
	public List<EmailDataFilePath> getEmailDataFilePathes() {
		return emaildatafiles;
	}
	
	@Override
	public PdfCreatorSignatureData getPdfCreatorSignatureData() {
		return signaturedata;
	}

	@Override
	public Sandbox getSandbox() {
		return sandbox;
	}


	@Override
	public String getName() {
		return name;
	}

}
