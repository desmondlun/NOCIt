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
		
		for (Locus locus : genotype.getLoci()) {
			Allele[] alleles = new Allele[genotype.locusValues.get(locus).length];
			for (int i = 0; i < genotype.locusValues.get(locus).length; i++)
				alleles[i] = genotype.locusValues.get(locus)[i];
			locusValues.put(locus, alleles);
		}
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
