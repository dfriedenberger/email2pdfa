package de.frittenburger.email2pdfa.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.frittenburger.email2pdfa.interfaces.CheckSumService;

public class CheckSumServiceImpl implements CheckSumService {

	@Override
	public String calculateMD5(String file) throws IOException, NoSuchAlgorithmException {
		
		 InputStream fis =  new FileInputStream(file);

	       byte[] buffer = new byte[1024];
	       MessageDigest complete = MessageDigest.getInstance("MD5");
	       int numRead;

	       do {
	           numRead = fis.read(buffer);
	           if (numRead > 0) {
	               complete.update(buffer, 0, numRead);
	           }
	       } while (numRead != -1);

	       fis.close();
	       byte b[] = complete.digest();

		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
		
	}

}
