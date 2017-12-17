package de.frittenburger.email2pdfa.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;

import de.frittenburger.core.SecretProvider;

public class FileSecretProvider implements SecretProvider {

	File tempDir = new File("temp");
	File secretFile = new File(tempDir, "secret.txt");

	public FileSecretProvider()
	{
		if(!tempDir.exists()) tempDir.mkdir();
		if(!secretFile.exists())
		{
			byte[] b = new byte[500];
			new Random().nextBytes(b);
			try {
				Files.write(secretFile.toPath(), b);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
	@Override
	public byte[] get128BitSecret() {
		
		byte[] data;
		try {
			data = Files.readAllBytes(secretFile.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return Arrays.copyOf(data, 16);
	}

}
