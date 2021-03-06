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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlParser {

	
	public Set<String> replaceContentIds(String srcPath, String srcEncoding, String destPath, String path , Map<String, String> mapping) throws IOException {
		
		Set<String> usedFiles = new HashSet<String>();
		Charset charset = Charset.forName(srcEncoding);
		String unsafe = readFile(srcPath,charset);
		//String safe = Jsoup.clean(unsafe, Whitelist.basic());
		Document doc = Jsoup.parse(unsafe);


		
		
		for(Element e : doc.select("[src]"))
		{
			String src = e.attr("src");
			if(src.startsWith("cid:"))
			{
				String id = src.substring(4);
				String relPath = mapping.get(id);
				usedFiles.add(relPath);
				File file = new File(path + "/" + relPath);

				if(!file.exists())
					throw new IOException(file + " not exists");
						
				e.attr("src",file.getName());
			}
		}
		
		for(Element e : doc.select("[href]"))
		{
			String href = e.attr("href");
			if(href.startsWith("cid:"))
			{
				String id = href.substring(4);
				String relPath = mapping.get(id);
				usedFiles.add(relPath);
				File file = new File(path + "/" + relPath);

				if(!file.exists())
					throw new IOException(file + " not exists");
				
				e.attr("href",file.getName());
			}
		}
		
		
		saveFile(doc.toString(),destPath);
		
		return usedFiles;
		
	}

	

	private String readFile(String file, Charset charset) throws IOException {
		
	    BufferedReader reader = new BufferedReader(new InputStreamReader( new FileInputStream (file), charset ));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}
	
	private void saveFile(String data, String path) throws IOException {
		FileOutputStream o = new FileOutputStream(path);
		 try {
		o.write(data.getBytes());
		 } finally {
		        o.close();
		    }	
		 }

}
