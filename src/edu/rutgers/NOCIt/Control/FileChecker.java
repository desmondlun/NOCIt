/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.NOCIt.Control;

import static edu.rutgers.NOCIt.UIMain.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.opencsv.CSVReader;

import edu.rutgers.NOCIt.Data.CSVModule;

/**
 *
 * @author rob
 * @author James Kelley
 */
public class FileChecker {
	private static boolean validBinsFile = false;
	
    public static boolean isValidFrequencyFile(String filePath) {
        String extension = getFileExtension(filePath);
        
        if (!extension.toLowerCase().equals("csv")) {
            return false;
        }
        
        try {
            CSVReader csvreader = new CSVReader( new FileReader(filePath) );
            List< String[] > entries = csvreader.readAll();
            csvreader.close();	

            String[] row = entries.get(0);

            if (row[0].equals("Locus") && row[1].equals("Allele") && row[2].equals("Frequency")) {
                
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }
    
    public static boolean isValidSampleFile(String filePath) {
        String extension = getFileExtension(filePath);
        
        if (!extension.toLowerCase().equals("csv")) {
            return false;
        }
        
        try {
            //File file = new File(filePath);
            CSVReader csvreader = new CSVReader( new FileReader(filePath) );
            List< String[] > entries = csvreader.readAll();
            csvreader.close();	

            String[] row = entries.get(0);

            if (row.length >=6) {
            	java.util.List<String> rowList = Arrays.asList(row);
            	// Look for occurrence of first columns. If not found
            	// file is not valid.
            	if (containsColumns(CSVModule.REQ_AUX_COLUMNS, rowList)) {
                	// Check that there are equal numbers of each three fields.
                	ArrayList< Integer > alleleIdxs = CSVModule.findAll( entries.get( 0 ), "Allele" );
    				ArrayList< Integer > heightIdxs = CSVModule.findAll( entries.get( 0 ), "Height" );
    				ArrayList< Integer > sizeIdxs = CSVModule.findAll( entries.get( 0 ), "Size" );
                	if (alleleIdxs.size() == heightIdxs.size() && heightIdxs.size() == sizeIdxs.size()) {
                		
                	} else {
                		return false;
                	}
                } else {
                	return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }
    
    public static boolean isValidCalibrationProjectFile(String filePath) {
        String extension = getFileExtension(filePath);
        
        if (!extension.toLowerCase().equals("zip")) {
            return false;
        }
        
        try {
            ZipFile zipFile = new ZipFile(filePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            int count = 0;
            while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();

                    if (entry.getName().endsWith(".values1") || entry.getName().endsWith(".values2") || entry.getName().endsWith(".values3") || entry.getName().endsWith(".values4")) {
                        count++;
                    }   			
            } 
            zipFile.close();
            
            if (count < 4) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    public static boolean isValidBinsFile(String filePath) {
        String extension = getFileExtension(filePath);
        
        if (!extension.toLowerCase().equals("txt")) {
            return false;
        } else {
        	try {
				checkIfBinsFileValid(new File(filePath));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				logger.error("Not a valid bins file.", e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				logger.error("Not a valid bins file.", e);
			}
        }
		return validBinsFile;
       
    }
    
    private static String getFileExtension(String filePath) {
        String extension = "";

        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i+1);
        }
        
        return extension;
    }
    
    private static boolean containsColumns( String[] columnNames, java.util.List<String> rowList ) {
    	for (int i = 0; i < columnNames.length; i++) {
    		if (!rowList.contains(columnNames[0])) {
        		return false;
        	}
    	}
    	
    	
		return true;
    	
    }
    
    private static void checkIfBinsFileValid(File binsFile) throws ParseException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(binsFile));

		validBinsFile = false;
		
		String line;
		while ((line = reader.readLine()) != null) {
			String[] elems = line.split("\t");
			// only accept new style bins files with Dyes present
			if (line.startsWith("Marker Name")) {
				if (elems.length > 3) {
					validBinsFile = true;
				}
			}
		}
		reader.close();

	}
}
