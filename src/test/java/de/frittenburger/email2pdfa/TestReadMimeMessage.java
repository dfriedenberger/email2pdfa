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

import org.junit.Test;

import de.frittenburger.email2pdfa.bo.MimeMessageParserException;
import de.frittenburger.email2pdfa.impl.MimeMessageParserImpl;
import de.frittenburger.email2pdfa.interfaces.MimeMessageParser;

public class TestReadMimeMessage {

	private static String resources = "src/test/resources/mail";
	private static String out = SandBoxTestImpl.getTestPath(TestReadMimeMessage.class);
	
	@Test
	public void testSimpleHtmlEmail() throws MimeMessageParserException {

		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl();
		mimeMessageParser.parse(resources + "/SimpleHtml.eml", new SandBoxTestImpl().setMessagePath(out));

	}

	
	@Test(expected = MimeMessageParserException.class)
	public void testFileNotExists() throws MimeMessageParserException {

		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl();
		mimeMessageParser.parse(resources + "/FileNotExists", new SandBoxTestImpl().setMessagePath(out));
		
	}
	
	@Test(expected = MimeMessageParserException.class)
	public void testEmptyFile() throws MimeMessageParserException {

		MimeMessageParser mimeMessageParser = new MimeMessageParserImpl();
		mimeMessageParser.parse(resources + "/EmptyFile.eml", new SandBoxTestImpl().setMessagePath(out));

	}
}
