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
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

	private static String INTENT = "src/main/resources/color/sRGB_CS_profile.icm";
	private static String FONT = "src/main/resources/font/FreeSans.ttf";
		
	
	@Override
	public String getTargetPdf(String messagePath, Sandbox sandbox) throws IOException {
		
		ObjectMapper mapper = new ObjectMapper();
		EmailHeader header = mapper.readValue(new File(messagePath + "/emailheader.json"), EmailHeader.class);

		
		String targetPath = getTargetPath(sandbox,header);
		return getTargetPdf(targetPath,header);
		
	}
	
	private String getTargetPdf(String targetPath, EmailHeader header) {
		return targetPath + "/" + header.mesgkey + ".pdf";
	}

	private String getTargetPath(Sandbox sandbox, EmailHeader header) {
		return sandbox.getArchivPath() + "/" + header.senderkey;
	}

	public void convert(String messagePath, Sandbox sandbox) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		EmailHeader header = mapper.readValue(new File(messagePath + "/emailheader.json"), EmailHeader.class);

	
		String path = getTargetPath(sandbox,header);
		new File(path).mkdir();
		
		String dest = getTargetPdf(path,header);
	
		
		PdfWriter writer = new PdfWriter(dest);
		PdfADocument pdf = new PdfADocument(writer, PdfAConformanceLevel.PDF_A_3A, new PdfOutputIntent(
				"Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", new FileInputStream(INTENT)));

		// Setting some required parameters
		pdf.setTagged();
		pdf.getCatalog().setLang(new PdfString("en-US"));
		pdf.getCatalog().setViewerPreferences(new PdfViewerPreferences().setDisplayDocTitle(true));
		PdfDocumentInfo info = pdf.getDocumentInfo();
		info.setTitle(header.subject);

		
		
		//Create Document
		Document document = new Document(pdf, PageSize.A4);
		document.setMargins(20, 20, 20, 20);
		
		//header
		addTitle(document,header);
		addQRCode(pdf,document,header.mesgkey);
		
		
		//Add Content
		String contents[] = new File(messagePath + "/content").list();
		Arrays.sort(contents);
		for(String content : contents)
		{
			if(content.endsWith(".png"))
			{
				addImageContent(pdf,document,messagePath + "/content/" + content);
				continue;
			}
			
			if(content.endsWith(".txt"))
			{
				addTextContent(document,messagePath + "/content/" + content);
				continue;
			}
			throw new RuntimeException("not implemented content "+content);
		}
		
		//Add Attachments 
		addAttachment(pdf, "emailheader.json" , "application/json", messagePath + "/emailheader.json");
		for(String attachment : new File(messagePath + "/attachments").list())
		{
			addAttachment(pdf, attachment, "application/octet-stream" , messagePath + "/attachments/" + attachment);
		}
		


		// Close document
		document.close();
	}




	private void addTitle(Document document,EmailHeader header) throws IOException {
		
		PdfFont font = PdfFontFactory.createFont(FONT, true);
		Paragraph p1 = new Paragraph();
		p1.add(new Text(header.subject).setFont(font).setFontSize(12));
		Paragraph p2 = new Paragraph();
		p2.add(new Text(header.from[0].address).setFont(font).setFontSize(6));
		Paragraph p3 = new Paragraph();
		p3.add(new Text(header.date[0]).setFont(font).setFontSize(6));
		
		document.add(p1);
		document.add(p2);
		document.add(p3);

	}

	private void addQRCode(PdfADocument pdf, Document document, String code) {
		//CreateQRCodeImage
		BarcodeQRCode barcode = new BarcodeQRCode();
		barcode.setCode(code);

		Rectangle barCodeRect = barcode.getBarcodeSize();
		PdfFormXObject template = new PdfFormXObject(
				new Rectangle(barCodeRect.getWidth() * 3, barCodeRect.getHeight() * 3));
		PdfCanvas templateCanvas = new PdfCanvas(template, pdf);
		barcode.placeBarcode(templateCanvas, Color.BLACK, 3);
		Image image = new Image(template);
		document.add(image);
		
	}


	private void addTextContent(Document document, String txtFile) throws IOException {
		
		PdfFont font = PdfFontFactory.createFont(FONT, true);
	
	
		document.add(new AreaBreak());

		
		for(String line : Files.readAllLines(Paths.get(txtFile),StandardCharsets.UTF_8))
		{
			Paragraph p1 = new Paragraph();
			p1.add(new Text(line).setFont(font).setFontSize(6));
			document.add(p1);
		}
		
	}



	private void addImageContent(PdfADocument pdf, Document document, String imgFile) throws MalformedURLException {
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



	private void addAttachment(PdfADocument pdf, String name,String mimetype,String path) throws IOException {
		
		PdfDictionary parameters = new PdfDictionary();
		parameters.put(PdfName.ModDate, new PdfDate().getPdfObject());
		
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		
		String description = name;
		String fileDisplay = name;
		
		PdfFileSpec fileSpec = PdfFileSpec.createEmbeddedFileSpec(pdf, bytes, description, fileDisplay , new PdfName(mimetype), parameters, PdfName.Data, false);
		fileSpec.put(new PdfName("AFRelationship"), new PdfName("Data"));
		pdf.addFileAttachment(description, fileSpec);

		PdfArray array = new PdfArray();
		array.add(fileSpec.getPdfObject().getIndirectReference());
		pdf.getCatalog().put(new PdfName("AF"), array);

	}






	
}
