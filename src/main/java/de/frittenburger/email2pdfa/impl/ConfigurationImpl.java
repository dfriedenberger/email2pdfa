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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;
import de.frittenburger.email2pdfa.interfaces.Configuration;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class ConfigurationImpl implements Configuration {

	private EmailServiceAccountData emailaccountdata = null;
	private PdfCreatorSignatureData signaturedata = null;
	private Sandbox sandbox = null;
	private String name = null;
	
	private ConfigurationImpl() {}
	
	
	public static List<Configuration> read(String path) throws IOException {

		File config[] = new File(path).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".properties");
			}
		});

		
		List<Configuration> configuration = new ArrayList<Configuration>();
		
		for (File c : config) {
			System.out.println("Read Properties " + c.getName());

			Properties properties = new Properties();
			FileInputStream is = null;
			try {
				is = new FileInputStream(c);
				properties.load(is);
			} finally {
				if (is != null)
					is.close();
			}

			ConfigurationImpl conf = new ConfigurationImpl();
			
			conf.name = c.getName().replaceFirst("[.][^.]+$", "");
			
			conf.sandbox = new SandboxImpl(properties.getProperty("sandboxpath").trim());
			
			
			conf.emailaccountdata = new EmailServiceAccountData();
			conf.emailaccountdata.provider = properties.getProperty("provider");
			conf.emailaccountdata.mailserver = properties.getProperty("mailserver");
			conf.emailaccountdata.username = properties.getProperty("username");
			conf.emailaccountdata.password = properties.getProperty("password");

			conf.signaturedata = new PdfCreatorSignatureData();
			conf.signaturedata.keyStorePath = properties.getProperty("keystorepath");
			conf.signaturedata.keyStorePassword = properties.getProperty("keystorepassword");
			conf.signaturedata.privateKeyPassword = properties.getProperty("privatekeypassword");
			conf.signaturedata.location = properties.getProperty("location");
			conf.signaturedata.reason = properties.getProperty("reason");
		
		
			configuration.add(conf);
			
		}
		return configuration;
	}


	@Override
	public EmailServiceAccountData getEmailServiceAccountData() {
		return emailaccountdata;
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


	@Override
	public String getEmailCacheFile() {
		return sandbox.getInBoxPath() + "/emailcache.txt";
	}

}
