package de.frittenburger.email2pdfa.impl;

import java.io.UnsupportedEncodingException;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParameterList;

import de.frittenburger.email2pdfa.bo.ParamHeader;
import de.frittenburger.email2pdfa.interfaces.BodyPartWrapper;
import de.frittenburger.email2pdfa.interfaces.HeaderParser;
import de.frittenburger.email2pdfa.interfaces.NameService;

public class BodyPartWrapperImpl implements BodyPartWrapper {

	private String filename = null;
	private String contentId = null;
	private String disposition = null;

	private final NameService nameService = new NameServiceImpl();

	private final HeaderParser headerParser = new HeaderParserImpl();
	private ContentType contentType = null;

	
	public BodyPartWrapperImpl(BodyPart bodyPart) throws MessagingException, UnsupportedEncodingException {
		
		
		ParamHeader contentTypeHeader = headerParser.parse(bodyPart.getHeader("Content-Type"));
		String p[] = contentTypeHeader.value.split("/");
		ParameterList params = new ParameterList();
		for(String k : contentTypeHeader.params.keySet())
		{
			params.set(k, contentTypeHeader.params.get(k));
		}
		contentType = new ContentType(p[0],p[1],params);

		
		
		
		
		ParamHeader dispositionHeader = headerParser.parse(bodyPart.getHeader("Content-Disposition"));
		if(dispositionHeader != null)
		{
			disposition = dispositionHeader.value;
			// has Filename?
			if(dispositionHeader.params.containsKey("filename"))
			{
				filename = dispositionHeader.params.get("filename"); //bodyPart.getFileName();
				String decodedFilename = MimeUtility.decodeText(filename);
				filename = nameService.parseValidFilename(decodedFilename,contentType);	
			}
		}
		
		ParamHeader contentIdHeader = headerParser.parse(bodyPart.getHeader("Content-ID"));
		if (contentIdHeader != null) {
			contentId = contentIdHeader.value.trim();
			contentId = contentId.substring(1, contentId.length()-1);
		}
		
	
	}

	@Override
	public String getContentId() {
		return contentId;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getDisposition() {
		return disposition;
	}

	@Override
	public ContentType getContentType() {
		return contentType;
	}

}
