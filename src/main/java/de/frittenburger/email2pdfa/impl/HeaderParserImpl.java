package de.frittenburger.email2pdfa.impl;

import javax.mail.MessagingException;

import de.frittenburger.email2pdfa.bo.ParamHeader;
import de.frittenburger.email2pdfa.interfaces.HeaderParser;
import de.frittenburger.email2pdfa.interfaces.Logger;

public class HeaderParserImpl implements HeaderParser {

	
	private final Logger logger = new LoggerImpl(this.getClass().getSimpleName());
	@Override
	public ParamHeader parse(String header[]) throws MessagingException {
		// 	attachment; filename=Herbstfeuerfest 2016

		
		if(header == null) return null; //no such header
		
		if(header.length < 1)
		{
			throw new MessagingException("no header value");
		}
		
		
		if(header.length > 1)
		{
			logger.error("more than one header value found, ignore others");
			for(String h : header)
				logger.error("header found "+h);

		}
		
		String text = header[0];
		
		String p[] = text.split(";");
		if(p.length < 1) throw new MessagingException(text);
		
		
		String value = p[0].trim();
		if(value.matches("[\";]"))
			throw new MessagingException(text);
		
		
		ParamHeader pheader = new ParamHeader();
		pheader.value = value;
		
		
		for(int i = 1;i < p.length;i++)
		{
			int ix = p[i].indexOf("=");
			if(ix <= 0) 
				throw new MessagingException(text);
			
			String key = p[i].substring(0, ix).trim();
			String val = p[i].substring(ix + 1).trim().replaceAll("^\"|\"$", "");
			if(key.matches("[;\"]"))
				throw new MessagingException(text);		
			if(val.matches("[;\"]"))
				throw new MessagingException(text);		
			
			
			if(key.equals("filename*"))
				key = "filename";
			if(key.equals("filename*0"))
				key = "filename";
			
			if(pheader.params.containsKey(key))
				throw new MessagingException(text);		
			
			pheader.params.put(key, val);
		}
		
		
		return pheader;
	}

	

}
