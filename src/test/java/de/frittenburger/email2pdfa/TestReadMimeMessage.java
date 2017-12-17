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

import org.junit.Assert;
import org.junit.Test;

import de.frittenburger.email2pdfa.bo.MimeMessageParserException;
import de.frittenburger.email2pdfa.impl.EmailCacheImpl;
import de.frittenburger.email2pdfa.impl.MimeMessageParserImpl;
import de.frittenburger.email2pdfa.interfaces.MimeMessageParser;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class TestReadMimeMessage {

	private static String resources = "src/test/resources/in";
	private static String out = SandboxTestImpl.getTestPath(TestReadMimeMessage.class);
	
	@Test
	public void testSimpleHtmlEmail() throws MimeMessageParserException {

		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl(new EmailCacheImpl());
		String emlFile = resources + "/SimpleHtml.eml";
		String messagekey = "sender@example.org_20160107_1033_41790c";
		
		Sandbox sandbox = new SandboxTestImpl().setMessagePath(out);
		
		String file = mimeMessageParser.getTargetDir(emlFile, sandbox);
		Assert.assertFalse(file + " not exists", new File(file).exists());
		mimeMessageParser.parse(emlFile, sandbox);
		
		
		Assert.assertTrue("emailheader.json exists",new File(out + "/"+ messagekey+ "/emailheader.json").exists());
		Assert.assertTrue("header.txt exists",new File(out + "/"+ messagekey+ "/header.txt").exists());
	}

	@Test
	public void testSignedEmail() throws MimeMessageParserException {

		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl(new EmailCacheImpl());
		String emlFile = resources + "/SignedEmail.eml";
		String messagekey = "dirk@friedenberger.net_20170112_0703_f74fb8";

		Sandbox sandbox = new SandboxTestImpl().setMessagePath(out);
		
		String file = mimeMessageParser.getTargetDir(emlFile, sandbox);
		Assert.assertFalse(file + " not exists", new File(file).exists());
		mimeMessageParser.parse(emlFile, sandbox);
		
		
		Assert.assertTrue("emailheader.json exists",new File(out + "/"+ messagekey+ "/emailheader.json").exists());
		Assert.assertTrue("signature.json exists",new File(out + "/"+ messagekey+ "/signature.json").exists());
		Assert.assertTrue("header.txt exists",new File(out + "/"+ messagekey+ "/header.txt").exists());
	}
	
	@Test(expected = MimeMessageParserException.class)
	public void testFileNotExists() throws MimeMessageParserException {

		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl(new EmailCacheImpl());
		mimeMessageParser.parse(resources + "/FileNotExists", new SandboxTestImpl().setMessagePath(out));
		
	}
	
	@Test(expected = MimeMessageParserException.class)
	public void testEmptyFile() throws MimeMessageParserException {

		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl(new EmailCacheImpl());
		mimeMessageParser.parse(resources + "/EmptyFile.eml", new SandboxTestImpl().setMessagePath(out));

	}
	
	@Test
	public void testEmptyTransferEncoding() throws MimeMessageParserException {

		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl(new EmailCacheImpl());
		mimeMessageParser.parse(resources + "/ErrorHeaderEmptyTransferEncoding.eml", new SandboxTestImpl().setMessagePath(out));
	}
	

	/*
	private static String individualMessageForParsing = "  \\in\\  " + " file.eml ";
	@Test
	public void testIndividual() throws MimeMessageParserException {
		
		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl();
		mimeMessageParser.parse(individualMessageForParsing, new SandboxTestImpl().setMessagePath(out));

	}
	*/
	
}
