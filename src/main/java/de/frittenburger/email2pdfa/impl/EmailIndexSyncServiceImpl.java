package de.frittenburger.email2pdfa.impl;

import java.io.IOException;

import de.frittenburger.email2pdfa.bo.FolderWrapper;
import de.frittenburger.email2pdfa.interfaces.EmailIndex;
import de.frittenburger.email2pdfa.interfaces.EmailIndexSyncService;
import de.frittenburger.email2pdfa.interfaces.Sequence;

public class EmailIndexSyncServiceImpl implements EmailIndexSyncService {

	private static int RANGE = 100;

	@Override
	public Sequence sync(EmailIndex emailIndex, String storeKey, FolderWrapper folder) throws IOException {

		Sequence seq = new SequenceImpl(folder.getCount(), RANGE);

		while (true) // Maximal 1
		{
			// Syncronize sequence
			int u = emailIndex.getUpperMost(storeKey, folder.getName());
			if (u == 0) {
				seq.setFrom(1);
				return seq;
			}

			int s = findUpperMostSyncPoint(0,u, emailIndex, storeKey, folder);
			if (s == u) {
				// Alles OK
				seq.setFrom(u + 1);
				return seq;
			}

			
			System.out.println(" synced = " + s + " upperMosted = " + u);
			
			if (s == -1)
				throw new IOException("No SyncPoint found");

			// synchronisieren
			if (s < u) {
				// Sync from s to u
				int ns = sync(s, u, emailIndex, storeKey, folder);
				if (ns == -1)
					throw new IOException("No SyncPoint found");

				if (ns > s) {
					// Noch mal probieren
					continue;
				}

				if (ns == s) {
					emailIndex.clear(storeKey, folder.getName(), s + 1, u);
					seq.setFrom(s + 1);
					return seq;
				}

			}

			break;

		}
		throw new IOException("Not implemented");

	}

	private int sync(int s, int u, EmailIndex emailIndex, String storeKey, FolderWrapper folder) throws IOException {

		// Was ist an Position s + 1
		int ix = s + 1;
		String boxMessageKey = folder.listMessage(ix);
		int i = emailIndex.getIndex(storeKey, folder.getName(), boxMessageKey);

		if (i == -1) {
			// not found in Index, muss eine neue Email sein
			return s;
		}

		if (i > ix) {
			// Die Mails im Index zwischen ix und i - 1 existieren nicht mehr,
			// wurden gelöscht.
			// clear Index
			emailIndex.clear(storeKey, folder.getName(), ix, i - 1);
			return s + 1;
		}

		throw new IOException("sync not implemented ix = "+ix+" i = "+i);
	}

	private int findUpperMostSyncPoint(int o,int u, EmailIndex emailIndex, String storeKey, FolderWrapper folder)
			throws IOException {

		String msgKey = folder.listMessage(u);
		if (msgKey != null) // Todo
		{
			int i = emailIndex.getIndex(storeKey, folder.getName(), msgKey);
			if (i == u) {
				return i;
			}
		}

		if(u == 1) //Das war das letzte Element
			return 0;
			
		if(o + 1 == u) //Da gibts nichts mehr zu finden
			return o;
		
		int no = o;
		// try
		while(true)
		{
			int nu = (no + u) / 2;
			
			if (nu <= o  || nu >= u)
				throw new IOException(" o = " + o + " u = " + u + " nu = " + nu+ " no = " + no);

			int ns = findUpperMostSyncPoint(no,nu, emailIndex, storeKey, folder);
		    if(ns == nu && no + 1 < u)
		    {
		    	//Vll gehts noch besser
		    	no = nu;
		    	continue;
		    }
		
		    return ns;
		}

	}

}
