package de.frittenburger.email2pdfa.bo;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessageContext {

	
	public String encoding;
	
	public String contentFilename = null;
	public String contentId = null;
	public Map<String,String> contentIdMapping = new HashMap<String,String>();

	
	
	public String rootPath = null; //SandboxPath
	public String relavtivePath = ""; //relativer Pfad
	public Set<String> pathCache = new HashSet<String>();
	public List<String> order = new ArrayList<String>();

	public Map<String,String> contentEncoding = new HashMap<String,String>();
	
	public String mesgPath() {
		return rootPath + relavtivePath;
	}
	
}
