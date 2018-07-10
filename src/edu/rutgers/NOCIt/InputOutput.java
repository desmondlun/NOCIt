/**
 * 
 */
/**
 * @author Wenfeng
 *
 */
package edu.rutgers.NOCIt;

import java.io.*;
import java.util.*;
//import java.util.HashMap;
import java.util.concurrent.*;
//import nocitv15_nodo1;


public class InputOutput {
	public static void main(String[] augs) throws InterruptedException, ExecutionException, IOException {
		String inputPath = "C:/workspace/NOCIt_MCMC_1/input/";
		String inputFile = inputPath + "tho_2p_test1.csv";
		OpenAndReadFile openAndReadFile = new OpenAndReadFile(inputFile);
		System.out.println("Number Of Lines = " + openAndReadFile.readLines());
//		for(String o : openAndReadFile.FileContents()) {
//			System.out.println(o);
//		}
		
//		String[] stringArray = openAndReadFile.FileContents(); 
//			//{"java", "Freq", "if", "it", "is", "to", "be"};
//		Freq freq = new Freq();
//		System.out.println(freq.printFreq(stringArray).size() + " distinct words: ");
//		System.out.println(freq.printFreq(stringArray));
		ParseSampleFile pSFObj = new ParseSampleFile(inputFile);
		
		
	}
	
	/* Parses sample file and creates hashmap with peaks and alleles at a locus. 
    OL peaks are removed. DO alleles are added.
    Sample file is a csv file: Gen info, Locus, Dye, (Allele,size,height)^p. First line is headers. */
	public static class ParseSampleFile {
		private String sample_file_path;
		private HashMap<String,HashMap<Integer,Peak>> loci_peaks;
		private HashMap<String,ArrayList<Integer>> loci_peakalleles;
		private ArrayList<String> sample_loci;
		private double amp;
		ParseSampleFile(String samplefilepath){
			sample_file_path = samplefilepath;
			loci_peaks = new HashMap<>();
			loci_peakalleles = new HashMap<>();
			sample_loci = new ArrayList<>();
			try {
				OpenAndReadFile samplefile = new OpenAndReadFile(sample_file_path);
				String[] samplefilelines = samplefile.FileContents();
				for(int linecount =1; linecount<samplefilelines.length;linecount++) {
					String line = samplefilelines[linecount];
					String[] line_parts = line.split(",");
					List<String> line_refresh = new ArrayList<>(); //Remove null in line_parts and instore into line_refresh
					for(String element:line_parts) {
						if(element != null) {
							line_refresh.add(element);
						}
					}
					System.out.println(line_refresh);
					
				}
				
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private static class FreqTable {
		private String path;
		private ArrayList<String> loci_list;
		private LinkedHashMap<String,LinkedHashMap<Integer,Double>> freq_table;
		private HashMap<String,ArrayList<Integer>> allele_list;
		
		FreqTable(String file_path) {
			path=file_path;
			loci_list = new ArrayList<>();
			freq_table = new LinkedHashMap<>();
			allele_list = new HashMap<>();
		
			try {
				OpenAndReadFile freqfile = new OpenAndReadFile(path);
				String[] fileLines = freqfile.FileContents();
				for (int i=1; i<fileLines.length; i++) {
					String line = fileLines[i];
					String[] parts = line.split("\t");
					String locus = parts[0];
					if(!(loci_list.contains(locus))) {
						loci_list.add(locus);
					}
				}
				for(String locus:loci_list) {
					freq_table.put(locus,new LinkedHashMap<Integer,Double>() );
					allele_list.put(locus,new ArrayList<Integer>());
				}
				for (int i=1; i<fileLines.length; i++) {
					String line = fileLines[i];
					String[] parts = line.split("\t");
					String locus = parts[0];
					double a = Double.parseDouble(parts[1]);
					int allele = (int) Math.round(a*10);
					double frequency = Double.parseDouble(parts[2]);
					if (Loci_Peaks.containsKey(locus) && Loci_Peaks.get(locus).containsKey(allele) ) {
						freq_table.get(locus).put(allele,frequency);
						allele_list.get(locus).add(allele);
					}	
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		public ArrayList<String> getFreqLociList() {
			return loci_list;
		}
		public LinkedHashMap<String,LinkedHashMap<Integer,Double>> getFreqTable() {
			return freq_table;
		}
		public HashMap<String,ArrayList<Integer>> getAlleleList() {
			return allele_list;
		}
	}
	
	/* Creates a peak object with 2 characteristics: allele (double) and height (int).
    Used for all loci except AMEL. */
	private static class Peak { 
		private final int allele;
		private final int height;
		Peak(int assignAllele, int assignHeight) {
			allele = assignAllele;
			height = assignHeight;
		}
		public int getAllele() {
			return allele;
		}
		public int getHeight() {
			return height;
		}
		@Override
		public String toString() {
			return(allele + "," + height);
		}
	}
	
	public static class Freq {
		public String[] stringArray;
	//	public Freq( HashMap<String, Integer> hashMap) {
	//		this.hashMap = hashMap;
	//	}
		public HashMap<String, Integer> printFreq (String[] stringArray) {
			HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
			for(String word:stringArray) {
				Integer freq = hashMap.get(word);
				hashMap.put(word, (freq==null) ? 1 : freq+1);
			}
			return hashMap;
		}
		
		
	}
	public static class OpenAndReadFile {
		private final String path;
		public OpenAndReadFile (String path) {
			this.path = path;
		}
		public String[] FileContents() throws IOException {
			FileReader fr = new FileReader(path);
			String[] textData;
			try (BufferedReader textReader = new BufferedReader(fr)) {
				int numberOfLines = readLines();
				textData = new String[numberOfLines];
				for(int i=0; i<numberOfLines; i++) {
					textData[i] = textReader.readLine();
				}
			}
			fr.close();
			return textData;
		}
		public int readLines() throws IOException {
			FileReader fileToRead = new FileReader(path);
			int numberOfLines;
			try (BufferedReader bf = new BufferedReader(fileToRead)) {
				numberOfLines = 0;
				while(bf.readLine() != null) {
					numberOfLines++;
				}
			}
			fileToRead.close();
			return numberOfLines;
		}		
		
	}
	
	private static HashMap<String, HashMap<Integer,Peak>> Loci_Peaks = new HashMap<>();


}