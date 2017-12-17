package de.frittenburger.email2pdfa.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import de.frittenburger.email2pdfa.bo.EmailDataFilePath;
import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;
import de.frittenburger.email2pdfa.bo.MimeMessageParserException;
import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;
import de.frittenburger.email2pdfa.bo.PollJob;
import de.frittenburger.email2pdfa.interfaces.Configuration;
import de.frittenburger.email2pdfa.interfaces.ContentConverter;
import de.frittenburger.email2pdfa.interfaces.EmailArchivReaderService;
import de.frittenburger.email2pdfa.interfaces.EmailArchiveList;
import de.frittenburger.email2pdfa.interfaces.EmailCache;
import de.frittenburger.email2pdfa.interfaces.EmailIndex;
import de.frittenburger.email2pdfa.interfaces.EmailService;
import de.frittenburger.email2pdfa.interfaces.JobQueue;
import de.frittenburger.email2pdfa.interfaces.Logger;
import de.frittenburger.email2pdfa.interfaces.MimeMessageParser;
import de.frittenburger.email2pdfa.interfaces.PDFACreator;
import de.frittenburger.email2pdfa.interfaces.PDFASigner;
import de.frittenburger.email2pdfa.interfaces.Sandbox;
import de.frittenburger.email2pdfa.interfaces.Sequence;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class JobQueueImpl implements JobQueue {

	// Poll pop3 => in
	private EmailCache emailCache = new EmailCacheImpl();
	private EmailIndex emailIndex = new EmailIndexImpl();
	private EmailArchiveList filelist = new EmailArchiveListImpl();
	private EmailService service = new EmailServiceImpl(emailCache,emailIndex);
	private List<PollJob>  pollJobs = new ArrayList<PollJob>();
	
	
	//Read Archiv nach in
	private EmailArchivReaderService emailArchivReaderService = new EmailArchivReaderServiceImpl(emailCache,filelist);
	private List<String> archivFiles = new ArrayList<String>();

	
	
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

	private final Logger logger = new LoggerImpl(this.getClass().getSimpleName());

	public JobQueueImpl(Configuration config) throws IOException {
		this.config = config;
		this.sandbox = config.getSandbox();
		emailCache.init(sandbox.getInBoxCachePath()+"/emailcache.txt");
		emailIndex.init(sandbox.getInBoxCachePath());
		filelist.init(sandbox.getInBoxCachePath()+"/emailarchivfiles.txt");
	}

	private void resolvePollJobs() {
		
		int read = 0;
		int sum = 0;
		pollJobs.clear();
		for(EmailServiceAccountData emailServiceAccountData : config.getEmailServiceAccountData())
		{
			try
			{
				service.open(emailServiceAccountData);
				List<String> folder = service.getFolders();
				int i = 0;
				for(String fd : folder)
				{
					Sequence seq = service.getUnreadMessages(fd);
					//System.out.println(fd+" "+seq);
					read += seq.offset();
					sum  += seq.range();
					
					if(seq.hasNext())
						pollJobs.add(new PollJob(emailServiceAccountData.name,fd,seq));
					
					printStatus("resolve "+emailServiceAccountData.username, i++, folder.size());

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally
			{
				service.close();
			}
		}
		logger.infoFormat("mails=%d read=%d",sum,read);
	}

	private void runPollJobs() {
		
		for(EmailServiceAccountData emailServiceAccountData : config.getEmailServiceAccountData())
		{
			try
			{
				service.open(emailServiceAccountData);
				for(PollJob job : pollJobs)
				{
					if(!job.getAccount().equals(emailServiceAccountData.name)) continue;
					
					logger.info(emailServiceAccountData.name+" "+job.getFolder()+" "+job.getSequence());
					service.openFolder(job.getFolder(),job.getSequence());
					
					while (true) {
						boolean ready = service.getMessages(sandbox);
						emailIndex.commit();

						if (ready)
							break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				service.close();
			}
		}
		
	}


	private void resolveArchivFileJobs()
	{
		archivFiles.clear();
		for(EmailDataFilePath path : config.getEmailDataFilePathes())
		{
			try
			{
				List<String> afiles = emailArchivReaderService.searchArchivFile(path);
				for(String aFile : afiles)
				{
					if(emailArchivReaderService.hasUnreadMessages(aFile))
					{
						archivFiles.add(aFile);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
						
		}

	
	}
	
	private void runArchivFileJobs()
	{
		
		for(String file : archivFiles)
		{
			try
			{
				emailArchivReaderService.getMessages(file,sandbox);
			} catch (IOException e) {
				e.printStackTrace();
			}
						
		}

	
	}
	
	
	
	

	private void resolveParserJobs() {

		emailFiles.clear();

		
		File files[] = new File(sandbox.getInBoxPath()).listFiles(new WildcardFilter("*.eml"));
		for (int i = 0; i < files.length; i++) {

			try {
				String targetDir = mimeMessageParser.getTargetDir(files[i].getPath(), sandbox);
				printStatus("resolve parser jobs", i, files.length);

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

	
	private void runParserJobs() {

		for (int x = 0;x < emailFiles.size();x++ ) {
			try {
				String emlFile = emailFiles.get(x);
				printStatus("run parser jobs", x, emailFiles.size());
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

	private void resolveConvertJobs() {
		messageFolders.clear();

		File folders[] = new File(sandbox.getMessagePath()).listFiles(directoryFilter);

		for (int i = 0; i < folders.length; i++) {

			String targetDir = contentConverter.getTargetDir(folders[i].getPath(), sandbox);
			printStatus("resolve convert jobs", i, folders.length);

			// TODO: Validate target , test content/xxx/emailheader.json (siehe
			// UnitTests)
			if (new File(targetDir).exists())
				continue;

			messageFolders.add(folders[i].getPath());
		}

	}

	private void runConvertJobs() {
		
		
		for (int x = 0; x < messageFolders.size();x++) {
			try {
				String messageFolder =  messageFolders.get(x);
				printStatus("run convert jobs", x, messageFolders.size());
				contentConverter.convert(messageFolder, sandbox);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(messageFolders.get(x));
			}
		}
	}

	private void resolveCreateJobs() {
		contentFolders.clear();

		File[] folders = new File(sandbox.getContentPath()).listFiles(directoryFilter);

		for (int i = 0; i < folders.length; i++) {

			try {
				String targetPdf = pdfCreator.getTargetPdf(folders[i].getPath(), sandbox);
				printStatus("resolve create jobs", i, folders.length);

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

	private void runCreateJobs() {
		for (int x = 0;x < contentFolders.size();x++) {
			try {
				printStatus("run create jobs", x, contentFolders.size());
				pdfCreator.convert(contentFolders.get(x), sandbox);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(contentFolders.get(x));

			}
		}

	}

	private void resolveSignJobs() {
		pdfFolders.clear();

		if(config.getPdfCreatorSignatureData() == null) return;
		
		File[] folders = new File(sandbox.getPdfPath()).listFiles(directoryFilter);

		for (int i = 0; i < folders.length; i++) {
			printStatus("create sign jobs", i, folders.length);
			if (pdfSigner.getUnsignedPdfs(folders[1].getName(), sandbox).size() == 0)
				continue;
			pdfFolders.add(folders[i].getName());
		}

	}

	private void runSignJobs() {

		PdfCreatorSignatureData pdfCreatorSignatureData = config.getPdfCreatorSignatureData();

		for (int x = 0;x < pdfFolders.size();x++) {
			try {
				printStatus("run sign jobs", x, pdfFolders.size());

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
	private void printStatus(String process, int i, int max) {		
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
		return "JobQueueImpl [pollJobs="+pollJobs.size()+" archivFiles="+archivFiles.size()+" emailFiles=" + emailFiles.size() + " messageFolders=" + messageFolders.size()
				+ " contentFolders=" + contentFolders.size() + " pdfFolders=" + pdfFolders.size() + "]";
	}

	@Override
	public void resolveJobs(JobType jobType) {

		
		switch(jobType)
		{
		case PollAccount:
			resolvePollJobs();
			break;
			
		case ReadArchivFile:
			resolveArchivFileJobs();
			break;
			
		case ParseEmlFile:
			resolveParserJobs();
			break;
		
		case ComposeContent:
			resolveConvertJobs();
			break;
			
		case CreatePdf:
			resolveCreateJobs();
			break;
	
	
		case SignPdf:
			resolveSignJobs();
			break;
		
		default:
			throw new  NotImplementedException();
		}
		
		
		
	}

	

	@Override
	public void runJobs(JobType jobType) {
		
		switch(jobType)
		{
		case PollAccount:
			runPollJobs();
			break;
			
		case ReadArchivFile:
			runArchivFileJobs();
			break;
			
		case ParseEmlFile:
			runParserJobs();
			break;
		
		case ComposeContent:
			runConvertJobs();
			break;
			
		case CreatePdf:
			runCreateJobs();
			break;
	
	
		case SignPdf:
			runSignJobs();
			break;
		
		default:
			throw new  NotImplementedException();
		}		
	}

	
}
