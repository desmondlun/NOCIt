/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.NOCIt.Data;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 *
 * @author rcarpenter
 */
public class Constants {
    public static final String APPLICATION_NAME = "NOCIt/CEESIt";
    public static final String APPLICATION_VERSION = "4.0.0";
    
    public static final double TWO_PI = 2 * Math.PI;
    public static final double SQRT_TWO_PI = Math.sqrt(2 * Math.PI);
    
    public static final String AUTOSAVE_FILENAME = "autosave.zip";
    public static final String AUTOSAVE_ERROR_TITLE = "NOCIt Autosave Detected";
    public static final String AUTOSAVE_ERROR_MESSAGE = "An autosave file has been detected. Would you like to continue where you left off? \n \nIf you don't, the autosave file will be deleted.";
    public static final String AUTOSAVE_DELETE_ERROR = "Error Deleting Autosave File";
    public static final String AUTOSAVE_HANDLE_ERROR = "Error Handling Autosave File Loading: ";
        
    public static final String VALIDATE_GENOTYPE_FILE_IMPORT_ERROR_MESSAGE = "The file selected is not a valid Genotypes file.";    
    public static final String LOAD_SAMPLE_FILES_ERROR_MESSAGE = "There was an error loading the sample files. Please check the files and try again.";
    public static final String LOAD_SAMPLE_FILES_ERROR_LOG_MESSAGE = "Error loading sample file folder: ";
    public static final String LOAD_SAMPLE_FILES_MATCH_GENOTYPE_MESSAGE = "Error matching genotypes. Genotype ID Index may be out of range.";
    public static final String LOAD_BINS_FILE_ERROR_MESSAGE = "There was an error loading the bins file. Please check the file and try again.";
    public static final String LOAD_BINS_FILE_ERROR_LOG_MESSAGE = "Error loading bins file: ";
    
    public static final String NEXT_CLICKED_ERROR_KNOWN_GENOTYPE_MESSAGE = "Please make sure each row is assigned a known genotype.";
    public static final String NEXT_CLICKED_ERROR_DNA_MASS_MESSAGE = "Please make sure each row has a DNA Mass value.";
    public static final String NEXT_CLICKED_ERROR_CALIBRATION_MESSAGE = "Please enter a calibration name to continue.";
    public static final String NEXT_CLICKED_ERROR_SAMPLE_FOLDER_MESSAGE = "Please select a sample folder to continue.";
    public static final String ERROR_NO_BINS_FILE_MESSAGE = "Please first load a bins file.";        
    public static final String NO_VALID_SAMPLE_FILES_IN_DIRECTORY = "No valid sample files found in this directory. Please select another directory";
    
    public static final String LOAD_KIT_AFTER_SAMPLES_WARNING_QUESTION = "Loading a new bins file will clear loaded sample files and calculations. "
    		+ "Are you sure you want to do this?";
    public static final String LOAD_SAMPLES_AFTER_CALCULATION_WARNING_QUESTION = "Loading new sample file(s) will clear calculations. "
    		+ "Are you sure you want to do this?";
    public static final String LOAD_GENOTYPES_AFTER_CALCULATION_WARNING_QUESTION = "Loading a new known genotype file will clear calculations. "
    		+ "Are you sure you want to do this?";
    
    public static final int NOCIT_TAB_INDEX = 0;
    public static final int CEESIT_TAB_INDEX = 1;
    public static final int CALIBRATION_TAB_INDEX = 1;
    
    public static final String TREE_TABLE_COLUMN_FORMULA = "Formula";
    public static final String TREE_TABLE_COLUMN_A = "a";
    public static final String TREE_TABLE_COLUMN_B = "b";
    public static final String TREE_TABLE_COLUMN_C = "c";
    public static final String TREE_TABLE_COLUMN_D = "d";
    public static final String TREE_TABLE_COLUMN_R_SQUARED = "R^2";
    
    public static final int TREE_TABLE_COLUMN_R_SQUARED_INDEX = 6;
    
    public static final String[] NOCIT_NOC_DROPDOWN_OPTIONS = new String[]{"1", "2", "3", "4", "5", "6"};
    public static final String[] CEESIT_NOC_DROPDOWN_OPTIONS = new String[]{"1", "2", "3"};
    
    public static final String NOCIT_TABLE_COLUMN_GRAPH = "Graph";
    public static final String NOCIT_TABLE_COLUMN_CASE = "Case #";
    public static final String NOCIT_TABLE_COLUMN_SAMPLE = "Sample";
    public static final String NOCIT_TABLE_COLUMN_FILTER = "Filter";
    public static final String NOCIT_TABLE_COLUMN_DNA = "DNA Input (ng)";
    public static final String NOCIT_TABLE_COLUMN_POPULATION = "Population";
    public static final String NOCIT_TABLE_COLUMN_NOC = "Max NOC";
    public static final String NOCIT_TABLE_COLUMN_CALIBRATION = "Calibration";
    public static final String NOCIT_TABLE_COLUMN_OUTPUT = "Output";
    public static final String NOCIT_TABLE_COLUMN_COMMENTS = "Comments";
    public static final String NOCIT_TABLE_COLUMN_THRESHOLDS = "Analytical Threshold Index";	
    public static final String NOCIT_TABLE_COLUMN_ROW_ID = "Row ID";
    
    public static final String[] NOCIT_COLUMN_NAMES = {
    	NOCIT_TABLE_COLUMN_GRAPH, NOCIT_TABLE_COLUMN_CASE,
    	NOCIT_TABLE_COLUMN_SAMPLE,
    	NOCIT_TABLE_COLUMN_FILTER, NOCIT_TABLE_COLUMN_DNA,
    	NOCIT_TABLE_COLUMN_POPULATION, NOCIT_TABLE_COLUMN_NOC, 
    	NOCIT_TABLE_COLUMN_CALIBRATION, NOCIT_TABLE_COLUMN_OUTPUT,
    	NOCIT_TABLE_COLUMN_COMMENTS, NOCIT_TABLE_COLUMN_THRESHOLDS, NOCIT_TABLE_COLUMN_ROW_ID};
    
    public static java.util.List<String> nocItColumnsList = Arrays.asList(NOCIT_COLUMN_NAMES);
    
    public static final int NOCIT_TABLE_COLUMN_GRAPH_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_GRAPH);
    public static final int NOCIT_TABLE_COLUMN_CASE_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_CASE);
    public static final int NOCIT_TABLE_COLUMN_SAMPLE_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_SAMPLE);
    public static final int NOCIT_TABLE_COLUMN_FILTER_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_FILTER);
    public static final int NOCIT_TABLE_COLUMN_DNA_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_DNA);
    public static final int NOCIT_TABLE_COLUMN_POPULATION_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_POPULATION);
    public static final int NOCIT_TABLE_COLUMN_NOC_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_NOC);
    public static final int NOCIT_TABLE_COLUMN_CALIBRATION_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_CALIBRATION);
    public static final int NOCIT_TABLE_COLUMN_OUTPUT_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_OUTPUT);
    public static final int NOCIT_TABLE_COLUMN_COMMENTS_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_COMMENTS);
    public static final int NOCIT_TABLE_COLUMN_THRESHOLDS_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_THRESHOLDS);
    public static final int NOCIT_TABLE_COLUMN_ROW_ID_INDEX = nocItColumnsList
    		.indexOf(NOCIT_TABLE_COLUMN_ROW_ID);
    
    public static final int NOCIT_TABLE_COLUMN_GRAPH_WIDTH = 50;
    public static final int NOCIT_TABLE_COLUMN_CASE_WIDTH = 75;
    public static final int NOCIT_TABLE_COLUMN_SAMPLE_WIDTH = 100;
    public static final int NOCIT_TABLE_COLUMN_FILTER_WIDTH = 50;
    public static final int NOCIT_TABLE_COLUMN_DNA_WIDTH = 50;
    public static final int NOCIT_TABLE_COLUMN_POPULATION_WIDTH = 75;
    public static final int NOCIT_TABLE_COLUMN_NOC_WIDTH = 65;
    public static final int NOCIT_TABLE_COLUMN_CALIBRATION_WIDTH = 75;
    public static final int NOCIT_TABLE_COLUMN_OUTPUT_WIDTH = 200;
    public static final int NOCIT_TABLE_COLUMN_COMMENTS_WIDTH = 75;   
    // for most columns this will allow full column name to be visible
    public static final int NOCIT_TABLE_COLUMN_MIN_WIDTH = 50;
    
    public static final String CEESIT_TABLE_COLUMN_GRAPH = "Graph";
    public static final String CEESIT_TABLE_COLUMN_CASE = "Case #";
    public static final String CEESIT_TABLE_COLUMN_SAMPLE = "Sample";
    public static final String CEESIT_TABLE_COLUMN_FILTER = "Filter";
    public static final String CEESIT_TABLE_COLUMN_DNA = "DNA Input (ng)";
    public static final String CEESIT_TABLE_COLUMN_POPULATION = "Population";
    public static final String CEESIT_TABLE_COLUMN_NOC = "NOC";
    public static final String CEESIT_TABLE_COLUMN_CALIBRATION = "Calibration";
    public static final String CEESIT_TABLE_COLUMN_POI = "Person Of Interest";
    public static final String CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS = "Known Contributors";
    public static final String CEESIT_TABLE_COLUMN_OUTPUT = "Output";
    public static final String CEESIT_TABLE_COLUMN_COMMENTS = "Comments";
    public static final String CEESIT_TABLE_COLUMN_ROW_ID = "Row ID";
    
    public static final String[] CEESIT_COLUMN_NAMES = {
    	CEESIT_TABLE_COLUMN_GRAPH, CEESIT_TABLE_COLUMN_CASE,
    	CEESIT_TABLE_COLUMN_SAMPLE,
    	CEESIT_TABLE_COLUMN_FILTER, CEESIT_TABLE_COLUMN_DNA,
    	CEESIT_TABLE_COLUMN_POPULATION, CEESIT_TABLE_COLUMN_NOC, 
    	CEESIT_TABLE_COLUMN_CALIBRATION, CEESIT_TABLE_COLUMN_POI, 
    	CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS, CEESIT_TABLE_COLUMN_OUTPUT,
    	CEESIT_TABLE_COLUMN_COMMENTS, CEESIT_TABLE_COLUMN_ROW_ID};
    
    public static java.util.List<String> ceesItColumnsList = Arrays.asList(CEESIT_COLUMN_NAMES);
    
    public static final int CEESIT_TABLE_COLUMN_GRAPH_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_GRAPH);
    public static final int CEESIT_TABLE_COLUMN_CASE_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_CASE);
    public static final int CEESIT_TABLE_COLUMN_SAMPLE_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_SAMPLE);
    public static final int CEESIT_TABLE_COLUMN_FILTER_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_FILTER);
    public static final int CEESIT_TABLE_COLUMN_DNA_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_DNA);
    public static final int CEESIT_TABLE_COLUMN_POPULATION_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_POPULATION);
    public static final int CEESIT_TABLE_COLUMN_NOC_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_NOC);
    public static final int CEESIT_TABLE_COLUMN_CALIBRATION_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_CALIBRATION);
    public static final int CEESIT_TABLE_COLUMN_POI_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_POI);
    public static final int CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS);
    public static final int CEESIT_TABLE_COLUMN_OUTPUT_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_OUTPUT);
    public static final int CEESIT_TABLE_COLUMN_COMMENTS_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_COMMENTS);
    public static final int CEESIT_TABLE_COLUMN_ROW_ID_INDEX = ceesItColumnsList
    		.indexOf(CEESIT_TABLE_COLUMN_ROW_ID);
    
    public static final int CEESIT_TABLE_COLUMN_GRAPH_WIDTH = 50;
    public static final int CEESIT_TABLE_COLUMN_CASE_WIDTH = 75;
    public static final int CEESIT_TABLE_COLUMN_SAMPLE_WIDTH = 100;
    public static final int CEESIT_TABLE_COLUMN_FILTER_WIDTH = 50;
    public static final int CEESIT_TABLE_COLUMN_DNA_WIDTH = 50;
    public static final int CEESIT_TABLE_COLUMN_POPULATION_WIDTH = 75;
    public static final int CEESIT_TABLE_COLUMN_NOC_WIDTH = 65;
    public static final int CEESIT_TABLE_COLUMN_CALIBRATION_WIDTH = 75;
    public static final int CEESIT_TABLE_COLUMN_POI_WIDTH = 75;
    public static final int CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_WIDTH = 75;
    public static final int CEESIT_TABLE_COLUMN_OUTPUT_WIDTH = 200;
    public static final int CEESIT_TABLE_COLUMN_COMMENTS_WIDTH = 75;   
    // for most columns this will allow full column name to be visible
    public static final int CEESIT_TABLE_COLUMN_MIN_WIDTH = 50;
    
    // Width allows up to 3 Known Contributors to be selected without showing ellipsis (...).
    public static final int CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_COMBO_WIDTH = 110;
    
    public static final String CEESIT_NO_KNOWN_CONTRIBUTORS_ENTRY = "None";
    
    public static final int COMMENTS_BOX_X_SIZE = 600;
    public static final int COMMENTS_BOX_Y_SIZE = 500;
    
    public static final String CEESIT_TABLE_CONTEXT_MENU_OPTION = "View/Edit POI Genotype";
    public static final String CEESIT_TABLE_CONTEXT_MENU_OPTION_DUPLICATE = "Create POI Genotype Copy";
    
    public static final String POI_GENOTYPE_TABLE_CONTEXT_MENU_OPTION_ADD_NEW = "Add New Genotype";
    public static final String POI_GENOTYPE_TABLE_CONTEXT_MENU_OPTION_DUPLICATE = "Create POI Genotype Copy";
    public static final String POI_GENOTYPE_TABLE_CONTEXT_MENU_OPTION = "View/Edit POI Genotype";
    public static final String POI_GENOTYPE_TABLE_CONTEXT_MENU_OPTION_DELETE = "Delete Genotype";
    
    public static final String CALIBRATION_TABLE_COLUMN_FILE_NAME = "File Name";
    public static final String CALIBRATION_TABLE_COLUMN_SAMPLE_NAME = "Sample Name";
    public static final String CALIBRATION_TABLE_COLUMN_FILTER = "Filter";
    public static final String CALIBRATION_TABLE_COLUMN_DNA_MASS = "DNA Mass (ng)";
    public static final String CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE = "Known Genotype";
    
    public static final String[] CALIBRATION_COLUMN_NAMES = {
    		CALIBRATION_TABLE_COLUMN_FILE_NAME, 
    		CALIBRATION_TABLE_COLUMN_SAMPLE_NAME,
    		CALIBRATION_TABLE_COLUMN_FILTER, CALIBRATION_TABLE_COLUMN_DNA_MASS,
    		CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE};
    
    public static java.util.List<String> calibrationColumnsList = Arrays.asList(CALIBRATION_COLUMN_NAMES);
    
    public static final int CALIBRATION_TABLE_COLUMN_FILE_NAME_INDEX = calibrationColumnsList
    		.indexOf(CALIBRATION_TABLE_COLUMN_FILE_NAME);
    public static final int CALIBRATION_TABLE_COLUMN_SAMPLE_NAME_INDEX = calibrationColumnsList
    		.indexOf(CALIBRATION_TABLE_COLUMN_SAMPLE_NAME);
    public static final int CALIBRATION_TABLE_COLUMN_FILTER_INDEX = calibrationColumnsList
    		.indexOf(CALIBRATION_TABLE_COLUMN_FILTER);
    public static final int CALIBRATION_TABLE_COLUMN_DNA_MASS_INDEX = calibrationColumnsList
    		.indexOf(CALIBRATION_TABLE_COLUMN_DNA_MASS);
    public static final int CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE_INDEX = calibrationColumnsList
    		.indexOf(CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE);
    
    public static final String VIEW_GENOTYPES_TABLE_CONTEXT_MENU_OPTION_ADD_NEW = "Add New Genotype";
    public static final String VIEW_GENOTYPES_TABLE_CONTEXT_MENU_OPTION_DUPLICATE = "Copy Genotype";
    public static final String VIEW_GENOTYPES_TABLE_CONTEXT_MENU_OPTION_EDIT = "View/Edit Genotype";
    public static final String VIEW_GENOTYPES_TABLE_CONTEXT_MENU_OPTION_DELETE = "Delete Genotype";
    
    public static final int VIEW_GENOTYPES_TABLE_COLUMNS_MIN_WIDTH = 75;
        
    public static final String GENOTYPE_TABLE_COLUMN_LOCUS = "Locus";    
    public static final String GENOTYPE_TABLE_COLUMN_ALLELE1 = "Allele1"; 
    public static final String GENOTYPE_TABLE_COLUMN_ALLELE2 = "Allele2";
    public static final String GENOTYPE_VALUE_X = "X";
    public static final String GENOTYPE_VALUE_Y = "Y";
    
    public static final String GENOTYPE_TABLE_CONTEXT_MENU_COPY = "Copy";
    public static final String GENOTYPE_TABLE_CONTEXT_MENU_COPY_ERROR_MESSAGE = "There was an error copying from the table to the clipboard. Please try again.";
    public static final String GENOTYPE_TABLE_CONTEXT_MENU_COPY_ERROR_LOG_MESSAGE = "Copy Error";
    public static final String GENOTYPE_TABLE_CONTEXT_MENU_PASTE = "Paste";
    public static final String GENOTYPE_TABLE_CONTEXT_MENU_PASTE_ERROR_MESSAGE = "There was an error parsing the clipboard data. Please try again.";
    public static final String GENOTYPE_TABLE_CONTEXT_MENU_PASTE_ERROR_LOG_MESSAGE = "Paste Error";
    
    public static final String GENOTYPE_POPUP_BUTTON_SAVE_CLOSE = "Save and Close";
    public static final String GENOTYPE_POPUP_BUTTON_CANCEL = "Cancel";
    public static final String GENOTYPE_POPUP_ERROR_NAME_CHECK = "Please enter a Genotype ID.";
    public static final String GENOTYPE_POPUP_LABEL_NAME = "Genotype ID";
    public static final String GENOTYPE_POPUP_ADD_TITLE = "Add New Genotype"; 
    public static final String GENOTYPE_POPUP_EDIT_TITLE = "Edit Genotype";    
    public static final String GENOTYPE_POPUP_ERROR_STR_ALLELE_CHECK = "Please enter a valid STR allele for locus";
    public static final String GENOTYPE_POPUP_ERROR_AMEL_ALLELE_CHECK = "Please enter either X or Y for AMEL Allele.";
        
    public static final String POPULATION_TABLE_COLUMN_ALLELE = "AlleleID"; 
    public static final String POPULATION_TABLE_COLUMN_FREQ = "Frequency";
    public static final String POPULATION_POPUP_LABEL_NAME = "Population Name";
    public static final String POPULATION_VIEWER_POPUP_TITLE = "View Population";
     
    public static final String SETTINGS_POPUP_TITLE = "Settings";
    public static final String SETTINGS_POPUP_LOAD_ERROR_LOG_MESSAGE = "Error Loading Settings Window: ";
    
    // used by project file filter
    public static final String CALIBRATION_FILE_NAME = "Calibration Files";
    public static final String CALIBRATION_FILE_EXTENSION = ".zip";
    public static final String INVALID_CALIBRATION_FILE_TYPE_ERROR_MESSAGE = "The selected file is not of the correct type. Please load a .zip file";
    public static final String INVALID_CALIBRATION_FILE_ERROR_MESSAGE = "The selected file is not a valid calibration file";
    public static final String CALIBRATION_NOT_CALCULATED_ERROR_MESSAGE = "The calibration file selected has not been calculated";
    
    public static final String LOG_FILE_EXTENSION = ".txt";
    
    public static final String CALCULATIONS_DNA_MASS_ERROR_MESSAGE = "Please make sure each DNA Mass value is a valid decimal number and is greater than zero.";
    public static final String INVALID_DOUBLE_ERROR_TITLE = "Invalid Entry Error";
    public static final String INVALID_DOUBLE_ERROR_MESSAGE = "Entry is not a number.";
    //directories used for writing log files
 	// The suffix must be used as there are bugs in getting the application data
 	// folder
 	// in Windows XP -
 	// http://stackoverflow.com/questions/1198911/how-to-get-local-application-data-folder-in-java
 	public static final String SETTINGS_PATH_SUFFIX_WINDOWS_7 = "/AppData/Local";
 	public static final String SETTINGS_PATH_SUFFIX_WINDOWS_XP = "/Local Settings/Application Data";

 	public static final String FOLDER_NAME = "NOCIt";
    public static final String EMPTY_CALIBRATION_VALUES_TABLE_SAVE_MESSAGE = "Calibration Values Table is Empty. "
    		+ "There is No Data to Save";
    
    public static final boolean DELETE_EMPTY_LOG_FILES = true;    
    
    public static final String CALCULATIONS_MISSING_VALUE_ERROR_MESSAGE = "Please make sure each table cell has a value. \n(Make sure there aren't any cells containing '"+ Constants.DEFAULT_CHOOSE + "')";
    public static final String DELETE_ROW_ERROR_MESSAGE = "Error deleting row.";
    public static final String DUPLICATE_OUTPUT_FILE_ERROR_MESSAGE = "Please make sure each output file is different for each population."
    		+ " (Outputs files can be the same if populations are different.)";
    public static final String FILE_EXISTS_WARNING_MESSAGE = "Warning: One or more output files already exist.";
    public static final String CEESIT_NOC_ERROR_MESSAGE = "Please make sure the number of contributors (NOC) is greater than the number of Known Contributors.";
    public static final String RESOLVIT_MISSING_VALUE_ERROR_MESSAGE = "Please make sure that all entries have a value (i.e. there are no blank entries).";

    public static final String ERROR_TITLE = "Error";
    public static final String WARNING_TITLE = "Warning";
    public static final String FILTERBOX_TITLE = "Filter";
    public static final String DATABASE_ERROR = "Database Error";
    public static final String ERROR_LAUNCHING_NOCIt = "NOCIt was unable to be launched";
    
    public static final String DEFAULT_CHOOSE = "<Choose>";
    public static final String DEFAULT_ADD_NEW = "<Add New>";
    public static final String DEFAULT_OUTPUT_CHOICE_STRING = "<Default>";
    
    public static final String KNOWN_GENOTYPE_DEFAULT_VALUE = DEFAULT_CHOOSE;
    
    public static final String SETTINGS_FILENAME = "Settings.properties";
    public static final String SETTINGS_LOAD_ERROR_LOG_MESSAGE = "Error Loading Settings Properties: ";
    public static final String SETTINGS_SAVE_ERROR_LOG_MESSAGE = "Error Saving Settings Properties: ";
    
    public static final String CALIBRATION_FILES_LOAD_ERROR_LOG_MESSAGE = "Error Loading Calibration Files: ";
    
    public static final String CHOOSE_DEFAULT_DIRECTORY = "Select Default Directory...";
    public static final String CHOOSE_DEFAULT_DIRECTORY_ERROR_LOG_MESSAGE = "The default file directory has been changed. \n\nIf this directory includes Calibration files (.cal), you will need to restart the application to load them.";
    
    public static final String SELECT_FILE = "Select File...";
    public static final String SELECT_FOLDER = "Select Folder...";
    
    public static final String CALIBRATION = "Calibration";
    public static final String CALIBRATION_SUCCESSFULLY_SAVED = "The calibration was successfully saved.";
    
    public static final String SAVE_OUTPUT_FILE = "Save Output File...";
    
    public static final String SAVE_PROGRESS = "Save Calibration?";
    public static final String SAVE_PROGRESS_MESSAGE = "Would you like to save your calibration before closing the application?";
    public static final String SAVE_PROGRESS_ERROR_LOG_MESSAGE = "Error Saving Application Progress: ";
    public static final String LOAD_PROGRESS_ERROR_LOG_MESSAGE = "Error Loading Application Progress: ";
    
    public static final String FILTER_HEIGHT_RATIO_ERROR = "Filter Height Ratio is not in a decimal number format.";
    public static final String FILTER_SIZE_RATIO_ERROR = "Filter Size Ratio is not in a decimal number format.";
    public static final String A_MINUS_PEAK_HEIGHT_RATIO_ERROR = "Filter Height Ratio is not in a decimal number format.";
    public static final String A_MINUS_PEAK_SIZE_RATIO_ERROR = "Filter Size Ratio is not in a decimal number format.";
    
    public static final String NUMBER_POI_SAMPLES_ERROR = "Enter a valid integer for Number of POI Samples.";
    
    public static final String NON_NUMERIC_ERROR_MESSAGE = "Value entered is not a number";
    public static final String NOT_INTEGER_ERROR_MESSAGE = "Value entered is not a integer";
    public static final String VALUE_OUT_OF_RANGE_ERROR_MESSAGE = "Value entered must be ";
    
    public static final String OPEN_LOGS_ERROR_LOG_MESSAGE = "Error Opening Logs Folder: ";
    public static final String DELETE_LOG_FILE_ERROR = "Error when attempting to delete empty log file";
    public static final String LOAD_PROJECT_DATA_ERROR = "Error loading project data";
    
    public static final String VALIDATE_KNOWN_GENOTYPE_BULK_IMPORT_FILES_ERROR = "Error validating known genotypes bulk import files";
    public static final String READ_KNOWN_GENOTYPE_BULK_IMPORT_FILES_ERROR = "Error reading known genotypes bulk import files";
    
    public static final String EDIT_ANALYTICAL_THRESHOLDS_ERROR = "Error editing analytical_thresholds";
    public static final String CONFIGURE_NOCIt_BATCH_RUN_ERROR = "Error configuring NOCIt batch run";
    public static final String ERROR_LOADING_POPULATION_FREQUENCIES = "Error loading Population Frequencies dialog";
    
    public static final String TIME_ELAPSED = "Time Elapsed: ";
    
    public static final String PLOT_GRAPH_ERROR_MESSAGE_LOG = "plotGraph() function was somehow called before a Calibration() object was defined in the BackendController.";
    public static final String SAVE_CALIBRATION_ERROR_MESSAGE_LOG = "saveCalibration() function was somehow called before a Calibration() object was defined in the BackendController.";
    
    public static final String RUN_NOCIT_THREAD_ERROR_LOG_MESSAGE = "Error Running NOCIt Thread: ";
    public static final String RUN_CEESIT_THREAD_ERROR_LOG_MESSAGE = "Error Running CEESIt Thread: ";
    public static final String RUN_RESOLVIT_THREAD_ERROR_LOG_MESSAGE = "Error Running ReSOLVIt Thread: ";
    public static final String RUN_NOCIT_ERROR_LOG_MESSAGE = "Error Running NOCIt: ";
    public static final String CANCEL_NOCIT_ERROR_LOG_MESSAGE = "Error Canceling NOCIt: ";    
    public static final String RUN_CEESIT_ERROR_LOG_MESSAGE = "Error Running CEESIt: "; // AA
    public static final String CANCEL_CEESIT_ERROR_LOG_MESSAGE = "Error Canceling CEESIt: ";  // AA
    public static final String RUN_RESOLVIT_ERROR_LOG_MESSAGE = "Error Running ReSOLVIt: ";
    public static final String CANCEL_RESOLVIT_ERROR_LOG_MESSAGE = "Error Canceling ReSOLVIt: ";
    public static final String CREATE_PDF_REPORT_ERROR_LOG_MESSAGE = "Error Creating PDF Report: ";
    public static final String CALCULATE_CURVE_FIT_ERROR_LOG_MESSAGE = "Error Calculating Curve Fit Function: ";
    public static final String CALCULATE_PARAMETERS_ERROR_LOG_MESSAGE = "Error Calculating Parameters TreeTable: ";
    
    public static final String FREQ_FILE_EXTENSION_1 = "Frequency File (*.csv)";
    public static final String FREQ_FILE_EXTENSION_2 = "*.csv";
    public static final String GENO_FILE_EXTENSION_1 = "Genotype File (*.csv)";
    public static final String GENO_FILE_EXTENSION_2 = "*.csv";
    public static final String BINS_FILE_EXTENSION_1 = "Bins File (*.txt)";
    public static final String BINS_FILE_EXTENSION_2 = "*.txt";
    
    public static final String CALIBRATION_SAVE_ERROR_LOG_MESSAGE = "Error Saving Calibration: ";
    public static final String CALIBRATION_LOAD_ERROR_LOG_MESSAGE = "Error Loading Calibration: ";
    
    public static final String FILTERING_READ_FILE_ERROR_LOG_MESSAGE = "Error Reading Sample File: ";
    public static final String FILTERING_SAVE_FILE_ERROR_LOG_MESSAGE = "Error Saving Filtered Sample File: ";

    public static final String PARSE_SAMPLE_FILE_ERROR_LOG_MESSAGE = "Error Parsing Sample File: ";
    
    public static final String INVALID_SAMPLE_FILES_WARNING = "The following files in the Sample File(s) Folder are not valid: ";
    
    public static final String USED_GENOTYPE_DELETION_WARNING_QUESTION = "Genotype is present in Calibration Table Known"
    		+ " Genotypes Column. If this Genotype is deleted all occurrences will be replaced with"
    		+ " " + DEFAULT_CHOOSE + ". Delete Genotype?";
    
    public static final String USED_GENOTYPE_EDIT_WARNING_QUESTION = "Genotype is present in Calibration Table Known"
    		+ " Genotypes Column. If this Genotype is edited all occurrences will be replaced with"
    		+ " the new Genotype ID. Edit Genotype?";
    
    public static final String SETTINGS_CHANGED_MATCH_GENOTYPES_QUESTION = "Do you want to match Known" 
    		+ " Genotypes with Saved Settings?";
    
    public static final String SETTINGS_CHANGED_MATCH_GENOTYPES_QUESTION_2 = " Matching genotypes will clear calculations.";
    
    public static final String LOCI_NOT_FOUND_IN_GENOTYPES_MESSAGE = "The following loci were not found in genotypes: ";
    
    public static final String NOCIt_BATCH_RUN_CALIBRATION_SELECTION_ERROR = "Error from selection"
    		+ " of calibration in NOCIt Batch Run";
    public static final String NOCIt_BATCH_RUN_SAMPLE_FILE_ALREADY_LOADED_WARNING = "There was an error uploading the sample files. Please \"Clear All\" before attempting to upload samples.";
    public static final String NOCIt_BATCH_RUN_SELECT_CALIBRATION_MESSAGE = "Please select a Calibration Project before adding samples.";
    public static final String NOCIt_OUTPUT_FILE_OVERWRITE_WARNING = "One or more output files already exists and will be"
			+ " overwritten if NOCIt is run. Continue?";
    
    public static final String NO_POPULATIONS_EXIST_ERROR = "A NOCIt Batch cannot be "
    		+ "created because no populations currently exist. Do you wish "
    		+ "to create a Population?";
    
    public static final String NO_POPULATIONS_EXIST_CEESIT_ERROR = "A CEESIt Batch cannot be "
    		+ "created because no populations currently exist. Do you wish "
    		+ "to create a Population?";
    
    public static final String CEESIt_OUTPUT_FILE_OVERWRITE_WARNING = "One or more output files already exists and will be"
			+ " overwritten if CEESIt is run. Continue?";
    
    public static final int ANALYTICAL_THRESHOLD_MAX_VALUE = 10000000;
    public static final String ANALYTICAL_THRESHOLD_ERROR_NUMBER_CHECK = "Please enter an integer >= 1 and <= " + ANALYTICAL_THRESHOLD_MAX_VALUE;
    
    public static final String DUPLICATE_POPULATION_NAME_PREFIX = "The Population Name ";
    public static final String DUPLICATE_POPULATION_NAME_SUFFIX = " is already in use. Please enter another name";
    
    public static final String NON_NUMERIC_NUMBER_OF_PEOPLE_ERROR = "Entry in Number of People field is not a number.";
    
    public static final String NOCIT_SAMPLES_UNABLE_TO_RUN_ERROR = "The following samples were unable to be run: ";
    public static final String NOCIT_PDFS_NOT_WRITTEN_ERROR = "The following pdf reports were unable to be written: ";
    
    public static final String DISABLE_COLUMN_REORDERING_ERROR = "Disable Column Reordering Error";
    
    public static final String SHOW_HELP_LOAD_ERROR = "Help was unable to load";
    
    public static class Terms {
        public static final String AMEL = "AMEL";       
        public static final String OL = "OL";
        public static final String MALE = "M";
        public static final String FEMALE = "F";
    }
    
    //UI tooltips
    public static final String NOCIt_PLUS_BUTTON_TOOLTIP = "Add a batch run";
    public static final String NOCIt_MINUS_BUTTON_TOOLTIP = "Remove selected row(s) from NOCIt table";
    public static final String START_NOCIt_BUTTON_TOOLTIP = "Start NOCIt analysis";
    public static final String CANCEL_NOCIt_BUTTON_TOOLTIP = "Immediately halt analysis";
    
    // Settings tooltips
    public static final String SAVE_CLOSE_BUTTON_TOOLTIP = "Save values in all tabs and close Settings dialog.";
    
    public static final String SAMPLE_ID_DELIMITER_LABEL_TOOLTIP = "Sample ID Delimiter\n"
    		+ "a character used to separate the different details included in the \"Sample File\" column \n"
    		+ "of the Calibration Files into different indices. It is used to recognize the Genotype \n"
    		+ "ID for automatic assignment of the Known Genotypes to the Calibration Files. For example, \n"
    		+ "if a Calibration File is named \"03-1ng,\" the delimiter may be set to a hyphen (-) to \n"
    		+ "separate the Genotype ID (\"03\") from the rest of the string. Commonly used delimiters \n"
    		+ "include symbols such as hyphens and underscores.";
    public static final String GENOTYPE_ID_INDEX_LABEL_TOOLTIP = "Genotype ID Index\n"
    		+ "a positive integer (may also be zero) which indicates the index or group containing the \n"
    		+ "Genotype ID. It is used to recognize the Genotype ID for automatic assignment of the cell \n"
    		+ "types in the Known Genotypes File to the Calibration Files. Where the Sample ID Delimiter \n"
    		+ "may be thought of as a character which splits or divides a sample name string into \"groups,\" \n"
    		+ "the Genotype ID Index is a number which tells the software which \"group\" contains the \n"
    		+ "Genotype ID. Following the example proposed above, if a Calibration File is named \"03-1ng\" \n"
    		+ "and the delimiter is set to a hyphen, the user should specify the Genotype ID Index as \"1,\" \n"
    		+ "indicating to the software that the Genotype ID is \"03.\" ";
    public static final String MAX_NUM_PROCESSORS_LABEL_TOOLTIP = "Maximum # of Processors\n"
    		+ "a non-zero, positive integer selected form a dropdown menu which controls the maximum number \n"
    		+ "of processors which NOCIt may utilize during computation. NOCIt will detect the total number \n"
    		+ "of processors on any PC and automatically set the maximum number of processors to the total \n"
    		+ "number of processors present. Generally, run times decrease with increasing numbers of processors; \n"
    		+ "thus, to minimize run-time, it is advantageous to maximize the number of processors utilized during \n"
    		+ "NOCIt analysis. Because this can be taxing on the operating system if other programs in addition to \n"
    		+ "NOCIt are running in parallel, the user may choose to utilize only a portion of the processors \n"
    		+ "available on the system for NOCIt computation.";
    public static final String PLOT_ALL_CALIBRATION_POINTS_LABEL_TOOLTIP = "Plot all Calibration Graph Points:\n"
    		+ "a check-box option to plot all points on the calibration graphs. The calibration graph at each locus \n"
    		+ "for every parameter will vary in the total number of data points per graph. To view all data points, \n"
    		+ "click on the check-box by this setting. The input field corresponding to the proceeding setting, \n"
    		+ "Max. # of Calibration Graph Points, will automatically be greyed-out and inactivated. Note that \n"
    		+ "for large datasets (i.e., many Calibration Files), there may thousands of data points on certain \n"
    		+ "calibration graphs which will cause the graphs to load slowly. Choosing to plot all data points \n"
    		+ "will not affect the calculated model parameters and only changes the number of data points \n"
    		+ " displayed on the graphs.";
    public static final String CALIBRATION_GRAPH_MAX_POINTS_LABEL_TOOLTIP = "Maximum # of Calibration Graph Points\n"
    		+ "a positive, non-zero integer which dictates the maximum number of points plotted on the calibration \n"
    		+ "graphs. The NOCIt default setting is 4000. Users may specify a lower value to decrease the time it \n"
    		+ "takes for the calibration graphs to load and be displayed. Altering this value will not affect the \n"
    		+ "calculated model parameters and will only change the number of data points displayed on the graphs. \n"
    		+ "If the total number of data points exceeds this setting for a given graph, a subset of the data \n"
    		+ "points will be selected at random and displayed.";
    public static final String RESET_DEFAULT_GENERAL_BUTTON_TOOLTIP = "Reset to Default\n"
    		+ "Reset General tab settings to default values.";
    public static final String SAVE_CLOSE_GENERAL_BUTTON_TOOLTIP = "Save and Close\n"
    		+ SAVE_CLOSE_BUTTON_TOOLTIP;

    public static final String PULL_UP_HEIGHT_RATIO_LABEL_TOOLTIP = "Pull-up Height Ratio\n"
    		+ "the maximum allowable value for the height of a pull-up peak divided by the height of a parent peak \n"
    		+ "in percent (%) form.";
    public static final String PULL_UP_SIZE_RANGE_LABEL_TOOLTIP = "Pull-up Size Range\n"
    		+ "a value in base pairs which accounts for variations in sizing/peak migration between the pull-up and parent peaks. ";
    public static final String COMPLEX_PULL_UP_HEIGHT_RATIO_LABEL_TOOLTIP = "Complex Pull-up Height Ratio\n"
    		+ "the maximum allowable value for the height of a pull-up peak divided by the height of the parent peak (i.e., \n"
    		+ "the shorter of the two sister alleles) in percent (%) form.";
    public static final String COMPLEX_PULL_UP_SISTER_HEIGHT_RATIO_LABEL_TOOLTIP = "Complex Pull-up Sister Height Ratio\n"
    		+ "the minimum acceptable value for the height of one sister allele divided by the other sister allele in \n"
    		+ "percent (%) form. ";
    public static final String COMPLEX_PULL_UP_SIZE_RANGE_LABEL_TOOLTIP = "Complex Pull-up Size Range\n"
    		+ "a value in base pairs which accounts for variations in sizing/peak migration between the pull-up and parent peaks. ";
    public static final String MINUS_A_HEIGHT_RATIO_LABEL_TOOLTIP = "Minus-A Height Ratio\n"
    		+ "the maximum allowable value for the height of a minus A peak divided by the height of a plus A in percent (%) form.";
    public static final String MINUS_A_SIZE_RANGE_LABEL_TOOLTIP = "Minus-A Size Range\n"
    		+ "a value in base pairs which accounts for variations in sizing/peak migration between the minus A and plus A peaks.";
    public static final String RESET_DEFAULT_FILTER_BUTTON_TOOLTIP = "Reset to Default\n"
    		+ "Reset Filter tab settings to default values.";
    public static final String SAVE_CLOSE_FILTER_BUTTON_TOOLTIP = "Save and Close\n"
    		+ SAVE_CLOSE_BUTTON_TOOLTIP;

    public static final String LEVELS_FOR_MIXTURE_RATIOS_LABEL_TOOLTIP = "Levels for Mixture Ratios\n"
    		+ "the number of evenly spaced discretization levels used for mixture ratios. For example, if there are 16 \n"
    		+ "levels, then a contributor in a mixture can contribute any fraction i/16 of the DNA of the sample, where i \n"
    		+ "is an integer greater than or equal to 1 and less than or equal to 16. The larger the value for this setting, \n"
    		+ "the longer NOCIt will take to run, but the more accurate its results will be in theory. In testing, settings \n"
    		+ "of 8 and 16 have been found to work well.";
    public static final String STD_ERROR_TOLERANCE_LABEL_TOOLTIP = "Standard Error Tolerance\n"
    		+ "the tolerance level on the estimated standard error of the a posteriori probability distribution used for \n"
    		+ "termination. NOCIt first performs an initial Monte Carlo sampling for each number of contributors from 0 to \n"
    		+ "the maximum number of contributors. After this initial sampling, NOCIt selectively performs more Monte Carlo \n"
    		+ "sampling until either (1) the estimated standard error of the a posterior probability for all numbers of \n"
    		+ "contributors is below this setting, or (2) the running time exceeds the refinement time limit (see Refinement \n"
    		+ "Time Limit).";
    public static final String REFINEMENT_TIME_LIMIT_LABEL_TOOLTIP = "Refinement Time Limit\n"
    		+ "the time limit applied during the refinement phase, after the initial sampling is complete (see Standard Error \n"
    		+ "Tolerance).  A setting of 0 disables the refinement time limit; NOCIt runs for as long as needed to satisfy the \n"
    		+ "standard error tolerance.";
    public static final String NUM_SAMPLES_IN_BATCH_FOR_NOC_1_LABEL_TOOLTIP = "# Samples in Batch for NOC = 1\n"
    		+ "number of Monte Carlo samples used at each locus during the initial Monte Carlo sampling (see Standard Error \n"
    		+ "Tolerance above) for the number of contributors set to 1.";
    public static final String MULTIPLICATIVE_FACTOR_LABEL_TOOLTIP = "Multiplicative Factor\n"
    		+ "the multiplicative factor used to determine the number of Monte Carlo samples used at each locus during the initial \n"
    		+ "Monte Carlo sampling (see Standard Error Tolerance above) for numbers of contributors greater than 1. The multiplicative \n"
    		+ "factor is applied each time the number of contributors is increased by 1. For example, if this setting is set to 4, then \n"
    		+ "the number of samples used at each locus during the initial Monte Carlo sampling is 4 times greater for the number of \n"
    		+ "contributors set to 2 than it is for the number of contributors set to 1, and it is 16 times greater for the number of \n"
    		+ "contributors set to 3 than it is for the number of contributors set to 1.";
    public static final String MAX_SAMPLES_TOOLTIP = "Maximum Number of Samples in Batch.";
    public static final String RESET_DEFAULT_NOCIt_BUTTON_TOOLTIP = "Reset to Default\n"
    		+ "Reset NOCIt tab settings to default values.";
    public static final String SAVE_CLOSE_NOCIt_BUTTON_TOOLTIP = "Save and Close\n"
    		+ SAVE_CLOSE_BUTTON_TOOLTIP;
    public static final String BIN_WIDTH_LABEL_TOOLTIP = "Minimum Bin Width\n"
    		+ "minimum width of a bin in log-likelihood ratio histogram in dB.";
    public static final String NUM_BINS_LABEL_TOOLTIP = "Maximum Number of Bins\n"
    		+ "maximum number of bins in log-likelihood ratio histogram.";
    public static final String POI_SAMPLES_LABEL_TOOLTIP = "POI Samples\n"
    		+ "";
    public static final String GENOTYPE_TOLERANCE_LABEL_TOOLTIP = "Genotype Tolerance\n"
    		+ "The standard error tolerance for the probability of evidence given a\n"
    		+ "locus, set of quantification parameters, and number of contributors.\n";
    public static final String POP_SUBSTRUCTURE_ADJ_LABEL_TOOLTIP = "Population Substructure Adjustment\n"
    		+ "The average inbreeding coefficient, a value between 0.0 and 1.0.\n";
    public static final String RESET_DEFAULT_CEESIt_BUTTON_TOOLTIP = "Reset to Default\n"
    		+ "Reset CEESIt tab settings to default values.";
    public static final String SAVE_CLOSE_CEESIt_BUTTON_TOOLTIP = "Save and Close\n"
    		+ SAVE_CLOSE_BUTTON_TOOLTIP;
    
    // NOCIt batch run tooltips
    public static final String NOCIt_BATCH_RUN_CALIBRATION_LABEL_TOOLTIP = "Select <Add New> from the dropdown menu to add a calibration zip file";
    public static final String NOCIt_BATCH_RUN_OUTPUT_FOLDER_LABEL_TOOLTIP = "Use the Browse button to designate a folder where the output and result files will be saved.\n"
    		+ "On completion of analysis, this folder will contain the PDF report results for all samples analyzed. ";
    public static final String NOCIt_BATCH_RUN_OUTPUT_FOLDER_BUTTON_TOOLTIP = "Click to designate a folder where the output and result files will be saved.\n"
    		+ "On completion of analysis, this folder will contain the PDF report results\nfor all samples analyzed. ";
    public static final String NOCIt_BATCH_RUN_CSV_OUTPUT_FILE_LABEL_TOOLTIP = "The csv file will be automatically be named and exported to a default location. To specify a location or\n"
    		+ " name different from the default, use the Browse button next to CSV Output File.";
    public static final String NOCIt_BATCH_RUN_CSV_OUTPUT_FILE_BUTTON_TOOLTIP = "The csv file will be automatically be named and exported to a default location.\n"
    		+ "To specify a location or name different from the default, click here.";
    public static final String NOCIt_BATCH_RUN_INCLUDE_RESULTS_AS_CSV_LABEL_TOOLTIP = "If interested in viewing all NOCIt results in one csv file, select the Include Results as CSV option.\n"
    		+ "Note that selecting this option will not override or in any way affect the PDF reports which are\nautomatically generated for each sample analyzed in NOCIt.";
    public static final String NOCIt_BATCH_RUN_MAX_NOC_LABEL_TOOLTIP = "Using the dropdown menu, select the maximum number of contributors. The probability distribution on the number of contributors\n"
    		+ " will be calculated from 0 to the maximum number of contributors set by the user in this step. The recommended value is 5.";
    public static final String NOCIt_BATCH_RUN_FILTER_LABEL_TOOLTIP = "If interested in applying the Filter function to all samples in a batch, click the check-box next to Filter.";
    public static final String NOCIt_BATCH_RUN_POPULATION_LABEL_TOOLTIP = "Select the Populations to test the samples against. By default, all Populations previously created will be checked off. If there\n"
    		+ " are multiple Populations and the user does not wish to test against all Populations, deselect those which are not needed\n by un-checking the box beside the appropriate"
    		+ " Populations. Verify that the selected Populations correspond to the Calibration\npreviously imported (i.e., both must be derived from the same STR amplification kit).";
    public static final String NOCIt_BATCH_RUN_ADD_SAMPLE_FILES_BUTTON_TOOLTIP = "Add the samples to be analyzed. (A calibration project must be loaded before loading sample files.) "
    		+ "Use the Browse button and\n select the csv Sample File(s) of interest. All samples will appear in the empty box below the Add New Sample File(s) button.\n "
    		+ "If any samples were added which the user would like to omit from analysis, click the X button beside the appropriate samples.";
    public static final String NOCIt_BATCH_RUN_CLEAR_ALL_BUTTON_TOOLTIP = "Remove all loaded samples";
    public static final String NOCIt_BATCH_RUN_AT_BUTTON_TOOLTIP = "Input a non-zero, positive integer RFU value in the Threshold input boxes beside each locus if interested in\n"
    		+ " applying an analytical threshold(s) other than 1 RFU. Do not insert symbols, alphabetic characters, or any\n non-numerical values in"
    		+ " Threshold input boxes. Scroll through all loci and verify that the desired analytical\nthresholds have be entered properly, then click Save.";
    public static final String NOCIt_BATCH_RUN_ADD_BATCH_BUTTON_TOOLTIP = "Review all input parameters in the NOCIt Batch Run window and verify that all inputs pertain\n"
    		+ "to the same STR amplification kit, then click Add Batch. The NOCIt table will then be populated\nwith the samples and input parameters specified in the batch, as shown below. This procedure\nmay be repeated to add additional batches of samples.";
    
    // CEESIt batch run tooltips, only where different than NOCIt
    public static final String CEESIt_BATCH_RUN_MAX_NOC_LABEL_TOOLTIP = "Using the dropdown menu, select the number of contributors.";
    public static final String CEESIt_BATCH_RUN_INCLUDE_RESULTS_AS_CSV_LABEL_TOOLTIP = "If interested in viewing all CEESIt results in one csv file, select the Include Results as CSV option.\n"
    		+ "Note that selecting this option will not override or in any way affect the PDF reports which are\nautomatically generated for each sample analyzed in CEESIt.";
    public static final String CEESIt_BATCH_RUN_ADD_BATCH_BUTTON_TOOLTIP = "Review all input parameters in the CEESIt Batch Run window and verify that all inputs pertain\n"
    		+ "to the same STR amplification kit, then click Add Batch. The CEESIt table will then be populated\nwith the samples and input parameters specified in the batch, as shown below. This procedure\nmay be repeated to add additional batches of samples.";
    
    public static final String DUPLICATE_OUTPUT_TOOLTIP_PREFIX = "Duplicate Output File - ";
    public static final String FILE_EXISTS_TOOLTIP_PREFIX = "File Exists - ";
    // end tooltips

    public static final String[] NOCIT_CSV_OUTPUT_FILE_HEADER = {"Output File Name", "Sample ID", 
    		"Sample File Name", "Case Number", "Comments", "Calibration Name", "Frequency File", 
    		"Population Name", "Number of People", "Bins File", "Filtered", 
    		"Pull-up Filter Height Ratio (%)", "Pull-up Filter Size Range (bp)",
    		"Complex Pull-up Filter Height Ratio (%)", "Complex Pull-up Filter Sister Height Ratio (%)",
    		"Complex Pull-up Filter Size Range (bp)", "Minus-A Filter Height Ratio (%)", 
    		"Minus-A Filter Size Range (bp)", "Maximum Number of Processors", 
    		"Discretization Levels for Mixture Ratios", "Standard Error Tolerance", 
    		"Refinement Time Limit (s)", "Number of Samples in Batch for NOC=1", 
    		"Multiplicative Factor", "Maximum # Samples in Batch", "Max NOC", "Log Likelihood n=0", 
    		"APP n=0", "Log Likelihood n=1", "APP n=1", "Log Likelihood n=2", "APP n=2", "Log Likelihood n=3", 
    		"APP n=3", "Log Likelihood n=4", "APP n=4", "Log Likelihood n=5", "APP n=5", "Locus", "AT", 
    		"Standard Error Tolerance Reached", "Refinement Time Limit Reached"};
    
    public static final String[] CEESIT_CSV_OUTPUT_FILE_HEADER = {"Output File Name", "Sample ID", 
    		"Sample File Name", "Case Number", "Comments", "Calibration Name", "Frequency File", 
    		"Population Name", "NOC Assumption", "Bins File", "Filtered", 
    		"Pull-up Filter Height Ratio (%)", "Pull-up Filter Size Range (bp)",
    		"Complex Pull-up Filter Height Ratio (%)", "Complex Pull-up Filter Sister Height Ratio (%)",
    		"Complex Pull-up Filter Size Range (bp)", "Minus-A Filter Height Ratio (%)", 
    		"Minus-A Filter Size Range (bp)", "Maximum Number of Processors", 
    		"Discretization Levels for Mixture Ratios", "Minimum Bin Width", "Maximum Number of Bins", 
    		"# Samples in Batch for NOC = 1", "Multiplicative Factor", "POI Samples", 
    		"Genotype Tolerance", "Population Substructure Adjustment",
    		"POI Genotype ID", "Known Genotypes IDs", "log10(POI LR)", "p-value", "Number of Samples with LR > 1", "Pr(LR > 1)"
    };
    
    public static java.util.List<String> ceesItCSVOutputFileList = Arrays.asList(CEESIT_CSV_OUTPUT_FILE_HEADER);
    
    public static final String CEESIT_CSV_OUTPUT_FILE_HEADER_BIN_PREFIX = "log10(LR) Bin ";
    public static final String CEESIT_CSV_OUTPUT_FILE_HEADER_FREQUENCY_COLUMN_NAME = "Frequency";
    
    public static final String[] CEESIT_CSV_OUTPUT_FILE_HEADER2 = {"Locus", "AT"
    };
    
    public static final DecimalFormat SCIENTIFIC_FORMATTER = new DecimalFormat("#.#E0");
	public static final double MIN_DECIMAL_FORMAT = 0.01;
	public static final double MAX_DECIMAL_FORMAT = 1000;
	public static final int SIGNIFICANT_FIGURES = 3;
	
	public static final int MAX_INTEGER_VALUE = 2147483647;
    
    public static final double TOOLTIP_FONT_SIZE = 12f;
    
    public static final boolean AUTOSAVE_ON = false;
    // set to true for release, set to false for troubleshooting
    public static final boolean USE_CALIBRATION_PROGRESS_BAR = true;
    
    public static final String DEFAULT_BAR_GRAPH_CSS_COLOR_NAME = "blue";
    
    // Used for lines that are too long to fit on one line of the report so the
    // second line is indented.
    public static final String LINE_TWO_INDENTATION = "          ";
}
