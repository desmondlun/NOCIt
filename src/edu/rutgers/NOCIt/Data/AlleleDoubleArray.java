package edu.rutgers.NOCIt.Data;

import java.util.Collection;

public class AlleleDoubleArray {
	double[] array;
	Collection<Allele> alleles = null;
	
	public AlleleDoubleArray(int maxVal) {		
		array = new double[maxVal + 1];
	}
	
	public AlleleDoubleArray(Collection<Allele> alleles) {
		int maxVal = 0;
		for (Allele allele : alleles)
			if (allele.hashCode() > maxVal)
				maxVal = allele.hashCode();
		
		this.alleles = alleles;
		array = new double[maxVal + 1];
	}
	
	public double get(Allele allele) {
		return array[allele.hashCode()];	
	}
	
	public void put(Allele allele, double value) {
		array[allele.hashCode()] = value;
	}
	
	public void inc(Allele allele, double value) {
		array[allele.hashCode()] += value;
	}
}
