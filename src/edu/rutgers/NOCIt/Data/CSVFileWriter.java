package edu.rutgers.NOCIt.Data;

import java.io.*;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.opencsv.CSVWriter;

/**
 * Writes a csv file using opencsv. Required inputs are file name as a String, a tab separated
 * String of header names, and an ArrayList of lines as String arrays.
 * @author jkelley
 *
 */
public class CSVFileWriter {

	public static void write(String fileName, String headerNames, ArrayList<String[]> lines) {
		CSVWriter writer;
		try {
			writer = new CSVWriter(new FileWriter(fileName));

			String[] header = headerNames.trim().split("\t");

			writer.writeNext(header);				

			for (int n = 0; n < lines.size(); n++) {
				writer.writeNext(lines.get(n));							
			}	
			writer.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,                
					"File Not Found Error.",                
					"Error",                                
					JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
		}
	}

	public static void main(String[] arg) throws Exception { 

	}
}


