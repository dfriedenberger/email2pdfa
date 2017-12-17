package de.frittenburger.email2pdfa.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import de.frittenburger.email2pdfa.bo.EmailDataFilePath;
import de.frittenburger.email2pdfa.bo.EmailHeader;
import de.frittenburger.email2pdfa.interfaces.CheckSumService;
import de.frittenburger.email2pdfa.interfaces.EmailArchivReaderService;
import de.frittenburger.email2pdfa.interfaces.EmailArchiveList;
import de.frittenburger.email2pdfa.interfaces.EmailCache;
import de.frittenburger.email2pdfa.interfaces.Logger;
import de.frittenburger.email2pdfa.interfaces.NameService;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class EmailArchivReaderServiceImpl implements EmailArchivReaderService {

	private final static String token = "From - "; //From - Mon Jan 04 16:27:14 2016
	private final Logger logger = new LoggerImpl(this.getClass().getSimpleName());
	private final EmailCache emailCache;
	private final EmailArchiveList archiveFileList;
	private final NameService nameService = new NameServiceImpl();
	private final CheckSumService cksumService = new CheckSumServiceImpl();

	public EmailArchivReaderServiceImpl(EmailCache emailCache,EmailArchiveList archiveFileList) {
		this.emailCache = emailCache;
		this.archiveFileList = archiveFileList;
	}

	
	@Override
	public List<String> searchArchivFile(EmailDataFilePath path) throws IOException {

		List<String> files = new ArrayList<String>();
		searchArchivFile(new File(path.path),files);
		return files;
		
	}
	
	@Override
	public boolean hasUnreadMessages(String aFile) throws IOException {
		try {
			String md5 = cksumService.calculateMD5(aFile);
			return !md5.equals(archiveFileList.getCkSum(aFile)); 

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void getMessages(String archivFile, Sandbox sandbox) throws IOException {
		logger.infoFormat("read %s", archivFile);	
		
	
		BufferedReader br = null;
		int header = -1;
		StringBuffer buffer = null;
		try
		{
			 br = new BufferedReader(new FileReader(archivFile));
			 while(true)
			 {
				 String line = br.readLine();
				 if(line == null || line.startsWith(token))
				 {
					 
					 if(buffer != null)
					 {
						 try
						 {
							 MimeMessage mime = readMime(buffer.subSequence(0, header));
							 EmailHeader emailheader = nameService.getEmailHeader(mime);
							 String msgid = emailheader.mesgkey;
							 //create Message
							 if (!emailCache.exists(msgid)) {
								    //System.out.println(emailheader.subject);
									String path = sandbox.getInBoxPath() + "/" + msgid + ".eml";
									saveBuffer(buffer,path);
									emailCache.add(msgid, msgid + ".eml");
								}
							 
						 }
						 catch(Exception e)
						 {
							 e.printStackTrace();
							 System.err.println(buffer.subSequence(0, header));
						 }
					 }
					 if(line == null) break;
					 buffer = new StringBuffer();
					 header = -1;
					 continue;
				 }
				 
				 
				 
				
				 buffer.append(line);
				 buffer.append("\r\n");
				 if(line.trim().equals("") && header == -1)
					 header = buffer.length();

				 
			 }
			 
			 

		} finally
		{
			br.close();
		}
		
		try {
			String md5 = cksumService.calculateMD5(archivFile);
			archiveFileList.putCkSum(archivFile,md5);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}

	private MimeMessage readMime(CharSequence charSequence) throws IOException, MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		InputStream is = null;
		MimeMessage mime = null;
		try {
			is = new ByteArrayInputStream(charSequence.toString().getBytes());
			mime = new MimeMessage(session, is);
			return mime;
		} finally {
			if (is != null)
				is.close();
			is = null;
		}
	}
	
	private void saveBuffer(StringBuffer buffer, String path) throws IOException {
		
		
		  BufferedWriter bwr = null;
		  
		  try
		  {
			  bwr = new BufferedWriter(new FileWriter(path));
		
          //write contents of StringBuffer to a file
          bwr.write(buffer.toString());
         
          //flush the stream
          bwr.flush();
		  } finally {
          //close the stream
          bwr.close();
		  }
	}


	private void searchArchivFile(File file, List<String> files) throws IOException {

		for(File f : file.listFiles())
		{

			if(f.isDirectory())
			{
				searchArchivFile(f,files);
			}
			else
			{
				//Check if Archiv File
				if(isArchivFile(f))
					files.add(f.getAbsolutePath());
			}
		}
		
	}




	private static boolean isArchivFile(File f) throws IOException {
		
		BufferedReader br = null;
		try
		{
			 br = new BufferedReader(new FileReader(f));
			 String text = br.readLine();
			 return text.startsWith(token);
		} finally
		{
			br.close();
		}
	
	}




	


	
	
}
