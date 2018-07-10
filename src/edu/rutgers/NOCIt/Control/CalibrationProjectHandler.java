package edu.rutgers.NOCIt.Control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import edu.rutgers.NOCIt.UIController;
import edu.rutgers.NOCIt.Data.CSVModule;
import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.Genotype;
import edu.rutgers.NOCIt.Data.Locus;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Rob Carpenter
 * @author Desmond Lun
 * @author James Kelley
 */
public class CalibrationProjectHandler {
	private static String SAMPLES_DIR = "samples/";
	private static String FILTERED_SAMPLES_DIR = "samples/filtered/";

	static FileSystem ZipFileSystem = null;
	private static boolean calibrationCalculated = false;
	private static boolean allSelected = true;
	
	public boolean newProject(java.io.File file) {
        try {
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file.getAbsolutePath()));
            zip.close();

            Path zipFile = Paths.get(file.getAbsolutePath());

            ZipFileSystem = FileSystems.newFileSystem(zipFile, null);
            //Create samples folder
            Path samplesDirectory = ZipFileSystem.getPath(SAMPLES_DIR); // from zip file system
            Files.createDirectories(samplesDirectory);
            //Create filtered folder inside of samples folder
            Path filteredDirectory = ZipFileSystem.getPath(FILTERED_SAMPLES_DIR); // from zip file system
            Files.createDirectories(filteredDirectory);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addFile(java.io.File file, String directory) {
        try {
            Path newFile = Paths.get(file.getAbsolutePath());
            Files.copy(newFile, ZipFileSystem.getPath(directory + file.getName()));
        } catch (Exception e) {
            return;
        }
    }

    public void saveAndClose() {
        try {
            ZipFileSystem.close();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
    
    public static void saveCalibration(File file, Calibration calibration) {
    	CalibrationProjectHandler pHandler = new CalibrationProjectHandler();
    	pHandler.newProject(file);

    	try {
    		File calibrationObjectValues = File.createTempFile("calibration", ".values4"); 
    		FileOutputStream fileOut = new FileOutputStream(calibrationObjectValues);
    		ObjectOutputStream out = new ObjectOutputStream(fileOut);

    		out.writeObject(calibration);
    		out.close();
    		fileOut.close();
    		
    		pHandler.addFile(calibrationObjectValues, "");             	
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}    	
    	
    	pHandler.saveAndClose();
    }

    public static void saveCalibrationProject(File file, UIController controller) {        
        CalibrationProjectHandler pHandler = new CalibrationProjectHandler();

        try {
            //First Tab    
        	pHandler.newProject(file);

        	//Save calibration files
        	String samplePath = controller.sampleFolderPath.getText();
        	if (!samplePath.endsWith(Constants.CALIBRATION_FILE_EXTENSION)) {
        		for (ObservableList<?> row : controller.calibrationTable.getItems()) {
        			String fileName = ((SimpleStringProperty) row.get(0)).getValue();
        			// Sample must have been loaded before saving since save is disabled until samples loaded.
        			// Therefore last calibration path must not be null and must be the path where samples
        			// files exist.
        			Settings.load();
        			File sampleFile = new File(Settings.lastCalibrationSamplesPath + "/" + samplePath + "/" + fileName);
        			if (sampleFile.exists()) {
        				pHandler.addFile(sampleFile, SAMPLES_DIR);
        			}
        		}
        	} else {
        		if (Constants.AUTOSAVE_ON) {
        			unZipIt(controller.sampleFolderPath.getText(), Settings.getSettingsPath(), controller);
            		File f = new File(Settings.getSettingsPath() + "/" + SAMPLES_DIR);
            		writeToZipAndDelete(f);
        		}
        	}

        	// Some files have more than one sample per file
        	HashMap<String, ArrayList<String>> fileNameFilteredSampleNamesMap = new HashMap<String, ArrayList<String>>();
        	List<List<String>> calibrationData = new ArrayList<>();
            for (ObservableList<?> row : controller.calibrationTable.getItems()) {
                List<String> rowData = new ArrayList<>();
                for (Object cell : row) {
                    if (cell instanceof SimpleBooleanProperty) {
                        rowData.add(((SimpleBooleanProperty) cell).getValue().toString());
                        if (((SimpleBooleanProperty) cell).getValue().toString().equals("true")) {
                        	// remove this condition for testing and if it is decided that filtering samples if calculations
    						// have not been run when saving is a good idea
                        	if (controller.isCalibrationCalculated()) {
                        		if (fileNameFilteredSampleNamesMap.containsKey(rowData.get(0))) {
                            		ArrayList<String> sampleNames = fileNameFilteredSampleNamesMap.get(rowData.get(0));
                            		sampleNames.add(rowData.get(1));
                            		fileNameFilteredSampleNamesMap.put(rowData.get(0), sampleNames);
                            	} else {
                            		ArrayList<String> sampleNames = new ArrayList<String>();
                            		sampleNames.add(rowData.get(1));
                            		fileNameFilteredSampleNamesMap.put(rowData.get(0), sampleNames);
                            	}
                        	}
                        }
                    } else {
                        rowData.add(((SimpleStringProperty) cell).getValue());
                    }
                }
                calibrationData.add(rowData);
            }
            
            File calibrationValues = File.createTempFile("calibrationValues", ".values1");
            FileOutputStream fileOut = new FileOutputStream(calibrationValues);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(calibrationData);
            out.close();
            fileOut.close();
            
            pHandler.addFile(calibrationValues, "");
            
            // remove this condition for testing and if it is decided that filtering samples if calculations
			// have not been run when saving is a good idea
            if (controller.isCalibrationCalculated()) {
            	for (CSVModule csvModule : controller.getFilteredCsvModuleMap().values()) {
                	// Add "filtered_" to filename since Excel will not allow two files of same name to be
            		// opened at same time even in different directories. Filtered files need to be compared
            		// to unfiltered files so both should be able to be opened at same time.
            		String fileName = Settings.getSettingsPath() + File.separatorChar + "filtered_" + csvModule.getFileName();
            		if (fileNameFilteredSampleNamesMap.containsKey(csvModule.getFileName()))
            			csvModule.writeSamples(fileName, fileNameFilteredSampleNamesMap.get(csvModule.getFileName()));
            		
                	File f = new File(fileName);
                	if (f.exists()) {
                		pHandler.addFile(f, FILTERED_SAMPLES_DIR);
                    	f.delete();
                	}
                }
            }

            //Second Tab
            //Build Parameters Tree Table Data
            List<List<List<String>>> treeTableData = null;
            if (controller.treeTableView.getRoot() != null) {
                treeTableData = new ArrayList<>();
                for (TreeItem<ObservableList<String>> row : controller.treeTableView.getRoot().getChildren()) {
                	List<List<String>> featureData = new ArrayList<>();
                	
                    List<String> parentRow = new ArrayList<>();
                    for (String value : row.getValue()) 
                    	parentRow.add(value);
                    
                    featureData.add(parentRow);

                    for (TreeItem<ObservableList<String>> row2 : row.getChildren()) {
                        List<String> childRow = new ArrayList<>();
                        for (String value : row2.getValue())
                        	childRow.add(value);
                        
                        featureData.add(childRow);
                    }
                    
                    treeTableData.add(featureData);
                }
            }

            if (treeTableData != null) {
                File parametersValues = File.createTempFile("paramatersValues", ".values2");
                fileOut = new FileOutputStream(parametersValues);
                out = new ObjectOutputStream(fileOut);
                out.writeObject(treeTableData);
                out.close();
                fileOut.close();
                
                pHandler.addFile(parametersValues, "");
            }

            //Save genotypes
            File genotypeValues = File.createTempFile("genotypes", ".values3");
            fileOut = new FileOutputStream(genotypeValues);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(controller.genotypes);
            out.close();
            fileOut.close();

            pHandler.addFile(genotypeValues, "");

            //Save calibration object data
            if (controller.backendController.getCalibration() != null) {
            	File calibrationObjectValues = File.createTempFile("calibration", ".values4");
            	
            	fileOut = new FileOutputStream(calibrationObjectValues);
                out = new ObjectOutputStream(fileOut);
                out.writeObject(controller.backendController.getCalibration());
                out.close();
                fileOut.close();
                
            	pHandler.addFile(calibrationObjectValues, "");     
            }
            
            // Save calibration curve fit data
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        pHandler.saveAndClose();
    }
    
    public static void loadCalibrationProject(File file, UIController controller) {    	
    	try {
    		String name = file.getName();
    		controller.calibrationName.setText(name.split("\\.")[0]);
    		//controller.sampleFolderPath.setText(file.getAbsolutePath());
    		controller.sampleFolderPath.setText(file.getName());
    		controller.browseSampleFiles.setDisable(true);
    		
    		Project project = loadProjectData(file);
    		controller.setCalibrationCalculated(calibrationCalculated);
    		controller.buildTreeTable(project.getTreeTableData());
    		controller.backendController.setCalibration(project.getCalibration());
    		
    		controller.getCsvModuleMap().clear();

    		ZipFile zipFile = new ZipFile(file.getAbsolutePath());
    		Enumeration<? extends ZipEntry> entries = zipFile.entries();

    		while (entries.hasMoreElements()) {
    			ZipEntry entry = entries.nextElement();
    			InputStream stream = zipFile.getInputStream(entry);

    			if (entry.getName().endsWith(".values1")) {
    				//Calibration values
    				ObjectInputStream in = new ObjectInputStream(stream);
    				List<List<String>> calibrationData = (List<List<String>>) in.readObject();
    				in.close();

    				controller.buildCalibrationTable();

    				for (List<String> row : calibrationData) {
    					ObservableList<Object> data = FXCollections.observableArrayList();
    					data.add(new SimpleStringProperty(row.get(0)));
    					data.add(new SimpleStringProperty(row.get(1)));
    					data.add(new SimpleBooleanProperty(Boolean.parseBoolean(row.get(2))));
    					// if any cell not selected all selected is false
    					if (Boolean.parseBoolean(row.get(2)) == false) {
    						allSelected = false;
    					}
    					data.add(new SimpleStringProperty(row.get(3)));
    					data.add(new SimpleStringProperty(row.get(4)));

    					controller.calibrationTable.getItems().add(data);
    				}
    			} else if (entry.getName().endsWith(".values3")) {
    				//Genotypes values
    				ObjectInputStream in = new ObjectInputStream(stream);
    				controller.genotypes = (TreeMap<String, Genotype>) in.readObject();
    				in.close();
    			} else if (entry.getName().startsWith(SAMPLES_DIR)
    					&& !entry.getName().startsWith(FILTERED_SAMPLES_DIR)) {
    				String fileName = (new File(entry.getName())).getName();
    				CSVModule csvModule = new CSVModule(fileName, new InputStreamReader(stream), project.getCalibration().getKit());
    				controller.getCsvModuleMap().put(fileName, csvModule);
    			}    			
    		} 
    		zipFile.close();
    		
    		for (ObservableList<?> calibrationRow : controller.calibrationTable.getItems()) {
    			String fileName = ((SimpleStringProperty) calibrationRow.get(0)).getValue();
            	String sampleName = ((SimpleStringProperty) calibrationRow.get(1)).getValue();
            	String genotypeID  = ((SimpleStringProperty) calibrationRow.get(4)).getValue();
            	
            	controller.getCsvModuleMap().get(fileName).getSamples().get(sampleName).setGenotype(controller.genotypes.get(genotypeID));
    		}
    	} catch (IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    }

	public static Project loadProjectData(File file) {    	
    	Project project = new Project();    	

    	if (file != null) {
    		try {
    			ZipFile zipFile = new ZipFile(file.getAbsolutePath());
    			Enumeration<? extends ZipEntry> entries = zipFile.entries();

    			while (entries.hasMoreElements()) {
    				ZipEntry entry = entries.nextElement();
    				InputStream stream = zipFile.getInputStream(entry);
    				if (entry.getName().endsWith(".values2")) {
    					//Parameters values
    					ObjectInputStream in = new ObjectInputStream(stream);
    					List<List<List<String>>> treeTableData = (List<List<List<String>>>) in.readObject();
    					calibrationCalculated = wasCalibrationCalculated(treeTableData);
    					in.close();

    					project.setTreeTableData(treeTableData);
    				} else if (entry.getName().endsWith(".values4")) {
    					//Calibration    			
    					ObjectInputStream in = new ObjectInputStream(stream);
    					Calibration calibration = (Calibration) in.readObject();
    					in.close();

    					// Populate locus to value map
    					for (Locus locus : calibration.getLoci())     				
    						new Locus(locus.getName());
                                        calibration.setCalibrationPath(file.getAbsolutePath());

    					calibration.setCalibrationPath(file.getAbsolutePath());
                                        project.setCalibration(calibration);
    				}
    			}

    			zipFile.close();
    		} catch (IOException | ClassNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}

		return project;
    }
    
    /**
     * Unzip it
     * @param zipFile input zip file
     * @param output zip file output folder
     * based on http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
     */
    public static void unZipIt(String zipFile, String outputFolder, UIController controller) {
    	byte[] buffer = new byte[1024];

    	try{

    		//create output directory is not exists
    		File folder = new File(outputFolder);
    		if(!folder.exists()){
    			folder.mkdir();
    		}

    		//get the zip file content
    		ZipInputStream zis = 
    				new ZipInputStream(new FileInputStream(zipFile));
    		//get the zipped file list entry
    		ZipEntry ze = zis.getNextEntry();

    		while(ze!=null){

    			String fileName = ze.getName();
    			if (fileName.endsWith(".csv")) {
    				File newFile = new File(outputFolder + File.separator + fileName);

        			//System.out.println("file unzip : "+ newFile.getAbsoluteFile());

        			//create all non exists folders
        			//else you will hit FileNotFoundException for compressed folder
        			new File(newFile.getParent()).mkdirs();

        			//Save calibration files
        			if (!newFile.isDirectory()) {
        				FileOutputStream fos = new FileOutputStream(newFile);             

            			int len;
            			while ((len = zis.read(buffer)) > 0) {
            				fos.write(buffer, 0, len);
            			}
            			fos.close(); 
        			}
    			}
    			  
    			ze = zis.getNextEntry();
    		}

    		zis.closeEntry();
    		zis.close();
    		
    	} catch (IOException ex) {
    		ex.printStackTrace(); 
    	}
    } 
    
    /**
     * Method used to determine what controls to enable and disable when loading a calibration
     * since calibrations can be saved with or without calculations being run.
     * Uses same logic as UIController buildTreeTable method to determine if calculations
     * have been performed, that is if the r2 value in a leaf equals "-"
     * @param treeTableData
     * @return
     */
    private static boolean wasCalibrationCalculated(List<List<List<String>>> treeTableData) {
    	if (treeTableData.size() > 0 && treeTableData.get(0).size() > 1
				&& treeTableData.get(0).get(1).size() > Constants.TREE_TABLE_COLUMN_R_SQUARED_INDEX) {
			if (treeTableData.get(0).get(1).get(Constants.TREE_TABLE_COLUMN_R_SQUARED_INDEX).equals("-")) {
				return false;
			}
		}
    	
		return true;
    	
    }
    
    /**
     * Writes files to autosave.zip file, then deletes files
     * @param folder
     */
    public static void writeToZipAndDelete(final File folder) {
    	CalibrationProjectHandler pHandler = new CalibrationProjectHandler();
        for (final File fileEntry : folder.listFiles()) {
            // there should not be any directories in the log directory since the directory
        	// is created by Nocit
        	if (fileEntry.isDirectory()) {
                //listFilesForFolder(fileEntry);
            } else {
                File file = new File(fileEntry.getAbsolutePath());
                if (file.exists()) {
        			pHandler.addFile(file, SAMPLES_DIR);
        			try{
                		file.delete();
//                		if(file.delete()){
//                			System.out.println(file.getName() + " is deleted!");
//                		}else{
//                			System.out.println("Delete operation is failed.");
//                		}
                	}catch(Exception e){
                		e.printStackTrace();
                	}
        		}
            }
        }
    }

	public static boolean isCalibrationCalculated() {
		return calibrationCalculated;
	}
	
	public static boolean isAllSelected() {
		return allSelected;
	}
    
}