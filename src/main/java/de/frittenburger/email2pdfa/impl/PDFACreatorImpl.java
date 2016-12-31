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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDate;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.filespec.PdfFileSpec;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.pdfa.PdfADocument;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;

import de.frittenburger.email2pdfa.bo.EmailHeader;
import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;
import de.frittenburger.email2pdfa.interfaces.PDFACreator;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class PDFACreatorImpl implements PDFACreator {

	private static final String AttachmentTypeUnknown = "unknown";
	private static final String AttachmentTypeImage = "image";
	private static final String AttachmentTypeScreen = "screen";


	public void convert(PdfCreatorSignatureData signatureData, String messagePath, Sandbox sandbox) throws IOException, GeneralSecurityException {

		ObjectMapper mapper = new ObjectMapper();
		EmailHeader header = mapper.readValue(new File(messagePath + "/emailheader.json"), EmailHeader.class);

		Map<String,List<String>> files = new HashMap<String,List<String>>();
		resolveFiles(new File(messagePath), files);

		String path = sandbox.getArchivPath() + "/" + header.senderkey;

		new File(path).mkdir();

		String INTENT = "src/main/resources/color/sRGB_CS_profile.icm";
		String FONT = "src/main/resources/font/FreeSans.ttf";

		String dest = path + "/" + header.mesgkey + ".pdf";
		String dest_signed = path + "/" + header.mesgkey + "_signed.pdf";

		if (new File(dest).exists())
			return; // Auf Datei pruefen, da im Folder mehrere Emails liegen

		
		
		PdfWriter writer = new PdfWriter(dest);
		
		
		PdfADocument pdf = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_3A, new PdfOutputIntent(
				"Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", new FileInputStream(INTENT)));

		// Setting some required parameters
		pdf.setTagged();
		pdf.getCatalog().setLang(new PdfString("en-US"));
		pdf.getCatalog().setViewerPreferences(new PdfViewerPreferences().setDisplayDocTitle(true));
		PdfDocumentInfo info = pdf.getDocumentInfo();
		info.setTitle(header.subject);

		// Add attachment

		// addAttachment(pdf,"database.csv");

		PdfFont font = PdfFontFactory.createFont(FONT, true);

		Document document = new Document(pdf, PageSize.A4);
		document.setMargins(20, 20, 20, 20);

		Paragraph p1 = new Paragraph();
		p1.add(new Text("23").setFont(font).setFontSize(12));
		p1.add(new Text("000").setFont(font).setFontSize(6));
		document.add(p1);

		
		
		//CreateQRCodeImage
		BarcodeQRCode barcode = new BarcodeQRCode();
		barcode.setCode(header.mesgkey);

		Rectangle barCodeRect = barcode.getBarcodeSize();
		PdfFormXObject template = new PdfFormXObject(
				new Rectangle(barCodeRect.getWidth() * 3, barCodeRect.getHeight() * 3));
		PdfCanvas templateCanvas = new PdfCanvas(template, pdf);
		barcode.placeBarcode(templateCanvas, Color.BLACK, 3);
		Image image = new Image(template);
		document.add(image);

		// Embed fonts

		// Add ScreenShots
		List<String> images = files.get(AttachmentTypeScreen);
		if(image != null)
			for (String imgFile : images) {
				
				Rectangle rect = pdf.getFirstPage().getPageSize();
				
				Image img = new Image(ImageDataFactory.create(imgFile));
				float imgWidth = img.getImageWidth();
				float imgHeight = img.getImageHeight();
				float offsetx = 0;
				
				if(imgWidth < rect.getWidth())
				{
					offsetx = (rect.getWidth() - imgWidth) / 2;
				}
				else
				{
					imgHeight *= rect.getWidth() / imgWidth;
					imgWidth = rect.getWidth();
				}
				
				int parts =  (int) (imgHeight / rect.getHeight());
				
				for(int i = 1;i <= parts + 1;i++)
				{
					float yoffset = -1 * imgHeight + i * rect.getHeight();
					document.add(new AreaBreak());
					img.scaleToFit(imgWidth, imgHeight);
					img.setFixedPosition(offsetx, yoffset);
					document.add(img);
				}
			}

		// Close document
		document.close();

		
		signDocument(signatureData,dest,dest_signed);
	
	}

	private void signDocument(PdfCreatorSignatureData signatureData, String src, String dest) throws GeneralSecurityException, IOException {
		
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        
        KeyStore ks = KeyStore.getInstance("pkcs12", provider.getName());
        ks.load(new FileInputStream(signatureData.keyStorePath), signatureData.keyStorePassword.toCharArray());
        String alias = ks.aliases().nextElement();
        System.out.println("alias = "+alias);
        PrivateKey pk = (PrivateKey) ks.getKey(alias, signatureData.privateKeyPassword.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);
        
        String digestAlgorithm = DigestAlgorithms.SHA256;
        String providerName = provider.getName();
        PdfSigner.CryptoStandard subfilter = PdfSigner.CryptoStandard.CMS;
        
		// Creating the reader and the signer
        PdfReader reader = new PdfReader(src);
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), false);
        // Creating the appearance
        PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                .setReason(signatureData.reason)
                .setLocation(signatureData.location)
                .setReuseAppearance(false);
        Rectangle rect = new Rectangle(36, 648, 200, 100);
        appearance
                .setPageRect(rect)
                .setPageNumber(1);
        signer.setFieldName("sig");
        // Creating the signature
        IExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, providerName);
        IExternalDigest digest = new BouncyCastleDigest();
        signer.signDetached(digest, pks, chain, null, null, null, 0, subfilter);
		
	}

	private void resolveFiles(File path, Map<String, List<String>> files) {
	
		for (File f : path.listFiles()) {
			if (f.isDirectory())
				resolveFiles(f, files);
			else
			{
				/* Dateien Typisieren */
				String key = AttachmentTypeUnknown;
				if( f.getName().endsWith(".png") 
						|| f.getName().endsWith(".jpg")
						|| f.getName().endsWith(".gif"))
				{
					key = AttachmentTypeImage;
					if(f.getName().startsWith("screen_")) //TODO Defines
						key = AttachmentTypeScreen;
				}
				
				if(!files.containsKey(key))
					files.put(key, new ArrayList<String>());
			
				files.get(key).add(f.getPath());
			
			}

		}

		
	}



	private void addAttachment(PdfADocument pdf, String filename) throws IOException {
		PdfDictionary parameters = new PdfDictionary();
		parameters.put(PdfName.ModDate, new PdfDate().getPdfObject());
		PdfFileSpec fileSpec = PdfFileSpec.createEmbeddedFileSpec(pdf, Files.readAllBytes(Paths.get(filename)),
				"database.csv", "database.csv", new PdfName("text/csv"), parameters, PdfName.Data, false);
		fileSpec.put(new PdfName("AFRelationship"), new PdfName("Data"));
		pdf.addFileAttachment("database.csv", fileSpec);

		PdfArray array = new PdfArray();
		array.add(fileSpec.getPdfObject().getIndirectReference());
		pdf.getCatalog().put(new PdfName("AF"), array);

	}

	
}
