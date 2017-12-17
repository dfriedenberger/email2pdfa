package de.frittenburger.email2pdfa.impl;

import de.frittenburger.email2pdfa.bo.Range;
import de.frittenburger.email2pdfa.interfaces.Sequence;

public class SequenceImpl implements Sequence {

	private int to;
	private int from;
	private int maxRange;

	public SequenceImpl(int cntMessages,int range) {
		this.to = cntMessages;
		this.from = 1;
		this.maxRange = range;
	}

	@Override
	public void setFrom(int index) {
		this.from = index;
	}

	@Override
	public Range next() {
		
		Range range = new Range();
		range.to = from + maxRange - 1;
		range.from = from;
		if(range.to > to)
			range.to = to;
		
		from = range.to+1;
		
		return range;
	}

	@Override
	public boolean hasNext() {
		return from <= to;
	}

	@Override
	public String toString() {
		return "SequenceImpl [from=" + from + ", to=" + to + "]";
	}

	@Override
	public int offset() {
		return from -1;
	}

	@Override
	public int range() {
		return to - from + 1;
	}

	
}
