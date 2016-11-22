package de.frittenburger.mail2pdfa;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import de.frittenburger.mail2pdfa.impl.HtmlEngine;

public class TestCallPhantomJs {

	@Test
	public void test() throws IOException, InterruptedException {

		
		HtmlEngine htmlEngine = new HtmlEngine();
		htmlEngine.createScreenShot("temp/test.html","temp/screen.jpg");
		htmlEngine.createScreenShot("temp/test.html","temp/screen.png");
	
		
	}

}
