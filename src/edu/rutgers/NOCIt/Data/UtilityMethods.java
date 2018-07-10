package edu.rutgers.NOCIt.Data;

import static edu.rutgers.NOCIt.UIMain.logger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.math3.util.FastMath;

import edu.rutgers.NOCIt.AnalyticalThresholdsCellFactory;
import edu.rutgers.NOCIt.Control.Settings;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

/**
 * @author James Kelley
 * @author Desmond Lun
 */
public class UtilityMethods {    
	// from http://stackoverflow.com/questions/19487506/built-in-methods-for-displaying-significant-figures
	public static String toSignificantFiguresString(BigDecimal bd, int significantFigures ){
		String test = String.format("%."+significantFigures+"G", bd);
		if (test.contains("E+")){
			test = String.format(Locale.US, "%.0f", Double.valueOf(String.format("%."+significantFigures+"G", bd)));
		}
		return test;
	}

	/**
	 * Returns number rounded to significant figures and in scientific notation if
	 * above or below input values
	 * @param value
	 * @param significantFigures
	 * @param sciFormatter
	 * @param minDecimalFormat
	 * @param maxDecimalFormat
	 * @return
	 */
	public static String roundToSignificantFigures(double value, int significantFigures, DecimalFormat sciFormatter, 
			double minDecimalFormat, double maxDecimalFormat) {
		String result = Double.toString(value);
		result = toSignificantFiguresString(BigDecimal.valueOf(value), significantFigures);
		if (Math.abs(Double.valueOf(result)) != 0.0 && (Math.abs(Double.valueOf(result)) < minDecimalFormat || Math.abs(Double.valueOf(result)) >= maxDecimalFormat)) {
			try {
				result = sciFormatter.format((Number)Double.valueOf(result));
			} catch (NumberFormatException nfe) {

			}
		}

		return result;
	}
	
	public static boolean isNumberInRange(double value, double lowerBound, double upperBound) {
		if (value >= lowerBound && value <= upperBound) {
			return true;
		}
		return false;
	}

    public static double logSum(double logX1, double logX2) {
    	if (logX1 == Double.NEGATIVE_INFINITY)
    		return logX2;
    	else if (logX1 == Double.POSITIVE_INFINITY)
    		return logX1;
    	
    	if (logX2 == Double.NEGATIVE_INFINITY)
    		return logX1;
    	else if (logX2 == Double.POSITIVE_INFINITY)
    		return logX2;
    	
    	double logSum;
    	if (logX1 >= logX2) {
            logSum = logX1 + FastMath.log(1.0 + FastMath.exp(logX2 - logX1));
        } else {
            logSum = logX2 + FastMath.log(1.0 + FastMath.exp(logX1 - logX2));
        }
    	
    	return logSum;
    }
    
    public static double logDiff(double logX1, double logX2) {
    	if (logX1 == Double.NEGATIVE_INFINITY) {
    		if (logX2 == Double.NEGATIVE_INFINITY)
    			return Double.NEGATIVE_INFINITY;
    		else     			
    			return Double.NaN;    		
    	}
    	else if (logX1 == Double.POSITIVE_INFINITY) {
    		if (logX2 == Double.POSITIVE_INFINITY)     			
    			return Double.NaN;    	
    		else
    			return logX1;
    	}
    	
    	if (logX2 == Double.NEGATIVE_INFINITY)
    		return logX1;
    	else if (logX2 == Double.POSITIVE_INFINITY)     		
    		return Double.NaN;    	
    	
    	double logDiff;
    	if (logX1 >= logX2) {
            logDiff = logX1 + FastMath.log(1.0 - FastMath.exp(logX2 - logX1));
        } else {
            logDiff = Double.NaN;
        }
    	
    	return logDiff;
    }       
    
    /**
     * Creates NOCIt default output path
     * @param path
     * @param sampleID
     * @param filtered
     * @param noc
     * @param calibrationName
     * @return
     */
    public static String nocItOutputFilePath(String path, String sampleID, boolean filtered, String noc, String calibrationName) {
    	String output = path + "/";
		try {
			output += URLEncoder.encode(sampleID, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (filtered) {
            output += "-filtered";
        }
        output += "-" + noc
        + "-" + calibrationName + ".pdf";
        
        return output;
    }
    
    /**
	 * Update CEESIt output name.
	 *
	 * @param rowList
	 *            the row list
	 * @param row
	 *            the row
	 */
	public static void updateNOCItOutputName(TableView<ObservableList<?>> table, 
			ObservableList<?> rowList, int row) {
		String sampleID = ((SimpleStringProperty) rowList.get(Constants.NOCIT_TABLE_COLUMN_SAMPLE_INDEX)).get();
		boolean filtered = ((SimpleBooleanProperty) rowList.get(Constants.NOCIT_TABLE_COLUMN_FILTER_INDEX)).get();
		String noc = ((SimpleStringProperty) rowList.get(Constants.NOCIT_TABLE_COLUMN_NOC_INDEX)).get();
		String calibrationName = ((SimpleStringProperty) rowList.get(Constants.NOCIT_TABLE_COLUMN_CALIBRATION_INDEX))
				.get();
		String output = UtilityMethods.nocItOutputFilePath(Settings.lastOutputFilePath, sampleID, filtered, noc,
				calibrationName);
		File outputFile = new File(output);
		((SimpleStringProperty) table.getItems().get(row).get(Constants.NOCIT_TABLE_COLUMN_OUTPUT_INDEX))
				.set(outputFile.getAbsolutePath());
	}
    
    /**
     * Creates CEESIt default output path
     * @param path
     * @param sampleID
     * @param filtered
     * @param noc
     * @param calibrationName
     * @param poi
     * @return
     */
    public static String ceesItOutputFilePath(String path, String sampleID, boolean filtered, String noc, 
    		String calibrationName, String poi, String knownContributors) {
    	String output = path + "/";
		try {
			output += URLEncoder.encode(sampleID, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String poiValue = "";
    	if (!poi.equals(Constants.DEFAULT_ADD_NEW) && !poi.equals(Constants.DEFAULT_CHOOSE)) {
    		poiValue = "-" + poi;
    	}
    	String knownContributorsValue = "";
    	if (knownContributors.length() > 0 && !knownContributors.equals(Constants.CEESIT_NO_KNOWN_CONTRIBUTORS_ENTRY)) {
    		// It is unclear if commas are permitted in file names. It is probably better to replace
    		// commas with underscores
    		String knownContributorsCleaned = knownContributors.replace(",", "_");
    		knownContributorsValue = "-" + knownContributorsCleaned;
    	}
        if (filtered) {
            output += "-filtered";
        }
        output += "-" + noc
        + "-" + calibrationName + poiValue + knownContributorsValue
        + ".pdf";
        
        return output;
    }
    
    /**
	 * Update CEESIt output name.
	 *
	 * @param rowList
	 *            the row list
	 * @param row
	 *            the row
	 */
	public static void updateCEESItOutputName(TableView<ObservableList<?>> table, 
			ObservableList<?> rowList, int row) {
		String output = outputFilePath(table, rowList, row);
		File outputFile = new File(output);
		((SimpleStringProperty) table.getItems().get(row).get(Constants.CEESIT_TABLE_COLUMN_OUTPUT_INDEX))
				.set(outputFile.getAbsolutePath());
	}
	
	public static String outputFilePath(TableView<ObservableList<?>> table, 
			ObservableList<?> rowList, int row) {
		String sampleID = ((SimpleStringProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_SAMPLE_INDEX)).get();
		boolean filtered = ((SimpleBooleanProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_FILTER_INDEX)).get();
		String noc = ((SimpleStringProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_NOC_INDEX)).get();
		String calibrationName = ((SimpleStringProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_CALIBRATION_INDEX))
				.get();
		String poi = ((SimpleStringProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_POI_INDEX)).get();
		String knownContributors = ((SimpleStringProperty) rowList.get(Constants.CEESIT_TABLE_COLUMN_KNOWN_CONTRIBUTORS_INDEX)).get();
		String output = UtilityMethods.ceesItOutputFilePath(Settings.lastOutputFilePath, sampleID, filtered, noc,
				calibrationName, poi, knownContributors);
		return output;
	}
    
    /**
     * Analytical Thresholds are stored in Settings directory using file names derived from the
     * kit name so that the correct saved thresholds can be loaded for the kit being used.
     * Some characters that occur in the kit name from "Chemistry Kit" line contain characters
     * such as \ or / that result in the file to not be able to be written. This method
     * removed these characters from the methods that write an read these files removing the
     * need to impose rules on users creating thses files.
     * @param kitName
     * @return
     */
    public static String cleanedUpKitName(String kitName) {
    	if (kitName.contains("/")) {
    		kitName = kitName.replace("/", "");
    	}
    	if (kitName.contains("\\")) {
    		kitName = kitName.replace("\\", "");
    	}
    	return kitName;
    }

    /**
     * Split path.
     *
     * @param path
     *            the path
     * @param splitIndex
     *            the split index
     * @return the string
     */
    public static String splitPath(String path, int splitIndex) {
    	if (path.length() > splitIndex) {
    		path = path.substring(0, splitIndex) + "\n" + Constants.LINE_TWO_INDENTATION
    				+ path.substring(splitIndex, path.length());
    	}
    	return path;
    }

    /**
     * Produces word-wrap for file paths that are too long to fit on one line in
     * report.
     *
     * @param path
     *            the path
     * @return the string
     */
    public static String multiLinePath(String path) {
    	int splitIndex = 60;
    	int splitIndex2 = 120;
    	int splitIndex3 = 180;
    	int splitIndex4 = 240;
    	if (path.length() > splitIndex4) {
    		path = splitPath(path, splitIndex4);
    	}
    	if (path.length() > splitIndex3) {
    		path = splitPath(path, splitIndex3);
    	}
    	if (path.length() > splitIndex2) {
    		path = splitPath(path, splitIndex2);
    	}
    	if (path.length() > splitIndex) {
    		path = splitPath(path, splitIndex);
    	}

    	return path;
    }
    
    /**
     * Create new file name if file already exists
     * Based on http://stackoverflow.com/questions/27096374/create-and-increment-file-name-if-the-name-exists
     */
    public static String createFilenameIfFileExists(String path) {
    	String pathExtensionRemoved = path.substring(0, path.length() - 4);
    	String extension = path.substring(path.length() - 4);
    	String newPath = "";
    	File f = null;
    	for (int i = 1;; i++) {
        	newPath = pathExtensionRemoved + "_" + i + extension;
            f = new File(newPath);
            if (!f.exists()) {
                break;
            }
        }
    	
		return newPath;
    }
    
    /**
	 * Based on https://docs.oracle.com/javase/tutorial/essential/io/pathOps.html
	 * and http://stackoverflow.com/questions/893977/java-how-to-find-out-whether-a-file-name-is-valid
	 * returns false if the path string cannot be converted to a Path (InvalidPathException)
	 * @param string
	 * @return
	 */
	public static boolean isValidPath(String string) {
		try {			
			// Unused warning but this line is required for NoSuchFileException
			// and IOException
		    @SuppressWarnings("unused")
			Path path = Paths.get(string);
		} catch (InvalidPathException x) {
		    // Logic for case when path string cannot be converted to a Path.
			return false;
		}
		return true;
		
	}
    
    /**
     * Creates a deep copy of thresholds data so changing thresholds of one sample does not
     * change the thresholds in all samples created in the same batch
     * @param thresholdData
     * @return
     */
    public static LinkedHashMap<Locus, Integer> copyThresholdData(LinkedHashMap<Locus, Integer> thresholdData) {
    	LinkedHashMap<Locus, Integer> thresholdDataCopy = new LinkedHashMap<Locus, Integer>();
    	for (Locus locus : thresholdData.keySet()) {
    		thresholdDataCopy.put(locus, thresholdData.get(locus));
    	}
    	
		return thresholdDataCopy;
    }
    
    /**
     * Creates an error message from a list
     * @param title
     * @param list
     * @return
     */
    public static String errorMessageFromList(String title, ArrayList<String> list) {
    	String message = "";
    	
    	message += title + "\n";
		for (int i = 0; i < list.size(); i++) {
			message += list.get(i) + "\n";
		}
		return message;
    }
    
    public static void showErrorMessage(Stage stage, String message) {
    	final int xSize = 600;
		final int ySize = 500;

		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setPrefSize(xSize, ySize);
		
		textArea.setText(message);

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

		stg.setTitle("Error");
		stg.setScene(scene);
		stg.initStyle(StageStyle.UTILITY);
		stg.initModality(Modality.WINDOW_MODAL);
		stg.initOwner(stage);
		stg.show();
    }
       
    /**
     * 
     * @param currentThresholdData
     * @param savedThresholdData
     * @param kit
     * @return
     */
    public static LinkedHashMap<Locus, Integer> updatedThresholdsData(HashMap<Locus, Integer> currentThresholdData, 
    		HashMap<Locus, Integer> savedThresholdData, Kit kit) {
    	LinkedHashMap<Locus, Integer> thresholdData = new LinkedHashMap<Locus, Integer>();
    	if (currentThresholdData != null) {
			for (Locus locus : currentThresholdData.keySet()) {
				if (savedThresholdData.containsKey(locus)) {
					thresholdData.put(locus, savedThresholdData.get(locus));
				}
			}
		} else {
			currentThresholdData = new LinkedHashMap<Locus, Integer>();
			for (Locus locus : kit.getLoci()) {
				if (savedThresholdData.containsKey(locus)) {
					thresholdData.put(locus, savedThresholdData.get(locus));
				} else {
					thresholdData.put(locus, 1);
				}
			}
		}
		return thresholdData;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static void populateThresholdsTable(HashMap<Locus, Integer> data, TableView<ObservableList<?>> table) {
    	table.setEditable(true);
        table.getColumns().clear();
        
        List<TableColumn<ObservableList<?>, ?>> columnList = new ArrayList<>();
        
        TableColumn<ObservableList<?>, String> column1 = new TableColumn<>("Locus");
        column1.setMinWidth(75);
        column1.setEditable(false);
        column1.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
                return (SimpleStringProperty) cellDataFeatures.getValue().get(0);
            }
        });
        column1.setCellFactory(TextFieldTableCell.<ObservableList<?>>forTableColumn());
        table.getColumns().add(column1);
        columnList.add(column1);

        TableColumn<ObservableList<?>, String> column2 = new TableColumn<>("Threshold");
        column2.setMinWidth(75);
        column2.setPrefWidth(75);
        column2.setEditable(true);
        column2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<?>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList<?>, String> cellDataFeatures) {
                return (SimpleStringProperty) cellDataFeatures.getValue().get(1);
            }
        });

        column2.setCellFactory(new AnalyticalThresholdsCellFactory());
        table.getColumns().add(column2);
        columnList.add(column2);
        
        // Add empty column to avoid undesirable behavior of when row is right-clicked 
        // in empty area, selected cells were deselected
        TableColumn<ObservableList<?>, String> column3 = new TableColumn<>("");
        column3.setMinWidth(1000);
        column3.setEditable(false);
        table.getColumns().add(column3);
        columnList.add(column3);
        
        ArrayList<Locus> locusKeys = new ArrayList<Locus>(data.keySet());

        for (Locus locus : locusKeys) {
            ObservableList<Object> initialRow = FXCollections.observableArrayList();
            initialRow.add(new SimpleStringProperty(locus.getName()));
            initialRow.add(new SimpleStringProperty(data.get(locus).toString()));
            table.getItems().add(initialRow);
        }
        
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Row Context Menu
        table.setRowFactory(new Callback<TableView<ObservableList<?>>, TableRow<ObservableList<?>>>() {
        	public TableRow<ObservableList<?>> call(TableView<ObservableList<?>> tableView) {
        		final TableRow<ObservableList<?>> row = new TableRow<>();
        		final ContextMenu contextMenu = new ContextMenu();

        		final MenuItem fillWithThresholdMenuItem = new MenuItem("Fill With Analytical Threshold");
        		fillWithThresholdMenuItem.setOnAction(new EventHandler<ActionEvent>() {
        			@Override
        			public void handle(ActionEvent event) {
        				ObservableList<?> rowList = (ObservableList<?>) table.getItems().get(row.getIndex());
        				String at = ((SimpleStringProperty) rowList.get(1)).get();
        				ObservableList<ObservableList<?>> rows = table.getSelectionModel().getSelectedItems();
        				for (int i = 0; i < rows.size(); i++) {
        					((SimpleStringProperty) rows.get(i).get(1)).set(at);
        				}
        				table.refresh();
        			}
        		});
        		contextMenu.getItems().add(fillWithThresholdMenuItem);
        		// Set context menu on row, but use a binding to make it only
        		// show for non-empty rows:
        		row.contextMenuProperty()
        		.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
        		return row;
        	}
        });
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
						logger.error(Constants.DISABLE_COLUMN_REORDERING_ERROR, e);
					}
				}
			}
		});
    }
    
    public static Stage buildAnalyticalThresholdsStage(Scene scene) {
    	Stage stage = new Stage();
        stage.setTitle("Analytical Thresholds");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setScene(scene);
        stage.setMinWidth(300);
        stage.setMinHeight(400);
        stage.setResizable(true);
		return stage;
    }
    
    public static String createPopulationTooltip(FreqTable frequency) {
    	String tooltip = "Path: " + frequency.getFilePath() + "\n"
        		+ "Number of People: " + frequency.getNumPeople();
		return tooltip;
    	
    }
    
    /**
     * If output names are the same and population name already exists
   	 * with this output name return true
     * @param f
     * @param popName
     * @param outputNamesPopulationMap
     * @return
     */
    public static boolean isDuplicateOutput(File f, String popName,
    		HashMap<String, ArrayList<String>> outputNamesPopulationMap) {
    	if (outputNamesPopulationMap.containsKey(f.getAbsolutePath())
				&& outputNamesPopulationMap.get(f.getAbsolutePath()) != null
				&& outputNamesPopulationMap.get(f.getAbsolutePath()).contains(popName)) {   		
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static HashMap<String, ArrayList<String>> updateutputNamesPopulationMap(
    		HashMap<String, ArrayList<String>> outputNamesPopulationMap, File f, String popName) {
    	if (outputNamesPopulationMap.get(f.getAbsolutePath()) == null) {
			ArrayList<String> popList = new ArrayList<String>();
			popList.add(popName);
			outputNamesPopulationMap.put(f.getAbsolutePath(), popList);
		} else {
			ArrayList<String> popList = outputNamesPopulationMap.get(f.getAbsolutePath());
			popList.add(popName);
			outputNamesPopulationMap.put(f.getAbsolutePath(), popList);
		}
    	
		return outputNamesPopulationMap;
    	
    }
    
    /**
     * Fixes bug where running NOCIt or CEESIt multiple times without closing the gui, if
     * file exists, the file was appended. The result was the same file name and population
     * combination written 2 times, 3 times, etc. Deleting the file solves this.
     * @param existingFileRowList
     * @param tableView
     * @param columnIndex
     */
    public static void deleteExistingFiles(ArrayList<Integer> existingFileRowList, 
    		TableView<ObservableList<?>> tableView, int columnIndex) {
    	for (int i = 0; i < existingFileRowList.size(); i++) {
			ObservableList<?> row = tableView.getItems().get(i);
			String output = ((SimpleStringProperty) row.get(columnIndex)).get();
			File f = new File(output);
			deleteFileIfExists(f.getAbsolutePath());
		}
    }
    
    // based on http://www.java2s.com/Code/Java/File-Input-Output/DeletefileusingJavaIOAPI.htm
    public static void delete(String fileName) {
    	File f = new File(fileName);

    	boolean success = f.delete();
    	if (success)
    		//System.out.println(fileName + " deletion succeeded");
    	if (!success) {
    		//System.out.println(fileName + " deletion failed");
    	}	 
    }
 	
 	public static void deleteFileIfExists(String filename) {
 		File f = new File(filename);
 		if (f.exists()) {
 			delete(filename);						
 		}
 	}
    
}
