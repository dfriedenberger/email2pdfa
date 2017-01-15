package de.frittenburger.email2pdfa.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;

import de.frittenburger.email2pdfa.bo.PdfCreatorSignatureData;
import de.frittenburger.email2pdfa.interfaces.PDFASigner;
import de.frittenburger.email2pdfa.interfaces.Sandbox;

public class PDFASignerImpl implements PDFASigner {

	
	@Override
	public Map<String,String> getUnsignedPdfs(String senderkey, Sandbox sandbox) {
		
		Map<String,String> sourceFiles = new HashMap<String,String>();
		
		String srcPath = sandbox.getPdfPath() + "/" + senderkey;
		String destPath = sandbox.getArchivPath() + "/" + senderkey;

		String[] files = new File(srcPath).list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".pdf");
			}});
		
		
		for(String file : files)
		{
			String fileNamePart = file.substring(0,file.length() - 4);
			String src = srcPath + "/" + file;
			String dest = destPath + "/" + fileNamePart + "_signed.pdf";
			
			if(new File(dest).exists())
				continue;
			sourceFiles.put(src,dest);
		}
		
		return sourceFiles;
	}

	
	
	@Override
	public void sign(PdfCreatorSignatureData pdfCreatorSignatureData, String senderkey,Sandbox sandbox) throws IOException, GeneralSecurityException {
		
		
		Map<String,String> files = getUnsignedPdfs(senderkey,sandbox);
		File destPath = new File(sandbox.getArchivPath() + "/" + senderkey);

		if(!destPath.exists())
			destPath.mkdir();
		
		for(String src : files.keySet())
		{
			String dest = files.get(src);
			signDocument(pdfCreatorSignatureData,src,dest);
		}
	
	}
	private void signDocument(PdfCreatorSignatureData signatureData, String src, String dest) throws GeneralSecurityException, IOException {
		
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        
        KeyStore ks = KeyStore.getInstance("pkcs12", provider.getName());
        ks.load(new FileInputStream(signatureData.keyStorePath), signatureData.keyStorePassword.toCharArray());
        String alias = ks.aliases().nextElement();
        //System.out.println("alias = "+alias);
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
	


}
