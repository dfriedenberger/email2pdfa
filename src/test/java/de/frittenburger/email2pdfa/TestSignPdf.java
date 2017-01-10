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

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;

import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;
import de.frittenburger.email2pdfa.impl.PDFASignerImpl;
import de.frittenburger.email2pdfa.interfaces.PDFASigner;

public class TestSignPdf {

	private static String out = SandboxTestImpl.getTestPath(TestSignPdf.class);
	private static String resource = "src/test/resources/archiv/email1@example.org/test.pdf";
	@Test
	public void test() throws IOException, GeneralSecurityException {
		
		String senderkey = "email1@example.org";

		new File(out + "/" + senderkey).mkdir();
		Files.copy(new File(resource).toPath(), new File(out + "/"+senderkey+"/test.pdf").toPath(),StandardCopyOption.REPLACE_EXISTING);
		
		
		PDFASigner pdfCreator = new PDFASignerImpl();
		
			PdfCreatorSignatureData pdfCreatorSignatureData = new PdfCreatorSignatureData();
			pdfCreatorSignatureData.keyStorePath = "src/test/resources/keystores/demo-rsa.p12";
			pdfCreatorSignatureData.keyStorePassword = "demo";
			pdfCreatorSignatureData.privateKeyPassword = "";
			pdfCreatorSignatureData.location = "irgendwo";
			pdfCreatorSignatureData.reason = "deshalb";
			
			pdfCreator.sign(pdfCreatorSignatureData,senderkey,new SandboxTestImpl().setArchivPath(out));
			
			Assert.assertTrue(senderkey + "/test_signed.pdf exists",new File(out + "/"+senderkey+"/test_signed.pdf").exists());
			
	}

}
