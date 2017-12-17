package de.frittenburger.email2pdfa.interfaces;

import java.io.IOException;

import de.frittenburger.email2pdfa.bo.FolderWrapper;

public interface EmailIndexSyncService {

	Sequence sync(EmailIndex index, String storeKey, FolderWrapper inbox) throws IOException;
	
}
