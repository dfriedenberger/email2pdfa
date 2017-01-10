package de.frittenburger.email2pdfa.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;
import de.frittenburger.email2pdfa.interfaces.Configuration;

public class ConfigurationImpl implements Configuration {

	private Map<String, EmailServiceAccountData> emailaccountdata = new HashMap<>();
	private Map<String, PdfCreatorSignatureData> signaturedata = new HashMap<>();

	public ConfigurationImpl(String path) throws IOException {

		File config[] = new File(path).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".properties");
			}
		});

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

			String name = c.getName().replaceFirst("[.][^.]+$", "");

			if (properties.containsKey("provider")) {
				EmailServiceAccountData emailServiceAccountData = new EmailServiceAccountData();
				emailServiceAccountData.provider = properties.getProperty("provider");
				emailServiceAccountData.mailserver = properties.getProperty("mailserver");
				emailServiceAccountData.username = properties.getProperty("username");
				emailServiceAccountData.password = properties.getProperty("password");
				emailaccountdata.put("name", emailServiceAccountData);
			} else if (properties.containsKey("keystorepath")) {
				PdfCreatorSignatureData pdfCreatorSignatureData = new PdfCreatorSignatureData();
				pdfCreatorSignatureData.keyStorePath = properties.getProperty("keystorepath");
				pdfCreatorSignatureData.keyStorePassword = properties.getProperty("keystorepassword");
				pdfCreatorSignatureData.privateKeyPassword = properties.getProperty("privatekeypassword");
				pdfCreatorSignatureData.location = properties.getProperty("location");
				pdfCreatorSignatureData.reason = properties.getProperty("reason");
				signaturedata.put(name, pdfCreatorSignatureData);
			} else {
				throw new IOException("Unknown properties type " + c.getName());
			}
		}
	}

	@Override
	public Set<String> getEmailServiceAccountDataKeys() {
		return emailaccountdata.keySet();
	}

	@Override
	public EmailServiceAccountData getEmailServiceAccountData(String key) {
		return emailaccountdata.get(key);
	}

	@Override
	public Set<String> getPdfCreatorSignatureDataKeys() {
		return signaturedata.keySet();
	}

	@Override
	public PdfCreatorSignatureData getPdfCreatorSignatureData(String key) {
		return signaturedata.get(key);
	}

}
