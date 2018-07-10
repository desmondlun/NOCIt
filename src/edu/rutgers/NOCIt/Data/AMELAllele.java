package edu.rutgers.NOCIt.Data;

import java.io.Serializable;

/**
 * 
 * Allele can only have names of X and Y
 *
 */
public class AMELAllele extends Allele implements Serializable, Comparable<AMELAllele> {
	private static final long serialVersionUID = 1L;
	public static final int MAX_HASH_CODE = 1;
	
	private int value;
	private String name;

	public AMELAllele(String name){
		name = name.toUpperCase();
		
		switch (name) {
		case "X":
			this.value = 0;
			this.name = name;
			break;
		case "Y":
			this.value = 1;
			this.name = name;
			break;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public int hashCode(){
		return value;
	}
	
	@Override
	public boolean equals(Object o) 
	{
		if (o instanceof AMELAllele) 
		{
			AMELAllele c = (AMELAllele) o;
			if ( this.value == c.value) //whatever here
				return true;
		}
		return false;
	}

	@Override
	public String toString() {		
		return name;
	}

	@Override
	public int getRepeats() {
		return value;
	}

	@Override
	public int getFraction() {
		return 0;
	}
	
	@Override
	public int compareTo(AMELAllele o) {
		return Integer.compare(this.value, o.value);
	}
}
