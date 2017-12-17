package de.frittenburger.email2pdfa.interfaces;

import de.frittenburger.email2pdfa.bo.Range;

public interface Sequence {

	public Range next();

	public boolean hasNext();

	public void setFrom(int index);

	public int offset();

	public int range();

}
