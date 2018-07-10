package edu.rutgers.NOCIt.Control;

import static edu.rutgers.NOCIt.UIMain.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import edu.rutgers.NOCIt.Data.AMELAllele;
import edu.rutgers.NOCIt.Data.CSVModule;
import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.FreqTable;
import edu.rutgers.NOCIt.Data.Locus;
import edu.rutgers.NOCIt.Data.MD5CheckSumGenerator;
import edu.rutgers.NOCIt.Data.STRAllele;
import edu.rutgers.NOCIt.Data.Sample;
import edu.rutgers.NOCIt.Data.UtilityMethods;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;

/**
 * This class implements NOCIt.
 *
 * @author Harish Swaminathan
 * @author Desmond Lun
 * @author Anurag Garg
 * @author James Kelley
 */
public class NOCIt {

	/**
	 * Callable that simulates a fixed number of genotypes at the AMEL locus by
	 * importance sampling and calculates the probability of observing the
	 * evidence for a given locus, set of quantification parameters, and number
	 * of contributors.
	 */
	private class CalcAMELProbCallable implements Callable<double[]> {

		/** The number of contributors. */
		private final int noc;

		/** The locus. */
		private final Locus locus;

		/** The quantification parameters. */
		private final double[][] quantParams;

		/** The number of iterations. */
		private final int numIterations;

		/**
		 * Instantiates a new calculate AMEL probability callable.
		 *
		 * @param noc
		 *            the number of contributors
		 * @param quantParams
		 *            the quantification parameters
		 * @param locus
		 *            the locus
		 * @param numIterations
		 *            the number of iterations
		 */
		CalcAMELProbCallable(int noc, double[][] quantParams, Locus locus, int numIterations) {
			this.noc = noc;
			this.locus = locus;
			this.quantParams = quantParams;
			this.numIterations = numIterations;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public double[] call() {
			double logProbSum = Double.NEGATIVE_INFINITY;
			double logProbSumSq = Double.NEGATIVE_INFINITY;

			AMELAllele[] trueAlleles = new AMELAllele[2 * noc];
			for (int iteration = 1; iteration <= numIterations; iteration++) {
				for (int contributor = 0; contributor < noc; contributor++) { // For
					// each
					// contributor
					AMELAllele[] alleles = probabilityModel.sampleAMELAllelePair();
					trueAlleles[2 * contributor] = alleles[0];
					trueAlleles[2 * contributor + 1] = alleles[1];
				}

				double logProb = probabilityModel.calcLogProbBernoulli(locus, trueAlleles, quantParams); // Peak
				// heights
				// prob

				logProbSum = UtilityMethods.logSum(logProbSum, logProb);
				logProbSumSq = UtilityMethods.logSum(logProbSumSq, 2 * logProb);
			}

			return new double[] { logProbSum, logProbSumSq };
		}
	}

	/**
	 * Callable that simulates a fixed number of genotypes at an STR locus by
	 * importance sampling and calculates the probability of observing the
	 * evidence for a given locus, set of quantification parameters, and number
	 * of contributors.
	 */
	private class CalcSTRProbCallable implements Callable<double[]> {

		/** The number of contributors. */
		private final int noc;

		/** The locus. */
		private final Locus locus;

		/** The quantification parameters. */
		private final double[][] quantParams;

		/** The number of iterations. */
		private final int numIterations;

		/**
		 * Instantiates a new calculate STR probability callable.
		 *
		 * @param noc
		 *            the number of contributors
		 * @param quantParams
		 *            the quantification parameters
		 * @param locus
		 *            the locus
		 * @param numIterations
		 *            the number of iterations
		 */
		CalcSTRProbCallable(int noc, double[][] quantParams, Locus locus, int numIterations) {
			this.noc = noc;
			this.locus = locus;
			this.quantParams = quantParams;
			this.numIterations = numIterations;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public double[] call() {
			double logProbSum = Double.NEGATIVE_INFINITY;
			double logProbSumSq = Double.NEGATIVE_INFINITY;

			STRAllele[] trueAlleles = new STRAllele[2 * noc];
			for (int iteration = 1; iteration <= numIterations; iteration++) { // Each
				// iteration

				double alleleProb = 1.0;
				double heightProb = 1.0;
				for (int i = 0; i < 2 * noc; i++) {
					STRAllele allele = probabilityModel.sampleAlleleByHeight(locus);
					trueAlleles[i] = allele;

					alleleProb *= probabilityModel.getAlleleProbByFreq(locus, allele);
					heightProb *= probabilityModel.getAlleleProbByHeight(locus, allele);
				}

				double weight = alleleProb / heightProb; // Weight
				double logProb = FastMath.log(weight);

				logProb += probabilityModel.calcLogProbBernoulli(locus, trueAlleles, quantParams); // Signal
				// prob

				logProbSum = UtilityMethods.logSum(logProbSum, logProb);
				logProbSumSq = UtilityMethods.logSum(logProbSumSq, 2 * logProb);
			}

			return new double[] { logProbSum, logProbSumSq };
		}
	}

	/**
	 * The Enum ExitCode.
	 */
	private enum ExitCode {
		/** The time limit reached. */
		TIME_LIMIT_REACHED,
		/** The error tolerance reached. */
		ERROR_TOL_REACHED
	};

	/**
	 * The Class MCRun.
	 */
	private class MCRun {

		/** The number of contributors. */
		private int noc;

		/** The theta (mixture ratio). */
		private double[][] theta;

		/** The quantification parameters. */
		private double[][] quantParams;

		/** The locus. */
		private Locus locus;

		/** The log of the prior probability for theta. */
		private double logThetaPriorProb;

		/** The batch size. */
		private int batchSize;

		/** The partial derivative of the standard error. */
		private double errPartialDeriv;

		/** The log of the sum of probabilities. */
		private double logSum = Double.NEGATIVE_INFINITY;

		/** The log of the sum of squared probabilities. */
		private double logSumSq = Double.NEGATIVE_INFINITY;

		/** The number of samples. */
		private long numSamples = 0;

		/** The log of the mean probability. */
		private double logMean;

		/** The log of the variance of the standard error. */
		private double logErrVar;

		/** The log of the 2nd moment of the probability. */
		private double log2ndMoment;

		/**
		 * Instantiates a new Monte Carlo run. Each run is used to calculate the
		 * posterior probability for a given locus and set of mixture ratios.
		 *
		 * @param noc
		 *            the number of contributors
		 * @param theta
		 *            the theta (mixture ratios)
		 * @param quantParams
		 *            the quantification parameters
		 * @param locus
		 *            the locus
		 * @param logThetaPriorProb
		 *            the log of the prior probability for theta
		 * @param batchSize
		 *            the batch size
		 */
		public MCRun(int noc, double[][] theta, double[][] quantParams, Locus locus, double logThetaPriorProb,
				int batchSize) {
			this.noc = noc;
			this.theta = theta;
			this.quantParams = quantParams;
			this.locus = locus;
			this.logThetaPriorProb = logThetaPriorProb;
			this.batchSize = batchSize;
		}

		/**
		 * Gets the partial derivative of the standard error.
		 *
		 * @return the partial derivative of the standard error
		 */
		public double getErrPartialDeriv() {
			return errPartialDeriv;
		}

		/**
		 * Gets the log of the 2nd moment of the probability.
		 *
		 * @return the log of the 2nd moment of the probability
		 */
		public double getLog2ndMoment() {
			return log2ndMoment;
		}

		/**
		 * Gets the log of the variance of the standard error.
		 *
		 * @return the log of the variance of the standard error
		 */
		public double getLogErrVar() {
			return logErrVar;
		}

		/**
		 * Gets the log of the mean probability.
		 *
		 * @return the log of the mean probability
		 */
		public double getLogMean() {
			return logMean;
		}

		/**
		 * Gets the log of the prior probability for theta.
		 *
		 * @return the log theta prior prob
		 */
		public double getLogThetaPriorProb() {
			return logThetaPriorProb;
		}

		/**
		 * Gets the number of samples.
		 *
		 * @return the number of samples
		 */
		public long getNumSamples() {
			return numSamples;
		}

		/**
		 * Gets the theta.
		 *
		 * @return the theta
		 */
		public double[][] getTheta() {
			return theta;
		}

		/**
		 * Samples another set of samples, specified by batchSize.
		 *
		 * @throws InterruptedException
		 *             the interrupted exception
		 * @throws ExecutionException
		 *             the execution exception
		 */
		public void sample() throws InterruptedException, ExecutionException {
			double[] values = calcLogLocusSumProbBatch(locus, batchSize, noc, quantParams);
			double logSumBatch = values[0];
			double logSumSqBatch = values[1];

			logSum = UtilityMethods.logSum(logSum, logSumBatch);
			logSumSq = UtilityMethods.logSum(logSumSq, logSumSqBatch);

			numSamples += batchSize;

			logMean = logSum - FastMath.log(numSamples);
			logErrVar = UtilityMethods.logDiff(logSumSq, 2 * logSum - FastMath.log(numSamples))
					- FastMath.log((numSamples - 1.0) * numSamples);
			log2ndMoment = UtilityMethods.logSum(2 * logMean, logErrVar);

			// System.out.println(locus + " " + Arrays.deepToString(theta) + " "
			// + logLocusMean);
		}

		/**
		 * Sets the partial derivative of the standard error.
		 *
		 * @param errPartialDeriv
		 *            the new partial derivative of the standard error
		 */
		public void setErrPartialDeriv(double errPartialDeriv) {
			this.errPartialDeriv = errPartialDeriv;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "MCRun [noc=" + noc + ", theta=" + Arrays.deepToString(theta) + ", locus=" + locus
					+ ", logThetaPriorProb=" + logThetaPriorProb + ", batchSize=" + batchSize + ", logSum=" + logSum
					+ ", logSumSq=" + logSumSq + ", numSamples=" + numSamples + ", logLocusMean=" + logMean
					+ ", logLocusErrVar=" + logErrVar + ", logLocus2ndMoment=" + log2ndMoment + "]";
		}
	}

	/**
	 * The Class MCRunComparator.
	 */
	private class MCRunComparator implements Comparator<MCRun> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(MCRun mcRun1, MCRun mcRun2) {
			return -Double.compare(mcRun1.getErrPartialDeriv(), mcRun2.getErrPartialDeriv());
		}
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		String calibrationFilePath = "calib_idp_10s.zip";
		String sampleFilePath = "etc/IP/testing_samples/1p_10s/1.csv";
		String freqTableFilePath = "etc/ABI IP CAUC freq_COUNTS n=349.csv";
		int npop = 343;
		int maxNOC = 4;		
		int iArgStart = 0;
		
		System.out.println("reading " + args.length + " arguments");
		System.out.println(Arrays.toString(args));
		if (args.length > 0) {
			sampleFilePath = args[iArgStart + 0];
		}
		if (args.length > 1) {
			calibrationFilePath = args[iArgStart + 1];
		}
		if (args.length > 2) {
			maxNOC = Integer.parseInt(args[iArgStart + 2]);
		}

		File sampleFile = new File(sampleFilePath);
		File f = new File(calibrationFilePath);
		if (f.exists()) {
			try {
				Project project = CalibrationProjectHandler.loadProjectData(f);

				if (args.length > 3) {
					freqTableFilePath = args[iArgStart + 3];
					npop=Integer.parseInt(args[iArgStart + 4]);
				}
				if (args.length > 5) {
					Settings.load(args[iArgStart + 5]);
					System.out.println("Settings file : "+args[iArgStart + 5]);
				}
				String[] fElements = freqTableFilePath.split("=");

				String fName = fElements[0].split("/")[1];
				System.out.println("freq : " + fName + " ; n=" + npop);

				FreqTable freqTable = new FreqTable(freqTableFilePath, fName, npop);
				CSVModule csvModule = new CSVModule(sampleFile, project.getCalibration().getKit());

				for (String sampleID : csvModule.getSamples().keySet()) {
					Sample sample = csvModule.getSamples().get(sampleID);

					HashMap<Locus, Integer> analyticalThresholds = new HashMap<>();
					for (Locus locus : sample.getLoci())
						analyticalThresholds.put(locus, 1);
					sample.applyThresholds(analyticalThresholds);

					NOCIt nocIt = new NOCIt(project.getCalibration());
					nocIt.runNOCIt(sampleID, sample, maxNOC, freqTable, analyticalThresholds, null, null);
					System.out.println(nocIt.getResultsString(analyticalThresholds));

					FileWriter foutW = new FileWriter(new File(sampleID + ".out.txt"));
					foutW.write(nocIt.getResultsString(analyticalThresholds));
					foutW.close();
				}
			} catch (InterruptedException | ExecutionException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** The exit code. */
	private ExitCode exitCode;

	/** The quantification parameters. */
	private HashMap<Locus, ArrayList<Double>> quantParams;

	/** The loci list from the sample file. */
	private Collection<Locus> sampleFileLoci;

	/** The maximum number of contributors. */
	private int maxNOC;

	/** The log likelihood for each NOC. */
	private double[] logLs;

	/** The standard error of the log likelihood for each NOC. */
	private double[] logLStdErrs;

	/** The APP distribution on the number of contributors. */
	private double[] nocProbDist;

	/** The standard error of the APP distribution on the number of contributors. */
	private double[] nocProbStdErr;

	private double[] nocRefPriority;

	/** The total time taken by the algorithm. */
	private double timeTaken;

	/** The loci that NOCIt uses. */
	private HashSet<Locus> workingLoci;

	/** The loci that are not in the calibration data. */
	private HashSet<Locus> lociNotInCalib;

	/** The loci that are not in the frequency table. */
	private HashSet<Locus> lociNotInFreqTable;

	/** The calibration. */
	private final Calibration calibration;

	/** The probability model. */
	private ProbabilityModel probabilityModel;

	/** The output file path. */
	private String outputFilePath;

	/** The sample ID. */
	private String sampleID;
	
	/** The sample file name. */
	private String sampleFileName;

	/** The case number. */
	private String caseNumber;

	/** The comments. */
	private String comments;

	/** The number of cores. */
	private int cores;

	/** Flag indicating whether the sample is filtered. */
	private boolean sampleFiltered;

	/** The population name. */
	private String populationName = "";

	/** The population file path. */
	private String populationFilePath = "";

	/** The number of people sampled for the frequency table. */
	private int numPeople;

	/** The lines in the CSV output. */
	private ArrayList<ArrayList<String>> csvOutputLines = new ArrayList<ArrayList<String>>();

	/**
	 * Instantiates a new NOCIt.
	 *
	 * @param calibration
	 *            the calibration
	 */
	public NOCIt(Calibration calibration) {
		this.calibration = calibration;
	}

	/**
	 * Returns the log of the sum of the probabilities for a given locus, set of
	 * quantification parameters, and number of contributors obtained from a
	 * batch of random samples of size batchSize.
	 *
	 * @param locus
	 *            the locus
	 * @param batchSize
	 *            the batch size
	 * @param noc
	 *            the number of contributors
	 * @param quantParams
	 *            the quantification parameters
	 * @return a double array of length 2 (where the first double is the
	 *         mantissa and the second is the exponent with base e) that
	 *         represents the log of the sum of the probabilities
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws ExecutionException
	 *             the execution exception
	 */
	private double[] calcLogLocusSumProbBatch(Locus locus, int batchSize, int noc, double[][] quantParams)
			throws InterruptedException, ExecutionException {
		int threadIterations = batchSize / cores;
		ExecutorService executor = Executors.newFixedThreadPool(cores);
		List<Future<double[]>> futures;
		if (!locus.isAMEL()) { // Non-AMEL
			Set<CalcSTRProbCallable> callables = new HashSet<>();
			for (int j = 1; j <= cores; j++) {
				callables.add(new CalcSTRProbCallable(noc, quantParams, locus, threadIterations));
			}

			futures = executor.invokeAll(callables);
		} else { // AMEL locus
			Set<CalcAMELProbCallable> callables = new HashSet<>();
			for (int j = 1; j <= cores; j++) {
				callables.add(new CalcAMELProbCallable(noc, quantParams, locus, threadIterations));
			}

			futures = executor.invokeAll(callables);
		}

		double logSum = Double.NEGATIVE_INFINITY;
		double logSumSq = Double.NEGATIVE_INFINITY;
		for (Future<double[]> future : futures) {
			double[] futureValues = future.get();
			logSum = UtilityMethods.logSum(logSum, futureValues[0]);
			logSumSq = UtilityMethods.logSum(logSumSq, futureValues[1]);
		}

		executor.shutdown();

		return new double[] { logSum, logSumSq };
	}

	/**
	 * Gets the lines in the CSV output.
	 *
	 * @return the lines in the CSV output
	 */
	public ArrayList<ArrayList<String>> getCsvOutputLines() {
		return csvOutputLines;
	}

	/**
	 * Returns a string describing the results of NOCIt.
	 *
	 * @param analyticalThresholds
	 *            the analytical thresholds
	 * @return the results string
	 */
	public String getResultsString(HashMap<Locus, Integer> analyticalThresholds) {
		File f = new File(this.calibration.getCalibrationPath());
		String hashString = "";
		if (f.exists()) {
			try {
				hashString = "Calibration MD5 Hash: " + MD5CheckSumGenerator.convertToHexMethod1(
						MD5CheckSumGenerator.getMD5Bytes(this.calibration.getCalibrationPath())) + "\n";
			} catch (Exception e) {
				logger.error("Unable to generate MD5 Hash.", e);
				hashString = "Unable to generate MD5 Hash.";
			}
		}
		File f2 = new File(populationFilePath);
		String hashStringPop = "";
		if (f2.exists()) {
			try {
				hashStringPop = "Population File MD5 Hash: "
						+ MD5CheckSumGenerator.convertToHexMethod1(MD5CheckSumGenerator.getMD5Bytes(populationFilePath))
						+ "\n";
			} catch (Exception e) {
				logger.error("Unable to generate MD5 Hash.", e);
				hashStringPop = "Unable to generate MD5 Hash.";
			}
		}

		String results = "";

		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");
		// get current date time with Calendar()
		Calendar cal = Calendar.getInstance();

		results += "User: " + System.getProperty("user.name") + "\n";
		results += "Analysis Run: " + dateFormat.format(cal.getTime()) + "\n";

		results += "\n";

		results += "Case Number: " + caseNumber + "\n";
		results += "Sample ID: " + sampleID + "\n";
		results += "Sample File Name: " + sampleFileName + "\n";
		results += "Comments: " + comments + "\n";
		String calibrationPath = this.calibration.getCalibrationPath();
		results += "Calibration Name: " + UtilityMethods.multiLinePath(calibrationPath) + "\n";
		results += hashString;
		results += "Population Name: " + populationName + "\n";
		// File file = new File(populationFilePath);
		String populationFileReportPath = populationFilePath;
		results += "Population File Name: " + UtilityMethods.multiLinePath(populationFileReportPath) + "\n";
		results += hashStringPop;
		results += "Number of People: " + numPeople + "\n";
		results += "Kit File: " + this.calibration.getKit().getKitName() + "\n";
		results += "Filtered: " + sampleFiltered + "\n";
		if (sampleFiltered) {
			results += "Pull-up Filter Height Ratio: " + Settings.pullUpHeightPct + "\n";
			results += "Pull-up Filter Size Range: " + Settings.pullUpSizeRange + "\n";
			results += "Complex Pull-up Filter Height Ratio: " + Settings.complexPullUpHeightPct + "\n";
			results += "Complex Pull-up Filter Sister Height Ratio: " + Settings.complexPullUpSisterHeightPct + "\n";
			results += "Complex Pull-up Filter Size Range: " + Settings.complexPullUpSizeRange + "\n";
			results += "Minus-A Filter Height Ratio: " + Settings.minusAHeightPct + "\n";
			results += "Minus-A Filter Size Range: " + Settings.minusASizeRange + "\n";
		}

		results += "\n";
		results += "Analytical Thresholds:" + "\n";

		for (Locus locus : analyticalThresholds.keySet()) 
			results += locus.getName() + ": " + analyticalThresholds.get(locus) + "\n";

		// New lines added below so lines do not run off the page.
		Set<Locus> lociWithoutQuantParams = new HashSet<Locus>(sampleFileLoci);
		lociWithoutQuantParams.removeAll(quantParams.keySet());
		for (Locus locus : lociWithoutQuantParams)
			results += "Locus " + locus.getName()
			+ " was not included in the calculation because it does not have reliable\n"
			+ Constants.LINE_TWO_INDENTATION + "quantification parameters.\n";
		for (Locus locus : lociNotInCalib)
			results += "Locus " + locus.getName()
			+ " was not included in the calculation because it is not present in the\n"
			+ Constants.LINE_TWO_INDENTATION + "calibration samples.\n";
		for (Locus locus : lociNotInFreqTable)
			results += "Locus " + locus.getName()
			+ " was not included in the calculation because it is not present in the\n"
			+ Constants.LINE_TWO_INDENTATION + "frequency table.\n";
		for (Locus locus : calibration.getLociNoTrue())
			results += "Locus " + locus.getName()
			+ " was not included in the calculation because it had too few data points\n"
			+ Constants.LINE_TWO_INDENTATION + "for allelic peaks.\n";
		for (Locus locus : calibration.getLociNoNoise())
			results += "Locus " + locus.getName()
			+ " was not included in the calculation because it had too few data points\n"
			+ Constants.LINE_TWO_INDENTATION + "for noise peaks.\n";

		for (Locus locus : sampleFileLoci) {
			if (!calibration.getLociRStutter().contains(locus)) {
				if (!locus.isAMEL()) {
					results += "Locus " + locus.getName() + " had too few data points for reverse stutter.\n";
				}
			}
		}
		for (Locus locus : sampleFileLoci) {
			if (!calibration.getLociFStutter().contains(locus)) {
				if (!locus.isAMEL()) {
					results += "Locus " + locus + " had too few data points for forward stutter.\n";
				}
			}
		}
		results += "\n";

		for (int noc = 0; noc <= maxNOC; noc++) {
			results += "Number of contributors: " + noc + "\n";
			if (logLs[noc] > Double.NEGATIVE_INFINITY) {
				results += "Likelihood: exp(" + logLs[noc] + ") +/- exp(" + logLStdErrs[noc] + ")\n";						
				results += "Probability: " + nocProbDist[noc] + " +/- " + nocProbStdErr[noc] + "\n";
			} else {
				results += "Likelihood: 0.0\n";
				results += "Probability: " + nocProbDist[noc] + "\n";
			}
			results += "\n";
		}

//		results += "Total number of calcLocusProb calls: " + probabilityModel.getCalcProbCount() + "\n";
		results += "Time taken: " + timeTaken / 1000 + " s\n";

		csvOutputLines = new ArrayList<ArrayList<String>>();
		for (Locus locus : sampleFileLoci) {
			ArrayList<String> csvOutputLine = new ArrayList<String>();
			csvOutputLine.add(outputFilePath);
			csvOutputLine.add(sampleID);
			csvOutputLine.add(sampleFileName);
			csvOutputLine.add(caseNumber);
			csvOutputLine.add(comments);
			csvOutputLine.add(calibrationPath);
			csvOutputLine.add(populationFilePath);
			csvOutputLine.add(populationName);
			csvOutputLine.add(Integer.toString(numPeople));
			csvOutputLine.add(this.calibration.getKit().getKitName());
			csvOutputLine.add(Boolean.toString(sampleFiltered));
			csvOutputLine.add(Double.toString(Settings.pullUpHeightPct));
			csvOutputLine.add(Double.toString(Settings.pullUpSizeRange));
			csvOutputLine.add(Double.toString(Settings.complexPullUpHeightPct));
			csvOutputLine.add(Double.toString(Settings.complexPullUpSisterHeightPct));
			csvOutputLine.add(Double.toString(Settings.complexPullUpSizeRange));
			csvOutputLine.add(Double.toString(Settings.minusAHeightPct));
			csvOutputLine.add(Double.toString(Settings.minusASizeRange));
			csvOutputLine.add(Integer.toString(Settings.numProcessors));
			csvOutputLine.add(Integer.toString(Settings.thetaNumLevels));
			csvOutputLine.add(Double.toString(Settings.nocItStdErrorTol));
			csvOutputLine.add(Integer.toString((int) (Settings.nocItTimeLimit / 1000)));
			csvOutputLine.add(Integer.toString(Settings.numSamples1));
			csvOutputLine.add(Double.toString(Settings.numSamplesInc));
			csvOutputLine.add(Double.toString(Settings.maxNumSamples));

			csvOutputLine.add(Integer.toString(maxNOC));

			for (int noc = 0; noc <= 5; noc++) {
				if (noc <= maxNOC) {
					csvOutputLine.add(Double.toString(logLs[noc]));
					csvOutputLine.add(Double.toString(nocProbDist[noc]));
				} else {
					csvOutputLine.add("N/A");
					csvOutputLine.add("N/A");
				}
			}

			csvOutputLine.add(locus.toString());
			if (analyticalThresholds.get(locus) != null) {
				csvOutputLine.add(Integer.toString(analyticalThresholds.get(locus)));
			} else {
				csvOutputLine.add("N/A");
			}

			csvOutputLines.add(csvOutputLine);
		}

		boolean errorToleranceReached = false;
		boolean timeLimitReached = false;
		switch (exitCode) {
		case ERROR_TOL_REACHED:
			results += "Standard error tolerance " + Settings.nocItStdErrorTol + " reached.\n";
			errorToleranceReached = true;
			break;
		case TIME_LIMIT_REACHED:
			results += "Refinement time limit " + Settings.nocItTimeLimit + " ms reached.\n";
			timeLimitReached = true;
			break;
		default:
			break;
		}

		for (int i = 0; i < csvOutputLines.size(); i++) {
			csvOutputLines.get(i).add(Boolean.toString(errorToleranceReached));
			csvOutputLines.get(i).add(Boolean.toString(timeLimitReached));
		}

		return results;
	}

	/**
	 * Graph bar chart.
	 *
	 * @param title
	 *            the chart title
	 * @return the bar chart
	 */
	@SuppressWarnings({ "unchecked" })
	public BarChart<String, Number> graphBarChart(String title) {
		final CategoryAxis xAxis = new CategoryAxis();
		final NumberAxis yAxis = new NumberAxis(0.0, 1.0, 0.1);
		final BarChart<String, Number> bc = new BarChart<String, Number>(xAxis, yAxis);
		bc.setLegendVisible(false);
		// bc.setLegendSide(Side.BOTTOM);
		bc.setAnimated(false);
		bc.setVerticalGridLinesVisible(false);

		bc.setTitle(title);
		Tooltip t = new Tooltip(title);
		Tooltip.install(bc, t);
		xAxis.setLabel("NOC");
		yAxis.setLabel("Probability");

		// Based on code from
		// http://www.programcreek.com/java-api-examples/index.php?api=javafx.scene.chart.BarChart
		// fixes bug where bars don't line up with x axis increments.
		List<XYChart.Data<String, Number>> entries = new ArrayList<>(maxNOC);
		for (int n = 0; n <= maxNOC; n++) {
			entries.add(new XYChart.Data<>(Integer.toString(n), nocProbDist[n]));
		}

		XYChart.Series<String, Number> series1 = new XYChart.Series<>(FXCollections.observableList(entries));
		bc.getData().addAll(series1);

		// changes color of all nodes to color defined in Constants
		for (int i = 0; i < entries.size(); i++) {
			for (Node node : bc.lookupAll(".default-color" + i + ".chart-bar")) {
				node.setStyle("-fx-bar-fill: " + Constants.DEFAULT_BAR_GRAPH_CSS_COLOR_NAME + ";");
			}
		}

		AnchorPane.setTopAnchor(bc, 0.0);
		AnchorPane.setBottomAnchor(bc, 0.0);

		return bc;
	}

	private void calcAPP() {
		double maxLogL = Double.NEGATIVE_INFINITY;
		for (int noc = 0; noc < logLs.length; noc++) {
			if (logLs[noc] > maxLogL) {
				maxLogL = logLs[noc];
			}
		}

		double[] like = new double[maxNOC + 1];
		double[] likeStdErr = new double[maxNOC + 1];
		for (int noc = 0; noc < logLs.length; noc++) {
			like[noc] = FastMath.exp(logLs[noc] - maxLogL);
			likeStdErr[noc] = FastMath.exp(logLStdErrs[noc] - maxLogL); 
		}

		double totalLike = 0.0;
		double totalLikeVar = 0.0;
		for (int noc = 0; noc < logLs.length; noc++) {
			totalLike += like[noc];
			totalLikeVar += likeStdErr[noc] * likeStdErr[noc];			
		}

		if (totalLikeVar < totalLike * totalLike) {
			for (int noc = 0; noc < logLs.length; noc++) {
				nocProbDist[noc] = like[noc] / totalLike;
				nocProbStdErr[noc] = nocProbDist[noc] * FastMath.sqrt(FastMath.exp(2 * (logLStdErrs[noc] - logLs[noc]))
						+ totalLikeVar / totalLike / totalLike 
						- 2 * FastMath.exp(logLStdErrs[noc] - logLs[noc]) * likeStdErr[noc] / totalLike);
				nocRefPriority[noc] = nocProbStdErr[noc];
			}
		}
		else {
			for (int noc = 0; noc < logLs.length; noc++) {
				nocProbDist[noc] = like[noc] / totalLike;				
				nocProbStdErr[noc] = 1.0;
				nocRefPriority[noc] = likeStdErr[noc];
			}
		}
	}

	/**
	 * Calculates the a posteriori probability (APP) on the number of
	 * contributors over the range 0 to maxNOC.
	 *
	 * @param sampleID
	 *            the sample ID
	 * @param sample
	 *            the sample
	 * @param maxNOC
	 *            the max NOC
	 * @param freqTable
	 *            the frequency table
	 * @param analyticalThresholds
	 *            the analytical thresholds
	 * @param backendController
	 *            the backend controller            
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws ExecutionException
	 *             the execution exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void runNOCIt(String sampleID, Sample sample, int maxNOC, FreqTable freqTable, HashMap<Locus, Integer> analyticalThresholds, 
			String outputFilePath, BackendController backendController)
					throws InterruptedException, ExecutionException, IOException {

		lociNotInFreqTable = new HashSet<>();
		lociNotInCalib = new HashSet<>();
		workingLoci = new HashSet<>();

		// Inputs are collected below
		this.maxNOC = maxNOC;
		
		System.out.println("NOC "+maxNOC);
		System.out.println("TimeLimit "+Settings.nocItTimeLimit);
		System.out.println("Tolerance "+Settings.nocItStdErrorTol);
		System.out.println("NumProc "+Settings.numProcessors);
		
		freqTable.calcProbDists(calibration.getKit(), sample);
		System.out.println("Loci in freqTable : " + Arrays.deepToString(freqTable.getLoci().toArray()));

		// Process the sample file
		this.sampleID = sampleID;
		this.outputFilePath = outputFilePath;
		
		sample.calcQuantParams(calibration.getKit());
		sample.calcHeightDist(freqTable);

		sampleFiltered = sample.isFiltered();		
		sampleFileName = sample.getSampleFileName();
		sampleFileLoci = sample.getLoci();
		quantParams = sample.getQuantParams();
		populationName = freqTable.getName();
		caseNumber = sample.populationCaseNumberMap.get(populationName);
		comments = sample.populationCommentsMap.get(populationName);
		populationFilePath = freqTable.getFilePath();
		numPeople = freqTable.getNumPeople();

		System.out.println("Quantification parameters");
		for (Locus locus : quantParams.keySet()) {
			System.out.println(locus + " " + quantParams.get(locus).toString());
		}
		System.out.println();

		probabilityModel = new ProbabilityModel(sample, freqTable, calibration, analyticalThresholds);

		for (Locus locus : quantParams.keySet()) {
			if (calibration.getLoci().contains(locus)) {
				if (!calibration.getLociNoTrue().contains(locus) && !calibration.getLociNoTrue().contains(locus)) {
					if (!locus.isAMEL()) {
						if (freqTable.getLoci().contains(locus)) {
							if (calibration.getLociRStutter().contains(locus)
									&& calibration.getLociFStutter().contains(locus)) {
								workingLoci.add(locus);
								System.out.println("working on locus " + locus);
							}
						} else {
							lociNotInFreqTable.add(locus);
						}
					} else { // Locus is AMEL
						workingLoci.add(locus);
						System.out.println("working on locus " + locus);
					}
				}
			} else {
				lociNotInCalib.add(locus);
			}
		}

		cores = Settings.numProcessors;

		double startTime = System.currentTimeMillis();

		logLs = new double[maxNOC + 1];
		logLStdErrs = new double[maxNOC + 1];
		nocProbDist = new double[maxNOC + 1];
		nocProbStdErr = new double[maxNOC + 1];
		nocRefPriority = new double[maxNOC + 1];

		// Calculate log likelihood for 0 contributors
		double logL0 = 0.0;
		for (Locus locus : workingLoci)
			logL0 += probabilityModel.calcLogProbBernoulli(locus, new STRAllele[0], new double[2][0]);

		logLs[0] = logL0;
		logLStdErrs[0] = Double.NEGATIVE_INFINITY;

		HashMap<Integer, PriorityQueue<MCRun>> mcRunQueues = new HashMap<>();
		HashMap<Integer, HashMap<double[][], Double>> logThetaProbMap = new HashMap<>();
		HashMap<Integer, HashMap<double[][], Double>> logThetaProbErrVarMap = new HashMap<>();
		int numSamples = Settings.numSamples1;
		for (int noc = 1; noc <= maxNOC; noc++) {
			// Calculate initial estimate of log likelihood and its standard
			// error for each n
			mcRunQueues.put(noc, new PriorityQueue<>(Settings.thetaNumLevels - 1, new MCRunComparator()));
			logThetaProbMap.put(noc, new HashMap<>());
			logThetaProbErrVarMap.put(noc, new HashMap<>());

			if (Settings.thetaNumLevels >= noc) {
				Iterator<int[]> iter = CombinatoricsUtils.combinationsIterator(Settings.thetaNumLevels - 1, noc - 1);
				while (iter.hasNext()) {
					int[] comb = iter.next();

					double[] theta = new double[noc];
					double logThetaPriorProb;
					if (comb.length > 0) {
						int[] intTheta = new int[noc];

						intTheta[0] = 1 + comb[0];
						for (int i = 1; i < comb.length; i++) {
							intTheta[i] = comb[i] - comb[i - 1];
						}
						intTheta[noc - 1] = Settings.thetaNumLevels - 1 - comb[noc - 2];

						boolean isSorted = true;
						for (int i = 1; i < intTheta.length; i++) {
							if (intTheta[i] > intTheta[i - 1]) {
								isSorted = false;
								break;
							}
						}

						if (!isSorted) {
							continue;
						}

						for (int i = 0; i < theta.length; i++) {
							theta[i] = ((double) intTheta[i]) / Settings.thetaNumLevels;
						}

						logThetaPriorProb = CombinatoricsUtils.factorialLog(noc)
								- CombinatoricsUtils.binomialCoefficientLog(Settings.thetaNumLevels - 1, noc - 1);

						int[] intThetaCount = new int[Settings.thetaNumLevels];
						for (int i = 0; i < intTheta.length; i++) {
							intThetaCount[intTheta[i]]++;
						}

						for (int i = 0; i < intThetaCount.length; i++) {
							logThetaPriorProb -= CombinatoricsUtils.factorialLog(intThetaCount[i]);
						}
					} else {
						theta[0] = 1.0;
						logThetaPriorProb = 0.0;
					}

					HashMap<double[], HashMap<Locus, double[][]>> relThetaParamsMap = sample
							.generateDiffDegParams(theta);
					System.out.println(Arrays.toString(theta) + " " + relThetaParamsMap.size());

					for (double[] relTheta : relThetaParamsMap.keySet()) {
						double[][] fullTheta = new double[][] { theta, relTheta };

						double logThetaProb = logThetaPriorProb - FastMath.log(relThetaParamsMap.size());
						double log2ndMoment = 2 * (logThetaPriorProb - FastMath.log(relThetaParamsMap.size()));
						int rfact= (int) Math.min(relThetaParamsMap.size(), 
								Math.round(Math.floor(5 * Math.log10(relThetaParamsMap.size()) + 1)));
                      
						ArrayList<MCRun> mcRunList = new ArrayList<>();
						for (Locus locus : workingLoci) {
							MCRun mcRun = new MCRun(noc, fullTheta, relThetaParamsMap.get(relTheta).get(locus), locus,
									logThetaPriorProb, numSamples / relThetaParamsMap.size() * rfact);
							mcRun.sample();
							mcRunList.add(mcRun);

							logThetaProb += mcRun.getLogMean();
							log2ndMoment += mcRun.getLog2ndMoment();
						}

						double logThetaProbErrVar = UtilityMethods.logDiff(log2ndMoment, 2 * logThetaProb);

						for (MCRun mcRun : mcRunList) {
							mcRun.setErrPartialDeriv(log2ndMoment - mcRun.getLog2ndMoment() + mcRun.getLogErrVar()
								- 3.0 / 2 * FastMath.log(mcRun.getNumSamples()));
							mcRunQueues.get(noc).add(mcRun);
						}

						logThetaProbMap.get(noc).put(fullTheta, logThetaProb);
						logThetaProbErrVarMap.get(noc).put(fullTheta, logThetaProbErrVar);
					}
				}

				double logL = Double.NEGATIVE_INFINITY;
				for (double logThetaProb : logThetaProbMap.get(noc).values()) {
					logL = UtilityMethods.logSum(logL, logThetaProb);
				}

				double logLErrVar = Double.NEGATIVE_INFINITY;
				for (double logThetaProbErrVar : logThetaProbErrVarMap.get(noc).values()) {
					logLErrVar = UtilityMethods.logSum(logLErrVar, logThetaProbErrVar);
				}

				numSamples =  Math.min(Settings.maxNumSamples, (int) (numSamples * Settings.numSamplesInc));
				logLs[noc] = logL;
				logLStdErrs[noc] = 0.5 * logLErrVar;
			}

			if (backendController != null)
				backendController.updateNOCItProgress(0.5 * ((double) noc) / maxNOC);
		}

		calcAPP();

		// Heuristic to select noc for further sampling
		double totalRefPriority = 0.0;
		for (int noc = 1; noc < logLs.length; noc++) 
			totalRefPriority += nocRefPriority[noc];

		double rand = ThreadLocalRandom.current().nextDouble();
		int noc = 1;
		while (noc < maxNOC && rand > nocRefPriority[noc] / (totalRefPriority)) {
			rand -= nocRefPriority[noc] / (totalRefPriority);
			noc++;
		}

		double maxProbStdErr = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < nocProbStdErr.length; i++) {
			if (nocProbStdErr[i] > maxProbStdErr) {
				maxProbStdErr = nocProbStdErr[i];
			}
		}

		// Continue sampling selectively until error tolerance is met
		while (true) {
		    Double errMax=maxProbStdErr;
			if (maxProbStdErr < Settings.nocItStdErrorTol) {
				exitCode = ExitCode.ERROR_TOL_REACHED;
				break;
			}

			if (Settings.nocItTimeLimit > 0 && System.currentTimeMillis() - startTime > Settings.nocItTimeLimit) {
				exitCode = ExitCode.TIME_LIMIT_REACHED;
				break;
			}

			MCRun maxErrMCRun = mcRunQueues.get(noc).poll();
			maxErrMCRun.sample();
            if(errMax- maxProbStdErr > Settings.nocItStdErrorTol/10 ) {
			System.out.println(noc + " " + maxProbStdErr + " " + maxErrMCRun.getErrPartialDeriv() + " "
					+ Arrays.toString(logLs) + " " + Arrays.toString(logLStdErrs));
					errMax=maxProbStdErr ; 
            }

			double maxErrLogThetaProb = maxErrMCRun.getLogThetaPriorProb() + maxErrMCRun.getLogMean();
			double maxErrLog2ndMoment = 2 * maxErrMCRun.getLogThetaPriorProb() + maxErrMCRun.getLog2ndMoment();

			for (MCRun mcRun : mcRunQueues.get(noc)) {
				if (mcRun.getTheta() == maxErrMCRun.getTheta()) {
					maxErrLogThetaProb += mcRun.getLogMean();
					maxErrLog2ndMoment += mcRun.getLog2ndMoment();
				}
			}

			double maxErrLogThetaProbErrVar = UtilityMethods.logDiff(maxErrLog2ndMoment, 2 * maxErrLogThetaProb);

			maxErrMCRun.setErrPartialDeriv(maxErrLog2ndMoment - maxErrMCRun.getLog2ndMoment()
					+ maxErrMCRun.getLogErrVar() - 3.0 / 2 * FastMath.log(maxErrMCRun.getNumSamples()));
			mcRunQueues.get(noc).add(maxErrMCRun);

			logThetaProbMap.get(noc).put(maxErrMCRun.getTheta(), maxErrLogThetaProb);
			logThetaProbErrVarMap.get(noc).put(maxErrMCRun.getTheta(), maxErrLogThetaProbErrVar);

			double logL = Double.NEGATIVE_INFINITY;
			for (double logThetaProb : logThetaProbMap.get(noc).values()) {
				logL = UtilityMethods.logSum(logL, logThetaProb);
			}

			double logLErrVar = Double.NEGATIVE_INFINITY;
			for (double logThetaProbErrVar : logThetaProbErrVarMap.get(noc).values()) {
				logLErrVar = UtilityMethods.logSum(logLErrVar, logThetaProbErrVar);
			}

			logLs[noc] = logL;
			logLStdErrs[noc] = 0.5 * logLErrVar;

			calcAPP();

			// Heuristic to select noc for further sampling
			totalRefPriority = 0.0;
			for (noc = 1; noc < logLs.length; noc++) 
				totalRefPriority += nocRefPriority[noc];

			rand = ThreadLocalRandom.current().nextDouble();
			noc = 1;
			while (noc < maxNOC && rand > nocRefPriority[noc] / (totalRefPriority)) {
				rand -= nocRefPriority[noc] / (totalRefPriority);
				noc++;
			}

			maxProbStdErr = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < nocProbStdErr.length; i++) {
				if (nocProbStdErr[i] > maxProbStdErr) {
					maxProbStdErr = nocProbStdErr[i];
				}
			}
		}

		if (backendController != null)
			backendController.updateNOCItProgress(1.0);

		timeTaken = System.currentTimeMillis() - startTime;

		System.out.println("logLs = " + Arrays.toString(logLs));
		System.out.println("logLStdErrs = " + Arrays.toString(logLStdErrs));
	}

}
