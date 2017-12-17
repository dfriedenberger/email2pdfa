package de.frittenburger.email2pdfa;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.frittenburger.email2pdfa.bo.FolderWrapper;
import de.frittenburger.email2pdfa.bo.Range;
import de.frittenburger.email2pdfa.impl.EmailIndexSyncServiceImpl;
import de.frittenburger.email2pdfa.interfaces.EmailIndex;
import de.frittenburger.email2pdfa.interfaces.EmailIndexSyncService;
import de.frittenburger.email2pdfa.interfaces.Sequence;

public class TestEmailIndexSyncService {

	private void assertEqualLists(List<String> list1, List<String> list2, int size) {

		assertTrue(size <= list1.size());
		assertTrue(size <= list2.size());

		for(int i = 0;i < size;i++)
		{
			assertEquals(list1.get(i), list2.get(i));
		}
		
	}
	
	@Test
	public void test001() throws IOException {

		List<String> inbox = new ArrayList<String>();
		List<String> cache = new ArrayList<String>();

		inbox.add("mailkey1"); //(1)
		inbox.add("mailkey2"); //(2)
		inbox.add("mailkey3"); //(3)

		cache.add("mailkey1"); //(1)

		EmailIndexSyncService emailSyncService = new EmailIndexSyncServiceImpl();
		FolderWrapper box = new EmailBoxWrapperTestImpl(inbox,"folder");
		EmailIndex index = new EmailIndexTestImpl(cache);
		
		Sequence seq = emailSyncService.sync(index, "storekey" , box);
		
		assertTrue(seq.hasNext());
		
		Range r1 = seq.next();
		assertEquals(2,r1.from);
		assertEquals(3,r1.to);
		
		assertEquals(1,cache.size());
		assertEqualLists(inbox,cache,1);

	
	}

	@Test
	public void test002() throws IOException {

		List<String> inbox = new ArrayList<String>();
		List<String> cache = new ArrayList<String>();

		inbox.add("mailkey1"); //(1)
		inbox.add("mailkey2"); //(2) 
		inbox.add("mailkey3"); //(3)

		cache.add("mailkey1"); //(1)
		cache.add("mailkey2"); //(2)
		cache.add("mailkey3"); //(3)

		EmailIndexSyncService emailSyncService = new EmailIndexSyncServiceImpl();
		FolderWrapper box = new EmailBoxWrapperTestImpl(inbox,"folder");
		EmailIndex index = new EmailIndexTestImpl(cache);
		
		Sequence seq = emailSyncService.sync(index, "storekey" , box);
		
		assertFalse(seq.hasNext());
		
		assertEquals(3,cache.size());
		assertEqualLists(inbox,cache,3);
	
	}
	
	@Test
	public void test003() throws IOException {

		List<String> inbox = new ArrayList<String>();
		List<String> cache = new ArrayList<String>();

		inbox.add("mailkey1"); //(1)
		inbox.add("mailkey2"); //(2) 
		//inbox.add("mailkey3"); //(3) deleted

		cache.add("mailkey1"); //(1)
		cache.add("mailkey2"); //(2)
		cache.add("mailkey3"); //(3)

		EmailIndexSyncService emailSyncService = new EmailIndexSyncServiceImpl();
		FolderWrapper box = new EmailBoxWrapperTestImpl(inbox,"folder");
		EmailIndex index = new EmailIndexTestImpl(cache);
		
		Sequence seq = emailSyncService.sync(index, "storekey" , box);
		
		assertFalse(seq.hasNext());
		
		assertEquals(2,cache.size());
		assertEqualLists(inbox,cache,2);
	
	}
	
	

	@Test
	public void test101() throws IOException {

		List<String> inbox = new ArrayList<String>();
		List<String> cache = new ArrayList<String>();

		inbox.add("mailkey1"); //(1)
		//inbox.add("mailkey2"); //(2) => was deleted
		inbox.add("mailkey3"); //(3)
		inbox.add("mailkey4"); //(4)

		cache.add("mailkey1"); //(1)
		cache.add("mailkey2"); //(2)
		cache.add("mailkey3"); //(3)

		EmailIndexSyncService emailSyncService = new EmailIndexSyncServiceImpl();
		FolderWrapper box = new EmailBoxWrapperTestImpl(inbox,"folder");
		EmailIndex index = new EmailIndexTestImpl(cache);
		
		Sequence seq = emailSyncService.sync(index, "storekey" , box);
		
		assertTrue(seq.hasNext());
		
		Range r1 = seq.next();
		assertEquals(3,r1.from);
		assertEquals(3,r1.to);
			
		assertEquals(2,cache.size());
		assertEqualLists(inbox,cache,2);

	}
	
	@Test
	public void test102() throws IOException {

		List<String> inbox = new ArrayList<String>();
		List<String> cache = new ArrayList<String>();

		inbox.add("mailkey1"); //(1)
		//inbox.add("mailkey2"); //(2) => was deleted
		inbox.add("mailkey3"); //(3)

		cache.add("mailkey1"); //(1)
		cache.add("mailkey2"); //(2)
		cache.add("mailkey3"); //(3)

		EmailIndexSyncService emailSyncService = new EmailIndexSyncServiceImpl();
		FolderWrapper box = new EmailBoxWrapperTestImpl(inbox,"folder");
		EmailIndex index = new EmailIndexTestImpl(cache);
		
		Sequence seq = emailSyncService.sync(index, "storekey" , box);
		
		assertFalse(seq.hasNext());
			
	}

	
	@Test
	public void test201() throws IOException {

		List<String> inbox = new ArrayList<String>();
		List<String> cache = new ArrayList<String>();

		inbox.add("mailkey1"); //(1)
		inbox.add("mailkey2"); //(2) 
		inbox.add(null);       //(3)

		//inbox.add("mailkey3"); //(4) deleted

		cache.add("mailkey1"); //(1)
		cache.add("mailkey2"); //(2)
		cache.add(null); //(3)
		cache.add("mailkey3"); //(4)

		EmailIndexSyncService emailSyncService = new EmailIndexSyncServiceImpl();
		FolderWrapper box = new EmailBoxWrapperTestImpl(inbox,"folder");
		EmailIndex index = new EmailIndexTestImpl(cache);
		
		Sequence seq = emailSyncService.sync(index, "storekey" , box);
		
		
		assertTrue(seq.hasNext());
			
		Range r1 = seq.next();
		assertEquals(3,r1.from);
		assertEquals(3,r1.to);
		
		assertEquals(2,cache.size());
		assertEqualLists(inbox,cache,2);
	
	}
	
	@Test
	public void test202() throws IOException {

		List<String> inbox = new ArrayList<String>();
		List<String> cache = new ArrayList<String>();

		inbox.add("mailkey1"); //(1)
		inbox.add("mailkey2"); //(2) 
		inbox.add("mailkey3"); //(3)


		
		EmailIndexSyncService emailSyncService = new EmailIndexSyncServiceImpl();
		FolderWrapper box = new EmailBoxWrapperTestImpl(inbox,"folder");
		EmailIndex index = new EmailIndexTestImpl(cache);
		
		Sequence seq = emailSyncService.sync(index, "storekey" , box);
		
        assertTrue(seq.hasNext());
		
		Range r1 = seq.next();
		assertEquals(1,r1.from);
		assertEquals(3,r1.to);
		
		assertEquals(0,cache.size());
	
	}
	
	
	@Test
	public void test203() throws IOException {

		List<String> inbox = new ArrayList<String>();
		List<String> cache = new ArrayList<String>();

		inbox.add("mailkey7"); //(1)
		inbox.add("mailkey8"); //(2) 
		inbox.add("mailkey9"); //(3) 

		cache.add("mailkey1"); //(1)
		cache.add("mailkey2"); //(2)
		cache.add("mailkey3"); //(3)
		cache.add("mailkey4"); //(4)
		cache.add("mailkey5"); //(5)
		cache.add("mailkey6"); //(6)

		EmailIndexSyncService emailSyncService = new EmailIndexSyncServiceImpl();
		FolderWrapper box = new EmailBoxWrapperTestImpl(inbox,"folder");
		EmailIndex index = new EmailIndexTestImpl(cache);
		
		Sequence seq = emailSyncService.sync(index, "storekey" , box);
		
	    assertTrue(seq.hasNext());
		
		Range r1 = seq.next();
		assertEquals(1,r1.from);
		assertEquals(3,r1.to);
		
		assertEquals(0,cache.size());
	
	
	}
	
	
	
	
	@Test
	public void test204() throws IOException {

		List<String> inbox = new ArrayList<String>();
		List<String> cache = new ArrayList<String>();

		inbox.add("mailkey1"); //(1)
		inbox.add("mailkey2"); //(2)
		inbox.add("mailkey3"); //(3)
		inbox.add("mailkey6"); //(4)
		inbox.add("mailkey7"); //(5)
		inbox.add("mailkey8"); //(6)
		inbox.add("mailkey10"); //(7)

		
		cache.add("mailkey1"); //(1)
		cache.add("mailkey2"); //(2)
		cache.add("mailkey3"); //(3)
		cache.add("mailkey4"); //(4) (-)
		cache.add("mailkey5"); //(5) (-)
		cache.add("mailkey6"); //(6) (4)
		cache.add("mailkey7"); //(7) (5)
		cache.add("mailkey8"); //(8) (6)
		cache.add("mailkey9"); //(9)

	
		EmailIndexSyncService emailSyncService = new EmailIndexSyncServiceImpl();
		FolderWrapper box = new EmailBoxWrapperTestImpl(inbox,"folder");
		EmailIndex index = new EmailIndexTestImpl(cache);
		
		Sequence seq = emailSyncService.sync(index, "storekey" , box);
		
	    assertTrue(seq.hasNext());
		
		Range r1 = seq.next();
		assertEquals(7,r1.from);
		assertEquals(7,r1.to);
		
		assertEquals(6,cache.size());
		assertEqualLists(inbox,cache,6);

	
	}
	
	
	@Test
	public void test301() throws IOException {

		List<String> inbox = new ArrayList<String>();
		List<String> cache = new ArrayList<String>();

		int cnt = 100000;
		
		int p[] = new int[] { 3 , 27 , 2 , 102 , 1 , 1 , 1 };
		int delete = 0;
		int newMail = 72;
		
		for(int i = 0;i < cnt;i++)
		{
			inbox.add("mailkey"+i); 
			cache.add("mailkey"+i); 
		}
		
		//delete some mails, create gaps
		for(int i = 0;i < p.length;i++)
		{
			int pos = i * cnt / p.length;
			for(int d = 0;d < p[i];d++)
			{
				inbox.remove(pos);
				delete++;
			}
				
		}
		
		for(int i = 0;i < newMail;i++)
		{
			inbox.add("mailkeynew"+i); 
		}
		
		
		EmailIndexSyncService emailSyncService = new EmailIndexSyncServiceImpl();
		EmailBoxWrapperTestImpl box = new EmailBoxWrapperTestImpl(inbox,"folder");
		EmailIndex index = new EmailIndexTestImpl(cache);
		
		Sequence seq = emailSyncService.sync(index, "storekey" , box);
		
		System.out.println("Operations "+box.getReadOperations());
		
	    assertTrue(seq.hasNext());
		
		Range r1 = seq.next();
		assertEquals(cnt - delete + 1,r1.from);
		assertEquals(cnt - delete + newMail,r1.to);
		
		assertEquals(cnt - delete,cache.size());
		assertEqualLists(inbox,cache,cnt - delete);

	
	}
	
	
	
}
