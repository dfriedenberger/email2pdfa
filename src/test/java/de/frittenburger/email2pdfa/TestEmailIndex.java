package de.frittenburger.email2pdfa;

import static org.junit.Assert.*;

import org.junit.Test;

import de.frittenburger.email2pdfa.impl.EmailIndexImpl;
import de.frittenburger.email2pdfa.interfaces.EmailIndex;

public class TestEmailIndex {

	
	
	
	
	@Test
	public void test01() 
	{
		EmailIndex index = new EmailIndexImpl();
	
		index.register("store","folder","mesgKeyX",6);

		index.register("store","folder","mesgKeyA",4);
		index.registerError("store","folder",3);
		index.register("store","folder","mesgKeyC",2);
		index.register("store","folder","mesgKeyD",1);


		assertEquals(2,index.getIndex("store","folder","mesgKeyC"));
		assertEquals(4,index.getUpperMost("store","folder"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyX"));

	}

	@Test
	public void test02() 
	{
		EmailIndex index = new EmailIndexImpl();
	
		index.register("store","folder","mesgKeyX",8); 
		index.register("store","folder","mesgKeyF",6); //(3)
		index.register("store","folder","mesgKeyE",5); //(2)
		index.register("store","folder","mesgKeyD",4);
		index.registerError("store","folder",3);
		index.register("store","folder","mesgKeyB",2);
		index.register("store","folder","mesgKeyA",1);  //(1)

		index.clear("store","folder", 2, 4);
		
		
		assertEquals(1,index.getIndex("store","folder","mesgKeyA"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyB"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyC"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyD"));
		assertEquals(2,index.getIndex("store","folder","mesgKeyE"));
		assertEquals(3,index.getIndex("store","folder","mesgKeyF"));
		assertEquals(3,index.getUpperMost("store","folder"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyX"));

	}
	
	
	@Test
	public void test03()
	{
		EmailIndex index = new EmailIndexImpl();
	
		index.register("store","folder","mesgKeyX",8); 
		index.register("store","folder","mesgKeyF",6); //(3)
		index.register("store","folder","mesgKeyE",5); //(2)
		index.register("store","folder","mesgKeyD",4);
		index.registerError("store","folder",3);
		index.register("store","folder","mesgKeyB",2);
		index.register("store","folder","mesgKeyA",1);  //(1)

		index.clear("store","folder", 1,6);
		
		
		assertEquals(-1,index.getIndex("store","folder","mesgKeyA"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyB"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyC"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyD"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyE"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyF"));
		assertEquals(0,index.getUpperMost("store","folder"));
		assertEquals(-1,index.getIndex("store","folder","mesgKeyX"));

	}
}
