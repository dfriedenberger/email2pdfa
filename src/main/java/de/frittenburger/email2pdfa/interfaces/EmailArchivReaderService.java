package de.frittenburger.email2pdfa.interfaces;

import java.io.IOException;
import java.util.List;

import de.frittenburger.email2pdfa.bo.EmailDataFilePath;

public interface EmailArchivReaderService {

	List<String> searchArchivFile(EmailDataFilePath path) throws IOException;

	boolean hasUnreadMessages(String aFile) throws IOException;

	void getMessages(String archivFile, Sandbox sandbox) throws IOException;

}
