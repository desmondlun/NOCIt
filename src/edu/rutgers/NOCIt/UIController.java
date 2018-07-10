package edu.rutgers.NOCIt;

import static edu.rutgers.NOCIt.UIMain.logger;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import com.opencsv.CSVReader;

import edu.rutgers.NOCIt.Control.AutoSaver;
import edu.rutgers.NOCIt.Control.BackendController;
import edu.rutgers.NOCIt.Control.Calibration;
import edu.rutgers.NOCIt.Control.Calibration.Feature;
import edu.rutgers.NOCIt.Control.CalibrationProjectHandler;
import edu.rutgers.NOCIt.Control.FileChecker;
import edu.rutgers.NOCIt.Control.Project;
import edu.rutgers.NOCIt.Control.Settings;
import edu.rutgers.NOCIt.Data.AMELAllele;
import edu.rutgers.NOCIt.Data.Allele;
import edu.rutgers.NOCIt.Data.CSVModule;
import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.FileReaders;
import edu.rutgers.NOCIt.Data.FreqTable;
import edu.rutgers.NOCIt.Data.Genotype;
import edu.rutgers.NOCIt.Data.Kit;
import edu.rutgers.NOCIt.Data.Locus;
import edu.rutgers.NOCIt.Data.STRAllele;
import edu.rutgers.NOCIt.Data.Sample;
import edu.rutgers.NOCIt.Data.TextFileWriter;
import edu.rutgers.NOCIt.Data.TimeElapsed;
import edu.rutgers.NOCIt.Data.UtilityMethods;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

/**
 * Controller class for the main UI window.
 *
 * @author Rob Carpenter
 * @author Desmond Lun
 * @author James Kelley
 */
public class UIController implements Initializable {

	/**
	 * Displays error message after editing of text field if value entered is
	 * not an double within a given range
	 * @param textField
	 */
	public static void addTextFieldDoubleValidator(TextField textField, String oldCellValue, double lowerBound, double upperBound) {
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
	        @Override
	        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
	            if (!newValue.booleanValue() && !textField.getText().isEmpty()) {
	            	try {
	            		double entered = Double.parseDouble(textField.getText());
	            		if (!UtilityMethods.isNumberInRange(entered, lowerBound, upperBound)) {
	    	            	Alert alert = new Alert(AlertType.ERROR, Constants.VALUE_OUT_OF_RANGE_ERROR_MESSAGE + ">= " + lowerBound + " and <= " + upperBound);
	                        alert.showAndWait();
	                        textField.setText(oldCellValue);
	                        Platform.runLater(new Runnable() {
							    @Override
							    public void run() {
							    	textField.requestFocus();
							    }
							});
	    	            }
	            	}
	            	catch (NumberFormatException e) {
	            		Alert alert = new Alert(AlertType.ERROR, Constants.NON_NUMERIC_ERROR_MESSAGE);
	                    alert.showAndWait();
	                    textField.setText(oldCellValue);
	                    // Returns focus to cell where invalid value was entered
	                    Platform.runLater(new Runnable() {
						    @Override
						    public void run() {
						    	textField.requestFocus();
						    }
						});
	            	}
	            }
	        }
	    });
	}

	/**
	 * Selects text in field when clicked. Based on
	 * http://stackoverflow.com/questions/14965318/javafx-method-selectall-just-works-by-focus-with-keyboard
	 * @param textField
	 */
	public static void addTextFieldFocusListener(TextField textField) {
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
	        @SuppressWarnings("rawtypes")
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

	/**
	 * Displays error message after editing of text field if value entered is
	 * not an integer within a given range
	 * @param textField
	 */
	public static void addTextFieldIntegerValidator(TextField textField, String oldCellValue, int lowerBound, int upperBound) {
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
	        @Override
	        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
	            if (!newValue.booleanValue() && !textField.getText().isEmpty()) {
	            	try {
						double entry = Double.parseDouble(textField.getText());
						if (entry > Integer.MAX_VALUE) {
							Alert alert = new Alert(AlertType.ERROR, Constants.VALUE_OUT_OF_RANGE_ERROR_MESSAGE + ">= " + lowerBound + " and <= " + upperBound);
	                        alert.showAndWait();
	                        textField.setText(oldCellValue);
	                        Platform.runLater(new Runnable() {
							    @Override
							    public void run() {
							    	textField.requestFocus();
							    }
							});
						} else {
							try {
	                    		int entered = Integer.parseInt(textField.getText());
	            	            if (!UtilityMethods.isNumberInRange(entered, lowerBound, upperBound)) {
	            	            	Alert alert = new Alert(AlertType.ERROR, Constants.VALUE_OUT_OF_RANGE_ERROR_MESSAGE + ">= " + lowerBound + " and <= " + upperBound);
	                                alert.showAndWait();
	                                textField.setText(oldCellValue);
	                                Platform.runLater(new Runnable() {
	        						    @Override
	        						    public void run() {
	        						    	textField.requestFocus();
	        						    }
	        						});
	            	            }
	                    	}
	                    	catch (NumberFormatException e) {
	                    		Alert alert = new Alert(AlertType.ERROR, Constants.NOT_INTEGER_ERROR_MESSAGE);
	                            alert.showAndWait();
	                            textField.setText(oldCellValue);
	                            // Returns focus to cell where invalid value was entered
	                            Platform.runLater(new Runnable() {
	    						    @Override
	    						    public void run() {
	    						    	textField.requestFocus();
	    						    }
	    						});
	                    	}
						}
	            	}
	            	catch (NumberFormatException e) {
	            		Alert alert = new Alert(AlertType.ERROR, Constants.NON_NUMERIC_ERROR_MESSAGE);
	                    alert.showAndWait();
	                    textField.setText(oldCellValue);
	                    // Returns focus to cell where invalid value was entered
	                    Platform.runLater(new Runnable() {
						    @Override
						    public void run() {
						    	textField.requestFocus();
						    }
						});
	            	}
	            	
	            }
	        }
	    });
	}

	public static void addTextFieldLongValidator(TextField textField, String oldCellValue, long lowerBound, long upperBound) {
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
	        @Override
	        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
	            if (!newValue.booleanValue()) {
	            	try {
						double entry = Double.parseDouble(textField.getText());
						if (entry > Long.MAX_VALUE) {
							Alert alert = new Alert(AlertType.ERROR, Constants.VALUE_OUT_OF_RANGE_ERROR_MESSAGE + ">= " + lowerBound + " and <= " + upperBound);
	                        alert.showAndWait();
	                        textField.setText(oldCellValue);
	                        Platform.runLater(new Runnable() {
							    @Override
							    public void run() {
							    	textField.requestFocus();
							    }
							});
						} else {
							try {
	                    		long entered = Long.parseLong(textField.getText());
	            	            if (!UtilityMethods.isNumberInRange(entered, lowerBound, upperBound)) {
	            	            	Alert alert = new Alert(AlertType.ERROR, Constants.VALUE_OUT_OF_RANGE_ERROR_MESSAGE + ">= " + lowerBound + " and <= " + upperBound);
	                                alert.showAndWait();
	                                textField.setText(oldCellValue);
	                                Platform.runLater(new Runnable() {
	        						    @Override
	        						    public void run() {
	        						    	textField.requestFocus();
	        						    }
	        						});
	            	            }
	                    	}
	                    	catch (NumberFormatException e) {
	                    		Alert alert = new Alert(AlertType.ERROR, Constants.NOT_INTEGER_ERROR_MESSAGE);
	                            alert.showAndWait();
	                            textField.setText(oldCellValue);
	                            // Returns focus to cell where invalid value was entered
	                            Platform.runLater(new Runnable() {
	    						    @Override
	    						    public void run() {
	    						    	textField.requestFocus();
	    						    }
	    						});
	                    	}
						}
	            	}
	            	catch (NumberFormatException e) {
	            		Alert alert = new Alert(AlertType.ERROR, Constants.NON_NUMERIC_ERROR_MESSAGE);
	                    alert.showAndWait();
	                    textField.setText(oldCellValue);
	                    // Returns focus to cell where invalid value was entered
	                    Platform.runLater(new Runnable() {
						    @Override
						    public void run() {
						    	textField.requestFocus();
						    }
						});
	            	}
	            	
	            }
	        }
	    });
	}

	public static void addTimeFieldIntegerValidator(TextField textField, String oldCellValue, int lowerBound, int upperBound) {
		int oldTimeValue = Integer.parseInt(oldCellValue);
		oldCellValue = Integer.toString((int) oldTimeValue/1000);
		addTextFieldIntegerValidator(textField, oldCellValue, lowerBound, upperBound);
	}

	/**
	 * Display error dialog.
	 *
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 */
	public static void displayErrorDialog(String title, String message) {
		Alert alert = new Alert(AlertType.ERROR, message);
		alert.setHeaderText(title);
		alert.showAndWait();
	}

	/** The calibration table. */
	@FXML 
	public TableView<ObservableList<?>> calibrationTable;

	/** The tab pane. */
	@FXML
	private TabPane tabPane;

	/** The calibration name. */
	@FXML
	public TextField calibrationName;

	/** The calibration name label. */
	@FXML
	private Label calibrationNameLabel;

	/** The tree table view. */
	@FXML
	public TreeTableView<ObservableList<String>> treeTableView;

	/** The NOCIt progress bar. */
	@FXML
	public ProgressBar nocItProgressBar;

	/** The CEESIt progress bar. */
	@FXML
	public ProgressBar ceesItProgressBar;

	/** The sample folder path. */
	@FXML
	public TextField sampleFolderPath;

	/** The browse sample files. */
	@FXML
	public Button browseSampleFiles;

	/** The kit name. */
	@FXML
	private TextField kitName;

	/** The browse bins file. */
	@FXML
	private Button browseBinsFile;

	/** The line chart anchor. */
	@FXML
	private AnchorPane lineChartAnchor;

	/** The NOCIt table. */
	@FXML 
	public TableView<ObservableList<?>> nocItTable;

	/** The CEESIt table. */
	@FXML
	public TableView<ObservableList<?>> ceesItTable;

	/** The calculate calibration. */
	@FXML
	private Button calculateCalibration;

	/** The reset calibration. */
	@FXML
	private Button resetCalibration;

	/** The main pane. */
	@FXML
	private AnchorPane mainPane;

	/** The tab NOCIt. */
	@FXML
	private Tab tabNOCIt;

	/** The tab CEESIt. */
	@FXML
	private Tab tabCEESIt;

	/** The tab calibrate. */
	@FXML
	private Tab tabCalibrate;

	/** The start NOCIt button. */
	@FXML
	private Button startNOCItButton;
	
	/** The cancel NOCIt button. */
	@FXML
	private Button cancelNOCItButton;

	/** The NOCIt plus button. */
	@FXML
	private Button nocitPlusButton;

	/** The NOCIt minus button. */
	@FXML
	private Button nocitMinusButton;
	
	/** The CEESIt plus button. */
	@FXML
	private Button ceesitPlusButton;

	/** The CEESIt minus button. */
	@FXML
	private Button ceesitMinusButton;

	/** The start CEESIt button. */
	@FXML
	private Button startCEESItButton;

	/** The next button. */
	@FXML
	private Button nextButton;

	/** The anchor pane tree table. */
	@FXML
	private AnchorPane anchorPaneTreeTable;

	/** The NOCIt time elapsed label. */
	@FXML
	public Label nocItTimeElapsed;

	/** The CEESIt time elapsed label. */
	@FXML
	public Label ceesItTimeElapsed;

	/** The NOCIt bar chart area. */
	@FXML
	public TilePane nocItBarChartArea;

	/** The CEESIt bar chart area. */
	@FXML
	public TilePane ceesItBarChartArea;

	/** The calibration first page. */
	@FXML
	private VBox calibrationPage1;

	/** The calibration second page. */
	@FXML
	private VBox calibrationPage2;

	/** The calibration save button. */
	@FXML
	private Button calibrationSaveButton;

	/** The new calibration menu. */
	@FXML
	private MenuItem newCalibrationMenu;

	/** The load calibration menu. */
	@FXML
	private MenuItem loadCalibrationMenu;

	/** The save calibration menu. */
	@FXML
	private MenuItem saveCalibrationMenu;

	/** The import genotypes menu. */
	@FXML
	private MenuItem importGenotypesMenu;

	/** The save genotypes menu. */
	@FXML
	private MenuItem saveGenotypesMenu;

	/** The population type menu. */
	@FXML
	private MenuItem populationTypeMenu;

	/** The view genotypes menu. */
	@FXML
	private MenuItem viewGenotypesMenu;
   
	/** The view genotypes table. */
	private TableView<ObservableList<?>> viewGenotypesTable = new TableView<>();

	/** The genotypes. */
	public TreeMap<String, Genotype> genotypes = new TreeMap<>();

	/** The auto saver. */
	private AutoSaver autoSaver;

	/** The NOCIt time elapsed timer. */
	public TimeElapsed nocItTimeElapsedTimer;

	/** The CEESIt time elapsed timer. */
	public TimeElapsed ceesItTimeElapsedTimer;
	
	private boolean timerRunning = false;

	/** The initialized. */
	private boolean initialized = false;

	/** The show save changes prompt. */
	public boolean showSaveChangesPrompt = true;

	/** The stage. */
	private Stage stage;

	/** The backend controller. */
	public BackendController backendController;

	/** The is all selected. */
	private boolean isAllSelected = false;

	/** The is NOCIt graph all selected. */
	public boolean isNOCItGraphAllSelected = false;

	/** The is NOCIt filter all selected. */
	public boolean isNOCItFilterAllSelected = false;

	/** The is CEESIt graph all selected. */
	public boolean isCEESItGraphAllSelected = false;

	/** The is CEESIt filter all selected. */
	public boolean isCEESItFilterAllSelected = false;

	/** The settings stage. */
	private Stage settingsStage;

	/** The NOCIt stage. */
	private Stage nocitStage;
	
	/** The CEESIt stage. */
	private Stage ceesitStage;

	/** The add new genotype stage. */
	private Stage addNewGenotypeStage = new Stage();

	/** The view genotypes stage. */
	private Stage viewGenotypesStage = new Stage();
	
	private SettingsController settingsController;

	/** The select all check box. */
	private CheckBox selectAllCheckBox;

	/** The NOCIt graph select all check box. */
	private CheckBox nocItGraphSelectAllCheckBox;

	/** The NOCIt filter select all check box. */
	public CheckBox nocItFilterSelectAllCheckBox;

	/** The CEESIt graph select all check box. */
	private CheckBox ceesItGraphSelectAllCheckBox;

	/** The CEESIt filter select all check box. */
	public CheckBox ceesItFilterSelectAllCheckBox;

	/** Flag indicating whether a kit has been selected. */
	private boolean kitSelected = false;

	/** Flag indicating whether calibration has been calculated. */
	private boolean calibrationCalculated = false;

	/** Flag indicating whether file chooser is open. */
	private boolean fileChooserOpen = false;

	/** The CSV module map. */
	private Map<String, CSVModule> csvModuleMap = new TreeMap<String, CSVModule>();

	/** The filtered CSV module map. */
	private Map<String, CSVModule> filteredCsvModuleMap = new HashMap<String, CSVModule>();

	/** The calibrations. */
	public TreeMap<String, Calibration> calibrations = new TreeMap<String, Calibration>();

	/** The samples. */
	public HashMap<String, Sample> samples = new HashMap<String, Sample>();
	
	/** CEESIt samples. */
	public HashMap<String, Sample> ceesitSamples = new HashMap<String, Sample>();

	/** The calibration filter. */
	private FileChooser.ExtensionFilter calibrationFilter = new FileChooser.ExtensionFilter(
			Constants.CALIBRATION_FILE_NAME + "(*." + Constants.CALIBRATION_FILE_EXTENSION + ")",
			"*" + Constants.CALIBRATION_FILE_EXTENSION);

	/** The all files filter. */
	private FileChooser.ExtensionFilter allFilesFilter = new FileChooser.ExtensionFilter("All Files (*.*)", "*.*");

	/** The autosave loaded. */
	public boolean autosaveLoaded = false;

	/** The population names list. */
	public ArrayList<String> populationNamesList = new ArrayList<String>();

	/** The populations. */
	public TreeMap<String, FreqTable> populations = new TreeMap<>();

	/** The output names population map. */
	public HashMap<String, ArrayList<String>> outputNamesPopulationMap = new HashMap<String, ArrayList<String>>();
	
	/** The output names population map for CEESIt. */
	public HashMap<String, ArrayList<String>> outputNamesPopulationCEESItMap = new HashMap<String, ArrayList<String>>();

	/** The row ID analytical thresholds map. */
	public HashMap<String, LinkedHashMap<Locus, Integer>> rowIDAnalyticalThresholdsMap = new HashMap<String, LinkedHashMap<Locus, Integer>>();

	/** The row ID analytical thresholds map. */
	public HashMap<String, LinkedHashMap<Locus, Integer>> ceesItRowIDAnalyticalThresholdsMap = new HashMap<String, LinkedHashMap<Locus, Integer>>();
	
	/** The tree data. */
	private List<List<List<String>>> treeData;

	/** The NOCIt CSV output file path. */
	public String nocItCSVOutputFilePath = "";
	
	/** The CEESIt CSV output file path. */
	public String ceesItCSVOutputFilePath = "";

	/** The user selected NOCIt output file paths. */
	public ArrayList<String> userSelectedNOCItOutputFilePaths = new ArrayList<String>();
	
	/** The user selected CEESIt output file paths. */
	public ArrayList<String> userSelectedCEESItOutputFilePaths = new ArrayList<String>();

	/**
	 * Adds the samples.
	 *
	 * @param currentSamples
	 *            the current samples
	 */
	public void addSamples(Map<String, Sample> currentSamples) {
		this.samples.putAll(currentSamples);
	}
	
	public void addCEESItSamples(Map<String, Sample> currentSamples) {
		this.ceesitSamples.putAll(currentSamples);
	}
	
	/*
	 * Lists to store row indices of errors in NOCIt and CEESIt tables to be used for
	 * highlighting after Start button is clicked and error message shown to make
	 * it easier for the user to find errors
	 */
	private ArrayList<Integer> nocItDuplicateOutputRowList = new ArrayList<Integer>();
	private ArrayList<Integer> nocItChooseEntryRowList = new ArrayList<Integer>();
	private ArrayList<Integer> nocItExistingFileRowList = new ArrayList<Integer>();
	private ArrayList<Integer> ceesItDuplicateOutputRowList = new ArrayList<Integer>();
	private ArrayList<Integer> ceesItChooseEntryRowList = new ArrayList<Integer>();
	private ArrayList<Integer> ceesItNOCErrorRowList = new ArrayList<Integer>();
	private ArrayList<Integer> ceesItExistingFileRowList = new ArrayList<Integer>();
	
	/**
	 * Table error messages
	 */
	private String nocItTableErrorMessage = "";
	private String ceesItTableErrorMessage = "";
	
	/**
	 * Error checking only occurs after Start has been clicked
	 */
	private boolean startNOCItButtonClicked = false;
	private boolean startCEESItButtonClicked = false;
	
	/**
	 * This function autosaves the current progress in the application. The
	 * autosave file will automatically be loaded next time the application is
	 * launched.
	 */
	public void autoSave() {
		File file = new File(Settings.getSettingsPath() + "/" + Constants.AUTOSAVE_FILENAME);
		if (!autosaveLoaded) {
			CalibrationProjectHandler.saveCalibrationProject(file, this);
		} else {
			CalibrationProjectHandler.unZipIt(Settings.getSettingsPath() + "/" + Constants.AUTOSAVE_FILENAME,
					Settings.getSettingsPath(), this);
			CalibrationProjectHandler.saveCalibrationProject(file, this);
		}
	}

	/**
	 * This is called when the 'Back' button on the second Calibrate tab is
	 * clicked.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void backClicked(ActionEvent event) {
		calibrationPage1.setVisible(true);
		calibrationPage2.setVisible(false);
	}

	/**
	 * Builds/rebuilds the Calibration table (on first calibration pane) UI
	 * elements.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildCalibrationTable() {
		// Calibration table
		calibrationTable.getItems().clear();
		calibrationTable.getColumns().clear();

		calibrationTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		List<TableColumn<ObservableList<?>, ?>> columnList = new ArrayList<>();
		TableColumn<ObservableList<?>, String> column = new TableColumn<>(Constants.CALIBRATION_TABLE_COLUMN_FILE_NAME);
		column.setMinWidth(75);
		column.setPrefWidth(250);
		column.setEditable(false);
		column.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						cellDataFeatures.getValue();
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.CALIBRATION_TABLE_COLUMN_FILE_NAME_INDEX);
					}
				});
		column.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
		columnList.add(column);

		TableColumn<ObservableList<?>, String> column1 = new TableColumn<>(
				Constants.CALIBRATION_TABLE_COLUMN_SAMPLE_NAME);
		column1.setMinWidth(300);
		column1.setPrefWidth(400);

		column1.setEditable(false);
		column1.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						cellDataFeatures.getValue();
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.CALIBRATION_TABLE_COLUMN_SAMPLE_NAME_INDEX);
					}
				});
		column1.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
		columnList.add(column1);

		TableColumn<ObservableList<?>, Boolean> column2 = new TableColumn<>();
		column2.setMinWidth(75);
		column2.setPrefWidth(75);
		column2.setText(Constants.CALIBRATION_TABLE_COLUMN_FILTER);
		column2.setGraphic(getSelectAllCheckBox());
		column2.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(
							CellDataFeatures<ObservableList<?>, Boolean> cellDataFeatures) {
						boolean allSelected = true;
						// if any cell is not selected all selected is false
						for (ObservableList<?> row : calibrationTable.getItems()) {
							boolean value = ((SimpleBooleanProperty) row
									.get(Constants.CALIBRATION_TABLE_COLUMN_FILTER_INDEX)).get();
							if (value == false) {
								allSelected = false;
							}
						}
						getSelectAllCheckBox().setSelected(allSelected);
						isAllSelected = allSelected;
						return (SimpleBooleanProperty) cellDataFeatures.getValue().get(2);
					}
				});
		column2.setCellFactory(
				new Callback<TableColumn<ObservableList<?>, Boolean>, TableCell<ObservableList<?>, Boolean>>() {
					@Override
					public TableCell<ObservableList<?>, Boolean> call(TableColumn<ObservableList<?>, Boolean> p) {
						CheckBoxTableCell<ObservableList<?>, Boolean> checkBoxCell = new CheckBoxTableCell<>();
						checkBoxCell.addEventHandler(ActionEvent.ACTION, new EventHandler() {
							@Override
							public void handle(Event event) {
								// Changing filter should enable save changes prompt
								showSaveChangesPrompt = true;
							}
						});
						return checkBoxCell;
					}
				});
		// Individual samples should be able to be filtered
		column2.setEditable(true);
		columnList.add(column2);

		TableColumn<ObservableList<?>, String> column3 = new TableColumn<>(Constants.CALIBRATION_TABLE_COLUMN_DNA_MASS);
		// hide column
		column3.setMinWidth(0);
		column3.setMaxWidth(0);
		column3.setEditable(false);
		column3.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.CALIBRATION_TABLE_COLUMN_DNA_MASS_INDEX);
					}
				});
		column3.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
		columnList.add(column3);

		TableColumn<ObservableList<?>, String> genotypeColumn = new TableColumn<>(
				Constants.CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE);
		genotypeColumn.setMinWidth(150);
		genotypeColumn.setEditable(true);
		genotypeColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cdf) {
						return (SimpleStringProperty) cdf.getValue()
								.get(Constants.CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE_INDEX);
					}
				});
		genotypeColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = cellEditEvent.getNewValue();
				String oldValue = cellEditEvent.getOldValue();
				int row = cellEditEvent.getTablePosition().getRow();
				SimpleStringProperty property = ((SimpleStringProperty) calibrationTable.getItems().get(row)
						.get(Constants.CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE_INDEX));
				if (value.equals(Constants.DEFAULT_ADD_NEW)) {
					property.set("");
					property.set(oldValue);
					showGenotypeEditor(property, null);
				} else {
					property.set(value);
				}
				showSaveChangesPrompt = true;
			}
		});

		ObservableList<String> namesChoiceList = FXCollections.observableArrayList(this.getGenotypeSamples());
		genotypeColumn.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(namesChoiceList));
		columnList.add(genotypeColumn);

		calibrationTable.setRowFactory(new Callback<TableView<ObservableList<?>>, TableRow<ObservableList<?>>>() {
			@Override
			public TableRow<ObservableList<?>> call(TableView<ObservableList<?>> tableView) {
				final TableRow<ObservableList<?>> row = new TableRow<>();
				final ContextMenu contextMenu = new ContextMenu();
				final MenuItem resetGenotypeToChoose = new MenuItem("Reset to <Choose>");
				resetGenotypeToChoose.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						ObservableList<ObservableList<?>> rows = calibrationTable.getSelectionModel()
								.getSelectedItems();
						for (int i = 0; i < rows.size(); i++) {
							try {
								((SimpleStringProperty) rows.get(i).get(4)).set(Constants.DEFAULT_CHOOSE);
							} catch (Exception e) {
								// e.printStackTrace();
								logger.error("Error deleting genotype", e);
							}
						}
					}
				});
				contextMenu.getItems().add(resetGenotypeToChoose);
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty()
						.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
				return row;
			}
		});

		calibrationTable.getColumns().addAll(columnList);

		// based on
		// http://stackoverflow.com/questions/14116792/how-to-disable-the-reordering-of-table-columns-in-tableview
		// Code disables default behavior where columns can be rearranged.
		// Unfortunately column headers can still be dragged.
		// Selection of item in Known Genotype column dropbox throws an exception
		// java.lang.UnsupportedOperationException, hence the try/catch.
		calibrationTable.getColumns().addListener(new ListChangeListener() {
			@Override
			public void onChanged(Change change) {
				change.next();
				if (change.wasReplaced()) {
					try {
						calibrationTable.getColumns().clear();
						calibrationTable.getColumns().addAll(columnList);
					} catch (Exception e) {
						// e.printStackTrace();
						// logger.error(Constants.DISABLE_COLUMN_REORDERING_ERROR, e);
					}
				}
			}
		});
	}

	/**
	 * Builds/rebuilds the CEESIt table UI elements.
	 */
	private void buildCEESItTable() {
		ceesItTable.getColumns().clear();

		// Build Headers
		List<TableColumn<ObservableList<?>, ?>> columnList = new ArrayList<>();

		TableColumn<ObservableList<?>, Boolean> graphColumn = new TableColumn<>(Constants.CEESIT_TABLE_COLUMN_GRAPH);
		graphColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_MIN_WIDTH);
		graphColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_GRAPH_WIDTH);
		graphColumn.setEditable(true);
		graphColumn.setSortable(false);
		graphColumn.setGraphic(getCEESItGraphCheckBox());
		graphColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(
							CellDataFeatures<ObservableList<?>, Boolean> cellDataFeatures) {
						boolean allSelected = true;
						// if any cell is not selected all selected is false
						for (ObservableList<?> row : ceesItTable.getItems()) {
							boolean value = ((SimpleBooleanProperty) row.get(Constants.CEESIT_TABLE_COLUMN_GRAPH_INDEX))
									.get();
							if (value == false) {
								allSelected = false;
							}
						}
						getCEESItGraphCheckBox().setSelected(allSelected);
						isCEESItGraphAllSelected = allSelected;
						return (SimpleBooleanProperty) cellDataFeatures.getValue()
								.get(Constants.CEESIT_TABLE_COLUMN_GRAPH_INDEX);
					}
				});
		graphColumn.setCellFactory(
				new Callback<TableColumn<ObservableList<?>, Boolean>, TableCell<ObservableList<?>, Boolean>>() {
					@SuppressWarnings("unchecked")
					@Override
					public TableCell<ObservableList<?>, Boolean> call(TableColumn<ObservableList<?>, Boolean> p) {
						CheckBoxTableCell<ObservableList<?>, Boolean> checkBoxCell = new CheckBoxTableCell<>();
						checkBoxCell.addEventHandler(ActionEvent.ACTION, new EventHandler() {
							@Override
							public void handle(Event event) {
								backendController.updateCEESItGraphs();
							}
						});
						return checkBoxCell;
					}
				});
		columnList.add(graphColumn);
		
		TableColumn<ObservableList<?>, String> caseColumn = new TableColumn<>(Constants.CEESIT_TABLE_COLUMN_CASE);
		caseColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_MIN_WIDTH);
		caseColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_CASE_WIDTH);
		caseColumn.setEditable(true);
		caseColumn.setSortable(false);
		caseColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.CEESIT_TABLE_COLUMN_CASE_INDEX);
					}
				});
		caseColumn.setCellFactory(new EditingTableCellFactory());
		columnList.add(caseColumn);

		TableColumn<ObservableList<?>, String> sampleColumn = new TableColumn<>(Constants.CEESIT_TABLE_COLUMN_SAMPLE);
		sampleColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_MIN_WIDTH);
		sampleColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_SAMPLE_WIDTH);
		sampleColumn.setEditable(false);
		sampleColumn.setSortable(false);
		sampleColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.CEESIT_TABLE_COLUMN_SAMPLE_INDEX);
					}
				});
		// Please leave this here as a reminder of how to set a default cell in a table
		//sampleColumn.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
		sampleColumn.setCellFactory(column -> {
			return new TableCell<ObservableList<?>, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(item);
					if (item != null && item.length() > 0) {
						setTooltip(new Tooltip(item));
					}
				}
			};
		});
		columnList.add(sampleColumn);

		TableColumn<ObservableList<?>, Boolean> filterColumn = new TableColumn<>(Constants.CEESIT_TABLE_COLUMN_FILTER);
		filterColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_MIN_WIDTH);
		filterColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_FILTER_WIDTH);
		filterColumn.setEditable(true);
		filterColumn.setSortable(false);
		filterColumn.setGraphic(getCEESItFilterCheckBox());
		filterColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(
							CellDataFeatures<ObservableList<?>, Boolean> cellDataFeatures) {
						boolean allSelected = true;
						// if any cell is not selected all selected is false
						for (ObservableList<?> row : ceesItTable.getItems()) {
							boolean value = ((SimpleBooleanProperty) row
									.get(Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX)).get();
							if (value == false) {
								allSelected = false;
							}
						}
						getCEESItFilterCheckBox().setSelected(allSelected);
						isCEESItFilterAllSelected = allSelected;
						return (SimpleBooleanProperty) cellDataFeatures.getValue()
								.get(Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX);
					}
				});
		filterColumn.setCellFactory(
				new Callback<TableColumn<ObservableList<?>, Boolean>, TableCell<ObservableList<?>, Boolean>>() {
					@SuppressWarnings("unchecked")
					@Override
					public TableCell<ObservableList<?>, Boolean> call(TableColumn<ObservableList<?>, Boolean> p) {
						CheckBoxTableCell<ObservableList<?>, Boolean> checkBoxCell = new CheckBoxTableCell<>();
						checkBoxCell.addEventHandler(ActionEvent.ACTION, new EventHandler() {
							@Override
							public void handle(Event event) {
								ObservableList<?> rowList = (ObservableList<?>) ceesItTable.getItems()
										.get(checkBoxCell.getTableRow().getIndex());
								String outputName = ((SimpleStringProperty) rowList
										.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
								if (!userSelectedCEESItOutputFilePaths.contains(outputName)) {
									UtilityMethods.updateCEESItOutputName(ceesItTable, 
											rowList, checkBoxCell.getTableRow().getIndex());

								}
								if (startCEESItButtonClicked) {
									ceesItEditRowAction(rowList, Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX);
								}
							}
						});

						return checkBoxCell;
					}
				});
		columnList.add(filterColumn);

		TableColumn<ObservableList<?>, String> dnaColumn = new TableColumn<>(Constants.CEESIT_TABLE_COLUMN_DNA);
		dnaColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_MIN_WIDTH);
		dnaColumn.setEditable(true);
		dnaColumn.setSortable(false);
		dnaColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						cellDataFeatures.getValue();
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.CEESIT_TABLE_COLUMN_DNA_INDEX);
					}
				});
		dnaColumn.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
		dnaColumn.setVisible(false);
		columnList.add(dnaColumn);

		// Population Name
		TableColumn<ObservableList<?>, String> popColumn = new TableColumn<>(Constants.CEESIT_TABLE_COLUMN_POPULATION);
		popColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_POPULATION_WIDTH);
		popColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_POPULATION_WIDTH);
		popColumn.setEditable(true);
		popColumn.setSortable(false);
		popColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cdf) {
						return (SimpleStringProperty) cdf.getValue()
								.get(Constants.CEESIT_TABLE_COLUMN_POPULATION_INDEX);
					}
				});
		popColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = (String) cellEditEvent.getNewValue();
				((SimpleStringProperty) ceesItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
						.get(Constants.CEESIT_TABLE_COLUMN_POPULATION_INDEX)).set(value);
			}
		});

		ObservableList<String> popChoiceList = FXCollections.observableArrayList(populations.keySet());
		popColumn.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(popChoiceList));
		columnList.add(popColumn);

		TableColumn<ObservableList<?>, String> nocColumn = new TableColumn<>(Constants.CEESIT_TABLE_COLUMN_NOC);
		nocColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_NOC_WIDTH);
		nocColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_NOC_WIDTH);
		nocColumn.setEditable(true);
		nocColumn.setSortable(false);
		nocColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cdf) {
						return (SimpleStringProperty) cdf.getValue().get(Constants.CEESIT_TABLE_COLUMN_NOC_INDEX);
					}
				});
		nocColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = (String) cellEditEvent.getNewValue();
				String oldValue = (String) cellEditEvent.getOldValue();
				ObservableList<?> rowList = (ObservableList<?>) ceesItTable.getItems()
						.get(cellEditEvent.getTablePosition().getRow());
				String outputName = ((SimpleStringProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX))
						.get();
				
				((SimpleStringProperty) ceesItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
						.get(Constants.CEESIT_TABLE_COLUMN_NOC_INDEX)).set(value);
				if (!userSelectedCEESItOutputFilePaths.contains(outputName)) {
					UtilityMethods.updateCEESItOutputName(ceesItTable, 
							rowList, cellEditEvent.getTablePosition().getRow());
				}
				if (!value.equals(oldValue) && startCEESItButtonClicked) {
					ceesItEditRowAction(rowList, Constants.CEESIT_TABLE_COLUMN_NOC_INDEX);
				}
			}
		});
		ObservableList<String> nocChoiceList = FXCollections
				.observableArrayList(Arrays.asList(Constants.CEESIT_NOC_DROPDOWN_OPTIONS));
		// Please leave this here as a reminder of how to set a default combo box table cell
		//nocColumn.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(nocChoiceList));
		nocColumn.setCellFactory(col -> {
			return new ComboBoxTableCell<ObservableList<?>, String>(nocChoiceList) {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(item);
					if (getIndex() > -1) {
						try {
							// This line sometimes throws an index out of range exception when
							// scrolling the table. Unsure what is out of range.
							int rowID = ((SimpleIntegerProperty) ceesItTable.getItems().get(getIndex()).get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
							if (ceesItNOCErrorRowList.contains(rowID)) {
								setTextFill(Color.RED);
							} else {
								setTextFill(Color.BLACK);
							}
						} catch (Exception e) {
							
						}
					}
				}
			};
		});
		columnList.add(nocColumn);

		TableColumn<ObservableList<?>, String> calibrationColumn = new TableColumn<>(
				Constants.CEESIT_TABLE_COLUMN_CALIBRATION);
		calibrationColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_WIDTH);
		calibrationColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_WIDTH);
		calibrationColumn.setEditable(true);
		calibrationColumn.setSortable(false);
		calibrationColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cdf) {
						return (SimpleStringProperty) cdf.getValue()
								.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX);
					}
				});
		calibrationColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = (String) cellEditEvent.getNewValue();
				String oldValue = (String) cellEditEvent.getOldValue();
				if (value.equals("<Add New>")) {
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle(Constants.SELECT_FILE);
					// get last save path from settings
					String lastCalibrationPath = Settings.lastCalibrationPath;
					setFileChooserInitialDirectory(fileChooser, lastCalibrationPath);
					// set file name from calibration name field if directory
					// exists and field is not empty
					// TODO: check if this is necessary
					setFileChooserFileName(fileChooser, lastCalibrationPath, calibrationName.getText());
					// Set extension filters
					fileChooser.getExtensionFilters().add(calibrationFilter);
					fileChooser.getExtensionFilters().add(allFilesFilter);
					File file = fileChooser.showOpenDialog(stage);

					if (file != null) {
						if (!FileChecker.isValidCalibrationProjectFile(file.getAbsolutePath())) {
							UIController.displayErrorDialog("File Validation",
									"This is not a valid Calibration Project file: \r\n\r\n" + file.getAbsolutePath());
							((SimpleStringProperty) ceesItTable.getItems()
									.get(cellEditEvent.getTablePosition().getRow())
									.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX)).set("<wdw>");
							((SimpleStringProperty) ceesItTable.getItems()
									.get(cellEditEvent.getTablePosition().getRow())
									.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX)).set(oldValue);
							return;
						}
						Project project = CalibrationProjectHandler.loadProjectData(file);
						if (project.getCalibration() != null) {
							String name = file.getName().split("\\.")[0];
							updateCalibrationOptions(name, project.getCalibration());

							ObservableList<String> calibrationChoiceList = createCalibrationChoiceList();
							calibrationColumn.setCellFactory(
									ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(calibrationChoiceList));

							Settings.lastCalibrationPath = file.getParent();
							Settings.save();
							((SimpleStringProperty) ceesItTable.getItems()
									.get(cellEditEvent.getTablePosition().getRow())
									.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX)).set(name);
							
							ObservableList<?> rowList = (ObservableList<?>) ceesItTable.getItems()
									.get(cellEditEvent.getTablePosition().getRow());
							updateCEESItATsForSelectedCalibration(project.getCalibration(), rowList);
							String outputName = ((SimpleStringProperty) rowList
									.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
							if (!userSelectedCEESItOutputFilePaths.contains(outputName)) {
								UtilityMethods.updateCEESItOutputName(ceesItTable, rowList,
										cellEditEvent.getTablePosition().getRow());
								if (!value.equals(oldValue) && startCEESItButtonClicked) {
									ceesItEditRowAction(rowList, Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX);
								}
							}
						}
					} else {
						((SimpleStringProperty) ceesItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
								.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX)).set("<wdw>");
						((SimpleStringProperty) ceesItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
								.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX)).set(oldValue);
					}
				} else {
					((SimpleStringProperty) ceesItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
							.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX)).set(value);
					// Row list must be created after cell has been updated
					ObservableList<?> rowList = (ObservableList<?>) ceesItTable.getItems()
							.get(cellEditEvent.getTablePosition().getRow());
					String calibrationName = ((SimpleStringProperty) rowList
							.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX)).get();
					updateCEESItATsForSelectedCalibration(calibrations.get(calibrationName), rowList);
					String outputName = ((SimpleStringProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX))
							.get();
					if (!userSelectedCEESItOutputFilePaths.contains(outputName)) {
						UtilityMethods.updateCEESItOutputName(ceesItTable, 
								rowList, cellEditEvent.getTablePosition().getRow());
						if (!value.equals(oldValue) && startCEESItButtonClicked) {
							ceesItEditRowAction(rowList, Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX);
						}
					}
				}
			}
		});
		// add observable list created from settings load of recent files
		ObservableList<String> calibrationChoiceList = createCalibrationChoiceList();

		calibrationColumn
				.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(calibrationChoiceList));
		columnList.add(calibrationColumn);

		TableColumn<ObservableList<?>, String> poiColumn = new TableColumn<>(Constants.CEESIT_TABLE_COLUMN_POI);
		poiColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_POI_WIDTH);
		poiColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_POI_WIDTH);
		poiColumn.setEditable(true);
		poiColumn.setSortable(false);
		poiColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cdf) {
						return (SimpleStringProperty) cdf.getValue().get(Constants.CEESIT_TABLE_COLUMN_POI_INDEX);
					}
				});
		poiColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = cellEditEvent.getNewValue();
				String oldValue = cellEditEvent.getOldValue();
				int row = cellEditEvent.getTablePosition().getRow();
				ObservableList<?> rowList = (ObservableList<?>) ceesItTable.getItems()
						.get(cellEditEvent.getTablePosition().getRow());
				String outputName = ((SimpleStringProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX))
						.get();
				SimpleStringProperty property = ((SimpleStringProperty) ceesItTable.getItems().get(row)
						.get(Constants.CEESIT_TABLE_COLUMN_POI_INDEX));
				property.set(value);
				if (!userSelectedCEESItOutputFilePaths.contains(outputName)) {
					UtilityMethods.updateCEESItOutputName(ceesItTable, 
							rowList, cellEditEvent.getTablePosition().getRow());
					if (!value.equals(oldValue) && startCEESItButtonClicked) {
						ceesItEditRowAction(rowList, Constants.CEESIT_TABLE_COLUMN_POI_INDEX);
					}
				}
				int rowID = ((SimpleIntegerProperty) ceesItTable.getItems().get(cellEditEvent.getTablePosition().getRow()).get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
				if (!value.equals(Constants.DEFAULT_CHOOSE)) {
					if (ceesItChooseEntryRowList.contains(rowID)) {
						ceesItChooseEntryRowList.remove(ceesItChooseEntryRowList.indexOf(rowID));
					}
				}
				ceesItTable.refresh();
			}
		});

		ObservableList<String> poiNamesChoiceList = FXCollections.observableArrayList(this.getPOISamples());
		poiColumn.setCellFactory(col -> {
			return new ComboBoxTableCell<ObservableList<?>, String>(poiNamesChoiceList) {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(item);
					if (getIndex() > -1) {
						try {
							// This line sometimes throws an index out of range exception when
							// scrolling the table. Unsure what is out of range.
							int rowID = ((SimpleIntegerProperty) ceesItTable.getItems().get(getIndex()).get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
							if (ceesItChooseEntryRowList.contains(rowID)) {
								setTextFill(Color.RED);
							} else {
								setTextFill(Color.BLACK);
							}
						} catch (Exception e) {
							
						}
					}
				}
			};
		});
		columnList.add(poiColumn);
		
		TableColumn<ObservableList<?>, String> knownContributorsColumn = new TableColumn<>(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS);
		knownContributorsColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_WIDTH);
		knownContributorsColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_WIDTH);
		knownContributorsColumn.setEditable(true);
		knownContributorsColumn.setSortable(false);
		knownContributorsColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cdf) {
						return (SimpleStringProperty) cdf.getValue().get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX);
					}
				});
		knownContributorsColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = cellEditEvent.getNewValue();
				int row = cellEditEvent.getTablePosition().getRow();
				SimpleStringProperty property = ((SimpleStringProperty) ceesItTable.getItems().get(row)
						.get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX));
				property.set(value);
			}
		});

		ObservableList<String> knownContributorsChoiceList = FXCollections.observableArrayList(this.getKnownContributorsSamples());
		setKnownContributorsColumnCellFactory(knownContributorsColumn, knownContributorsChoiceList);
		columnList.add(knownContributorsColumn);

		TableColumn<ObservableList<?>, String> outputColumn = new TableColumn<>(Constants.CEESIT_TABLE_COLUMN_OUTPUT);
		outputColumn.setMinWidth(75);
		outputColumn.setEditable(true);
		outputColumn.setSortable(false);
		outputColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX);
					}
				});
		outputColumn.setOnEditStart(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = (String) cellEditEvent.getNewValue();
				String oldValue = (String) cellEditEvent.getOldValue();
				if (!fileChooserOpen) {
					fileChooserOpen = true;
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle(Constants.SAVE_OUTPUT_FILE);
					// get last save path from settings
					String lastPDFPath = Settings.lastPDFPath;
					setFileChooserInitialDirectory(fileChooser, lastPDFPath);
					fileChooser.getExtensionFilters().add(new ExtensionFilter("Output File (*.pdf)", "*.pdf"));
					fileChooser.getExtensionFilters().add(allFilesFilter);
					File file = fileChooser.showSaveDialog(stage);
					fileChooserOpen = false;

					if (file != null) {
						((SimpleStringProperty) ceesItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
								.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX)).set(file.getAbsolutePath());
						Settings.lastPDFPath = file.getParent();
						Settings.save();
						// Save user generated paths. Any paths not found in this
						// list will be auto-generated
						// and if user changes filter, NOC, or calibration, a new
						// auto-generated file name will be created.
						if (!userSelectedCEESItOutputFilePaths.contains(file.getAbsolutePath())) {
							userSelectedCEESItOutputFilePaths.add(file.getAbsolutePath());
						}
						int rowID = ((SimpleIntegerProperty) ceesItTable.getItems().get(cellEditEvent.getTablePosition().getRow()).get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
						if (ceesItExistingFileRowList.contains(rowID)) {
							if (!file.exists()) {
								ceesItExistingFileRowList.remove(ceesItExistingFileRowList.indexOf(rowID));
								ceesItTable.refresh();
							}
						}
						if (ceesItDuplicateOutputRowList.contains(rowID) &&
								!oldValue.equals(value)) {
							ceesItDuplicateOutputRowList.remove(ceesItDuplicateOutputRowList.indexOf(rowID));
							ceesItTable.refresh();
						}
					}
				}
			}
		});
		//outputColumn.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
		outputColumn.setCellFactory(column -> {
			return new TableCell<ObservableList<?>, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(item);
					if (getIndex() > -1) {
						// Duplicate file error more serious, cannot run the batch when
						// duplicate files exist. Existing files can be overwritten
						try {
							// This line sometimes throws an index out of range exception when
							// scrolling the table. Unsure what is out of range.
							int rowID = ((SimpleIntegerProperty) ceesItTable.getItems().get(getIndex()).get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
							boolean contains = false;
							if (ceesItExistingFileRowList.contains(rowID)) {
								setTextFill(Color.ORANGE);
								contains = true;
							}
							if (ceesItDuplicateOutputRowList.contains(rowID)) {
								setTextFill(Color.RED);
								contains = true;
							}
							if (!contains) {
								setTextFill(Color.BLACK);
							}
						} catch (Exception e) {
							
						}
					}
					if (item != null && item.length() > 0) {
						setTooltip(new Tooltip(item));
						if (ceesItExistingFileRowList.contains(getIndex())) {
							setTooltip(new Tooltip(Constants.FILE_EXISTS_TOOLTIP_PREFIX + item));
						}
						if (ceesItDuplicateOutputRowList.contains(getIndex())) {
							setTooltip(new Tooltip(Constants.DUPLICATE_OUTPUT_TOOLTIP_PREFIX + item));
						}
					} else {
						setTooltip(null);
					}
				}
			};
		});
		columnList.add(outputColumn);
		
		TableColumn<ObservableList<?>, String> commentsColumn = new TableColumn<>(
				Constants.CEESIT_TABLE_COLUMN_COMMENTS);
		commentsColumn.setMinWidth(Constants.CEESIT_TABLE_COLUMN_COMMENTS_WIDTH);
		commentsColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_COMMENTS_WIDTH);
		commentsColumn.setPrefWidth(Constants.CEESIT_TABLE_COLUMN_COMMENTS_WIDTH);
		commentsColumn.setEditable(false);
		commentsColumn.setSortable(false);
		commentsColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.CEESIT_TABLE_COLUMN_COMMENTS_INDEX);
					}
				});
		commentsColumn.setCellFactory(column -> {
			return new TableCell<ObservableList<?>, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(item);
					if (item != null && item.length() > 0) {
						setTooltip(new Tooltip(item));
					} else {
						setTooltip(null);
					}
				}
			};
		});
		columnList.add(commentsColumn);

		TableColumn<ObservableList<?>, Integer> rowIDColumn = new TableColumn<>();
		rowIDColumn.setEditable(true);
		rowIDColumn.setSortable(false);
		rowIDColumn.setVisible(false);
		columnList.add(rowIDColumn);

		ceesItTable.getColumns().setAll(columnList);

		ceesItTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		// Row Context Menu
		ceesItTable.setRowFactory(new Callback<TableView<ObservableList<?>>, TableRow<ObservableList<?>>>() {
			public TableRow<ObservableList<?>> call(TableView<ObservableList<?>> tableView) {
				final TableRow<ObservableList<?>> row = new TableRow<>();
				final ContextMenu contextMenu = new ContextMenu();

				final MenuItem thresholdMenuItem = new MenuItem("Set Analytical Thresholds");
				thresholdMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						ObservableList<?> currentRow = row.getItem();
						int rowID = ((SimpleIntegerProperty) currentRow.get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX))
								.get();

						editAnalyticalThresholds(ceesItRowIDAnalyticalThresholdsMap.get(Integer.toString(rowID)),
								row.getIndex());
					}
				});
				contextMenu.getItems().add(thresholdMenuItem);

				final MenuItem removeMenuItem = new MenuItem("Remove");
				removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						ceesItTable.getItems().remove(row.getItem());
						backendController.updateCEESItGraphs();
						if (startCEESItButtonClicked) {
							clearCEESItFileLists();
			    			ceesItTable.refresh();
						}
					}
				});
				contextMenu.getItems().add(removeMenuItem);
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty()
				.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
				final MenuItem fillWithCaseNumberMenuItem = new MenuItem("Fill With Case Number");
				fillWithCaseNumberMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						fillWithValue(ceesItTable, row, Constants.CEESIT_TABLE_COLUMN_CASE_INDEX, row.getIndex());
					}
				});
				contextMenu.getItems().add(fillWithCaseNumberMenuItem);
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty()
				.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
				final MenuItem fillWithPOIMenuItem = new MenuItem("Fill With POI");
				fillWithPOIMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						fillWithValue(ceesItTable, row, Constants.CEESIT_TABLE_COLUMN_POI_INDEX, row.getIndex());
						clearCEESItFileLists();
						ceesItTable.refresh();
					}
				});
				contextMenu.getItems().add(fillWithPOIMenuItem);
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty()
				.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
				final MenuItem fillWithKnownContributorMenuItem = new MenuItem("Fill With Known Contributors");
				fillWithKnownContributorMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						fillWithValue(ceesItTable, row, Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX, row.getIndex());
						clearCEESItFileLists();
						ceesItTable.refresh();
					}
				});
				contextMenu.getItems().add(fillWithKnownContributorMenuItem);
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty()
				.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
				final MenuItem commentsMenuItem = new MenuItem("Add/Edit Comments");
				commentsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						buildCommentsBox(ceesItTable, row, Constants.CEESIT_TABLE_COLUMN_COMMENTS_INDEX);
					}
				});
				contextMenu.getItems().add(commentsMenuItem);
				
				SeparatorMenuItem separator = new SeparatorMenuItem();
				contextMenu.getItems().add(separator);
				
				final MenuItem duplicateRowMenuItem = new MenuItem("Duplicate Row");
				duplicateRowMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						duplicateRow(ceesItTable, row, ceesItRowIDAnalyticalThresholdsMap);
						if (startCEESItButtonClicked) {
							clearCEESItFileLists();
			    			ceesItTable.refresh();
						}
					}
				});
				contextMenu.getItems().add(duplicateRowMenuItem);
				
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty()
				.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
				return row;
			}
		});
		ceesItTable.getColumns().addListener(new ListChangeListener() {
			@Override
			public void onChanged(Change change) {
				change.next();
				if (change.wasReplaced()) {
					try {
						ceesItTable.getColumns().clear();
						ceesItTable.getColumns().addAll(columnList);
					} catch (Exception e) {
						// e.printStackTrace();
						logger.error(Constants.DISABLE_COLUMN_REORDERING_ERROR, e);
					}
				}
			}
		});
	}

	/**
	 * Builds the genotype split pane (shown when adding, viewing, or editing a
	 * genotype).
	 *
	 * @param table
	 *            the table
	 * @param sampleName
	 *            the sample name
	 * @param save
	 *            the save button
	 * @param cancel
	 *            the cancel button
	 * @return the split pane
	 */
	private SplitPane buildGenotypeSplitPane(TableView<ObservableList<?>> table, TextField sampleName, Button save,
			Button cancel) {
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

		Label label = new Label(Constants.GENOTYPE_POPUP_LABEL_NAME);
		AnchorPane.setTopAnchor(label, 10.0);
		AnchorPane.setLeftAnchor(label, 5.0);

		middlePane.getChildren().add(scrollPane);
		topPane.getChildren().add(label);
		topPane.getChildren().add(sampleName);

		scrollPane.setContent(table);
		bottomPane.getChildren().add(stackPane);
		stackPane.getChildren().add(save);
		stackPane.getChildren().add(cancel);
		pane.getItems().addAll(topPane, table, bottomPane);

		return pane;
	}

	/**
	 * Builds/rebuilds the NOCIt table UI elements.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildNOCItTable() {
		nocItTable.getColumns().clear();
		
		// Build Headers
		List<TableColumn<ObservableList<?>, ?>> columnList = new ArrayList<>();

		TableColumn<ObservableList<?>, Boolean> graphColumn = new TableColumn<>(Constants.NOCIT_TABLE_COLUMN_GRAPH);
		graphColumn.setMinWidth(Constants.NOCIT_TABLE_COLUMN_MIN_WIDTH);
		graphColumn.setPrefWidth(Constants.NOCIT_TABLE_COLUMN_GRAPH_WIDTH);
		graphColumn.setEditable(true);
		graphColumn.setSortable(false);
		graphColumn.setGraphic(getNOCItGraphCheckBox());
		graphColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(
							CellDataFeatures<ObservableList<?>, Boolean> cellDataFeatures) {
						boolean allSelected = true;
						// if any cell is not selected all selected is false
						for (ObservableList<?> row : nocItTable.getItems()) {
							boolean value = ((SimpleBooleanProperty) row.get(Constants.NOCIT_TABLE_COLUMN_GRAPH_INDEX))
									.get();
							if (value == false) {
								allSelected = false;
							}
						}
						getNOCItGraphCheckBox().setSelected(allSelected);
						isNOCItGraphAllSelected = allSelected;
						return (SimpleBooleanProperty) cellDataFeatures.getValue()
								.get(Constants.NOCIT_TABLE_COLUMN_GRAPH_INDEX);
					}
				});
		graphColumn.setCellFactory(
				new Callback<TableColumn<ObservableList<?>, Boolean>, TableCell<ObservableList<?>, Boolean>>() {
					@Override
					public TableCell<ObservableList<?>, Boolean> call(TableColumn<ObservableList<?>, Boolean> p) {
						CheckBoxTableCell<ObservableList<?>, Boolean> checkBoxCell = new CheckBoxTableCell<>();
						checkBoxCell.addEventHandler(ActionEvent.ACTION, new EventHandler() {
							@Override
							public void handle(Event event) {
								backendController.updateNOCItGraphs();
							}
						});
						return checkBoxCell;
					}
				});
		columnList.add(graphColumn);

		TableColumn<ObservableList<?>, String> caseColumn = new TableColumn<>(Constants.NOCIT_TABLE_COLUMN_CASE);
		caseColumn.setMinWidth(Constants.NOCIT_TABLE_COLUMN_MIN_WIDTH);
		caseColumn.setPrefWidth(Constants.NOCIT_TABLE_COLUMN_CASE_WIDTH);
		caseColumn.setEditable(true);
		caseColumn.setSortable(false);
		caseColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.NOCIT_TABLE_COLUMN_CASE_INDEX);
					}
				});
		// caseColumn.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
		caseColumn.setCellFactory(new EditingTableCellFactory());
		columnList.add(caseColumn);

		TableColumn<ObservableList<?>, String> sampleColumn = new TableColumn<>(Constants.NOCIT_TABLE_COLUMN_SAMPLE);
		sampleColumn.setMinWidth(Constants.NOCIT_TABLE_COLUMN_MIN_WIDTH);
		sampleColumn.setPrefWidth(Constants.NOCIT_TABLE_COLUMN_SAMPLE_WIDTH);
		sampleColumn.setEditable(false);
		sampleColumn.setSortable(false);
		sampleColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.NOCIT_TABLE_COLUMN_SAMPLE_INDEX);
					}
				});
		sampleColumn.setCellFactory(column -> {
			return new TableCell<ObservableList<?>, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(item);
					if (item != null && item.length() > 0) {
						setTooltip(new Tooltip(item));
					}
				}
			};
		});
		columnList.add(sampleColumn);

		TableColumn<ObservableList<?>, Boolean> filterColumn = new TableColumn<>(Constants.NOCIT_TABLE_COLUMN_FILTER);
		filterColumn.setMinWidth(Constants.NOCIT_TABLE_COLUMN_MIN_WIDTH);
		filterColumn.setPrefWidth(Constants.NOCIT_TABLE_COLUMN_FILTER_WIDTH);
		filterColumn.setEditable(true);
		filterColumn.setSortable(false);
		filterColumn.setGraphic(getNOCItFilterCheckBox());
		filterColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(
							CellDataFeatures<ObservableList<?>, Boolean> cellDataFeatures) {
						boolean allSelected = true;
						// if any cell is not selected all selected is false
						for (ObservableList<?> row : nocItTable.getItems()) {
							boolean value = ((SimpleBooleanProperty) row.get(Constants.NOCIT_TABLE_COLUMN_FILTER_INDEX))
									.get();
							if (value == false) {
								allSelected = false;
							}
						}
						getNOCItFilterCheckBox().setSelected(allSelected);
						isNOCItFilterAllSelected = allSelected;
						return (SimpleBooleanProperty) cellDataFeatures.getValue()
								.get(Constants.NOCIT_TABLE_COLUMN_FILTER_INDEX);
					}
				});
		filterColumn.setCellFactory(
				new Callback<TableColumn<ObservableList<?>, Boolean>, TableCell<ObservableList<?>, Boolean>>() {
					@Override
					public TableCell<ObservableList<?>, Boolean> call(TableColumn<ObservableList<?>, Boolean> p) {
						CheckBoxTableCell<ObservableList<?>, Boolean> checkBoxCell = new CheckBoxTableCell<>();
						checkBoxCell.addEventHandler(ActionEvent.ACTION, new EventHandler() {
							@Override
							public void handle(Event event) {
								ObservableList rowList = (ObservableList) nocItTable.getItems()
										.get(checkBoxCell.getTableRow().getIndex());
								String outputName = ((SimpleStringProperty) rowList
										.get(Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
								if (!userSelectedNOCItOutputFilePaths.contains(outputName)) {
									UtilityMethods.updateNOCItOutputName(nocItTable, 
											rowList, checkBoxCell.getTableRow().getIndex());
									if (startNOCItButtonClicked) {
										clearNOCItFileLists();
										nocItTable.refresh();
									}
								}
							}
						});

						return checkBoxCell;
					}
				});
		columnList.add(filterColumn);

		TableColumn<ObservableList<?>, String> dnaColumn = new TableColumn<>(Constants.NOCIT_TABLE_COLUMN_DNA);
		dnaColumn.setMinWidth(Constants.NOCIT_TABLE_COLUMN_MIN_WIDTH);
		dnaColumn.setEditable(true);
		dnaColumn.setSortable(false);
		dnaColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						cellDataFeatures.getValue();
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.NOCIT_TABLE_COLUMN_DNA_INDEX);
					}
				});
		dnaColumn.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
		// Column no longer used, hide it for now to keep existing code using
		// column index working
		dnaColumn.setVisible(false);
		columnList.add(dnaColumn);

		// Population Name
		TableColumn<ObservableList<?>, String> popColumn = new TableColumn<>(Constants.NOCIT_TABLE_COLUMN_POPULATION);
		popColumn.setMinWidth(Constants.NOCIT_TABLE_COLUMN_POPULATION_WIDTH);
		popColumn.setPrefWidth(Constants.NOCIT_TABLE_COLUMN_POPULATION_WIDTH);
		popColumn.setEditable(true);
		popColumn.setSortable(false);
		popColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cdf) {
						return (SimpleStringProperty) cdf.getValue().get(Constants.NOCIT_TABLE_COLUMN_POPULATION_INDEX);
					}
				});
		popColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = (String) cellEditEvent.getNewValue();
				((SimpleStringProperty) nocItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
						.get(Constants.NOCIT_TABLE_COLUMN_POPULATION_INDEX)).set(value);
			}
		});
		ObservableList<String> popChoiceList = FXCollections.observableArrayList(this.populations.keySet());
		popColumn.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(popChoiceList));
		columnList.add(popColumn);

		TableColumn<ObservableList<?>, String> nocColumn = new TableColumn<>(Constants.NOCIT_TABLE_COLUMN_NOC);
		nocColumn.setMinWidth(Constants.NOCIT_TABLE_COLUMN_NOC_WIDTH);
		nocColumn.setPrefWidth(Constants.NOCIT_TABLE_COLUMN_NOC_WIDTH);
		nocColumn.setEditable(true);
		nocColumn.setSortable(false);
		nocColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cdf) {
						return (SimpleStringProperty) cdf.getValue().get(Constants.NOCIT_TABLE_COLUMN_NOC_INDEX);
					}
				});
		nocColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = (String) cellEditEvent.getNewValue();
				String oldValue = (String) cellEditEvent.getOldValue();
				ObservableList rowList = (ObservableList) nocItTable.getItems()
						.get(cellEditEvent.getTablePosition().getRow());
				String outputName = ((SimpleStringProperty) rowList.get(Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX))
						.get();
				((SimpleStringProperty) nocItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
						.get(Constants.NOCIT_TABLE_COLUMN_NOC_INDEX)).set(value);
				if (!userSelectedNOCItOutputFilePaths.contains(outputName)) {
					UtilityMethods.updateNOCItOutputName(nocItTable, 
							rowList, cellEditEvent.getTablePosition().getRow());
				}
				if (!value.equals(oldValue) && startNOCItButtonClicked) {
					clearNOCItFileLists();
					nocItTable.refresh();
				}
			}
		});
		ObservableList<String> nocChoiceList = FXCollections
				.observableArrayList(Arrays.asList(Constants.NOCIT_NOC_DROPDOWN_OPTIONS));
		nocColumn.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(nocChoiceList));
		columnList.add(nocColumn);

		final TableColumn<ObservableList<?>, String> calibrationColumn = new TableColumn<>(
				Constants.NOCIT_TABLE_COLUMN_CALIBRATION);
		calibrationColumn.setMinWidth(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_WIDTH);
		calibrationColumn.setPrefWidth(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_WIDTH);
		calibrationColumn.setEditable(true);
		calibrationColumn.setSortable(false);
		calibrationColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cdf) {
						return (SimpleStringProperty) cdf.getValue()
								.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX);
					}
				});
		calibrationColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = (String) cellEditEvent.getNewValue();
				String oldValue = (String) cellEditEvent.getOldValue();
				if (value.equals("<Add New>")) {
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle(Constants.SELECT_FILE);
					// get last save path from settings
					String lastCalibrationPath = Settings.lastCalibrationPath;
					setFileChooserInitialDirectory(fileChooser, lastCalibrationPath);
					// set file name from calibration name field if directory
					// exists and field is not empty
					// TODO: check if this is necessary
					setFileChooserFileName(fileChooser, lastCalibrationPath, calibrationName.getText());
					// Set extension filters
					fileChooser.getExtensionFilters().add(calibrationFilter);
					fileChooser.getExtensionFilters().add(allFilesFilter);
					File file = fileChooser.showOpenDialog(stage);

					if (file != null) {
						if (!FileChecker.isValidCalibrationProjectFile(file.getAbsolutePath())) {
							UIController.displayErrorDialog("File Validation",
									"This is not a valid Calibration Project file: \r\n\r\n" + file.getAbsolutePath());
							((SimpleStringProperty) nocItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
									.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX)).set("<wdw>");
							((SimpleStringProperty) nocItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
									.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX)).set(oldValue);
							return;
						}
						Project project = CalibrationProjectHandler.loadProjectData(file);
						if (project.getCalibration() != null) {
							String name = file.getName().split("\\.")[0];
							updateCalibrationOptions(name, project.getCalibration());

							ObservableList<String> calibrationChoiceList = createCalibrationChoiceList();
							calibrationColumn.setCellFactory(
									ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(calibrationChoiceList));

							Settings.lastCalibrationPath = file.getParent();
							Settings.save();
							((SimpleStringProperty) nocItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
									.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX)).set(name);

							ObservableList rowList = (ObservableList) nocItTable.getItems()
									.get(cellEditEvent.getTablePosition().getRow());
							updateNOCItATsForSelectedCalibration(project.getCalibration(), rowList);
							String outputName = ((SimpleStringProperty) rowList
									.get(Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
							if (!userSelectedNOCItOutputFilePaths.contains(outputName)) {
								UtilityMethods.updateNOCItOutputName(nocItTable, rowList,
										cellEditEvent.getTablePosition().getRow());
								if (!value.equals(oldValue) && startNOCItButtonClicked) {
									clearNOCItFileLists();
									nocItTable.refresh();
								}
							}
						}
					} else {
						((SimpleStringProperty) nocItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
								.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX)).set("<wdw>");
						((SimpleStringProperty) nocItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
								.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX)).set(oldValue);
					}
				} else {
					((SimpleStringProperty) nocItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
							.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX)).set(value);
					// Row list must be created after cell has been updated
					ObservableList rowList = (ObservableList) nocItTable.getItems()
							.get(cellEditEvent.getTablePosition().getRow());
					String calibrationName = ((SimpleStringProperty) rowList
							.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX)).get();
					updateNOCItATsForSelectedCalibration(calibrations.get(calibrationName), rowList);
					String outputName = ((SimpleStringProperty) rowList.get(Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX))
							.get();
					if (!userSelectedNOCItOutputFilePaths.contains(outputName)) {
						UtilityMethods.updateNOCItOutputName(nocItTable, rowList, 
								cellEditEvent.getTablePosition().getRow());
						if (!value.equals(oldValue) && startNOCItButtonClicked) {
							clearNOCItFileLists();
							nocItTable.refresh();
						}
					}
				}
			}
		});
		// add observable list created from settings load of recent files
		ObservableList<String> calibrationChoiceList = createCalibrationChoiceList();

		calibrationColumn
				.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(calibrationChoiceList));
		columnList.add(calibrationColumn);

		TableColumn<ObservableList<?>, String> outputColumn = new TableColumn<>(Constants.NOCIT_TABLE_COLUMN_OUTPUT);
		outputColumn.setMinWidth(Constants.NOCIT_TABLE_COLUMN_MIN_WIDTH);
		outputColumn.setPrefWidth(Constants.NOCIT_TABLE_COLUMN_OUTPUT_WIDTH);
		outputColumn.setEditable(true);
		outputColumn.setSortable(false);
		outputColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX);
					}
				});
		outputColumn.setOnEditStart(new EventHandler<TableColumn.CellEditEvent<ObservableList<?>, String>>() {
			@Override
			public void handle(TableColumn.CellEditEvent<ObservableList<?>, String> cellEditEvent) {
				String value = (String) cellEditEvent.getNewValue();
				String oldValue = (String) cellEditEvent.getOldValue();
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle(Constants.SAVE_OUTPUT_FILE);
				// get last save path from settings
				String lastPDFPath = Settings.lastPDFPath;
				setFileChooserInitialDirectory(fileChooser, lastPDFPath);
				fileChooser.getExtensionFilters().add(new ExtensionFilter("Output File (*.pdf)", "*.pdf"));
				fileChooser.getExtensionFilters().add(allFilesFilter);
				File file = fileChooser.showSaveDialog(stage);

				if (file != null) {
					((SimpleStringProperty) nocItTable.getItems().get(cellEditEvent.getTablePosition().getRow())
							.get(Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX)).set(file.getAbsolutePath());
					Settings.lastPDFPath = file.getParent();
					Settings.save();
					// Save user generated paths. Any paths not found in this
					// list will be auto-generated
					// and if user changes filter, NOC, or calibration, a new
					// auto-generated file name will be created.
					if (!userSelectedNOCItOutputFilePaths.contains(file.getAbsolutePath())) {
						userSelectedNOCItOutputFilePaths.add(file.getAbsolutePath());
					}
					int rowID = ((SimpleIntegerProperty) nocItTable.getItems().get(cellEditEvent.getTablePosition().getRow()).get(Constants.NOCIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
					if (nocItExistingFileRowList.contains(rowID)) {
						if (!file.exists()) {
							nocItExistingFileRowList.remove(nocItExistingFileRowList.indexOf(rowID));
							nocItTable.refresh();
						}
					}
					if (nocItDuplicateOutputRowList.contains(rowID) &&
							!oldValue.equals(value)) {
						nocItDuplicateOutputRowList.remove(nocItDuplicateOutputRowList.indexOf(rowID));
						nocItTable.refresh();
					}
					
				}
			}
		});
		outputColumn.setCellFactory(column -> {
			return new TableCell<ObservableList<?>, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(item);
					if (getIndex() > -1) {
						// Duplicate file error more serious, cannot run the batch when
						// duplicate files exist. Existing files can be overwritten
						try {
							// This line sometimes throws an index out of range exception when
							// scrolling the table. Unsure what is out of range.
							int rowID = ((SimpleIntegerProperty) nocItTable.getItems().get(getIndex()).get(Constants.NOCIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
							boolean contains = false;
							if (nocItExistingFileRowList.contains(rowID)) {
								setTextFill(Color.ORANGE);
								contains = true;
							}
							if (nocItDuplicateOutputRowList.contains(rowID)) {
								setTextFill(Color.RED);
								contains = true;
							}
							if (!contains) {
								setTextFill(Color.BLACK);
							}
						} catch (Exception e) {
							
						}
					}
					if (item != null && item.length() > 0) {
						setTooltip(new Tooltip(item));
						if (nocItExistingFileRowList.contains(getIndex())) {
							setTooltip(new Tooltip(Constants.FILE_EXISTS_TOOLTIP_PREFIX + item));
						}
						if (nocItDuplicateOutputRowList.contains(getIndex())) {
							setTooltip(new Tooltip(Constants.DUPLICATE_OUTPUT_TOOLTIP_PREFIX + item));
						}
					} else {
						setTooltip(null);
					}
				}
			};
		});
		columnList.add(outputColumn);

		TableColumn<ObservableList<?>, String> commentsColumn = new TableColumn<>(
				Constants.NOCIT_TABLE_COLUMN_COMMENTS);
		commentsColumn.setMinWidth(Constants.NOCIT_TABLE_COLUMN_COMMENTS_WIDTH);
		commentsColumn.setPrefWidth(Constants.NOCIT_TABLE_COLUMN_COMMENTS_WIDTH);
		commentsColumn.setPrefWidth(Constants.NOCIT_TABLE_COLUMN_COMMENTS_WIDTH);
		commentsColumn.setEditable(false);
		commentsColumn.setSortable(false);
		commentsColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue()
								.get(Constants.NOCIT_TABLE_COLUMN_COMMENTS_INDEX);
					}
				});
		commentsColumn.setCellFactory(column -> {
			return new TableCell<ObservableList<?>, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(item);
					if (item != null && item.length() > 0) {
						setTooltip(new Tooltip(item));
					} else {
						setTooltip(null);
					}
				}
			};
		});
		columnList.add(commentsColumn);

		// Column no longer used, hide it for now to keep existing code using
		// column index working
		TableColumn<ObservableList<?>, String> thresholdsColumn = new TableColumn<>();
		thresholdsColumn.setEditable(true);
		thresholdsColumn.setSortable(false);
		thresholdsColumn.setVisible(false);
		columnList.add(thresholdsColumn);

		TableColumn<ObservableList<?>, Integer> rowIDColumn = new TableColumn<>();
		rowIDColumn.setEditable(true);
		rowIDColumn.setSortable(false);
		rowIDColumn.setVisible(false);
		columnList.add(rowIDColumn);

		nocItTable.getColumns().setAll(columnList);

		nocItTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		// Row Context Menu
		nocItTable.setRowFactory(new Callback<TableView<ObservableList<?>>, TableRow<ObservableList<?>>>() {
			public TableRow<ObservableList<?>> call(TableView<ObservableList<?>> tableView) {
				final TableRow<ObservableList<?>> row = new TableRow<>();
				final ContextMenu contextMenu = new ContextMenu();

				final MenuItem thresholdMenuItem = new MenuItem("Set Analytical Thresholds");
				thresholdMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						ObservableList<?> currentRow = row.getItem();
						int rowID = ((SimpleIntegerProperty) currentRow.get(Constants.NOCIT_TABLE_COLUMN_ROW_ID_INDEX))
								.get();
				
						editAnalyticalThresholds(rowIDAnalyticalThresholdsMap.get(Integer.toString(rowID)),
								row.getIndex());
					}
				});
				contextMenu.getItems().add(thresholdMenuItem);

				final MenuItem removeMenuItem = new MenuItem("Remove");
				removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						nocItTable.getItems().remove(row.getItem());
						backendController.updateNOCItGraphs();
						if (startNOCItButtonClicked) {
							clearNOCItFileLists();
							nocItTable.refresh();
						}
					}
				});
				contextMenu.getItems().add(removeMenuItem);
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty()
						.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
				final MenuItem fillDownCaseNumberMenuItem = new MenuItem("Fill With Case Number");
				fillDownCaseNumberMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						fillWithValue(nocItTable, row, Constants.NOCIT_TABLE_COLUMN_CASE_INDEX, row.getIndex());
					}
				});
				contextMenu.getItems().add(fillDownCaseNumberMenuItem);
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty()
						.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
				final MenuItem commentsMenuItem = new MenuItem("Add/Edit Comments");
				commentsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						buildCommentsBox(nocItTable, row, Constants.NOCIT_TABLE_COLUMN_COMMENTS_INDEX);
					}
				});
				contextMenu.getItems().add(commentsMenuItem);
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty()
						.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
				return row;
			}
		});
		nocItTable.getColumns().addListener(new ListChangeListener() {
			@Override
			public void onChanged(Change change) {
				change.next();
				if (change.wasReplaced()) {
					try {
						nocItTable.getColumns().clear();
						nocItTable.getColumns().addAll(columnList);
					} catch (Exception e) {
						// e.printStackTrace();
						logger.error(Constants.DISABLE_COLUMN_REORDERING_ERROR, e);
					}
				}
			}
		});
	}

	/**
	 * Builds/Rebuilds the TreeTable UI element (on second calibration pane) and
	 * inserts the data values into the table.
	 *
	 * @param treeTableData
	 *            the tree table data
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildTreeTable(List<List<List<String>>> treeTableData) {
		if (treeTableView.getRoot() != null) {
			treeTableView.getRoot().getChildren().clear();
		}
		treeTableView.setRoot(null);
		treeTableView.getColumns().removeAll(treeTableView.getColumns());
		if (!calibrationCalculated) {
			treeTableView.setEditable(true);
		} else {
			treeTableView.setEditable(false);
		}

		List<TreeTableColumn<ObservableList<String>, String>> columnList = new ArrayList<>();

		// Build Headers
		TreeTableColumn<ObservableList<String>, String> column = new TreeTableColumn<>("");
		// Should this be editable?
		// column.setEditable(true);
		column.setEditable(false);
		column.setMinWidth(200);
		column.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TreeTableColumn.CellDataFeatures<ObservableList<String>, String> p) {
						return new ReadOnlyStringWrapper(p.getValue().getValue().get(0));
					}
				});
		column.setCellFactory(TextFieldTreeTableCell.<ObservableList<String>>forTreeTableColumn());
		treeTableView.getColumns().add(column);
		columnList.add(column);

		TreeTableColumn<ObservableList<String>, String> column1 = new TreeTableColumn<>(
				Constants.TREE_TABLE_COLUMN_FORMULA);
		column1.setEditable(false);
		column1.setMinWidth(75);
		column1.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TreeTableColumn.CellDataFeatures<ObservableList<String>, String> p) {
						return new ReadOnlyStringWrapper(p.getValue().getValue().get(1));
					}
				});
		column1.setCellFactory(TextFieldTreeTableCell.<ObservableList<String>>forTreeTableColumn());
		treeTableView.getColumns().add(column1);
		columnList.add(column1);

		final TreeTableColumn<ObservableList<String>, String> column2 = new TreeTableColumn<>(
				Constants.TREE_TABLE_COLUMN_A);
		column2.setEditable(true);
		column2.setMinWidth(50);
		column2.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TreeTableColumn.CellDataFeatures<ObservableList<String>, String> p) {
						return new SimpleStringProperty(p.getValue().getValue().get(2));
					}
				});
		column2.setCellFactory(new EditingTreeTableCellFactory());
		column2.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<ObservableList<String>, String>>() {
			@Override
			public void handle(
					javafx.scene.control.TreeTableColumn.CellEditEvent<ObservableList<String>, String> cellEditEvent) {
				ObservableList<String> list = cellEditEvent.getRowValue().getValue();
				try {
					double value = Double.parseDouble(cellEditEvent.getNewValue());
					list.set(2, Double.toString(value));
				} catch (NumberFormatException e) {
					displayErrorDialog(Constants.INVALID_DOUBLE_ERROR_TITLE, Constants.INVALID_DOUBLE_ERROR_MESSAGE);
				}
				cellEditEvent.getRowValue().setValue(list);
				// refresh row display
				column2.setCellFactory(new EditingTreeTableCellFactory());
			}
		});
		treeTableView.getColumns().add(column2);
		columnList.add(column2);

		final TreeTableColumn<ObservableList<String>, String> column3 = new TreeTableColumn<>(
				Constants.TREE_TABLE_COLUMN_B);
		column3.setEditable(true);
		column3.setMinWidth(50);
		column3.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TreeTableColumn.CellDataFeatures<ObservableList<String>, String> p) {
						// return new
						// ReadOnlyStringWrapper(p.getValue().getValue().get(3));
						return new SimpleStringProperty(p.getValue().getValue().get(3));
					}
				});
		column3.setCellFactory(new EditingTreeTableCellFactory());
		column3.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<ObservableList<String>, String>>() {
			@Override
			public void handle(
					javafx.scene.control.TreeTableColumn.CellEditEvent<ObservableList<String>, String> cellEditEvent) {
				ObservableList<String> list = cellEditEvent.getRowValue().getValue();
				try {
					double value = Double.parseDouble(cellEditEvent.getNewValue());
					list.set(3, Double.toString(value));
				} catch (NumberFormatException e) {
					displayErrorDialog(Constants.INVALID_DOUBLE_ERROR_TITLE, Constants.INVALID_DOUBLE_ERROR_MESSAGE);
				}
				cellEditEvent.getRowValue().setValue(list);
				// refresh row display
				column3.setCellFactory(new EditingTreeTableCellFactory());
			}
		});
		treeTableView.getColumns().add(column3);
		columnList.add(column3);

		final TreeTableColumn<ObservableList<String>, String> column4 = new TreeTableColumn<>(
				Constants.TREE_TABLE_COLUMN_C);
		column4.setEditable(true);
		column4.setMinWidth(50);
		column4.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TreeTableColumn.CellDataFeatures<ObservableList<String>, String> p) {
						return new SimpleStringProperty(p.getValue().getValue().get(4));
					}
				});
		column4.setCellFactory(new EditingTreeTableCellFactory());
		column4.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<ObservableList<String>, String>>() {
			@Override
			public void handle(
					javafx.scene.control.TreeTableColumn.CellEditEvent<ObservableList<String>, String> cellEditEvent) {
				ObservableList<String> list = cellEditEvent.getRowValue().getValue();
				try {
					double value = Double.parseDouble(cellEditEvent.getNewValue());
					list.set(4, Double.toString(value));
				} catch (NumberFormatException e) {
					displayErrorDialog(Constants.INVALID_DOUBLE_ERROR_TITLE, Constants.INVALID_DOUBLE_ERROR_MESSAGE);
				}
				cellEditEvent.getRowValue().setValue(list);
				// refresh row display
				column4.setCellFactory(new EditingTreeTableCellFactory());
			}
		});
		treeTableView.getColumns().add(column4);
		columnList.add(column4);

		final TreeTableColumn<ObservableList<String>, String> column5 = new TreeTableColumn<>(
				Constants.TREE_TABLE_COLUMN_D);
		column5.setEditable(true);
		column5.setMinWidth(50);
		column5.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TreeTableColumn.CellDataFeatures<ObservableList<String>, String> p) {
						// return new
						// ReadOnlyStringWrapper(p.getValue().getValue().get(4));
						return new SimpleStringProperty(p.getValue().getValue().get(5));
					}
				});
		column5.setCellFactory(new EditingTreeTableCellFactory());
		column5.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<ObservableList<String>, String>>() {
			@Override
			public void handle(
					javafx.scene.control.TreeTableColumn.CellEditEvent<ObservableList<String>, String> cellEditEvent) {
				ObservableList<String> list = cellEditEvent.getRowValue().getValue();
				try {
					double value = Double.parseDouble(cellEditEvent.getNewValue());
					list.set(5, Double.toString(value));
				} catch (NumberFormatException e) {
					displayErrorDialog(Constants.INVALID_DOUBLE_ERROR_TITLE, Constants.INVALID_DOUBLE_ERROR_MESSAGE);
				}
				cellEditEvent.getRowValue().setValue(list);
				// refresh row display
				column5.setCellFactory(new EditingTreeTableCellFactory());
			}
		});
		treeTableView.getColumns().add(column5);
		columnList.add(column5);

		TreeTableColumn<ObservableList<String>, String> column6 = new TreeTableColumn<>(
				Constants.TREE_TABLE_COLUMN_R_SQUARED);
		column6.setEditable(false);
		column6.setMinWidth(50);
		column6.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(
							TreeTableColumn.CellDataFeatures<ObservableList<String>, String> p) {
						return new ReadOnlyStringWrapper(p.getValue().getValue().get(6));
					}
				});
		column6.setCellFactory(TextFieldTreeTableCell.<ObservableList<String>>forTreeTableColumn());
		treeTableView.getColumns().add(column6);
		columnList.add(column6);

		final TreeItem<ObservableList<String>> root = new TreeItem<>(
				FXCollections.observableArrayList("Data", "", "", "", "", ""));
		if (treeTableData != null) {
			for (int i = 0; i < treeTableData.size(); i++) {
				List<List<String>> parent = treeTableData.get(i);
				final TreeItem<ObservableList<String>> parentNode = new TreeItem<>(
						FXCollections.observableArrayList(parent.get(0)));
				for (int j = 1; j < parent.size(); j++) {
					List<String> child = parent.get(j);
					final TreeItem<ObservableList<String>> childNode = new TreeItem<>(
							FXCollections.observableArrayList(child));
					parentNode.getChildren().add(childNode);
				}
				root.getChildren().add(parentNode);
			}
		}
		treeTableView.setShowRoot(false);
		treeTableView.setRoot(root);

		treeTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener() {
			@Override
			public void onChanged(Change change) {
				// Check whether item is selected and set value of selected item
				// to Label
				if (treeTableView.getSelectionModel().getSelectedItem() != null) {
					lineChartAnchor.getChildren().clear();
					TreeItem<ObservableList<String>> item = treeTableView.getSelectionModel().getSelectedItem();

					if (item.getParent() != null) {
						TreeItem<ObservableList<String>> parent = item.getParent();

						if (!parent.getValue().get(0).equals("Data")) {
							Locus locus = new Locus(item.getValue().get(0));
							String label = parent.getValue().get(0);

							if (calibrationCalculated) {
								LineChart<Number, Number> lineChart = backendController.plotGraph(label, locus);
								if (lineChart != null) {
									lineChartAnchor.getChildren().add(lineChart);
									AnchorPane.setTopAnchor(lineChart, 0.0);
									AnchorPane.setBottomAnchor(lineChart, 0.0);
									AnchorPane.setLeftAnchor(lineChart, 0.0);
									AnchorPane.setRightAnchor(lineChart, 0.0);
								}
							}
						}
					}
				}
			}
		});

		// disable reordering of columns
		treeTableView.getColumns().addListener(new ListChangeListener() {
			@Override
			public void onChanged(Change change) {
				change.next();
				if (change.wasReplaced()) {
					try {
						treeTableView.getColumns().clear();
						treeTableView.getColumns().addAll(columnList);
					} catch (Exception e) {
						// e.printStackTrace();
						logger.error(Constants.DISABLE_COLUMN_REORDERING_ERROR, e);
					}
				}
			}
		});
	}

	/**
	 * Builds the view genotypes table.
	 */
	private void buildViewGenotypesTable() {
		viewGenotypesTable.getColumns().clear();
		viewGenotypesTable.getItems().clear();
		viewGenotypesTable.setEditable(true);

		TableColumn<ObservableList<?>, String> column1 = new TableColumn<>("Genotype ID");
		column1.setMinWidth(Constants.VIEW_GENOTYPES_TABLE_COLUMNS_MIN_WIDTH);
		column1.setEditable(false);
		column1.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue().get(0);
					}
				});
		column1.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
		viewGenotypesTable.getColumns().add(column1);	

		// Set up context menu
		final MenuItem menuAddNew = new MenuItem(Constants.VIEW_GENOTYPES_TABLE_CONTEXT_MENU_OPTION_ADD_NEW);
		menuAddNew.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				showGenotypeEditor(null, null);
			}
		});

		final MenuItem menuDuplicate = new MenuItem(Constants.VIEW_GENOTYPES_TABLE_CONTEXT_MENU_OPTION_DUPLICATE);
		menuDuplicate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				ObservableList<?> row = viewGenotypesTable.getSelectionModel().getSelectedItem();
				if (row != null) {
					Genotype genotype = genotypes.get(((SimpleStringProperty) row.get(0)).getValue());
					copyGenotype(genotype);
				}
			}
		});

		final MenuItem menuEdit = new MenuItem(Constants.VIEW_GENOTYPES_TABLE_CONTEXT_MENU_OPTION_EDIT);
		menuEdit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				ObservableList<?> row = viewGenotypesTable.getSelectionModel().getSelectedItem();
				if (row != null) {
					SimpleStringProperty property = ((SimpleStringProperty) row.get(0));
					Genotype genotype = genotypes.get(property.getValue());
					showGenotypeEditor(null, genotype);
				}
			}
		});

		final MenuItem menuDelete = new MenuItem(Constants.VIEW_GENOTYPES_TABLE_CONTEXT_MENU_OPTION_DELETE);
		menuDelete.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				ObservableList<?> row = viewGenotypesTable.getSelectionModel().getSelectedItem();
				if (row != null) {
					Genotype genotype = genotypes.get(((SimpleStringProperty) row.get(0)).getValue());
					List<String> previousValues = new ArrayList<>();
					for (ObservableList<?> calibrationRow : calibrationTable.getItems()) {
						String value = ((SimpleStringProperty) calibrationRow
								.get(Constants.CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE_INDEX)).get();
						previousValues.add(value);
					}
					if (previousValues.contains(genotype.getGenotypeID())) {
						ButtonType[] results = { ButtonType.YES, ButtonType.NO };
						Alert alert = new Alert(AlertType.WARNING, Constants.USED_GENOTYPE_DELETION_WARNING_QUESTION,
								results);
						Optional<ButtonType> result = alert.showAndWait();
						if (result.isPresent() && result.get() == ButtonType.YES) {
							for (int j = 0; j < previousValues.size(); j++) {
								if (previousValues.get(j).equals(genotype.getGenotypeID())) {
									previousValues.set(j, Constants.DEFAULT_CHOOSE);
								}
							}
							deleteGenotypeSample(genotype);

							int i = 0;
							for (ObservableList<?> calibrationRow : calibrationTable.getItems()) {
								((SimpleStringProperty) calibrationRow
										.get(Constants.CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE_INDEX))
												.set(previousValues.get(i));
								i++;
							}
						} else {
							return;
						}
					} else {
						deleteGenotypeSample(genotype);
					}
				}
			}
		});

		viewGenotypesTable.setContextMenu(new ContextMenu(menuAddNew, menuDuplicate, menuEdit, menuDelete));
		
		viewGenotypesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
					if (genotypes.size() == 0) {
						menuDuplicate.setDisable(true);
						menuEdit.setDisable(true);
						menuDelete.setDisable(true);
					}					
				}
			}
		});
		
		LinkedHashSet<Locus> genotypeLoci = new LinkedHashSet<Locus>();		
		if (backendController.getCalibration() != null) 
			genotypeLoci.addAll(backendController.getCalibration().getLoci());		
		for (String genotypeId : genotypes.keySet()) 
			genotypeLoci.addAll(genotypes.get(genotypeId).getLoci());

		// create columns for loci
		int i = 1;
		for (Locus locus : genotypeLoci) {
			final int columnIndex = i;
			
			TableColumn<ObservableList<?>, String> column = new TableColumn<>(locus.getName());
			column.setMinWidth(Constants.VIEW_GENOTYPES_TABLE_COLUMNS_MIN_WIDTH);
			column.setEditable(false);
			column.setCellValueFactory(
					new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
						@Override
						public ObservableValue<String> call(
								CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
							return (SimpleStringProperty) cellDataFeatures.getValue().get(columnIndex);
						}
					});
			column.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
			viewGenotypesTable.getColumns().add(column);

			i++;
		}

		ListProperty<SimpleStringProperty> listProperty = new SimpleListProperty<SimpleStringProperty>(
				FXCollections.<SimpleStringProperty>observableArrayList());

		for (String genotypeId : genotypes.keySet()) {
			listProperty = new SimpleListProperty<SimpleStringProperty>(
					FXCollections.<SimpleStringProperty>observableArrayList());
			listProperty.add(new SimpleStringProperty(genotypeId));
			for (Locus locus : genotypeLoci) {
				String str = "";
				if (genotypes.get(genotypeId).containsLocus(locus)) {
					String x = genotypes.get(genotypeId).getAlleles(locus)[0].toString();
					String y = genotypes.get(genotypeId).getAlleles(locus)[1].toString();
					str = x + "," + y;
				}
				listProperty.add(new SimpleStringProperty(str));
			}
			viewGenotypesTable.getItems().addAll(FXCollections.observableArrayList(Arrays.asList(listProperty)));
		}
	}

	/**
	 * This is called when the Calculate button on the second Calibrate page is
	 * clicked.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void calculateParameters(ActionEvent event) {
		if (Constants.USE_CALIBRATION_PROGRESS_BAR) {
			calculateParametersTask();
		} else {
			calculateParametersMethod(null);
			reloadTreeTableAfterCalculation();
		}
		showSaveChangesPrompt = true;
		calibrationCalculated = true;
	}

	/**
	 * This is called when the Calculate button on the second Calibrate page is
	 * clicked.
	 *
	 * @param progressBar
	 *            the progress bar
	 */
	private void calculateParametersMethod(ProgressBar progressBar) {
		filteredCsvModuleMap = new HashMap<String, CSVModule>();

		for (String fileName : csvModuleMap.keySet())
			filteredCsvModuleMap.put(fileName, new CSVModule(csvModuleMap.get(fileName)));

		for (ObservableList<?> calibrationRow : calibrationTable.getItems()) {
			String fileName = ((SimpleStringProperty) calibrationRow.get(0)).getValue();
			String sampleName = ((SimpleStringProperty) calibrationRow.get(1)).getValue();
			boolean filter = ((SimpleBooleanProperty) calibrationRow.get(2)).getValue();

			if (filter) {
				filteredCsvModuleMap.get(fileName).getSamples().get(sampleName)
						.filter(backendController.getCalibration().getKit());
				filteredCsvModuleMap.get(fileName).getSamples().get(sampleName).setFiltered(true);
			} else
				filteredCsvModuleMap.get(fileName).getSamples().get(sampleName).setFiltered(false);
		}

		treeData = new ArrayList<>();
		// List<List<List<String>>> treeData = new ArrayList<>();
		for (TreeItem<ObservableList<String>> row : treeTableView.getRoot().getChildren()) {
			List<List<String>> featureData = new ArrayList<>();
			List<String> rowData = new ArrayList<>();
			for (String value : row.getValue()) {
				rowData.add(value);
			}
			featureData.add(rowData);

			for (TreeItem<ObservableList<String>> cell : row.getChildren()) {
				List<String> cellData = new ArrayList<>();
				for (String value : cell.getValue()) {
					cellData.add(value);
					System.out.println("added Row " + cell.toString() + " for feature value :" + value);
				}
				featureData.add(cellData);
			}

			treeData.add(featureData);
		}

		HashMap<Feature, double[]> initialParams = new HashMap<Feature, double[]>();

		int i = 0;
		for (Feature feature : Feature.values()) {
			double[] params = new double[feature.getModel().getVarCount()];
			for (int j = 0; j < params.length; j++) {
				params[j] = Double.parseDouble(treeData.get(i).get(0).get(2 + j));
			}
			initialParams.put(feature, params);
			i++;
		}

		backendController.calculateParameters(filteredCsvModuleMap.values(), initialParams, progressBar);
		HashMap<Feature, List<Double>> rSquaredValues = backendController.getCalibration().getRSquaredValues();

		NumberFormat formatter = new DecimalFormat("#0.00");
		i = 0;
		for (Feature feature : Feature.values()) {
			String rSquaredStr = rSquaredValues.get(feature).get(0).isNaN() ? "NaN" 
					: formatter.format(rSquaredValues.get(feature).get(0));
			treeData.get(i).get(0).set(Constants.TREE_TABLE_COLUMN_R_SQUARED_INDEX, rSquaredStr);

			List<Locus> loci = backendController.getCalibration().getLoci();
			List<List<String>> rowList = treeData.get(i);
			Iterator<List<String>> iter = rowList.iterator();

			iter.next();
			while (iter.hasNext()) {
				List<String> row = iter.next();

				Locus locus = new Locus(row.get(0));
				double[] params = backendController.getCalibration().getParams(feature).get(locus);
				if (params != null) {
					for (int i1 = 0; i1 < Math.min(params.length,
							Constants.TREE_TABLE_COLUMN_R_SQUARED_INDEX - 2); i1++) {
						String paramValue = UtilityMethods.roundToSignificantFigures(params[i1],
								Constants.SIGNIFICANT_FIGURES, Constants.SCIENTIFIC_FORMATTER,
								Constants.MIN_DECIMAL_FORMAT, Constants.MAX_DECIMAL_FORMAT);
						row.set(2 + i1, paramValue);
						// row.set(2 + i1, formatter.format(params[i1]));
					}

					rSquaredStr = rSquaredValues.get(feature).get(loci.indexOf(locus) + 1).isNaN() ? "NaN" 
							: formatter.format(rSquaredValues.get(feature).get(loci.indexOf(locus) + 1));
					row.set(Constants.TREE_TABLE_COLUMN_R_SQUARED_INDEX, rSquaredStr);
				} else {
					iter.remove();
				}
			}

			i++;
		}
	}

	/**
	 * Runs calibration calculations while showing a progress bar.
	 */
	private void calculateParametersTask() {
		final double width = 300.0d;
		final double height = 10.0d;
		Label updateLabel = new Label("Calculating ...");
		updateLabel.setPrefWidth(width);
		ProgressBar progressBar = new ProgressBar();
		progressBar.setProgress(0);
		progressBar.setPrefWidth(width);
		progressBar.setPrefHeight(height);

		VBox updatePane = new VBox();
		updatePane.setPadding(new Insets(10));
		updatePane.setSpacing(5.0d);
		updatePane.getChildren().addAll(updateLabel, progressBar);

		Stage taskUpdateStage = new Stage(StageStyle.UTILITY);
		taskUpdateStage.initModality(Modality.APPLICATION_MODAL);
		taskUpdateStage.setScene(new Scene(updatePane));
		taskUpdateStage.show();

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				calculateParametersMethod(progressBar);
				Thread.sleep(100);

				return null;
			}
		};

		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				taskUpdateStage.hide();
				reloadTreeTableAfterCalculation();
			}
		});

		taskUpdateStage.show();
		new Thread(task).start();
	}

	/**
	 * This function is called when the Save button on the second calibration
	 * page is clicked.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void calibrationSaveClicked(ActionEvent event) {
		saveCalibration();
	}

	/**
	 * This is called when the Cancel button is clicked on the CEESIt tab.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void cancelCEESItCalculations(ActionEvent event) {
		this.backendController.cancelCEESIt();
	}
	
	/**
	 * This is called when the Cancel button is clicked on the NOCIt tab.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void cancelNOCItCalculations(ActionEvent event) {
		this.backendController.cancelNOCIt();
	}

	/**
	 * Prompts user to choose bins file.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void chooseBinsFile(ActionEvent event) {
		if (!sampleFolderPath.getText().equals("")) {
			ButtonType[] results = { ButtonType.YES, ButtonType.NO };
			Alert alert = new Alert(AlertType.WARNING, Constants.LOAD_KIT_AFTER_SAMPLES_WARNING_QUESTION, results);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.YES) {
				sampleFolderPath.setText("");
				sampleFolderPath.setDisable(false);
				// clear table and calculations on next page when loading new
				// sample files
				calibrationTable.getItems().clear();
				resetTreeTable();
			} else {
				return;
			}
		}
		// get last path from settings
		String lastBinsFilePath = Settings.lastBinsFilePath;
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(Constants.SELECT_FILE);
		// if last path is a directory, set chooser to last path
		setFileChooserInitialDirectory(fileChooser, lastBinsFilePath);
		fileChooser.getExtensionFilters()
				.add(new ExtensionFilter(Constants.BINS_FILE_EXTENSION_1, Constants.BINS_FILE_EXTENSION_2));
		fileChooser.getExtensionFilters().add(allFilesFilter);
		File file = fileChooser.showOpenDialog(stage);

		if (file != null) {
			if (!FileChecker.isValidBinsFile(file.getAbsolutePath())) {
				UIController.displayErrorDialog("File Validation",
						"This is not a valid Bins file: \r\n\r\n" + file.getAbsolutePath());
				kitName.setText("");
				kitName.setDisable(false);
				return;
			}
			// path chosen by user written to settings
			Settings.lastBinsFilePath = file.getParent();
			Settings.save();

			try {
				Kit kit = new Kit(file.getPath());
				backendController.setKit(kit);
				kitName.setText(kit.getKitName());
				kitName.setDisable(true);
				kitSelected = true;
				// Calibration name field is only disabled when opening a
				// calibration. Old calibration
				// name must be cleared when loading a kit.
				if (calibrationName.isDisabled()) {
					calibrationName.setText("");
				}
				calibrationName.setDisable(false);
				sampleFolderPath.setText("");
				sampleFolderPath.setDisable(false);
				calibrationSaveButton.setDisable(true);
				saveCalibrationMenu.setDisable(true);
				getSelectAllCheckBox().setDisable(true);
				// fixes bug where if Open Calibration is clicked, then Kit
				// Browse button clicked, tables cleared but Load Sample File(s) remained
				// disabled, leaving gui in an unusable state.
				browseSampleFiles.setDisable(false);
				calibrationCalculated = false;
				showSaveChangesPrompt = true;
				// Clear Genotypes table and dropboxes. This is necessary since
				// the Genotypes that is loaded may not contain all loci in the bins file.
				genotypes.clear();
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR, Constants.LOAD_BINS_FILE_ERROR_MESSAGE);
				alert.showAndWait();
				// e.printStackTrace();
				logger.error(Constants.LOAD_BINS_FILE_ERROR_LOG_MESSAGE, e);
			}
		}
	}

	/**
	 * Prompts user to choose the folder containing the Sample Files and loads
	 * them.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void chooseSampleFilesFolder(ActionEvent event) {
		if (!kitSelected) {
			Alert alert = new Alert(AlertType.ERROR, Constants.ERROR_NO_BINS_FILE_MESSAGE);
			alert.showAndWait();
			return;

		}
		if (calibrationCalculated) {
			ButtonType[] results = { ButtonType.YES, ButtonType.NO };
			Alert alert = new Alert(AlertType.WARNING, Constants.LOAD_SAMPLES_AFTER_CALCULATION_WARNING_QUESTION,
					results);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.YES) {
				resetTreeTable();
			} else {
				return;
			}
		}
		// get last path from settings
		String lastCalibrationSamplesPath = Settings.lastCalibrationSamplesPath;
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle(Constants.SELECT_FOLDER);
		// if last path is a directory, set chooser to last path
		setDirectoryChooserInitialDirectory(directoryChooser, lastCalibrationSamplesPath);
		File file = directoryChooser.showDialog(stage);

		if (file != null) {
			sampleFolderPath.setText(file.getName());
			sampleFolderPath.setDisable(true);

			// path chosen by user written to settings
			String path = file.getAbsolutePath();
			if (file.getParent() != null) {
				path = file.getParent();
			}
			Settings.lastCalibrationSamplesPath = path;
			Settings.save();

			try {
				csvModuleMap.clear();

				boolean validFiles = true;
				ArrayList<String> invalidFileList = new ArrayList<String>();
				for (File sampleFile : file.listFiles()) {
					if (sampleFile.getName().endsWith(".csv") || sampleFile.getName().endsWith(".txt")) {
						try {
							CSVModule csvModule = new CSVModule(sampleFile,
									backendController.getCalibration().getKit());
							// If csvModule contains no samples, the file read
							// is not a file of the correct type.
							if (csvModule.getSamples().size() > 0 && csvModule.isValidFile()) {
								csvModuleMap.put(csvModule.getFileName(), csvModule);
							} else {
								validFiles = false;
								invalidFileList.add(sampleFile.getAbsolutePath());
							}
						} catch (Exception e) {
							validFiles = false;
							logger.warn(Constants.INVALID_SAMPLE_FILES_WARNING, e);
						}
					}
				}
				if (!validFiles) {
					// prevent two alerts from occurring if directory contains 0
					// valid files
					if (csvModuleMap.size() > 0) {
						Alert alert = new Alert(AlertType.WARNING,
								Constants.INVALID_SAMPLE_FILES_WARNING + invalidFileList.toString() + ".");
						alert.showAndWait();
					}
				}

				// clear table and calculations on next page when loading new
				// sample files
				calibrationTable.getItems().clear();
				resetTreeTable();
				calibrationCalculated = false;

				// no valid files in directory
				if (csvModuleMap.size() == 0) {
					// System.out.println("No valid files in directory");
					Alert alert = new Alert(AlertType.ERROR, Constants.NO_VALID_SAMPLE_FILES_IN_DIRECTORY);
					alert.showAndWait();
					sampleFolderPath.setText("");
					sampleFolderPath.setDisable(false);
				} else {
					for (CSVModule csvModule : csvModuleMap.values()) {
						HashMap<String, String> genotypeIDs = matchGenotypeIDs(csvModule);
						try {
							// HashMap<String, Double> dnaMasses =
							// csvModule.matchDNAMasses();
							for (int i = 0; i < csvModule.getSampleNames().size(); i++) {
								String sampleName = csvModule.getSampleNames().get(i);
								Boolean sampleFilter = false;

								ObservableList<Object> data = FXCollections.observableArrayList();
								data.add(new SimpleStringProperty(csvModule.getFileName()));
								data.add(new SimpleStringProperty(sampleName));
								data.add(new SimpleBooleanProperty(sampleFilter));
								// dna mass hidden column entry
								data.add(new SimpleStringProperty("0"));
								// if Load Sample Files is done before Import Known Genotype File
								// this value may be null
								if (genotypeIDs != null && genotypeIDs.get(sampleName) != null) {
									data.add(new SimpleStringProperty(genotypeIDs.get(sampleName)));
								} else {
									data.add(new SimpleStringProperty(Constants.KNOWN_GENOTYPE_DEFAULT_VALUE));
								}

								calibrationTable.getItems().add(data);
							}
						} catch (Exception e) {
							Alert alert = new Alert(AlertType.ERROR, Constants.LOAD_SAMPLE_FILES_ERROR_MESSAGE);
							alert.showAndWait();
							logger.error(Constants.LOAD_SAMPLE_FILES_ERROR_LOG_MESSAGE, e);
							sampleFolderPath.setText("");
							sampleFolderPath.setDisable(false);
						}
					}

					kitName.setDisable(true);
					calibrationSaveButton.setDisable(false);
					saveCalibrationMenu.setDisable(false);
					getSelectAllCheckBox().setDisable(false);
					showSaveChangesPrompt = true;
				}
			} catch (Exception e) {
				// e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR, Constants.LOAD_SAMPLE_FILES_ERROR_MESSAGE);
				alert.showAndWait();
				logger.error(Constants.LOAD_SAMPLE_FILES_ERROR_LOG_MESSAGE, e);
			}
		}
	}

	/**
	 * This is called when File -> Close is clicked on the top menu bar.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	public void close(ActionEvent event) {
		closeMethod();
	}

	/**
	 * Actions that must be performed before closing the program.
	 */
	private void closeActions() {
		Settings.save();

		if (Constants.DELETE_EMPTY_LOG_FILES) {
			// delete empty log files on close
			File logDirectory = new File(Settings.getSettingsPath() + "/logs/");
			deleteEmptyFilesFromLogFolder(logDirectory);
		}
	}

	/**
	 * Close method.
	 */
	public void closeMethod() {
		if (Constants.AUTOSAVE_ON) {
			this.autoSave();
			autoSaver.stop();
		}

		this.nocItTimeElapsedTimer.stop();
		disableItemsWhileNOCItRunning(false);
		nocItProgressBar.setProgress(0);

		this.ceesItTimeElapsedTimer.stop();
		disableItemsWhileCEESItRunning(false);
		ceesItProgressBar.setProgress(0);

		// only display close dialog if table not empty and not saved
		if (calibrationTable.getItems().size() > 0 && showSaveChangesPrompt) {
			ButtonType[] results = { ButtonType.YES, ButtonType.NO, ButtonType.CANCEL };
			Alert alert = new Alert(AlertType.CONFIRMATION, Constants.SAVE_PROGRESS_MESSAGE, results);
			alert.setHeaderText(Constants.SAVE_PROGRESS);
			Optional<ButtonType> result = alert.showAndWait();

			if (result.isPresent() && result.get() == ButtonType.YES) {
				saveCalibration();

				if (Constants.AUTOSAVE_ON) {
					File f = new File(Settings.getSettingsPath() + "/" + Constants.AUTOSAVE_FILENAME);
					if (f.exists()) {
						if (!f.delete()) {
							logger.error(Constants.AUTOSAVE_DELETE_ERROR);
						}
					}
				}
				closeActions();
				System.exit(0);
			} else if (result.isPresent() && result.get() == ButtonType.NO) {
				if (Constants.AUTOSAVE_ON) {
					File f = new File(Settings.getSettingsPath() + "/" + Constants.AUTOSAVE_FILENAME);
					if (f.exists()) {
						if (!f.delete()) {
							logger.error(Constants.AUTOSAVE_DELETE_ERROR);
						}
					}
				}
				closeActions();
				System.exit(0);
			} else if (result.isPresent() && result.get() == ButtonType.CANCEL) {
				return;
			} else {
				closeActions();
				return;
			}
		} else {
			closeActions();
			System.exit(0);
		}
	}

	/**
	 * This method is run when the "+" button is clicked on NOCIt tab.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void configureBatchRun(ActionEvent event) {
		if (populations.size() == 0) {
			ButtonType[] results = { ButtonType.YES, ButtonType.NO };
			Alert alert = new Alert(AlertType.ERROR, Constants.NO_POPULATIONS_EXIST_ERROR, results);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.YES) {
				showPopulationDialog();
			} else {
				return;
			}
		} else {
			try {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("DrillDownNOCIt.fxml"));
				Parent root = loader.load();
				Scene scene = new Scene(root);

				nocitStage = new Stage();
				nocitStage.setTitle("NOCIt Batch Run");
				nocitStage.initModality(Modality.WINDOW_MODAL);
				nocitStage.setScene(scene);
				nocitStage.setMinWidth(600);
				nocitStage.setMinHeight(475);
				nocitStage.setResizable(false);
				nocitStage.getIcons().add(new Image(getClass().getResourceAsStream("dna2.png")));
				nocitStage.initOwner(stage);
				nocitStage.show();

				final DrillDownNOCItController controller = loader.getController();
				controller.setStage(nocitStage, this);
				controller.buildCalibrationChoiceList();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(Constants.CONFIGURE_NOCIt_BATCH_RUN_ERROR, e);
			}
		}
	}
	
	/**
	 * This method is run when the "+" button is clicked on CEESIt tab.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void configureCEESItBatchRun(ActionEvent event) {
		if (populations.size() == 0) {
			ButtonType[] results = { ButtonType.YES, ButtonType.NO };
			Alert alert = new Alert(AlertType.ERROR, Constants.NO_POPULATIONS_EXIST_CEESIT_ERROR, results);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.YES) {
				showPopulationDialog();
			} else {
				return;
			}
		} else {
			try {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("DrillDownCEESIt.fxml"));
				Parent root = loader.load();
				Scene scene = new Scene(root);

				ceesitStage = new Stage();
				ceesitStage.setTitle("CEESIt Batch Run");
				ceesitStage.initModality(Modality.WINDOW_MODAL);
				ceesitStage.setScene(scene);
				ceesitStage.setMinWidth(600);
				ceesitStage.setMinHeight(475);
				ceesitStage.setResizable(false);
				ceesitStage.getIcons().add(new Image(getClass().getResourceAsStream("dna2.png")));
				ceesitStage.initOwner(stage);
				ceesitStage.show();

				final DrillDownCEESItController controller = loader.getController();
				controller.setStage(ceesitStage, this);
				controller.buildCalibrationChoiceList();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(Constants.CONFIGURE_NOCIt_BATCH_RUN_ERROR, e);
			}
		}
	}

	/**
	 * Called when copy genotype is selected.
	 *
	 * @param genotype
	 *            the genotype
	 */
	private void copyGenotype(Genotype genotype) {
		int i = 1;
		String copyID = genotype.getGenotypeID() + "(" + i + ")";
		while (genotypes.containsKey(copyID)) {
			i++;
			copyID = genotype.getGenotypeID() + "(" + i + ")";
		}

		Genotype genotypeCopy = new Genotype(genotype);
		genotypeCopy.setGenotypeID(copyID);		

		genotypes.put(copyID, genotypeCopy);
		if (viewGenotypesStage.isShowing()) {
			buildViewGenotypesTable();
		}
		updateGenotypeOptions();
	}

	/**
	 * Gets calibrations list and adds "Add New" entry.
	 *
	 * @return the list of calibrations with the "Add New" entry
	 */
	public ObservableList<String> createCalibrationChoiceList() {
		ObservableList<String> calibrationChoiceList = FXCollections.observableArrayList(calibrations.keySet());
		calibrationChoiceList.add("<Add New>");

		return calibrationChoiceList;
	}

	/**
	 * Creates table used for adding/viewing/editing a genotype.
	 *
	 * @return the table view
	 */
	@SuppressWarnings("unchecked")
	private TableView<ObservableList<?>> createGenotypeTable() {
		final TableView<ObservableList<?>> table = new TableView<>();
		table.setEditable(true);
		// disables row selection
		table.setSelectionModel(null);
		
		List<TableColumn<ObservableList<?>, ?>> columnList = new ArrayList<>();

		// Build Columns and Headers
		TableColumn<ObservableList<?>, String> column1 = new TableColumn<>(Constants.GENOTYPE_TABLE_COLUMN_LOCUS);
		column1.setMinWidth(90);
		column1.setMaxWidth(90);
		column1.setEditable(false);
		column1.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue().get(0);
					}
				});
		column1.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
		table.getColumns().add(column1);
		columnList.add(column1);

		TableColumn<ObservableList<?>, String> column2 = new TableColumn<>(Constants.GENOTYPE_TABLE_COLUMN_ALLELE1);
		column2.setMinWidth(90);
		column2.setMaxWidth(90);
		column2.setEditable(true);
		column2.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue().get(1);
					}
				});

		column2.setCellFactory(new GenotypeFieldCellFactory());
		table.getColumns().add(column2);
		columnList.add(column2);

		TableColumn<ObservableList<?>, String> column3 = new TableColumn<>(Constants.GENOTYPE_TABLE_COLUMN_ALLELE2);
		column3.setMinWidth(90);
		column3.setMaxWidth(90);
		column3.setEditable(true);
		column3.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
						return (SimpleStringProperty) cellDataFeatures.getValue().get(2);
					}
				});

		column3.setCellFactory(new GenotypeFieldCellFactory());
		table.getColumns().add(column3);
		columnList.add(column3);
		
		// Disable column reordering
		table.getColumns().addListener(new ListChangeListener() {
			@Override
			public void onChanged(Change change) {
				change.next();
				if (change.wasReplaced()) {
					try {
						table.getColumns().clear();
						table.getColumns().addAll(columnList);
					} catch (Exception e) {
						// e.printStackTrace();
						// logger.error(Constants.DISABLE_COLUMN_REORDERING_ERROR, e);
					}
				}
			}
		});
		
		// Default context menu Paste item cleared table and showed error message when clicked. Override
		// replaces default context menu with Copy menu. Cells cannot be selected in Locus column
		// or empty column on right so Copy just returns an empty string. A preferable solution would
		// be to remove the context menu if anyone can figure out how to do so.
		// based on http://stackoverflow.com/questions/11347535/javafx-tableview-copy-to-clipboard
		table.setRowFactory(new Callback<TableView<ObservableList<?>>, TableRow<ObservableList<?>>>() {
			@Override
			public TableRow<ObservableList<?>> call(TableView<ObservableList<?>> tableView) {
				final TableRow<ObservableList<?>> row = new TableRow<>();
				final ContextMenu contextMenu = new ContextMenu();
				final MenuItem copyMenu = new MenuItem("Copy");
				copyMenu.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						String entry = "";
						StringBuilder clipboardString = new StringBuilder();
						try {
							ObservableList<ObservableList<?>> rows = table.getSelectionModel()
									.getSelectedItems();					        
					        for (int i = 0; i < rows.size(); i++) {
					        	for (int j = 0; j < rows.get(i).size(); j++) {
					        		entry = ((SimpleStringProperty) rows.get(i).get(j)).get();
						            clipboardString.append(entry);
					        	}
					        }
						} catch (Exception e) {
							
						}
						final ClipboardContent content = new ClipboardContent();
				        content.putString(clipboardString.toString());
				        Clipboard.getSystemClipboard().setContent(content);
					}
				});
				contextMenu.getItems().add(copyMenu);
				// Set context menu on row, but use a binding to make it only
				// show for non-empty rows:
				row.contextMenuProperty()
						.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
				return row;
			}
		});

		return table;
	}

	/**
	 * Deletes empty log files. Will not delete current log file since it is
	 * open.
	 *
	 * @param folder
	 *            the folder
	 */
	private void deleteEmptyFilesFromLogFolder(final File folder) {
		for (final File fileEntry : folder.listFiles()) {
			// there should not be any directories in the log directory since
			// the directory is created by NOCIt
			if (fileEntry.isDirectory()) {
				// listFilesForFolder(fileEntry);
			} else {
				File file = new File(fileEntry.getAbsolutePath());
				if (file.exists() && file.length() == 0
						&& file.getAbsolutePath().endsWith(Constants.LOG_FILE_EXTENSION)) {
					try {
						file.delete();
					} catch (Exception e) {
						// e.printStackTrace();
						// No good reason to show an alert here. The only
						// purpose of this code is
						// to prevent the build up of empty log files in the
						// directory where logs are saved.
						logger.error(Constants.DELETE_LOG_FILE_ERROR, e);
					}
				}
			}
		}
	}

	/**
	 * Delete genotype.
	 *
	 * @param genotype
	 *            the genotype to delete
	 */
	// delete
	private void deleteGenotypeSample(Genotype genotype) {
		genotypes.remove(genotype.getGenotypeID());
		if (viewGenotypesStage.isShowing()) {
			buildViewGenotypesTable();
		}
		updateGenotypeOptions();
		if (genotypes.size() == 0) {
			saveGenotypesMenu.setDisable(true);
		} else {
			saveGenotypesMenu.setDisable(false);
		}
	}

	/**
	 * Disable items while CEESIt running.
	 *
	 * @param running
	 *            the running
	 */
	public void disableItemsWhileCEESItRunning(boolean running) {
		ceesItTable.setDisable(running);
		startCEESItButton.setDisable(running);
		newCalibrationMenu.setDisable(running);
		loadCalibrationMenu.setDisable(running);
		ceesitPlusButton.setDisable(running);
		ceesitMinusButton.setDisable(running);
		timerRunning = running;
	}

	/**
	 * Disable items while NOCIt running.
	 *
	 * @param running
	 *            the running
	 */
	public void disableItemsWhileNOCItRunning(boolean running) {
		nocItTable.setDisable(running);
		startNOCItButton.setDisable(running);
		newCalibrationMenu.setDisable(running);
		loadCalibrationMenu.setDisable(running);
		nocitPlusButton.setDisable(running);
		nocitMinusButton.setDisable(running);
		timerRunning = running;
	}	
	
	/**
	 * Edits the analytical thresholds.
	 *
	 * @param thresholdData
	 *            the threshold data
	 * @param rowIndex
	 *            the row index
	 */
	private void editAnalyticalThresholds(LinkedHashMap<Locus, Integer> thresholdData, int rowIndex) {
		try {
			EditAnalyticalThresholdsController controller = new EditAnalyticalThresholdsController();
			FXMLLoader loader = new FXMLLoader();
			loader.setController(controller);
			loader.setLocation(getClass().getResource("AnalyticalThresholds.fxml"));

			Parent root = loader.load();
			Scene scene = new Scene(root);

			nocitStage = new Stage();
			nocitStage.setTitle("Edit Analytical Thresholds");
			nocitStage.initModality(Modality.WINDOW_MODAL);
			nocitStage.setScene(scene);
			nocitStage.setMinWidth(300);
			nocitStage.setMinHeight(400);
			nocitStage.setResizable(true);
			nocitStage.getIcons().add(new Image(getClass().getResourceAsStream("dna2.png")));
			nocitStage.initOwner(stage);
			nocitStage.show();

			controller.setStage(nocitStage, this, rowIndex);
			controller.populateTable(thresholdData);
		} catch (Exception e) {
			// e.printStackTrace();
			Alert alert = new Alert(AlertType.ERROR, Constants.EDIT_ANALYTICAL_THRESHOLDS_ERROR);
			alert.showAndWait();
			logger.error(Constants.EDIT_ANALYTICAL_THRESHOLDS_ERROR, e);
		}
	}

	/**
	 * This is called when Edit -> Settings is clicked on the top menu bar.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void editSettings(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("Settings.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);

			settingsStage = new Stage();
			settingsStage.setTitle(Constants.SETTINGS_POPUP_TITLE);
			settingsStage.initModality(Modality.WINDOW_MODAL);
			settingsStage.setScene(scene);
			// settingsStage.setMinWidth(534);
			// settingsStage.setMinHeight(400);
			settingsStage.setResizable(false);
			settingsStage.getIcons().add(new Image(getClass().getResourceAsStream("dna2.png")));
			settingsStage.initOwner(stage);
			settingsStage.show();

			final SettingsController controller = loader.getController();
			setSettingsController(controller);
			controller.setStage(settingsStage, this);
			if (timerRunning) {
				controller.disableControls(true);
			}
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error(Constants.SETTINGS_POPUP_LOAD_ERROR_LOG_MESSAGE, e);
		}
	}

	/**
	 * When project is loaded, if Calculate has been run before Saving project,
	 * need to disable Calculate button and enable Reset button, or do the
	 * opposite if Calculate has not been run. The R^2 column is not editable
	 * and values can only be set by running Calculate, so if this column
	 * contains data, Calculate must have been run.
	 *
	 * @param project
	 *            the project
	 * @return hasData - true if calculation has been run, false otherwise
	 */
	public boolean enableCalculateButton(Project project) {
		boolean hasData = false;
		if (project.getTreeTableData().size() > 1 && project.getTreeTableData().get(0).get(0).size() == 6) {
			if (!project.getTreeTableData().get(0).get(0).get(5).isEmpty()) {
				hasData = true;
			}
		}
		return hasData;
	}

	/**
	 * Returns the copy menu item used in Edit Genotypes. May be usable in other
	 * tables?
	 *
	 * @param table
	 *            the table
	 * @return the menu item
	 */
	private MenuItem genotypesTableCopyItem(TableView<ObservableList<?>> table) {
		MenuItem menuCopy = new MenuItem(Constants.GENOTYPE_TABLE_CONTEXT_MENU_COPY);
		menuCopy.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				try {
					String clipboardString = "";

					for (ObservableList<?> row : table.getItems()) {
						for (Object cell : row) {
							clipboardString += (((SimpleStringProperty) cell).getValue() + "\t");
						}
						clipboardString += "\n";
					}

					StringSelection selection = new StringSelection(clipboardString);
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR, Constants.GENOTYPE_TABLE_CONTEXT_MENU_COPY_ERROR_MESSAGE);
					alert.showAndWait();
					logger.warn(Constants.GENOTYPE_TABLE_CONTEXT_MENU_COPY_ERROR_LOG_MESSAGE, e);
				}
			}
		});
		return menuCopy;
	}

	/**
	 * Returns the paste menu item used in Edit Genotypes. May be usable in
	 * other tables?
	 *
	 * @param table
	 *            the table
	 * @return the menu item
	 */
	private MenuItem genotypesTablePasteItem(TableView<ObservableList<?>> table) {
		MenuItem menuPaste = new MenuItem(Constants.GENOTYPE_TABLE_CONTEXT_MENU_PASTE);
		menuPaste.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				try {
					String clipboardData = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
							.getData(DataFlavor.stringFlavor);
					List<List<String>> tableData = new ArrayList<>();

					for (String loopRow : clipboardData.split("\n")) {
						List<String> row = new ArrayList<>();
						row.addAll(Arrays.asList(loopRow.split("\t")));
						tableData.add(row);
					}

					table.getItems().clear();

					for (List<String> row : tableData) {
						table.getItems()
								.add(FXCollections.observableArrayList(Arrays.asList(new SimpleStringProperty[] {
										new SimpleStringProperty(row.get(0)), new SimpleStringProperty(row.get(1)),
										new SimpleStringProperty(row.get(2)) })));
					}
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR, Constants.GENOTYPE_TABLE_CONTEXT_MENU_PASTE_ERROR_MESSAGE);
					alert.showAndWait();
					logger.warn(Constants.GENOTYPE_TABLE_CONTEXT_MENU_PASTE_ERROR_LOG_MESSAGE, e);
				}
			}
		});
		return menuPaste;
	}

	/**
	 * Gets the calibration with a given name.
	 *
	 * @param name
	 *            the name
	 * @return the calibration
	 */
	public Calibration getCalibration(String name) {
		return calibrations.get(name);
	}

	/**
	 * Gets the CEESIt filter check box.
	 *
	 * @return the CEESIt filter check box
	 */
	private CheckBox getCEESItFilterCheckBox() {
		if (ceesItFilterSelectAllCheckBox == null) {
			final CheckBox selectAllCheckBox = new CheckBox();

			// Adding EventHandler to the CheckBox to select/deselect all
			selectAllCheckBox.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					int count = 0;
					if (isCEESItFilterAllSelected) {
						for (ObservableList<?> row : ceesItTable.getItems()) {
							((SimpleBooleanProperty) row.get(Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX)).set(false);
							isCEESItFilterAllSelected = false;
							renameCEESItOutputPath(count, false);
							if (startCEESItButtonClicked) {
								ceesItEditRowAction(row, Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX);
							}
							count += 1;
						}
					} else {
						for (ObservableList<?> row : ceesItTable.getItems()) {
							((SimpleBooleanProperty) row.get(Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX)).set(true);
							isCEESItFilterAllSelected = true;
							renameCEESItOutputPath(count, true);
							if (startCEESItButtonClicked) {
								ceesItEditRowAction(row, Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX);
							}
							count += 1;
						}
					}
				}
			});

			ceesItFilterSelectAllCheckBox = selectAllCheckBox;
		}
		return ceesItFilterSelectAllCheckBox;
	}

	/**
	 * Gets the CEESIt graph check box.
	 *
	 * @return the CEESIt graph check box
	 */
	private CheckBox getCEESItGraphCheckBox() {
		if (ceesItGraphSelectAllCheckBox == null) {
			final CheckBox selectAllCheckBox = new CheckBox();

			// Adding EventHandler to the CheckBox to select/deselect all
			selectAllCheckBox.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if (isCEESItGraphAllSelected) {
						for (ObservableList<?> row : ceesItTable.getItems()) {
							((SimpleBooleanProperty) row.get(Constants.CEESIT_TABLE_COLUMN_GRAPH_INDEX)).set(false);
							isCEESItGraphAllSelected = false;
						}
					} else {
						for (ObservableList<?> row : ceesItTable.getItems()) {
							((SimpleBooleanProperty) row.get(Constants.CEESIT_TABLE_COLUMN_GRAPH_INDEX)).set(true);
							isCEESItGraphAllSelected = true;
						}
					}
					
					backendController.updateCEESItGraphs();
				}
			});

			ceesItGraphSelectAllCheckBox = selectAllCheckBox;
		}
		return ceesItGraphSelectAllCheckBox;
	}

	/**
	 * Gets the CSV module map.
	 *
	 * @return the CSV module map
	 */
	public Map<String, CSVModule> getCsvModuleMap() {
		return csvModuleMap;
	}

	/**
	 * This builds the default tree table data model and returns it.
	 *
	 * @param loci
	 *            the loci
	 * @return the default tree table data
	 */
	private List<List<List<String>>> getDefaultTreeTableData(Collection<Locus> loci) {
		List<List<List<String>>> data = new ArrayList<>();

		for (Feature feature : Feature.values()) {
			List<List<String>> featureData = new ArrayList<>();
			List<String> category = new ArrayList<String>();
			category.add(feature.toString());
			category.add(feature.getModel().toString());

			for (double param : feature.getDefaultInitialParams()) {
				category.add(Double.toString(param));
			}
			for (int i = 0; i < 4 - feature.getDefaultInitialParams().length; i++) {
				category.add("-");
			}
			category.add("");

			featureData.add(category);

			if (loci != null) {
				for (Locus locus : loci) {
					List<String> testData = Arrays.asList(
							new String[] { locus.getName(), feature.getModel().toString(), "-", "-", "-", "-", "-" });
					featureData.add(testData);
				}
			}

			data.add(featureData);
		}

		return data;
	}

	/**
	 * Gets the filtered CSV module map.
	 *
	 * @return the filtered CSV module map
	 */
	public Map<String, CSVModule> getFilteredCsvModuleMap() {
		return filteredCsvModuleMap;
	}

	/**
	 * Returns a list of the different genotype sample names, and adds the '<Add
	 * New>' entry to the end.
	 *
	 * @return the list of genotype sample names
	 */
	private List<String> getGenotypeSamples() {
		List<String> sampleNames = new ArrayList<String>();
	
		sampleNames.add(Constants.DEFAULT_ADD_NEW);
		sampleNames.add(Constants.DEFAULT_CHOOSE);
		sampleNames.addAll(genotypes.keySet());

		return sampleNames;
	}
	
	/**
	 * POI list should not contain Add New
	 * @return
	 */
	private List<String> getPOISamples() {
		List<String> sampleNames = new ArrayList<String>();
	
		sampleNames.add(Constants.DEFAULT_CHOOSE);
		sampleNames.addAll(genotypes.keySet());

		return sampleNames;
	}
	
	/**
	 * Known Contributors list should not contain Add New or Choose
	 * @return
	 */
	private List<String> getKnownContributorsSamples() {
		List<String> sampleNames = new ArrayList<String>();
	
		//sampleNames.add(Constants.CEESIT_NO_KNOWN_CONTRIBUTORS_ENTRY);
		sampleNames.addAll(genotypes.keySet());

		return sampleNames;
	}

	/**
	 * Gets the NOCIt filter check box.
	 *
	 * @return the NOCIt filter check box
	 */
	private CheckBox getNOCItFilterCheckBox() {
		if (nocItFilterSelectAllCheckBox == null) {
			final CheckBox selectAllCheckBox = new CheckBox();

			// Adding EventHandler to the CheckBox to select/deselect all
			selectAllCheckBox.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					int count = 0;
					if (isNOCItFilterAllSelected) {
						for (ObservableList<?> row : nocItTable.getItems()) {
							((SimpleBooleanProperty) row.get(Constants.NOCIT_TABLE_COLUMN_FILTER_INDEX)).set(false);
							isNOCItFilterAllSelected = false;
							renameNOCItOutputPath(count, false);
							count += 1;
						}
					} else {
						for (ObservableList<?> row : nocItTable.getItems()) {
							((SimpleBooleanProperty) row.get(Constants.NOCIT_TABLE_COLUMN_FILTER_INDEX)).set(true);
							isNOCItFilterAllSelected = true;
							renameNOCItOutputPath(count, true);
							count += 1;
						}
					}
				}
			});

			nocItFilterSelectAllCheckBox = selectAllCheckBox;
		}
		return nocItFilterSelectAllCheckBox;
	}

	/**
	 * Gets the NOCIt graph check box.
	 *
	 * @return the NOCIt graph check box
	 */
	private CheckBox getNOCItGraphCheckBox() {
		if (nocItGraphSelectAllCheckBox == null) {
			final CheckBox selectAllCheckBox = new CheckBox();

			// Adding EventHandler to the CheckBox to select/deselect all
			selectAllCheckBox.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if (isNOCItGraphAllSelected) {
						for (ObservableList<?> row : nocItTable.getItems()) {
							((SimpleBooleanProperty) row.get(Constants.NOCIT_TABLE_COLUMN_GRAPH_INDEX)).set(false);
							isNOCItGraphAllSelected = false;
						}
					} else {
						for (ObservableList<?> row : nocItTable.getItems()) {
							((SimpleBooleanProperty) row.get(Constants.NOCIT_TABLE_COLUMN_GRAPH_INDEX)).set(true);
							isNOCItGraphAllSelected = true;
						}
					}

					backendController.updateNOCItGraphs();
				}
			});

			nocItGraphSelectAllCheckBox = selectAllCheckBox;
		}
		return nocItGraphSelectAllCheckBox;
	}

	/**
	 * Based on
	 * https://github.com/Nekrofage/javafx-demos/blob/master/javafx-demos/src/main/java/com/ezest/javafx/demogallery/tableviews/TableViewCheckBoxColumnDemo.java
	 * Lazy getter for the selectAllCheckBox.
	 *
	 * @return selectAllCheckBox
	 */
	private CheckBox getSelectAllCheckBox() {
		if (selectAllCheckBox == null) {
			final CheckBox selectAllCheckBox = new CheckBox();

			// Adding EventHandler to the CheckBox to select/deselect all in
			// table.
			selectAllCheckBox.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if (isAllSelected) {
						for (ObservableList<?> row : calibrationTable.getItems()) {
							((SimpleBooleanProperty) row.get(2)).set(false);
							isAllSelected = false;
						}
					} else {
						for (ObservableList<?> row : calibrationTable.getItems()) {
							((SimpleBooleanProperty) row.get(2)).set(true);
							isAllSelected = true;
						}
					}
					showSaveChangesPrompt = true;
				}
			});

			this.selectAllCheckBox = selectAllCheckBox;
		}
		return selectAllCheckBox;
	}
	
	public Stage getStage() {
		return stage;
	}

	/**
	 * This is called when File ->Import Genotypes is clicked on the top menu
	 * bar.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void importGenotypes(ActionEvent event) {
		if (calibrationCalculated) {
			ButtonType[] results = { ButtonType.YES, ButtonType.NO };
			Alert alert = new Alert(AlertType.WARNING, Constants.LOAD_GENOTYPES_AFTER_CALCULATION_WARNING_QUESTION,
					results);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.YES) {
				resetTreeTable();
			} else {
				return;
			}
		}

		if (!fileChooserOpen) {
			fileChooserOpen = true;
			String lastGenotypeFilePath = Settings.lastGenotypeFilePath;
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(Constants.SELECT_FILE);
			setFileChooserInitialDirectory(fileChooser, lastGenotypeFilePath);
			fileChooser.getExtensionFilters()
					.add(new ExtensionFilter(Constants.GENO_FILE_EXTENSION_1, Constants.GENO_FILE_EXTENSION_2));
			fileChooser.getExtensionFilters().add(allFilesFilter);
			File file = fileChooser.showOpenDialog(stage);
			fileChooserOpen = false;
			if (file != null) {
				String importGenotypeFilePath = file.getAbsolutePath();
				try {
					genotypes = FileReaders.createGenotypesMap(importGenotypeFilePath);
					if (genotypes.size() == 0) {
						loadGenotypeErrorAction(Constants.VALIDATE_GENOTYPE_FILE_IMPORT_ERROR_MESSAGE);
						return;
					}

					saveGenotypesMenu.setDisable(false);
					showSaveChangesPrompt = true;
					calibrationCalculated = false;
					resetTreeTable();

					updateCalibrationGenotypeMatching();
					// populates combo boxes after loading genotype
					updateGenotypeOptions();

					// System.out.println(genLoci);
					// System.out.println(backendController.getCalibration().getKit().getLoci());

					// path chosen by user written to settings
					Settings.lastGenotypeFilePath = file.getParent();
					Settings.save();
				} catch (Exception e) {
					loadGenotypeErrorAction(Constants.VALIDATE_GENOTYPE_FILE_IMPORT_ERROR_MESSAGE);
					return;
				}
			}
		}
	}
	
	private void loadGenotypeErrorAction(String message) {
		genotypes.clear();
		saveGenotypesMenu.setDisable(true);
		Alert alert = new Alert(AlertType.ERROR, message);
		alert.showAndWait();
	}

	/**
	 * Initializes the controller class.
	 *
	 * @param url
	 *            the url
	 * @param resourceBundle
	 *            the resource bundle
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		if (!initialized) {
			// Initialize the BackendController object.
			this.backendController = new BackendController(this, nocItProgressBar, ceesItProgressBar);
			// Load initial stuff
			Settings.load();
			// load "saved" populations from file in Settings directory
			loadSavedPopulationsFile();

			// Buid the two table UIs
			buildCalibrationTable();
			buildNOCItTable();
			buildCEESItTable(); // CEESIt
			
			// Based on https://stackoverflow.com/questions/31069300/how-to-fire-event-when-scrolling-up-javafx
			// When CEESIt table is loaded, a scroll bar change listener is added. This fixes the bug
			// where the Known Contributors cells were updated when scrolling incorrectly, that is
			// based on view row as opposed to data row. Refreshing the table when hitting Start
			// fixed the incorrect entries. Scroll bar change refreshes the table. Scrolling is
			// slightly slower in the CEESIt table than the NOCIt table because of this but the
			// difference is not very noticeable.
			ceesItTable.getItems().addListener( (ListChangeListener) c -> {    
				// Check if scroll bar is visible on the table
				// And if yes, move the arrow images to not be over the scroll bar
				Double lScrollBarWidth = null;
				Set<Node> nodes = ceesItTable.lookupAll( ".scroll-bar" );
				for ( final Node node : nodes )
				{
					if ( node instanceof ScrollBar )
					{
						ScrollBar sb = (ScrollBar) node;
						if ( sb.getOrientation() == Orientation.VERTICAL )
						{
							sb.valueProperty().addListener(new ChangeListener() {

								@Override
								public void changed(ObservableValue observable, Object oldValue, Object newValue) {
									ceesItTable.refresh();
								}
								
							});
						}
					}
				}
			});
			
			// Initialize the UI tooltips
			this.initializeToolTips();

			saveCalibrationMenu.setDisable(true);
			importGenotypesMenu.setDisable(false);
			saveGenotypesMenu.setDisable(true);
			populationTypeMenu.setDisable(false);
			viewGenotypesMenu.setDisable(false);
			getSelectAllCheckBox().setDisable(true);

			tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
				SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
				if (selectionModel.getSelectedItem().equals(tabNOCIt)) {
					// newCalibrationMenu.setDisable(true);
					// loadCalibrationMenu.setDisable(true);
					saveCalibrationMenu.setDisable(true);
					populationTypeMenu.setDisable(false);
				} else if (selectionModel.getSelectedItem().equals(tabCEESIt)) {
					// newCalibrationMenu.setDisable(true);
					// loadCalibrationMenu.setDisable(true);
					saveCalibrationMenu.setDisable(true);
					populationTypeMenu.setDisable(false);
				} else if (selectionModel.getSelectedItem().equals(tabCalibrate)) {
					newCalibrationMenu.setDisable(false);
					loadCalibrationMenu.setDisable(false);
					populationTypeMenu.setDisable(true);
					if (calibrationCalculated || kitSelected) {
						saveCalibrationMenu.setDisable(false);
					} else {
						saveCalibrationMenu.setDisable(true);
					}
				}
			});
			
			// add tooltips
			startNOCItButton.setTooltip(new Tooltip(Constants.START_NOCIt_BUTTON_TOOLTIP));
			cancelNOCItButton.setTooltip(new Tooltip(Constants.CANCEL_NOCIt_BUTTON_TOOLTIP));
			nocitPlusButton.setTooltip(new Tooltip(Constants.NOCIt_PLUS_BUTTON_TOOLTIP));
			nocitMinusButton.setTooltip(new Tooltip(Constants.NOCIt_MINUS_BUTTON_TOOLTIP));
			
			nocItTimeElapsedTimer = new TimeElapsed(this, nocItTimeElapsed);
			ceesItTimeElapsedTimer = new TimeElapsed(this, ceesItTimeElapsed);

			// Initialize the Autosaver that will automatically save progress on
			// a fixed time interval
			if (Constants.AUTOSAVE_ON) {
				// Initialize the Autosaver that will automatically save
				// progress on a fixed time interval
				autoSaver = new AutoSaver(this);
			}

			initialized = true;
		}
	}

	/**
	 * Initialize tool tips.
	 */
	public void initializeToolTips() {
		Tooltip tooltip;
		double fontSize = Constants.TOOLTIP_FONT_SIZE;

		tooltip = new Tooltip();
		tooltip.setFont(Font.font(fontSize));
		tooltip.setText("This name will be used to distinguish your calibration run.");
		calibrationName.setTooltip(tooltip);
	}

	/**
	 * Checks if calibration has been calculated.
	 *
	 * @return true, if calibration has been calculated
	 */
	public boolean isCalibrationCalculated() {
		return calibrationCalculated;
	}

	/**
	 * Load calibration project.
	 */
	@FXML
	public void loadCalibration() {
		autosaveLoaded = false;
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(Constants.SELECT_FILE);
		// get last save path from settings
		String lastCalibrationPath = Settings.lastCalibrationPath;
		setFileChooserInitialDirectory(fileChooser, lastCalibrationPath);
		// Set extension filters
		fileChooser.getExtensionFilters().add(calibrationFilter);
		fileChooser.getExtensionFilters().add(allFilesFilter);
		// fileChooser.getExtensionFilters().add(new
		// ExtensionFilter(Constants.PROGRESS_FILE_EXTENSION_1,
		// Constants.PROGRESS_FILE_EXTENSION_2));
		File file = fileChooser.showOpenDialog(stage);

		if (file != null) {
			if (file.getAbsolutePath().endsWith(Constants.CALIBRATION_FILE_EXTENSION)) {				
				SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
				selectionModel.select(tabCalibrate);
				boolean valid = false;
				try {
					CalibrationProjectHandler.loadCalibrationProject(file, this);
					if (backendController.getCalibration() != null) {
						valid = true;
					}
				} catch (Exception e) {
					Alert alert = new Alert(AlertType.ERROR, Constants.INVALID_CALIBRATION_FILE_ERROR_MESSAGE);
					alert.showAndWait();
					logger.error(Constants.INVALID_CALIBRATION_FILE_ERROR_MESSAGE, e);
					resetCalibrationTab();
					return;
				}
				if (valid) {
					Settings.lastCalibrationPath = file.getParent();
					Settings.save();
					calibrationNameLabel.setText("Calibration: " + calibrationName.getText());

					isAllSelected = CalibrationProjectHandler.isAllSelected();
					getSelectAllCheckBox().setSelected(isAllSelected);

					// calibrationCalculated =
					// CalibrationProjectHandler.isCalibrationCalculated();
					// enable buttons based on if calibration was calculated
					calculateCalibration.setDisable(calibrationCalculated);
					resetCalibration.setDisable(!calibrationCalculated);
					// if not calculated put gui on first page where Next button can
					// check if
					// all genotypes have been matched
					if (!calibrationCalculated) {
						calibrationPage1.setVisible(true);
						calibrationPage2.setVisible(false);
					}

					// Leave items in same state that it would be in if sample files
					// loaded.
					// Before this state, there is nothing to save and Save Calibration
					// menu
					// item is disabled.
					kitName.setText(backendController.getCalibration().getKit().getKitName());
					kitName.setDisable(true);
					sampleFolderPath.setDisable(true);
					saveCalibrationMenu.setDisable(false);
					if (genotypes.size() == 0) {
						saveGenotypesMenu.setDisable(true);
					} else {
						saveGenotypesMenu.setDisable(false);
					}
					getSelectAllCheckBox().setDisable(false);
					browseSampleFiles.setDisable(false);
					kitSelected = true;
					updateGenotypeOptions();
					showSaveChangesPrompt = false;
					lineChartAnchor.getChildren().clear();	
				} else {
					resetCalibrationTab();
					Alert alert = new Alert(AlertType.ERROR, Constants.INVALID_CALIBRATION_FILE_ERROR_MESSAGE);
					alert.showAndWait();
					return;
				}
			} else {
				Alert alert = new Alert(AlertType.ERROR, Constants.INVALID_CALIBRATION_FILE_TYPE_ERROR_MESSAGE);
				alert.showAndWait();
				return;
			}
		}
	}

	/**
	 * Loads saved populations file.
	 */
	private void loadSavedPopulationsFile() {
		File f = new File(Settings.getSettingsPath() + File.separatorChar + "Frequencies.csv");
		if (f.exists()) {
			CSVReader reader;

			try {
				reader = new CSVReader(new FileReader(f), ',');
				String[] dataArray;
				int count = 0;
				try {
					while ((dataArray = reader.readNext()) != null) {
						if (count > 0) {
							String name = dataArray[0];
							String path = dataArray[1];
							String num = dataArray[2];
							try {
								int numPeople = Integer.parseInt(num);
								File popFile = new File(path);
								if (popFile.exists()) {
									FreqTable freq = new FreqTable(path, name, numPeople);
									populationNamesList.add(name);
									populations.put(name, freq);
								}
							} catch (Exception e) {
								Alert alert = new Alert(AlertType.ERROR, "Unable to Create Saved Population");
								alert.showAndWait();
								// e.printStackTrace();
								logger.error("Unable to Create Saved Population", e);
							}
						}
						count += 1;
					}
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Match genotype IDs to sample IDs.
	 *
	 * @param csvModule
	 *            the CSV module containing the samples and their IDs
	 * @return map of sample IDs and their corresponding genotype ID matches
	 */
	private HashMap<String, String> matchGenotypeIDs(CSVModule csvModule) {
		HashMap<String, String> genotypeIDs = null;
		try {
			genotypeIDs = csvModule.matchGenotypes(genotypes);
		} catch (Exception e) {
			// If it is ever desired to show an error message if split character
			// is out of range, uncomment the commented out lines. An error
			// message is written to logs to help the user determine why no
			// genotypes are matched if an exception is thrown due to an out of
			// range error.

			// Alert alert = new Alert(AlertType.ERROR,
			// Constants.LOAD_SAMPLE_FILES_MATCH_GENOTYPE_MESSAGE);
			// alert.showAndWait();
			logger.error(Constants.LOAD_SAMPLE_FILES_MATCH_GENOTYPE_MESSAGE, e);
			// Samples should load even if genotype id cannot be matched
			// sampleFolderPath.setText("");
			// sampleFolderPath.setDisable(false);
		}
		return genotypeIDs;
	}

	/**
	 * Called when File -> New Calibration is selected.
	 */
	@FXML
	public void newCalibration() {
		resetCalibrationTab();
	}
	
	private void resetCalibrationTab() {
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		selectionModel.select(tabCalibrate);
		autosaveLoaded = false;
		calibrationName.setText("");
		calibrationName.setDisable(false);
		kitName.setDisable(false);
		kitName.setText("");
		browseSampleFiles.setDisable(false);
		sampleFolderPath.setText("");
		sampleFolderPath.setDisable(false);
		buildCalibrationTable();
		calibrationNameLabel.setText("Calibration: " + "");
		treeTableView.getColumns().clear();
		treeTableView.setRoot(null);

		calibrationPage1.setVisible(true);
		calibrationPage2.setVisible(false);

		calculateCalibration.setDisable(false);
		resetCalibration.setDisable(true);
		saveCalibrationMenu.setDisable(true);
		saveGenotypesMenu.setDisable(true);
		genotypes.clear();
		getSelectAllCheckBox().setDisable(true);
		resetTreeTable();
		calibrationCalculated = false;
		showSaveChangesPrompt = false;
	}

	/**
	 * This is called when the 'Next' button on the first Calibrate tab is
	 * clicked. This performs error checking and then advances the UI to the
	 * next step.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void nextClicked(ActionEvent event) {
		// Check table to make sure each DNA Mass has a value.
		// Check table to make sure each Known Genotype has a value.
		for (ObservableList<?> row : calibrationTable.getItems()) {
			if (((SimpleStringProperty) row.get(4)).getValue().equals(Constants.KNOWN_GENOTYPE_DEFAULT_VALUE)) {
				Alert alert = new Alert(AlertType.ERROR, Constants.NEXT_CLICKED_ERROR_KNOWN_GENOTYPE_MESSAGE);
				alert.showAndWait();
				return;
			} else if (((SimpleStringProperty) row.get(3)).getValue().isEmpty()) {
				Alert alert = new Alert(AlertType.ERROR, Constants.NEXT_CLICKED_ERROR_DNA_MASS_MESSAGE);
				alert.showAndWait();
				return;
			}
		}

		// Check to make sure a Calibration Name has been entered.
		if (calibrationName.getText().equals("")) {
			Alert alert = new Alert(AlertType.ERROR, Constants.NEXT_CLICKED_ERROR_CALIBRATION_MESSAGE);
			alert.showAndWait();
			calibrationName.requestFocus();
			return;
		}
		if (sampleFolderPath.getText().equals("")) {
			Alert alert = new Alert(AlertType.ERROR, Constants.NEXT_CLICKED_ERROR_SAMPLE_FOLDER_MESSAGE);
			alert.showAndWait();
			browseSampleFiles.requestFocus();
			return;
		}

		// Advance the UI
		calibrationPage1.setVisible(false);
		calibrationPage2.setVisible(true);

		calibrationNameLabel.setText("Calibration: " + calibrationName.getText());

		if (treeTableView.getRoot() == null) {
			// Build the Tree Table with these values.
			buildTreeTable(getDefaultTreeTableData(backendController.getCalibration().getLoci()));
		}

		// If calibration has not been calculated calculation button enabled
		// otherwise reset button enabled. This allows user to go back and forth
		// between the two screens after calculation
		if (!calibrationCalculated) {
			calculateCalibration.setDisable(false);
			resetCalibration.setDisable(true);
		} else {
			calculateCalibration.setDisable(true);
			resetCalibration.setDisable(false);
		}
	}

	/**
	 * Populates loci in edit genotype table with kit loci.
	 *
	 * @param table
	 *            the table
	 */
	private void populateGenotypeTableWithLoci(TableView<ObservableList<?>> table) {
		// Populate Table with Loci
		LinkedHashSet<Locus> genotypeLociSet = new LinkedHashSet<Locus>();		
		if (backendController.getCalibration() != null) 
			genotypeLociSet.addAll(backendController.getCalibration().getLoci());		
		for (String genotypeId : genotypes.keySet()) 
			genotypeLociSet.addAll(genotypes.get(genotypeId).getLoci());
		
		ArrayList<Locus> genotypeLociList = new ArrayList<Locus>(genotypeLociSet);

		// populate table with loci from kit
		for (int i = 0; i < genotypeLociList.size(); i++) {
			table.getItems()
					.addAll(FXCollections.observableArrayList(Arrays
							.asList(new SimpleStringProperty[] { new SimpleStringProperty(genotypeLociList.get(i).toString()),
									new SimpleStringProperty(""), new SimpleStringProperty("") })));
		}
		
		setGenotypeFieldCellFactoryAMELIndex(genotypeLociList);
	}

	/**
	 * * Populates loci in edit genotype table with data from given genotype.
	 *
	 * @param table
	 *            the table
	 * @param genotype
	 *            the genotype
	 */
	private void populateGenotypeTableWithGenotype(TableView<ObservableList<?>> table, Genotype genotype) {
		ArrayList<Locus> genotypeLociList = new ArrayList<Locus>(genotype.getLoci());

		for (int i = 0; i < genotypeLociList.size(); i++) {
			Allele allele1 = genotype.getAlleles(genotypeLociList.get(i))[0];
			Allele allele2 = genotype.getAlleles(genotypeLociList.get(i))[1];

			table.getItems().addAll(FXCollections.observableArrayList(Arrays.asList(new SimpleStringProperty[] {
					new SimpleStringProperty(genotypeLociList.get(i).getName()), new SimpleStringProperty(allele1.toString()),
					new SimpleStringProperty(allele2.toString()) })));
		}
		
		setGenotypeFieldCellFactoryAMELIndex(genotypeLociList);
	}

	/**
	 * Reloads tree table and sets post conditions. When this code is run in
	 * calculateParametersTask, the following error is encountered: Exception in
	 * thread "Thread-4" java.lang.IllegalStateException: Not on FX application
	 * thread; currentThread = Thread-4 but if this code is run after task has
	 * succeeded, there are no errors.
	 */
	private void reloadTreeTableAfterCalculation() {
		buildTreeTable(treeData);
		calculateCalibration.setDisable(true);
		resetCalibration.setDisable(false);
		calibrationCalculated = true;
		treeTableView.setEditable(false);
	}

	/**
	 * Removes the CEESIt table rows.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void removeCEESItTableRows(ActionEvent event) {
		removeTableRows(ceesItTable);
		backendController.updateCEESItGraphs();
		if (startCEESItButtonClicked) {
			clearCEESItFileLists();
			ceesItTable.refresh();
		}
	}

	/**
	 * Removes the NOCIt table rows.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void removeNOCItTableRows(ActionEvent event) {
		removeTableRows(nocItTable);
		backendController.updateNOCItGraphs();
		if (startNOCItButtonClicked) {
			clearNOCItFileLists();
			nocItTable.refresh();
		}
	}
	
	private void removeTableRows(TableView<ObservableList<?>> table) {
		// Fixes bug where selected rows were not being deleted correctly
		// http://stackoverflow.com/questions/18700430/issue-with-removing-multiple-rows-at-once-from-javafx-tableview
		table.getItems().removeAll(table.getSelectionModel().getSelectedItems());
		table.refresh();
	}

	/**
	 * Rename NOCIt output path.
	 *
	 * @param rowIndex
	 *            the row index
	 * @param filtered
	 *            the filtered
	 */
	private void renameNOCItOutputPath(int rowIndex, boolean filtered) {
		ObservableList<?> rowList = (ObservableList<?>) nocItTable.getItems().get(rowIndex);
		String outputName = ((SimpleStringProperty) rowList.get(Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
		if (!userSelectedNOCItOutputFilePaths.contains(outputName)) {
			UtilityMethods.updateNOCItOutputName(nocItTable, rowList, rowIndex);
			if (startNOCItButtonClicked) {
				clearNOCItFileLists();
				nocItTable.refresh();
			}
		}
	}

	/**
	 * Rename CEESIt output path.
	 *
	 * @param rowIndex
	 *            the row index
	 * @param filtered
	 *            the filtered
	 */
	private void renameCEESItOutputPath(int rowIndex, boolean filtered) {
		ObservableList<?> rowList = (ObservableList<?>) ceesItTable.getItems().get(rowIndex);
		String outputName = ((SimpleStringProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
		if (!userSelectedCEESItOutputFilePaths.contains(outputName)) {
			UtilityMethods.updateCEESItOutputName(ceesItTable, rowList, rowIndex);
			if (startCEESItButtonClicked) {
				ceesItDuplicateOutputRowList.clear(); 
    			ceesItExistingFileRowList.clear(); 
    			ceesItTable.refresh();
			}
		}
	}
	
	/**
	 * This is called when the Reset button on the second Calibrate page is
	 * clicked. This will reset the tree table parameters to their default
	 * values.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void resetParameters(ActionEvent event) {
		resetTreeTable();
		showSaveChangesPrompt = true;
	}

	/**
	 * Reset the tree table parameters to default values.
	 */
	public void resetTreeTable() {
		treeTableView.getColumns().clear();
		treeTableView.setRoot(null);

		if (backendController.getCalibration() != null && backendController.getCalibration().getLoci() != null) {
			buildTreeTable(getDefaultTreeTableData(backendController.getCalibration().getLoci()));
		}
		resetCalibration.setDisable(true);
		calculateCalibration.setDisable(false);

		lineChartAnchor.getChildren().clear();
		calibrationCalculated = false;
		treeTableView.setEditable(true);
	}

	/**
	 * This method is called when File -> Save Calibration is selected.
	 */
	@FXML
	public void saveCalibration() {
		autosaveLoaded = false;
		if (calibrationTable.getItems().size() == 0) {
			Alert alert = new Alert(AlertType.ERROR, Constants.EMPTY_CALIBRATION_VALUES_TABLE_SAVE_MESSAGE);
			alert.showAndWait();
		} else {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(Constants.SELECT_FILE);
			// get last save path from settings
			String lastCalibrationPath = Settings.lastCalibrationPath;
			new File(lastCalibrationPath);
			setFileChooserInitialDirectory(fileChooser, lastCalibrationPath);
			// set file name from calibration name field if directory exists and
			// field is not empty
			setFileChooserFileName(fileChooser, lastCalibrationPath, calibrationName.getText());
			// Set extension filters
			fileChooser.getExtensionFilters().add(calibrationFilter);
			fileChooser.getExtensionFilters().add(allFilesFilter);
			// fileChooser.getExtensionFilters().add(new
			// ExtensionFilter(Constants.PROGRESS_FILE_EXTENSION_1,
			// Constants.PROGRESS_FILE_EXTENSION_2));
			File file = fileChooser.showSaveDialog(stage);

			if (file != null) {
				CalibrationProjectHandler.saveCalibrationProject(file, this);
				Settings.lastCalibrationPath = file.getParent();
				Settings.save();
				calibrationName.setText(file.getName().substring(0, file.getName().lastIndexOf(".")));
				calibrationName.selectAll();
				if (calibrationNameLabel.isVisible()) {
					calibrationNameLabel.setText("Calibration: " + calibrationName.getText());
				}
				showSaveChangesPrompt = false;
			}
		}
	}

	/**
	 * Check edit genotypes table for errors and omissions, such as empty cells.
	 * If entries are valid, update hashmap containing alleles for each locus.
	 *
	 * @param table
	 *            the table
	 * @param hashMap
	 *            the hashmap containing alleles for each locus
	 * @return true, if successful
	 */
	private boolean saveGenotypeFromTable(TableView<ObservableList<?>> table, HashMap<Locus, Allele[]> hashMap) {
		for (ObservableList<?> row : table.getItems()) {
			Locus locus = new Locus(((SimpleStringProperty) row.get(0)).getValue());
			String allele1 = ((SimpleStringProperty) row.get(1)).getValue();
			String allele2 = ((SimpleStringProperty) row.get(2)).getValue();
			if (!allele1.trim().equals("") || !allele2.trim().equals("")) {
				Allele[] alleles = new Allele[2];
				if (locus.isAMEL()) {
					try {
						alleles[0] = new AMELAllele(allele1.toUpperCase());
					} catch (Exception e) {
						Alert alert = new Alert(AlertType.ERROR, Constants.GENOTYPE_POPUP_ERROR_AMEL_ALLELE_CHECK);
						alert.showAndWait();
						return false;
					}
					try {
						alleles[1] = new AMELAllele(allele2.toUpperCase());
					} catch (Exception e) {
						Alert alert = new Alert(AlertType.ERROR, Constants.GENOTYPE_POPUP_ERROR_AMEL_ALLELE_CHECK);
						alert.showAndWait();
						return false;
					}
				} else {
					try {
						alleles[0] = new STRAllele(allele1);
					} catch (Exception e) {
						Alert alert = new Alert(AlertType.ERROR,
								Constants.GENOTYPE_POPUP_ERROR_STR_ALLELE_CHECK + " " + locus);
						alert.showAndWait();
						return false;
					}
					try {
						alleles[1] = new STRAllele(allele2);
					} catch (Exception e) {
						Alert alert = new Alert(AlertType.ERROR,
								Constants.GENOTYPE_POPUP_ERROR_STR_ALLELE_CHECK + " " + locus);
						alert.showAndWait();
						return false;
					}
				}

				hashMap.put(locus, alleles);
			}
		}

		return true;
	}

	/**
	 * This is called when Edit > Save Genotypes is clicked on the top menu bar.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void saveGenotypes(ActionEvent event) {
		if (!fileChooserOpen) {
			fileChooserOpen = true;
			String lastGenotypeFilePath = Settings.lastGenotypeFilePath;
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(Constants.SELECT_FILE);
			setFileChooserInitialDirectory(fileChooser, lastGenotypeFilePath);
			fileChooser.getExtensionFilters()
					.add(new ExtensionFilter(Constants.GENO_FILE_EXTENSION_1, Constants.GENO_FILE_EXTENSION_2));
			fileChooser.getExtensionFilters().add(allFilesFilter);
			File file = fileChooser.showSaveDialog(stage);
			fileChooserOpen = false;
			if (file != null) {
				String path = file.getAbsolutePath();
				if (path.endsWith(Constants.GENO_FILE_EXTENSION_2)) {
					path += Constants.GENO_FILE_EXTENSION_2;
				}
				writeGenotypesFile(path);
				// path chosen by user written to settings
				Settings.lastGenotypeFilePath = file.getParent();
				Settings.save();
			}
		}

	}

	/**
	 * Sets the auto saver.
	 *
	 * @param autoSaver
	 *            the new auto saver
	 */
	public void setAutoSaver(AutoSaver autoSaver) {
		this.autoSaver = autoSaver;
	}

	/**
	 * Sets the calibration calculated.
	 *
	 * @param calibrationCalculated
	 *            the new calibration calculated
	 */
	public void setCalibrationCalculated(boolean calibrationCalculated) {
		this.calibrationCalculated = calibrationCalculated;
	}

	/**
	 * Sets directory in directory chooser if directory exists, otherwise sets
	 * to default.
	 *
	 * @param directoryChooser
	 *            the directory chooser
	 * @param directory
	 *            the directory
	 */
	public void setDirectoryChooserInitialDirectory(DirectoryChooser directoryChooser, String directory) {
		File f = new File(directory);
		if (f.exists() && f.isDirectory()) {
			directoryChooser.setInitialDirectory(new File(directory));
		} else {
			directoryChooser.setInitialDirectory(new File(Settings.defaultDirectory));
		}
	}

	/**
	 * Sets file name in file chooser if file exists.
	 *
	 * @param fileChooser
	 *            the file chooser
	 * @param directory
	 *            the directory
	 * @param name
	 *            the name
	 */
	private void setFileChooserFileName(FileChooser fileChooser, String directory, String name) {
		File f = new File(directory);
		if (f.exists()) {
			if (name != null && name.length() > 0) {
				fileChooser.setInitialFileName(name);
			}
		}
	}

	/**
	 * Sets directory in file chooser if directory exists, otherwise sets to
	 * default.
	 *
	 * @param fileChooser
	 *            the file chooser
	 * @param directory
	 *            the directory
	 */
	public void setFileChooserInitialDirectory(FileChooser fileChooser, String directory) {

		File f = (directory == null) ? null : new File(directory);
		if (f != null && f.exists()) {
			fileChooser.setInitialDirectory(new File(directory));
		} else {
			fileChooser.setInitialDirectory(new File(Settings.defaultDirectory));
		}
	}

	/**
	 * Sets GenotypeFieldCellFactory AMEL Index from Locus list if Locus list
	 * contains AMEL locus, else sets AMEL Index to -1.
	 *
	 * @param genLoci
	 *            the new genotype field cell factory AMEL index
	 */
	private void setGenotypeFieldCellFactoryAMELIndex(ArrayList<Locus> genLoci) {
		boolean contains = false;
		for (int i = 0; i < genLoci.size(); i++) {
			if (genLoci.get(i).isAMEL()) {
				GenotypeFieldCellFactory.setAmelIndex(i);
				contains = true;
			}
		}
		if (!contains) {
			GenotypeFieldCellFactory.setAmelIndex(-1);
		}
	}

	/**
	 * Store the Stage object in this controller so we can reference it for the
	 * popup windows.
	 *
	 * @param stage
	 *            the new stage
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Shows the interface for adding/viewing/editing a genotype.
	 *
	 * @param property
	 *            the property
	 * @param oldGenotype
	 *            the old genotype (i.e. the one the editor should be loaded
	 *            with)
	 */
	private void showGenotypeEditor(SimpleStringProperty property, Genotype oldGenotype) {
		// Popup
		final TextField genotypeIDField = new TextField();
		genotypeIDField.setMaxWidth(90);
		// selects all text when focused
		genotypeIDField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						genotypeIDField.selectAll();
					}
				});
			}
		});
		AnchorPane.setRightAnchor(genotypeIDField, 110.0);
		AnchorPane.setTopAnchor(genotypeIDField, 5.0);

		final TableView<ObservableList<?>> table = createGenotypeTable();

		if (oldGenotype != null) {
			populateGenotypeTableWithGenotype(table, oldGenotype);
			genotypeIDField.setText(oldGenotype.getGenotypeID());
		} else
			populateGenotypeTableWithLoci(table);

		// Table Context Menu
		MenuItem menuPaste = genotypesTablePasteItem(table);
		MenuItem menuCopy = genotypesTableCopyItem(table);
		table.setContextMenu(new ContextMenu(menuCopy, menuPaste));

		// Button Handler
		Button save = new Button(Constants.GENOTYPE_POPUP_BUTTON_SAVE_CLOSE);
		save.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (genotypeIDField.getText().isEmpty()) {
					Alert alert = new Alert(AlertType.ERROR, Constants.GENOTYPE_POPUP_ERROR_NAME_CHECK);
					alert.showAndWait();
					return;
				}

				String genotypeID = genotypeIDField.getText().trim();
				if (genotypes.containsKey(genotypeID) && (oldGenotype == null)) {
					Alert alert = new Alert(AlertType.ERROR,
							"Genotype already present. Please enter a different name.");
					alert.showAndWait();
					return;
				}

				LinkedHashMap<Locus, Allele[]> hashMap = new LinkedHashMap<Locus, Allele[]>();
				boolean validSaveGenotypes = saveGenotypeFromTable(table, hashMap);
				if (validSaveGenotypes) {
					if (oldGenotype == null) {
						Genotype genotype = new Genotype(genotypeID);
						genotype.setLocusValues(hashMap);

						genotypes.put(genotypeID, genotype);
					} else {
						if (!oldGenotype.getGenotypeID().equals(genotypeID)) {
							List<String> previousValues = new ArrayList<>();
							for (ObservableList<?> calibrationRow : calibrationTable.getItems()) {
								String value = ((SimpleStringProperty) calibrationRow
										.get(Constants.CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE_INDEX)).get();
								previousValues.add(value);
							}

							if (previousValues.contains(oldGenotype.getGenotypeID())) {
								ButtonType[] results = { ButtonType.YES, ButtonType.NO };
								Alert alert = new Alert(AlertType.WARNING,
										Constants.USED_GENOTYPE_EDIT_WARNING_QUESTION, results);
								Optional<ButtonType> result = alert.showAndWait();
								if (result.isPresent() && result.get() == ButtonType.YES) {
									for (int j = 0; j < previousValues.size(); j++) {
										if (previousValues.get(j).equals(oldGenotype.getGenotypeID())) {
											previousValues.set(j, genotypeIDField.getText());
										}
									}
									deleteGenotypeSample(oldGenotype);

									int i = 0;
									for (ObservableList<?> calibrationRow : calibrationTable.getItems()) {
										((SimpleStringProperty) calibrationRow
												.get(Constants.CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE_INDEX))
														.set(previousValues.get(i));
										i++;
									}
								} else {
									return;
								}
							}

							Genotype genotype = new Genotype(genotypeID);
							genotype.setLocusValues(hashMap);

							genotypes.put(genotypeID, genotype);
							genotypes.remove(oldGenotype.getGenotypeID());
						} else {
							genotypes.get(genotypeID).setLocusValues(hashMap);
						}
					}

					addNewGenotypeStage.close();
					if (viewGenotypesStage.isShowing()) {
						buildViewGenotypesTable();
					}
					
					// update calibration table cell when new Known Genotype is
					// added and update is not in Calibration genotypes table
					if (property != null)
						property.set(genotypeID);

					updateGenotypeOptions();
					if (genotypes.size() == 0) {
						saveGenotypesMenu.setDisable(true);
					} else {
						saveGenotypesMenu.setDisable(false);
					}
				}
			}
		});
		StackPane.setMargin(save, new Insets(0.0, 100.0, 0.0, 0.0));

		Button cancel = new Button(Constants.GENOTYPE_POPUP_BUTTON_CANCEL);
		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				addNewGenotypeStage.close();
			}
		});
		StackPane.setMargin(cancel, new Insets(0.0, 0.0, 0.0, 100.0));

		// Build the Popup control
		SplitPane pane = buildGenotypeSplitPane(table, genotypeIDField, save, cancel);
		Scene scene = new Scene(pane);

		addNewGenotypeStage = new Stage();
		
		if (oldGenotype == null)
			addNewGenotypeStage.setTitle(Constants.GENOTYPE_POPUP_ADD_TITLE);
		else
			addNewGenotypeStage.setTitle(Constants.GENOTYPE_POPUP_EDIT_TITLE);
		
		addNewGenotypeStage.initModality(Modality.WINDOW_MODAL);
		addNewGenotypeStage.setScene(scene);
		addNewGenotypeStage.setMinWidth(350);
		addNewGenotypeStage.setMaxWidth(350);
		addNewGenotypeStage.setMaxHeight(400);
		addNewGenotypeStage.setMinHeight(400);
		addNewGenotypeStage.getIcons().add(new Image(getClass().getResourceAsStream("dna2.png")));
		if (viewGenotypesStage.isShowing()) {
			addNewGenotypeStage.initOwner(viewGenotypesStage);
		} else {
			addNewGenotypeStage.initOwner(stage);
		}

		addNewGenotypeStage.show();
	}

	/**
	 * This will open the "Logs" folder in Windows Explorer, or the native file
	 * explorer when running on Linux or OS X.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	public void showLogs(ActionEvent event) {
		try {
			File file = new File(Settings.getSettingsPath() + "/logs/");
			Desktop desktop = Desktop.getDesktop();
			desktop.open(file);
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error(Constants.OPEN_LOGS_ERROR_LOG_MESSAGE, e);
		}
	}

	/**
	 * Show population dialog.
	 */
	@FXML
	private void showPopulationDialog() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("FrequencyManager.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);

			nocitStage = new Stage();
			nocitStage.setTitle("Population Manager");
			nocitStage.initModality(Modality.WINDOW_MODAL);
			nocitStage.setScene(scene);
			nocitStage.setResizable(false);
			nocitStage.getIcons().add(new Image(getClass().getResourceAsStream("dna2.png")));
			nocitStage.initOwner(stage);
			nocitStage.show();

			final PopulationManagerController controller = loader.getController();
			controller.setStage(nocitStage, this);
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error(Constants.ERROR_LOADING_POPULATION_FREQUENCIES, e);
		}
	}

	/**
	 * Show version.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void showVersion(ActionEvent event) {
//		String ver = UIMain.class.getPackage().getImplementationVersion();
		String ver = Constants.APPLICATION_VERSION;
		
		String content = Constants.APPLICATION_NAME + "\n\n" 
				+ "Version: " + ver + "\n\n"
				+ "Developed and Written with Contributions by:\n"
				+ "Desmond Lun, Catherine Grgicak, Anurag Arnold, Lauren Alfonse, Robert Carpenter,\n"
				+ "Abhishek Garg, Marc Jackson, Slim Karkar, James Kelley, Anatoliy Lane, Amanda Garrett,\n"
				+ "Shay Maor, Muriel Medard, Mark Moore, Jigar Patel, Harish Swaminathan,\n"
				+ "Xia Yearwood-Garcia\n\n" 				
				+ "For more information, see http://lftdi.com";

		final int xSize = 550;
		final int ySize = 300;

		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setPrefSize(xSize, ySize);
		textArea.setWrapText(true);
		textArea.setPadding(new Insets(20));

		textArea.setText(content);

		Button b = new Button("OK");
		b.setAlignment(Pos.BOTTOM_CENTER);

		VBox box = new VBox(1);
		box.getChildren().add(textArea);
		box.getChildren().add(b);
		box.setPrefSize(xSize, ySize);
		box.setAlignment(Pos.CENTER);
		Scene scene = new Scene(box);

		Stage stg = new Stage();

		b.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				stg.close();
			}
		});

		stg.setTitle("About " + Constants.APPLICATION_NAME);
		stg.setScene(scene);
		stg.initStyle(StageStyle.UTILITY);
		stg.initModality(Modality.WINDOW_MODAL);
		stg.initOwner(stage);
		stg.show();
	}

	/**
	 * Check CEESIt table for errors when clicking Start
	 */
	private void runCEESItTableErrorCheck() {
		ceesItTableErrorMessage = "";
		ceesItDuplicateOutputRowList.clear();
		ceesItChooseEntryRowList.clear();
		ceesItNOCErrorRowList.clear();
		ceesItExistingFileRowList.clear();
		outputNamesPopulationCEESItMap.clear();
		String[] knownContributorsArray = null;
		boolean fileExists = false;
		for (ObservableList<?> row : ceesItTable.getItems()) {
			knownContributorsArray = null;
			String sampleFileName = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_SAMPLE_INDEX)).get();
			String popName = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_POPULATION_INDEX)).get();
			String noc = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_NOC_INDEX)).get();
			String calibrationName = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX))
					.get();
			String poiGenotypeID = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_POI_INDEX)).get();
			String knownContributorsEntry = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX)).get();
			if (knownContributorsEntry.length() > 0 && !knownContributorsEntry.equals(Constants.CEESIT_NO_KNOWN_CONTRIBUTORS_ENTRY)) {
				knownContributorsArray = knownContributorsEntry.split(",");
			}
			String output = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
			int rowID = ((SimpleIntegerProperty) row.get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
			
			File f = new File(output);
			if (f.exists()) {
				ceesItExistingFileRowList.add(rowID);
				fileExists = true;
			}

			// if output names are the same and population name already exists
			// with this output name
			if (UtilityMethods.isDuplicateOutput(f, popName, outputNamesPopulationCEESItMap)) {
				if (!ceesItTableErrorMessage.contains(Constants.DUPLICATE_OUTPUT_FILE_ERROR_MESSAGE + "\n\n")) {
					ceesItTableErrorMessage += Constants.DUPLICATE_OUTPUT_FILE_ERROR_MESSAGE + "\n\n";
				}
				ceesItDuplicateOutputRowList.add(rowID);
			} else {
				outputNamesPopulationCEESItMap = UtilityMethods.updateutputNamesPopulationMap(
						outputNamesPopulationCEESItMap, f, popName);
			}
			// Length cannot be obtained for a null array, but the array must be null for ceesIt code
			// to work. Hence a variable for the length is set to 0 for the null array
			int knownContributorsArrayLength = 0;
			if (knownContributorsArray != null) {
				knownContributorsArrayLength = knownContributorsArray.length;
			}
			if (Integer.parseInt(noc) <= knownContributorsArrayLength) {
				if (!ceesItTableErrorMessage.contains(Constants.CEESIT_NOC_ERROR_MESSAGE + "\n\n")) {
					ceesItTableErrorMessage += Constants.CEESIT_NOC_ERROR_MESSAGE + "\n\n";
				}
				ceesItNOCErrorRowList.add(rowID);
			}
			if (sampleFileName.equals(Constants.DEFAULT_CHOOSE) || popName.equals(Constants.DEFAULT_CHOOSE)
					|| noc.equals(Constants.DEFAULT_CHOOSE) || calibrationName.equals(Constants.DEFAULT_CHOOSE)
					|| poiGenotypeID.equals(Constants.DEFAULT_CHOOSE)) {
				if (!ceesItTableErrorMessage.contains(Constants.CALCULATIONS_MISSING_VALUE_ERROR_MESSAGE + "\n\n")) {
					ceesItTableErrorMessage += Constants.CALCULATIONS_MISSING_VALUE_ERROR_MESSAGE + "\n\n";
				}
				ceesItChooseEntryRowList.add(rowID);
			}
		}
		if (fileExists) {
			if (!ceesItTableErrorMessage.contains(Constants.FILE_EXISTS_WARNING_MESSAGE)) {
				ceesItTableErrorMessage += Constants.FILE_EXISTS_WARNING_MESSAGE;
			}
		}
	}
	
	/**
	 * Check NOCIt table for errors when clicking Start
	 */
	private void runNOCItTableErrorCheck() {
		nocItTableErrorMessage = "";
		nocItDuplicateOutputRowList.clear();
		nocItChooseEntryRowList.clear();
		nocItExistingFileRowList.clear();
		outputNamesPopulationMap.clear();
		boolean fileExists = false;
		for (ObservableList<?> row : nocItTable.getItems()) {
			String sampleID = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_SAMPLE_INDEX)).get();
			String popName = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_POPULATION_INDEX)).get();
			String maxNOC = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_NOC_INDEX)).get();
			String calibrationName = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX))
					.get();
			String output = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
			int rowID = ((SimpleIntegerProperty) row.get(Constants.NOCIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
			
			File f = new File(output);
			if (f.exists()) {
				nocItExistingFileRowList.add(rowID);
				fileExists = true;
			}

			// if output names are the same and population name already exists
			// with this output name
			if (UtilityMethods.isDuplicateOutput(f, popName, outputNamesPopulationMap)) {
				if (!nocItTableErrorMessage.contains(Constants.DUPLICATE_OUTPUT_FILE_ERROR_MESSAGE + "\n\n")) {
					nocItTableErrorMessage += Constants.DUPLICATE_OUTPUT_FILE_ERROR_MESSAGE + "\n\n";
				}
				nocItDuplicateOutputRowList.add(rowID);
			} else {
				outputNamesPopulationMap = UtilityMethods.updateutputNamesPopulationMap(
						outputNamesPopulationMap, f, popName);
			}

			// Is this even possible?
			if (sampleID.equals(Constants.DEFAULT_CHOOSE) || popName.equals(Constants.DEFAULT_CHOOSE)
					|| maxNOC.equals(Constants.DEFAULT_CHOOSE) || calibrationName.equals(Constants.DEFAULT_CHOOSE)
					|| output.equals(Constants.DEFAULT_CHOOSE)) {
				if (!nocItTableErrorMessage.contains(Constants.CALCULATIONS_MISSING_VALUE_ERROR_MESSAGE + "\n\n")) {
					nocItTableErrorMessage += Constants.CALCULATIONS_MISSING_VALUE_ERROR_MESSAGE + "\n\n";
				}
				nocItChooseEntryRowList.add(rowID);
			}
		}
		if (fileExists) {
			if (!nocItTableErrorMessage.contains(Constants.FILE_EXISTS_WARNING_MESSAGE)) {
				nocItTableErrorMessage += Constants.FILE_EXISTS_WARNING_MESSAGE;
			}
		}
	}

	/**
	 * This is called when the Start button is clicked on the CEESIt tab.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void startCEESItCalculations(ActionEvent event) {
		// Map used to avoid two rows writing to same output file unless
		// populations are different
		outputNamesPopulationCEESItMap = new HashMap<String, ArrayList<String>>();
		startCEESItButtonClicked = true;
	
		if (ceesItTable.getItems().size() <= 0) {
			return;
		}
		
		runCEESItTableErrorCheck();
		String[] knownContributorsArray = null;
		
		if (ceesItDuplicateOutputRowList.size() > 0 || ceesItChooseEntryRowList.size() > 0 || 
				ceesItNOCErrorRowList.size() > 0) {
			ceesItTable.refresh();
			Alert alert = new Alert(AlertType.ERROR, ceesItTableErrorMessage);
			alert.showAndWait();
			return;
		}
		
		if (ceesItExistingFileRowList.size() > 0) {
			ceesItTable.refresh();
			ButtonType[] results = { ButtonType.YES, ButtonType.NO };
			Alert alert = new Alert(AlertType.WARNING, Constants.CEESIt_OUTPUT_FILE_OVERWRITE_WARNING, results);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.YES) {
				UtilityMethods.deleteExistingFiles(ceesItExistingFileRowList, ceesItTable,
						Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX);
			} else {
				ceesItTable.getSelectionModel().select(-1);
				ceesItTable.refresh();
				return;
			}
		}
	
		// start button was clicked. let's calculate.
		ceesItProgressBar.setProgress(0);
	
		ceesItBarChartArea.getChildren().clear();
	
		for (ObservableList<?> row : ceesItTable.getItems()) {
			knownContributorsArray = null;
			String sampleID = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_SAMPLE_INDEX)).get();
			String caseNumber = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_CASE_INDEX)).get();
			boolean filter = ((SimpleBooleanProperty) row.get(Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX)).get();
			String popName = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_POPULATION_INDEX)).get();
			int noc = Integer.parseInt(((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_NOC_INDEX)).get());
			String calibrationName = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX))
					.get();
			String poiGenotypeID = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_POI_INDEX)).get();
			String knownContributorsEntry = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX)).get();
			if (knownContributorsEntry.length() > 0 && !knownContributorsEntry.equals(Constants.CEESIT_NO_KNOWN_CONTRIBUTORS_ENTRY)) {
				knownContributorsArray = knownContributorsEntry.split(",");
			}
			String output = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
			String comments = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_COMMENTS_INDEX)).get();
			int rowID = ((SimpleIntegerProperty) row.get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
	
			// Check for default output file
			if (output.equals(Constants.DEFAULT_OUTPUT_CHOICE_STRING)) {
				output = Settings.lastOutputFilePath + "/" + sampleID;
				if (filter)
					output += "-filtered";
				output += "-" + noc + "-" + calibrationName + ".pdf";
			}
			
			Genotype poiGenotype = genotypes.get(poiGenotypeID);
			
			try {
				Calibration calibration = calibrations.get(calibrationName);
				// Calibration parameters needed for report
				calibration.setCalibrationName(calibrationName);				
	
				Sample sample = ceesitSamples.get(sampleID);
				sample.populationCaseNumberMap.put(popName, caseNumber);
				sample.populationCommentsMap.put(popName, comments);
	
				sample.applyThresholds(ceesItRowIDAnalyticalThresholdsMap.get(Integer.toString(rowID)));
	
				if (filter) {
					sample.filter(calibration.getKit());
					sample.setFiltered(true);
				} else {
					sample.setFiltered(false);
				}
	
				File f = new File(output.substring(0, output.lastIndexOf(".")) + ".csv");					
				sample.writeFilteredSampleFile(f.getAbsolutePath(), sampleID);
	
				List<Genotype> knownGenotypes = null;
				if (knownContributorsArray != null && knownContributorsArray.length > 0) {
					knownGenotypes = new ArrayList<Genotype>();
					for (int k = 0; k < knownContributorsArray.length; k++) {
						knownGenotypes.add(genotypes.get(knownContributorsArray[k]));
					}
				}
				
				FreqTable freq = this.populations.get(popName);
				backendController.runCEESIt(calibration, sampleID, sample, new File(output), noc, poiGenotype, knownGenotypes,
						rowID, freq, ceesItRowIDAnalyticalThresholdsMap.get(Integer.toString(rowID)));
			} catch (Exception e) {
				logger.error(Constants.RUN_CEESIT_ERROR_LOG_MESSAGE, e);
			}
		}
	
		// Start the Time Elapsed timer.
		ceesItTimeElapsedTimer.start();
		disableItemsWhileCEESItRunning(true);
	}

	/**
	 * This is called when the Start button is clicked on the NOCIt tab.
	 *
	 * @param event
	 *            the event
	 */
	@FXML
	private void startNOCItCalculations(ActionEvent event) {
		// Map used to avoid two rows writing to same output file unless
		// populations are different
		outputNamesPopulationMap = new HashMap<String, ArrayList<String>>();
		startNOCItButtonClicked = true;

		if (nocItTable.getItems().size() <= 0) {
			return;
		}

		runNOCItTableErrorCheck();
		
		if (nocItDuplicateOutputRowList.size() > 0 || nocItChooseEntryRowList.size() > 0) {
			nocItTable.refresh();
			Alert alert = new Alert(AlertType.ERROR, nocItTableErrorMessage);
			alert.showAndWait();
			return;
		}

		if (nocItExistingFileRowList.size() > 0) {
			nocItTable.refresh();
			ButtonType[] results = { ButtonType.YES, ButtonType.NO };
			Alert alert = new Alert(AlertType.WARNING, Constants.NOCIt_OUTPUT_FILE_OVERWRITE_WARNING, results);
			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.YES) {
				UtilityMethods.deleteExistingFiles(nocItExistingFileRowList, nocItTable,
						Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX);
			} else {
				nocItTable.getSelectionModel().select(-1);
				nocItTable.refresh();
				return;
			}
		}

		// start button was clicked. let's calculate.
		nocItProgressBar.setProgress(0);

		nocItBarChartArea.getChildren().clear();

		for (ObservableList<?> row : nocItTable.getItems()) {
			String sampleID = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_SAMPLE_INDEX)).get();
			String caseNumber = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_CASE_INDEX)).get();
			boolean filter = ((SimpleBooleanProperty) row.get(Constants.NOCIT_TABLE_COLUMN_FILTER_INDEX)).get();
			String popName = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_POPULATION_INDEX)).get();
			int maxNOC = Integer
					.parseInt(((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_NOC_INDEX)).get());
			String calibrationName = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX))
					.get();
			String output = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
			String comments = ((SimpleStringProperty) row.get(Constants.NOCIT_TABLE_COLUMN_COMMENTS_INDEX)).get();
			int rowID = ((SimpleIntegerProperty) row.get(Constants.NOCIT_TABLE_COLUMN_ROW_ID_INDEX)).get();

			// Check for default output file
			if (output.equals(Constants.DEFAULT_OUTPUT_CHOICE_STRING)) {
				output = Settings.lastOutputFilePath + "/" + sampleID;
				if (filter)
					output += "-filtered";
				output += "-" + maxNOC + "-" + calibrationName + ".pdf";
			}

			try {
				Calibration calibration = calibrations.get(calibrationName);
				// Calibration parameters needed for report
				calibration.setCalibrationName(calibrationName);

				Sample sample = samples.get(sampleID);
				sample.populationCaseNumberMap.put(popName, caseNumber);
				sample.populationCommentsMap.put(popName, comments);

				sample.applyThresholds(rowIDAnalyticalThresholdsMap.get(Integer.toString(rowID)));

				if (filter) {
					sample.filter(calibration.getKit());
					sample.setFiltered(true);
				} else {
					sample.setFiltered(false);
				}

				File f = new File(output.substring(0, output.lastIndexOf(".")) + ".csv");
				sample.writeFilteredSampleFile(f.getAbsolutePath(), sampleID);

				FreqTable freq = this.populations.get(popName);
				backendController.runNOCIt(calibration, sampleID, sample, new File(output), maxNOC, rowID, freq,
						rowIDAnalyticalThresholdsMap.get(Integer.toString(rowID)));
			} catch (Exception e) {
				// e.printStackTrace();
				logger.error(Constants.RUN_NOCIT_ERROR_LOG_MESSAGE, e);
			}
		}

		// Start the Time Elapsed timer.
		nocItTimeElapsedTimer.start();
		disableItemsWhileNOCItRunning(true);
	}

	/**
	 * Update calibration genotype matching.
	 */
	public void updateCalibrationGenotypeMatching() {
		int i = 0;
		for (CSVModule csvModule : csvModuleMap.values()) {
			HashMap<String, String> genotypeIDs = matchGenotypeIDs(csvModule);
			// for (String sampleName : csvModule.getSamples().keySet()) {
			for (int j = 0; j < csvModule.getSampleNames().size(); j++) {
				String sampleName = csvModule.getSampleNames().get(j);
				SimpleStringProperty property = ((SimpleStringProperty) calibrationTable.getItems().get(i)
						.get(Constants.CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE_INDEX));
				if (genotypeIDs != null && genotypeIDs.get(sampleName) != null) {
					property.set(genotypeIDs.get(sampleName));
				} else {
					property.set(Constants.DEFAULT_CHOOSE);
				}
				i++;
			}
		}
	}

	/**
	 * Updates options for calibration dropdown boxes when calibrations are
	 * changed.
	 *
	 * @param name
	 *            the name
	 * @param calibration
	 *            the calibration
	 */
	@SuppressWarnings("unchecked")
	public void updateCalibrationOptions(String name, Calibration calibration) {
		calibrations.put(name, calibration);

		ObservableList<String> calibrationChoiceList = createCalibrationChoiceList();

		TableColumn<ObservableList<?>, String> calibrationColumn = (TableColumn<ObservableList<?>, String>) nocItTable
				.getColumns().get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX);
		calibrationColumn
				.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(calibrationChoiceList));

		calibrationColumn = (TableColumn<ObservableList<?>, String>) ceesItTable.getColumns()
				.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX);
		calibrationColumn
				.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(calibrationChoiceList));
	}

	/**
	 * Updates options for genotype ID dropdown boxes when genotypes are
	 * changed.
	 */
	private void updateGenotypeOptions() {
		ObservableList<String> namesChoiceList = FXCollections.observableArrayList(getGenotypeSamples());

		if (calibrationTable.getColumns().size() > Constants.CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE_INDEX) {
			@SuppressWarnings("unchecked")
			TableColumn<ObservableList<?>, String> column = (TableColumn<ObservableList<?>, String>) calibrationTable
					.getColumns().get(Constants.CALIBRATION_TABLE_COLUMN_KNOWN_GENOTYPE_INDEX);
			column.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(namesChoiceList));
		}

		ObservableList<String> poiNamesChoiceList = FXCollections.observableArrayList(getPOISamples());
		if (ceesItTable.getColumns().size() > Constants.CEESIT_TABLE_COLUMN_POI_INDEX) {
			@SuppressWarnings("unchecked")
			TableColumn<ObservableList<?>, String> poiColumn = (TableColumn<ObservableList<?>, String>) ceesItTable
					.getColumns().get(Constants.CEESIT_TABLE_COLUMN_POI_INDEX);
			//poiColumn.setCellFactory(ComboBoxTableCell.<ObservableList<?>, String>forTableColumn(poiNamesChoiceList));
			poiColumn.setCellFactory(col -> {
				return new ComboBoxTableCell<ObservableList<?>, String>(poiNamesChoiceList) {
					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						setText(item);
						if (getIndex() > -1) {
							try {
								// This line sometimes throws an index out of range exception when
								// scrolling the table. Unsure what is out of range.
								int rowID = ((SimpleIntegerProperty) ceesItTable.getItems().get(getIndex()).get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
								if (ceesItChooseEntryRowList.contains(rowID)) {
									setTextFill(Color.RED);
								} else {
									setTextFill(Color.BLACK);
								}
							} catch (Exception e) {
								
							}
						}
					}
				};
			});
		}
		ObservableList<String> knownContributorsChoiceList = FXCollections.observableArrayList(getKnownContributorsSamples());
		if (ceesItTable.getColumns().size() > Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX) {
			TableColumn<ObservableList<?>, String> knownContributorsColumn = (TableColumn<ObservableList<?>, String>) ceesItTable
					.getColumns().get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX);
			setKnownContributorsColumnCellFactory(knownContributorsColumn, knownContributorsChoiceList);				
		}
	}
	
	/**
	 * Set CheckComboBoxTableCell factory that uses CheckComboBox from ControlsFX. 
	 * http://stackoverflow.com/questions/26631041/javafx-combobox-with-checkboxes
	 * http://fxexperience.com/controlsfx/
	 * Override of default behavior of the cell during updates occurs here as well in 
	 * CheckComboBoxTableCell class.
	 * @param knownContributorsColumn
	 * @param knownContributorsChoiceList
	 */
	private void setKnownContributorsColumnCellFactory(TableColumn<ObservableList<?>, String> knownContributorsColumn, 
			ObservableList<String> knownContributorsChoiceList) {
		knownContributorsColumn.setCellFactory(col -> {
			return new CheckComboBoxTableCell<ObservableList<?>, String>(knownContributorsColumn, 
					knownContributorsChoiceList, ceesItNOCErrorRowList, 
					ceesItDuplicateOutputRowList, ceesItExistingFileRowList, 
					startCEESItButtonClicked) {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if (item != null && item.length() > 0) {
						setTooltip(new Tooltip(item));
					} else {
						setTooltip(null);
					}
				}
			};
		});
	}

	/**
	 * Update analytical thresholds when calibration is selected in Calibration
	 * column of nocItTable.
	 *
	 * @param calibration
	 *            the calibration
	 * @param rowList
	 *            the row list
	 */
	private void updateNOCItATsForSelectedCalibration(Calibration calibration, ObservableList<?> rowList) {
		// This code sets thresholds from the kit from the calibration
		LinkedHashMap<Locus, Integer> analyticalThresholds = new LinkedHashMap<>();

		for (Locus locus : calibration.getKit().getLoci()) {
			analyticalThresholds.put(locus, 1);
		}
		int rowID = ((SimpleIntegerProperty) rowList.get(Constants.NOCIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
		rowIDAnalyticalThresholdsMap.put(Integer.toString(rowID), analyticalThresholds);
	}
	
	/**
	 * Update analytical thresholds when calibration is selected in Calibration
	 * column of nocItTable.
	 *
	 * @param calibration
	 *            the calibration
	 * @param rowList
	 *            the row list
	 */
	private void updateCEESItATsForSelectedCalibration(Calibration calibration, ObservableList<?> rowList) {
		// This code sets thresholds from the kit from the calibration
		LinkedHashMap<Locus, Integer> analyticalThresholds = new LinkedHashMap<>();

		for (Locus locus : calibration.getKit().getLoci()) {
			analyticalThresholds.put(locus, 1);
		}
		int rowID = ((SimpleIntegerProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
		ceesItRowIDAnalyticalThresholdsMap.put(Integer.toString(rowID), analyticalThresholds);
	}	

	/**
	 * Called when View -> Genotypes is selected.
	 */
	@FXML
	public void viewGenotypes() {
		buildViewGenotypesTable();

		Button cancel = new Button("Close");
		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewGenotypesStage.close();
			}
		});
		StackPane.setMargin(cancel, new Insets(0.0, 0.0, 0.0, 0.0));

		// Build the Popup control
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
		// AnchorPane.setTopAnchor(scrollPane, 25.0);
		scrollPane.setMaxWidth(825);
		scrollPane.setMinWidth(625);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		StackPane stackPane = new StackPane();
		stackPane.setAlignment(Pos.CENTER);
		AnchorPane.setTopAnchor(stackPane, 0.0);
		AnchorPane.setBottomAnchor(stackPane, 0.0);
		AnchorPane.setLeftAnchor(stackPane, 0.0);
		AnchorPane.setRightAnchor(stackPane, 0.0);

		AnchorPane.setTopAnchor(viewGenotypesTable, 0.0);
		AnchorPane.setBottomAnchor(viewGenotypesTable, 0.0);
		AnchorPane.setLeftAnchor(viewGenotypesTable, 0.0);
		AnchorPane.setRightAnchor(viewGenotypesTable, 0.0);

		middlePane.getChildren().add(scrollPane);
		scrollPane.setContent(viewGenotypesTable);
		bottomPane.getChildren().add(stackPane);
		stackPane.getChildren().add(cancel);
		pane.getItems().addAll(viewGenotypesTable, bottomPane);

		Scene scene = new Scene(pane);
		viewGenotypesStage = new Stage();
		viewGenotypesStage.setTitle("View Genotypes");
		viewGenotypesStage.initModality(Modality.WINDOW_MODAL);
		viewGenotypesStage.setScene(scene);
		viewGenotypesStage.setMinWidth(850);
		viewGenotypesStage.setMinHeight(650);
		viewGenotypesStage.getIcons().add(new Image(getClass().getResourceAsStream("dna2.png")));
		viewGenotypesStage.initOwner(stage);
		viewGenotypesStage.showAndWait();
	}

	/**
	 * Write genotypes file.
	 *
	 * @param path
	 *            the path
	 */
	private void writeGenotypesFile(String path) {
		final ArrayList<Locus> genLoci = new ArrayList<Locus>();

		HashMap<Locus, Allele[]> genoView = new LinkedHashMap<>();		

		for (String gen : genotypes.keySet()) {
			for (Locus h : genotypes.get(gen).getLoci()) 
				genLoci.add(h);
			break;
		}

		ArrayList<String> headerNames = new ArrayList<String>();
		headerNames.add("Sample ID");
		// get column names
		for (int i = 1; i <= genLoci.size(); i++) {
			String columnName = genLoci.get(i - 1).getName();
			headerNames.add(columnName);
		}

		ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>();
		for (String genotypeId : genotypes.keySet()) {
			ArrayList<String> line = new ArrayList<String>();
			Genotype genotypeView = genotypes.get(genotypeId);
			line.add(genotypeId);
			for (Locus locus : genLoci) {
				String str = "";
				if (genotypeView.containsLocus(locus)) {
					String x = genotypeView.getAlleles(locus)[0].toString();
					String y = genotypeView.getAlleles(locus)[1].toString();
					str = "\"" + x + "," + y + "\"";
				}
				line.add(str);
			}
			lines.add(line);
		}
		TextFileWriter.write(path, headerNames, lines);
	}
	
	private void buildCommentsBox(TableView<ObservableList<?>> table, TableRow<ObservableList<?>> row, int columnIndex) {
		final int xSize = Constants.COMMENTS_BOX_X_SIZE;
		final int ySize = Constants.COMMENTS_BOX_Y_SIZE;

		TextArea textArea = new TextArea();
		textArea.setEditable(true);
		textArea.setPrefSize(xSize, ySize);
		textArea.focusedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						textArea.selectAll();
					}
				});
			}
		});
		
		ObservableList<?> rowList = (ObservableList<?>) table.getItems().get(row.getIndex());
		String comment = ((SimpleStringProperty) rowList
				.get(columnIndex)).get();
		
		textArea.setText(comment);

		Button b = new Button("OK");
		b.setAlignment(Pos.BOTTOM_CENTER);

		VBox box = new VBox(1);
		box.getChildren().add(textArea);
		box.getChildren().add(b);
		box.setPrefSize(xSize, ySize);
		box.setAlignment(Pos.CENTER);
		Scene scene = new Scene(box);

		Stage stg = new Stage();

		b.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// System.out.println(textArea.getText());
				((SimpleStringProperty) table.getItems().get(row.getIndex())
						.get(columnIndex)).set(textArea.getText());
				stg.close();
			}
		});

		stg.setTitle("Add/Edit Comments");
		stg.setScene(scene);
		stg.initStyle(StageStyle.UTILITY);
		stg.initModality(Modality.WINDOW_MODAL);
		stg.initOwner(stage);
		stg.show();
	}
	
	private void fillWithValue(TableView<ObservableList<?>> table, TableRow<ObservableList<?>> row, 
			int columnIndex, int rowIndex) {
		ObservableList<?> rowList = (ObservableList<?>) table.getItems().get(row.getIndex());
		String value = ((SimpleStringProperty) rowList.get(columnIndex)).get();

		ObservableList<ObservableList<?>> rows = table.getSelectionModel().getSelectedItems();
		for (int i = 0; i < rows.size(); i++) {
			((SimpleStringProperty) rows.get(i).get(columnIndex)).set(value);
			if (table.getColumns().size() == Constants.ceesItColumnsList.size() &&
					(columnIndex == Constants.CEESIT_TABLE_COLUMN_POI_INDEX ||
					columnIndex == Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX)) {
				((SimpleStringProperty) rows.get(i).get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX))
				.set(UtilityMethods.outputFilePath(ceesItTable, rows.get(i), rowIndex));
			}
		}
		table.refresh();
	}
	
	private void duplicateRow(TableView<ObservableList<?>> table, TableRow<ObservableList<?>> selectedRow, 
			HashMap<String, LinkedHashMap<Locus, Integer>> tableRowIDAnalyticalThresholdsMap) {
		// duplicate last row
		ObservableList<?> lastRow = (ObservableList<?>) table.getItems()
				.get(table.getItems().size() - 1);	
		boolean graph = ((SimpleBooleanProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_GRAPH_INDEX)).get();
		String caseNum = ((SimpleStringProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_CASE_INDEX)).get();
		String sampleID = ((SimpleStringProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_SAMPLE_INDEX)).get();
		boolean filtered = ((SimpleBooleanProperty) lastRow
				.get(Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX)).get();
		String dna = ((SimpleStringProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_DNA_INDEX)).get();
		String population = ((SimpleStringProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_POPULATION_INDEX)).get();
		String noc = ((SimpleStringProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_NOC_INDEX)).get();
		String calibrationName = ((SimpleStringProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX)).get();
		String poi = ((SimpleStringProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_POI_INDEX)).get();
		String knownContributors = ((SimpleStringProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX)).get();
		String output = ((SimpleStringProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
		String comments = ((SimpleStringProperty) lastRow.get(Constants.CEESIT_TABLE_COLUMN_COMMENTS_INDEX)).get();
		
		ObservableList<Object> newRow = FXCollections.observableArrayList();
		newRow.add(new SimpleBooleanProperty(graph));
		newRow.add(new SimpleStringProperty(caseNum));
        newRow.add(new SimpleStringProperty(sampleID));
		newRow.add(new SimpleBooleanProperty(filtered));
		newRow.add(new SimpleStringProperty(dna));
        newRow.add(new SimpleStringProperty(population));
        newRow.add(new SimpleStringProperty(noc));
        newRow.add(new SimpleStringProperty(calibrationName));
        newRow.add(new SimpleStringProperty(poi));
        newRow.add(new SimpleStringProperty(knownContributors));
        newRow.add(new SimpleStringProperty(output));
        newRow.add(new SimpleStringProperty(comments));
             
        LinkedHashMap<Locus, Integer> currentThresholdData = tableRowIDAnalyticalThresholdsMap.get(Integer.toString(table.getItems().size() - 1));
        tableRowIDAnalyticalThresholdsMap.put(Integer.toString(table.getItems().size()), UtilityMethods.copyThresholdData(currentThresholdData));
        
        newRow.add(new SimpleIntegerProperty(table.getItems().size()));
        table.getItems().add(newRow);
		
        // copy each row down to next row
		if (table.getItems().size() > 1) {
			for (int i = table.getItems().size() - 2; i > selectedRow.getIndex() - 1; i--) {
				ObservableList<?> row = (ObservableList<?>) table.getItems()
						.get(i);
				
				graph = ((SimpleBooleanProperty) row.get(Constants.CEESIT_TABLE_COLUMN_GRAPH_INDEX))
						.get();
				caseNum = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_CASE_INDEX)).get();
				sampleID = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_SAMPLE_INDEX)).get();
				filtered = ((SimpleBooleanProperty) row.get(Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX)).get();
				dna = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_DNA_INDEX)).get();
				population = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_POPULATION_INDEX)).get();
				noc = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_NOC_INDEX)).get();
				calibrationName = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX)).get();
				poi = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_POI_INDEX)).get();
				knownContributors = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX)).get();
				output = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX)).get();
				comments = ((SimpleStringProperty) row.get(Constants.CEESIT_TABLE_COLUMN_COMMENTS_INDEX)).get();
				
				ObservableList<?> nextRow = (ObservableList<?>) table.getItems()
						.get(i + 1);
				((SimpleBooleanProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_GRAPH_INDEX)).set(graph);
				((SimpleStringProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_CASE_INDEX)).set(caseNum);
				((SimpleStringProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_SAMPLE_INDEX)).set(sampleID);
				((SimpleBooleanProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX)).set(filtered);
				((SimpleStringProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_DNA_INDEX)).set(dna);
				((SimpleStringProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_POPULATION_INDEX)).set(population);
				((SimpleStringProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_NOC_INDEX)).set(noc);
				((SimpleStringProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX)).set(calibrationName);
				((SimpleStringProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_POI_INDEX)).set(poi);
				((SimpleStringProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX)).set(knownContributors);
				((SimpleStringProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX)).set(output);
				((SimpleStringProperty) nextRow.get(Constants.CEESIT_TABLE_COLUMN_COMMENTS_INDEX)).set(comments);
				
				LinkedHashMap<Locus, Integer> currentThresholdData1 = tableRowIDAnalyticalThresholdsMap.get(Integer.toString(i + 1));
		        tableRowIDAnalyticalThresholdsMap.put(Integer.toString(table.getItems().size()), UtilityMethods.copyThresholdData(currentThresholdData1));
			}
		}
		
		table.refresh();
		table.getSelectionModel().select(selectedRow.getIndex());
	}
	
	/**
	 * Any edits to the ceesit table that will change error highlighting will remove ID for noc from
	 * list causing cell to be black again. File lists are cleared. The entire
     * set of files in the table could be checked but for now
     * just clear the list. Hitting the Start button will run the check again. Existing
     * file check has also been replaced with clearing the list.
	 * @param rowList
	 */
	private void ceesItEditRowAction(ObservableList<?> rowList, int columnIndex) {
		int rowID = ((SimpleIntegerProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).get();
		if (ceesItNOCErrorRowList.contains(rowID)) {
			if (columnIndex == Constants.CEESIT_TABLE_COLUMN_NOC_INDEX) {
				ceesItNOCErrorRowList.remove(ceesItNOCErrorRowList.indexOf(rowID));
			}
		}
		ceesItDuplicateOutputRowList.clear(); 
		ceesItExistingFileRowList.clear(); 
		ceesItTable.refresh();
	}
	
	/**
	 * For actions like deleting rows, just clear all error checking lists for now
	 */
	private void clearCEESItFileLists() {
		ceesItNOCErrorRowList.clear();
		ceesItDuplicateOutputRowList.clear(); 
		ceesItExistingFileRowList.clear(); 
	}
	
	/**
	 * For actions like deleting rows, just clear all error checking lists for now
	 */
	private void clearNOCItFileLists() {
		nocItDuplicateOutputRowList.clear(); 
		nocItExistingFileRowList.clear(); 
	}
	
	public TabPane getTabPane() {
		return tabPane;
	}
	
	public SettingsController getSettingsController() {
		return settingsController;
	}

	public void setSettingsController(SettingsController settingsController) {
		this.settingsController = settingsController;
	}

	public boolean isTimerRunning() {
		return timerRunning;
	}
}
