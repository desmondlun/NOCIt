package edu.rutgers.NOCIt.Data;

import java.io.Serializable;

public class DegradedCalibrationPeak extends Peak implements Serializable {	
	private static final long serialVersionUID = 1L;
	
	private double dnaMass;
	private double qi;
	private int assocHeight;		// Height of associated peak
	
	public DegradedCalibrationPeak(Allele allele, double size, int height, double dNAm, double qi) {
		super(allele, size, height);
		this.dnaMass = dNAm;
		this.qi = qi;		
	}		
	
	public DegradedCalibrationPeak(Peak peak, double dNAm, double qi) {
		super(peak.getAllele(), peak.getSize(), peak.getHeight());
		this.dnaMass = dNAm;
		this.qi = qi;		
	}
	
	public int getAssocHeight() {
		return assocHeight;
	}

	public void setAssocHeight(int assocHeight) {
		this.assocHeight = assocHeight;
	}
	
	public double getDNAMass(){
		return dnaMass;
	}
	
	public double getQI(){
		return qi; 
	}
	
	public String toString(){
		return (super.toString() + "," + dnaMass + "," + qi+ "," + this.assocHeight);
	}       
}
