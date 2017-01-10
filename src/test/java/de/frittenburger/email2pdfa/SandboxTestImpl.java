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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class SandboxTestImpl implements Sandbox {

	private String messagePath;
	private String inBoxPath;
	private String contentPath;
	private String archivPath;

	public SandboxTestImpl setMessagePath(String messagePath) {
		this.messagePath = messagePath;
		return this;
	}

	public SandboxTestImpl setInBoxPath(String inBoxPath) {
		this.inBoxPath = inBoxPath;
		return this;
	}

	public SandboxTestImpl setContentPath(String contentPath) {
		this.contentPath = contentPath;
		return this;
	}

	public SandboxTestImpl setArchivPath(String archivPath) {
		this.archivPath = archivPath;
		return this;
	}
	
	public String getMessagePath() {
		return messagePath; 
	}

	public String getInBoxPath() {
		return inBoxPath; 
	}

	public String getContentPath() {
		return contentPath;
	}
	
	public String getArchivPath() {
		return archivPath; 
	}
	
	public void init() {
		//nix
	}

	public static <T> String getTestPath(Class<T> class1) {

		String path = "sandbox/tests";
		new File(path).mkdir();
		String testpath = path+ "/" + class1.getSimpleName();
		new File(testpath).mkdir();
		
		//clean folder
		try
		{
			for(File f : new File(testpath).listFiles())
				delete(f);
		}
		catch(IOException io)
		{
			throw new RuntimeException(io);
		}
		
		return testpath;
		
	}

	static void  delete(File f) throws IOException {
		  if (f.isDirectory()) {
		    for (File c : f.listFiles())
		      delete(c);
		  }
		  if (!f.delete())
		    throw new FileNotFoundException("Failed to delete file: " + f);
		}

	

}
