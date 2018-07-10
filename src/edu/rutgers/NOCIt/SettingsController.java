package edu.rutgers.NOCIt;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import edu.rutgers.NOCIt.Control.Settings;
import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.UtilityMethods;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

/**
 * FXML Controller class
 * @author rob
 * @author James Kelley
 */
public class SettingsController implements Initializable {
	
	/** The tab pane. */
	@FXML
	private TabPane tabPane;
	
	@FXML
    private Tab tabGeneralSettings;
	@FXML
    private Tab tabFilterSettings;
	@FXML
    private Tab tabNOCItSettings;
	@FXML
    private Tab tabCEESItSettings;
	
	// General
//	@FXML
//	private TextField defaultDirectoryField;
	@FXML
	private TextField sampleIDDelimiterField;
	@FXML
	private TextField genotypeIDIndexField;
	@FXML
	private ChoiceBox<Integer> maxNumberProcessorsField;
	@FXML
	private CheckBox plotAllPointsCheckBox;
	@FXML
	private TextField maxNumberPointsField;
	@FXML
	private Label sampleIDDelimiterLabel;
	@FXML
	private Label genotypeIDIndexLabel;
	@FXML
	private Label maxNumberProcessorsLabel;
	@FXML
	private Label plotAllPointsLabel;
	@FXML
	private Label maxNumberPointsLabel;
	@FXML
	private Button resetToDefaultGeneralButton;
	@FXML
	private Button settingsSaveCloseGeneralButton;
	
	// Filter
	@FXML
	private TextField pullUpHeightRatioField;
	@FXML
	private TextField pullUpSizeRangeField;
	@FXML
	private TextField complexPullUpHeightRatioField;
	@FXML
	private TextField complexPullUpSisterHeightRatioField;	
	@FXML
	private TextField complexPullUpSizeRangeField;
	@FXML
	private TextField minusAPeakHeightRatioField;
	@FXML
	private TextField minusAPeakSizeRangeField;
	@FXML
	private Label pullUpHeightRatioLabel;
	@FXML
	private Label pullUpSizeRangeLabel;
	@FXML
	private Label complexPullUpHeightRatioLabel;
	@FXML
	private Label complexPullUpSisterHeightRatioLabel;	
	@FXML
	private Label complexPullUpSizeRangeLabel;
	@FXML
	private Label minusAPeakHeightRatioLabel;
	@FXML
	private Label minusAPeakSizeRangeLabel;
	@FXML
	private Button resetToDefaultFilterButton;
	@FXML
	private Button settingsSaveCloseFilterButton;
	
	// NOCIt
	@FXML
	private TextField thetaNumLevelsField;
	@FXML
	private TextField nocItStdErrorTolField;
	@FXML
	private TextField nocItTimeLimitField;
	@FXML
	private TextField numSamples1Field;
	@FXML
	private TextField numSamplesIncField;
	@FXML
	private TextField maxNumSamplesField;
	@FXML
	private Label thetaNumLevelsLabel;
	@FXML
	private Label nocItStdErrorTolLabel;
	@FXML
	private Label nocItTimeLimitLabel;
	@FXML
	private Label numSamples1Label;
	@FXML
	private Label multFactorLabel;
	@FXML
	private Label maxSamplesLabel;
	@FXML
	private Button resetToDefaultNOCItButton;
	@FXML
	private Button settingsSaveCloseNOCItButton;
	
	// CEESIt
	@FXML
	private TextField thetaNumLevelsCEESItField;
	@FXML
	private TextField binWidthField;
	@FXML
	private TextField numBinsField;
	@FXML
	private TextField numSamples1CEESItField;
	@FXML
	private TextField numSamplesIncCEESItField;
	@FXML
	private TextField poiSamplesField;
	@FXML
	private TextField genotypeToleranceField;
	@FXML
	private TextField popSubstructureAdjField;
	@FXML
	private Label thetaNumLevelsCEESItLabel;
	@FXML
	private Label binWidthLabel;
	@FXML
	private Label numBinsLabel;
	@FXML
	private Label numSamples1CEESItLabel;
	@FXML
	private Label multFactorCEESItLabel;
	@FXML
	private Label poiSamplesLabel;
	@FXML
	private Label genotypeToleranceLabel;
	@FXML
	private Label popSubstructureAdjLabel;
	@FXML
	private Button resetToDefaultCEESItButton;
	@FXML
	private Button settingsSaveCloseCEESItButton;
	
	ObservableList<Integer> numProcessorsList = FXCollections.observableArrayList();
	
	private String oldSampleIDDelimiter = "";
	private int olsGenotypeIDIndex = 0;
	
	private UIController uiController;
	
	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		for (int i = 1; i <= Settings.defaultMaxNumberProcessors; i++) {
			numProcessorsList.add(i);
		}
		maxNumberProcessorsField.setItems(numProcessorsList);
		maxNumberProcessorsField.getSelectionModel().select(numProcessorsList.indexOf(Settings.numProcessors));

		sampleIDDelimiterLabel.setTooltip(new Tooltip(Constants.SAMPLE_ID_DELIMITER_LABEL_TOOLTIP));
		genotypeIDIndexLabel.setTooltip(new Tooltip(Constants.GENOTYPE_ID_INDEX_LABEL_TOOLTIP));
		maxNumberProcessorsLabel.setTooltip(new Tooltip(Constants.MAX_NUM_PROCESSORS_LABEL_TOOLTIP));
		plotAllPointsLabel.setTooltip(new Tooltip(Constants.PLOT_ALL_CALIBRATION_POINTS_LABEL_TOOLTIP));
		maxNumberPointsLabel.setTooltip(new Tooltip(Constants.CALIBRATION_GRAPH_MAX_POINTS_LABEL_TOOLTIP));
		resetToDefaultGeneralButton.setTooltip(new Tooltip(Constants.RESET_DEFAULT_GENERAL_BUTTON_TOOLTIP));
		settingsSaveCloseGeneralButton.setTooltip(new Tooltip(Constants.SAVE_CLOSE_GENERAL_BUTTON_TOOLTIP));
		
		pullUpHeightRatioLabel.setTooltip(new Tooltip(Constants.PULL_UP_HEIGHT_RATIO_LABEL_TOOLTIP));
		pullUpSizeRangeLabel.setTooltip(new Tooltip(Constants.PULL_UP_SIZE_RANGE_LABEL_TOOLTIP));
		complexPullUpHeightRatioLabel.setTooltip(new Tooltip(Constants.COMPLEX_PULL_UP_HEIGHT_RATIO_LABEL_TOOLTIP));
		complexPullUpSisterHeightRatioLabel.setTooltip(new Tooltip(Constants.COMPLEX_PULL_UP_SISTER_HEIGHT_RATIO_LABEL_TOOLTIP));	
		complexPullUpSizeRangeLabel.setTooltip(new Tooltip(Constants.COMPLEX_PULL_UP_SIZE_RANGE_LABEL_TOOLTIP));
		minusAPeakHeightRatioLabel.setTooltip(new Tooltip(Constants.MINUS_A_HEIGHT_RATIO_LABEL_TOOLTIP));
		minusAPeakSizeRangeLabel.setTooltip(new Tooltip(Constants.MINUS_A_SIZE_RANGE_LABEL_TOOLTIP));
		resetToDefaultFilterButton.setTooltip(new Tooltip(Constants.RESET_DEFAULT_FILTER_BUTTON_TOOLTIP));
		settingsSaveCloseFilterButton.setTooltip(new Tooltip(Constants.SAVE_CLOSE_FILTER_BUTTON_TOOLTIP));
		
		thetaNumLevelsLabel.setTooltip(new Tooltip(Constants.LEVELS_FOR_MIXTURE_RATIOS_LABEL_TOOLTIP));
		nocItStdErrorTolLabel.setTooltip(new Tooltip(Constants.STD_ERROR_TOLERANCE_LABEL_TOOLTIP));
		nocItTimeLimitLabel.setTooltip(new Tooltip(Constants.REFINEMENT_TIME_LIMIT_LABEL_TOOLTIP));
		numSamples1Label.setTooltip(new Tooltip(Constants.NUM_SAMPLES_IN_BATCH_FOR_NOC_1_LABEL_TOOLTIP));
		multFactorLabel.setTooltip(new Tooltip(Constants.MULTIPLICATIVE_FACTOR_LABEL_TOOLTIP));
 
		maxSamplesLabel.setTooltip(new Tooltip(Constants.MAX_SAMPLES_TOOLTIP));
		
		resetToDefaultNOCItButton.setTooltip(new Tooltip(Constants.RESET_DEFAULT_NOCIt_BUTTON_TOOLTIP));
		settingsSaveCloseNOCItButton.setTooltip(new Tooltip(Constants.SAVE_CLOSE_NOCIt_BUTTON_TOOLTIP));
		
		thetaNumLevelsCEESItLabel.setTooltip(new Tooltip(Constants.LEVELS_FOR_MIXTURE_RATIOS_LABEL_TOOLTIP));
		binWidthLabel.setTooltip(new Tooltip(Constants.BIN_WIDTH_LABEL_TOOLTIP));
		numBinsLabel.setTooltip(new Tooltip(Constants.NUM_BINS_LABEL_TOOLTIP));
		numSamples1CEESItLabel.setTooltip(new Tooltip(Constants.NUM_SAMPLES_IN_BATCH_FOR_NOC_1_LABEL_TOOLTIP));
		multFactorCEESItLabel.setTooltip(new Tooltip(Constants.MULTIPLICATIVE_FACTOR_LABEL_TOOLTIP));
		poiSamplesLabel.setTooltip(new Tooltip(Constants.POI_SAMPLES_LABEL_TOOLTIP));
		genotypeToleranceLabel.setTooltip(new Tooltip(Constants.GENOTYPE_TOLERANCE_LABEL_TOOLTIP));
		popSubstructureAdjLabel.setTooltip(new Tooltip(Constants.POP_SUBSTRUCTURE_ADJ_LABEL_TOOLTIP));
		
		resetToDefaultCEESItButton.setTooltip(new Tooltip(Constants.RESET_DEFAULT_CEESIt_BUTTON_TOOLTIP));
		settingsSaveCloseCEESItButton.setTooltip(new Tooltip(Constants.SAVE_CLOSE_CEESIt_BUTTON_TOOLTIP));
		
		Settings.load();
//		defaultDirectoryField.setText(Settings.defaultDirectory);
		pullUpHeightRatioField.setText(Double.toString(Settings.pullUpHeightPct));
		pullUpSizeRangeField.setText(Double.toString(Settings.pullUpSizeRange));
		complexPullUpHeightRatioField.setText(Double.toString(Settings.complexPullUpHeightPct));
		complexPullUpSisterHeightRatioField.setText(Double.toString(Settings.complexPullUpSisterHeightPct));		
		complexPullUpSizeRangeField.setText(Double.toString(Settings.complexPullUpSizeRange));
		minusAPeakHeightRatioField.setText(Double.toString(Settings.minusAHeightPct));
		minusAPeakSizeRangeField.setText(Double.toString(Settings.minusASizeRange));
		sampleIDDelimiterField.setText(Settings.sampleIDDelimiter);
		genotypeIDIndexField.setText(Integer.toString(Settings.genotypeIDIndex));
		plotAllPointsCheckBox.setSelected(Settings.defaultPlotAllCalibrationGraphPointsValue);
		if (Settings.plotAllCalibrationGraphPoints!= null) {
			if (Settings.plotAllCalibrationGraphPoints.equals("true")) {
				plotAllPointsCheckBox.setSelected(true);
			} else if (Settings.plotAllCalibrationGraphPoints.equals("false")) {
				plotAllPointsCheckBox.setSelected(false);
			}
		}
		maxNumberPointsField.setText(Integer.toString(Settings.calibrationGraphMaxNumPoints));
		maybeEnableNumPointsField();
		
		oldSampleIDDelimiter = Settings.sampleIDDelimiter;
		olsGenotypeIDIndex = Settings.genotypeIDIndex;
		
		thetaNumLevelsField.setText(Integer.toString(Settings.thetaNumLevels));
		nocItStdErrorTolField.setText(Double.toString(Settings.nocItStdErrorTol));
		int timeLimit = Settings.nocItTimeLimit;
		nocItTimeLimitField.setText(Integer.toString((int) (timeLimit/1000)));
		numSamples1Field.setText(Integer.toString(Settings.numSamples1));
		numSamplesIncField.setText(Double.toString(Settings.numSamplesInc));
		maxNumSamplesField.setText(Integer.toString(Settings.maxNumSamples));
		
		thetaNumLevelsCEESItField.setText(Integer.toString(Settings.thetaNumLevelsCEESIt));
		binWidthField.setText(Double.toString(Settings.binWidthFactor));
		numBinsField.setText(Integer.toString(Settings.numBins));
		numSamples1CEESItField.setText(Integer.toString(Settings.numSamples1CEESIt));
		numSamplesIncCEESItField.setText(Double.toString(Settings.numSamplesIncCEESIt));
		poiSamplesField.setText(Long.toString(Settings.numberPOISamples));
		genotypeToleranceField.setText(Double.toString(Settings.genotypeTolerance));
		popSubstructureAdjField.setText(Double.toString(Settings.popSubstructureAdj));
	
		// Field validation
		
		// General tab
		UIController.addTextFieldFocusListener(sampleIDDelimiterField);  // maybe needs a length check?
		UIController.addTextFieldFocusListener(genotypeIDIndexField);
		UIController.addTextFieldIntegerValidator(genotypeIDIndexField, Integer.toString(Settings.genotypeIDIndex), 
				Settings.GENOTYPE_ID_INDEX_MIN_VALUE, Settings.GENOTYPE_ID_INDEX_MAX_VALUE);
		UIController.addTextFieldFocusListener(maxNumberPointsField);
		UIController.addTextFieldIntegerValidator(maxNumberPointsField, Integer.toString(Settings.calibrationGraphMaxNumPoints), 
				Settings.CALIBRATION_GRAPH_MAX_POINTS_MIN_VALUE, Settings.CALIBRATION_GRAPH_MAX_POINTS_MAX_VALUE);
		
		// Filter tab
		UIController.addTextFieldFocusListener(pullUpHeightRatioField);	
		UIController.addTextFieldDoubleValidator(pullUpHeightRatioField, Double.toString(Settings.pullUpHeightPct), 
				Settings.PULL_UP_HEIGHT_RATIO_MIN_VALUE, Settings.PULL_UP_HEIGHT_RATIO_MAX_VALUE);
		UIController.addTextFieldFocusListener(pullUpSizeRangeField);
		UIController.addTextFieldDoubleValidator(pullUpSizeRangeField, Double.toString(Settings.pullUpSizeRange), 
				Settings.PULL_UP_SIZE_RANGE_MIN_VALUE, Settings.PULL_UP_SIZE_RANGE_MAX_VALUE);
		UIController.addTextFieldFocusListener(complexPullUpHeightRatioField);	
		UIController.addTextFieldDoubleValidator(complexPullUpHeightRatioField, Double.toString(Settings.complexPullUpHeightPct), 
				Settings.COMPLEX_PULL_UP_HEIGHT_RATIO_MIN_VALUE, Settings.COMPLEX_PULL_UP_HEIGHT_RATIO_MAX_VALUE);
		UIController.addTextFieldFocusListener(complexPullUpSisterHeightRatioField);	
		UIController.addTextFieldDoubleValidator(complexPullUpSisterHeightRatioField, Double.toString(Settings.complexPullUpSisterHeightPct), 
				Settings.COMPLEX_PULL_UP_SISTER_HEIGHT_RATIO_MIN_VALUE, Settings.COMPLEX_PULL_UP_SISTER_HEIGHT_RATIO_MAX_VALUE);
		UIController.addTextFieldFocusListener(complexPullUpSizeRangeField);
		UIController.addTextFieldDoubleValidator(complexPullUpSizeRangeField, Double.toString(Settings.complexPullUpSizeRange), 
				Settings.COMPLEX_PULL_UP_SIZE_RANGE_MIN_VALUE, Settings.COMPLEX_PULL_UP_SIZE_RANGE_MAX_VALUE);
		UIController.addTextFieldFocusListener(minusAPeakHeightRatioField);
		UIController.addTextFieldDoubleValidator(minusAPeakHeightRatioField, Double.toString(Settings.minusAHeightPct), 
				Settings.MINUS_A_HEIGHT_RATIO_MIN_VALUE, Settings.MINUS_A_HEIGHT_RATIO_MAX_VALUE);
		UIController.addTextFieldFocusListener(minusAPeakSizeRangeField);
		UIController.addTextFieldDoubleValidator(minusAPeakSizeRangeField, Double.toString(Settings.minusASizeRange), 
				Settings.MINUS_A_SIZE_RANGE_MIN_VALUE, Settings.MINUS_A_SIZE_RANGE_MAX_VALUE);
		
		// NOCIt tab
		UIController.addTextFieldFocusListener(thetaNumLevelsField);
		UIController.addTextFieldIntegerValidator(thetaNumLevelsField, Integer.toString(Settings.thetaNumLevels), 
				Settings.LEVELS_FOR_MIXTURE_RATIOS_MIN_VALUE, Settings.LEVELS_FOR_MIXTURE_RATIOS_MAX_VALUE);
		UIController.addTextFieldFocusListener(nocItStdErrorTolField);
		UIController.addTextFieldDoubleValidator(nocItStdErrorTolField, Double.toString(Settings.nocItStdErrorTol), 
				Settings.STD_ERROR_TOLERANCE_MIN_VALUE, Settings.STD_ERROR_TOLERANCE_MAX_VALUE);
		UIController.addTextFieldFocusListener(nocItTimeLimitField);
		UIController.addTimeFieldIntegerValidator(nocItTimeLimitField, Integer.toString(Settings.nocItTimeLimit), 
				Settings.REFINEMENT_TIME_LIMIT_MIN_VALUE, Settings.REFINEMENT_TIME_LIMIT_MAX_VALUE);
		UIController.addTextFieldFocusListener(numSamples1Field);
		UIController.addTextFieldIntegerValidator(numSamples1Field, Integer.toString(Settings.numSamples1), 
				Settings.NUM_SAMPLES_IN_BATCH_FOR_NOC_1_MIN_VALUE, Settings.NUM_SAMPLES_IN_BATCH_FOR_NOC_1_MAX_VALUE);
		UIController.addTextFieldFocusListener(numSamplesIncField);
		UIController.addTextFieldDoubleValidator(numSamplesIncField, Double.toString(Settings.numSamplesInc), 
				Settings.MULTIPLICATIVE_FACTOR_MIN_VALUE, Settings.MULTIPLICATIVE_FACTOR_MAX_VALUE);
		UIController.addTextFieldFocusListener(maxNumSamplesField);
		UIController.addTextFieldIntegerValidator(maxNumSamplesField, Integer.toString(Settings.maxNumSamples), 
				Settings.MAX_NUM_SAMPLES_IN_BATCH_MIN_VALUE, Settings.MAX_NUM_SAMPLES_IN_BATCH_MAX_VALUE);
		
		// CEESIt tab
		UIController.addTextFieldFocusListener(thetaNumLevelsCEESItField);
		UIController.addTextFieldIntegerValidator(thetaNumLevelsCEESItField, Integer.toString(Settings.thetaNumLevelsCEESIt), 
				Settings.LEVELS_FOR_MIXTURE_RATIOS_MIN_VALUE, Settings.LEVELS_FOR_MIXTURE_RATIOS_MAX_VALUE);
		UIController.addTextFieldFocusListener(binWidthField);
		UIController.addTextFieldDoubleValidator(binWidthField, Double.toString(Settings.binWidthFactor), 
				Settings.BIN_WIDTH_MIN_VALUE, Settings.BIN_WIDTH_MAX_VALUE);
		UIController.addTextFieldFocusListener(numBinsField);
		UIController.addTextFieldIntegerValidator(numBinsField, Integer.toString(Settings.numBins), 
				Settings.NUM_BINS_MIN_VALUE, Settings.NUM_BINS_MAX_VALUE);
		UIController.addTextFieldFocusListener(numSamples1CEESItField);
		UIController.addTextFieldIntegerValidator(numSamples1CEESItField, Integer.toString(Settings.numSamples1CEESIt), 
				Settings.NUM_SAMPLES_IN_BATCH_FOR_NOC_1_CEESIT_MIN_VALUE, Settings.NUM_SAMPLES_IN_BATCH_FOR_NOC_1_CEESIT_MAX_VALUE);
		UIController.addTextFieldFocusListener(numSamplesIncCEESItField);
		UIController.addTextFieldDoubleValidator(numSamplesIncCEESItField, Double.toString(Settings.numSamplesIncCEESIt), 
				Settings.MULTIPLICATIVE_FACTOR_CEESIT_MIN_VALUE, Settings.MULTIPLICATIVE_FACTOR_CEESIT_MAX_VALUE);
		UIController.addTextFieldFocusListener(poiSamplesField);
		UIController.addTextFieldLongValidator(poiSamplesField, Long.toString(Settings.numberPOISamples), 
				Settings.POI_SAMPLES_MIN_VALUE, Settings.POI_SAMPLES_MAX_VALUE);
		UIController.addTextFieldFocusListener(genotypeToleranceField);
		UIController.addTextFieldDoubleValidator(genotypeToleranceField, Double.toString(Settings.genotypeTolerance), 
				Settings.GENOTYPE_TOLERANCE_MIN_VALUE, Settings.GENOTYPE_TOLERANCE_MAX_VALUE);
		UIController.addTextFieldFocusListener(popSubstructureAdjField);
		UIController.addTextFieldDoubleValidator(popSubstructureAdjField, Double.toString(Settings.popSubstructureAdj), 
				Settings.POP_SUBSTRUCTURE_ADJ_MIN_VALUE, Settings.POP_SUBSTRUCTURE_ADJ_MAX_VALUE);
		
		// tab listener is used to refresh the display after NOCIt or CEESIt timer stops
		tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
			disableControls(uiController.isTimerRunning());
		});
	} 

	@FXML
	private void resetToDefaultGeneral(ActionEvent event) {
//		defaultDirectoryField.setText(Settings.defaultDefaultDirectory);
		sampleIDDelimiterField.setText(Settings.defaultSampleIDDelimiter);
		genotypeIDIndexField.setText(Integer.toString(Settings.defaultGenotypeIDIndex));
		
		maxNumberProcessorsField.getSelectionModel().select(numProcessorsList.indexOf(Settings.defaultMaxNumberProcessors));
		plotAllPointsCheckBox.setSelected(Settings.defaultPlotAllCalibrationGraphPointsValue);
		maxNumberPointsField.setText(Integer.toString(Settings.defaultCalibrationGraphMaxNumPoints));
		
//		defaultDirectoryField.setTooltip(new Tooltip(defaultDirectoryField.getText()));
	}
	
	@FXML
	private void resetToDefaultFilter(ActionEvent event) {
		pullUpHeightRatioField.setText(Double.toString(Settings.defaultPullUpHeightPct));
		pullUpSizeRangeField.setText(Double.toString(Settings.defaultPullUpSizeRange));
		complexPullUpHeightRatioField.setText(Double.toString(Settings.defaultComplexPullUpHeightPct));
		complexPullUpSisterHeightRatioField.setText(Double.toString(Settings.defaultComplexPullUpSisterHeightPct));
		complexPullUpSizeRangeField.setText(Double.toString(Settings.defaultComplexPullUpSizeRange));
		minusAPeakHeightRatioField.setText(Double.toString(Settings.defaultMinusAHeightPct));
		minusAPeakSizeRangeField.setText(Double.toString(Settings.defaultMinusASizeRange));
	}
	
	@FXML
	private void resetToDefaultNOCIt(ActionEvent event) {
		thetaNumLevelsField.setText(Integer.toString(Settings.defaultThetaNumLevels));
		nocItStdErrorTolField.setText(Double.toString(Settings.defaultNocItStdErrorTol));
		int timeLimit = Settings.defaultNocItTimeLimit;
		nocItTimeLimitField.setText(Integer.toString((int) (timeLimit/1000)));
		numSamples1Field.setText(Integer.toString(Settings.defaultNumSamples1));
		numSamplesIncField.setText(Double.toString(Settings.defaultNumSamplesInc));
		maxNumSamplesField.setText(Integer.toString(Settings.defaultMaxNumSamples));
	}
	
	@FXML
	private void resetToDefaultCEESIt(ActionEvent event) {
		thetaNumLevelsCEESItField.setText(Integer.toString(Settings.defaultThetaNumLevelsCEESIt));
		binWidthField.setText(Double.toString(Settings.defaultBinWidthFactor));
		numBinsField.setText(Integer.toString(Settings.defaultNumBins));
		numSamples1CEESItField.setText(Integer.toString(Settings.defaultNumSamples1CEESIt));
		numSamplesIncCEESItField.setText(Double.toString(Settings.defaultNumSamplesIncCEESIt));
		poiSamplesField.setText(Long.toString(Settings.defaultNumberPOISamples));
		genotypeToleranceField.setText(Double.toString(Settings.defaultGenotypeTolerance));
		popSubstructureAdjField.setText(Double.toString(Settings.defaultPopSubstructureAdj));
	}
	
	@FXML
	private void settingsSaveClose(ActionEvent event) {
//		Settings.defaultDirectory = defaultDirectoryField.getText();
		Settings.numProcessors = maxNumberProcessorsField.getSelectionModel().getSelectedItem();
		
		Settings.pullUpHeightPct = Double.parseDouble(pullUpHeightRatioField.getText());
		Settings.pullUpSizeRange = Double.parseDouble(pullUpSizeRangeField.getText());
		Settings.complexPullUpHeightPct = Double.parseDouble(complexPullUpHeightRatioField.getText());
		Settings.complexPullUpSisterHeightPct = Double.parseDouble(complexPullUpSisterHeightRatioField.getText());
		Settings.complexPullUpSizeRange = Double.parseDouble(complexPullUpSizeRangeField.getText());
		Settings.minusAHeightPct = Double.parseDouble(minusAPeakHeightRatioField.getText());
		Settings.minusASizeRange = Double.parseDouble(minusAPeakSizeRangeField.getText());
		
		Settings.sampleIDDelimiter = sampleIDDelimiterField.getText();
		Settings.genotypeIDIndex = Integer.parseInt(genotypeIDIndexField.getText());
		if (plotAllPointsCheckBox.isSelected()) {
			Settings.plotAllCalibrationGraphPoints = "true";
		} else {
			Settings.plotAllCalibrationGraphPoints = "false";
		}
		Settings.calibrationGraphMaxNumPoints = Integer.parseInt(maxNumberPointsField.getText());
		
		Settings.thetaNumLevels = Integer.parseInt(thetaNumLevelsField.getText());
		Settings.nocItStdErrorTol = Double.parseDouble(nocItStdErrorTolField.getText());
		int timeLimit = Integer.parseInt(nocItTimeLimitField.getText());
		Settings.nocItTimeLimit = timeLimit*1000;
		Settings.numSamples1 = Integer.parseInt(numSamples1Field.getText());
		Settings.numSamplesInc = Double.parseDouble(numSamplesIncField.getText());
		Settings.maxNumSamples = Integer.parseInt(maxNumSamplesField.getText());
		
		Settings.thetaNumLevelsCEESIt = Integer.parseInt(thetaNumLevelsCEESItField.getText());
		Settings.binWidthFactor = Double.parseDouble(binWidthField.getText());
		Settings.numBins = Integer.parseInt(numBinsField.getText());
		Settings.numSamples1CEESIt = Integer.parseInt(numSamples1CEESItField.getText());
		Settings.numSamplesIncCEESIt = Double.parseDouble(numSamplesIncCEESItField.getText());
		Settings.numberPOISamples = Long.parseLong(poiSamplesField.getText());
		Settings.genotypeTolerance = Double.parseDouble(genotypeToleranceField.getText());
		Settings.popSubstructureAdj = Double.parseDouble(popSubstructureAdjField.getText());
		
		Settings.save();
		
		// If either of these values are changed, check if user wants to match
		// genotypes using the new settings values if any samples are loaded
		// in the calibration table, and genotypes have been loaded.
		if (!oldSampleIDDelimiter.equals(Settings.sampleIDDelimiter) || 
				olsGenotypeIDIndex != Settings.genotypeIDIndex) {
			if (uiController.calibrationTable.getItems() != null &&
					uiController.calibrationTable.getItems().size() > 0) {
				ButtonType[] results = {ButtonType.YES, ButtonType.NO, ButtonType.CANCEL};
				String message = Constants.SETTINGS_CHANGED_MATCH_GENOTYPES_QUESTION;
				if (uiController.isCalibrationCalculated()) {
					message += Constants.SETTINGS_CHANGED_MATCH_GENOTYPES_QUESTION_2;
				}
                Alert alert = new Alert(AlertType.INFORMATION, message, results);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.YES) {
                	uiController.updateCalibrationGenotypeMatching();
                	uiController.resetTreeTable();
                	uiController.setCalibrationCalculated(false);
                	uiController.resetTreeTable();
                	uiController.showSaveChangesPrompt = true;
                } else if (result.isPresent() && result.get() == ButtonType.NO) {
                	// close settings on No click
                } else {
                	// keep dialog open with Cancel click
                    return;
                }
			}
		}
		
		((Stage)pullUpHeightRatioField.getScene().getWindow()).close();
	}
	
	@FXML
    private void plotAllPointsAction(ActionEvent event) {
		maybeEnableNumPointsField();
	}
	
	private void maybeEnableNumPointsField() {
		if (!plotAllPointsCheckBox.isSelected()) {
			maxNumberPointsField.setDisable(false);
		} else {
			maxNumberPointsField.setDisable(true);
		}
	}	
    
    public void disableControls(boolean disable) {
    	pullUpHeightRatioField.setDisable(disable);
    	pullUpSizeRangeField.setDisable(disable);
    	complexPullUpHeightRatioField.setDisable(disable);
    	complexPullUpSisterHeightRatioField.setDisable(disable);	
    	complexPullUpSizeRangeField.setDisable(disable);
    	minusAPeakHeightRatioField.setDisable(disable);
    	minusAPeakSizeRangeField.setDisable(disable);
    	
    	resetToDefaultFilterButton.setDisable(disable);
    	settingsSaveCloseFilterButton.setDisable(disable);
    	
    	thetaNumLevelsField.setDisable(disable);
    	nocItStdErrorTolField.setDisable(disable);
    	nocItTimeLimitField.setDisable(disable);
    	numSamples1Field.setDisable(disable);
    	numSamplesIncField.setDisable(disable);
    	maxNumSamplesField.setDisable(disable);
    	
    	resetToDefaultNOCItButton.setDisable(disable);
    	settingsSaveCloseNOCItButton.setDisable(disable);
    	
    	thetaNumLevelsCEESItField.setDisable(disable);
    	binWidthField.setDisable(disable);
    	numBinsField.setDisable(disable);
    	numSamples1CEESItField.setDisable(disable);
    	numSamplesIncCEESItField.setDisable(disable);
    	poiSamplesField.setDisable(disable);
    	genotypeToleranceField.setDisable(disable);
    	popSubstructureAdjField.setDisable(disable);
    	
    	resetToDefaultCEESItButton.setDisable(disable);
    	settingsSaveCloseCEESItButton.setDisable(disable);
    }
    
    public void refresh() {
    	if (tabPane != null) {
    		int index = tabPane.getSelectionModel().getSelectedIndex();
    		// index 0 is enabled when timer is running, selecting it will have no effect
    		// on the display
    		tabPane.getSelectionModel().select(0);
    		tabPane.getSelectionModel().select(index);
    	}
    }

	public void setStage(Stage settingsStage, UIController controller) {
    	this.uiController = controller;
	}
    
}
