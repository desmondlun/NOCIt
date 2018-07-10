/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.NOCIt;

import edu.rutgers.NOCIt.Control.FileChecker;
import edu.rutgers.NOCIt.Control.Settings;
import edu.rutgers.NOCIt.Data.CSVFileWriter;
import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.FreqTable;
import edu.rutgers.NOCIt.Data.Locus;
import edu.rutgers.NOCIt.Data.STRAllele;
import edu.rutgers.NOCIt.Data.UtilityMethods;

import static edu.rutgers.NOCIt.UIMain.logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * FXML Controller class
 * Class contains controls where required inputs to create populations can be loaded, 
 * entered, and viewed. These fields include population name, frequency file, and number
 * of people. The Add Population button validates input in these fields and adds the
 * population to a scroll pane and saves the population. Populations can be removed 
 * from this pane. OK button checks if any populations have been entered but not saved
 * and closes the dialog.
 *
 * @author rob
 * @author James Kelley
 */
public class PopulationManagerController implements Initializable {
    private Stage stage;
    private UIController uiController;
    @FXML
    private TextField frequencyFileName;
    @FXML
    private TextField numberOfPeople;
    @FXML
    private VBox frequencies;
    @FXML
    private TextField frequencyName;
    @FXML
    private Button browseButton;
    @FXML
    private Button okButton;
    
    private String frequencyFilePath;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	addTextFieldFocusListener(frequencyName);
    	frequencyFileName.setEditable(false);
    	addTextFieldFocusListener(numberOfPeople);
    }    
    
    public void setStage(Stage stage, UIController controller) {
        this.stage = stage;
        this.uiController = controller;
        
        for (int i = 0; i < this.uiController.populationNamesList.size(); i++) {
        	this.displayNewFrequency(this.uiController.populations.get(this.uiController.populationNamesList.get(i)));
        }
    }

    @FXML
    private void chooseFrequencyFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Constants.SELECT_FILE);
        // get last save path from settings
        String lastFrequencyPath = Settings.defaultDirectory;
        if (Settings.lastFrequencyPath != null) {
        	lastFrequencyPath = Settings.lastFrequencyPath;
        }
        uiController.setFileChooserInitialDirectory(fileChooser, lastFrequencyPath);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(Constants.FREQ_FILE_EXTENSION_1, Constants.FREQ_FILE_EXTENSION_2));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            if (!FileChecker.isValidFrequencyFile(file.getAbsolutePath())) {
                    UIController.displayErrorDialog("File Validation", "This is not a valid Frequency file: \r\n\r\n" + file.getAbsolutePath());
                    return;
                }
            Settings.lastFrequencyPath = file.getParent();
            Settings.save();

            frequencyFileName.setText(file.getName());
            frequencyFileName.setTooltip(new Tooltip(file.getName()));
            frequencyFilePath = file.getAbsolutePath();
        }
    }

    @FXML
    private void addNewFrequency(ActionEvent event) {
        if (this.checkFormErrors()) {
            int _numberOfPeople = Integer.parseInt(numberOfPeople.getText());
            
            if (!this.uiController.populations.containsKey(this.frequencyName.getText())) {
            	try {
            		FreqTable frequency = new FreqTable(frequencyFilePath, this.frequencyName.getText(), _numberOfPeople);

            		this.uiController.populationNamesList.add(this.frequencyName.getText());
            		this.uiController.populations.put(this.frequencyName.getText(), frequency);             		
            		this.displayNewFrequency(frequency);
            		writeFrequenciesFile();

            		frequencyName.setText("");
            		frequencyFileName.setText("");
            		frequencyFileName.setTooltip(new Tooltip(""));
            		numberOfPeople.setText("");
            	} catch (Exception e) {
            		UIController.displayErrorDialog("Error", "");
            		//e.printStackTrace();
            		logger.error(Constants.OPEN_LOGS_ERROR_LOG_MESSAGE, e);
            	}
            } else {
                String existingName = this.uiController.populations.get(this.frequencyName.getText()).getName();
                UIController.displayErrorDialog("Error", Constants.DUPLICATE_POPULATION_NAME_PREFIX + existingName + Constants.DUPLICATE_POPULATION_NAME_SUFFIX);
                // Returns focus to cell where invalid value was entered
				frequencyName.requestFocus();
            }
        }
    }
    
    public void displayNewFrequency(FreqTable frequency) {     
        AnchorPane pane = new AnchorPane();
        Button removeButton = new Button("X");
        Button viewButton = new Button("View");
        removeButton.setTooltip(new Tooltip("Delete population"));
        viewButton.setTooltip(new Tooltip("View data imported from the Allele Frequency File"));

        TextField text = new TextField();
        text.setText(frequency.getName());
        text.setTooltip(new Tooltip(UtilityMethods.createPopulationTooltip(frequency)));
        text.setEditable(false);

        pane.getChildren().add(removeButton);
        pane.getChildren().add(text);
        pane.getChildren().add(viewButton);
        
        AnchorPane.setLeftAnchor(removeButton, 10.0);
        AnchorPane.setLeftAnchor(text, 45.0);
        AnchorPane.setRightAnchor(text, 70.0);
        AnchorPane.setRightAnchor(viewButton, 10.0);

        Pane spacerPane = new Pane();
        spacerPane.setMaxHeight(5);
        spacerPane.setMinHeight(5);

        this.frequencies.getChildren().add(spacerPane);
        this.frequencies.getChildren().add(pane);

        removeButton.setOnAction(event -> {
            this.frequencies.getChildren().remove(spacerPane);
            this.frequencies.getChildren().remove(pane);
            this.uiController.populations.remove(frequency.getName());
            this.uiController.populationNamesList.remove(this.uiController.populationNamesList.indexOf(frequency.getName()));
            // Unsure if this does anything
            this.uiController.populations.remove(frequency.getFilePath());            
            writeFrequenciesFile();
        });
        
        viewButton.setOnAction(event -> {
            this.viewPopulationPopUp(frequency);
        });
    }

    /**
     * Check if any text fields are empty or contain invalid values in the case of
     * number of people text field which must contain a positive integer.
     * @return
     */
    private boolean checkFormErrors() {
        if (frequencyName.getText().isEmpty()) {
            UIController.displayErrorDialog("Validation", "Please enter a population name.");
            frequencyName.requestFocus();
            return false;
        }
        if (frequencyFileName.getText().isEmpty()) {
            UIController.displayErrorDialog("Validation", "Please select a frequency file.");
            browseButton.requestFocus();
            return false;
        }
        if (numberOfPeople.getText().isEmpty()) {
            UIController.displayErrorDialog("Validation", "Please enter the number of people.");
            numberOfPeople.requestFocus();
            return false;
        } else {
        	try {
                Double.parseDouble(numberOfPeople.getText());
                try {
                    int numPeople = Integer.parseInt(numberOfPeople.getText());
                    if (numPeople < 1) {
                    	UIController.displayErrorDialog("Validation", Constants.VALUE_OUT_OF_RANGE_ERROR_MESSAGE + "an integer >= 1 and <= " + Constants.MAX_INTEGER_VALUE);
                        numberOfPeople.requestFocus();
                        return false;
                    }
                } catch (Exception e) {
                    UIController.displayErrorDialog("Validation", Constants.VALUE_OUT_OF_RANGE_ERROR_MESSAGE + "an integer >= 1 and <= " + Constants.MAX_INTEGER_VALUE);
                    numberOfPeople.requestFocus();
                    return false;
                }
            } catch (NumberFormatException nfe) {
            	UIController.displayErrorDialog("Validation", Constants.NON_NUMERIC_NUMBER_OF_PEOPLE_ERROR);
                numberOfPeople.requestFocus();
                return false;
            }
        }

        return true;
    }
    
    // Population Popup is a viewer and is not editable.
    public void viewPopulationPopUp(final FreqTable population) {
        
        TableColumn<ObservableList<?>, String> column = new TableColumn<>("Population");
        column.setMinWidth(150);
        column.setEditable(true);

        //Popup
        final TableView<ObservableList<?>> table = new TableView<>();
        table.getItems().clear();
        table.setEditable(false);

        final TextField sampleName = new TextField();
        AnchorPane.setRightAnchor(sampleName, 75.0);
        AnchorPane.setTopAnchor(sampleName, 5.0);
        sampleName.setText(population.getName());
        sampleName.setEditable(false);

        //Build Columns and Headers
        TableColumn<ObservableList<?>, String> column1 = new TableColumn<>(Constants.GENOTYPE_TABLE_COLUMN_LOCUS);
        column1.setMinWidth(125);
        column1.setEditable(false);
        column1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
                return (SimpleStringProperty) cellDataFeatures.getValue().get(0);
            }
        });
        column1.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
        table.getColumns().add(column1);

        TableColumn<ObservableList<?>, String> column2 = new TableColumn<>(Constants.POPULATION_TABLE_COLUMN_ALLELE);
        column2.setMinWidth(75);
        column2.setEditable(false);
        column2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
                return (SimpleStringProperty) cellDataFeatures.getValue().get(1);
            }
        });
        column2.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
        table.getColumns().add(column2);

        TableColumn<ObservableList<?>, String> column3 = new TableColumn<>(Constants.POPULATION_TABLE_COLUMN_FREQ);
        column3.setMinWidth(75);
        column3.setEditable(true);
        column3.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
                return (SimpleStringProperty) cellDataFeatures.getValue().get(2);
            }
        });
        column3.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
        table.getColumns().add(column3);
   
        HashMap<Locus, HashMap<STRAllele, Integer>> dists = population.getFreqDists();
        
        for (Locus l : population.getLoci()) {
        	ArrayList<STRAllele> alleleKeys = new ArrayList<STRAllele>(dists.get(l).keySet());
        	Collections.sort(alleleKeys);
        	for (STRAllele a : alleleKeys) {
                table.getItems().addAll(FXCollections.observableArrayList(Arrays.asList(new SimpleStringProperty[]{new SimpleStringProperty(l.getName()), new SimpleStringProperty(a.toString()), new SimpleStringProperty(Double.toString(dists.get(l).get(a)))})));
            }
        }

		//Button Handler
        // Add Check againt sqlite for Allele Column for both pairs.
        
       

        Button cancel = new Button(Constants.GENOTYPE_POPUP_BUTTON_CANCEL);
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //sampleStage.close();
            }
        });
        StackPane.setMargin(cancel, new Insets(0.0, 0.0, 0.0, 100.0));

        //Build the Popup control
        SplitPane pane = new SplitPane();
        pane.setMinHeight(400);
        pane.setMaxHeight(400);

        pane.setOrientation(Orientation.VERTICAL);
        AnchorPane topPane = new AnchorPane();
        topPane.setMinHeight(40);
        topPane.setMaxHeight(40);
        AnchorPane middlePane = new AnchorPane();
        AnchorPane bottomPane = new AnchorPane();
        bottomPane.setMinHeight(40);
        bottomPane.setMaxHeight(40);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setMaxWidth(300);
        scrollPane.setMinWidth(300);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(stackPane, 0.0);
        AnchorPane.setBottomAnchor(stackPane, 0.0);
        AnchorPane.setLeftAnchor(stackPane, 0.0);
        AnchorPane.setRightAnchor(stackPane, 0.0);

        AnchorPane.setTopAnchor(table, 0.0);
        AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0);
        AnchorPane.setRightAnchor(table, 0.0);

        Label label = new Label(Constants.POPULATION_POPUP_LABEL_NAME);
        AnchorPane.setTopAnchor(label, 10.0);
        AnchorPane.setLeftAnchor(label, 10.0);

        middlePane.getChildren().add(scrollPane);
        //topPane.getChildren().add(label);
        topPane.getChildren().add(sampleName);

        scrollPane.setContent(table);
        bottomPane.getChildren().add(stackPane);
        pane.getItems().addAll(topPane, table);

        Scene scene = new Scene(pane);

        Stage sampleStage = new Stage();
        sampleStage.setTitle(Constants.POPULATION_VIEWER_POPUP_TITLE);
        sampleStage.initModality(Modality.WINDOW_MODAL);
        sampleStage.setScene(scene);
        sampleStage.setMinWidth(350);
        sampleStage.setMaxWidth(350);
        sampleStage.setMinHeight(400);
        sampleStage.getIcons().add(new Image(getClass().getResourceAsStream("dna2.png")));
        sampleStage.initOwner(stage);
        sampleStage.showAndWait();
        
    }
    
    /**
     * Selects text in field when clicked. Based on
     * http://stackoverflow.com/questions/14965318/javafx-method-selectall-just-works-by-focus-with-keyboard
     * @param textField
     */
    private void addTextFieldFocusListener(TextField textField) {
    	textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean t, Boolean t1) {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (textField.isFocused() && !textField.getText().isEmpty()) {
                        	textField.selectAll();
                        }
                    }
                });
            }
        });
    }
    
    @FXML
    private void closeDialog() {
    	// if all fields filled out and user hits OK, they may assume the population will be
    	// added by OK, but you have to click Add Population button to add it.
    	if (!frequencyName.getText().isEmpty() && !frequencyFileName.getText().isEmpty() &&
    			!numberOfPeople.getText().isEmpty()) {
    		ButtonType[] results = {ButtonType.YES, ButtonType.NO};
    		Alert alert = new Alert(AlertType.WARNING, "A population has been created but has not yet"
    				+ " been added. Do you wish to exit Population Manager without adding Population?", results);
    		Optional<ButtonType> result = alert.showAndWait();
    		if (result.isPresent() && result.get() == ButtonType.YES) {
    			this.stage.close();
    		} else {
    			return;
    		}
    	} else {
    		this.stage.close();
    	}
    	
        ObservableList<String> popChoiceList = FXCollections.observableArrayList(uiController.populations.keySet());
        Collections.sort(popChoiceList);
        
        TableColumn<ObservableList<?>, String> popColumn = (TableColumn<ObservableList<?>, String>) uiController.nocItTable.getColumns().get(Constants.NOCIT_TABLE_COLUMN_POPULATION_INDEX);
        popColumn.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(popChoiceList));
    	
        popColumn = (TableColumn<ObservableList<?>, String>) uiController.ceesItTable.getColumns().get(Constants.CEESIT_TABLE_COLUMN_POPULATION_INDEX);
        popColumn.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(popChoiceList));
    }
    
    /**
     * Writes a csv file to the Settings directory where each row contains the name of a population,
     * the path of the file used to create a population, and the number of people in the population.
     * This file allows populations to be "saved" when NOCIt is closed and reopened, since this file
     * is loaded when NOCIt is started.
     */
    private void writeFrequenciesFile() {
    	String header = "Name" + "\t" + "Path" + "\t" + "Num People" + "\t";
    	ArrayList<String[]> entries = new ArrayList<String[]>();
    	for (int i = 0; i < this.uiController.populationNamesList.size(); i++) {
    		FreqTable frequency = this.uiController.populations.get(this.uiController.populationNamesList.get(i));
    		String line = "";
    		line += frequency.getName() + "\t";
    		String path = frequency.getFilePath();
    		if (path.contains("\\")) {
    			path = path.replace("\\", "/");
    		}
    		line += path + "\t";
    		line += frequency.getNumPeople() + "\t";
    		String[] entry = line.substring(0, line.length() - 1).split("\t");
    		entries.add(entry);
    		line = "";
    	}

    	CSVFileWriter.write(Settings.getSettingsPath() + File.separatorChar + "Frequencies.csv", header, entries);
    }
}
