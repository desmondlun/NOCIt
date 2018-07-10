/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.NOCIt;

import edu.rutgers.NOCIt.Control.Settings;
import edu.rutgers.NOCIt.Data.Kit;
import edu.rutgers.NOCIt.Data.Locus;
import edu.rutgers.NOCIt.Data.UtilityMethods;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;

import com.opencsv.CSVWriter;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 * Class contains a table where analytical thresholds can be edited for each locus
 * and a Save button to save the data entered by the user.
 *
 * @author rob
 * @author James Kelley
 */
public class AnalyticalThresholdsController implements Initializable {
    @FXML
    private TableView<ObservableList<?>> table;
    private Stage stage;
    private DrillDownNOCItController nocitController;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void setStage(Stage stage, DrillDownNOCItController controller) {
        this.stage = stage;
        this.nocitController = controller;    
    }
    
    /**
     * Populates analytical thresholds table using data from a hash map with loci as
     * keys and analytical thresholds as integer values.
     * @param data
     */
    public void populateTable(HashMap<Locus, Integer> data) {
    	UtilityMethods.populateThresholdsTable(data, table);
    }

    @FXML
    private void saveThresholds(ActionEvent event) {
        for (ObservableList<?> row : this.table.getItems()) {
            String locusName = ((StringProperty)row.get(0)).getValue();
            String value = ((StringProperty)row.get(1)).getValue();
            
            for (Locus locus : this.nocitController.currentThresholdData.keySet()) {
                if (locus.getName().equals(locusName)) {
                    this.nocitController.currentThresholdData.put(locus, Integer.parseInt(value));
                    break;
                }
            }
        }
        writeAnalyticalThresholdsToFile();

        this.stage.close();
    }
    
    /**
     * Writes a csv file to the Settings directory using the kit name as the file name. This file
     * is used to save analytical thresholds for each locus in this kit.
     */
    private void writeAnalyticalThresholdsToFile() {
    	Kit kit = nocitController.getCurrentCalibration().getKit();
    	File directory = new File(Settings.getSettingsPath() + "Saved_Analytical_Thresholds" + File.separatorChar);
    	if(!directory.exists()) {
    		directory.mkdir();
    	}
    		
        File f = new File(directory.getAbsolutePath() + File.separatorChar + UtilityMethods.cleanedUpKitName(kit.getKitName()) + ".csv");
        
        CSVWriter writer;
		try {
			writer = new CSVWriter(new FileWriter(f), ',');
			Set<Locus> keys = this.nocitController.currentThresholdData.keySet();
	        for (Locus locus : keys) {
	        	String line = "";
				line += locus.toString() + "\t";
				line += Integer.toString(this.nocitController.currentThresholdData.get(locus)) + "\t";
				String [] entries = line.substring(0, line.length() - 1).split("\t");
				writer.writeNext(entries);
	        }
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
}
