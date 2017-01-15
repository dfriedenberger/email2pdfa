package de.frittenburger.email2pdfa.impl;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.SMIMEToolkit;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;

import de.frittenburger.email2pdfa.bo.SignatureInfo;
import de.frittenburger.email2pdfa.interfaces.SignatureEngine;

public class SignatureEngineImpl implements SignatureEngine {

	private static final SMIMEToolkit toolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());

	
	//see http://programtalk.com/vs/bc-java/mail/src/test/java/org/bouncycastle/mail/smime/test/SMIMEToolkitTest.java/
	@Override
	public SignatureInfo check(MimeMessage message) throws IOException {
		
		try
		{
		   SignatureInfo info = new SignatureInfo(toolkit.isSigned(message));
		   
		   if(!info.hasSignature)
			   return info;
		   
		   SMIMESigned smimeSigned = null;
		   Object msgContent = message.getContent();
	       if(msgContent instanceof Multipart)
	       {
	    	   Multipart multipart = (Multipart) msgContent;
	    	   smimeSigned = new SMIMESigned((MimeMultipart) multipart);
	       }
	       else
	       {
	    	   throw new RuntimeException("Unknown Content "+msgContent.getClass().getName());
	       }

	       SignerInformation signerInformation = (SignerInformation)smimeSigned.getSignerInfos().getSigners().iterator().next();
	       X509CertificateHolder certificate =  toolkit.extractCertificate(message, signerInformation);

		   if(toolkit.isValidSignature(message, new JcaSimpleSignerInfoVerifierBuilder().setProvider(new BouncyCastleProvider()).build(certificate)))
		   {
			   info.subject.put("string",certificate.getSubject().toString() );
			   for(RDN rdn : certificate.getSubject().getRDNs())
			   {
				   AttributeTypeAndValue av = rdn.getFirst();
				   String type = av.getType().getId();
				   String val = IETFUtils.valueToString(av.getValue());
				   info.subject.put(type, val);
			   }
			 
		   }
		   return info;
		   
		} catch (MessagingException e) {
			throw new IOException(e);
		} catch (CMSException e) {
			throw new IOException(e);
		} catch (SMIMEException e) {
			throw new IOException(e);
		} catch (OperatorCreationException e) {
			throw new IOException(e);
		} catch (CertificateException e) {
			throw new IOException(e);
		}
		
	}

}
