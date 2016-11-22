package de.frittenburger.mail2pdfa.impl;

import java.io.File;

import de.frittenburger.mail2pdfa.interfaces.ContentConverter;

public class ContentConverterImpl implements ContentConverter {

	public void convert(String path) {
		convertDirectories(path);				
	}
	
	private void convertDirectories(String path) {
		for(File f : new File(path).listFiles())
		{
			
			if(f.isDirectory()) 
				convertDirectories(f.getPath());
			
			if(f.isFile())
			{
				if(f.getName().endsWith(".html"))
				{
					try
					{
						HtmlParser htmlParser = new HtmlParser();
						htmlParser.replaceContentIds(f.getPath(),f.getPath());
						
						HtmlEngine htmlEngine = new HtmlEngine();
						htmlEngine.createScreenShot(f.getPath(),f.getPath()+".png");
					} catch(Exception e)
					{
						e.printStackTrace();
						//ToDo
					}
				}
			}
						
		}
		
	}

}
