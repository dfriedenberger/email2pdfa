package de.frittenburger.mail2pdfa.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.barcodes.BarcodeEAN;
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
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfOutputIntent;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.filespec.PdfFileSpec;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.pdfa.PdfADocument;

import de.frittenburger.mail2pdfa.bo.EmailHeader;
import de.frittenburger.mail2pdfa.interfaces.PDFACreator;

public class PDFACreatorImpl implements PDFACreator {

	public void convert(String messagePath, String archivPath) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		EmailHeader header = mapper.readValue(new File(messagePath + "/emailheader.json"), EmailHeader.class);
		
		List<String> images = new ArrayList<String>();
		resolveFiles(new File(messagePath),images,".png");
		
		
		String path = archivPath + "/" + header.senderkey;		
		new File(path).mkdir();
		
		
		 String INTENT = "src/main/resources/color/sRGB_CS_profile.icm";
		 String FONT = "src/main/resources/font/FreeSans.ttf";

		 String dest = path +"/" + header.mesgkey+".pdf";
		
		 PdfADocument pdf = new PdfADocument(new PdfWriter(dest),
		            PdfAConformanceLevel.PDF_A_3A,
		            new PdfOutputIntent("Custom", "", "http://www.color.org",
		                    "sRGB IEC61966-2.1", new FileInputStream(INTENT)));
		      
		     
		 
		        //Setting some required parameters
		        pdf.setTagged();
		        pdf.getCatalog().setLang(new PdfString("en-US"));
		        pdf.getCatalog().setViewerPreferences(new PdfViewerPreferences().setDisplayDocTitle(true));
		        PdfDocumentInfo info = pdf.getDocumentInfo();
		        info.setTitle(header.subject);
		 
		        //Add attachment
		       
		        //addAttachment(pdf,"database.csv");
		       
		    	PdfFont font = PdfFontFactory.createFont(FONT, true);

		
		        Document document = new Document(pdf, PageSize.A4);
		        document.setMargins(20, 20, 20, 20);
		        
		   	    Paragraph p1 = new Paragraph();
		        p1.add(new Text("23").setFont(font).setFontSize(12));
		        p1.add(new Text("000").setFont(font).setFontSize(6));
		        document.add(p1);
		        
		        
		        BarcodeQRCode barcode = new BarcodeQRCode();
		        barcode.setCode(header.mesgkey);
		        
		        Rectangle barCodeRect = barcode.getBarcodeSize();
		        PdfFormXObject template = new PdfFormXObject(new Rectangle(barCodeRect.getWidth() * 3, barCodeRect.getHeight() * 3 ));
		        PdfCanvas templateCanvas = new PdfCanvas(template, pdf);
		        barcode.placeBarcode(templateCanvas, Color.BLACK,3);
		        Image image = new Image(template);
		        document.add(image);
		        
		    
		        
		        //Embed fonts
			

				
			
				//Add Image

				
				for(String imgFile : images)
				{
				  document.add(new AreaBreak());
			        Rectangle rect = pdf.getFirstPage().getPageSize();

				  document.setMargins(20, 20, 20, 20);
				  Image img = new Image(ImageDataFactory.create(imgFile));
			      img.scaleToFit(rect.getWidth(), rect.getHeight());
			      img.setFixedPosition(0, 0);

			      document.add(img);
			    }
		          
		 
		        //Close document
		        document.close();
		
		
		
		
		
		
	}

	private void resolveFiles(File path, List<String> files, String ext) {

		for(File f : path.listFiles())
		{
			if(f.isDirectory())
				resolveFiles(f, files, ext);
			
			if(f.getName().endsWith(ext))
				files.add(f.getPath());
			
		}
		
		
		
	}

	private Image getQRCode(PdfADocument pdf, String mesgkey) {
		 
		    // Create content
	        BarcodeQRCode barcode = new BarcodeQRCode(mesgkey);
	        Rectangle rect = barcode.getBarcodeSize();
	        
	        
	        PdfFormXObject template = new PdfFormXObject(new Rectangle(rect.getWidth(), rect.getHeight() + 10));
	        PdfCanvas canvas = new PdfCanvas(template, pdf);
	        barcode.placeBarcode(canvas, Color.GRAY, 12);

	        return new Image(template);
	        
	}

	private void addAttachment(PdfADocument pdf, String filename) throws IOException {
		 PdfDictionary parameters = new PdfDictionary();
	        parameters.put(PdfName.ModDate, new PdfDate().getPdfObject());
	        PdfFileSpec fileSpec = PdfFileSpec.createEmbeddedFileSpec(
	            pdf, Files.readAllBytes(Paths.get(filename)), "database.csv",
	            "database.csv", new PdfName("text/csv"), parameters,
	            PdfName.Data, false);
	        fileSpec.put(new PdfName("AFRelationship"), new PdfName("Data"));
	        pdf.addFileAttachment("database.csv", fileSpec);
	        
	        
	        PdfArray array = new PdfArray();
	        array.add(fileSpec.getPdfObject().getIndirectReference());
	        pdf.getCatalog().put(new PdfName("AF"), array);
		
	}

}
