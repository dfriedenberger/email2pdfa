package de.frittenburger.email2pdfa.impl;
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

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.concurrent.TimeUnit;

public class HtmlEngine {

	public void createScreenShot(String srcPath, String imageFile) throws IOException, InterruptedException {
		
		
		String path = "file:///" + new File(srcPath).getAbsolutePath();
		
		
		path = path.replaceFirst(":\\\\", "://");
		path = path.replaceAll("\\\\", "/");

		System.out.println("Path "+path);
		System.out.println("Image "+imageFile);

		
		ProcessBuilder processBuilder = new ProcessBuilder("phantomjs/phantomjs.exe","phantomjs/mkScreenshot.js", path  ,imageFile);
		processBuilder.redirectOutput(Redirect.INHERIT);
		processBuilder.redirectError(Redirect.INHERIT);
		Process process = processBuilder.start();
		if(!process.waitFor(60, TimeUnit.SECONDS))
		{
			System.err.println("Timeout");
			process.destroy();
		}
		
		
	}

}
