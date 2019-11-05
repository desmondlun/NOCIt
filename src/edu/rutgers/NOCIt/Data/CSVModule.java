package edu.rutgers.NOCIt.Data;

import static edu.rutgers.NOCIt.UIMain.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.core.util.Integers;

import com.opencsv.CSVReader;

import edu.rutgers.NOCIt.Control.Settings;

/**
 * Class for reading in a CSV file generated by GeneMapper that contains one or
 * more samples.
 * 
 * @author Tony Lane
 * @author Desmond Lun
 * @author James Kelley
 */

public class CSVModule {
	/** The required auxiliary columns. */
	public static final String[] REQ_AUX_COLUMNS = { "Sample File", "Marker" };

	/** Number of allele peaks to write in filtered output files. */
	private static final int NUMBER_OF_ALLELE_PEAKS = 100;

	/** The file name. */
	private String fileName;

	/** The samples. */
	private Map<String, Sample> samples = new LinkedHashMap<String, Sample>();

	/** Flag indicating whether the file is valid. */
	private boolean validFile = true;
	
	public CSVModule() { };

	/**
	 * Instantiates a new CSV module.
	 *
	 * @param file
	 *            the file
	 * @param kit
	 *            the kit
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	public CSVModule(File file, Kit kit) throws FileNotFoundException {
		this(file.getName(), new FileReader(file), kit);
	}

	/**
	 * Instantiates a new CSV module.
	 *
	 * @param fileName
	 *            the file name
	 * @param reader
	 *            the input stream reader
	 * @param kit
	 *            the kit
	 */
	public CSVModule(String fileName, Reader reader, Kit kit) {
		this.fileName = fileName;

		try {
			if (fileName.endsWith(".txt")) {
				try {
					CSVReader csvreader = new CSVReader(reader, '\t');
					List<String[]> entries = csvreader.readAll();
					csvreader.close();
					processFile(kit, REQ_AUX_COLUMNS, entries);
				} catch (Exception e) {
					validFile = false;
					// e.printStackTrace();
					logger.error("Error reading sample file", e);
				}
			} else {
				try {
					CSVReader csvreader = new CSVReader(reader);
					List<String[]> entries = csvreader.readAll();
					csvreader.close();
					// will work for kit files with Dye data
					processFile(kit, REQ_AUX_COLUMNS, entries);
				} catch (Exception e) {
					validFile = false;
					// e.printStackTrace();
					logger.error("Error reading sample file", e);
				}
			}

			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Copy constructor.
	 *
	 * @param csvModule
	 *            the CSV module to copy
	 */
	public CSVModule(CSVModule csvModule) {
		fileName = csvModule.fileName;
		for (String sampleID : csvModule.samples.keySet()) {
			samples.put(sampleID, new Sample(csvModule.samples.get(sampleID)));
		}
	}

	/**
	 * Finds all instances where a given word appears as a substring in an array
	 * of strings.
	 *
	 * @param strings
	 *            array of strings from file
	 * @param word
	 *            string to be located
	 * @return List of indices in strings where word appears
	 */
	public static ArrayList<Integer> findAll(String[] strings, String word) {
		ArrayList<Integer> result = new ArrayList<Integer>();

		for (int i = 0; i < strings.length; ++i) {
			if (strings[i].contains(word))
				result.add(i);
		}

		return result;
	}

	/**
	 * Processes entries from CSV file, extracting the peaks, marker
	 * information, and sample name, and creating Sample instances to store the
	 * data.
	 *
	 * @param kit
	 *            the kit
	 * @param auxColumnNames
	 *            the auxiliary column names
	 * @param entries
	 *            the entries of the CSV file
	 */
	private void processFile(Kit kit, String[] auxColumnNames, List<String[]> entries) {
		if (entries.size() > 0 && entries.get(0).length > 0 && containsColumns(auxColumnNames, entries)) {
			int sampleFileIdx = findAll(entries.get(0), "Sample File").get(0);
			int dyeIdx = -1;
			if (Arrays.asList(auxColumnNames).contains("Dye")) {
				dyeIdx = findAll(entries.get(0), "Dye").get(0);
			}
			int markerIdx = findAll(entries.get(0), "Marker").get(0);
			ArrayList<Integer> alleleIdxs = findAll(entries.get(0), "Allele");
			ArrayList<Integer> heightIdxs = findAll(entries.get(0), "Height");
			ArrayList<Integer> sizeIdxs = findAll(entries.get(0), "Size");

			for (int it = 1; it < entries.size(); ++it) {
				ArrayList<String> alleleNames = new ArrayList<String>();
				ArrayList<Integer> heights = new ArrayList<Integer>();
				ArrayList<Double> sizes = new ArrayList<Double>();

				String[] row = entries.get(it);

				// Ignore loci not in kit.
				// Note: This may affect pull-up filtering.
				if (kit.getLocusMap().containsKey(row[markerIdx])) {
					// Fill the LocusData structure
					LocusData locusData = new LocusData(new Locus(row[markerIdx]));					

					for (int idx : alleleIdxs)
						if (idx < row.length && !row[idx].isEmpty())
							alleleNames.add(row[idx]);

					for (int idx : heightIdxs)
						if (idx < row.length && !row[idx].isEmpty())
							heights.add(Integers.parseInt(row[idx]));

					for (int idx : sizeIdxs)
						if (idx < row.length && !row[idx].isEmpty())
							sizes.add(Double.parseDouble(row[idx]));

					int is = 0;
					for (String alleleName : alleleNames) {
						try {
							Allele allele;
							if (locusData.getLocus().isAMEL())
								allele = new AMELAllele(alleleName);
							else
								allele = new STRAllele(alleleName);

							Peak peak = new Peak(allele, sizes.get(is), heights.get(is));

							// Take larger of repeated peaks
							if (!locusData.getPeaks().containsKey(allele)
									|| locusData.getPeaks().get(allele).getHeight() < peak.getHeight())
								locusData.getPeaks().put(allele, peak);
						} catch (IllegalArgumentException e) {
						}

						is++;
					}

					String sampleName = row[sampleFileIdx];
					String dye = kit.getLocusMap().get(row[markerIdx]).getDye();
					if (dyeIdx > -1) {
						dye = row[dyeIdx];
					}

					if (!samples.containsKey(sampleName)) {
						samples.put(sampleName, new Sample());
					}

					samples.get(sampleName).addData(dye, locusData);
				}
			}
		}
	}
	
	public void writeSamples(String fileName) {
		writeSamples(fileName, new ArrayList<String>(samples.keySet()));
	}

	/**
	 * Write samples to file.
	 *
	 * @param sampleNames
	 *            list of samples to write
	 */
	public void writeSamples(String fileName, List<String> sampleNames) {
		ArrayList<String[]> entries = new ArrayList<String[]>();

		String line = "";
		Collections.sort(sampleNames);
		for (int i = 0; i < sampleNames.size(); i++) {
			if (samples.containsKey(sampleNames.get(i))) {
				Sample sample = samples.get(sampleNames.get(i));
				// use for testing and if it is decided that filtering
				// samples if calculations
				// have not been run when saving is a good idea
				// if (!samples.get(sampleNames.get(i)).isFiltered()) {
				// sampleCopy.filter();
				// }
				for (Locus locus : sample.getLoci()) {
					line += sampleNames.get(i) + "\t";
					line += locus.toString() + "\t";
					if (locus.getDye() != null)
						line += locus.getDye().substring(0, 1);
					line += "\t";
					LocusData locusData = sample.getLociData().get(locus);
					ArrayList<Allele> alleleList = new ArrayList<Allele>(locusData.getPeaks().keySet());
					ArrayList<String> sortedAlleleNames = locusData.sortedAllelesByName(alleleList);
					for (int j = 0; j < sortedAlleleNames.size(); j++) {
						for (Allele allele : locusData.getPeaks().keySet()) {
							if (allele.toString().equals(sortedAlleleNames.get(j))) {
								Peak peak = locusData.getPeaks().get(allele);
								line += peak.getAllele() + "\t";
								line += peak.getSize() + "\t";
								line += peak.getHeight() + "\t";
							}
						}
					}
					String[] entry = line.substring(0, line.length() - 1).split("\t");
					entries.add(entry);
					line = "";
				}
			}
		}

		// Add "filtered_" to filename since Excel will not allow two files of
		// same name to be
		// opened at same time even in different directories. Filtered files
		// need to be compared
		// to unfiltered files so both should be able to be opened at same time.
		CSVFileWriter.write(fileName, makeHeader(), entries);
	}

	/**
	 * Return header to use for written files.
	 *
	 * @return the string
	 */
	private String makeHeader() {
		String headerNames = "";
		headerNames += "Sample File" + "\t";
		headerNames += "Marker" + "\t";
		headerNames += "Dye" + "\t";
		for (int j = 1; j <= NUMBER_OF_ALLELE_PEAKS; j++) {
			headerNames += "Allele " + j + "\t";
			headerNames += "Size " + j + "\t";
			headerNames += "Height " + j + "\t";
		}
		return headerNames;
	}

	/**
	 * Checks to see if column names are in the first row of entries.
	 *
	 * @param columnNames
	 *            the column names
	 * @param entries
	 *            the entries
	 * @return true, if column names are all present; false, otherwise
	 */
	private boolean containsColumns(String[] columnNames, List<String[]> entries) {
		for (int i = 0; i < columnNames.length; i++) {
			if (findAll(entries.get(0), columnNames[i]).size() == 0) {
				return false;
			}
		}

		return true;

	}

	/**
	 * Matches sample IDs with genotype IDs. A substring of from the sample IDs
	 * is extracted by separating the ID by delimiters and picking out a certain
	 * column (specified in Settings). The genotype ID with the longest common
	 * substring with this substring from the sample ID is matched. If there are
	 * two matches of the same length, the one that occurs earlier in the
	 * substring from the sample ID is preferred.
	 *
	 * @param genotypes
	 *            the genotypes
	 * @return map of sample IDs and their corresponding genotype ID matches
	 */
	public HashMap<String, String> matchGenotypes(Map<String, Genotype> genotypes) {
		HashMap<String, String> genotypeIDs = new HashMap<String, String>();

		for (String sampleID : samples.keySet()) {
			String str = sampleID;
			if (Settings.genotypeIDIndex > 0 && !Settings.sampleIDDelimiter.isEmpty())
				str = sampleID.split(Settings.sampleIDDelimiter)[Settings.genotypeIDIndex - 1];

			// Find longest match
			String bestGenotypeID = "";
			for (String genotypeID : genotypes.keySet()) {
				if (str.contains(genotypeID) && (genotypeID.length() > bestGenotypeID.length()
						|| (genotypeID.length() == bestGenotypeID.length()
								&& str.indexOf(genotypeID) < str.indexOf(bestGenotypeID))))
					bestGenotypeID = genotypeID;
			}

			if (bestGenotypeID.isEmpty()) {
				System.out.println("Sample " + sampleID + " could not be matched to a genotype.");
			} else {
				samples.get(sampleID).setGenotype(genotypes.get(bestGenotypeID));
				genotypeIDs.put(sampleID, bestGenotypeID);
			}
		}

		return genotypeIDs;
	}	

	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Gets the samples.
	 *
	 * @return the samples
	 */
	public Map<String, Sample> getSamples() {
		return samples;
	}

	/**
	 * Gets the sample names.
	 *
	 * @return the sample names
	 */
	public List<String> getSampleNames() {
		return new ArrayList<String>(samples.keySet());
	}

	/**
	 * Checks if is valid file.
	 *
	 * @return true, if is valid file
	 */
	public boolean isValidFile() {
		return validFile;
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		String sampleFilePath = "/Users/dslun/Dropbox/MatchQuest/NOCIt test files for SlimJim/IP RD14 10s Input Files/01_CF_RD14 IP_10s_GM_U/RD14-0003(011816CMG_10sec)_GM_U_1P.csv";
		// String sampleFilePath = "etc/IP/testing_samples/1p_10s/1.csv";
		String binsFilePath = "etc/Identifiler_Plus_Bins_v1X - ColorDesignation.txt";
		String filteredFilePath = "filtered.csv";
		String filterSampleName = "B01_RD14-0003-23d2a-0.5IP-Q1.0_002.10sec.fsa";

		try {
			// Kit kit = new Kit("Identifiler Plus");
			Kit kit = new Kit(binsFilePath);
			CSVModule mod = new CSVModule(new File(sampleFilePath), kit);

			Sample sample = mod.getSamples().get(filterSampleName);
			sample.filter(kit);
			sample.writeFilteredSampleFile(filteredFilePath, filterSampleName);

			final long startTime = System.currentTimeMillis();
			HashMap<String, HashMap<Locus, ArrayList<Double>>> quantParams = new HashMap<String, HashMap<Locus, ArrayList<Double>>>();

			for (Entry<String, Sample> entry : mod.getSamples().entrySet()) {
				entry.getValue().calcQuantParams(kit);
				quantParams.put(entry.getKey(), entry.getValue().getQuantParams());
			}
			final long endTime = System.currentTimeMillis();
			System.out.println("Regression execution time: " + (endTime - startTime) + " ms");

			System.out.println(quantParams.keySet());
			Set<String> keys = quantParams.keySet();

			for (String key : keys) {
				System.out.println(
						"key of main HashMap: " + key + "   innerHashMap keys: " + quantParams.get(key).keySet());
				Set<Locus> innerKeyset = quantParams.get(key).keySet();

				for (Locus innerKey : innerKeyset) {
					System.out.println("key: " + innerKey + "  regression Values : "
							+ quantParams.get(key).get(innerKey).toString());
				}
			}
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
