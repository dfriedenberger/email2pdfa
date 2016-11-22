package de.frittenburger.mail2pdfa.interfaces;

import java.io.IOException;

public interface PDFACreator {

	void convert(String messagePath, String archivPath) throws IOException;

}
