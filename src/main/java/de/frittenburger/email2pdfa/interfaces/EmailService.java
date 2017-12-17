package de.frittenburger.email2pdfa.interfaces;
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
import java.util.List;

import de.frittenburger.email2pdfa.bo.EmailServiceAccountData;

public interface EmailService {

	void open(EmailServiceAccountData emailServiceAccountData) throws IOException;
	
	List<String> getFolders() throws IOException;
	
	Sequence getUnreadMessages(String folder) throws IOException;

	void openFolder(String folder,Sequence sequence) throws IOException;
	
	boolean getMessages(Sandbox sandbox);
	
	void close();


}
