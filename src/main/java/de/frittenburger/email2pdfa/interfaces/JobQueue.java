package de.frittenburger.email2pdfa.interfaces;


public interface JobQueue {
	
	enum JobType {
		
		PollAccount,    //Polling emails from pop3 or imap Account
		
		ReadArchivFile, //read archiv files e.g. from mozilla
		
		ParseEmlFile, //parse Files and extract Content
		
		ComposeContent, //e.g. Create screenshots from Html 
		
		CreatePdf, //Create pdf/a file
		
		SignPdf //Sign pdf/a file
		
	};
	

	
	void resolveJobs(JobType jobType);
	void runJobs(JobType jobType);
	
}
