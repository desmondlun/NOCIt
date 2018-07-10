package edu.rutgers.NOCIt.Control;

import static edu.rutgers.NOCIt.UIMain.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import edu.rutgers.NOCIt.Data.Constants;

/**
 * The Class Settings. This class is used to save values from the Settings
 * dialog and other fields to a file which is loaded when NOCIt is started
 * allowing values to be stored between runs.
 *
 * @author Rob Carpenter
 * @author James Kelley
 * @author Desmond Lun
 */
public class Settings {
	// acceptable ranges
	public static final int GENOTYPE_ID_INDEX_MIN_VALUE = 0;
	public static final int GENOTYPE_ID_INDEX_MAX_VALUE = 20;
	public static final int CALIBRATION_GRAPH_MAX_POINTS_MIN_VALUE = 1;
	public static final int CALIBRATION_GRAPH_MAX_POINTS_MAX_VALUE = Constants.MAX_INTEGER_VALUE;

	public static final double PULL_UP_HEIGHT_RATIO_MIN_VALUE = 0;
	public static final double PULL_UP_HEIGHT_RATIO_MAX_VALUE = 100;
	public static final double PULL_UP_SIZE_RANGE_MIN_VALUE = 0;
	public static final double PULL_UP_SIZE_RANGE_MAX_VALUE = 100;
	public static final double COMPLEX_PULL_UP_HEIGHT_RATIO_MIN_VALUE = 0;
	public static final double COMPLEX_PULL_UP_HEIGHT_RATIO_MAX_VALUE = 100;
	public static final double COMPLEX_PULL_UP_SISTER_HEIGHT_RATIO_MIN_VALUE = 0;
	public static final double COMPLEX_PULL_UP_SISTER_HEIGHT_RATIO_MAX_VALUE = 100;
	public static final double COMPLEX_PULL_UP_SIZE_RANGE_MIN_VALUE = 0;
	public static final double COMPLEX_PULL_UP_SIZE_RANGE_MAX_VALUE = 100;
	public static final double MINUS_A_HEIGHT_RATIO_MIN_VALUE = 0;
	public static final double MINUS_A_HEIGHT_RATIO_MAX_VALUE = 100;
	public static final double MINUS_A_SIZE_RANGE_MIN_VALUE = 0;
	public static final double MINUS_A_SIZE_RANGE_MAX_VALUE = 100;

	public static final int LEVELS_FOR_MIXTURE_RATIOS_MIN_VALUE = 1;
	public static final int LEVELS_FOR_MIXTURE_RATIOS_MAX_VALUE = 100000;
	public static final double STD_ERROR_TOLERANCE_MIN_VALUE = Double.MIN_VALUE;
	public static final double STD_ERROR_TOLERANCE_MAX_VALUE = 1;
	public static final int REFINEMENT_TIME_LIMIT_MIN_VALUE = 1;
	public static final int REFINEMENT_TIME_LIMIT_MAX_VALUE = 100000;
	public static final int NUM_SAMPLES_IN_BATCH_FOR_NOC_1_MIN_VALUE = 1;
	public static final int NUM_SAMPLES_IN_BATCH_FOR_NOC_1_MAX_VALUE = Constants.MAX_INTEGER_VALUE;
	public static final double MULTIPLICATIVE_FACTOR_MIN_VALUE = 1.0;
	public static final double MULTIPLICATIVE_FACTOR_MAX_VALUE = 100000.0;
	public static final int MAX_NUM_SAMPLES_IN_BATCH_MIN_VALUE = 1;
	public static final int MAX_NUM_SAMPLES_IN_BATCH_MAX_VALUE = Constants.MAX_INTEGER_VALUE;

	public static final double BIN_WIDTH_MIN_VALUE = Double.MIN_VALUE;
	public static final double BIN_WIDTH_MAX_VALUE = Constants.MAX_INTEGER_VALUE;
	public static final int NUM_BINS_MIN_VALUE = 1;
	public static final int NUM_BINS_MAX_VALUE = Constants.MAX_INTEGER_VALUE;
	public static final int NUM_SAMPLES_IN_BATCH_FOR_NOC_1_CEESIT_MIN_VALUE = 1;
	public static final int NUM_SAMPLES_IN_BATCH_FOR_NOC_1_CEESIT_MAX_VALUE = Constants.MAX_INTEGER_VALUE;
	public static final int MULTIPLICATIVE_FACTOR_CEESIT_MIN_VALUE = 1;
	public static final int MULTIPLICATIVE_FACTOR_CEESIT_MAX_VALUE = 100000;
	// do we want this to be validated as an integer but allow values greater
	// than the max integer value?
	public static final long POI_SAMPLES_MIN_VALUE = 1;
	public static final long POI_SAMPLES_MAX_VALUE = Long.MAX_VALUE;
	public static final double GENOTYPE_TOLERANCE_MIN_VALUE = Double.MIN_VALUE;
	public static final double GENOTYPE_TOLERANCE_MAX_VALUE = Constants.MAX_INTEGER_VALUE;
	
	public static final double POP_SUBSTRUCTURE_ADJ_MIN_VALUE = 0;
	public static final double POP_SUBSTRUCTURE_ADJ_MAX_VALUE = 1;

	/** The properties. */
	private static Properties properties = new Properties();

	/** The Constant defaultMaxNumberProcessors. */
	public static final int defaultMaxNumberProcessors = Runtime.getRuntime().availableProcessors();

	/** The Constant defaultGenotypeIDIndex. */
	public static final int defaultGenotypeIDIndex = 1;

	/** The Constant defaultsampleIDdelimiter. */
	public static final String defaultSampleIDDelimiter = "";

	/** The Constant defaultPlotAllCalibrationGraphPointsValue. */
	public static final boolean defaultPlotAllCalibrationGraphPointsValue = false;

	/** The Constant defaultPlotAllCalibrationGraphPoints. */
	public static final String defaultPlotAllCalibrationGraphPoints = Boolean
			.toString(defaultPlotAllCalibrationGraphPointsValue);

	/** The Constant defaultCalibrationGraphMaxNumPoints. */
	// 4000 or less points seem to graph quickly
	public static final int defaultCalibrationGraphMaxNumPoints = 4000;

	/** The Constant defaultPullUpHeightPct. */
	public static final double defaultPullUpHeightPct = 5;

	/** The Constant defaultPullUpSizeRange. */
	public static final double defaultPullUpSizeRange = 0.3;

	/** The Constant defaultMinusAHeightPct. */
	public static final double defaultMinusAHeightPct = 7;

	/** The Constant defaultMinusASizeRange. */
	public static final double defaultMinusASizeRange = 0.6;

	/** The Constant defaultComplexPullUpHeightPct. */
	public static final double defaultComplexPullUpHeightPct = 6;

	/** The Constant defaultComplexPullUpSisterHeightPct. */
	public static final double defaultComplexPullUpSisterHeightPct = 50;

	/** The Constant defaultComplexPullUpSizeRange. */
	public static final double defaultComplexPullUpSizeRange = 0.3;

	/** The Constant defaultThetaNumLevels. */
	public static final int defaultThetaNumLevels = 16;

	/** The Constant defaultNocItStdErrorTol. */
	public static final double defaultNocItStdErrorTol = 0.05;

	/** The Constant defaultNocItTimeLimit. */
	public static final int defaultNocItTimeLimit = 300;

	/** The Constant defaultNumSamples1. */
	public static final int defaultNumSamples1 = 6000;

	/** The Constant defaultNumSamplesInc. */
	public static final double defaultNumSamplesInc = 3.0;

    /** The Constant defaultMaxBatchSize. */
	public static final int defaultMaxNumSamples = 175000;
	
	/** The Constant defaultThetaNumLevels. */
	public static final int defaultThetaNumLevelsCEESIt = 8;
	
	/** The Constant defaultBinWidthFactor. */
	public static final double defaultBinWidthFactor = 10;
	
	/** The Constant defaultNumBins. */
	public static final int defaultNumBins = 20;
	
	/** The Constant defaultNumSamples1. */
	public static final int defaultNumSamples1CEESIt = 50;

	/** The Constant defaultNumSamplesInc. */
	public static final double defaultNumSamplesIncCEESIt = 1.5;

	/** The Constant defaultNumberPOISamples. */
	public static final long defaultNumberPOISamples = 1000000;
	
	/**
	 * The Constant standard error tolerance for the probability of evidence given a
	 * locus, set of quantification parameters, and number of contributors.
	 */
	public static final double defaultGenotypeTolerance = 0.5;
	
	/**
	 * The Constant defaultPopSubstructureAdj, also called the "average inbreeding coefficient"
	 */
	public static final double defaultPopSubstructureAdj = 0.01;
	
	/** The default directory. */
	public static String defaultDirectory = System.getProperty("user.home");

	/** The default NelderMeadModelSolver maxEval. */
	public static int defaultNelderMeadModelSolverMaxEval = 1000000;

	/** The NelderMeadModelSolver maxEval. */
	public static int nelderMeadModelSolverMaxEval = defaultNelderMeadModelSolverMaxEval;

	/** The pull up height percentage. */
	public static double pullUpHeightPct = defaultPullUpHeightPct;

	/** The pull up size range. */
	public static double pullUpSizeRange = defaultPullUpSizeRange;

	/** The minus A height percentage. */
	public static double minusAHeightPct = defaultMinusAHeightPct;

	/** The minus A size range. */
	public static double minusASizeRange = defaultMinusASizeRange;

	/** The complex pull up height percentage. */
	public static double complexPullUpHeightPct = defaultComplexPullUpHeightPct;

	/** The complex pull up sister height percentage. */
	public static double complexPullUpSisterHeightPct = defaultComplexPullUpSisterHeightPct;

	/** The complex pull up size range. */
	public static double complexPullUpSizeRange = defaultComplexPullUpSizeRange;

	/** The plot all calibration graph points. */
	public static String plotAllCalibrationGraphPoints = defaultPlotAllCalibrationGraphPoints;

	/** The dna mass index. */
	// DNA Mass column hidden and no longer used. Setting index to 0 should have
	// no effect on calibration.
	public static int dnaMassIndex = 0;

	/** The sample ID delimiter. */
	public static String sampleIDDelimiter = defaultSampleIDDelimiter;

	/** The genotype ID index. */
	public static int genotypeIDIndex = defaultGenotypeIDIndex;

	/** The minimum number of counts for an allele in frequency distribution. */
	public static int minCount = 5;

	// NOCIt settings
	/** The number of discretization levels for mixture ratios. */
	public static int thetaNumLevels = defaultThetaNumLevels;

	/** The NOCIt standard error tolerance. */
	public static double nocItStdErrorTol = defaultNocItStdErrorTol;

	/** The NOCIt refinement time limit (in milliseconds). */
	public static int nocItTimeLimit = defaultNocItTimeLimit;

	/** The number of samples in batch for noc = 1. */
	public static int numSamples1 = defaultNumSamples1;

	/**
	 * The multiplicative factor by which number of samples are increased when
	 * noc is increased by 1.
	 */
	public static double numSamplesInc = defaultNumSamplesInc;
	
	public static int maxNumSamples = defaultMaxNumSamples;
	
	// CEESIt Settings
	/** The number of discretization levels for mixture ratios. */
	public static int thetaNumLevelsCEESIt = defaultThetaNumLevelsCEESIt;
	
	/** The bin width factor for CEESIt */
	public static double binWidthFactor = defaultBinWidthFactor;
	
	/** The number of Bins for CEESIt. */
	public static int numBins = defaultNumBins;
	
	/** The number of samples in batch for noc = 1. */
	public static int numSamples1CEESIt = defaultNumSamples1CEESIt;

	/**
	 * The multiplicative factor by which number of samples are increased when
	 * noc is increased by 1.
	 */
	public static double numSamplesIncCEESIt = defaultNumSamplesIncCEESIt;
	
	/** The number of POI samples. */
	public static long numberPOISamples = defaultNumberPOISamples;
	
	/**
	 * The standard error tolerance for the probability of evidence given a
	 * locus, set of quantification parameters, and number of contributors.
	 */
	public static double genotypeTolerance = defaultGenotypeTolerance;
	
	/**
	 * The Population Substructure Adjustment or average inbreeding coefficient
	 */
	public static double popSubstructureAdj = defaultPopSubstructureAdj;

	/** The number of processors to use. */
	public static int numProcessors = defaultMaxNumberProcessors;

	/** The calibration graph maximum number of points. */
	public static int calibrationGraphMaxNumPoints = defaultCalibrationGraphMaxNumPoints;

	/** The last calibration samples path. */
	// Used for calibration files such as those in the etc folder. This path
	// must be a directory.
	public static String lastCalibrationSamplesPath = defaultDirectory;

	/** The last calibration path. */
	// Used for calibration project
	public static String lastCalibrationPath = defaultDirectory;

	/** The last sample path. */
	public static String lastSamplePath = defaultDirectory;

	/** The last PDF path. */
	public static String lastPDFPath = defaultDirectory;

	/** The last frequency path. */
	public static String lastFrequencyPath = defaultDirectory;

	/** The last input file path. */
	public static String lastInputFilePath = defaultDirectory;

	/** The last bins file path. */
	public static String lastBinsFilePath = defaultDirectory;

	/** The last genotype file path. */
	public static String lastGenotypeFilePath = defaultDirectory;

	/** The last output file path. */
	public static String lastOutputFilePath = defaultDirectory;

	/** The recent NOCIt files. */
	//public static String recentNocitFiles = "";

	/** The last NOCIt calibration. */
	// saves last selections in NOCIt Batch Run
	public static String lastNOCItCalibration = "";
	
	/** The last CEESIt calibration. */
	// saves last selections in NOCIt Batch Run
	public static String lastCEESItCalibration = "";

	/** The last NOC selection. */
	public static String lastNOCSelection = "1";

	/** The last filter selection. */
	public static String lastFilterSelection = "false";

	/** The last write CSV selection. */
	public static String lastWriteCSVSelection = "false";
	
	/** The last CEESIt NOC selection. */
	public static String lastCEESItNOCSelection = "1";
	
	/** The last CEESIt filter selection. */
	public static String lastCEESItFilterSelection = "false";

	/** The last CEESIt write CSV selection. */
	public static String lastCEESItWriteCSVSelection = "false";

	/**
	 * Load settings file.
	 */
	public static void load() {
		load(getSettingsPath() + Constants.SETTINGS_FILENAME);
	}

	public static void load(String arg) {
		File settingsFile = new File(arg);

		if (!settingsFile.exists()) {
			System.out.println("Settings file not found");
			save();
		} else {
			try {
				properties.load(new FileInputStream(arg));

				if (properties.getProperty("numProcessors") != null)
					numProcessors = Integer.parseInt(properties.getProperty("numProcessors"));
				if (properties.getProperty("pullUpHeightPct") != null)
					pullUpHeightPct = Double.parseDouble(properties.getProperty("pullUpHeightPct"));
				if (properties.getProperty("pullUpSizeRange") != null)
					pullUpSizeRange = Double.parseDouble(properties.getProperty("pullUpSizeRange"));
				if (properties.getProperty("minusAHeightPct") != null)
					minusAHeightPct = Double.parseDouble(properties.getProperty("minusAHeightPct"));
				if (properties.getProperty("minusASizeRange") != null)
					minusASizeRange = Double.parseDouble(properties.getProperty("minusASizeRange"));
				if (properties.getProperty("complexPullUpHeightPct") != null)
					complexPullUpHeightPct = Double.parseDouble(properties.getProperty("complexPullUpHeightPct"));
				if (properties.getProperty("complexPullUpSisterHeightPct") != null)
					complexPullUpSisterHeightPct = Double
							.parseDouble(properties.getProperty("complexPullUpSisterHeightPct"));
				if (properties.getProperty("complexPullUpSizeRange") != null)
					complexPullUpSizeRange = Double.parseDouble(properties.getProperty("complexPullUpSizeRange"));
				
				if (properties.getProperty("lastCalibrationSamplesPath") != null)
					lastCalibrationSamplesPath = properties.getProperty("lastCalibrationSamplesPath");
				if (properties.getProperty("lastCalibrationPath") != null)
					lastCalibrationPath = properties.getProperty("lastCalibrationPath");
				if (properties.getProperty("lastSamplePath") != null)
					lastSamplePath = properties.getProperty("lastSamplePath");
				if (properties.getProperty("lastPDFPath") != null)
					lastPDFPath = properties.getProperty("lastPDFPath");
				if (properties.getProperty("lastFrequencyPath") != null)
					lastFrequencyPath = properties.getProperty("lastFrequencyPath");
				if (properties.getProperty("lastInputFilePath") != null)
					lastInputFilePath = properties.getProperty("lastInputFilePath");
				if (properties.getProperty("lastBinsFilePath") != null)
					lastBinsFilePath = properties.getProperty("lastBinsFilePath");
				if (properties.getProperty("lastGenotypeFilePath") != null)
					lastGenotypeFilePath = properties.getProperty("lastGenotypeFilePath");
				if (properties.getProperty("lastOutputFilePath") != null)
					lastOutputFilePath = properties.getProperty("lastOutputFilePath");
//				if (properties.getProperty("recentNocitFiles") != null)
//					recentNocitFiles = properties.getProperty("recentNocitFiles");
				if (properties.getProperty("sampleIDDelemeter") != null)
					sampleIDDelimiter = properties.getProperty("sampleIDDelemeter");
				if (properties.getProperty("genotypeIDIndex") != null)
					genotypeIDIndex = Integer.parseInt(properties.getProperty("genotypeIDIndex"));
				if (properties.getProperty("plotAllCalibrationGraphPoints") != null)
					plotAllCalibrationGraphPoints = properties.getProperty("plotAllCalibrationGraphPoints");
				if (properties.getProperty("calibrationGraphMaxNumPoints") != null)
					calibrationGraphMaxNumPoints = Integer
							.parseInt(properties.getProperty("calibrationGraphMaxNumPoints"));

				if (properties.getProperty("thetaNumLevels") != null)
					thetaNumLevels = Integer.parseInt(properties.getProperty("thetaNumLevels"));
				if (properties.getProperty("nocItStdErrorTol") != null)
					nocItStdErrorTol = Double.parseDouble(properties.getProperty("nocItStdErrorTol"));
				if (properties.getProperty("nocItTimeLimit") != null)
					nocItTimeLimit = Integer.parseInt(properties.getProperty("nocItTimeLimit"));
				if (properties.getProperty("numSamples1") != null)
					numSamples1 = Integer.parseInt(properties.getProperty("numSamples1"));
				if (properties.getProperty("numSamples") != null)
					numSamplesInc = Double.parseDouble(properties.getProperty("numSamples"));
				if (properties.getProperty("maxNumSamples") != null)
					maxNumSamples = Integer.parseInt(properties.getProperty("maxNumSamples"));
				
				if (properties.getProperty("thetaNumLevelsCEESIt") != null)
					thetaNumLevelsCEESIt = Integer.parseInt(properties.getProperty("thetaNumLevelsCEESIt"));
				if (properties.getProperty("binWidthFactor") != null)
					binWidthFactor = Double.parseDouble(properties.getProperty("binWidthFactor"));
				if (properties.getProperty("numBins") != null)
					numBins = Integer.parseInt(properties.getProperty("numBins"));
				if (properties.getProperty("numSamples1CEESIt") != null)
					numSamples1CEESIt = Integer.parseInt(properties.getProperty("numSamples1CEESIt"));
				if (properties.getProperty("numSamplesCEESIt") != null)
					numSamplesIncCEESIt = Double.parseDouble(properties.getProperty("numSamplesCEESIt"));
				if (properties.getProperty("numberPOISamples") != null)
					numberPOISamples = Long.parseLong(properties.getProperty("numberPOISamples"));
				if (properties.getProperty("genotypeTolerance") != null)
					genotypeTolerance = Double.parseDouble(properties.getProperty("genotypeTolerance"));
				if (properties.getProperty("popSubstructureAdj") != null)
					popSubstructureAdj = Double.parseDouble(properties.getProperty("popSubstructureAdj"));

				if (properties.getProperty("lastNOCItCalibration") != null)
					lastNOCItCalibration = properties.getProperty("lastNOCItCalibration");
				if (properties.getProperty("lastNOCSelection") != null)
					lastNOCSelection = properties.getProperty("lastNOCSelection");
				if (properties.getProperty("lastFilterSelection") != null)
					lastFilterSelection = properties.getProperty("lastFilterSelection");
				if (properties.getProperty("lastWriteCSVSelection") != null)
					lastWriteCSVSelection = properties.getProperty("lastWriteCSVSelection");
				
				if (properties.getProperty("lastCEESItCalibration") != null)
					lastCEESItCalibration = properties.getProperty("lastCEESItCalibration");
				if (properties.getProperty("lastCEESItNOCSelection") != null)
					lastCEESItNOCSelection = properties.getProperty("lastCEESItNOCSelection");
				if (properties.getProperty("lastCEESItFilterSelection") != null)
					lastCEESItFilterSelection = properties.getProperty("lastCEESItFilterSelection");
				if (properties.getProperty("lastCEESItWriteCSVSelection") != null)
					lastCEESItWriteCSVSelection = properties.getProperty("lastCEESItWriteCSVSelection");
				
				if (properties.getProperty("nelderMeadModelSolverMaxEval") != null)
					nelderMeadModelSolverMaxEval = Integer
							.parseInt(properties.getProperty("nelderMeadModelSolverMaxEval"));
			} catch (Exception e) {
				logger.error(Constants.SETTINGS_LOAD_ERROR_LOG_MESSAGE, e);
			}
		}
	}

	/**
	 * Save settings file.
	 */
	public static void save() {
		try {
			properties.setProperty("numProcessors", Integer.toString(numProcessors));
			properties.setProperty("genotypeIDIndex", Integer.toString(genotypeIDIndex));
			properties.setProperty("pullUpHeightPct", Double.toString(pullUpHeightPct));
			properties.setProperty("pullUpSizeRange", Double.toString(pullUpSizeRange));
			properties.setProperty("minusAHeightPct", Double.toString(minusAHeightPct));
			properties.setProperty("minusASizeRange", Double.toString(minusASizeRange));
			properties.setProperty("complexPullUpHeightPct", Double.toString(complexPullUpHeightPct));
			properties.setProperty("complexPullUpSisterHeightPct", Double.toString(complexPullUpSisterHeightPct));
			properties.setProperty("complexPullUpSizeRange", Double.toString(complexPullUpSizeRange));
			
			properties.setProperty("lastCalibrationSamplesPath", lastCalibrationSamplesPath);
			properties.setProperty("lastCalibrationPath", lastCalibrationPath);
			properties.setProperty("lastSamplePath", lastSamplePath);
			properties.setProperty("lastPDFPath", lastPDFPath);
			properties.setProperty("lastFrequencyPath", lastFrequencyPath);
			properties.setProperty("lastInputFilePath", lastInputFilePath);
			properties.setProperty("lastBinsFilePath", lastBinsFilePath);
			properties.setProperty("lastGenotypeFilePath", lastGenotypeFilePath);
			properties.setProperty("lastOutputFilePath", lastOutputFilePath);
			//properties.setProperty("recentNocitFiles", recentNocitFiles);
			properties.setProperty("sampleIDDelemeter", sampleIDDelimiter);
			properties.setProperty("genotypeIDIndex", Integer.toString(genotypeIDIndex));
			properties.setProperty("dnaMassIndex", Integer.toString(dnaMassIndex));
			properties.setProperty("plotAllCalibrationGraphPoints", plotAllCalibrationGraphPoints);
			properties.setProperty("calibrationGraphMaxNumPoints", Integer.toString(calibrationGraphMaxNumPoints));

			properties.setProperty("thetaNumLevels", Integer.toString(thetaNumLevels));
			properties.setProperty("nocItStdErrorTol", Double.toString(nocItStdErrorTol));
			properties.setProperty("nocItTimeLimit", Integer.toString(nocItTimeLimit));
			properties.setProperty("numSamples1", Integer.toString(numSamples1));
			properties.setProperty("numSamples", Double.toString(numSamplesInc));
			properties.setProperty("maxNumSamples", Integer.toString(maxNumSamples));
			
			properties.setProperty("thetaNumLevelsCEESIt", Integer.toString(thetaNumLevelsCEESIt));
			properties.setProperty("binWidthFactor", Double.toString(binWidthFactor));
			properties.setProperty("numBins", Integer.toString(numBins));
			properties.setProperty("numSamples1CEESIt", Integer.toString(numSamples1CEESIt));
			properties.setProperty("numSamplesCEESIt", Double.toString(numSamplesIncCEESIt));
			properties.setProperty("numberPOISamples", Long.toString(numberPOISamples));
			properties.setProperty("genotypeTolerance", Double.toString(genotypeTolerance));
			properties.setProperty("popSubstructureAdj", Double.toString(popSubstructureAdj));

			properties.setProperty("lastNOCItCalibration", lastNOCItCalibration);
			properties.setProperty("lastNOCSelection", lastNOCSelection);
			properties.setProperty("lastFilterSelection", lastFilterSelection);
			properties.setProperty("lastWriteCSVSelection", lastWriteCSVSelection);
			
			properties.setProperty("lastCEESItCalibration", lastCEESItCalibration);
			properties.setProperty("lastCEESItNOCSelection", lastCEESItNOCSelection);
			properties.setProperty("lastCEESItFilterSelection", lastCEESItFilterSelection);
			properties.setProperty("lastCEESItWriteCSVSelection", lastCEESItWriteCSVSelection);
			
			properties.setProperty("nelderMeadModelSolverMaxEval", Integer.toString(nelderMeadModelSolverMaxEval));

			properties.store(new FileOutputStream(getSettingsPath() + Constants.SETTINGS_FILENAME), null);
		} catch (Exception e) {
			logger.error(Constants.SETTINGS_SAVE_ERROR_LOG_MESSAGE, e);
		}
	}

	/**
	 * Gets the settings path.
	 *
	 * @return the settings path
	 */
	public static String getSettingsPath() {
		File destDir = null;
		if (System.getProperty("os.name").contains("Windows")) {
			if (System.getProperty("os.name").equals("Windows XP")) {
				destDir = new File(System.getProperty("user.home") + Constants.SETTINGS_PATH_SUFFIX_WINDOWS_XP + "/"
						+ Constants.FOLDER_NAME);
			} else {
				destDir = new File(System.getenv("LOCALAPPDATA") + "/" + Constants.FOLDER_NAME);
			}
		} else if (System.getProperty("os.name").toLowerCase().contains("mac os x")) {
			destDir = new File(System.getenv("HOME") + "/Library/" + Constants.FOLDER_NAME);
		} else if (System.getProperty("os.name").equals("Linux")) {
			destDir = new File(Constants.FOLDER_NAME);
		}
		
		if (!destDir.exists()) 
			destDir.mkdir();		

		return destDir.getAbsolutePath() + File.separatorChar;
	}
}