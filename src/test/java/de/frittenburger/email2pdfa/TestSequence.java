package de.frittenburger.email2pdfa;

import static org.junit.Assert.*;

import org.junit.Test;

import de.frittenburger.email2pdfa.bo.Range;
import de.frittenburger.email2pdfa.impl.SequenceImpl;
import de.frittenburger.email2pdfa.interfaces.Sequence;

public class TestSequence {

	@Test
	public void test01() {

		Sequence seq = new SequenceImpl(13,10);
		
		assertTrue(seq.hasNext());
		assertEquals(0,seq.offset());
		assertEquals(13,seq.range());
		
		Range r1 = seq.next();
		assertEquals(1,r1.from);
		assertEquals(10, r1.to);
		
		assertTrue(seq.hasNext());
		
		Range r2 = seq.next();
		assertEquals(11,r2.from);
		assertEquals(13, r2.to);
		
		assertFalse(seq.hasNext());

	
	}

	
	@Test
	public void test02() {

		Sequence seq = new SequenceImpl(5026,10);
		seq.setFrom(5017);
		
		assertTrue(seq.hasNext());
		assertEquals(5016,seq.offset());
		assertEquals(10,seq.range());
		
		Range r1 = seq.next();
		assertEquals(5017,r1.from);
		assertEquals(5026, r1.to);
		
		assertFalse(seq.hasNext());
	
	}
	
	@Test
	public void test03() {

		Sequence seq = new SequenceImpl(0,10);
	
		assertFalse(seq.hasNext());
	    assertEquals(0,seq.offset());
	    assertEquals(0,seq.range());
	
	}
	
	
	@Test
	public void test04() {

		Sequence seq = new SequenceImpl(3,10);
	
		seq.setFrom(3);
		
		assertTrue(seq.hasNext());
		assertEquals(2,seq.offset());
		assertEquals(1,seq.range());
		
		Range r1 = seq.next();
		assertEquals(3, r1.from);
		assertEquals(3, r1.to);
		
		assertFalse(seq.hasNext());
	
	}
	
	
}
