/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.NOCIt;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.Locus;
import edu.rutgers.NOCIt.Data.UtilityMethods;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Rob Carpenter
 * @author James Kelley
 * @author Desmond Lun
 */
public class EditAnalyticalThresholdsController implements Initializable {
    @FXML
    private TableView<ObservableList<?>> table;
    private Stage stage;
    private UIController uiController;
    private int rowIndex; 
    private LinkedHashMap<Locus, Integer> currentThresholdData;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void setStage(Stage stage, UIController controller, int rowIndex) {
        this.stage = stage;
        this.uiController = controller;    
        this.rowIndex = rowIndex;
    }
    
    /**
     * Populates analytical thresholds table using data from a hash map with loci as
     * keys and analytical thresholds as integer values.
     * @param data
     */
    public void populateTable(LinkedHashMap<Locus, Integer> data) {
        currentThresholdData = data;
        UtilityMethods.populateThresholdsTable(data, table);
    }

    @FXML
    private void saveThresholds(ActionEvent event) {
        for (ObservableList<?> row : this.table.getItems()) {
            String locusName = ((StringProperty)row.get(0)).getValue();
            String value = ((StringProperty)row.get(1)).getValue();
                        
            for (Locus locus : currentThresholdData.keySet()) {
                if (locus.getName().equals(locusName)) {
                    currentThresholdData.put(locus, Integer.parseInt(value));
                    break;
                }
            }      
        }
        
        if (uiController.getTabPane().getSelectionModel().getSelectedIndex() == Constants.NOCIT_TAB_INDEX) {
        	ObservableList rowList = 
    		        (ObservableList) uiController.nocItTable.getItems().get(this.rowIndex);
            int rowID = ((SimpleIntegerProperty) rowList.get(Constants.NOCIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
            this.uiController.rowIDAnalyticalThresholdsMap.put(Integer.toString(rowID), currentThresholdData);
            
            ((SimpleStringProperty)this.uiController.nocItTable.getItems().get(this.rowIndex).get(Constants.NOCIT_TABLE_COLUMN_THRESHOLDS_INDEX)).setValue("");
        } else if (uiController.getTabPane().getSelectionModel().getSelectedIndex() == Constants.NOCIT_TAB_INDEX) {
        	ObservableList rowList = 
    		        (ObservableList) uiController.ceesItTable.getItems().get(this.rowIndex);
            int rowID = ((SimpleIntegerProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
            this.uiController.ceesItRowIDAnalyticalThresholdsMap.put(Integer.toString(rowID), currentThresholdData);
        }
        
        this.stage.close();
    }
    
}
