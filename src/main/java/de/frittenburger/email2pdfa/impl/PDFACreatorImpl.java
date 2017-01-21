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
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.filespec.PdfFileSpec;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.pdfa.PdfADocument;

import de.frittenburger.email2pdfa.bo.EmailHeader;
import de.frittenburger.email2pdfa.bo.SignatureInfo;
import de.frittenburger.email2pdfa.interfaces.PDFACreator;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class PDFACreatorImpl implements PDFACreator {

	private static String INTENT = "src/main/resources/color/sRGB_CS_profile.icm";
	private static String FONT = "src/main/resources/font/FreeSans.ttf";
	private static String PENCIL_ICON = "src/main/resources/icons/pencil.png";
	
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
		return sandbox.getPdfPath() + "/" + header.senderkey;
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
		
		//Title page 
		
	    //TODO: icon's  
		Image qr = getQRCodeImage(pdf,header.mesgkey);

		
		addTitle(document,header,qr);
		//Add Signing war digital unterschrieben
		File signatureFile = new File(messagePath + "/signature.json");
		if(signatureFile.exists())
		{
			addEmptyLine(document);
			
	    	SignatureInfo signatureinfo = mapper.readValue(signatureFile, SignatureInfo.class);
			addSignatureInfo(document,signatureinfo);

		}
		addEmptyLine(document);

		
		
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




	private void addEmptyLine(Document document) throws IOException {
		// TODO 
		PdfFont font = PdfFontFactory.createFont(FONT, true);
    	Paragraph p = new Paragraph();
    	p.add(new Text("\n").setFont(font).setFontSize(12));
    	document.add(p);
	}

	private void addSignatureInfo(Document document, SignatureInfo signatureinfo) throws IOException {
		
	    if(!signatureinfo.hasSignature) return;

	
	    
    	PdfFont font = PdfFontFactory.createFont(FONT, true);

	    Image img = new Image(ImageDataFactory.create(PENCIL_ICON));

    	Paragraph info = new Paragraph();
    	info.add(new Text("Die Email war digital unterschrieben").setFont(font).setFontSize(12));
	  
    	Table table = new Table(new float[]{1, 5});
	    table.setWidthPercent(100);
	    //table.setBorder(new Border(0));
	    
		Paragraph t1 = new Paragraph();
		t1.add(new Text("Email").setFont(font).setFontSize(12));
		Paragraph p1 = new Paragraph();
		p1.add(new Text(signatureinfo.email).setFont(font).setFontSize(12));
	
		
		Paragraph t2 = new Paragraph();
		t2.add(new Text("Info").setFont(font).setFontSize(12));
		Paragraph p2 = new Paragraph();
		p2.add(new Text(signatureinfo.info).setFont(font).setFontSize(12));
    	
		table.addCell(img.setAutoScale(true));
		table.addCell(info);
		
		table.addCell(t1);
		table.addCell(p1);
		table.addCell(t2);
		table.addCell(p2);
		
		document.add(table);

	}

	private void addTitle(Document document,EmailHeader header, Image qr) throws IOException {
		
		Table table = new Table(new float[]{1, 5});
	    table.setWidthPercent(100);
		
		
		PdfFont font = PdfFontFactory.createFont(FONT, true);
		Paragraph t1 = new Paragraph();
		t1.add(new Text("Betreff").setFont(font).setFontSize(12));
		Paragraph p1 = new Paragraph();
		p1.add(new Text(header.subject).setFont(font).setFontSize(12));
	
		
		Paragraph t2 = new Paragraph();
		t2.add(new Text("Sender").setFont(font).setFontSize(12));
		Paragraph p2 = new Paragraph();
		p2.add(new Text(header.from[0].address).setFont(font).setFontSize(12));
		
		
		Paragraph t3 = new Paragraph();
		t3.add(new Text("Empfänger").setFont(font).setFontSize(12));
		Paragraph p3 = new Paragraph();
		p3.add(new Text(header.date[0]).setFont(font).setFontSize(12));
		
		Paragraph p4 = new Paragraph();
		p4.add(new Text(header.mesgkey).setFont(font).setFontSize(12));
		
		table.addCell(t1);
		table.addCell(p1);
		table.addCell(t2);
		table.addCell(p2);
		table.addCell(t3);
		table.addCell(p3);
		
		table.addCell(qr);
		table.addCell(p4);
		
		document.add(table);
	}

	private Image getQRCodeImage(PdfADocument pdf, String code) {
		//CreateQRCodeImage
		BarcodeQRCode barcode = new BarcodeQRCode();
		barcode.setCode(code);

		Rectangle barCodeRect = barcode.getBarcodeSize();
		PdfFormXObject template = new PdfFormXObject(new Rectangle(barCodeRect.getWidth() * 3, barCodeRect.getHeight() * 3));
		PdfCanvas templateCanvas = new PdfCanvas(template, pdf);
		
		
		barcode.placeBarcode(templateCanvas, Color.BLACK, 3);
		
		Image image = new Image(template);
		image.setBorder(new SolidBorder(1));
		return image;
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
