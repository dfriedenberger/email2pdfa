package de.frittenburger.email2pdfa.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.bo.MimeMessageParserException;
import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;
import de.frittenburger.email2pdfa.interfaces.Configuration;
import de.frittenburger.email2pdfa.interfaces.ContentConverter;
import de.frittenburger.email2pdfa.interfaces.EmailService;
import de.frittenburger.email2pdfa.interfaces.JobQueue;
import de.frittenburger.email2pdfa.interfaces.MimeMessageParser;
import de.frittenburger.email2pdfa.interfaces.PDFACreator;
import de.frittenburger.email2pdfa.interfaces.PDFASigner;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class JobQueueImpl implements JobQueue {

	private Sandbox sandbox;

	//Poll pop3 => in
	private EmailService service = new EmailServiceImpl();

	
	//Email parsing in => message
	private List<String> emailFiles = null;
	private MimeMessageParser mimeMessageParser = new MimeMessageParserImpl();
	
    //Converter message => content
	private List<String> messageFolders = null;
	private ContentConverter contentConverter = new ContentConverterImpl();
	
    //PDF Creator content => archiv
	private List<String> contentFolders = null;
	private PDFACreator pdfCreator = new PDFACreatorImpl();

	//Sign Pdf's 
	private List<String> archivFolders = null;
	private PDFASigner pdfSigner = new PDFASignerImpl();
	
	private Configuration config;

	

	
	


	public JobQueueImpl(Sandbox sandbox, Configuration config) {
		this.sandbox = sandbox;
		this.config = config;
	}


	@Override
	public void resolvePollJobs() {
		//TODO: calculate jobs
	}


	@Override
	public void runPollJobs() {
		
		for(String key : config.getEmailServiceAccountDataKeys())
		{
			EmailServiceAccountData emailServiceAccountData = config.getEmailServiceAccountData(key);
			for(int i = 0;i < 100;i++)
			    service.getMessages(emailServiceAccountData, sandbox);
		}
		
	}


	@Override
	public void resolveParserJobs() {
		emailFiles = new ArrayList<String>();
		for (File f : new File(sandbox.getInBoxPath()).listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".eml");
			}
		})) 
		{
			
			try {
				String targetDir = mimeMessageParser.getTargetDir(f.getPath(), sandbox);
				System.out.println("Check "+new File(targetDir).getName());

				//TODO: Validate target
				if(new File(targetDir).exists()) continue;
				
				emailFiles.add(f.getPath());
				
			} catch (MimeMessageParserException e) {
				e.printStackTrace();
			}
		
		}		
	}
	
	@Override
	public void runParserJobs() {
		for (String emlFile : emailFiles) {
			try {
				mimeMessageParser.parse(emlFile, sandbox);
			} catch (MimeMessageParserException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	




	@Override
	public void resolveConvertJobs() {
		messageFolders = new ArrayList<String>();
		for (File folder : new File(sandbox.getMessagePath()).listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})) {
			
			String targetDir = contentConverter.getTargetDir(folder.getPath(), sandbox);
			System.out.println("Check "+new File(targetDir).getName());

			//TODO: Validate target , test content/xxx/emailheader.json (siehe UnitTests)
			if(new File(targetDir).exists()) continue;
			
			messageFolders.add(folder.getPath());
		}
		
	}


	@Override
	public void runConvertJobs() {
		for (String messageFolder : messageFolders) {
			try {
				contentConverter.convert(messageFolder, sandbox);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	



	@Override
	public void resolveCreateJobs() {
		contentFolders = new ArrayList<String>();
		
		File[] folders = new File(sandbox.getContentPath()).listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		
		System.out.println("Found  "+folders.length+ " folders");

		for (File folder : folders) {

			try {
				String targetPdf = pdfCreator.getTargetPdf(folder.getPath(), sandbox);
				System.out.println("Check "+new File(targetPdf).getName());
				
				if (new File(targetPdf).exists())
					continue; // Auf Datei pruefen, da im Folder mehrere PDF's
							// liegen

				contentFolders.add(folder.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}





	@Override
	public void runCreateJobs() {
		for (String contentFolder : contentFolders) {
			try {
				pdfCreator.convert(contentFolder, sandbox);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void resolveSignJobs() {
		archivFolders = new ArrayList<String>();
		for (File folder : new File(sandbox.getArchivPath()).listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})) {
			if(pdfSigner.getUnsignedPdfs(folder.getName(), sandbox).size() == 0)
				continue;
			archivFolders.add(folder.getName());
		}
		
	}


	@Override
	public void runSignJobs() {
		
		Set<String> keys = config.getPdfCreatorSignatureDataKeys();
		if(keys.size() != 1)
			throw new RuntimeException("select sign config " +  keys);
		PdfCreatorSignatureData pdfCreatorSignatureData = config.getPdfCreatorSignatureData(keys.iterator().next());
		
		
		for (String archivFolder : archivFolders) {
			try {
				pdfSigner.sign(pdfCreatorSignatureData, archivFolder , sandbox);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return "JobQueueImpl [emailFiles=" + emailFiles.size() + " messageFolders="+messageFolders.size() 
		+ " contentFolders="+contentFolders.size()+" archivFolders="+archivFolders.size()+"]";
	}









}
