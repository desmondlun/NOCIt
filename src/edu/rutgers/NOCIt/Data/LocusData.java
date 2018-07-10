package edu.rutgers.NOCIt.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocusData
{
	private ConcurrentMap<Allele, Peak> peaks = new ConcurrentHashMap<Allele, Peak>();
	private Locus locus;
	
	public LocusData(Locus locus) 
	{
		this.locus = locus;
	}
	
	public LocusData(LocusData locusData) {
		for (Allele allele : locusData.peaks.keySet())
			peaks.put(allele, locusData.peaks.get(allele));
		locus = locusData.locus;
	}
	
	public Double getSumHeights()
	{
		double sum = 0.0;
		
		for(Peak peak : peaks.values())			
			sum += peak.getHeight();
		
		return sum;
	}
	
	public ArrayList<String> sortedAllelesByName(ArrayList<Allele> alleles) {
		ArrayList<String> sorted = new ArrayList<String>();
		ArrayList<String> alleleList = new ArrayList<String>();
		// Allele names should parse as doubles. If an allele name does not,
		// add to string list that will be used after first list.
		ArrayList<String> alleleStringList = new ArrayList<String>();
		for (Allele allele : alleles) {
			if (isDouble(allele.toString())) {
				alleleList.add(allele.toString());
			} else {
				alleleStringList.add(allele.toString());
			}
		}
		Collections.sort(alleleList, new Comparator<String>() 
		{
		     @Override
		     public int compare(String a, String b) {

		       return Double.valueOf(a).compareTo(Double.valueOf(b));
		      }
		 });
		Collections.sort(alleleStringList);
		for (int i = 0; i < alleleList.size(); i++) {
			sorted.add(alleleList.get(i));
		}
		for (int j = 0; j < alleleStringList.size(); j++) {
			sorted.add(alleleStringList.get(j));
			
		}
		return sorted;
	}
	
	private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;

    }

	public ConcurrentMap<Allele, Peak> getPeaks() {
		return peaks;
	}

	public Locus getLocus() {
		return locus;
	}

	public double getMeanSize(Kit kit) {
		double numer = 0.0;
		double denom = 0.0;
		
		for(Peak peak : peaks.values()) {
			numer += kit.calcFragmentSize(locus, peak.getAllele()) * peak.getHeight();
			denom += peak.getHeight();
		}
		
		return numer / denom;
	}
}
