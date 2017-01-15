package de.frittenburger.email2pdfa;
/*
 *  Copyright notice
 *
 *  (c) 2016 Dirk Friedenberger <projekte@frittenburger.de>
 *
 *  All rights reserved
 *
 *  This script is part of the Email2PDFA project. The Email2PDFA is
 *  free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The GNU General Public License can be found at
 *  http://www.gnu.org/copyleft/gpl.html.
 *
 *  This script is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  This copyright notice MUST APPEAR in all copies of the script!
 */

import java.util.List;
import java.util.Scanner;

import de.frittenburger.email2pdfa.impl.ConfigurationImpl;
import de.frittenburger.email2pdfa.impl.JobQueueImpl;
import de.frittenburger.email2pdfa.interfaces.Configuration;
import de.frittenburger.email2pdfa.interfaces.JobQueue;

public class ConsoleApp {

	public static void main(String[] args) throws Exception {

		List<Configuration> configurations = ConfigurationImpl.read("config");

		for (Configuration config : configurations)
			System.out.println(config.getName());

		Scanner scanner = new Scanner(System.in);
		System.out.println("Select Config");
		System.out.print(">");
		System.out.flush();
		String confname = scanner.next();
		
		for (Configuration config : configurations) {
			if (!config.getName().equals(confname))
				continue;

			JobQueue jobqueue = new JobQueueImpl(config);

			while (true) {

				jobqueue.resolvePollJobs();
				jobqueue.resolveParserJobs();
				jobqueue.resolveConvertJobs();
				jobqueue.resolveCreateJobs();
				jobqueue.resolveSignJobs();

				System.out.println(jobqueue);
				printHelp();
				System.out.print(">");
				System.out.flush();
				String command = scanner.next();

				if (command.equals("exit")) {
					break;
				} else if (command.equals("poll")) {
					// Download emails
					jobqueue.runPollJobs();
				} else if (command.equals("parse")) {
					jobqueue.runParserJobs();
				} else if (command.equals("convert")) {
					jobqueue.runConvertJobs();
				} else if (command.equals("create")) {
					jobqueue.runCreateJobs();
				} else if (command.equals("sign")) {
					jobqueue.runSignJobs();

				} else {
					System.out.println("Unknown Command " + command);
					printHelp();
				}

			}
			
			scanner.close();
			System.exit(0);
		}

	}

	private static void printHelp() {
		System.out.println("use following Arguments");
		System.out.println("poll - for polling emails");
		System.out.println("parse - parse and extract emails");
		System.out.println("convert - create screenshots from html parts");
		System.out.println("create - create pdf/a files");
		System.out.println("sign - sign pdf/a files");
	}

}
