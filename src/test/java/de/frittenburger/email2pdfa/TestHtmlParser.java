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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.frittenburger.email2pdfa.impl.HtmlParser;


public class TestHtmlParser {

	
	private static String resources = "src/test/resources/messages/testmail2";
	private static String out = SandboxTestImpl.getTestPath(TestHtmlParser.class);
	
	@Test
	public void test() throws IOException {
		
		HtmlParser htmlParser = new HtmlParser();
		Map<String,String> files = new HashMap<String,String>();
		files.put("xxxx","abcdef.png");
		
		Set<String> inline = htmlParser.replaceContentIds(resources + "/alternative/test.html",out + "/test.html", resources , files);
		Assert.assertEquals(0, inline.size());	

	}
	
}
