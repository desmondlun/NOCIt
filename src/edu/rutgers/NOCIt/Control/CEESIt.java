/*
 * Calculate Pr(E|POI genotype) for a person of interest (POI). Also compute a p-value for the same.
 * All possible POI probabilities are computed.
 * Constant mixture ratio across all loci: a constant number is used per n. 
 * Importance sampling is used for interference genotypes.
 * Tree algorithm is used to calculate p value: first random, then deterministic.
 */

package edu.rutgers.NOCIt.Control;

import static edu.rutgers.NOCIt.UIMain.logger;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import com.sun.javafx.charts.Legend;

import edu.rutgers.NOCIt.Data.AMELAllele;
import edu.rutgers.NOCIt.Data.Allele;
import edu.rutgers.NOCIt.Data.CSVModule;
import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.FreqTable;
import edu.rutgers.NOCIt.Data.Genotype;
import edu.rutgers.NOCIt.Data.Locus;
import edu.rutgers.NOCIt.Data.MD5CheckSumGenerator;
import edu.rutgers.NOCIt.Data.STRAllele;
import edu.rutgers.NOCIt.Data.Sample;
import edu.rutgers.NOCIt.Data.UtilityMethods;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * This class implements CEESIt.
 *
 * @author Harish Swaminathan
 * @author Desmond Lun
 * @author James Kelley
 */
public class CEESIt {
	private class AllelesLogProb {
		private double logProb;
		private List<Allele> alleles;
		
		AllelesLogProb(List<Allele> alleles, double logProb) {
			this.logProb = logProb;
			this.alleles = alleles;
		}
		
		public double getLogProb() {
			return logProb;
		}
		
		public List<Allele> getAlleles() {
			return alleles;
		}			
	}
	
	private class CalcLogProbCallable implements Callable<AllelesLogProb> {
		private Locus locus;
		private List<Allele> alleles;
		private double[][] quantParams;
		
		CalcLogProbCallable(Locus locus, List<Allele> alleles, double[][] quantParams) {
			this.locus = locus;
			this.alleles = alleles;
			this.quantParams = quantParams;
		}

		@Override
		public AllelesLogProb call() throws Exception {
			if (alleles.size() == 2 * noc) {
				Allele[] allelesArray = alleles.toArray(new Allele[alleles.size()]);
				double[] prob = probabilityModel.calcProbIntegrate(locus, allelesArray, quantParams);
				return new AllelesLogProb(alleles.subList(0, 2), FastMath.log(prob[0]) + prob[1]);
			}
			
			double logProbSum = Double.NEGATIVE_INFINITY;
			double logProbSumSq = Double.NEGATIVE_INFINITY;
			int numSamples = 0;

			int numIterations = (int) (numSamples1 * FastMath.pow(numSamplesInc, noc - 2));

			double logLocusMean;
			double logLocusErrVar;

			do {
				if (locus.isAMEL()) {
					AMELAllele[] trueAlleles = new AMELAllele[2 * noc];
					for (int i = 0; i < alleles.size(); i++)
						trueAlleles[i] = (AMELAllele) alleles.get(i);
					
					for (int iteration = 1; iteration <= numIterations; iteration++) { 
						for (int contributor = alleles.size() / 2; contributor < noc; contributor++) { 
							AMELAllele[] alleles = probabilityModel.sampleAMELAllelePair();
							trueAlleles[2 * contributor] = alleles[0];
							trueAlleles[2 * contributor + 1] = alleles[1];
						}

						double[] prob = probabilityModel.calcProbIntegrate(locus, trueAlleles, quantParams);
						double logProb = FastMath.log(prob[0]) + prob[1]; 

						logProbSum = UtilityMethods.logSum(logProbSum, logProb);
						logProbSumSq = UtilityMethods.logSum(logProbSumSq, 2 * logProb);
					}
				} else {
					STRAllele[] trueAlleles = new STRAllele[2 * noc];
					for (int i = 0; i < alleles.size(); i++)
						trueAlleles[i] = (STRAllele) alleles.get(i);
					
					for (int iteration = 1; iteration <= numIterations; iteration++) {
						double alleleProb = 1.0;
						double heightProb = 1.0;			
						for (int contributor = alleles.size() / 2; contributor < noc; contributor++) { 
							STRAllele allele1 = probabilityModel.sampleAlleleByHeight(locus);
							STRAllele allele2 = probabilityModel.sampleAlleleByHeight(locus);
							
							trueAlleles[2 * contributor] = allele1;
							trueAlleles[2 * contributor + 1] = allele2;
							
							if (!allele1.equals(allele2)) {
								alleleProb *= probabilityModel.getAlleleProbByFreq(locus, allele1)
										* probabilityModel.getAlleleProbByFreq(locus, allele2) * (1 - popSubstructureAdj);
							}
							else {
								double p = probabilityModel.getAlleleProbByFreq(locus, allele1);
								alleleProb *= p * p + p * (1 - p) * popSubstructureAdj;
							}
							
							heightProb *= probabilityModel.getAlleleProbByHeight(locus, allele1)
									* probabilityModel.getAlleleProbByHeight(locus, allele2);
						}

						double[] prob = probabilityModel.calcProbIntegrate(locus, trueAlleles, quantParams);
						double logProb = FastMath.log(alleleProb / heightProb * prob[0]) + prob[1]; 

						logProbSum = UtilityMethods.logSum(logProbSum, logProb);
						logProbSumSq = UtilityMethods.logSum(logProbSumSq, 2 * logProb);
					}
				}

				numSamples += numIterations;

				logLocusMean = logProbSum - FastMath.log(numSamples);
				logLocusErrVar = UtilityMethods.logDiff(logProbSumSq, 2 * logProbSum - FastMath.log(numSamples))
						- FastMath.log((numSamples - 1) * numSamples);
			} while (logLocusErrVar / 2 - logLocusMean > FastMath.log(genotypeTolerance));
			
//			System.out.println(locus + " " + alleles + " " + numSamples);

			return new AllelesLogProb(alleles.subList(0, 2), logLocusMean);
		}
		
	}

	/**
	 * Callable that generates random POI genotypes and calculates the number of
	 * samples at least as large as the POI likelihood and the sum of log
	 * likelihoods.
	 */
	private class RandomPOICallable implements Callable<double[]> {

		/** The number of iterations. */
		private final long numIterations;

		/**
		 * Instantiates a new random POI callable.
		 *
		 * @param numIterations
		 *            the number of iterations
		 */
		RandomPOICallable(long numIterations) {
			this.numIterations = numIterations;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public double[] call() throws IOException {
			long numAtLeast = 0;
			double llSum = Double.NEGATIVE_INFINITY;
			long numIter = 0;

			HashMap<Locus, Allele[]> sampGeno = new HashMap<>();							

			for (long i = 0; i < numIterations; i++) {
				for (Locus locus : workingLoci) {
					if (locus.isAMEL()) 
						sampGeno.put(locus, probabilityModel.sampleAMELAllelePair());
					else
						sampGeno.put(locus, probabilityModel.sampleAllelePairByFreq(locus));						
				}
								
				boolean equalsPoiGeno = true;
				for (Locus locus : workingLoci) 
					equalsPoiGeno &= Arrays.equals(sampGeno.get(locus), poiGenotype.getAlleles(locus));							
										
				if (equalsPoiGeno)
					continue;
				
				double[] mixRatioProbs = new double[thetas.size()];

				double max = Double.NEGATIVE_INFINITY;
				for (int k = 0; k < thetas.size(); k++) { 
					double mixRatioProb = 0.0;
					for (Locus locus : workingLoci) { // Each locus
						List<Allele> geno = Arrays.asList(sampGeno.get(locus));
						mixRatioProb += genoProbs.get(locus).get(geno).get(k);
					}

					mixRatioProbs[k] = mixRatioProb;

					if (mixRatioProb > max)
						max = mixRatioProb;
				}

				double logLikelihood;
				if (max == Double.NEGATIVE_INFINITY) {
					logLikelihood = Double.NEGATIVE_INFINITY;
				} else {
					double sum = 0.0;
					for (double logValue : mixRatioProbs)
						sum += FastMath.exp(logValue - max);

					logLikelihood = max + FastMath.log(sum); // Mix Ratio probs
					logLikelihood -= FastMath.log(thetas.size());
				}

				llSum = UtilityMethods.logSum(llSum, logLikelihood);

				double llHistKey = FastMath.floor(logLikelihood / minBinWidth);
				if (!llHist.containsKey(llHistKey))
					llHist.put(llHistKey, new LongAdder());
				llHist.get(llHistKey).increment();

				if (logLikelihood >= poiLogProb)
					numAtLeast++;
				
				numIter++;
			}

			return new double[] { numAtLeast, llSum, numIter };
		}
	}

	/**
	 * The standard error tolerance for the probability of evidence given a
	 * locus, set of quantification parameters, and number of contributors.
	 */
	private double genotypeTolerance = Settings.genotypeTolerance;
	private double minBinWidth = Settings.binWidthFactor / 10 * FastMath.log(10);
	private int maxNumLlrHistBins = Settings.numBins;
	private int numSamples1 = Settings.numSamples1CEESIt;
	private double numSamplesInc = Settings.numSamplesIncCEESIt;
	private int thetaNumLevels = Settings.thetaNumLevelsCEESIt;
	private long numPoiSamples = Settings.numberPOISamples;
	private double popSubstructureAdj = Settings.popSubstructureAdj;

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		File sampleFile = new File("etc/IP/testing_samples/1p_10s/1.csv");
//		File sampleFile = new File("etc/IP/calibration samples - 10s/53.csv");
//		File sampleFile = new File("etc/IP/testing_samples/2p_10s/1.csv");		
		String calibrationFilePath = "calib_idp_10s.zip";
		String freqTableFilePath = "etc/ABI IP CAUC freq_COUNTS n=349.csv";
		
//		File sampleFile = new File("etc/EF_2p_RD12-0002(011613CMG_5sec)_F-failed_sample1.csv");
//		String calibrationFilePath = "CP_RD12_IP_5s_F.zip";
//		String freqTableFilePath = "etc/FF_IP_CAUC_n=349.csv";
		
		int noc = 1;
		
		Genotype poiGenotype = new Genotype("01");
		poiGenotype.putAlleles(new Locus("D8S1179"),
				new STRAllele[] { new STRAllele("13"), new STRAllele("14") });
		poiGenotype.putAlleles(new Locus("D21S11"),
				new STRAllele[] { new STRAllele("27"), new STRAllele("29") });
		poiGenotype.putAlleles(new Locus("D7S820"),
				new STRAllele[] { new STRAllele("8"), new STRAllele("12") });
		poiGenotype.putAlleles(new Locus("CSF1PO"),
				new STRAllele[] { new STRAllele("10"), new STRAllele("12") });
		poiGenotype.putAlleles(new Locus("D3S1358"),
				new STRAllele[] { new STRAllele("16"), new STRAllele("18") });
		poiGenotype.putAlleles(new Locus("TH01"), new STRAllele[] { new STRAllele("7"), new STRAllele("8") });
		poiGenotype.putAlleles(new Locus("D13S317"),
				new STRAllele[] { new STRAllele("12"), new STRAllele("14") });
		poiGenotype.putAlleles(new Locus("D16S539"),
				new STRAllele[] { new STRAllele("8"), new STRAllele("9") });
		poiGenotype.putAlleles(new Locus("D2S1338"),
				new STRAllele[] { new STRAllele("17"), new STRAllele("17") });
		poiGenotype.putAlleles(new Locus("D19S433"),
				new STRAllele[] { new STRAllele("13"), new STRAllele("13") });
		poiGenotype.putAlleles(new Locus("vWA"),
				new STRAllele[] { new STRAllele("17"), new STRAllele("19") });
		poiGenotype.putAlleles(new Locus("TPOX"),
				new STRAllele[] { new STRAllele("9"), new STRAllele("10") });
		poiGenotype.putAlleles(new Locus("D18S51"),
				new STRAllele[] { new STRAllele("15"), new STRAllele("16") });
		poiGenotype.putAlleles(new Locus("AMEL"),
				new AMELAllele[] { new AMELAllele("X"), new AMELAllele("Y") });
		poiGenotype.putAlleles(new Locus("D5S818"),
				new STRAllele[] { new STRAllele("12"), new STRAllele("12") });
		poiGenotype.putAlleles(new Locus("FGA"),
				new STRAllele[] { new STRAllele("22"), new STRAllele("24") });
		
		List<Genotype> knownGenotypes = new ArrayList<Genotype>();
//		knownGenotypes.add(poiGenotype);

		File f = new File(calibrationFilePath);
		if (f.exists()) {
			try {
				Project project = CalibrationProjectHandler.loadProjectData(f);
				FreqTable freqTable = new FreqTable(freqTableFilePath, "Caucasian", 349);
				CSVModule csvModule = new CSVModule(sampleFile, project.getCalibration().getKit());

				for (String sampleID : csvModule.getSampleNames()) {
					Sample sample = csvModule.getSamples().get(sampleID);

					HashMap<Locus, Integer> analyticalThresholds = new HashMap<>();
					for (Locus locus : sample.getLoci())
						analyticalThresholds.put(locus, 50);
					sample.applyThresholds(analyticalThresholds);

					CEESIt ceesIt = new CEESIt(project.getCalibration());
					ceesIt.runCEESIt(sampleID, sample, noc, poiGenotype, knownGenotypes, freqTable,
							analyticalThresholds, null, null);
					System.out.println(ceesIt.getResultsString(analyticalThresholds));	
				}
			} catch (InterruptedException | ExecutionException | IOException | NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/** The number of processors. */
	private int numProcessors;

	/** The mixture ratios. */
	private ArrayList<double[]> thetas = new ArrayList<>();

	/** The log probability of the evidence for the POI. */
	private double poiLogProb;

	/** The LR for the POI. */
	private double poiLLR;

	/** The p value for the poi. */
	private double poiPValue;
	
	private Genotype poiGenotype;
	private List<Genotype> knownGenotypes;

	/**
	 * The probability of all the genotypes at each locus.
	 */
	private HashMap<Locus, HashMap<List<Allele>, ArrayList<Double>>> genoProbs = new HashMap<>(); //

	/** The loci that CEESIt uses. */
	private HashSet<Locus> workingLoci = new HashSet<Locus>();

	/** The loci that are not in the calibration data. */
	private HashSet<Locus> lociNotInCalib = new HashSet<Locus>();

	/** The loci that are not in the frequency table. */
	private HashSet<Locus> lociNotInFreqTable = new HashSet<Locus>();

	/** The loci not in the POI genotype. */
	private HashSet<Locus> lociNotInPoiGenotype = new HashSet<Locus>();

	/** The sample file loci. */
	private Set<Locus> sampleFileLoci;

	/** The quantification parameters. */
	private HashMap<Locus, ArrayList<Double>> quantParams;

	/** The number of contributors. */
	private int noc;

	/** The calibration. */
	private final Calibration calibration;
	
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
	
	/** Flag indicating whether the sample is filtered. */
	private boolean sampleFiltered;

	/** The population name. */
	private String populationName = "";

	/** The population file path. */
	private String populationFilePath = "";

	/** The number of people sampled for the frequency table. */
	private int numPeople;

	/** The time taken. */
	private double timeTaken;
	/** The log likelihood histogram. */
	private ConcurrentHashMap<Double, LongAdder> llHist = new ConcurrentHashMap<>();

	/** The log likelihood ratio histogram. */
	private NavigableMap<Double, LongAdder> llrHist = new TreeMap<>();

	/** The log likelihood ratio histogram bin width. */
	private double llrHistBinWidth;

	/** The probability model. */
	private ProbabilityModel probabilityModel;
	
	/** The lines in the CSV output. */
	private ArrayList<ArrayList<String>> csvOutputLines = new ArrayList<ArrayList<String>>();
	
	private long numLRGTOne = 0;
	private double probLRGTOne = 0.0;

	/**
	 * Instantiates a new CEESIt.
	 *
	 * @param calibration
	 *            the calibration
	 */
	public CEESIt(Calibration calibration) {
		this.calibration = calibration;
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
	 * Returns a string describing the results of CEESIt.
	 *
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

		Set<Locus> lociWithoutQuantParams = new HashSet<Locus>(sampleFileLoci);
		lociWithoutQuantParams.removeAll(quantParams.keySet());
		for (Locus locus : lociWithoutQuantParams)
			results += "Locus " + locus.getName()
					+ " was not included in the calculation because it does not have reliable\n"
					+ Constants.LINE_TWO_INDENTATION + "quantification parameters.\n";
		for (Locus locus : lociNotInPoiGenotype)
			results += "Locus " + locus.getName()
					+ " was not included in the calculation because it is not present in the\n"
					+ Constants.LINE_TWO_INDENTATION + "POI genotype.\n";
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
		
		String knownGenotypesString = Constants.CEESIT_NO_KNOWN_CONTRIBUTORS_ENTRY;
		if (knownGenotypes != null && knownGenotypes.size() > 0) {
			knownGenotypesString = knownGenotypes.get(0).getGenotypeID();
			for (int i = 1; i < knownGenotypes.size(); i++)
				knownGenotypesString += "," + knownGenotypes.get(i).getGenotypeID();
		}

		results += "Number of contributors: " + noc + "\n";
		results += "Time taken: " + timeTaken + " s\n\n";
		results += "Number of samples: " + numPoiSamples + "\n\n";
		results += "POI genotype ID: " + poiGenotype.getGenotypeID() + "\n";
		results += "Known genotypes IDs: " + knownGenotypesString + "\n";
		results += "\n";
		
		results += "log10(POI LR): " + String.format("%.3g", poiLLR / FastMath.log(10)) + "\n";
		results += "p-value: " + String.format("%.3g", poiPValue) + "\n";
		results += "Number of samples with LR > 1: " + numLRGTOne + "\n";
		results += "Pr(LR > 1): " + String.format("%.3g", probLRGTOne) + "\n";

		results += "\n\n";
		
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
			csvOutputLine.add(Integer.toString(noc));
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
			csvOutputLine.add(Integer.toString(Settings.thetaNumLevelsCEESIt));
			csvOutputLine.add(Double.toString(Settings.binWidthFactor));
			csvOutputLine.add(Integer.toString(Settings.numBins));
			csvOutputLine.add(Integer.toString(Settings.numSamples1CEESIt));
			csvOutputLine.add(Double.toString(Settings.numSamplesIncCEESIt));
			csvOutputLine.add(Long.toString(Settings.numberPOISamples));
			csvOutputLine.add(Double.toString(Settings.genotypeTolerance));
			csvOutputLine.add(Double.toString(Settings.popSubstructureAdj));
			
			csvOutputLine.add(poiGenotype.getGenotypeID());
			csvOutputLine.add(knownGenotypesString);
			
			csvOutputLine.add(String.format("%.3g", poiLLR / FastMath.log(10)));
			csvOutputLine.add(String.format("%.3g", poiPValue));
			
			int tx = llrHist.keySet().size();
			int[] xCounter = new int[tx];
			double[] binStart = new double[tx];
			double[] binEnd = new double[tx];

			int i = 0;
			for (double key : llrHist.keySet()) {
				if (Double.isFinite(key)) {
					binStart[i] = key / FastMath.log(10);
					binEnd[i] = (key + llrHistBinWidth) / FastMath.log(10);

					if (i < maxNumLlrHistBins) {
						csvOutputLine.add(String.format("%.2g", binStart[i]) + " to " + String.format("%.2g", binEnd[i]));
						xCounter[i] = llrHist.get(key).intValue();
						csvOutputLine.add(Integer.toString(xCounter[i]));
					} else {
						csvOutputLine.add("");
						csvOutputLine.add("");
					}

					i++;
				}
			}
			if (Settings.numBins > llrHist.keySet().size()) {
				int diff = Settings.numBins - llrHist.keySet().size();
				for (int j = 0; j < diff; j++) {
					csvOutputLine.add("");
					csvOutputLine.add("");
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

		for (int n = 0; n < csvOutputLines.size(); n++) {
			csvOutputLines.get(n).add(Constants.ceesItCSVOutputFileList.indexOf("Number of Samples with LR > 1"), 
					Long.toString(numLRGTOne));
			csvOutputLines.get(n).add(Constants.ceesItCSVOutputFileList.indexOf("Pr(LR > 1)"), 
					String.format("%.3g", probLRGTOne));
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
	public BarChart<String, Number> graphBarChart(String title) {
		ObservableList<String> xLabels = FXCollections.observableArrayList();
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);

		bc.setLegendVisible(true);
		bc.setLegendSide(Side.BOTTOM);
		bc.setAnimated(false);
		bc.setBarGap(0);
		bc.setCategoryGap(20);
		bc.setVerticalGridLinesVisible(false);

		bc.setTitle(title);
		Tooltip t = new Tooltip(title);
		Tooltip.install(bc, t);
		xAxis.setLabel("log\u2081\u2080(LR)");
		yAxis.setLabel("Frequency");

		int tx = llrHist.keySet().size();
		int[] xCounter = new int[tx];
		double[] binStart = new double[tx];
		double[] binEnd = new double[tx];

		int i = 0;
		for (double key : llrHist.keySet()) {
			if (Double.isFinite(key)) {
				binStart[i] = key / FastMath.log(10);
				binEnd[i] = (key + llrHistBinWidth) / FastMath.log(10);

				xLabels.add(String.format("%.2g", binStart[i]) + " - " + String.format("%.2g", binEnd[i]));
				xCounter[i] = llrHist.get(key).intValue();

				i++;
			}
		}

		xAxis.setTickLabelRotation(90);
		xAxis.setCategories(xLabels);
		
		XYChart.Series<String, Number> series1 = new XYChart.Series<>();
		for (i = 0; i < xLabels.size(); i++)
			series1.getData().add(new XYChart.Data<>(xLabels.get(i), xCounter[i]));

		bc.getData().add(series1);
		// bc.setPrefSize(700, 400);

		for (i = 0; i < series1.getData().size(); i++) {
			XYChart.Data<String, Number> data = series1.getData().get(i);

			if (binEnd[i] < poiLLR / FastMath.log(10))
				data.getNode().setStyle("-fx-bar-fill: green;");
			else if (binStart[i] > poiLLR / FastMath.log(10))
				data.getNode().setStyle("-fx-bar-fill: orange;");
			else
				data.getNode().setStyle("-fx-bar-fill: grey;");
		}

		Legend legend = (Legend) bc.lookup(".chart-legend");
		Legend.LegendItem li1 = new Legend.LegendItem("Less than log\u2081\u2080(POI LR)",
				new Rectangle(10, 8, Color.GREEN));
		Legend.LegendItem li2 = new Legend.LegendItem("Greater than log\u2081\u2080(POI LR)",
				new Rectangle(10, 8, Color.ORANGE));
		legend.getItems().setAll(li1, li2);

		double maxBarWidth = 10;
		double minCategoryGap = 5;
		double barWidth = 0;
		do {
			double catSpace = xAxis.getCategorySpacing();
			double avilableBarSpace = catSpace - (bc.getCategoryGap() + bc.getBarGap());
			barWidth = (avilableBarSpace / bc.getData().size()) - bc.getBarGap();
			if (barWidth > maxBarWidth) {
				avilableBarSpace = (maxBarWidth + bc.getBarGap()) * bc.getData().size();
				bc.setCategoryGap(catSpace - avilableBarSpace - bc.getBarGap());
			}
		} while (barWidth > maxBarWidth);

		do {
			double catSpace = xAxis.getCategorySpacing();
			double avilableBarSpace = catSpace - (minCategoryGap + bc.getBarGap());
			barWidth = FastMath.min(maxBarWidth, (avilableBarSpace / bc.getData().size()) - bc.getBarGap());
			avilableBarSpace = (barWidth + bc.getBarGap()) * bc.getData().size();
			bc.setCategoryGap(catSpace - avilableBarSpace - bc.getBarGap());
		} while (barWidth < maxBarWidth && bc.getCategoryGap() > minCategoryGap);

		return bc;
	}

	/**
	 * Calculates the p-value based on sampling of random POIs.
	 *
	 * @return the double[]
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws ExecutionException
	 *             the execution exception
	 */

	private double[] sampleRandomPOIs() throws InterruptedException, ExecutionException {
		double pValue = 0.0;
		double llSum = Double.NEGATIVE_INFINITY;
		long numIterations = 0;

		ExecutorService executor = Executors.newFixedThreadPool(numProcessors);
		Set<RandomPOICallable> callables = new HashSet<>();
		for (int j = 1; j <= numProcessors; j++)
			callables.add(new RandomPOICallable(numPoiSamples / numProcessors));

		List<Future<double[]>> futures = executor.invokeAll(callables);
		for (Future<double[]> future : futures) {
			double[] futureValues = future.get();
			pValue += futureValues[0];
			llSum = UtilityMethods.logSum(llSum, futureValues[1]);
			numIterations += futureValues[2];
		}

		executor.shutdown();

		pValue /= numIterations;

		return new double[] { pValue, llSum };
	}

	/**
	 * Calculates the likelihood ratio and p-value for a given person of
	 * interest (POI) as well as the likelihood ratio distribution for randomly
	 * generated POIs.
	 *
	 * @param sampleID
	 *            the sample ID
	 * @param sample
	 *            the sample
	 * @param noc
	 *            the number of contributors
	 * @param poiGenotype
	 *            the POI genotype
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
	public void runCEESIt(String sampleID, Sample sample, int noc, Genotype poiGenotype, List<Genotype> knownGenotypes,
			FreqTable freqTable, HashMap<Locus, Integer> analyticalThresholds, String outputFilePath, BackendController backendController)
			throws InterruptedException, ExecutionException, IOException {
		this.noc = noc; // Number of contributors
		this.poiGenotype = poiGenotype;
		this.knownGenotypes = knownGenotypes;
		
		if (knownGenotypes != null && noc <= knownGenotypes.size()) {
			System.out.println("Number of contributors must be greater than the number of known contributors.");
			return;
		}

		thetas = new ArrayList<>();
		if (noc == 1)
			thetas.add(new double[] { 1.0 });
		else {
			for (int i = 1; i < thetaNumLevels - (noc - 2); i++) {
				Iterator<int[]> iter = CombinatoricsUtils.combinationsIterator(thetaNumLevels - i - 1, noc - 2);
				while (iter.hasNext()) {
					int[] comb = iter.next();

					double[] theta = new double[noc];
					if (comb.length > 0) {
						int[] intTheta = new int[noc];

						intTheta[0] = i;
						intTheta[1] = 1 + comb[0];
						for (int i1 = 1; i1 < comb.length; i1++) {
							intTheta[i1 + 1] = comb[i1] - comb[i1 - 1];
						}
						intTheta[noc - 1] = thetaNumLevels - i - 1 - comb[noc - 3];

						boolean isSorted = true;
						for (int i1 = 2; i1 < intTheta.length; i1++) {
							if (intTheta[i1] > intTheta[i1 - 1]) {
								isSorted = false;
								break;
							}
						}

						if (!isSorted) {
							continue;
						}

						for (int i1 = 0; i1 < theta.length; i1++)
							theta[i1] = ((double) intTheta[i1]) / thetaNumLevels;
					} else {
						theta[0] = ((double) i) / thetaNumLevels;
						theta[1] = 1.0 - theta[0];
					}

					thetas.add(theta);
				}
			}
		}

		numProcessors = Settings.numProcessors;
		freqTable.calcProbDists(calibration.getKit(), sample);

		this.sampleID = sampleID;
		this.outputFilePath = outputFilePath;
		
		sample.calcQuantParams(calibration.getKit());
		sample.calcHeightDist(freqTable);
		sampleFiltered = sample.isFiltered();
		sampleFileName = sample.getSampleFileName();		

		populationName = freqTable.getName();
		caseNumber = sample.populationCaseNumberMap.get(populationName);
		comments = sample.populationCommentsMap.get(populationName);
		populationFilePath = freqTable.getFilePath();
		numPeople = freqTable.getNumPeople();

		sampleFileLoci = sample.getLoci();
		quantParams = sample.getQuantParams();

		for (Locus locus : quantParams.keySet()) {
			if (poiGenotype.containsLocus(locus)) {
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
			} else
				lociNotInPoiGenotype.add(locus);
		}

		// Genotypes to be used for p-value sampling
		HashMap<Locus, Set<List<Allele>>> workGenotypes = new HashMap<>();

		genoProbs = new HashMap<>();
		for (Locus locus : workingLoci) {
			genoProbs.put(locus, new HashMap<>());
			workGenotypes.put(locus, new HashSet<>());
		}

		probabilityModel = new ProbabilityModel(sample, freqTable, calibration, analyticalThresholds, popSubstructureAdj);

		double initialTime = System.currentTimeMillis(); // Starting time

		System.out.println("Pre-calculation of locus probabilities begins.");

		for (int k = 0; k < thetas.size(); k++) { // Each mixture ratio
			double[] mixRatio = thetas.get(k); // Mixture ratio
			System.out.println(Arrays.toString(mixRatio));

			for (Locus locus : workingLoci) { // Each locus;				
				double[][] quantParams = new double[2][noc];
				for (int i = 0; i < noc; i++) {
					quantParams[0][i] = this.quantParams.get(locus).get(0) * mixRatio[i];
					quantParams[1][i] = this.quantParams.get(locus).get(1);
				}
				
				ExecutorService executor = Executors.newFixedThreadPool(numProcessors);
				Set<CalcLogProbCallable> callables = new HashSet<>();

				if (!locus.isAMEL()) {
					System.out.println(locus + " " + freqTable.getProbDists().get(locus).keySet().size());
					
					for (STRAllele allele1 : freqTable.getProbDists().get(locus).keySet()) {
						for (STRAllele allele2 : freqTable.getProbDists().get(locus).keySet()) {
							if (allele1.compareTo(allele2) <= 0) {
								List<Allele> alleles = new ArrayList<>();
								alleles.add(allele1);
								alleles.add(allele2);
								
								if (knownGenotypes != null)
									for (Genotype knownGenotype : knownGenotypes)
										alleles.addAll(Arrays.asList(knownGenotype.getAlleles(locus)));
								
								callables.add(new CalcLogProbCallable(locus, alleles, quantParams));
							}
						}
					}
				} else {
					for (AMELAllele[] sexGenotype : ProbabilityModel.SEX_GENOTYPES.values()) {
						List<Allele> alleles = new ArrayList<>();
						alleles.add(sexGenotype[0]);
						alleles.add(sexGenotype[1]);
						
						if (knownGenotypes != null)
							for (Genotype knownGenotype : knownGenotypes)
								alleles.addAll(Arrays.asList(knownGenotype.getAlleles(locus)));
						
						callables.add(new CalcLogProbCallable(locus, alleles, quantParams));
					}
				}
				
				List<Future<AllelesLogProb>> futures = executor.invokeAll(callables);
				for (Future<AllelesLogProb> future : futures) {
					double logProb = future.get().getLogProb();										
					List<Allele> genotype = future.get().getAlleles();
					
					if (logProb != Double.NEGATIVE_INFINITY)
						workGenotypes.get(locus).add(genotype);

					if (!genoProbs.get(locus).containsKey(genotype))
						genoProbs.get(locus).put(genotype, new ArrayList<>());					
					genoProbs.get(locus).get(genotype).add(logProb);
				}				
				
				executor.shutdown();
			}
		}

		double treeTime = System.currentTimeMillis(); // Tree creation time
		System.out.println("Pre-calculation of locus probabilities over. Time taken: "
				+ (treeTime - initialTime) / 60000.00 + " minutes.");

		if (backendController != null)
			backendController.updateCEESItProgress(0.5);
		
		double poiGenoProb = 1.0;
		for (Locus locus : workingLoci) {
			if (!locus.isAMEL()) {
				if (!poiGenotype.getAlleles(locus)[0].equals(poiGenotype.getAlleles(locus)[1])) {
					poiGenoProb *= 2 * probabilityModel.getAlleleProbByFreq(locus, poiGenotype.getAlleles(locus)[0])
							* probabilityModel.getAlleleProbByFreq(locus, poiGenotype.getAlleles(locus)[1]) * (1 - popSubstructureAdj);
				}
				else {
					double p = probabilityModel.getAlleleProbByFreq(locus, poiGenotype.getAlleles(locus)[0]);
					poiGenoProb *= p * p + p * (1 - p) * popSubstructureAdj;					
				}				
			}
			else
				poiGenoProb *= 0.5;
		}

		double probWorkGenotypes = 1.0;
		boolean poiInWorkGenotypes = true;
		for (Locus locus : workingLoci) {
			double probLocus = 0.0;
			if (locus.isAMEL()) {
				probLocus = ((double) workGenotypes.get(locus).size()) / ProbabilityModel.SEX_GENOTYPES.size();
			} else {
				for (List<Allele> geno : workGenotypes.get(locus)) {
					Allele allele1 = geno.get(0);
					Allele allele2 = geno.get(1);
					double freq;
					if (freqTable.getProbDists().get(locus).containsKey(allele1)
							&& freqTable.getProbDists().get(locus).containsKey(allele2))
						freq = freqTable.getProbDists().get(locus).get(allele1)
								* freqTable.getProbDists().get(locus).get(allele2); 
					else
						freq = 0.0;
					
					if (!allele1.equals(allele2))
						freq *= 2;

					probLocus += freq;
				}
			}

			System.out.println(locus + " " + probLocus);
			probWorkGenotypes *= probLocus;
			
			poiInWorkGenotypes &= workGenotypes.get(locus).contains(Arrays.asList(poiGenotype.getAlleles(locus)));
		}
			
		if (poiInWorkGenotypes)
			probWorkGenotypes -= poiGenoProb;
		
		// Calculation of true POI probability
		double[] mixRatioProbs1 = new double[thetas.size()];
		double max1 = Double.NEGATIVE_INFINITY;
		for (int k = 0; k < thetas.size(); k++) {
			double mixRatioProb = 0;
			for (Locus locus : workingLoci) {
				List<Allele> poiGeno = Arrays.asList(poiGenotype.getAlleles(locus));
				double logLocusProb = genoProbs.get(locus).get(poiGeno).get(k);
				mixRatioProb += logLocusProb;
			}
			mixRatioProbs1[k] = mixRatioProb;

			if (mixRatioProb > max1)
				max1 = mixRatioProb;
		}

		double sum1 = 0.0;
		for (int i = 0; i < mixRatioProbs1.length; i++) {
			sum1 += FastMath.exp(mixRatioProbs1[i] - max1);
		}
		poiLogProb = max1 + FastMath.log(sum1); // Mix Ratio probs
		poiLogProb -= FastMath.log(thetas.size()); // Averaged	

		double randomPathTime1 = System.currentTimeMillis();
		System.out.println("Random path traversal begins.");
		double[] values = sampleRandomPOIs(); // Try random path method
		double randomPathTime2 = System.currentTimeMillis();
		System.out.println("Random path traversal ends. Time taken: " + (randomPathTime2 - randomPathTime1) / 60000.00
				+ " minutes.");

		poiPValue = values[0] * probWorkGenotypes + poiGenoProb;
		double lrDen = UtilityMethods.logSum(FastMath.log(probWorkGenotypes) + values[1] - FastMath.log(numPoiSamples),
				poiLogProb + FastMath.log(poiGenoProb));
		poiLLR = poiLogProb - lrDen;
		
		if (llHist.size() > 0) {
			if (llHist.containsKey(Double.NEGATIVE_INFINITY)) {
				llrHist.put(Double.NEGATIVE_INFINITY, llHist.get(Double.NEGATIVE_INFINITY));
				llHist.remove(Double.NEGATIVE_INFINITY);
			}

			int maxBin = Collections.max(llHist.keySet()).intValue();
			int minBin = Collections.min(llHist.keySet()).intValue();
			int numBins = maxBin - minBin + 1;
			int scaleFactor = (int) Math.ceil((double) numBins / maxNumLlrHistBins);

			for (int i = minBin; i <= maxBin; i += scaleFactor)
				llrHist.put(i * minBinWidth - lrDen, new LongAdder());

			llrHistBinWidth = scaleFactor * minBinWidth;
			for (double llHistKey : llHist.keySet()) {
				double llrHistKey = (Math.floor((llHistKey - minBin) / scaleFactor) * scaleFactor + minBin) * minBinWidth - lrDen;				
				llrHist.get(llrHistKey).add(llHist.get(llHistKey).intValue());
				
				if (llHistKey * minBinWidth - lrDen > 0)
					numLRGTOne += llHist.get(llHistKey).intValue();
			}
			llHist.clear();
		}
		
		if (poiLLR > 0)
			probLRGTOne = poiGenoProb;
		else
			probLRGTOne = 0.0;
		
		probLRGTOne += (probWorkGenotypes * numLRGTOne) / numPoiSamples;

		if (backendController != null)
			backendController.updateCEESItProgress(1.0);

		double finalTime = System.currentTimeMillis(); // Ending time
		//double time = (finalTime - initialTime) / 60000;
		//timeTaken = FastMath.round(time * 100.0) / 100.0;
		
		// report time in seconds to match NOCIt report
		timeTaken = (finalTime - initialTime) / 1000;
	}
}
