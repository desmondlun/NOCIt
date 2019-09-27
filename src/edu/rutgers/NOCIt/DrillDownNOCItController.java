package edu.rutgers.NOCIt;

import static edu.rutgers.NOCIt.UIMain.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.opencsv.CSVReader;

import edu.rutgers.NOCIt.Control.Calibration;
import edu.rutgers.NOCIt.Control.CalibrationProjectHandler;
import edu.rutgers.NOCIt.Control.FileChecker;
import edu.rutgers.NOCIt.Control.Project;
import edu.rutgers.NOCIt.Control.Settings;
import edu.rutgers.NOCIt.Data.CSVModule;
import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.FileReaders;
import edu.rutgers.NOCIt.Data.FreqTable;
import edu.rutgers.NOCIt.Data.Kit;
import edu.rutgers.NOCIt.Data.Locus;
import edu.rutgers.NOCIt.Data.Sample;
import edu.rutgers.NOCIt.Data.UtilityMethods;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 * Class contains controls where the required inputs for running NOCIt can be loaded and
 * entered by the user. These inputs include a calibration project, output folder,
 * csv output file path, include results as csv, max number of contributors, filter, 
 * population(s), sample files, analytical thresholds. The Add Batch button performs 
 * input validation and loads values into the NOCIt table.
 *
 * @author Rob Carpenter
 * @author James Kelley
 * @author Desmond Lun
 */
public class DrillDownNOCItController implements Initializable {

    @FXML
    private VBox samples;
    @FXML
    private ChoiceBox<String> numberOfContribs;
    @FXML
    private CheckBox isFiltered;
    @FXML
    private CheckBox writeCSV;
    @FXML
    private ChoiceBox<String> calibrationsBox;
    @FXML
    private Button addSampleFilesButton;
    @FXML
    private Button clearAllButton;
    @FXML
    private Button addBatchButton;
    @FXML
    private Button chooseOutputFileButton;
    
    private UIController uiController = null;
    private Stage stage;
    private Map<String, Sample> currentSamples = new HashMap<>();
    private ArrayList<String> currentSampleNames = new ArrayList<String>();
    //private List<File> loadedSampleFiles = new ArrayList<File>();
    private List<String> loadedSampleFileNames = new ArrayList<String>();
    private List<String> sampleFileNamesNotToBeLoaded = new ArrayList<String>();
    private List<String> currentPopulations = new ArrayList<>();
    private Calibration currentCalibration = null;    

    private FileChooser.ExtensionFilter projectExtFilter
        = new FileChooser.ExtensionFilter(Constants.CALIBRATION_FILE_NAME + "(*." + Constants.CALIBRATION_FILE_EXTENSION + ")", "*" + Constants.CALIBRATION_FILE_EXTENSION);

    private FileChooser.ExtensionFilter csvFilesFilter
    	= new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
    
    private FileChooser.ExtensionFilter allFilesFilter
    	= new FileChooser.ExtensionFilter("All Files (*.*)", "*.*");
    
    @FXML
    private VBox populationList;
    @FXML
    private TextField outputFolderField;
    @FXML
    private TextField csvOutputFileField;
    @FXML
    private Button thresholdsButton;
    @FXML
    private Button csvOutputFileButton;
    
    @FXML
	private Label calibrationLabel;
    @FXML
	private Label outputFolderLabel;
    @FXML
	private Label csvOutputFileLabel;
    @FXML
	private Label includeResultsAsCSVLabel;
    @FXML
	private Label maxNOCLabel;
    @FXML
	private Label filterLabel;
    @FXML
	private Label populationsLabel;

    public LinkedHashMap<Locus, Integer> currentThresholdData = null;
    
    private static int nextValidRowId = 0;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	Date myDate = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    	String dateString = sdf.format(myDate);
    	
    	String[] nocDropdownOptions = new String[Constants.NOCIT_MAX_NOC_CHOICE];
		for (int i = 0; i < nocDropdownOptions.length; i++)
			nocDropdownOptions[i] = Integer.toString(i + 1);		
    	ObservableList<String> nocChoiceList = FXCollections.observableArrayList(nocDropdownOptions);
    	numberOfContribs.setItems(nocChoiceList);
    	if (Settings.lastNOCSelection != null && nocChoiceList.contains(Settings.lastNOCSelection)) {
    		numberOfContribs.getSelectionModel().select(nocChoiceList.indexOf(Settings.lastNOCSelection));
    	} else {
    		numberOfContribs.getSelectionModel().select(0);
    	}
    	if (Settings.lastFilterSelection != null && Settings.lastFilterSelection.equals("true")) {
    		isFiltered.setSelected(true);
    	}
    	if (Settings.lastWriteCSVSelection != null) {
    		if (Settings.lastWriteCSVSelection.equals("true")) {
    			writeCSV.setSelected(true);
        		csvOutputFileButton.setDisable(false);
        		csvOutputFileField.setDisable(false);
    		} else {
    			csvOutputFileButton.setDisable(true);
    			csvOutputFileField.setDisable(true);
    		}
    	}
    	
    	String lastOutputFilePath = Settings.defaultDirectory;
        if (Settings.lastOutputFilePath != null) {
        	lastOutputFilePath = Settings.lastOutputFilePath;
        }
        File file = new File(lastOutputFilePath);
        if (file.exists()) {
        	outputFolderField.setText(lastOutputFilePath);
        	outputFolderField.setTooltip(new Tooltip(lastOutputFilePath));
        	csvOutputFileField.setText(lastOutputFilePath + File.separatorChar + dateString + ".csv");
        	csvOutputFileField.setTooltip(new Tooltip(lastOutputFilePath + File.separatorChar + dateString + ".csv"));
        } else {
        	outputFolderField.setText(Settings.defaultDirectory);
        	outputFolderField.setTooltip(new Tooltip(Settings.defaultDirectory));
        	csvOutputFileField.setText(Settings.defaultDirectory + "\\" + dateString + ".csv");
        	csvOutputFileField.setTooltip(new Tooltip(Settings.defaultDirectory + "\\" + dateString + ".csv"));
        }
    	clearAllButton.setDisable(true);
    	Insets i = new Insets(0, 0, 0, 15);
    	populationList.setPadding(i);
    	
    	calibrationLabel.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_CALIBRATION_LABEL_TOOLTIP));
    	outputFolderLabel.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_OUTPUT_FOLDER_LABEL_TOOLTIP));
    	csvOutputFileLabel.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_CSV_OUTPUT_FILE_LABEL_TOOLTIP));
    	includeResultsAsCSVLabel.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_INCLUDE_RESULTS_AS_CSV_LABEL_TOOLTIP));
    	maxNOCLabel.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_MAX_NOC_LABEL_TOOLTIP));
    	filterLabel.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_FILTER_LABEL_TOOLTIP));
    	populationsLabel.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_POPULATION_LABEL_TOOLTIP));
    	addSampleFilesButton.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_ADD_SAMPLE_FILES_BUTTON_TOOLTIP));
    	clearAllButton.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_CLEAR_ALL_BUTTON_TOOLTIP));
    	thresholdsButton.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_AT_BUTTON_TOOLTIP));
    	addBatchButton.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_ADD_BATCH_BUTTON_TOOLTIP));
    	chooseOutputFileButton.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_OUTPUT_FOLDER_BUTTON_TOOLTIP));
    	csvOutputFileButton.setTooltip(new Tooltip(Constants.NOCIt_BATCH_RUN_CSV_OUTPUT_FILE_BUTTON_TOOLTIP));
    }

    public void setStage(Stage stage, UIController controller) {
    	this.stage = stage;
    	this.uiController = controller;

    	for (int i = 0; i < this.uiController.populationNamesList.size(); i++) {
    		FreqTable frequency = this.uiController.populations.get(this.uiController.populationNamesList.get(i));
    		Label label = new Label("   ");

    		populationList.getChildren().add(label);
    		currentPopulations.add(frequency.getName());

    		CheckBox cb = new CheckBox(frequency.getName());
    		cb.setTooltip(new Tooltip(UtilityMethods.createPopulationTooltip(frequency)));
    		cb.setSelected(true);
    		cb.setMnemonicParsing(false);
    		cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
    			@Override
    			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
    				if (newValue) {
    					currentPopulations.add(frequency.getName());
    				} else {
    					currentPopulations.remove(frequency.getName());
    				}
    			}

    		});

    		populationList.getChildren().add(cb);
    	}
    }

    @FXML
    private void addNewSampleFiles(ActionEvent event2) {
        if (this.currentCalibration == null) {
            UIController.displayErrorDialog("Validation", Constants.NOCIt_BATCH_RUN_SELECT_CALIBRATION_MESSAGE);
            return;
        } 
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Constants.SELECT_FILE);
        // get last save path from settings
        String lastSamplePath = Settings.defaultDirectory;
        if (Settings.lastSamplePath != null) {
        	lastSamplePath = Settings.lastSamplePath;;
        }
        uiController.setFileChooserInitialDirectory(fileChooser, lastSamplePath);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Sample File (*.csv)", "*.csv"));
        List<File> list = fileChooser.showOpenMultipleDialog(stage);
        if (list != null) {  
        	for (int i = 0; i < list.size(); i++) {
                    if (!FileChecker.isValidSampleFile(list.get(i).getAbsolutePath())) {
                        UIController.displayErrorDialog("File Validation", "This is not a valid Sample file: \r\n\r\n" + list.get(i).getAbsolutePath());
                        return;
                    }
        		if (this.loadedSampleFileNames.contains(list.get(i).getName())) {
        			UIController.displayErrorDialog("Warning", Constants.NOCIt_BATCH_RUN_SAMPLE_FILE_ALREADY_LOADED_WARNING);
        			return;
        		} else {
        			this.loadedSampleFileNames.add(list.get(i).getName());
        		}
        	}
        	Settings.lastSamplePath = list.get(0).getParent();
        	Settings.save();

        	Kit kit = currentCalibration.getKit();
        	List<CSVModule> csvModuleList = FileReaders.createCSVModuleList(list, kit, null);
        	List<String> invalidCsvModuleList = new ArrayList<String>();
        	for (CSVModule csvModule : csvModuleList) {
        		if (!csvModule.isValidFile()) {
        			invalidCsvModuleList.add(csvModule.getFileName());
        		}
        	}
        	
        	if (invalidCsvModuleList.size() > 0) {
        		Alert alert = new Alert(AlertType.WARNING, Constants.INVALID_SAMPLE_FILES_WARNING + 
        				invalidCsvModuleList.toString() + ". Click View -> Logs for more details.");
                alert.showAndWait();
        	}
        	
        	// Since some sample files contain multiple samples, it is possible that if some samples
        	// of from a file are deleted, and the file is subsequently reloaded, some samples could
        	// be present in the sample pane.
        	for (CSVModule csvModule : csvModuleList) {
        		if (!invalidCsvModuleList.contains(csvModule.getFileName())) {
        			for (int j = 0; j < csvModule.getSampleNames().size(); j++) {
            			String sampleID = csvModule.getSampleNames().get(j);
            			if (currentSampleNames.contains(sampleID)) {
            				if (!sampleFileNamesNotToBeLoaded.contains(csvModule.getFileName())) {
            					sampleFileNamesNotToBeLoaded.add(csvModule.getFileName());
            				}
            			}
            		}
        		}
        	}
        	if (sampleFileNamesNotToBeLoaded.size() > 0 && currentSamples.size() > 0) {
        		UIController.displayErrorDialog("Warning", Constants.NOCIt_BATCH_RUN_SAMPLE_FILE_ALREADY_LOADED_WARNING);
        		return;
        	}

        	Set<Locus> loci = new LinkedHashSet<Locus>();
        	for (CSVModule csvModule : csvModuleList) {
        		if (!invalidCsvModuleList.contains(csvModule.getFileName())) {
        			for (int i = 0; i < csvModule.getSampleNames().size(); i++) {
        				String sampleID = csvModule.getSampleNames().get(i);
        				if (!currentSampleNames.contains(sampleID)) {
        					AnchorPane pane = new AnchorPane();
        					Button button = new Button("X");

        					TextField text = new TextField();
        					text.setText(sampleID);
        					text.setTooltip(new Tooltip("File Name: " + csvModule.getFileName()));
        					text.setEditable(false);

        					pane.getChildren().add(button);
        					pane.getChildren().add(text);

        					AnchorPane.setLeftAnchor(button, 10.0);
        					AnchorPane.setLeftAnchor(text, 45.0);
        					AnchorPane.setRightAnchor(text, 10.0);

        					Pane spacerPane = new Pane();
        					spacerPane.setMaxHeight(5);
        					spacerPane.setMinHeight(5);

        					this.samples.getChildren().add(spacerPane); 
        					this.samples.getChildren().add(pane);
        					csvModule.getSamples().get(sampleID).setSampleFileName(csvModule.getFileName());
        					this.currentSamples.put(sampleID, csvModule.getSamples().get(sampleID));
        					currentSampleNames.add(sampleID);

        					button.setOnAction(event -> {
        						this.samples.getChildren().remove(spacerPane);
        						this.samples.getChildren().remove(pane);
        						this.currentSamples.remove(sampleID);
        						if (this.loadedSampleFileNames.contains(csvModule.getFileName())) {
        							this.loadedSampleFileNames.remove(this.loadedSampleFileNames.indexOf(csvModule.getFileName()));
        						}
        						currentSampleNames.remove(currentSampleNames.indexOf(sampleID));
        					});
        					
        					loci.addAll(currentSamples.get(sampleID).getLoci());

        					this.thresholdsButton.setVisible(true);
        				}
        			}       
        		}
        	}

        	LinkedHashMap<Locus, Integer> analyticalThresholds = new LinkedHashMap<>();            
        	for (Locus locus : loci) {
        		if (currentThresholdData != null && currentThresholdData.containsKey(locus))
        			analyticalThresholds.put(locus, currentThresholdData.get(locus));  
        		else
        			analyticalThresholds.put(locus, 1);  
        	}
        	currentThresholdData = analyticalThresholds;
        	
        	sampleFileNamesNotToBeLoaded.clear();
        	if (currentSampleNames.size() > 0) {
        		clearAllButton.setDisable(false);
        	}
        }
    }
    
    @FXML
    private void clearAllSampleFiles(ActionEvent event) {
    	samples.getChildren().clear();
		currentSamples.clear();
		currentSampleNames.clear();
		clearAllButton.setDisable(true);
		this.loadedSampleFileNames.clear();
		this.sampleFileNamesNotToBeLoaded.clear();
    }

    @FXML
    private void processAndAddBatch(ActionEvent event) {
        if (this.checkFormErrors()) {        	
        	for (int i = 0; i < currentSampleNames.size(); i++) {
        		String sampleID = currentSampleNames.get(i);
                for (String frequency : currentPopulations) {
                    ObservableList<Object> initialRow = FXCollections.observableArrayList();
                    initialRow.add(new SimpleBooleanProperty(false));
                    initialRow.add(new SimpleStringProperty(""));
                    initialRow.add(new SimpleStringProperty(sampleID));
                    initialRow.add(new SimpleBooleanProperty(isFiltered.isSelected()));
                    initialRow.add(new SimpleStringProperty(""));
                    initialRow.add(new SimpleStringProperty(frequency));
                    initialRow.add(new SimpleStringProperty(numberOfContribs.getSelectionModel().getSelectedItem()));
                    initialRow.add(new SimpleStringProperty(calibrationsBox.getSelectionModel().getSelectedItem()));
                    String output = Settings.lastOutputFilePath + "/" + sampleID;
                    if (isFiltered.isSelected()) {
                        output += "-filtered";
                    }
                    output += "-" + numberOfContribs.getSelectionModel().getSelectedItem()
                    + "-" + calibrationsBox.getSelectionModel().getSelectedItem() + ".pdf";
                    File f = new File(output);
                    // fixes bug where slashes may not match, some forward, some back
                    initialRow.add(new SimpleStringProperty(f.getAbsolutePath()));
                    initialRow.add(new SimpleStringProperty(""));
                    initialRow.add(new SimpleStringProperty(""));
                    
                    uiController.rowIDAnalyticalThresholdsMap.put(Integer.toString(nextValidRowId), UtilityMethods.copyThresholdData(currentThresholdData));
                    
                    initialRow.add(new SimpleIntegerProperty(nextValidRowId++));
                    uiController.nocItTable.getItems().add(initialRow);
                    
                    Settings.lastNOCSelection = numberOfContribs.getSelectionModel().getSelectedItem();
                    Settings.lastNOCItCalibration = calibrationsBox.getSelectionModel().getSelectedItem();
                    if (isFiltered.isSelected()) {
                    	Settings.lastFilterSelection = "true";
                    	uiController.nocItFilterSelectAllCheckBox.setSelected(true);
                    	uiController.isNOCItFilterAllSelected = true;
                    } else {
                    	Settings.lastFilterSelection = "false";
                    	uiController.nocItFilterSelectAllCheckBox.setSelected(false);
                    	uiController.isNOCItFilterAllSelected = false;
                    }
                    if (writeCSV.isSelected()) {
                    	Settings.lastWriteCSVSelection = "true";
                    } else {
                    	Settings.lastWriteCSVSelection = "false";
                    }
                    Settings.save();
                }
            }
           
            uiController.addSamples(currentSamples);

            this.stage.close();
            
            // reloads table so easy edit cells are only enabled in rows with data
            uiController.nocItTable.refresh();                       
            
            if (writeCSV.isSelected()) {
            	uiController.nocItCSVOutputFilePath = csvOutputFileField.getText();
            }
        }
    }

    @FXML
    private void chooseOutputFile(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(Constants.SELECT_FOLDER);
        // get last save path from settings
        String lastOutputFilePath = Settings.defaultDirectory;
        if (Settings.lastOutputFilePath != null) {
        	lastOutputFilePath = Settings.lastOutputFilePath;
        }
        uiController.setDirectoryChooserInitialDirectory(directoryChooser, lastOutputFilePath);
        File folder = directoryChooser.showDialog(stage);

        if (folder != null) {
            this.outputFolderField.setText(folder.getAbsolutePath());
            outputFolderField.setTooltip(new Tooltip(folder.getAbsolutePath()));
            Settings.lastOutputFilePath = folder.getAbsolutePath();
            Settings.save();
        }
    }
    
    @FXML
    private void chooseCSVOutputFile(ActionEvent event) {
    	File f = getCSVOutputFile();
    	// It looks like the file chooser already takes care of this. Leave this code
    	// here in case testing disproves this.
//    	if (f.exists()) {
//    		ButtonType[] results = {ButtonType.YES, ButtonType.NO, ButtonType.CANCEL};
//            Alert alert = new Alert(AlertType.ERROR, "CSV Output File Exists. Overwrite?", results);
//            Optional<ButtonType> result = alert.showAndWait();
//            if (result.isPresent() && result.get() == ButtonType.YES) {
//            	
//            } else if (result.isPresent() && result.get() == ButtonType.NO) {
//            	f = getCSVOutputFile();
//            } else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
//            	
//            }
//    	} 
		
		csvOutputFileField.setText(f.getAbsolutePath());
		csvOutputFileField.setTooltip(new Tooltip(f.getAbsolutePath()));
    }
    
    private File getCSVOutputFile() {
    	FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(Constants.SELECT_FILE);
		// get last save path from settings
		String lastOutputFilePath = Settings.defaultDirectory;
		if (Settings.lastOutputFilePath != null) {
			lastOutputFilePath = Settings.lastOutputFilePath;
		}
		uiController.setFileChooserInitialDirectory(fileChooser, lastOutputFilePath);
		fileChooser.getExtensionFilters().add(csvFilesFilter);
		fileChooser.getExtensionFilters().add(allFilesFilter);
		File file = fileChooser.showSaveDialog(stage);
		String path = file.getAbsolutePath();
		if (!path.endsWith(".csv")) {
			path += ".csv";
		}
		
		File f = new File(path);
		
		return f;
    }

    public void buildCalibrationChoiceList() {
    	ObservableList<String> calibrationChoiceList = uiController.createCalibrationChoiceList();
        calibrationsBox.setItems(calibrationChoiceList);
        // set last used calibration
        if (Settings.lastNOCItCalibration != null && calibrationChoiceList.contains(Settings.lastNOCItCalibration)) {
    		calibrationsBox.getSelectionModel().select(calibrationChoiceList.indexOf(Settings.lastNOCItCalibration));
    		currentCalibration = uiController.getCalibration(Settings.lastNOCItCalibration);
    		// Fixes bug where opening dialog and a calibration has already been selected
        	// from a previous run, ATs were set to 1. Also fixes bug where if x (close) button
    		// clicked, next run will have all ATs at 1.
			loadAnalyticalThresholdsFromFile();
    	}
        
        calibrationsBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            	try {
            		if (newValue.intValue() > -1) {
            			String text = calibrationsBox.getItems().get(newValue.intValue());

                		if (text.equals("<Add New>")) {
                			FileChooser fileChooser = new FileChooser();
                			fileChooser.setTitle(Constants.SELECT_FILE);
                			// get last save path from settings
                			String lastCalibrationPath = Settings.defaultDirectory;
                			if (Settings.lastCalibrationPath != null) {
                				lastCalibrationPath = Settings.lastCalibrationPath;
                			}
                			uiController.setFileChooserInitialDirectory(fileChooser, lastCalibrationPath);
                			fileChooser.getExtensionFilters().add(projectExtFilter);
                			fileChooser.getExtensionFilters().add(allFilesFilter);
                			File file = fileChooser.showOpenDialog(stage);

                			if (file != null) {
                				String message = Constants.CALIBRATION_NOT_CALCULATED_ERROR_MESSAGE;
                				Project project = null;
                				boolean valid = true;
                				if (!file.getAbsolutePath().endsWith(Constants.CALIBRATION_FILE_EXTENSION)) {
                					valid =  false;
                					message = Constants.INVALID_CALIBRATION_FILE_TYPE_ERROR_MESSAGE; 
                				} else {
                					project = CalibrationProjectHandler.loadProjectData(file);
                					if (project.getCalibration() == null) {
        								message = Constants.INVALID_CALIBRATION_FILE_ERROR_MESSAGE;
        								valid = false;
        							} else if (!CalibrationProjectHandler.isCalibrationCalculated()) {
        								valid = false;
        							}
                				}
                				if (!valid) {	
                					Alert alert = new Alert(AlertType.ERROR, message);
                		            alert.showAndWait();
                		            calibrationsBox.getSelectionModel().select(oldValue.intValue());
                				} else {
                					if (project != null) {
                						currentCalibration = project.getCalibration();
                        				if (currentCalibration != null) {
                        					String name = file.getName().split("\\.")[0];
                        					uiController.updateCalibrationOptions(name, currentCalibration);
                        					// File location necessary to generate MD5 Hash
                        					currentCalibration.setCalibrationPath(file.getAbsolutePath());

                        					ObservableList<String> calibrationChoiceList = uiController.createCalibrationChoiceList();
                        					calibrationsBox.setItems(calibrationChoiceList);
                        					//calibrationsBox.getSelectionModel().selectFirst();
                        					calibrationsBox.getSelectionModel().select(calibrationChoiceList.indexOf(name));

                        					Settings.lastCalibrationPath = file.getParent();
                        					Settings.save();
                        					
                        					resetDialog();
                        					loadAnalyticalThresholdsFromFile();
                        				}
                					}
                				}
                			} else
                				calibrationsBox.getSelectionModel().clearSelection();
                		} else {
                			currentCalibration = uiController.getCalibration(text);
                		}
            		}
                } catch (Exception e) {
                	e.printStackTrace();
                	logger.error(Constants.NOCIt_BATCH_RUN_CALIBRATION_SELECTION_ERROR, e);
                }
            }

        });
    }

    /**
     * Checks if fields that require an value are empty.
     * @return
     */
    public boolean checkFormErrors() {
        if (this.outputFolderField.getText().isEmpty()) {
            UIController.displayErrorDialog("Validation", "Please select an output folder.");
            return false;
        }

        if (this.currentSamples.isEmpty()) {
            UIController.displayErrorDialog("Validation", "Please add at least one sample file.");
            return false;
        }

        if (this.currentPopulations.isEmpty()) {
            UIController.displayErrorDialog("Validation", "Please select at least one population.");
            return false;
        }

        return true;
    }

    /**
     * Creates and loads analytical thresholds table.
     */
    private void setAnalyticalThresholds() {
        try {
            AnalyticalThresholdsController controller = new AnalyticalThresholdsController();       
            FXMLLoader loader = new FXMLLoader();
            loader.setController(controller);
            loader.setLocation(getClass().getResource("AnalyticalThresholds.fxml"));
            
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage nocitStage = UtilityMethods.buildAnalyticalThresholdsStage(scene);
            nocitStage.getIcons().add(new Image(getClass().getResourceAsStream("dna2.png")));
            nocitStage.initOwner(stage);
            nocitStage.show();

            controller.setStage(nocitStage, this);

            HashMap<Locus, Integer> data = this.currentThresholdData;
            controller.populateTable(data);
        } catch (Exception e) {

        }
    }

    /**
     * Loads analytical thresholds from csv file saved in Settings if file exists for
     * the kit name from the loaded calibration.
     */
    private void loadAnalyticalThresholdsFromFile() {
    	Kit kit = currentCalibration.getKit();
    	File f = new File(Settings.getSettingsPath() + "Saved_Analytical_Thresholds" + 
    			File.separatorChar + UtilityMethods.cleanedUpKitName(kit.getKitName()) + ".csv");
    	if (f.exists()) {
    		HashMap<Locus, Integer> savedThresholdData = new LinkedHashMap<Locus, Integer>();
    		CSVReader reader;

    		try {
    			reader = new CSVReader(new FileReader(f), ',');
    			String [] dataArray;
    			try {
    				while ((dataArray = reader.readNext()) != null) {
    					savedThresholdData.put(new Locus(dataArray[0]), new Integer(dataArray[1]));
    				}
    				reader.close();
    				// Saved thresholds are saved by kit name in "Chemistry Kit" line. If two kits have the
    				// same name but not all loci are the same, using the saved thresholds will not produce
    				// the correct set of loci. Instead use the current thresholds then if the saved thresholds
    				// contain a locus in the current thresholds, update the current thresholds using the
    				// saved thresholds value.
    				currentThresholdData = UtilityMethods.updatedThresholdsData(currentThresholdData, savedThresholdData, kit);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		}
    	}
    }

    @FXML
    private void setAnalyticalThresholds(ActionEvent event) {
        this.setAnalyticalThresholds();
    }
    
    @FXML
    private void writeCSVAction(ActionEvent event) {
    	if (writeCSV.isSelected()) {
    		csvOutputFileButton.setDisable(false);
    		csvOutputFileField.setDisable(false);
		} else {
			csvOutputFileButton.setDisable(true);
			csvOutputFileField.setDisable(true);
		}
    }
    
    /**
     * Resets fields to initial conditions.
     */
    private void resetDialog() {
    	samples.getChildren().clear();
		currentSamples.clear();
		currentSampleNames.clear();
		this.loadedSampleFileNames.clear();
		this.sampleFileNamesNotToBeLoaded.clear();
		clearAllButton.setDisable(true);
    }
    
    public Calibration getCurrentCalibration() {
		return currentCalibration;
	}
    
}
