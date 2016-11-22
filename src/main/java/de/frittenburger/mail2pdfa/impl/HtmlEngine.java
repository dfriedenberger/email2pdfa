package de.frittenburger.mail2pdfa.impl;

import java.io.File;
import java.io.IOException;

public class HtmlEngine {

	public void createScreenShot(String srcPath, String imageFile) throws IOException, InterruptedException {
		
		
		String path = "file:///" + new File(srcPath).getAbsolutePath();
		
		
		path = path.replaceFirst(":\\\\", "://");
		path = path.replaceAll("\\\\", "/");

		System.out.println("Path "+path);
		System.out.println("Image "+imageFile);

		Process process = new ProcessBuilder("phantomjs/phantomjs.exe","phantomjs/mkScreenshot.js",path  ,imageFile).start();
		process.waitFor();
		
	}

}
