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

import org.junit.Test;

import de.frittenburger.email2pdfa.impl.HtmlEngine;

public class TestCallPhantomJs {

	private static String resources = "src/test/resources/content/testmail1/temp";
	
	private static String out = SandboxTestImpl.getTestPath(TestCallPhantomJs.class);
	
	@Test
	public void test() throws IOException, InterruptedException {

		HtmlEngine htmlEngine = new HtmlEngine();
		htmlEngine.createScreenShot(resources + "/tempname.html",out + "/screen.jpg");
		htmlEngine.createScreenShot(resources + "/tempname.html",out + "/screen.png");
	
	}

	/*
	@Test
	public void testIndividual() throws IOException, InterruptedException {

		HtmlEngine htmlEngine = new HtmlEngine();
		
		String htmlFile = "sandbox/content/" + " file .html";
		htmlEngine.createScreenShot(htmlFile,out + "/screenIndividual.png");
	
	}
	*/
}
