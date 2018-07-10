package edu.rutgers.NOCIt.Data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

// Partly based on http://stackoverflow.com/questions/17837009/create-csv-file-using-java
// OpenCSV puts quotes around the Genotype IDs. It is possible to turn off quotes in OpenCSV
// http://stackoverflow.com/questions/13969254/unwanted-double-quotes-in-generated-csv-file
// but the resulting file either has allele pairs with no quotes or if you add quotes like
// "1,2" to resulting file gives double quotes if quotes are turned off ""1,2"".
// A text file writer does not have this problem.
/**
 * @author Rob Carpenter
 * @author James Kelley
 */
public class TextFileWriter {
	public static void write(String file, ArrayList<String> headerNames, ArrayList<ArrayList<String>> lines) {
		try {
			FileWriter writer = new FileWriter(file);

			for (int i = 0; i < headerNames.size(); i++) {
				writer.append(headerNames.get(i));
				writer.append(',');  
			}
			writer.append('\n');
					
			for (int j = 0; j < lines.size(); j++) {
				for (int k = 0; k < lines.get(j).size(); k++) {
					writer.append(lines.get(j).get(k));
					writer.append(',');
				}
				writer.append('\n');
			}

			writer.flush();
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
