package de.frittenburger.email2pdfa.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.frittenburger.email2pdfa.interfaces.EmailCache;

public class EmailCacheImpl implements EmailCache {

	private Set<String> messagekeys = new HashSet<String>();
	private Map<String,String> messagefiles = new HashMap<String,String>();
	private String emailCacheFile = null;

	@Override
	public void init(String emailCacheFile) throws IOException {
		this.emailCacheFile = emailCacheFile;
		messagekeys.clear();
		messagefiles.clear();
		
		if(new File(emailCacheFile).exists())
		{
			List<String> lines=Files.readAllLines(Paths.get(emailCacheFile), Charset.forName("UTF-8"));
			for(String line:lines){
			 
				String p[] = line.split(" ");
				if(p.length != 2)
					throw new IOException("invalid cache file "+line);
				messagekeys.add(p[0]);
				messagefiles.put(p[1], p[0]);
			}
		}
		
	}

	@Override
	public String getMesgKey(String emlFile) {
		return messagefiles.get(emlFile);
	}

	@Override
	public boolean exists(String msgkey) {
		return messagekeys.contains(msgkey);
	}

	@Override
	public void add(String msgkey, String emlFile) {

		messagekeys.add(msgkey);
		messagefiles.put(emlFile,msgkey);
		
		try {
			String line = String.format("%s %s\r\n", msgkey,emlFile);
			
		    Files.write(Paths.get(emailCacheFile), line.getBytes("UTF-8"), new File(emailCacheFile).exists() ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
		} catch (IOException e) {
		   e.printStackTrace();
		}
		
	}

}
