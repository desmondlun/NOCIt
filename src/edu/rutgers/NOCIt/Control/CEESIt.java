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
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.DoubleAdder;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import com.opencsv.CSVReader;
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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

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
							
							alleleProb *= probabilityModel.getAllelePairProbByFreq(locus, allele1, allele2);
										
							heightProb *= probabilityModel.getAlleleProbByHeight(locus, allele1)
									* probabilityModel.getAlleleProbByHeight(locus, allele2);
							if (!allele1.equals(allele2))
								heightProb *= 2;
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
			double numAtLeast = 0.0;
			double llSum = Double.NEGATIVE_INFINITY;
			long numIter = 0;

			HashMap<Locus, Allele[]> sampGeno = new HashMap<>();							

			for (long i = 0; i < numIterations; i++) {
				if (ThreadLocalRandom.current().nextDouble() > POP_FREQ_SAMPLE_PROB)
					for (Locus locus : workingLoci) {
						if (locus.isAMEL()) 
							sampGeno.put(locus, probabilityModel.sampleAMELAllelePair());
						else
						{
							STRAllele[] sample;	

							STRAllele allele1 = probabilityModel.sampleAlleleByHeight(locus);
							STRAllele allele2 = probabilityModel.sampleAlleleByHeight(locus);
							//Organize Pair with smaller one in front
							if (allele1.compareTo(allele2) <= 0) 
								sample = new STRAllele[] {allele1, allele2};						
							else						
								sample = new STRAllele[] {allele2, allele1};

							sampGeno.put(locus, sample);

						}

					}
				else 
					for (Locus locus : workingLoci) {
						if (locus.isAMEL()) 
							sampGeno.put(locus, probabilityModel.sampleAMELAllelePair());
						else
							sampGeno.put(locus, probabilityModel.sampleAllelePairByFreq(locus));						
					}

				double freqProb = 1.0;
				double heightProb = 1.0;
				for (Locus locus : workingLoci) 
					if (!locus.isAMEL()) {
						//importance sampling weight calculation 		
						STRAllele allele1 = (STRAllele) sampGeno.get(locus)[0];
						STRAllele allele2 = (STRAllele) sampGeno.get(locus)[1];

						freqProb *= probabilityModel.getAllelePairProbByFreq(locus, allele1, allele2);
						heightProb *= probabilityModel.getAlleleProbByHeight(locus, allele1)
								* probabilityModel.getAlleleProbByHeight(locus, allele2);
						if (!allele1.equals(allele2))
							heightProb *=2;	
					}

				boolean equalsPoiGeno = true;
				for (Locus locus : workingLoci) 
					equalsPoiGeno &= Arrays.equals(sampGeno.get(locus), poiGenotype.getAlleles(locus));							

				if (equalsPoiGeno)
					continue;

				freqProb /= 1.0 - poiGenoProbByFreq;
				heightProb /= 1.0 - poiGenoProbByHeight;
				double samplingProb = (1.0 - POP_FREQ_SAMPLE_PROB) * heightProb + POP_FREQ_SAMPLE_PROB * freqProb;

				double[] mixRatioProbs = new double[thetas.size()];

				double max = Double.NEGATIVE_INFINITY;
				for (int k = 0; k < thetas.size(); k++) { 
					double mixRatioProb = 0.0;
					for (Locus locus : workingLoci) { // Each locus

						List<Allele> geno = Arrays.asList(sampGeno.get(locus));
						if (locus.isAMEL())
						{
							mixRatioProb += genoProbs.get(locus).get(geno).get(k);
						}
						else
						{	
							mixRatioProb += genoProbs.get(locus).get(geno).get(k);
						}
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

				llSum = UtilityMethods.logSum(llSum, logLikelihood + FastMath.log(freqProb / samplingProb));

				double llHistKey = FastMath.floor(logLikelihood / minBinWidth);
				if (!llHist.containsKey(llHistKey))
					llHist.put(llHistKey, new DoubleAdder());
				llHist.get(llHistKey).add(freqProb / samplingProb);			

				if (logLikelihood >= poiLogProb)
					numAtLeast += freqProb / samplingProb;

				numIter++;
			}
			
			System.out.println("\t\nNumber At least: " + numAtLeast + "\nllSum: " + llSum + "\nNumber of Iteration: " + numIter);
			return new double[] { numAtLeast, llSum, numIter };
		}
	}
	
	private static final double POP_FREQ_SAMPLE_PROB = 0.1;

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String calibrationFilePath = "calib_idp_10s.zip";
		String freqTableFilePath = "etc/ABI IP CAUC freq_COUNTS n=349.csv";
		File knownGenotypesFile = new File("etc/Known Genotypes.csv");
		double genotypeTolerance = Settings.genotypeTolerance;
		long numPoiSamples = Settings.numPoiSamples;

		if(args.length == 0)
		{
			System.out.println("Missing Arguments: -f [Test File Name]");
			System.exit(0);
		}
		
		int noc = 1;
		int sampleIDknowngeno = 1; 
		
		//Added Command Line Arguments
		//Example: -f etc/IP/testing_samples/1p_10s/1.csv -k 4 -n 1 -t 0.5 -nop 1000000000 
		Options options = new Options();
		Option sampleFileInput = new Option("f", "file", true, "Sample File");
		sampleFileInput.setArgName("FILE PATH");
		options.addOption(sampleFileInput);
		
		Option nocValue = new Option("n", "noc", true, "Number of Contributers");
		nocValue.setArgName("INTEGER");
		options.addOption(nocValue);
		
		Option knowngenotypeIDValue = new Option("k", "knownid", true, "SampleID from Known Genotype.csv");
		knowngenotypeIDValue.setArgName("INTEGER");
		options.addOption(knowngenotypeIDValue);
		
		Option genotypeToleranceValue = new Option("t", "tol", true, "Genotype Tolerance");
		genotypeToleranceValue.setArgName("DOUBLE");
		options.addOption(genotypeToleranceValue);
		
		Option nopValue = new Option("p", "nop",true, "Number of POI Samples");
		nopValue.setArgName("LONG");
		options.addOption(nopValue);

		CommandLineParser clp = new DefaultParser();
		HelpFormatter hf = new HelpFormatter();
		CommandLine commandline = null;
		
		try {
			commandline = clp.parse(options, args);
					
		}catch(ParseException e) {
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
			hf.printHelp("CEESIt", options);
			System.exit(1);
		}
		if (commandline.hasOption('n')) {
			noc = Integer.parseInt(commandline.getOptionValue("n"));				//default to 1 contributer
		}
		if (commandline.hasOption('k')) {
			sampleIDknowngeno = Integer.parseInt(commandline.getOptionValue('k'));		//default to first Sample Id in KnownGenotype.csv
		}
		if (commandline.hasOption('t')) {
			genotypeTolerance = Double.parseDouble(commandline.getOptionValue('t'));		//default value to 0.5 from Settings
		}
		if (commandline.hasOption('p')) {
			numPoiSamples = Long.parseLong(commandline.getOptionValue('p'));		//default value to 1000000000 from Settings
		}

		File sampleFile = new File(commandline.getOptionValue("file"));					//required file (ex. etc/IP/testing_samples/1p_10s/1.csv)
		
		CSVReader csvreader = new CSVReader( new FileReader(knownGenotypesFile) );
		List< String[] > allRows = csvreader.readAll();
		csvreader.close();
		
		String[] header = allRows.get(0);
	
		Genotype poiGenotype = new Genotype(Integer.toString(sampleIDknowngeno));
		
		for (int i = 1; i < allRows.size(); i++) {
			
			String[] row = allRows.get(i);
			if(Integer.parseInt(row[0]) == sampleIDknowngeno)
			{
				for(int j = 1; j < header.length;j++)
				{
					
					String[] allelePair = row[j].split(",");
					if ((allelePair[0].equals("X")) || (allelePair[0].equals("Y")))
						poiGenotype.putAlleles(new Locus("AMEL"),
								new AMELAllele[] { new AMELAllele(allelePair[0]), new AMELAllele(allelePair[1]) });
					else
						poiGenotype.putAlleles(new Locus(header[j]),
							new STRAllele[] { new STRAllele(allelePair[0]), new STRAllele(allelePair[1]) });
				}
				break;
			}
			
			else if((i == allRows.size()-1)&& (Integer.parseInt(row[0]) != sampleIDknowngeno))
				System.out.println("No Matching Sample ID");
		}
		
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
					ceesIt.setGenotypeTolerance(genotypeTolerance);
					ceesIt.setNumPoiSamples(numPoiSamples);
					ceesIt.runCEESIt(sampleID, sample, noc, poiGenotype, knownGenotypes, freqTable,
							analyticalThresholds, null, null);
					System.out.println(ceesIt.getResultsString(analyticalThresholds));	
				}
			} catch (InterruptedException | ExecutionException | IOException | NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
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
	private ConcurrentHashMap<Double, DoubleAdder> llHist = new ConcurrentHashMap<>();

	/** The log likelihood ratio histogram. */
	private NavigableMap<Double, DoubleAdder> llrHist = new TreeMap<>();

	/** The log likelihood ratio histogram bin width. */
	private double llrHistBinWidth;

	/** The probability model. */
	private ProbabilityModel probabilityModel;
	
	/** The lines in the CSV output. */
	private ArrayList<ArrayList<String>> csvOutputLines = new ArrayList<ArrayList<String>>();
	
	private double numLRGTOne = 0.0;
	private double probLRGTOne = 0.0;
	private double poiGenoProbByFreq = 1.0;
	private double poiGenoProbByHeight = 1.0;
	

	/**
	 * The standard error tolerance for the probability of evidence given a
	 * locus, set of quantification parameters, and number of contributors.
	 */
	private double genotypeTolerance;
	
	private double minBinWidth;
	private int maxNumLlrHistBins;
	private int numSamples1;
	private double numSamplesInc;
	private int thetaNumLevels;
	private long numPoiSamples;	

	/**
	 * Instantiates a new CEESIt.
	 *
	 * @param calibration
	 *            the calibration
	 */
	public CEESIt(Calibration calibration) {
		this.calibration = calibration;
		
		genotypeTolerance = Settings.genotypeTolerance;		
		minBinWidth = Settings.binWidthFactor / 10 * FastMath.log(10);
		maxNumLlrHistBins = Settings.numBins;
		numSamples1 = Settings.numSamples1CEESIt;
		numSamplesInc = Settings.numSamplesIncCEESIt;
		thetaNumLevels = Settings.thetaNumLevelsCEESIt;
		numPoiSamples = Settings.numPoiSamples;	
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
		results += "Genotype Tolerance: " + genotypeTolerance +"\n";
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
			csvOutputLine.add(Integer.toString(thetaNumLevels));
			csvOutputLine.add(Double.toString(Settings.binWidthFactor));
			csvOutputLine.add(Integer.toString(maxNumLlrHistBins));
			csvOutputLine.add(Integer.toString(numSamples1));
			csvOutputLine.add(Double.toString(numSamplesInc));
			csvOutputLine.add(Long.toString(numPoiSamples));
			csvOutputLine.add(Double.toString(genotypeTolerance));
			csvOutputLine.add(Double.toString(Settings.popSubstructureAdj));
			
			csvOutputLine.add(poiGenotype.getGenotypeID());
			csvOutputLine.add(knownGenotypesString);
			
			csvOutputLine.add(String.format("%.3g", poiLLR / FastMath.log(10)));
			csvOutputLine.add(String.format("%.3g", poiPValue));
			
			int tx = llrHist.keySet().size();
			double[] binStart = new double[tx];
			double[] binEnd = new double[tx];

			int i = 0;
			for (double key : llrHist.keySet()) {
				if (Double.isFinite(key)) {
					binStart[i] = key / FastMath.log(10);
					binEnd[i] = (key + llrHistBinWidth) / FastMath.log(10);

					if (i < maxNumLlrHistBins) {
						csvOutputLine.add(String.format("%.2g", binStart[i]) + " to " + String.format("%.2g", binEnd[i]));			
						csvOutputLine.add(Double.toString(llrHist.get(key).doubleValue()));
					} else {
						csvOutputLine.add("");
						csvOutputLine.add("");
					}

					i++;
				}
			}
			if (maxNumLlrHistBins > llrHist.keySet().size()) {
				int diff = maxNumLlrHistBins - llrHist.keySet().size();
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
					Double.toString(numLRGTOne));
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
		double[] xCounter = new double[tx];
		double[] binStart = new double[tx];
		double[] binEnd = new double[tx];

		int i = 0;
		for (double key : llrHist.keySet()) {
			if (Double.isFinite(key)) {
				binStart[i] = key / FastMath.log(10);
				binEnd[i] = (key + llrHistBinWidth) / FastMath.log(10);

				xLabels.add(String.format("%.2g", binStart[i]) + " - " + String.format("%.2g", binEnd[i]));
				xCounter[i] = llrHist.get(key).doubleValue();

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
	
		probabilityModel = new ProbabilityModel(sample, freqTable, calibration, analyticalThresholds, Settings.popSubstructureAdj);
	
		double initialTime = System.currentTimeMillis(); // Starting time
	
		System.out.println("Pre-calculation of locus probabilities begins.");
		PrintWriter writer = new PrintWriter("locusProbabilities.txt", "UTF-8");
	
	
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
		writer.close();
		double treeTime = System.currentTimeMillis(); // Tree creation time
		System.out.println("Pre-calculation of locus probabilities over. Time taken: "
				+ (treeTime - initialTime) / 60000.00 + " minutes.");
	
		if (backendController != null)
			backendController.updateCEESItProgress(0.5);
		
		for (Locus locus : workingLoci) {
			if (!locus.isAMEL()) {
				poiGenoProbByFreq *= probabilityModel.getAllelePairProbByFreq(locus, poiGenotype.getAlleles(locus)[0], poiGenotype.getAlleles(locus)[1]);		
				poiGenoProbByHeight *= probabilityModel.getAlleleProbByHeight(locus, poiGenotype.getAlleles(locus)[0]);
				poiGenoProbByHeight *= probabilityModel.getAlleleProbByHeight(locus, poiGenotype.getAlleles(locus)[1]);
				if (poiGenotype.getAlleles(locus)[0] != poiGenotype.getAlleles(locus)[1])
					poiGenoProbByHeight *= 2;
			}
			else {
				poiGenoProbByFreq *= 0.5;
				poiGenoProbByHeight *= 0.5;
			}
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
			probWorkGenotypes -= poiGenoProbByFreq;
		
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
	
		poiPValue = values[0] * probWorkGenotypes + poiGenoProbByFreq;
		double lrDen = UtilityMethods.logSum(FastMath.log(probWorkGenotypes) + values[1] - FastMath.log(numPoiSamples),
				poiLogProb + FastMath.log(poiGenoProbByFreq));
		poiLLR = poiLogProb - lrDen;
		System.out.println("poiLogProb: " + poiLogProb + "\nlrDen: " + lrDen);
		System.out.println("values[1]: " + values[1]);
		System.out.println("FastMath.log(probWorkGenotypes): " + FastMath.log(probWorkGenotypes));
		
		double llHistTotal = 0.0;
		if (llHist.size() > 0) {
			if (llHist.containsKey(Double.NEGATIVE_INFINITY)) {
				llrHist.put(Double.NEGATIVE_INFINITY, llHist.get(Double.NEGATIVE_INFINITY));
				llHist.remove(Double.NEGATIVE_INFINITY);
				
				llHistTotal += llHist.get(Double.NEGATIVE_INFINITY).doubleValue();
			}
	
			int maxBin = Collections.max(llHist.keySet()).intValue();
			int minBin = Collections.min(llHist.keySet()).intValue();
			int numBins = maxBin - minBin + 1;
			int scaleFactor = (int) Math.ceil((double) numBins / maxNumLlrHistBins);
	
			for (int i = minBin; i <= maxBin; i += scaleFactor)
				llrHist.put(i * minBinWidth - lrDen, new DoubleAdder());
	
			llrHistBinWidth = scaleFactor * minBinWidth;
			for (double llHistKey : llHist.keySet()) {
				double llrHistKey = (Math.floor((llHistKey - minBin) / scaleFactor) * scaleFactor + minBin) * minBinWidth - lrDen;
				if (llrHist.get(llrHistKey) != null)
					llrHist.get(llrHistKey).add(llHist.get(llHistKey).doubleValue());				
				if (llHistKey * minBinWidth - lrDen > 0)
					numLRGTOne += llHist.get(llHistKey).doubleValue();
				
				llHistTotal += llHist.get(llHistKey).doubleValue();
			}
			llHist.clear();
		}
		
		System.out.println("llHistTotal: " + llHistTotal);
		
		if (poiLLR > 0)
			probLRGTOne = poiGenoProbByFreq;
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

		System.out.println("p-value from random POI sampling = " + pValue +  "\nllSum = " + llSum);
		return new double[] { pValue, llSum };
	}

	public void setGenotypeTolerance(double genotypeTolerance) {
		this.genotypeTolerance = genotypeTolerance;
	}

	public void setNumPoiSamples(long numPoiSamples) {
		this.numPoiSamples = numPoiSamples;
	}
}
