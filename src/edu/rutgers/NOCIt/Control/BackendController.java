package edu.rutgers.NOCIt.Control;

import static edu.rutgers.NOCIt.UIMain.logger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFMergerUtility;

import edu.rutgers.NOCIt.UIController;
import edu.rutgers.NOCIt.Control.Calibration.Feature;
import edu.rutgers.NOCIt.Data.CSVFileWriter;
import edu.rutgers.NOCIt.Data.CSVModule;
import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.FreqTable;
import edu.rutgers.NOCIt.Data.Genotype;
import edu.rutgers.NOCIt.Data.Kit;
import edu.rutgers.NOCIt.Data.Locus;
import edu.rutgers.NOCIt.Data.Sample;
import edu.rutgers.NOCIt.Data.UtilityMethods;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.WritableImage;

/**
 *
 * @author rcarpenter
 * @author James Kelley
 * @author Desmond Lun
 */
public class BackendController {
    private Calibration calibration = null;
    private UIController uiController;
    private Queue<Thread> nocItThreadQueue = new LinkedList<Thread>();
    private Queue<Thread> ceesItThreadQueue = new LinkedList<Thread>();
    private Map<Integer, BarChart<String, Number>> nocItCharts = new HashMap<>();
    private Map<Integer, BarChart<String, Number>> ceesItCharts = new HashMap<>();
    private int nocItJobsTotal = 0;
    private int nocItJobsComplete = 0;
    // saves files where output names are the same but populations are different
    // so these files can be combined
    private HashMap<String, File> appendFilesMap = new HashMap<String, File>();
    private HashMap<String, File> appendFilesCEESItMap = new HashMap<String, File>();
	private int ceesItJobsTotal = 0;
	private int ceesItJobsComplete = 0;
	private ArrayList<String> unwrittenFiles = new ArrayList<String>();
	private ArrayList<String> failedSamples = new ArrayList<String>();
	private ArrayList<ArrayList<ArrayList<String>>> nocItCsvOutputLinesList = new ArrayList<ArrayList<ArrayList<String>>>();
	private ArrayList<ArrayList<ArrayList<String>>> ceesItCsvOutputLinesList = new ArrayList<ArrayList<ArrayList<String>>>();
	
    public BackendController(UIController controller, ProgressBar nocItProgressBar,
            ProgressBar ceesItProgressBar) {
        this.uiController = controller;
    }

    public void calculateParameters(Collection<CSVModule> csvModules, HashMap<Feature, double[]> initialParams, ProgressBar progressBar) {
        System.out.println("Calib.calPar() called");
        calibration.calculateParameters(csvModules, initialParams, progressBar);
    }

    public void cancelCEESIt() {
        try {
			for (Thread thread : ceesItThreadQueue)
				thread.interrupt();       	      
            this.ceesItThreadQueue.clear();
            
            resetCEESIt();
        } catch (final Exception e) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    logger.error(Constants.CANCEL_CEESIT_ERROR_LOG_MESSAGE, e);
                }
            });
        }
    }

    public void cancelNOCIt() {
        try {
        	for (Thread thread : nocItThreadQueue)
				thread.interrupt();       	 	
            this.nocItThreadQueue.clear();
            
            resetNOCIt();
        } catch (final Exception e) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    logger.error(Constants.CANCEL_NOCIT_ERROR_LOG_MESSAGE, e);
                }
            });
        }
    }
    
    private synchronized void ceesItThreadFinished(final BarChart<String, Number> barChart, final String results, final File outputFile, int rowID) {        
        this.ceesItThreadQueue.remove();
        this.ceesItCharts.put(rowID, barChart);
        ceesItJobsComplete++;
        
        Thread nextThread = ceesItThreadQueue.peek();
        if (nextThread != null)
        	nextThread.start();       

        //Add the Bar Chart to the UI.
        if (barChart != null) {
        	Platform.runLater(new Runnable() {
        		@Override
        		public void run() {
        			uiController.ceesItBarChartArea.getChildren().add(barChart);
        			if (appendFilesCEESItMap.containsKey(outputFile.getAbsolutePath()) &&
    						appendFilesCEESItMap.get(outputFile.getAbsolutePath()).exists()) {
    					File temp = appendFilesCEESItMap.get(outputFile.getAbsolutePath());
    					temp.renameTo(new File(Settings.getSettingsPath() + File.separatorChar + "temp.pdf"));
    					File oldFile = new File(Settings.getSettingsPath() + File.separatorChar + "temp.pdf");
    					PDFMergerUtility ut = new PDFMergerUtility();
    					createCEESItPDFReport(barChart, results, outputFile); 
    					ut.addSource(oldFile);
    					ut.addSource(outputFile);
    					ut.setDestinationFileName(outputFile.getAbsolutePath());
    					try {
    						ut.mergeDocuments();
    						oldFile.delete();
    					} catch (COSVisitorException | IOException e) {
    						logger.error("Error Merging PDF files", e);
    					}
    				} else {
    					createCEESItPDFReport(barChart, results, outputFile); 
    					if (uiController.outputNamesPopulationCEESItMap.get(outputFile.getAbsolutePath()) != null &&
    							uiController.outputNamesPopulationCEESItMap.get(outputFile.getAbsolutePath()).size() > 1) {
    						appendFilesCEESItMap.put(outputFile.getAbsolutePath(), outputFile);
    					}
    				}
        			updateCEESItGraphs();

        		}
        	});
        }

        if (ceesItThreadQueue.isEmpty()) {
            //Stop the progress bar and stop the Time Elapsed timer
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	resetCEESIt();
                }
            });
        }
    }
    
    public void createCEESItPDFReport(BarChart<String, Number> barChart, String results, File outputFile) {
        try {
            PDDocument document = new PDDocument();

            //Create the image from the BarChart node.
            SnapshotParameters parameters = new SnapshotParameters();
            //WritableImage wimage = new WritableImage(1000, 1000);
            WritableImage wimage = barChart.snapshot(parameters, null);
            BufferedImage originalImage = SwingFXUtils.fromFXImage(wimage, null);
            int cropAmount = 30;
            BufferedImage croppedImage = originalImage.getSubimage(0, cropAmount, originalImage.getWidth(), originalImage.getHeight() - cropAmount);
//            File file = File.createTempFile("tempImage", ".temp");
//            RenderedImage renderedImage = SwingFXUtils.fromFXImage(wimage, null);
//            ImageIO.write(renderedImage, "png", file);
//            PDXObjectImage image = new PDPixelMap(document, ImageIO.read(file));
            PDXObjectImage image = new PDPixelMap(document, croppedImage);

            //Create an array of lines by splitting the string according to the newline sequence.
            String[] lines = results.split("\n");

            //Set the font
            PDFont font = PDType1Font.HELVETICA_BOLD;
            //Calculate the font height 
            float fontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * 12;
            //Calculate the page height based on the number of lines of text and the height of the image.
            float pageHeight = ((lines.length + 3) * fontHeight) + image.getHeight() + 100;

            //Add a page to the document
            PDPage page = new PDPage(new PDRectangle(PDPage.PAGE_SIZE_LETTER.getWidth(), pageHeight));
            document.addPage(page);
            // Start a new content stream which will "hold" the to be created content
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            //Populate the content stream with the lines of text
            float y = pageHeight - 100;
            for (String line : lines) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.moveTextPositionByAmount(50, y);
                contentStream.drawString(line);
                contentStream.endText();

                y -= fontHeight;
            }

            //Populate the content stream with the image.
            contentStream.moveTo(50, y);
            contentStream.drawImage(image, 50, y - image.getHeight());

            // Make sure that the content stream is closed:
            contentStream.close();

            // Save the results and ensure that the document is properly closed:
            document.save(outputFile.getAbsolutePath());
            document.close();
        } catch (final Exception e) {
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
                    logger.error(Constants.CREATE_PDF_REPORT_ERROR_LOG_MESSAGE, e);
                    unwrittenFiles.add(outputFile.getAbsolutePath());
//                }
//            });
        }
    }

    public void createNOCItPDFReport(BarChart<String, Number> barChart, String results, File outputFile) {
        try {
            PDDocument document = new PDDocument();

            //Create the image from the BarChart node.
            SnapshotParameters parameters = new SnapshotParameters();
            WritableImage wimage = barChart.snapshot(parameters, null);
            BufferedImage originalImage = SwingFXUtils.fromFXImage(wimage, null);
            int cropAmount = 30;
            BufferedImage croppedImage = originalImage.getSubimage(0, cropAmount, originalImage.getWidth(), originalImage.getHeight() - cropAmount);
//            File file = File.createTempFile("tempImage", ".temp");
//            RenderedImage renderedImage = SwingFXUtils.fromFXImage(wimage, null);
//            ImageIO.write(renderedImage, "png", file);
//            PDXObjectImage image = new PDPixelMap(document, ImageIO.read(file));
            PDXObjectImage image = new PDPixelMap(document, croppedImage);

            //Create an array of lines by splitting the string according to the newline sequence.
            String[] lines = results.split("\n");

            //Set the font
            PDFont font = PDType1Font.HELVETICA_BOLD;
            //Calculate the font height 
            float fontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * 12;
            //Calculate the page height based on the number of lines of text and the height of the image.
            float pageHeight = ((lines.length + 3) * fontHeight) + image.getHeight() + 100;

            //Add a page to the document
            PDPage page = new PDPage(new PDRectangle(PDPage.PAGE_SIZE_LETTER.getWidth(), pageHeight));
            document.addPage(page);
            // Start a new content stream which will "hold" the to be created content
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            //Populate the content stream with the lines of text
            float y = pageHeight - 100;
            for (String line : lines) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.moveTextPositionByAmount(50, y);
                contentStream.drawString(line);
                contentStream.endText();

                y -= fontHeight;
            }

            //Populate the content stream with the image.
            contentStream.moveTo(50, y);
            contentStream.drawImage(image, 50, y - image.getHeight());

            // Make sure that the content stream is closed:
            contentStream.close();

            // Save the results and ensure that the document is properly closed:
            document.save(outputFile.getAbsolutePath());
            document.close();
        } catch (final Exception e) {
        	logger.error(Constants.CREATE_PDF_REPORT_ERROR_LOG_MESSAGE, e);
        	unwrittenFiles.add(outputFile.getAbsolutePath());
        }
    }

    public Calibration getCalibration() {
        return this.calibration;
    }
    
    private synchronized void nocItThreadFinished(final BarChart<String, Number> barChart, final String results, final File outputFile, int rowID) {        
        this.nocItThreadQueue.remove();
        this.nocItCharts.put(rowID, barChart);
        nocItJobsComplete++;
        
        Thread nextThread = nocItThreadQueue.peek();
        if (nextThread != null)
        	nextThread.start();       

    	Platform.runLater(new Runnable() {
    		@Override
    		public void run() {
    			//Add the Bar Chart to the UI.
    			if (barChart != null) {
    				uiController.nocItBarChartArea.getChildren().add(barChart);
    				if (appendFilesMap.containsKey(outputFile.getAbsolutePath()) &&
    						appendFilesMap.get(outputFile.getAbsolutePath()).exists()) {
    					File temp = appendFilesMap.get(outputFile.getAbsolutePath());
    					temp.renameTo(new File(Settings.getSettingsPath() + File.separatorChar + "temp.pdf"));
    					File oldFile = new File(Settings.getSettingsPath() + File.separatorChar + "temp.pdf");
    					PDFMergerUtility ut = new PDFMergerUtility();
    					createNOCItPDFReport(barChart, results, outputFile); 
    					ut.addSource(oldFile);
    					ut.addSource(outputFile);
    					ut.setDestinationFileName(outputFile.getAbsolutePath());
    					try {
    						ut.mergeDocuments();
    						oldFile.delete();
    					} catch (COSVisitorException | IOException e) {
    						logger.error("Error Merging PDF files", e);
    					}
    				} else {
    					createNOCItPDFReport(barChart, results, outputFile); 
    					if (uiController.outputNamesPopulationMap.get(outputFile.getAbsolutePath()) != null &&
    							uiController.outputNamesPopulationMap.get(outputFile.getAbsolutePath()).size() > 1) {
    						appendFilesMap.put(outputFile.getAbsolutePath(), outputFile);
    					}
    				}
    				updateNOCItGraphs();
    			}      
    			
    			if (nocItThreadQueue.isEmpty()) {
    	            //Stop the progress bar and stop the Time Elapsed timer
    				resetNOCIt();
    			}
    		}
    	});
    }

    public LineChart<Number, Number> plotGraph(String label, Locus locus) {
        if (calibration != null) {
            return calibration.plotGraph(label, locus);
        }
        logger.error(Constants.PLOT_GRAPH_ERROR_MESSAGE_LOG);
        return null;
    }

    private void refreshSettingsDialog() {
		if (uiController.getSettingsController() != null) {
			uiController.getSettingsController().refresh();
		}
	}

    private void resetCEESIt() {
        uiController.ceesItTimeElapsedTimer.stop();
        uiController.disableItemsWhileCEESItRunning(false);
        uiController.ceesItProgressBar.setProgress(0);
        
        ceesItJobsTotal = 0;
        ceesItJobsComplete = 0;
        
        if (Settings.lastCEESItWriteCSVSelection.equals("true")) {
        	writeCeesItCSVOutputFile(ceesItCsvOutputLinesList);
        }
        ceesItCsvOutputLinesList.clear();
        
        if (unwrittenFiles.size() > 0 || failedSamples.size() > 0) {
        	showErrorMessageIfErrorsOccurred();
        }
        refreshSettingsDialog();
    }

    private void resetNOCIt() {
        uiController.nocItTimeElapsedTimer.stop();
        uiController.disableItemsWhileNOCItRunning(false);
        uiController.nocItProgressBar.setProgress(0);
        
        nocItJobsTotal = 0;
        nocItJobsComplete = 0;
        
        if (Settings.lastWriteCSVSelection.equals("true")) {
        	writeNocItCSVOutputFile(nocItCsvOutputLinesList);
        }
        nocItCsvOutputLinesList.clear();
        
        if (unwrittenFiles.size() > 0 || failedSamples.size() > 0) {
        	showErrorMessageIfErrorsOccurred();
        }
        refreshSettingsDialog();
    }     
    
    public synchronized void runCEESIt(final Calibration calibration, String sampleID, Sample sample, final File outputFile,
            final int noc, final Genotype poiGenotype, final List<Genotype> knownGenotypes, final int rowID,
            final FreqTable freqTable, HashMap<Locus,Integer> analyticalThresholds) {
        final BackendController backendController = this;
        Thread ceesItThread = new Thread() {
            public synchronized void run() {
                synchronized (uiController) {
                    try {
                        CEESIt ceesIt = new CEESIt(calibration);
                        ceesIt.runCEESIt(sampleID, sample, noc, poiGenotype, knownGenotypes, freqTable, analyticalThresholds,
                        		outputFile.getAbsolutePath(), backendController);
                        backendController.ceesItThreadFinished(ceesIt.graphBarChart(outputFile.getName()), 
                        		ceesIt.getResultsString(analyticalThresholds), outputFile, rowID);
                        ceesItCsvOutputLinesList.add(ceesIt.getCsvOutputLines());
                    } catch (InterruptedException e) {
                    	// Thread interrupted; most probably because Cancel button was pressed
                    } catch (final Exception e) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                            	backendController.ceesItThreadFinished(null, null, outputFile, rowID);
                                logger.error(Constants.RUN_CEESIT_THREAD_ERROR_LOG_MESSAGE, e);
                                failedSamples.add(sampleID);
                            }
                        });

                    }
                }
            }
        };

        if (ceesItThreadQueue.isEmpty())
        	ceesItThread.start();
        
        ceesItThreadQueue.add(ceesItThread);
        ceesItJobsTotal++;
    }

    public synchronized void runNOCIt(final Calibration calibration, String sampleID, Sample sample,
            final File outputFile, final int maxNOC, final int rowID, final FreqTable freqTable, HashMap<Locus,Integer> analyticalThesholds) {
    	final BackendController backendController = this;
        Thread nocItThread = new Thread() {
            public synchronized void run() {
                synchronized (uiController) {
                    try {
                        NOCIt nocIt = new NOCIt(calibration);
                        nocIt.runNOCIt(sampleID, sample, maxNOC, freqTable, analyticalThesholds, outputFile.getAbsolutePath(), backendController);                        
                        backendController.nocItThreadFinished(nocIt.graphBarChart(outputFile.getName()), 
                        		nocIt.getResultsString(analyticalThesholds), outputFile, rowID);
                        nocItCsvOutputLinesList.add(nocIt.getCsvOutputLines());
                    } catch (InterruptedException e) {
                    	// Thread interrupted; most probably because Cancel button was pressed
                    } catch (final Exception e) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                            	backendController.nocItThreadFinished(null, null, outputFile, rowID);
                            	failedSamples.add(sampleID);
                                logger.error(Constants.RUN_NOCIT_THREAD_ERROR_LOG_MESSAGE, e);
                            }
                        });

                    }
                }
            }
        };
        
        if (nocItThreadQueue.isEmpty())
        	nocItThread.start();
        
        nocItThreadQueue.add(nocItThread);
        nocItJobsTotal++;
    }

    public void setCalibration(Calibration calibration) {
        this.calibration = calibration;
    }
    
    public void setKit(Kit kit) {
        calibration = new Calibration(kit);
    }
    
    private void showErrorMessageIfErrorsOccurred() {
    	String message = "";
    	if (failedSamples.size() > 0) {
    		message += UtilityMethods.errorMessageFromList(Constants.NOCIT_SAMPLES_UNABLE_TO_RUN_ERROR, failedSamples);
    	}
    	if (unwrittenFiles.size() > 0) {
    		message += UtilityMethods.errorMessageFromList(Constants.NOCIT_PDFS_NOT_WRITTEN_ERROR, unwrittenFiles);
    	}
    	
    	UtilityMethods.showErrorMessage(uiController.getStage(), message);
		
		unwrittenFiles.clear();
        failedSamples.clear();
    }
    
    public void updateCEESItGraphs() {
        uiController.ceesItBarChartArea.getChildren().clear();
        
        for (ObservableList<?> row : uiController.ceesItTable.getItems()) {
        	boolean graph = ((SimpleBooleanProperty) row.get(Constants.CEESIT_TABLE_COLUMN_GRAPH_INDEX)).getValue();
        	int rowID = ((SimpleIntegerProperty) row.get(Constants.CEESIT_TABLE_COLUMN_ROW_ID_INDEX)).getValue();
        	
        	if (graph && ceesItCharts.get(rowID) != null)
        		uiController.ceesItBarChartArea.getChildren().add(ceesItCharts.get(rowID));
        	
        }
    }
    
    public void updateCEESItProgress(double progress) {
    	uiController.ceesItProgressBar.setProgress((ceesItJobsComplete + progress) / ceesItJobsTotal);
    }

	public void updateNOCItGraphs() {
        uiController.nocItBarChartArea.getChildren().clear();
        
        for (ObservableList<?> row : uiController.nocItTable.getItems()) {
        	boolean graph = ((SimpleBooleanProperty) row.get(Constants.NOCIT_TABLE_COLUMN_GRAPH_INDEX)).getValue();
        	int rowID = ((SimpleIntegerProperty) row.get(Constants.NOCIT_TABLE_COLUMN_ROW_ID_INDEX)).getValue();
        	
        	if (graph && nocItCharts.get(rowID) != null)
        		uiController.nocItBarChartArea.getChildren().add(nocItCharts.get(rowID));
        	
        }
    }

	public void updateNOCItProgress(double progress) {
    	uiController.nocItProgressBar.setProgress((nocItJobsComplete + progress) / nocItJobsTotal);
    } 

	private void writeCeesItCSVOutputFile(ArrayList<ArrayList<ArrayList<String>>> ceesItCsvOutputLinesList) {
    	File file = new File(uiController.ceesItCSVOutputFilePath);
    	if (file.exists()) {
    		file = new File(UtilityMethods.createFilenameIfFileExists(uiController.ceesItCSVOutputFilePath));
    	}
    	UtilityMethods.createFilenameIfFileExists(uiController.ceesItCSVOutputFilePath);
    	ArrayList<String []> lines = new ArrayList<String []>();

    	for (int i = 0; i < ceesItCsvOutputLinesList.size(); i++) {
    		for (int j = 0; j < ceesItCsvOutputLinesList.get(i).size(); j++) {
    			String line = "";
    			for (int k = 0; k < ceesItCsvOutputLinesList.get(i).get(j).size(); k++) {	
    				line += ceesItCsvOutputLinesList.get(i).get(j).get(k) + "\t";
    			}
    			String [] entries = line.substring(0, line.length() - 1).split("\t");
    			lines.add(entries);
    		}
    	}
    	String headerNames = "";
		for (int j = 0; j < Constants.CEESIT_CSV_OUTPUT_FILE_HEADER.length; j++) {
			headerNames += Constants.CEESIT_CSV_OUTPUT_FILE_HEADER[j] + "\t";
		}
		for (int k = 0; k < Settings.numBins; k++) {
			headerNames += Constants.CEESIT_CSV_OUTPUT_FILE_HEADER_BIN_PREFIX + (k + 1) + "\t";
			headerNames += Constants.CEESIT_CSV_OUTPUT_FILE_HEADER_FREQUENCY_COLUMN_NAME + "\t";
		}
		for (int j = 0; j < Constants.CEESIT_CSV_OUTPUT_FILE_HEADER2.length; j++) {
			headerNames += Constants.CEESIT_CSV_OUTPUT_FILE_HEADER2[j] + "\t";
		}
    	CSVFileWriter.write(file.getAbsolutePath(), headerNames, lines);

    }

	private void writeNocItCSVOutputFile(ArrayList<ArrayList<ArrayList<String>>> nocItCsvOutputLinesList) {
    	File file = new File(uiController.nocItCSVOutputFilePath);
    	if (file.exists()) {
    		file = new File(UtilityMethods.createFilenameIfFileExists(uiController.nocItCSVOutputFilePath));
    	}
    	ArrayList<String []> lines = new ArrayList<String []>();

    	for (int i = 0; i < nocItCsvOutputLinesList.size(); i++) {
    		for (int j = 0; j < nocItCsvOutputLinesList.get(i).size(); j++) {
    			String line = "";
    			for (int k = 0; k < nocItCsvOutputLinesList.get(i).get(j).size(); k++) {	
    				line += nocItCsvOutputLinesList.get(i).get(j).get(k) + "\t";
    			}
    			String[] entries = line.substring(0, line.length() - 1).split("\t");
    			lines.add(entries);
    		}
    	}
    	String headerNames = String.join("\t", Constants.NOCIT_CSV_OUTPUT_FILE_HEADER_1);
    	for (int j = 0; j <= Constants.NOCIT_MAX_NOC_CHOICE; j++) {
    		headerNames += "\tLog Likelihood n=" + j;
    		headerNames += "\tAPP n=" + j;
    	}
    	headerNames += "\t" + String.join("\t", Constants.NOCIT_CSV_OUTPUT_FILE_HEADER_2);

    	CSVFileWriter.write(file.getAbsolutePath(), headerNames, lines);
    }
}
