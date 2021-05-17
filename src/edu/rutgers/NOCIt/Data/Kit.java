package edu.rutgers.NOCIt.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class Kit implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private class LinFunction implements ParametricUnivariateFunction {

		@Override
		public double[] gradient(double x, double[] parameters) {
			double aGrad = 1;
			double bGrad = x;
			double[] grad = new double[]{aGrad, bGrad};
			return grad;
		}

		@Override
		public double value(double x, double[] parameters) {
			double a = parameters[0];
			double b = parameters[1];
			return a + b * x;
		}
	}
	
	private HashMap<Locus, Double> offsets = new HashMap<>();
	private HashMap<Locus, Double> repeatLengths = new HashMap<>();
	private HashMap<Locus, List<Allele>> allelesMap = new LinkedHashMap<>();
	private HashMap<String, Locus> locusMap = new LinkedHashMap<>(); 
	private String kitName;
	
	public static void main(String[] args) {		
		try {
//			Kit kit = new Kit("etc/Identifiler_Plus_Bins_v1X.txt");
//			Kit kit = new Kit("etc/GlobalFiler_Bins_v1.txt");
			Kit kit = new Kit("etc/PowerPlex_Fusion_Bins_IDX_v2.0.txt");
			System.out.println(kit.calcFragmentSize(new Locus("CSF1PO"), new STRAllele("10.3")));
			System.out.println(kit.calcFragmentSize(new Locus("AMEL"), new AMELAllele("Y")));
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readBinsFile(File binsFile) throws ParseException, IOException, IllegalArgumentException {
		BufferedReader reader = new BufferedReader(new FileReader(binsFile));
		Locus locus = null;
		ArrayList<Allele> alleles = new ArrayList<>();
		ArrayList<BigDecimal> sizes = new ArrayList<>();

		String line;
		while ((line = reader.readLine()) != null) {
			String[] elems = line.split("\t");
			if (line.startsWith("Version")) {}
			else if (line.startsWith("Chemistry Kit")) {
				kitName = elems[1];
			}
			else if (line.startsWith("BinSet Name")) {}
			else if (line.startsWith("Panel Name")) {}
			else if (line.startsWith("Marker Name")) {
				if (locus != null) 
					processLocus(locus, alleles, sizes);

				locus = new Locus(elems[1]);
				if (elems.length > 3) {
					locus.setDye(elems[3]);
				}
				alleles.clear();
				sizes.clear();
			}
			else {
				if (locus.isAMEL())
					alleles.add(new AMELAllele(elems[0]));
				else
					alleles.add(new STRAllele(elems[0]));

				sizes.add(new BigDecimal(elems[1]));
			}
		}
		reader.close();

		if (locus != null) 
			processLocus(locus, alleles, sizes);
	}

	private void processLocus(Locus locus, ArrayList<Allele> alleles, ArrayList<BigDecimal> sizes) throws ParseException {
		WeightedObservedPoints obs = new WeightedObservedPoints();
		for (int i = 0; i < alleles.size(); i++) 
			obs.add(alleles.get(i).getRepeats(), sizes.get(i).doubleValue() - alleles.get(i).getFraction());
		
		SimpleCurveFitter curveFitter = SimpleCurveFitter.create(new LinFunction(), new double[]{0.0, 0.0});
		double[] fitCoefs = curveFitter.fit(obs.toList());

		offsets.put(locus, fitCoefs[0]);
		repeatLengths.put(locus, fitCoefs[1]);
		allelesMap.put(locus, new ArrayList<>(alleles));
		locusMap.put(locus.toString(), locus);
	}

	public Kit(String filePath) throws ParseException, IOException {
		this.kitName = filePath;
		readBinsFile(new File(filePath));
	}
	
	public Collection<Locus> getLoci() {
		return allelesMap.keySet();
	}
	
	public Collection<Allele> getAlleles(Locus locus) {
		if (allelesMap.containsKey(locus))
			return allelesMap.get(locus);
		else
			return new ArrayList<Allele>();
	}
	
	public double calcFragmentSize(Locus locus, Allele allele) {
		return offsets.get(locus) + allele.getRepeats() * repeatLengths.get(locus) 
				+ allele.getFraction();
	}
	
	public double[] calcSizeRange(Locus locus) {
		double minSize = Double.POSITIVE_INFINITY;
		double maxSize = Double.NEGATIVE_INFINITY;
		
		for (Allele allele : allelesMap.get(locus)) {
			double size = calcFragmentSize(locus, allele);
			
			if (size < minSize)
				minSize = size;
			
			if (size > maxSize)
				maxSize = size;
		}
		
		return new double[] {minSize, maxSize};
	}
		
	public String getKitName() {
		return kitName;
	}

	public HashMap<String, Locus> getLocusMap() {
		return locusMap;
	}

	public double getRepeatLength(Locus locus) {
		return repeatLengths.get(locus);
	}
}
