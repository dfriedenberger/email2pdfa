package de.frittenburger.email2pdfa.impl;
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

import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class SandboxImpl implements Sandbox {

	public String getInBoxPath() {
		return "sandbox/in";
	}

	public String getMessagePath() {
		return "sandbox/messages";
	}

	public String getArchivPath() {
		return "sandbox/archiv";
	}
	
	public String getContentPath() {
		//return "sandbox/messages";
		return "sandbox/content";
	}
	
	public void init() {
		new File(getInBoxPath()).mkdir();
		new File(getMessagePath()).mkdir();	
		new File(getContentPath()).mkdir();		
		new File(getArchivPath()).mkdir();		
	}

	

	
}
