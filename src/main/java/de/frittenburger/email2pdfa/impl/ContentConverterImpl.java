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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import de.frittenburger.email2pdfa.interfaces.ContentConverter;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class ContentConverterImpl implements ContentConverter {

	public void convert(String path, Sandbox sandbox) throws IOException {
		String targetPath = sandbox.getContentPath() + "/" + new File(path).getName();
		convertDirectories(path, targetPath);
	}

	private void convertDirectories(String path, String targetPath) throws IOException {
		
		new File(targetPath).mkdir();
		
		for (File f : new File(path).listFiles()) {

			if (f.isDirectory())
				convertDirectories(f.getPath(), targetPath +  "/" + f.getName());

			if (f.isFile()) {
				String fileName = f.getName();
				String extension = "";
				String fileNamePart = fileName;
				int i = fileName.lastIndexOf('.');
				if (i > 0) {
					extension = fileName.substring(i + 1);
					fileNamePart = fileName.substring(0, i);
				}

				if (extension.toLowerCase().equals("html")) {
					try {
						String convHtml = targetPath + "/conv_" + fileNamePart + ".html";
						String screen = targetPath + "/screen_" + fileNamePart + ".png";

						if (!new File(convHtml).exists() || !new File(convHtml).exists()) {
							HtmlParser htmlParser = new HtmlParser();
							System.out.println("Create " + convHtml);
							htmlParser.replaceContentIds(f.getPath(), convHtml);

							HtmlEngine htmlEngine = new HtmlEngine();
							System.out.println("Create " + screen);
							htmlEngine.createScreenShot(convHtml, screen);
						}

					} catch (Exception e) {
						e.printStackTrace();
						// ToDo
					}
				}
				
			    //Todo 
				Files.copy(f.toPath(), new File(targetPath + "/" + fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
				
				
				
			}

		}

	}

}
