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
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.frittenburger.email2pdfa.impl.ContentConverterImpl;
import de.frittenburger.email2pdfa.interfaces.ContentConverter;

public class TestConverter {

	private static String out = SandboxTestImpl.getTestPath(TestConverter.class);
	private static String resources = "src/test/resources/messages";
	
	
	
	@Test
	public void test001() throws IOException {
		String mail = "/testmail1";
		ContentConverter contentConverter = new ContentConverterImpl();
		contentConverter.convert(resources + mail,new SandboxTestImpl().setContentPath(out));

		Assert.assertTrue("header.txt exists", new File(out + mail + "/attachments/header.txt").isFile());
		Assert.assertTrue("emailheader.json exists", new File(out + mail + "/emailheader.json").isFile());
		Assert.assertTrue("Folder content exists", new File(out + mail + "/content").isDirectory());
		Assert.assertTrue("File content exists", new File(out + mail + "/content/part001.txt").isFile());

	}
	
	
	@Test
	public void test002() throws IOException {
		String mail = "/testmail2";
		ContentConverter contentConverter = new ContentConverterImpl();
		contentConverter.convert(resources + mail,new SandboxTestImpl().setContentPath(out));
		
		Assert.assertTrue("header.txt exists", new File(out + mail + "/attachments/header.txt").isFile());
		Assert.assertTrue("emailheader.json exists", new File(out + mail + "/emailheader.json").isFile());
		Assert.assertTrue("Folder content exists", new File(out + mail + "/content").isDirectory());
		Assert.assertTrue("File part001.txt exists", new File(out + mail + "/content/part001.txt").isFile());
		Assert.assertTrue("File part002.png exists", new File(out + mail + "/content/part002.png").isFile());

	}
	
	
	@Test
	public void test003() throws IOException {
		String mail = "/testmail3";
		ContentConverter contentConverter = new ContentConverterImpl();
		contentConverter.convert(resources + mail,new SandboxTestImpl().setContentPath(out));

		Assert.assertTrue("header.txt exists", new File(out + mail + "/attachments/header.txt").isFile());
		Assert.assertTrue("emailheader.json exists", new File(out + mail + "/emailheader.json").isFile());
		Assert.assertTrue("Folder content exists", new File(out + mail + "/content").isDirectory());
		Assert.assertTrue("File part001.png exists", new File(out + mail + "/content/part001.png").isFile());
		Assert.assertTrue("File part002.txt exists", new File(out + mail + "/content/part002.txt").isFile());

	}

	@Test
	public void test004() throws IOException {
		String mail = "/testmail4";
		ContentConverter contentConverter = new ContentConverterImpl();
		contentConverter.convert(resources + mail,new SandboxTestImpl().setContentPath(out));
		
		Assert.assertTrue("header.txt exists", new File(out + mail + "/attachments/header.txt").isFile());
		Assert.assertTrue("emailheader.json exists", new File(out + mail + "/emailheader.json").isFile());
		Assert.assertTrue("Folder content exists", new File(out + mail + "/content").isDirectory());
		Assert.assertTrue("File content exists", new File(out + mail + "/content/part001.txt").isFile());

		
		Assert.assertTrue("Folder attachments exists", new File(out + mail + "/attachments").isDirectory());
		Assert.assertEquals(2, new File(out + mail + "/attachments").listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String name) {
				return name.endsWith(".pdf");
			}}).length);


	}
		
	@Test
	public void test005() throws IOException {
		String mail = "/testmail5";
		ContentConverter contentConverter = new ContentConverterImpl();
		contentConverter.convert(resources + mail,new SandboxTestImpl().setContentPath(out));

		Assert.assertTrue("header.txt exists", new File(out + mail + "/attachments/header.txt").isFile());
		Assert.assertTrue("emailheader.json exists", new File(out + mail + "/emailheader.json").isFile());
		Assert.assertTrue("Folder content exists", new File(out + mail + "/content").isDirectory());
		Assert.assertTrue("File part001.png exists", new File(out + mail + "/content/part001.png").isFile());
		Assert.assertTrue("File part002.txt exists", new File(out + mail + "/content/part002.txt").isFile());

	}
	
	@Test
	public void test006() throws IOException {
		String mail = "/testmail6";
		ContentConverter contentConverter = new ContentConverterImpl();
		contentConverter.convert(resources + mail,new SandboxTestImpl().setContentPath(out));

		Assert.assertTrue("header.txt exists", new File(out + mail + "/attachments/header.txt").isFile());
		Assert.assertTrue("emailheader.json exists", new File(out + mail + "/emailheader.json").isFile());
		Assert.assertTrue("Folder content exists", new File(out + mail + "/content").isDirectory());
		Assert.assertTrue("File part001.png exists", new File(out + mail + "/content/part001.png").isFile());
		Assert.assertTrue("File part002.txt exists", new File(out + mail + "/content/part002.txt").isFile());
		Assert.assertTrue("File part003.txt exists", new File(out + mail + "/content/part003.txt").isFile());
		Assert.assertTrue("File part004.txt exists", new File(out + mail + "/content/part004.txt").isFile());
		Assert.assertTrue("Folder attachments exists", new File(out + mail + "/attachments").isDirectory());
		
		Assert.assertEquals(1, new File(out + mail + "/attachments").listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String name) {
				return name.endsWith(".png");
			}}).length);

		
	}
	
	@Test
	public void test007() throws IOException {
		String mail = "/testmail7";
		ContentConverter contentConverter = new ContentConverterImpl();
		contentConverter.convert(resources + mail,new SandboxTestImpl().setContentPath(out));

		Assert.assertTrue("header.txt exists", new File(out + mail + "/attachments/header.txt").isFile());
		Assert.assertTrue("emailheader.json exists", new File(out + mail + "/emailheader.json").isFile());
		Assert.assertTrue("signature.json exists", new File(out + mail + "/signature.json").isFile());
		Assert.assertTrue("Folder content exists", new File(out + mail + "/content").isDirectory());
		Assert.assertTrue("File part001.txt exists", new File(out + mail + "/content/part001.txt").isFile());
	
		Assert.assertTrue("Folder attachments exists", new File(out + mail + "/attachments").isDirectory());
		
		Assert.assertEquals(1, new File(out + mail + "/attachments").listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String name) {
				return name.endsWith(".png");
			}}).length);
		
		Assert.assertEquals(2, new File(out + mail + "/attachments").listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String name) {
				return name.endsWith(".txt");
			}}).length);
		
		Assert.assertEquals(1, new File(out + mail + "/attachments").listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String name) {
				return name.endsWith(".p7s");
			}}).length);
	}
	
	
}
