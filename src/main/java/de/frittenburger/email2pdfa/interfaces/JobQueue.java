package de.frittenburger.email2pdfa.interfaces;

public interface JobQueue {


	void resolveParserJobs();

	void runParserJobs();

	void resolveConvertJobs();

	void runConvertJobs();

	void resolveCreateJobs();

	void runCreateJobs();

	void resolvePollJobs();
	
	void runPollJobs();

	void resolveSignJobs();

	void runSignJobs();

}
