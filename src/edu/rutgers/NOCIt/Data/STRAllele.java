package edu.rutgers.NOCIt.Data;

import java.io.Serializable;

/**
 * This class extends Allele class and can be used to represent all Alleles but AMEL
 * 
 * @author AG
 *
 */
public class STRAllele extends Allele implements Serializable, Comparable<STRAllele> {
	private static final long serialVersionUID = 1L;
	
	// Upper bound on possible repeat length for calculating hash code; for speed, use a power of 2
	private static final int REPEAT_LENGTH_UB = 8; 	 
	
	private int repeats;	
	private int fraction;
	
    /**
     * This constructor can be used when allele value is of String type
     * @param name - String datatype
     */
	public STRAllele(String name) {
		try {
			String[] strSplit = name.split("\\.");
			this.repeats = Integer.parseInt(strSplit[0]);
			if (strSplit.length > 1)
				this.fraction = Integer.parseInt(strSplit[1]);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Copy constructor
	 * @param allele
	 */	
	public STRAllele(STRAllele allele) {
		this.repeats = allele.repeats;
		this.fraction = allele.fraction;
	}

	@Override
	public int hashCode(){
		return repeats * REPEAT_LENGTH_UB + fraction;
	}

	@Override
	public boolean equals(Object o) 
	{
		if (o instanceof STRAllele) 
		{
			STRAllele c = (STRAllele) o;
			if (this.repeats == c.repeats && this.fraction == c.fraction)
				return true;
		}
		return false;
	}

	@Override
	public String toString(){
		String value = Integer.toString(repeats);
		if (fraction != 0) {
			value += "." + fraction;
		}
		return value;
	}

	public STRAllele rStutterAllele() {
		STRAllele allele = new STRAllele(this);
		allele.repeats -= 1;
		return allele;
	}

	public STRAllele fStutterAllele() {
		STRAllele allele = new STRAllele(this);
		allele.repeats += 1;
		return allele;
	}	    
	
	public STRAllele rParentAllele() {
		STRAllele allele = new STRAllele(this);
		allele.repeats += 1;
		return allele;
	}
	
	public STRAllele fParentAllele() {
		STRAllele allele = new STRAllele(this);
		allele.repeats -= 1;
		return allele;
	}

	@Override
	public int getRepeats() {
		return repeats;
	}    
	
	@Override
	public int getFraction() {
		return fraction;
	}	

	@Override
	public int compareTo(STRAllele o) {
		if (this.repeats < o.repeats) 
			return -1;
		else if (this.repeats == o.repeats) 
			return Integer.compare(this.fraction, o.fraction);
		else 
			return 1;	
	}
}
