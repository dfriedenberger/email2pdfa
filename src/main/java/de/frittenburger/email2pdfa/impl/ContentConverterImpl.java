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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.frittenburger.email2pdfa.interfaces.ContentConverter;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class ContentConverterImpl implements ContentConverter {

	enum TypeOfPart {
		Attachment,  Content, Ignore, Signature
	}

	private File targetPath;
	private File contentPath;
	private File tempPath;
	private File attachmentPath;
	private int contentCounter;
	
	
	@Override
	public String getTargetDir(String path, Sandbox sandbox) {
		return sandbox.getContentPath() + "/" + new File(path).getName();
	}
	
	@Override
	public void convert(String path, Sandbox sandbox) throws IOException {
		
		
		String targetPathStr = getTargetDir(path,sandbox);
		
		println("convert "+path+ " to "+ targetPathStr);

		
		targetPath = new File(targetPathStr);
		targetPath.mkdir();
		contentPath = new File(targetPathStr + "/content");
		contentPath.mkdir(); 
		tempPath = new File(targetPathStr + "/temp");
		tempPath.mkdir(); 
		attachmentPath = new File(targetPathStr + "/attachments");
		attachmentPath.mkdir(); 
		contentCounter = 1;
		
		
		//EmailHeader
		File emailheader = new File(path + "/emailheader.json");
		if(!emailheader.exists()) 
			throw new IOException("Missing emailheader.json");
		
		//header.txt
		File txtheader = new File(path + "/header.txt");
		if(!txtheader.exists()) 
			throw new IOException("Missing header.txt");
		
		//Signature
		File signatureFile = new File(path + "/signature.json");
		
		//Order
		File orderFile = new File(path + "/order.json");
		if(!orderFile.exists()) 
			throw new IOException("Missing order.json");
		
		ObjectMapper mapper = new ObjectMapper();
		List<String> order = mapper.readValue(orderFile, new TypeReference<List<String>>() {});
		 
		//Mapping
		File mappingFile = new File(path + "/mapping.json");
		Map<String,String> mapping = new HashMap<String,String>();
		if(mappingFile.exists()) 
			mapping = mapper.readValue(mappingFile, new TypeReference<HashMap<String,String>>() {});

		//Encoding
		File encodingFile = new File(path + "/encoding.json");
		Map<String,String> encoding = new HashMap<String,String>();
		if(encodingFile.exists()) 
			encoding = mapper.readValue(encodingFile, new TypeReference<HashMap<String,String>>() {});
		
		
		Files.copy(emailheader.toPath(), new File(targetPath.getPath() + "/emailheader.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
		Files.copy(txtheader.toPath(), new File(attachmentPath.getPath() + "/header.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		if(signatureFile.exists())
			Files.copy(signatureFile.toPath(), new File(targetPath.getPath() + "/signature.json").toPath(), StandardCopyOption.REPLACE_EXISTING);

		
		List<String> usedFiles = new ArrayList<String>();
		List<String> ignoredFiles = new ArrayList<String>();

		for(String file : order)
		{
			File f = new File(path + "/" + file);
			if(!f.exists())
				throw new IOException(file + " not exists");
			
			
			println(file);

			if(f.isDirectory())
				continue;
			
			String fileName = f.getName();
			String extension = "";
			
			int i = fileName.lastIndexOf('.');
			if (i > 0) {
				extension = fileName.substring(i + 1).toUpperCase();
			}
			
			switch(typeofpart(path + "/" + file,extension))
			{
			case Attachment:
				Files.copy(f.toPath(), new File(attachmentPath.getPath() + "/" + fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
			break;
			case Content:
				String charset = encoding.get(file);
				if(charset == null) charset = "utf-8";
				
				//Create copy in attachments
				Files.copy(f.toPath(), new File(attachmentPath.getPath() + "/" + charset +"_" + fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);

				
				if (extension.equals("TXT")) {
					String text = getContentPartPath("txt");
					transform(f,charset,new File(text),"UTF-8");
					
					break;
				}
				
				if (extension.equals("HTML")) {

					String tempHtml = getTempPath("html");
					String screen = getContentPartPath("png");

					HtmlParser htmlParser = new HtmlParser();
					println("Create " + tempHtml);
					
					Set<String> inlineImages = htmlParser.replaceContentIds(f.getPath(), charset, tempHtml,path,mapping);

					//Copy Files
					for(String inlineImage : inlineImages)
					{
						usedFiles.add(inlineImage);
						File inline = new File(path + "/" + inlineImage);
						Files.copy(inline.toPath(), new File(tempPath.getPath() + "/" + inline.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
						
					
					try {
						HtmlEngine htmlEngine = new HtmlEngine();
						println("Create " + screen);
						htmlEngine.createScreenShot(tempHtml, screen);
					} catch (InterruptedException e) {
						throw new IOException(e);
					}
					break;
				}
				throw new RuntimeException("Not implemented " + f.getName() + " in Content");
			case Ignore:
				println("Ignore "+f.getPath());
				ignoredFiles.add(file);
			break;
			case Signature:
				//copy original signature
				Files.copy(f.toPath(), new File(attachmentPath.getPath() + "/" + fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
				break;
			default:
				throw new RuntimeException(" default case ");
			}			
		}
		
		println("UsedFiles "+usedFiles);
		println("IgnoredFiles "+ignoredFiles);
		
		for(String file : ignoredFiles)
		{
			if(usedFiles.contains(file)) continue;
			//Attach
			File f = new File(path + "/" + file);
			Files.copy(f.toPath(), new File(attachmentPath.getPath() + "/" + f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		
	
	
	}
	
	private void println(Object obj) {
		//do nothing in moment
	}

	private TypeOfPart typeofpart(String filepath,String extension) throws IOException {
		
		String directory = new File(filepath).getParentFile().getName();
		
		if(directory.startsWith("alternative"))
		{
			if (extension.equals("HTML") || extension.equals("TXT")) return TypeOfPart.Content;
			if (extension.equals("ICS")) return TypeOfPart.Attachment;
			throw new IOException("unexpected file "+filepath);
		}
		
		if(directory.startsWith("related"))
		{
			if (extension.equals("HTML")) return TypeOfPart.Content;
			return TypeOfPart.Ignore;
		}
		
		if(directory.startsWith("mixed"))
		{
			if (extension.equals("HTML") || extension.equals("TXT")) return TypeOfPart.Content;
			return TypeOfPart.Attachment;
		}
		
		if(directory.startsWith("signed"))
		{
			if (extension.equals("P7S")) return TypeOfPart.Signature;
		}
		
		//Root
		if (extension.equals("HTML") || extension.equals("TXT")) return TypeOfPart.Content;
		
		throw new IOException("unexpected extension "+extension+" in Directory "+directory);
	}

	private void transform(File source, String srcEncoding, File target, String tgtEncoding) throws IOException {
	    try (
	      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(source), srcEncoding));
	      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), tgtEncoding)); ) {
	          char[] buffer = new char[16384];
	          int read;
	          while ((read = br.read(buffer)) != -1)
	              bw.write(buffer, 0, read);
	    } 
	}
	private String getTempPath(String extension) {
		return String.format("%s/%s.%s",tempPath.getPath(),UUID.randomUUID(),extension);
	}

	private String getContentPartPath(String extension) {
		return String.format("%s/part%03d.%s",contentPath.getPath(),contentCounter++,extension);
	}

	



}


