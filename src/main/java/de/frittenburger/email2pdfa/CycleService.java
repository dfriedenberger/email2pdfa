package de.frittenburger.email2pdfa;

import java.net.InetSocketAddress;
import java.util.Locale;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import de.frittenburger.core.AdminPanel;
import de.frittenburger.core.Group;
import de.frittenburger.core.LoggerListener;
import de.frittenburger.email2pdfa.bo.GlobalConfig;
import de.frittenburger.email2pdfa.impl.ConfigurationImpl;
import de.frittenburger.email2pdfa.impl.FileSecretProvider;
import de.frittenburger.email2pdfa.impl.JobQueueImpl;
import de.frittenburger.email2pdfa.impl.LoggerImpl;
import de.frittenburger.email2pdfa.interfaces.Configuration;
import de.frittenburger.email2pdfa.interfaces.JobQueue;
import de.frittenburger.email2pdfa.interfaces.Logger;
import de.frittenburger.form.DataDirectory;
import de.frittenburger.form.EmailAccount;
import de.frittenburger.form.Signature;
import de.frittenburger.web.AdminPanelServlet;

public class CycleService {

	public static void main(String[] args) throws Exception {

		init();

		Logger logger = new LoggerImpl(CycleService.class.getSimpleName());

		// Create Jetty Server
		Server server = new Server(new InetSocketAddress("localhost", 3333));

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		ServletHandler handler = context.getServletHandler();
		handler.addServletWithMapping(new ServletHolder(new AdminPanelServlet()), "/admin/*");

		server.setHandler(context);

		// Start things up!
		server.start();

		boolean shouldRunning = true;
		do {

			// check
			Configuration config = ConfigurationImpl.read();

			logger.info(config.getName());

			JobQueue jobqueue = new JobQueueImpl(config);

			// Resolve Jobs
			for (JobQueue.JobType type : JobQueue.JobType.values()) {

				jobqueue.resolveJobs(type);
				logger.info(jobqueue);
				jobqueue.runJobs(type);
				
			}

			Thread.sleep(1000 * 60);

		} while (shouldRunning);

		server.join();
	}

	static void init() {

		AdminPanel.setLoggerListener(new LoggerListener() {

			@Override
			public void log(int level, String message, String dump) {
				if (level == 0) {
					System.err.println(message);
					if (dump != null)
						System.err.println(dump);
				} else {
					System.out.println(message);
					if (dump != null)
						System.out.println(dump);
				}
			}
		});

		AdminPanel.setSecretProvider(new FileSecretProvider());

		AdminPanel.withDefaults(Locale.GERMANY);
		AdminPanel.createPage("global", "Konfiguration").setSingletonForm(GlobalConfig.class);
		AdminPanel.createPage("account", "Kontos").setForm(EmailAccount.class);
		AdminPanel.createPage("files", "Lokale Email Ordner").setForm(DataDirectory.class);
		AdminPanel.createPage("signature", "Signature").setSingletonForm(Signature.class);
		AdminPanel.selection().forGroup(Group.User).forPage("global").forPage("account").forPage("files")
				.forPage("signature").allowAll();

	}

}
