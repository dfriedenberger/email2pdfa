package de.frittenburger.email2pdfa.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class WildcardFilter implements FilenameFilter {

	private String regex;

	public WildcardFilter(String pattern) {
		regex = "^" + pattern.replace(".", "[.]")
                	.replace("?", ".")
                	.replace("*", ".*") + "$";
	}

	@Override
	public boolean accept(File arg0, String filename) {
		return Pattern.matches(regex, filename);
	}

}
