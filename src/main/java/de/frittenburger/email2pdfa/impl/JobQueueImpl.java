package de.frittenburger.email2pdfa.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.bo.MimeMessageParserException;
import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;
import de.frittenburger.email2pdfa.interfaces.Configuration;
import de.frittenburger.email2pdfa.interfaces.ContentConverter;
import de.frittenburger.email2pdfa.interfaces.EmailCache;
import de.frittenburger.email2pdfa.interfaces.EmailService;
import de.frittenburger.email2pdfa.interfaces.JobQueue;
import de.frittenburger.email2pdfa.interfaces.MimeMessageParser;
import de.frittenburger.email2pdfa.interfaces.PDFACreator;
import de.frittenburger.email2pdfa.interfaces.PDFASigner;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class JobQueueImpl implements JobQueue {

	// Poll pop3 => in
	private EmailCache emailCache = new EmailCacheImpl();

	private EmailService service = new EmailServiceImpl(emailCache);

	// Email parsing in => message
	private List<String> emailFiles = new ArrayList<String>();
	private MimeMessageParser mimeMessageParser = new MimeMessageParserImpl(emailCache);

	// Converter message => content
	private List<String> messageFolders = new ArrayList<String>();
	private ContentConverter contentConverter = new ContentConverterImpl();

	// PDF Creator content => archiv
	private List<String> contentFolders = new ArrayList<String>();
	private PDFACreator pdfCreator = new PDFACreatorImpl();

	// Sign Pdf's
	private List<String> pdfFolders = new ArrayList<String>();
	private PDFASigner pdfSigner = new PDFASignerImpl();

	private Configuration config;
	private Sandbox sandbox;

	public JobQueueImpl(Configuration config) {
		this.config = config;
		this.sandbox = config.getSandbox();
	}

	@Override
	public void resolvePollJobs()  {
		try {
			emailCache.init(config.getEmailCacheFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void runPollJobs() {
		EmailServiceAccountData emailServiceAccountData = config.getEmailServiceAccountData();
		
		while (true) {
			int r = service.getMessages(emailServiceAccountData, sandbox);
			if (r > 0)
				continue;
			// if(r < 0) error
			break;
		}
	}

	private final static FilenameFilter emlFileFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".eml");
		}
	};

	@Override
	public void resolveParserJobs() {

		emailFiles.clear();

		
		File files[] = new File(sandbox.getInBoxPath()).listFiles(emlFileFilter);
		for (int i = 0; i < files.length; i++) {

			try {
				String targetDir = mimeMessageParser.getTargetDir(files[i].getPath(), sandbox);
				printStatus("resolve parser jobs", i, files.length, targetDir);

				// TODO: Validate target
				if (new File(targetDir).exists())
					continue;

				emailFiles.add(files[i].getPath());

			} catch (MimeMessageParserException e) {
				e.printStackTrace();
				System.err.println(files[i].getPath());
			}
		}

	}

	@Override
	public void runParserJobs() {

		for (int x = 0;x < emailFiles.size();x++ ) {
			try {
				String emlFile = emailFiles.get(x);
				printStatus("run parser jobs", x, emailFiles.size(), emlFile);
				mimeMessageParser.parse(emlFile, sandbox);
				
			} catch (MimeMessageParserException e) {
				e.printStackTrace();
				System.err.println(emailFiles.get(x));

			}
		}

	}

	private final static FileFilter directoryFilter = new FileFilter() {

		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	};

	@Override
	public void resolveConvertJobs() {
		messageFolders.clear();

		File folders[] = new File(sandbox.getMessagePath()).listFiles(directoryFilter);

		for (int i = 0; i < folders.length; i++) {

			String targetDir = contentConverter.getTargetDir(folders[i].getPath(), sandbox);
			printStatus("resolve convert jobs", i, folders.length, new File(targetDir).getName());

			// TODO: Validate target , test content/xxx/emailheader.json (siehe
			// UnitTests)
			if (new File(targetDir).exists())
				continue;

			messageFolders.add(folders[i].getPath());
		}

	}

	@Override
	public void runConvertJobs() {
		
		
		for (int x = 0; x < messageFolders.size();x++) {
			try {
				String messageFolder =  messageFolders.get(x);
				printStatus("run convert jobs", x, messageFolders.size(), messageFolder);
				contentConverter.convert(messageFolder, sandbox);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(messageFolders.get(x));
			}
		}
	}

	@Override
	public void resolveCreateJobs() {
		contentFolders.clear();

		File[] folders = new File(sandbox.getContentPath()).listFiles(directoryFilter);

		for (int i = 0; i < folders.length; i++) {

			try {
				String targetPdf = pdfCreator.getTargetPdf(folders[i].getPath(), sandbox);
				printStatus("resolve create jobs", i, folders.length, new File(targetPdf).getName());

				if (new File(targetPdf).exists())
					continue; // Auf Datei pruefen, da im Folder mehrere PDF's
								// liegen

				contentFolders.add(folders[i].getPath());
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(folders[i].getPath());
			}

		}
	}

	@Override
	public void runCreateJobs() {
		for (int x = 0;x < contentFolders.size();x++) {
			try {
				printStatus("run create jobs", x, contentFolders.size(), contentFolders.get(x));
				pdfCreator.convert(contentFolders.get(x), sandbox);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(contentFolders.get(x));

			}
		}

	}

	@Override
	public void resolveSignJobs() {
		pdfFolders.clear();

		File[] folders = new File(sandbox.getPdfPath()).listFiles(directoryFilter);

		for (int i = 0; i < folders.length; i++) {
			printStatus("create sign jobs", i, folders.length, folders[1].getName());
			if (pdfSigner.getUnsignedPdfs(folders[1].getName(), sandbox).size() == 0)
				continue;
			pdfFolders.add(folders[i].getName());
		}

	}

	@Override
	public void runSignJobs() {

		PdfCreatorSignatureData pdfCreatorSignatureData = config.getPdfCreatorSignatureData();

		for (int x = 0;x < pdfFolders.size();x++) {
			try {
				printStatus("run sign jobs", x, pdfFolders.size(), pdfFolders.get(x));

				pdfSigner.sign(pdfCreatorSignatureData, pdfFolders.get(x), sandbox);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(pdfFolders.get(x));

			} catch (GeneralSecurityException e) {
				e.printStackTrace();
				System.err.println(pdfFolders.get(x));
			}
		}
	}

	
	private int current = -1;
	private void printStatus(String process, int i, int max, String target) {		
		int percent = (((i + 1) * 100) / (max * 2));
		
		
		if(current == -1)
		{
			System.out.printf("%-25s",process);
			current = 0;
		}
		
		
		 for(;current < percent;current++) System.out.print("#");
		
		if((i + 1) == max)
		{
			System.out.println(" done");
			current = -1;
		}
	}

	@Override
	public String toString() {
		return "JobQueueImpl [emailFiles=" + emailFiles.size() + " messageFolders=" + messageFolders.size()
				+ " contentFolders=" + contentFolders.size() + " pdfFolders=" + pdfFolders.size() + "]";
	}

}
