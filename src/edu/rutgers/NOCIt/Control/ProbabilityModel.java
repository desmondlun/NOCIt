package edu.rutgers.NOCIt.Control;

import static org.apache.commons.math3.special.Erf.erf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.util.FastMath;

import edu.rutgers.NOCIt.Data.AMELAllele;
import edu.rutgers.NOCIt.Data.Allele;
import edu.rutgers.NOCIt.Data.AlleleDoubleArray;
import edu.rutgers.NOCIt.Data.Constants;
import edu.rutgers.NOCIt.Data.FreqTable;
import edu.rutgers.NOCIt.Data.Kit;
import edu.rutgers.NOCIt.Data.Locus;
import edu.rutgers.NOCIt.Data.LocusData;
import edu.rutgers.NOCIt.Data.Peak;
import edu.rutgers.NOCIt.Data.STRAllele;
import edu.rutgers.NOCIt.Data.Sample;

/**
 * This class implements the probability model used to assess the probability of
 * observing a certain set of peak heights at a locus.
 * 
 * @author Abhishek Garg
 * @author Desmond Lun
 * @author Harish Swaminathan
 */
public class ProbabilityModel {

	/**
	 * The length of the arrays used to sample alleles according to observed
	 * peak heights and population frequency.
	 */
	/** The Constant SEXES. */
	private final static String[] SEXES = new String[] { Constants.Terms.MALE, Constants.Terms.FEMALE };

	/** The Constant SEX_GENOTYPES. */
	public final static HashMap<String, AMELAllele[]> SEX_GENOTYPES;
	static {
		SEX_GENOTYPES = new HashMap<String, AMELAllele[]>();
		AMELAllele xAllele = new AMELAllele("X");
		AMELAllele yAllele = new AMELAllele("Y");
		SEX_GENOTYPES.put(Constants.Terms.MALE, new AMELAllele[] { xAllele, yAllele });
		SEX_GENOTYPES.put(Constants.Terms.FEMALE, new AMELAllele[] { xAllele, xAllele });
	}

	/** The analytical thresholds. */
	private HashMap<Locus, Integer> analyticalThresholds;

	/** The loci data. */
	private HashMap<Locus, LocusData> lociData = null;

	/**
	 * The probability distributions for alleles based on observed peak heights.
	 */
	private HashMap<Locus, HashMap<STRAllele, Double>> heightDists = null;

	/** The height sampling arrays. */
	private HashMap<Locus, TreeMap<Double, STRAllele>> heightCumDists;
	
	/**
	 * The probability distributions for alleles based on population frequency.
	 */
	private HashMap<Locus, HashMap<STRAllele, Double>> freqDists = null;

	/** The frequency sampling arrays. */
	private HashMap<Locus, TreeMap<Double, STRAllele[]>> freqPairCumDists;

	/** All possible true and stutter alleles at each locus. */
	private HashMap<Locus, HashSet<STRAllele>> possibleAlleles;

	/**
	 * The number of times CalcSTRProb() and CalcAMELProb() have been called.
	 */
	private long calcProbCount = 0;

	/** The calibration. */
	private Calibration calibration;

	/** The maximum hash code for the STR allele arrays. */
	private int strAlleleMaxHashCode;
	
	private double popSubstructureAdj;

	/**
	 * Instantiates a new probability model.
	 *
	 * @param sample
	 *            the sample
	 * @param freqTable
	 *            the frequency table
	 * @param calibration
	 *            the calibration
	 * @param analyticalThresholds
	 *            the analytical thresholds
	 * @param popSubstructureAdj
	 * 			  the population substructure adjustment parameter
	 */
	public ProbabilityModel(Sample sample, FreqTable freqTable, Calibration calibration,
			HashMap<Locus, Integer> analyticalThresholds, double popSubstructureAdj) {
		this.analyticalThresholds = analyticalThresholds;
		this.calibration = calibration;
		this.popSubstructureAdj = popSubstructureAdj;
		
		strAlleleMaxHashCode = freqTable.getMaxHashCode();

		if (sample != null) {
			this.lociData = sample.getLociData();
			
			// Heights intervals map is updated
			heightDists = sample.getHeightDists();
			this.heightCumDists = new HashMap<>();		
			for (Locus locus : heightDists.keySet()) {
				heightCumDists.put(locus, new TreeMap<>());

				double cumProb = 0.0;
				for (STRAllele allele : heightDists.get(locus).keySet()) {
					heightCumDists.get(locus).put(cumProb, allele);
					cumProb += heightDists.get(locus).get(allele);
				}
			}
		}
		
		// Freq intervals map is updated
		freqDists = freqTable.getProbDists();	
		this.freqPairCumDists = new HashMap<>();
		for (Locus locus : freqDists.keySet()) {
			freqPairCumDists.put(locus, new TreeMap<>());

			double cumProb = 0.0;
			for (STRAllele allele1 : freqDists.get(locus).keySet()) {
				for (STRAllele allele2 : freqDists.get(locus).keySet()) {
					freqPairCumDists.get(locus).put(cumProb, new STRAllele[]{allele1, allele2});
					
					if (allele1.compareTo(allele2) <= 0) {
						if (!allele1.equals(allele2)) 
							cumProb += 2 * freqDists.get(locus).get(allele1) * freqDists.get(locus).get(allele2) * (1 - popSubstructureAdj);
						else {
							double p = freqDists.get(locus).get(allele1);
							cumProb += p * p + p * (1 - p) * popSubstructureAdj;
						}
					}
				}
			}
		}

		possibleAlleles = new HashMap<>();
		for (Locus locus : freqDists.keySet()) {
			possibleAlleles.put(locus, new HashSet<>()); 

			for (STRAllele allele : freqDists.get(locus).keySet()) {
				possibleAlleles.get(locus).add(allele);
				possibleAlleles.get(locus).add(allele.rStutterAllele());
				possibleAlleles.get(locus).add(allele.fStutterAllele());
			}
		}
	}

	/**
	 * Calculate probability of peak heights at AMEL locus.
	 *
	 * @param locus
	 *            the locus
	 * @param alleles
	 *            the alleles
	 * @param quantParams
	 *            the quantification parameters
	 * @param trueDO
	 *            boolean array indicating whether each allele has dropped out
	 * @return a double array of length 2 (where the first double is the
	 *         mantissa and the second is the exponent with base e)
	 */
	private double[] calcAMELProb(Locus locus, Allele[] alleles, double[][] quantParams, boolean[] trueDO) {
		Kit kit = calibration.getKit();
		int threshold = analyticalThresholds.get(locus);

		// Variables
		AlleleDoubleArray decayedMasses = new AlleleDoubleArray(AMELAllele.MAX_HASH_CODE);

		HashSet<AMELAllele> presentTrueAlleles = new HashSet<>();
		HashSet<Allele> trueAlleles = new HashSet<>(Arrays.asList(alleles));
		Map<Allele, Peak> allObsPeaks = lociData.get(locus).getPeaks();

		calcProbCount++;

		for (int index = 0; index < alleles.length; index++) {
			if (quantParams[0][index / 2] > 0.0) {
				AMELAllele allele = (AMELAllele) alleles[index];
				if (!trueDO[index]) { // Allele is present
					int contrib = index / 2;
					double decayedMass = quantParams[0][contrib]
							* FastMath.exp(quantParams[1][contrib] * kit.calcFragmentSize(locus, allele));

					presentTrueAlleles.add(allele);
					decayedMasses.inc(allele, decayedMass);
				}
			}
		}

		// Dropped out alleles cannot be observed
		for (Allele allele : trueAlleles)
			if (!presentTrueAlleles.contains(allele) && allObsPeaks.containsKey(allele))
				return new double[] { 0.0, Double.NEGATIVE_INFINITY };

		double multTerm = 1.0;
		double expTerm = 0.0;
		for (AMELAllele allele : SEX_GENOTYPES.get(Constants.Terms.MALE)) {
			double[] weights;
			double[] means;
			double[] vars;
			if (presentTrueAlleles.contains(allele)) {
				// True peak only
				double decayedMass = decayedMasses.get(allele);
				double[] values = calibration.calcTrue(locus, decayedMass);

				weights = new double[] { 1.0 };
				means = new double[] { values[0] };
				vars = new double[] { values[1] * values[1] };
			} else {
				// Noise
				double probDO = calibration.calcNoiseDO(locus);

				double decayedMass = 0.0;
				for (int i = 0; i < quantParams[0].length; i++)
					decayedMass += quantParams[0][i]
							* FastMath.exp(quantParams[1][i] * kit.calcFragmentSize(locus, allele));
				double[] values = calibration.calcNoise(locus, decayedMass);

				weights = new double[] { probDO, 1 - probDO };
				means = new double[] { 0.0, values[0] };
				vars = new double[] { 0.0, values[1] * values[1] };
			}

			int peakHeight = allObsPeaks.containsKey(allele) ? allObsPeaks.get(allele).getHeight() : 0;

			double[] gmmValues = calcGMMProb(peakHeight, weights, means, vars, threshold);
			multTerm *= gmmValues[0];
			expTerm += gmmValues[1];
		}

		return new double[] { multTerm, expTerm };
	}

	/**
	 * Calculate probability of observing a peak of a particular height under a
	 * Gaussian mixture model.
	 *
	 * @param height
	 *            the height
	 * @param weights
	 *            the weights
	 * @param means
	 *            the means
	 * @param vars
	 *            the variances
	 * @param threshold
	 *            the analytical threshold
	 * @return a double array of length 2 (where the first double is the
	 *         mantissa and the second is the exponent with base e)
	 */
	private double[] calcGMMProb(int height, double[] weights, double[] means, double[] vars, int threshold) {
		double multTerm = 0.0;
		double expTerm;
		if (height < threshold) {
			for (int i = 0; i < weights.length; i++) {
				if (vars[i] == 0.0)
					multTerm += weights[i];
				else
					multTerm += calcNormCDF(threshold - 0.5, means[i], vars[i]);
			}

			expTerm = 0.0;
		} else {
			double[] expTerms = new double[weights.length];
			expTerm = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < weights.length; i++) {
				if (vars[i] > 0.0) {
					expTerms[i] = -0.5 * (height - means[i]) * (height - means[i]) / vars[i];

					if (expTerms[i] > expTerm)
						expTerm = expTerms[i];
				}
			}

			for (int i = 0; i < weights.length; i++) {
				if (vars[i] > 0.0)
					multTerm += weights[i] / FastMath.sqrt(Constants.TWO_PI * vars[i])
							* FastMath.exp(expTerms[i] - expTerm);
			}
		}

		return new double[] { multTerm, expTerm };
	}

	/**
	 * Calculates natural log of the probability of observing the evidence at an
	 * STR locus, given the alleles and mixture ratio.
	 *
	 * @param locus
	 *            the locus
	 * @param alleles
	 *            the alleles
	 * @param quantParams
	 *            the quantification parameters
	 * @return natural log of the probability
	 */
	public double calcLogProbBernoulli(Locus locus, Allele[] alleles, double[][] quantParams) {
		boolean[] trueDO = new boolean[alleles.length];
		sampleDO(locus, alleles, quantParams, trueDO);

		double[] prob;
		if (locus.isAMEL())
			prob = calcAMELProb(locus, alleles, quantParams, trueDO);
		else
			prob = calcSTRProb(locus, alleles, quantParams, trueDO);

		return FastMath.log(prob[0]) + prob[1];
	}

	/**
	 * Calculates CDF of normal distribution, given mean, variance, and height.
	 *
	 * @param height
	 *            the height
	 * @param mean
	 *            the mean
	 * @param var
	 *            the variance
	 * @return the double
	 */
	private double calcNormCDF(double height, double mean, double var) {
		return 0.5 * (1.0 + erf((height - mean) / (FastMath.sqrt(2 * var))));
	}

	/**
	 * Calculates the probability of observing the evidence at a locus, given
	 * the alleles and mixture ratio. This function is determinstic because
	 * dropout is integrated, not sampled.
	 *
	 * @param locus
	 *            the locus
	 * @param alleles
	 *            the alleles
	 * @param quantParams
	 *            the quantification parameters
	 * @return a double array of length 2 (where the first double is the
	 *         mantissa and the second is the exponent with base e)
	 */
	public double[] calcProbIntegrate(Locus locus, Allele[] alleles, double[][] quantParams) {
		Kit kit = calibration.getKit();

		int numAlleles = alleles.length;
		int numCombos = 1 << numAlleles;
		boolean[][] doCombos = new boolean[numCombos][numAlleles]; 
		
		for (int i = 0; i < numCombos; i++)
			for (int j = 0; j < numAlleles; j++)
				doCombos[i][j] = ((i >> j) & 1) != 0;

		double[] multTerms = new double[numCombos];
		double[] expTerms = new double[numCombos];
		for (int i = 0; i < numCombos; i++) { // For each DO combo
			boolean[] trueDO = doCombos[i];

			double comboProb = 1.0;
			for (int index = 0; index < alleles.length; index++) {
				Allele allele = alleles[index];
				double mass = quantParams[0][index / 2];
				double decayedMass = mass
						* FastMath.exp(quantParams[1][index / 2] * kit.calcFragmentSize(locus, allele));

				double probDO = calibration.calcTrueDO(locus, decayedMass);
				if (trueDO[index])
					comboProb *= probDO;
				else
					comboProb *= 1 - probDO;
			}

			double[] probValues;
			if (locus.isAMEL())
				probValues = calcAMELProb(locus, alleles, quantParams, trueDO);
			else
				probValues = calcSTRProb(locus, alleles, quantParams, trueDO);

			multTerms[i] = comboProb * probValues[0];
			expTerms[i] = probValues[1];
		}

		double expTerm = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < numCombos; i++)
			if (expTerms[i] > expTerm && multTerms[i] > 0.0)
				expTerm = expTerms[i];

		double multTerm = 0.0;
		for (int i = 0; i < numCombos; i++)
			if (multTerms[i] > 0.0)
				multTerm += multTerms[i] * FastMath.exp(expTerms[i] - expTerm);

		return new double[] { multTerm, expTerm };
	}

	/**
	 * Calculate probability of peak heights at an STR locus.
	 *
	 * @param locus
	 *            the locus
	 * @param alleles
	 *            the alleles
	 * @param quantParams
	 *            the quantification parameters
	 * @param trueDO
	 *            the true DO
	 * @return a double array of length 2 (where the first double is the
	 *         mantissa and the second is the exponent with base e)
	 */
	private double[] calcSTRProb(Locus locus, Allele[] alleles, double[][] quantParams, boolean[] trueDO) {
		Kit kit = calibration.getKit();
		int threshold = analyticalThresholds.get(locus);

		// Variables
		AlleleDoubleArray decayedMasses = new AlleleDoubleArray(strAlleleMaxHashCode);

		HashSet<STRAllele> presentTrueAlleles = new HashSet<>(); 
		HashSet<Allele> trueAlleles = new HashSet<>(Arrays.asList(alleles));
		Map<Allele, Peak> allObsPeaks = lociData.get(locus).getPeaks(); 

		calcProbCount++;

		for (int index = 0; index < alleles.length; index++) {
			if (quantParams[0][index / 2] > 0.0) {
				STRAllele allele = (STRAllele) alleles[index];
				if (!trueDO[index]) { // Allele is present
					int contrib = index / 2;
					double decayedMass = quantParams[0][contrib]
							* FastMath.exp(quantParams[1][contrib] * kit.calcFragmentSize(locus, allele));

					presentTrueAlleles.add(allele);
					decayedMasses.inc(allele, decayedMass);
				}
			}
		}

		// Dropped out alleles cannot be observed
		for (Allele allele : trueAlleles)
			if (!presentTrueAlleles.contains(allele) && allObsPeaks.containsKey(allele) && allObsPeaks.get(allele).getHeight() > 0)
				return new double[] { 0.0, Double.NEGATIVE_INFINITY };

		HashSet<STRAllele> rStutterAlleles = new HashSet<>();
		HashSet<STRAllele> fStutterAlleles = new HashSet<>();
		for (STRAllele allele : presentTrueAlleles) {
			rStutterAlleles.add(allele.rStutterAllele());
			fStutterAlleles.add(allele.fStutterAllele());
		}

		double multTerm = 1.0;
		double expTerm = 0.0;
		for (Allele allele : possibleAlleles.get(locus)) {
			double[] weights;
			double[] means;
			double[] vars;
			if (presentTrueAlleles.contains(allele)) {
				if (rStutterAlleles.contains(allele)) {
					// True peak and possible reverse stutter
					double[] trueValues = calibration.calcTrue(locus, decayedMasses.get(allele));

					STRAllele parentAllele = ((STRAllele) allele).rParentAllele();
					int parentHeight = allObsPeaks.containsKey(parentAllele) ? allObsPeaks.get(parentAllele).getHeight() : 0;
					double probDO = calibration.calcRStutterDO(locus, parentHeight);
					double[] stutterValues = calibration.calcRStutter(locus, parentHeight);

					weights = new double[] { probDO, 1 - probDO };
					means = new double[] { trueValues[0], trueValues[0] + stutterValues[0] };
					vars = new double[] { trueValues[1] * trueValues[1],
							trueValues[1] * trueValues[1] + stutterValues[1] * stutterValues[1] };

				} else {
					// True peak only
					double decayedMass = decayedMasses.get(allele);
					double[] values = calibration.calcTrue(locus, decayedMass);

					weights = new double[] { 1.0 };
					means = new double[] { values[0] };
					vars = new double[] { values[1] * values[1] };
				}
			} else {
				if (rStutterAlleles.contains(allele)) {
					if (fStutterAlleles.contains(allele)) {
						// Possible forward and reverse stutter

						STRAllele rParentAllele = ((STRAllele) allele).rParentAllele();
						STRAllele fParentAllele = ((STRAllele) allele).fParentAllele();
						int rParentHeight = allObsPeaks.containsKey(rParentAllele) ? allObsPeaks.get(rParentAllele).getHeight() : 0;
						int fParentHeight = allObsPeaks.containsKey(fParentAllele) ? allObsPeaks.get(fParentAllele).getHeight() : 0;
						double probRStutterDO = calibration.calcRStutterDO(locus, rParentHeight);
						double probFStutterDO = calibration.calcFStutterDO(locus, fParentHeight);
						double[] rStutterValues = calibration.calcRStutter(locus, rParentHeight);
						double[] fStutterValues = calibration.calcFStutter(locus, fParentHeight);

						weights = new double[] { probFStutterDO * probRStutterDO, probFStutterDO * (1 - probRStutterDO),
								(1 - probFStutterDO) * probRStutterDO, (1 - probFStutterDO) * (1 - probRStutterDO) };
						means = new double[] { 0.0, rStutterValues[0], fStutterValues[0],
								rStutterValues[0] + fStutterValues[0] };
						vars = new double[] { 0.0, rStutterValues[1] * rStutterValues[1],
								fStutterValues[1] * fStutterValues[1],
								rStutterValues[1] * rStutterValues[1] + fStutterValues[1] * fStutterValues[1] };
					} else {
						// Possible reverse stutter only
						STRAllele parentAllele = ((STRAllele) allele).rParentAllele();
						int parentHeight = allObsPeaks.containsKey(parentAllele) ? allObsPeaks.get(parentAllele).getHeight() : 0;
						double probDO = calibration.calcRStutterDO(locus, parentHeight);
						double[] values = calibration.calcRStutter(locus, parentHeight);

						weights = new double[] { probDO, 1 - probDO };
						means = new double[] { 0.0, values[0] };
						vars = new double[] { 0.0, values[1] * values[1] };
					}
				} else {
					if (fStutterAlleles.contains(allele)) {
						// Possible forward stutter only
						STRAllele parentAllele = ((STRAllele) allele).fParentAllele();
						int parentHeight = allObsPeaks.containsKey(parentAllele) ? allObsPeaks.get(parentAllele).getHeight() : 0;
						double probDO = calibration.calcFStutterDO(locus, parentHeight);
						double[] values = calibration.calcFStutter(locus, parentHeight);

						weights = new double[] { probDO, 1 - probDO };
						means = new double[] { 0.0, values[0] };
						vars = new double[] { 0.0, values[1] * values[1] };
					} else {
						// Noise
						double probDO = calibration.calcNoiseDO(locus);

						double decayedMass = 0.0;
						for (int i = 0; i < quantParams[0].length; i++)
							decayedMass += quantParams[0][i]
									* FastMath.exp(quantParams[1][i] * kit.calcFragmentSize(locus, allele));
						double[] values = calibration.calcNoise(locus, decayedMass);

						weights = new double[] { probDO, 1 - probDO };
						means = new double[] { 0.0, values[0] };
						vars = new double[] { 0.0, values[1] * values[1] };
					}
				}
			}

			int peakHeight = allObsPeaks.containsKey(allele) ? allObsPeaks.get(allele).getHeight() : 0;

			double[] gmmValues = calcGMMProb(peakHeight, weights, means, vars, threshold);
			multTerm *= gmmValues[0];
			expTerm += gmmValues[1];
		}

		return new double[] { multTerm, expTerm };
	}
	
	public double getAlleleProbByFreq(Locus locus, Allele allele, Allele[] sampledAlleles, int numSampled) {
		if (popSubstructureAdj == 0.0 || numSampled == 0)
			return freqDists.get(locus).get(allele);
		
		int numOfAllele = 0;
		for (int i = 0; i < numSampled; i++)
			if (sampledAlleles[i].equals(allele))
				numOfAllele++;
		
		return (numOfAllele * popSubstructureAdj + (1 - popSubstructureAdj) * freqDists.get(locus).get(allele)) 
				/ (1 + (numSampled - 1) * popSubstructureAdj);
	}
	
	/**
	 * Gets the probability of a pair of alleles according to the population frequency
	 * distribution.
	 *
	 * @param locus
	 *            the locus
	 * @param allele1
	 *            the first allele
	 * @param allele2
	 *            the second allele
	 * @return the probability of the allele
	 */
	public double getAllelePairProbByFreq(Locus locus, Allele allele1, Allele allele2) {		
		if (!allele1.equals(allele2)) {		
			if (freqDists.get(locus).containsKey(allele1) && freqDists.get(locus).containsKey(allele2))
				return 2 * freqDists.get(locus).get(allele1) * freqDists.get(locus).get(allele2) * (1 - popSubstructureAdj);
			else
				return 0.0;
		}
		else {
			if (freqDists.get(locus).containsKey(allele1)) {
				double p = freqDists.get(locus).get(allele1);
				return p * p + p * (1 - p) * popSubstructureAdj;
			}
			else
				return 0.0;
		}
	}

	/**
	 * Gets the probability of an allele according to the observed peak heights
	 * distribution.
	 *
	 * @param locus
	 *            the locus
	 * @param allele
	 *            the allele
	 * @return the probability of the allele
	 */
	public double getAlleleProbByHeight(Locus locus, Allele allele) {
		if (heightDists.get(locus).containsKey(allele))
			return heightDists.get(locus).get(allele);
		else
			return 0.0;
	}

	/**
	 * Gets the number of times CalcSTRProb() and CalcAMELProb() have been
	 * called.
	 *
	 * @return the number of times CalcSTRProb() and CalcAMELProb() have been
	 *         called
	 */
	public long getCalcProbCount() {
		return calcProbCount;
	}

	/**
	 * Gets the calibration.
	 *
	 * @return the calibration
	 */
	public Calibration getCalibration() {
		return calibration;
	}

	/**
	 * Sample pair of alleles at an STR locus according to the population frequency distribution.
	 *
	 * @param locus
	 *            the locus
	 * @return the sampled allele
	 */
	public STRAllele[] sampleAllelePairByFreq(Locus locus) {
		double r = ThreadLocalRandom.current().nextDouble();
		return freqPairCumDists.get(locus).floorEntry(r).getValue();
	}

	/**
	 * Sample allele at an STR locus according to the observed peak heights distribution.
	 *
	 * @param locus
	 *            the locus
	 * @return the sampled allele
	 */
	public STRAllele sampleAlleleByHeight(Locus locus) {
		double r = ThreadLocalRandom.current().nextDouble();
		return heightCumDists.get(locus).floorEntry(r).getValue();
	}

	/**
	 * Sample AMEL alleles according to an equal probability for each sex.
	 *
	 * @return an array of length 2 of sampled AMEL alleles
	 */
	public AMELAllele[] sampleAMELAllelePair() {
		String sex = SEXES[ThreadLocalRandom.current()
				.nextInt(SEXES.length)]; /* Pick sex at random */
		return SEX_GENOTYPES.get(sex);
	}

	/**
	 * Sample dropout at a locus by Bernoulli trial.
	 *
	 * @param locus
	 *            the locus
	 * @param alleles
	 *            the alleles
	 * @param quantParams
	 *            the quantification parameters
	 * @param trueDO
	 *            the true DO
	 */
	private void sampleDO(Locus locus, Allele[] alleles, double[][] quantParams, boolean[] trueDO) {
		Kit kit = calibration.getKit();

		for (int index = 0; index < alleles.length; index++) {
			Allele allele = alleles[index];
			double mass = quantParams[0][index / 2];
			double decayedMass = mass * FastMath.exp(quantParams[1][index / 2] * kit.calcFragmentSize(locus, allele));

			double probDO = calibration.calcTrueDO(locus, decayedMass);
			if (ThreadLocalRandom.current().nextDouble(1) > probDO) {
				trueDO[index] = false;
			} else
				trueDO[index] = true;
		}

		return;
	}
}
