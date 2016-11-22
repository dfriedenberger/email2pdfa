package de.frittenburger.mail2pdfa;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.junit.Test;

import de.frittenburger.mail2pdfa.impl.HtmlParser;


public class TestHtmlParser {

	@Test
	public void test() throws IOException {
		
		HtmlParser htmlParser = new HtmlParser();
		htmlParser.replaceContentIds("temp/multipart_related.eml.1.html","temp/test.html");
		
	
	}
	
	/*
	private void dump(Elements children,String tab,boolean paraentHasText) {
		
		for(Element x : children)
		{
			boolean hasText = !x.ownText().trim().isEmpty();
			boolean pr = paraentHasText || hasText;

			if(x.nodeName() == "br" || x.nodeName() == "div" || x.nodeName() == "p")
				System.out.println();
			
			if(pr)
			{
				System.out.print(tab + x.ownText());
				if(x.nodeName() == "a")
					System.out.println(" "+x.attr("href"));
			}
			dump(x.children(),tab + (pr?"   ":""),pr);
			
			
		}
		
		
	}
*/



}
