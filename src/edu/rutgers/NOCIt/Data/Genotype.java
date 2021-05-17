package edu.rutgers.NOCIt.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Class used to represent a genotype with attributes of genotypeID and a 
 * of locusValues hashmap with of Locus keys and Allele values.
 *
 * @author Rob Carpenter
 * @author James Kelley
 */
public class Genotype implements Serializable {	
	private static final long serialVersionUID = 1L;
	
	private String genotypeID;
	private LinkedHashMap<Locus, Allele[]> locusValues;

	public Genotype(String genotypeID) {
		this.genotypeID = genotypeID;
		this.locusValues = new LinkedHashMap<Locus, Allele[]>();
	}
	
	public Genotype(Genotype genotype) {
		this.genotypeID = genotype.genotypeID;
		this.locusValues = new LinkedHashMap<Locus, Allele[]>();
		
		for (Locus locus : genotype.getLoci()) {
			Allele[] alleles = new Allele[genotype.locusValues.get(locus).length];
			for (int i = 0; i < genotype.locusValues.get(locus).length; i++)
				alleles[i] = genotype.locusValues.get(locus)[i];
			locusValues.put(locus, alleles);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for (Locus locus : locusValues.keySet())
			for (Allele allele : locusValues.get(locus))
				result = prime * result + allele.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Genotype other = (Genotype) obj;
		if (locusValues == null) {
			if (other.locusValues != null)
				return false;
		} else {
			if (!locusValues.keySet().equals(other.locusValues.keySet()))
				return false;
			
			for (Locus locus : locusValues.keySet())
				if (!Arrays.equals(locusValues.get(locus), other.locusValues.get(locus)))
					return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		String str = "[genotypeID=" + genotypeID + ", ";
		for (Locus locus : locusValues.keySet())
			str += locus + ": " + Arrays.toString(locusValues.get(locus)) + ", ";
		str = str.substring(0, str.length() - 2) + "]";
		
		return str;
	}

	public boolean isEmpty() {
		return locusValues.isEmpty();
	}
	
	public Set<Locus> getLoci() {
		return locusValues.keySet();
	}
	
	public boolean containsLocus(Locus locus) {
		return locusValues.containsKey(locus);
	}
	
	public Allele[] getAlleles(Locus locus) {
		return locusValues.get(locus);
	}
	
	public void putAlleles(Locus locus, Allele[] alleles) {
		Arrays.sort(alleles);
		locusValues.put(locus, alleles);
	}

	public void setLocusValues(LinkedHashMap<Locus, Allele[]> values) {
		this.locusValues = values;
	}

	public String getGenotypeID() {
		return this.genotypeID;
	}
	
	public void setGenotypeID(String genotypeID) {
		this.genotypeID = genotypeID;
	}
}
