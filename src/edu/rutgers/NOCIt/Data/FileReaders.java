package edu.rutgers.NOCIt.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.opencsv.CSVReader;

import edu.rutgers.NOCIt.UIMain;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Class of file reading methods.
 * 
 * 
 */
public class FileReaders {

	public static TreeMap<String, Genotype> createGenotypesMap(String path) {
		TreeMap<String, Genotype> genotypes = new TreeMap<String, Genotype>();
		try {
			// Load genotypes
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			CSVReader csvReader = new CSVReader(br);
		
			String[] headers = csvReader.readNext();

			String[] row;
			while ((row = csvReader.readNext()) != null) {
				Genotype genotype = new Genotype(row[0]);
				for (int i = 1; i < row.length; i++) {
					Locus locus = new Locus(headers[i].replace("\"", ""));

					String cell = row[i];
					cell = cell.replace("\"", "");

					if (cell.contains(",")) {
						String[] s_alleles = cell.split(",");
						Allele[] alleles = new Allele[2];
						if (locus.isAMEL()) {
							alleles[0] = new AMELAllele(s_alleles[0]);
							alleles[1] = new AMELAllele(s_alleles[1]);
						}	
						else {
							alleles[0] = new STRAllele(s_alleles[0]);
							alleles[1] = new STRAllele(s_alleles[1]);
						}

						genotype.putAlleles(locus, alleles);						
					}
				}				

				if (!genotype.isEmpty()) {
					genotypes.put(row[0], genotype);
				}
				//System.out.println(genotype.getLocusValues());
			}
			csvReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR, Constants.VALIDATE_GENOTYPE_FILE_IMPORT_ERROR_MESSAGE);
			alert.showAndWait();
			UIMain.logger.error(Constants.VALIDATE_GENOTYPE_FILE_IMPORT_ERROR_MESSAGE, e);
		}
		return genotypes;
	}

	public static ArrayList<CSVModule> createCSVModuleList(List<File> fileList, Kit kit, Map<String, Genotype> genotypes) {
		ArrayList<CSVModule> csvModuleList = new ArrayList<CSVModule>();		
		for (File f : fileList) {
			// list of all files, and only read if the file is of type .csv
			if (f.getName().endsWith(".csv")) {
				CSVModule csvModule;
				try {
					csvModule = new CSVModule(f, kit);
					if (genotypes != null)
						csvModule.matchGenotypes(genotypes);				
					//					csvModule.matchDNAMasses();

					csvModuleList.add(csvModule);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
		return csvModuleList;
	}


}
