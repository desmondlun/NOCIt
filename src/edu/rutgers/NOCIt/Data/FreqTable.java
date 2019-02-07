package edu.rutgers.NOCIt.Data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.opencsv.CSVReader;

import edu.rutgers.NOCIt.Control.Settings;

/**
 * Creates a frequency hashmap from the frequency table file. The file is a csv
 * file: Locus,Allele,Frequency. First line are headers.
 * @author Anurag Arnold
 * @author Abhishek Garg
 * @author Desmond Lun
 */
public class FreqTable {
	private int maxHashCode;
	private HashMap<Locus, HashMap<STRAllele, Double>> probDists = new HashMap<>();
	private HashMap<Locus, HashMap<STRAllele, Integer>> freqDists = new LinkedHashMap<>(); 
	private String name;
	private String filePath;
	private int numPeople;

	public static void main(String[] args) {
		try {
			Kit kit = new Kit("etc/Identifiler_Plus_Bins_v1X.txt");
			CSVModule csvModule = new CSVModule(new File("etc/testing_samples/1p_10s/1.csv"), kit);
			FreqTable freqTable = new FreqTable("etc/ABI IP AA freq_COUNTS n=357.csv",
					"African American", 357);
			for (Sample sample : csvModule.getSamples().values()) {
				freqTable.calcProbDists(kit, sample);

				for (Locus locus : freqTable.getLoci()) 
					System.out.println(locus + ": " + freqTable.getProbDists().get(locus));
			}
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FreqTable() {};
	
	public FreqTable(String filePath, String popName, int numPeople) throws IOException {
		this.name = popName;
		this.filePath = filePath;
		this.numPeople = numPeople;

		File file = new File(filePath);
		CSVReader csvreader = new CSVReader( new FileReader(file) );
		List< String[] > entries = csvreader.readAll();
		csvreader.close();	

		for (int i = 0; i < entries.size(); i++) {
			String[] row = entries.get(i);

			if (row[0].equals("Locus"))
				continue;

			Locus locus = new Locus(row[0]);
			STRAllele allele = new STRAllele(row[1]);
			int numCounts;
			if (!row[2].isEmpty()) {
				numCounts = (int) Math.round(Double.parseDouble(row[2]) / 100 * numPeople * 2);
			}
			else
				numCounts = 0;

			if (!freqDists.containsKey(locus))
				freqDists.put(locus, new HashMap<>());
			freqDists.get(locus).put(allele, numCounts);
		}
	}
	
	public void calcProbDists(Kit kit, Sample sample) {
		maxHashCode = 0;
		for (Locus locus : freqDists.keySet()) {
			HashMap<STRAllele, Integer> freqDist = new HashMap<>(freqDists.get(locus));
				
			if (kit != null)
				for (Allele allele : kit.getAlleles(locus))
					if (!freqDist.containsKey(allele))
						freqDist.put((STRAllele) allele, 0);
			
			if (sample != null && sample.getLociData().containsKey(locus))
				for (Allele allele : sample.getLociData().get(locus).getPeaks().keySet())
					if (!freqDist.containsKey(allele))
						freqDist.put((STRAllele) allele, 0);
			
			int totalFreq = 0;
			for (STRAllele allele : freqDist.keySet()) 
				totalFreq += freqDist.get(allele);
			
			HashMap<STRAllele, Double> probDist = new HashMap<>();
			for (STRAllele allele : freqDist.keySet()) {
				probDist.put(allele, (freqDist.get(allele) + 1.0 / freqDist.size()) / (totalFreq + 1.0));
				
				if (allele.fStutterAllele().hashCode() > maxHashCode)
					maxHashCode = allele.fStutterAllele().hashCode();
			}
			
			probDists.put(locus, probDist);
		}
	}
	
	public Collection<Locus> getLoci() {
		return freqDists.keySet();
	}

	public HashMap<Locus, HashMap<STRAllele, Double>> getProbDists() {
		return this.probDists;
	}
        
	public HashMap<Locus, HashMap<STRAllele, Integer>> getFreqDists() {
		return this.freqDists;
	}

	public int getMaxHashCode() {
		return maxHashCode;
	}

	public String getName() {
		return this.name;
	}

	public String getFilePath() {
		return this.filePath;
	}
	
	public int getNumPeople() {
		return numPeople;
	}
}
