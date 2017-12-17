package de.frittenburger.email2pdfa.interfaces;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface CheckSumService {

	String calculateMD5(String file) throws IOException, NoSuchAlgorithmException;

}
