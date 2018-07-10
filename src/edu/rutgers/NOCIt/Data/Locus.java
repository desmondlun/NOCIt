package edu.rutgers.NOCIt.Data;

import java.io.Serializable;

/**
 * This is the class to represent locus which will use two different constructors
 *  based on data and input parameters. 
 * @author AG
 */
public class Locus implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int locusValue;
	private String dye;
	
	/**
	 * This constructor will be used in all other cases but when data fetching data from database
	 * @param name	- String locus name
	 */
	public Locus(String name){
		this.name = name;
		locusValue = name.toUpperCase().hashCode();
	}
	
	@Override
	public int hashCode(){
		return locusValue;
	}

	@Override
	public boolean equals(Object o) 
	{
		if (o instanceof Locus) 
		{
			Locus c = (Locus) o;
			if ( this.name.equalsIgnoreCase(c.name)) //whatever here
				return true;
		}
		return false;
	}	
	
	public String getName() {
		return name;
	}
	
	public boolean isAMEL() {
		return name.equalsIgnoreCase(Constants.Terms.AMEL);
	}
	
	public String getDye() {
		return dye;
	}

	public void setDye(String dye) {
		this.dye = dye;
	}

	public String toString() {
		return this.name;
	}
}
