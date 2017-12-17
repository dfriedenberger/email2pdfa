package de.frittenburger.email2pdfa.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.frittenburger.email2pdfa.interfaces.EmailArchiveList;

public class EmailArchiveListImpl implements EmailArchiveList {

	private Map<String, String> messagefiles = new HashMap<String, String>();
	private String filename = null;

	@Override
	public void init(String filename) throws IOException {
		this.filename = filename;
		messagefiles.clear();

		if (new File(filename).exists()) {
			List<String> lines = Files.readAllLines(Paths.get(filename), Charset.forName("UTF-8"));
			for (String line : lines) {

				String p[] = line.split(" ");
				if (p.length != 2)
					throw new IOException("invalid cache file " + line);
				messagefiles.put(p[0], p[1]);
			}
		}

	}

	@Override
	public void putCkSum(String file, String md5) {

		messagefiles.put(file, md5);
		commit();

	}

	private void commit() {
		try {

			PrintWriter out = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8), true);

			for (String file : messagefiles.keySet()) {
				String md5 = messagefiles.get(file);

				String line = String.format("%s %s", file, md5);
				out.println(line);

			}
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getCkSum(String file) {
		if (messagefiles.containsKey(file))
			return messagefiles.get(file);
		return null;
	}

}
