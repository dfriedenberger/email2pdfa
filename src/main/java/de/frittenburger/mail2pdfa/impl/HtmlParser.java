package de.frittenburger.mail2pdfa.impl;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlParser {

	public void replaceContentIds(String srcPath, String destPath) throws IOException {
		
		
		String unsafe = readFile(srcPath);
		//String safe = Jsoup.clean(unsafe, Whitelist.basic());
		Document doc = Jsoup.parse(unsafe);


		
		for(Element e : doc.select("[src]"))
		{
			String src = e.attr("src");
			if(src.startsWith("cid:"))
			{
				e.attr("src",src.substring(4));
				System.out.println(e);
			}
		}
		for(Element e : doc.select("[href]"))
		{
			String href = e.attr("href");
			if(href.startsWith("cid:"))
			{
				e.attr("href",href.substring(4));
				System.out.println(e);
			}
		}
		
		
		saveFile(doc.toString(),destPath);
		
		
		
	}

	private String readFile(String file) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader (file));
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
