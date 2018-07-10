package edu.rutgers.NOCIt.Data;

/**
 * Creates a peak object with 2 characteristics: allele (double) and height (int).
 * Used for all loci except AMEL.
 *
 */
public class Peak { 
	private Allele allele;
	private double size = Double.NaN;
	private int height;
	
	public Peak(Allele allele, int height){
		this.allele = allele;		
		this.height = height;
	}
	
	public Peak(Allele allele, double size, int height) {
		this.allele = allele;
		this.size = size;
		this.height = height;
	}
	
	public Allele getAllele(){
		return allele;
	}
	
	public double getSize() {
		return size;
	}
	
	public void setSize(double size) {
		this.size = size;
	}

	public int getHeight(){
		return height;
	}
	
	@Override
	public boolean equals(Object o) 
	{
		if (o instanceof Peak) 
		{
			Peak c = (Peak) o;
			if (this.allele.equals(c.allele) && this.height == c.height) 
				return true;
		}
		return false;
	}
	
	@Override
	public String toString(){
		return (allele + "," + size + "," + height);
	}
}
