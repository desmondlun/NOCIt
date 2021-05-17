package edu.rutgers.NOCIt.Control;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import edu.rutgers.NOCIt.UIMain;
import edu.rutgers.NOCIt.Data.AMELAllele;
import edu.rutgers.NOCIt.Data.Allele;
import edu.rutgers.NOCIt.Data.CSVModule;
import edu.rutgers.NOCIt.Data.DegradedCalibrationPeak;
import edu.rutgers.NOCIt.Data.FileReaders;
import edu.rutgers.NOCIt.Data.Genotype;
import edu.rutgers.NOCIt.Data.Kit;
import edu.rutgers.NOCIt.Data.Locus;
import edu.rutgers.NOCIt.Data.LocusData;
import edu.rutgers.NOCIt.Data.Peak;
import edu.rutgers.NOCIt.Data.STRAllele;
import edu.rutgers.NOCIt.Data.Sample;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ProgressBar;

/**
 * This class implements the calibration.
 *
 * @author Abhishek Garg
 * @author Desmond Lun
 * @author Harish Swaminathan
 * @author James Kelley 
 */
public class Calibration implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The number of points used for the plot curve. */
	private static final int POINTS_FOR_CURVE = 100;
	
	private static final double DEFAULT_PLOT_MAX_X = 100;

	/** The loci with enough data points to fit reverse stutter model. */
	private HashSet<Locus> lociRStutter = new HashSet<>();

	/** The loci with enough data points to fit forward stutter model. */
	private HashSet<Locus> lociFStutter = new HashSet<>();

	/** The loci without enough data points to fit allele peak model. */
	private HashSet<Locus> lociNoTrue = new HashSet<>();

	/** The loci without enough data points to fit noise model. */
	private HashSet<Locus> lociNoNoise = new HashSet<>();

	/** The loci. */
	private List<Locus> loci = new ArrayList<Locus>();

	/** The model parameters for allele peaks. */
	private HashMap<Locus, double[]> trueParams = new HashMap<>();

	/** The model parameters for reverse stutter peaks. */
	private HashMap<Locus, double[]> rStutterParams = new HashMap<>();

	/** The model parameters for forward stutter peaks. */
	private HashMap<Locus, double[]> fStutterParams = new HashMap<>();

	/** The model parameters for noise peaks. */
	private HashMap<Locus, double[]> noiseParams = new HashMap<>();

	/** The model parameters for allele peak dropout. */
	private HashMap<Locus, double[]> trueDOParams = new HashMap<>();

	/** The model parameters for reverse stutter dropout. */
	private HashMap<Locus, double[]> rStutterDOParams = new HashMap<>();

	/** The model parameters for forward stutter dropout. */
	private HashMap<Locus, double[]> fStutterDOParams = new HashMap<>();

	/** The model parameters for noise dropout. */
	private HashMap<Locus, double[]> noiseDOParams = new HashMap<>();

	/** The x data for the allele peak model. */
	private HashMap<Locus, ArrayList<Double>> trueXData = new HashMap<Locus, ArrayList<Double>>();

	/** The x data for the reverse stutter model. */
	private HashMap<Locus, ArrayList<Double>> rStutterXData = new HashMap<Locus, ArrayList<Double>>();

	/** The x data for the forward stutter model. */
	private HashMap<Locus, ArrayList<Double>> fStutterXData = new HashMap<Locus, ArrayList<Double>>();

	/** The x data for the noise model. */
	private HashMap<Locus, ArrayList<Double>> noiseXData = new HashMap<Locus, ArrayList<Double>>();

	/** The x data for the allele peak dropout model. */
	private HashMap<Locus, ArrayList<Double>> trueDOXData = new HashMap<Locus, ArrayList<Double>>();

	/** The x data for the reverse stutter dropout model. */
	private HashMap<Locus, ArrayList<Double>> rStutterDOXData = new HashMap<Locus, ArrayList<Double>>();

	/** The x data for the forward stutter dropout model. */
	private HashMap<Locus, ArrayList<Double>> fStutterDOXData = new HashMap<Locus, ArrayList<Double>>();

	/** The x data for the noise dropout model. */
	private HashMap<Locus, ArrayList<Double>> noiseDOXData = new HashMap<Locus, ArrayList<Double>>();

	/** The y data for the allele peak model. */
	private HashMap<Locus, ArrayList<Double>> trueYData = new HashMap<Locus, ArrayList<Double>>();

	/** The y data for the reverse stutter model. */
	private HashMap<Locus, ArrayList<Double>> rStutterYData = new HashMap<Locus, ArrayList<Double>>();

	/** The y data for the forward stutter model. */
	private HashMap<Locus, ArrayList<Double>> fStutterYData = new HashMap<Locus, ArrayList<Double>>();

	/** The y data for the noise model. */
	private HashMap<Locus, ArrayList<Double>> noiseYData = new HashMap<Locus, ArrayList<Double>>();

	/** The y data for the allele peak dropout model. */
	private HashMap<Locus, ArrayList<Double>> trueDOYData = new HashMap<Locus, ArrayList<Double>>();

	/** The y data for the reverse stutter dropout model. */
	private HashMap<Locus, ArrayList<Double>> rStutterDOYData = new HashMap<Locus, ArrayList<Double>>();

	/** The y data for the forward stutter dropout model. */
	private HashMap<Locus, ArrayList<Double>> fStutterDOYData = new HashMap<Locus, ArrayList<Double>>();

	/** The y data for the noise dropout model. */
	private HashMap<Locus, ArrayList<Double>> noiseDOYData = new HashMap<Locus, ArrayList<Double>>();

	/** The R^2 values. */
	private HashMap<Feature, List<Double>> rSquaredValues = new HashMap<Feature, List<Double>>();

	/** The kit. */
	private Kit kit;

	/** The calibration name. */
	// Used for NOCIt report
	private String calibrationName;

	/** The calibration path. */
	private String calibrationPath;

	/** The plot all calibration points. */
	private boolean plotAllCalibrationPoints = Settings.defaultPlotAllCalibrationGraphPointsValue;

	/**
	 * The Enum Feature.
	 */
	public enum Feature {

		/** Allele peaks. */
		TRUE("Allele Peak", new TruePeakModel(), new double[] { 0.5, 0.0, 0.1, 0.0 }),

		/** Reverse stutter peaks. */
		RSTUTTER("Reverse Stutter Peak", new StutterModel(), new double[] { 0.5, 1.0, 0.5, 1.0 }),

		/** Forward stutter peaks. */
		FSTUTTER("Forward Stutter Peak", new StutterModel(), new double[] { 0.5, 1.0, 0.5, 1.0 }),

		/** Noise peaks. */
		NOISE("Noise Peak", new NoisePeakModel(), new double[] { 0.5, 0.0, 0.0, 1.0 }),

		/** Allele dropout. */
		TRUE_DO("Allele Dropout", new DropOutModel(), new double[] { 0.9, 0.0 }),

		/** Reverse stutter dropout. */
		RSTUTTER_DO("Reverse Stutter Dropout", new StutterDropOutModel(), new double[] { 0.9, 0.0 }),

		/** Forward stutter dropout. */
		FSTUTTER_DO("Forward Stutter Dropout", new StutterDropOutModel(), new double[] { 0.9, 0.0 }),

		/** Noise dropout. */
		NOISE_DO("Noise Dropout", new NoiseDropOutModel(), new double[] { 0.5 });

		/** The name. */
		private final String name;

		/** The model. */
		private final SolverModel model;

		/** The default initial parameters. */
		private final double[] defaultInitialParams;

		/**
		 * Instantiates a new feature.
		 *
		 * @param name
		 *            the name
		 * @param model
		 *            the model
		 * @param defaultInitialParams
		 *            the default initial parameters
		 */
		Feature(String name, SolverModel model, double[] defaultInitialParams) {
			this.name = name;
			this.model = model;
			this.defaultInitialParams = defaultInitialParams;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return name;
		}

		/**
		 * Gets the model.
		 *
		 * @return the model
		 */
		public SolverModel getModel() {
			return model;
		}

		/**
		 * Gets the default initial parameters.
		 *
		 * @return the default initial parameters
		 */
		public double[] getDefaultInitialParams() {
			return defaultInitialParams;
		}
	}

	/**
	 * Calculate parameters of calibration model for all features.
	 *
	 * @param csvModules
	 *            the CSV modules containing the calibration samples
	 * @param initialParams
	 *            the initial parameters
	 * @param progressBar
	 *            the progress bar
	 */
	public void calculateParameters(Collection<CSVModule> csvModules, HashMap<Feature, double[]> initialParams,
			ProgressBar progressBar) {
		HashMap<Locus, ArrayList<DegradedCalibrationPeak>> truePeaks = new HashMap<Locus, ArrayList<DegradedCalibrationPeak>>();
		HashMap<Locus, ArrayList<DegradedCalibrationPeak>> rStutterPeaks = new HashMap<Locus, ArrayList<DegradedCalibrationPeak>>();
		HashMap<Locus, ArrayList<DegradedCalibrationPeak>> fStutterPeaks = new HashMap<Locus, ArrayList<DegradedCalibrationPeak>>();
		HashMap<Locus, ArrayList<DegradedCalibrationPeak>> noisePeaks = new HashMap<Locus, ArrayList<DegradedCalibrationPeak>>();

		for (CSVModule csvModule : csvModules) {
			for (String sampleID : csvModule.getSamples().keySet()) {
				// System.out.println("sample read :"+sampleID);
				Sample sample = csvModule.getSamples().get(sampleID);

				if (sample.getQuantParams().isEmpty()) 
					sample.calcQuantParams(kit, false, false);
				
				Genotype genotype = sample.getGenotype();
				if (genotype == null) {
					continue;
				}

				for (Locus locus : sample.getLociData().keySet()) {
					if (!genotype.containsLocus(locus)) {
						System.out.println(locus.getName() + " not found in genotypes");
						continue;
					}

					if (!sample.getQuantParams().containsKey(locus))
						continue;

					if (!loci.contains(locus))
						continue;

					ArrayList<Double> quantParams = sample.getQuantParams().get(locus);
					LocusData locusData = sample.getLociData().get(locus);
					Map<Allele, Peak> allPeaks = locusData.getPeaks();

					Allele trueAllele1 = genotype.getAlleles(locus)[0];
					Allele trueAllele2 = genotype.getAlleles(locus)[1];
					if (!locus.isAMEL()) {
						if (!trueAllele1.equals(trueAllele2)) {
							ArrayList<STRAllele> possibleTrueAllele = new ArrayList<STRAllele>();
							ArrayList<STRAllele> possibleRevStrAllele = new ArrayList<STRAllele>();
							ArrayList<STRAllele> possibleFwdStrAllele = new ArrayList<STRAllele>();
							ArrayList<Allele> possibleNoiseAllele = new ArrayList<Allele>(kit.getAlleles(locus));

							possibleTrueAllele.add((STRAllele) trueAllele1);
							possibleTrueAllele.add((STRAllele) trueAllele2);

							for (STRAllele al : possibleTrueAllele) {
								possibleRevStrAllele.add(al.rStutterAllele());
								possibleFwdStrAllele.add(al.fStutterAllele());
							}

							possibleNoiseAllele.removeAll(possibleFwdStrAllele);
							possibleNoiseAllele.removeAll(possibleRevStrAllele);
							possibleNoiseAllele.removeAll(possibleTrueAllele);

							removeDuplicates(possibleTrueAllele, possibleRevStrAllele, possibleFwdStrAllele);

							for (STRAllele al : possibleTrueAllele) {
								int peakHeight;
								if (allPeaks.containsKey(al)) {
									peakHeight = allPeaks.get(al).getHeight();
								} else {
									// we have a drop out
									peakHeight = 0;
								}

								DegradedCalibrationPeak trueDCP = new DegradedCalibrationPeak(al,
										kit.calcFragmentSize(locus, al), peakHeight, quantParams.get(0),
										FastMath.exp(-quantParams.get(1)));
								if (!truePeaks.containsKey(locus))
									truePeaks.put(locus, new ArrayList<DegradedCalibrationPeak>());
								truePeaks.get(locus).add(trueDCP);
							}

							for (STRAllele al : possibleRevStrAllele) {
								STRAllele ppa = al.rParentAllele();
								if (possibleTrueAllele.contains(ppa)) {
									int peakHeight;
									if (allPeaks.containsKey(al)) {
										peakHeight = allPeaks.get(al).getHeight();
									} else
										// we have a drop out
										peakHeight = 0;

									DegradedCalibrationPeak revStrDCP = new DegradedCalibrationPeak(al,
											kit.calcFragmentSize(locus, al), peakHeight, quantParams.get(0),
											FastMath.exp(-quantParams.get(1)));
									if (allPeaks.containsKey(ppa))
										revStrDCP.setAssocHeight(allPeaks.get(ppa).getHeight());
									else
										revStrDCP.setAssocHeight(0);

									if (!rStutterPeaks.containsKey(locus))
										rStutterPeaks.put(locus, new ArrayList<DegradedCalibrationPeak>());
									rStutterPeaks.get(locus).add(revStrDCP);
								}
							}

							for (STRAllele al : possibleFwdStrAllele) {
								STRAllele ppa = al.fParentAllele();
								if (possibleTrueAllele.contains(ppa)) {
									int peakHeight;
									if (allPeaks.containsKey(al)) {
										peakHeight = allPeaks.get(al).getHeight();
									} else
										// we have a drop out
										peakHeight = 0;

									DegradedCalibrationPeak fwdStrDCP = new DegradedCalibrationPeak(al,
											kit.calcFragmentSize(locus, al), peakHeight, quantParams.get(0),
											FastMath.exp(-quantParams.get(1)));
									if (allPeaks.containsKey(ppa))
										fwdStrDCP.setAssocHeight(allPeaks.get(ppa).getHeight());
									else
										fwdStrDCP.setAssocHeight(0);

									if (!fStutterPeaks.containsKey(locus))
										fStutterPeaks.put(locus, new ArrayList<DegradedCalibrationPeak>());
									fStutterPeaks.get(locus).add(fwdStrDCP);
								}
							}

							for (Allele al : possibleNoiseAllele) {
								int peakHeight;
								if (allPeaks.containsKey(al)) {
									peakHeight = allPeaks.get(al).getHeight();
								} else
									// we have a drop out
									peakHeight = 0;

								DegradedCalibrationPeak noiseDCP = new DegradedCalibrationPeak(al,
										kit.calcFragmentSize(locus, al), peakHeight, quantParams.get(0),
										FastMath.exp(-quantParams.get(1)));
								if (!noisePeaks.containsKey(locus))
									noisePeaks.put(locus, new ArrayList<DegradedCalibrationPeak>());
								noisePeaks.get(locus).add(noiseDCP);
							}
						}
					} else {
						ArrayList<Allele> trueAlleles = new ArrayList<Allele>();
						trueAlleles.add(trueAllele1);
						trueAlleles.add(trueAllele2);

						if (!trueAllele1.equals(trueAllele2)) { // Hetero
							for (Allele al : trueAlleles) {
								int peakHeight;
								if (allPeaks.containsKey(al)) { // Allele is
																// present
									peakHeight = allPeaks.get(al).getHeight();
								} else { // Allele not present - DO
									peakHeight = 0;
								}

								DegradedCalibrationPeak trueDCP = new DegradedCalibrationPeak(al,
										kit.calcFragmentSize(locus, al), peakHeight, quantParams.get(0),
										FastMath.exp(-quantParams.get(1)));
								if (!truePeaks.containsKey(locus))
									truePeaks.put(locus, new ArrayList<DegradedCalibrationPeak>());
								truePeaks.get(locus).add(trueDCP);
							}
						} else { // Homozygous locus is used for Noise for AMEL
									// alone; assume must be X, X, so Y is noise
							Allele al = new AMELAllele("Y");
							int peakHeight;
							if (allPeaks.containsKey(al)) { // Allele is present
								peakHeight = allPeaks.get(al).getHeight();
							} else { // Allele not present - DO
								peakHeight = 0;
							}

							DegradedCalibrationPeak noiseDCP = new DegradedCalibrationPeak(al,
									kit.calcFragmentSize(locus, al), peakHeight, quantParams.get(0),
									FastMath.exp(-quantParams.get(1)));
							if (!noisePeaks.containsKey(locus))
								noisePeaks.put(locus, new ArrayList<DegradedCalibrationPeak>());
							noisePeaks.get(locus).add(noiseDCP);
						}
					}
				}
				// System.out.println("sample "+sampleID+" extracted ; now
				// "+noisePeaks.size()+"loci in noise");
			}

		}

		int iFeature = 0;
		for (Feature feature : Feature.values()) {
			HashMap<Locus, ArrayList<DegradedCalibrationPeak>> peaks;
			HashMap<Locus, ArrayList<Double>> xData;
			HashMap<Locus, ArrayList<Double>> yData;
			ModelSolverInterface solver;

			rSquaredValues.put(feature, new ArrayList<Double>(loci.size() + 1));
			for (int i = 0; i < loci.size() + 1; i++)
				rSquaredValues.get(feature).add(0.0);

			switch (feature) {
			case TRUE:
				peaks = truePeaks;
				solver = new NelderMeadModelSolver();
				xData = trueXData;
				yData = trueYData;
				break;
			case RSTUTTER:
				peaks = rStutterPeaks;
				solver = new NelderMeadModelSolver();// CMAESModelSolver(new
														// double[]{0.1, 0.1,
														// 0.1, 0.1, 0.1}, 5);
				xData = rStutterXData;
				yData = rStutterYData;
				break;
			case FSTUTTER:
				peaks = fStutterPeaks;
				solver = new NelderMeadModelSolver();
				xData = fStutterXData;
				yData = fStutterYData;
				break;
			case NOISE:
				peaks = noisePeaks;
				solver = new NelderMeadModelSolver();
				xData = noiseXData;
				yData = noiseYData;
				break;
			case TRUE_DO:
				peaks = truePeaks;
				solver = new CMAESModelSolver(new double[] { 0.001, 0.1 }, 5);// BOBYQAModelSolver();//
																				// IpoptModelSolver();
				xData = trueDOXData;
				yData = trueDOYData;
				break;
			case RSTUTTER_DO:
				peaks = rStutterPeaks;
				solver = new CMAESModelSolver(new double[] { 0.001, 0.1 }, 5);// CMAESModelSolver(new
																				// double[]{0.01,
																				// 0.01},
																				// 10);//IpoptModelSolver();
				xData = rStutterDOXData;
				yData = rStutterDOYData;
				break;
			case FSTUTTER_DO:
				peaks = fStutterPeaks;
				solver = new CMAESModelSolver(new double[] { 0.001, 0.1 }, 5);// CMAESModelSolver(new
																				// double[]{0.01,
																				// 0.01},
																				// 10);//BOBYQAModelSolver();//new
																				// IpoptModelSolver();
				xData = fStutterDOXData;
				yData = fStutterDOYData;
				break;
			case NOISE_DO:
				peaks = noisePeaks;
				solver = new NelderMeadModelSolver();// IpoptModelSolver();
				xData = noiseDOXData;
				yData = noiseDOYData;
				break;
			default:
				peaks = new HashMap<Locus, ArrayList<DegradedCalibrationPeak>>();
				solver = null;
				xData = null;
				yData = null;
				break;
			}

			SolverModel solverModel = feature.getModel();
			solverModel.setStartingPoint(initialParams.get(feature));

			double sumLL = 0.0;
			double sumLL0 = 0.0;
			int sumN = 0;
			int iLocus = 0;
			for (Locus locus : peaks.keySet()) {
				solverModel.setData(peaks.get(locus));
				int N = solverModel.getDataSize();

				double[] result = solver.solveModel(solverModel);
				double LL = -solverModel.getObjective(result);

				double[] result0 = solverModel.getNullSolution();
				double LL0 = -solverModel.getObjective(result0);
				if (LL < LL0) {
					result = result0;
					LL = LL0;
				}

				getParams(feature).put(locus, result);

				// Calculate McFadden's pseudo-R^2
				// double rSquared = 1 - LL / LL0;
				// Calculate Nagelkerke pseudo-R^2
				double rSquared = (1 - FastMath.exp(2.0 / N * (LL0 - LL))) / (1 - FastMath.exp(2.0 / N * LL0));
				rSquaredValues.get(feature).set(loci.indexOf(locus) + 1, rSquared);

				System.out.println(feature.name + " " + locus + ": " + N + " Peaks ; " + Arrays.toString(result)
						+ " ; -LL/N=" + (-LL) / N + " ; R^2=" + rSquared);

				sumLL += LL;
				sumLL0 += LL0;
				sumN += N;

				xData.put(locus, new ArrayList<Double>(solverModel.getX()));
				yData.put(locus, new ArrayList<Double>(solverModel.getY()));

				iLocus++;

				if (progressBar != null) {
					progressBar.setProgress(
							(iFeature + ((double) iLocus) / peaks.keySet().size()) / Feature.values().length);
				}
			}
			// double rSquared = 1 - sumLL / sumLL0;
			double rSquared = (1 - FastMath.exp(2.0 / sumN * (sumLL0 - sumLL)))
					/ (1 - FastMath.exp(2.0 / sumN * sumLL0));
			rSquaredValues.get(feature).set(0, rSquared);

			iFeature++;
		}

		lociNoTrue = new HashSet<Locus>(loci);
		for (Locus locus : truePeaks.keySet())
			lociNoTrue.remove(locus);

		for (Locus locus : rStutterPeaks.keySet())
			lociRStutter.add(locus);

		for (Locus locus : fStutterPeaks.keySet())
			lociFStutter.add(locus);

		lociNoNoise = new HashSet<Locus>(loci);
		for (Locus locus : noisePeaks.keySet())
			lociNoNoise.remove(locus);

		System.out.println(rSquaredValues);
	}

	/**
	 * Removes the duplicates.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @param third
	 *            the third
	 */
	private void removeDuplicates(ArrayList<STRAllele> first, ArrayList<STRAllele> second, ArrayList<STRAllele> third) {
		ArrayList<STRAllele> temp = new ArrayList<STRAllele>(first);
		ArrayList<STRAllele> temp2 = new ArrayList<STRAllele>(second);
		ArrayList<STRAllele> temp3 = new ArrayList<STRAllele>(third);

		first.removeAll(second);
		first.removeAll(third);

		second.removeAll(temp);
		second.removeAll(temp3);

		third.removeAll(temp);
		third.removeAll(temp2);
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		String sampleFileFolderPath = "etc/IP/calibration samples - 10s/";
		String genotypesFilePath = "etc/Known Genotypes.csv";
		String binsFilePath = "etc/Identifiler_Plus_Bins_v1X - ColorDesignation.txt";

		String outputFilePath = "test_calib.zip";

		Settings.load();

		Kit kit = null;
		try {
			kit = new Kit(binsFilePath);
			Calibration calibration = new Calibration(kit);
			Map<String, Genotype> genotypes = FileReaders.createGenotypesMap(genotypesFilePath);
			ArrayList<CSVModule> csvModuleList = FileReaders
					.createCSVModuleList(Arrays.asList((new File(sampleFileFolderPath)).listFiles()), kit, genotypes);

			HashMap<Feature, double[]> initialParams = new HashMap<Feature, double[]>();
			for (Feature feature : Feature.values()) {
				initialParams.put(feature, feature.getDefaultInitialParams());
			}
			calibration.calculateParameters(csvModuleList, initialParams, null);

			CalibrationProjectHandler.saveCalibration(new File(outputFilePath), calibration);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Instantiates a new calibration.
	 *
	 * @param kit
	 *            the kit
	 */
	public Calibration(Kit kit) {
		this.kit = kit;
		loci = new ArrayList<Locus>(kit.getLoci());
	}

	/**
	 * Plot graph.
	 *
	 * @param label
	 *            the label
	 * @param locus
	 *            the locus
	 * @return the line chart
	 */
	public LineChart<Number, Number> plotGraph(String label, Locus locus) {
		List<Double> xData;
		List<Double> yData;
		XYChart.Series<Number, Number> curveSeries = new XYChart.Series<>();
		XYChart.Series<Number, Number> curveSeries1 = new XYChart.Series<>();
		XYChart.Series<Number, Number> curveSeries2 = new XYChart.Series<>();
		String xLabel;
		String yLabel;

		if (Settings.plotAllCalibrationGraphPoints.equals("true")) {
			plotAllCalibrationPoints = true;
		} else if (Settings.plotAllCalibrationGraphPoints.equals("false")) {
			plotAllCalibrationPoints = false;
		}

		// add points to series
		if (label.equals(Feature.TRUE.toString())) {
			xData = trueXData.get(locus);
			yData = trueYData.get(locus);
			double maxX = xData.size() > 0 ? Collections.max(xData) : DEFAULT_PLOT_MAX_X;
			for (int i = 0; i < POINTS_FOR_CURVE; i++) {
				double x = maxX * i / (POINTS_FOR_CURVE - 1);
				double[] y = calcTrue(locus, x);
				curveSeries.getData().add(new XYChart.Data<Number, Number>(x, y[0]));
				curveSeries1.getData().add(new XYChart.Data<Number, Number>(x, y[0] + y[1]));
				curveSeries2.getData().add(new XYChart.Data<Number, Number>(x, y[0] - y[1]));
			}
			xLabel = "Decayed Amplitude (RFU)" + numOfPointsPlottedString(xData.size(), plotAllCalibrationPoints);
			yLabel = "Peak Height (RFU)";
		} else if (label.equals(Feature.RSTUTTER.toString())) {
			xData = rStutterXData.get(locus);
			yData = rStutterYData.get(locus);
			double maxX = xData.size() > 0 ? Collections.max(xData) : DEFAULT_PLOT_MAX_X;
			for (int i = 0; i < POINTS_FOR_CURVE; i++) {
				double x = maxX * i / (POINTS_FOR_CURVE - 1);
				double[] y = calcRStutter(locus, x);
				curveSeries.getData().add(new XYChart.Data<Number, Number>(x, y[0]));
				curveSeries1.getData().add(new XYChart.Data<Number, Number>(x, y[0] + y[1]));
				curveSeries2.getData().add(new XYChart.Data<Number, Number>(x, y[0] - y[1]));
			}
			xLabel = "Parent Peak Height (RFU)" + numOfPointsPlottedString(xData.size(), plotAllCalibrationPoints);
			yLabel = "Peak Height (RFU)";
		} else if (label.equals(Feature.FSTUTTER.toString())) {
			xData = fStutterXData.get(locus);
			yData = fStutterYData.get(locus);
			double maxX = xData.size() > 0 ? Collections.max(xData) : DEFAULT_PLOT_MAX_X;
			for (int i = 0; i < POINTS_FOR_CURVE; i++) {
				double x = maxX * i / (POINTS_FOR_CURVE - 1);
				double[] y = calcFStutter(locus, x);
				curveSeries.getData().add(new XYChart.Data<Number, Number>(x, y[0]));
				curveSeries1.getData().add(new XYChart.Data<Number, Number>(x, y[0] + y[1]));
				curveSeries2.getData().add(new XYChart.Data<Number, Number>(x, y[0] - y[1]));
			}
			xLabel = "Parent Peak Height (RFU)" + numOfPointsPlottedString(xData.size(), plotAllCalibrationPoints);
			yLabel = "Peak Height (RFU) ";
		} else if (label.equals(Feature.NOISE.toString())) {
			xData = noiseXData.get(locus);
			yData = noiseYData.get(locus);
			double maxX = xData.size() > 0 ? Collections.max(xData) : DEFAULT_PLOT_MAX_X;
			for (int i = 0; i < POINTS_FOR_CURVE; i++) {
				double x = maxX * i / (POINTS_FOR_CURVE - 1);
				double[] y = calcNoise(locus, x);
				curveSeries.getData().add(new XYChart.Data<Number, Number>(x, y[0]));
				curveSeries1.getData().add(new XYChart.Data<Number, Number>(x, y[0] + y[1]));
				curveSeries2.getData().add(new XYChart.Data<Number, Number>(x, y[0] - y[1]));
			}
			xLabel = "Decayed Amplitude (RFU)" + numOfPointsPlottedString(xData.size(), plotAllCalibrationPoints);
			yLabel = "Peak Height (RFU)";
		} else if (label.equals(Feature.TRUE_DO.toString())) {
			xData = trueDOXData.get(locus);
			yData = trueDOYData.get(locus);
			double maxX = xData.size() > 0 ? Collections.max(xData) : DEFAULT_PLOT_MAX_X;
			for (int i = 0; i < POINTS_FOR_CURVE; i++) {
				double x = maxX * i / (POINTS_FOR_CURVE - 1);
				double y = calcTrueDO(locus, x);
				curveSeries.getData().add(new XYChart.Data<Number, Number>(x, y));
			}
			xLabel = "Decayed Amplitude (RFU)" + numOfPointsPlottedString(xData.size(), plotAllCalibrationPoints);
			yLabel = "Probability of Dropout";
		} else if (label.equals(Feature.RSTUTTER_DO.toString())) {
			xData = rStutterDOXData.get(locus);
			yData = rStutterDOYData.get(locus);
			double maxX = xData.size() > 0 ? Collections.max(xData) : DEFAULT_PLOT_MAX_X;
			for (int i = 0; i < POINTS_FOR_CURVE; i++) {
				double x = maxX * i / (POINTS_FOR_CURVE - 1);
				double y = calcRStutterDO(locus, x);
				curveSeries.getData().add(new XYChart.Data<Number, Number>(x, y));
			}
			xLabel = "Parent Peak Height (RFU)" + numOfPointsPlottedString(xData.size(), plotAllCalibrationPoints);
			yLabel = "Probability of Dropout";
		} else if (label.equals(Feature.FSTUTTER_DO.toString())) {
			xData = fStutterDOXData.get(locus);
			yData = fStutterDOYData.get(locus);
			double maxX = xData.size() > 0 ? Collections.max(xData) : DEFAULT_PLOT_MAX_X;
			for (int i = 0; i < POINTS_FOR_CURVE; i++) {
				double x = maxX * i / (POINTS_FOR_CURVE - 1);
				double y = calcFStutterDO(locus, x);
				curveSeries.getData().add(new XYChart.Data<Number, Number>(x, y));
			}
			xLabel = "Parent Peak Height (RFU)" + numOfPointsPlottedString(xData.size(), plotAllCalibrationPoints);
			yLabel = "Probability of Dropout";
		} else if (label.equals(Feature.NOISE_DO.toString())) {
			xData = noiseDOXData.get(locus);
			yData = noiseDOYData.get(locus);
			// if all points are not plotted, points are removed and the size
			// will no longer be correct for the number of points plotted string.
			int xDataSize = xData.size();
			double maxX = xData.size() > 0 ? Collections.max(xData) : DEFAULT_PLOT_MAX_X;
			// Need to get corresponding y for each x
			if (xData.size() > Settings.calibrationGraphMaxNumPoints && !plotAllCalibrationPoints) {
				List<ArrayList<Double>> data = randomSamplesSubsetList(xData, yData, maxX);
				xData = data.get(0);
				yData = data.get(1);
			}
			for (int i = 0; i < POINTS_FOR_CURVE; i++) {
				double x = maxX * i / (POINTS_FOR_CURVE - 1);
				double y = calcNoiseDO(locus);
				curveSeries.getData().add(new XYChart.Data<Number, Number>(x, y));
			}
			xLabel = "Decayed Amplitude (RFU)" + numOfPointsPlottedString(xDataSize, plotAllCalibrationPoints);
			yLabel = "Probability of Dropout";
		} else {
			xData = new ArrayList<Double>();
			yData = new ArrayList<Double>();
			xLabel = "";
			yLabel = "";
		}

		XYChart.Series<Number, Number> pointSeries = new XYChart.Series<>();

		// System.out.println("x " + xData.size());

		double ratio = 1.0;
		boolean addAllPoints = true;

		// Noise DO is processes above so number of points will <=
		// Constants.CALIBRATION_GRAPH_MAX_NUMBER_OF_POINTS
		if (xData.size() > Settings.calibrationGraphMaxNumPoints && !plotAllCalibrationPoints) {
			ratio = (double) Settings.calibrationGraphMaxNumPoints / xData.size();
			addAllPoints = false;
		}

		// Generate random number between 0 and 1 if xData is larger than max
		// number of points.
		// Plot the point if ratio > random number.
		// int count = 0;
		for (int i = 0; i < xData.size(); i++) {
			if (!addAllPoints) {
				double randomNumber = Math.random();
				// System.out.println(randomNumber);
				if (ratio > randomNumber) {
					pointSeries.getData().add(new Data<Number, Number>(xData.get(i), yData.get(i)));
					// count += 1;
				}
			} else {
				pointSeries.getData().add(new Data<Number, Number>(xData.get(i), yData.get(i)));
			}
		}
		// System.out.println("count " + count);

		pointSeries.setName("Data");

		curveSeries.setName("Model");

		final NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel(xLabel);

		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel(yLabel);

		final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
		lineChart.getData().add(pointSeries);
		lineChart.getData().add(curveSeries);
		if (!curveSeries1.getData().isEmpty()) {
			curveSeries1.setName("+1 std");
			lineChart.getData().add(curveSeries1);
		}
		if (!curveSeries2.getData().isEmpty()) {
			curveSeries2.setName("-1 std");
			lineChart.getData().add(curveSeries2);
		}
		lineChart.getStylesheets().add(UIMain.class.getResource("LineChart.css").toExternalForm());

		return lineChart;
	}

	/**
	 * Calculates the mean and standard deviation of the height of an allele
	 * peak.
	 *
	 * @param locus
	 *            the locus
	 * @param decayedMass
	 *            mass expected to be present because of degradation
	 * @return double array containing the mean and standard deviation
	 */
	public double[] calcTrue(Locus locus, double decayedMass) {
		double mean = trueParams.get(locus)[0] * decayedMass + trueParams.get(locus)[1];
		double std = trueParams.get(locus)[2] * decayedMass + trueParams.get(locus)[3];
		return new double[] { mean, std };
	}

	/**
	 * Calculate allele peak dropout probability
	 *
	 * @param locus
	 *            the locus
	 * @param decayedMass
	 *            the decayed mass
	 * @return the double
	 */
	public double calcTrueDO(Locus locus, double decayedMass) {
		return trueDOParams.get(locus)[0] * FastMath.exp(trueDOParams.get(locus)[1] * decayedMass);
	}

	/**
	 * Calculates the mean and standard deviation of the height of a reverse
	 * stutter peak.
	 *
	 * @param locus
	 *            the locus
	 * @param x
	 *            the x
	 * @return double array containing the mean and standard deviation
	 */
	public double[] calcRStutter(Locus locus, double x) {
		double mean = rStutterParams.get(locus)[0] * x + rStutterParams.get(locus)[1];
		double std = rStutterParams.get(locus)[2] * x + rStutterParams.get(locus)[3];
		return new double[] { mean, std };
	}

	/**
	 * Calculate reverse stutter dropout probability
	 *
	 * @param locus
	 *            the locus
	 * @param decayedMass
	 *            the decayed mass
	 * @return the double
	 */
	public double calcRStutterDO(Locus locus, double decayedMass) {
		return rStutterDOParams.get(locus)[0] * FastMath.exp(rStutterDOParams.get(locus)[1] * decayedMass);
	}

	/**
	 * Calculates the mean and standard deviation of the height of a forward
	 * stutter peak
	 *
	 * @param locus
	 *            the locus
	 * @param x
	 *            the x
	 * @return double array containing the mean and standard deviation
	 */
	public double[] calcFStutter(Locus locus, double x) {
		double mean = fStutterParams.get(locus)[0] * x + fStutterParams.get(locus)[1];
		double std = fStutterParams.get(locus)[2] * x + fStutterParams.get(locus)[3];
		return new double[] { mean, std };
	}

	/**
	 * Calculate forward stutter dropout probability
	 *
	 * @param locus
	 *            the locus
	 * @param decayedMass
	 *            the decayed mass
	 * @return the probability
	 */
	public double calcFStutterDO(Locus locus, double decayedMass) {
		return fStutterDOParams.get(locus)[0] * FastMath.exp(fStutterDOParams.get(locus)[1] * decayedMass);
	}

	/**
	 * Calculates the mean and standard deviation of the height of a noise peak
	 *
	 * @param locus
	 *            the locus
	 * @param decayedMass
	 *            the decayed mass
	 * @return double array containing the mean and standard deviation
	 */
	public double[] calcNoise(Locus locus, double decayedMass) {
		double mean = noiseParams.get(locus)[0] * decayedMass + noiseParams.get(locus)[1];
		double std = noiseParams.get(locus)[2] * decayedMass + noiseParams.get(locus)[3];
		return new double[] { mean, std };
	}

	/**
	 * Calculate noise dropout probability.
	 *
	 * @param locus
	 *            the locus
	 * @return the probability
	 */
	public double calcNoiseDO(Locus locus) {
		return noiseDOParams.get(locus)[0];
	}

	/**
	 * Gets the loci with enough data points to fit forward stutter model.
	 *
	 * @return the loci with enough data points to fit forward stutter model
	 */
	public HashSet<Locus> getLociFStutter() {
		return lociFStutter;
	}

	/**
	 * Gets the loci with enough data points to fit reverse stutter model.
	 *
	 * @return the loci with enough data points to fit reverse stutter model
	 */
	public HashSet<Locus> getLociRStutter() {
		return lociRStutter;
	}

	/**
	 * Gets the loci without enough data points to fit allele peak model.
	 *
	 * @return the loci without enough data points to fit allele peak model
	 */
	public HashSet<Locus> getLociNoTrue() {
		return lociNoTrue;
	}

	/**
	 * Gets the loci without enough data points to fit noise model.
	 *
	 * @return the loci without enough data points to fit noise model
	 */
	public HashSet<Locus> getLociNoNoise() {
		return lociNoNoise;
	}

	/**
	 * Gets the loci.
	 *
	 * @return the loci
	 */
	public List<Locus> getLoci() {
		return loci;
	}

	/**
	 * Gets the parameters.
	 *
	 * @param feature
	 *            the feature
	 * @return the parameters
	 */
	public HashMap<Locus, double[]> getParams(Feature feature) {
		HashMap<Locus, double[]> params = null;

		switch (feature) {
		case TRUE:
			params = trueParams;
			break;
		case RSTUTTER:
			params = rStutterParams;
			break;
		case FSTUTTER:
			params = fStutterParams;
			break;
		case NOISE:
			params = noiseParams;
			break;
		case FSTUTTER_DO:
			params = fStutterDOParams;
			break;
		case NOISE_DO:
			params = noiseDOParams;
			break;
		case RSTUTTER_DO:
			params = rStutterDOParams;
			break;
		case TRUE_DO:
			params = trueDOParams;
			break;
		default:
			break;
		}

		return params;
	}

	/**
	 * Gets the R^2 values.
	 *
	 * @return the R^2 values
	 */
	public HashMap<Feature, List<Double>> getRSquaredValues() {
		return rSquaredValues;
	}

	/**
	 * Gets the kit.
	 *
	 * @return the kit
	 */
	public Kit getKit() {
		return kit;
	}

	/**
	 * Gets the calibration name.
	 *
	 * @return the calibration name
	 */
	public String getCalibrationName() {
		return calibrationName;
	}

	/**
	 * Sets the calibration name.
	 *
	 * @param calibrationName
	 *            the new calibration name
	 */
	public void setCalibrationName(String calibrationName) {
		this.calibrationName = calibrationName;
	}

	/**
	 * Gets the calibration path.
	 *
	 * @return the calibration path
	 */
	public String getCalibrationPath() {
		return calibrationPath;
	}

	/**
	 * Sets the calibration path.
	 *
	 * @param calibrationPath
	 *            the new calibration path
	 */
	public void setCalibrationPath(String calibrationPath) {
		this.calibrationPath = calibrationPath;
	}

	/**
	 * Noise DO data tends to be highly skewed. This method produces a graph
	 * that resembles a graph where all points are plotted. xData is split into
	 * 10 equal sized bins. Each bin is filled to 1/10 of the desired number of
	 * points if there exists a sufficient number of points in the bin. If any
	 * bins are not filled the list is filled to the desired size by adding data
	 * from the xData list after shuffling the list. If the list contains an x
	 * value, it is not added. An assumption is made that it is extremely
	 * unlikely that two points will have the same x value.
	 *
	 * @param xData
	 *            the x data
	 * @param yData
	 *            the y data
	 * @param max
	 *            the max
	 * @return the list
	 */
	private List<ArrayList<Double>> randomSamplesSubsetList(List<Double> xData, List<Double> yData, double max) {
		List<ArrayList<Double>> xyDataList = new ArrayList<ArrayList<Double>>();
		List<Double> xList = new ArrayList<Double>(xData);
		List<Double> yList = new ArrayList<Double>(yData);
		// System.out.println(max);

		ArrayList<Double> smallXList = new ArrayList<Double>();
		ArrayList<Double> smallYList = new ArrayList<Double>();

		// max < 1 avoids placing data in bins for yData in NoiseDO where all y
		// values are 0 or 1
		int count1 = 0;
		int count2 = 0;
		int count3 = 0;
		int count4 = 0;
		int count5 = 0;
		int count6 = 0;
		int count7 = 0;
		int count8 = 0;
		int count9 = 0;
		int count10 = 0;

		// only calculate these once
		double cutoff1 = 0.1 * max;
		double cutoff2 = 0.2 * max;
		double cutoff3 = 0.3 * max;
		double cutoff4 = 0.4 * max;
		double cutoff5 = 0.5 * max;
		double cutoff6 = 0.6 * max;
		double cutoff7 = 0.7 * max;
		double cutoff8 = 0.8 * max;
		double cutoff9 = 0.9 * max;

		int binSize = Settings.calibrationGraphMaxNumPoints / 10;

		// Get either all points in a category or 1/10 of bin size
		for (int i = 0; i < xList.size(); i++) {
			if (xList.get(i) < cutoff1 && count1 < binSize) {
				// System.out.println("cat 1 " + xList.get(i));
				smallXList.add(xList.get(i));
				// Add corresponding y data
				smallYList.add(yList.get(i));
				count1 += 1;
			} else if (xList.get(i) >= cutoff1 && xList.get(i) < cutoff2 && count2 < binSize) {
				// System.out.println("cat 2 " + xList.get(i));
				smallXList.add(xList.get(i));
				smallYList.add(yList.get(i));
				count2 += 1;
			} else if (xList.get(i) >= cutoff2 && xList.get(i) < cutoff3 && count3 < binSize) {
				// System.out.println("cat 3 " + xList.get(i));
				smallXList.add(xList.get(i));
				smallYList.add(yList.get(i));
				count3 += 1;
			} else if (xList.get(i) >= cutoff3 && xList.get(i) < cutoff4 && count4 < binSize) {
				// System.out.println("cat 4 " + xList.get(i));
				smallXList.add(xList.get(i));
				smallYList.add(yList.get(i));
				count4 += 1;
			} else if (xList.get(i) >= cutoff4 && xList.get(i) < cutoff5 && count5 < binSize) {
				// System.out.println("cat 5 " + xList.get(i));
				smallXList.add(xList.get(i));
				smallYList.add(yList.get(i));
				count5 += 1;
			} else if (xList.get(i) >= cutoff5 && xList.get(i) < cutoff6 && count6 < binSize) {
				// System.out.println("cat 6 " + xList.get(i));
				smallXList.add(xList.get(i));
				smallYList.add(yList.get(i));
				count6 += 1;
			} else if (xList.get(i) >= cutoff6 && xList.get(i) < cutoff7 && count7 < binSize) {
				// System.out.println("cat 7 " + xList.get(i));
				smallXList.add(xList.get(i));
				smallYList.add(yList.get(i));
				count7 += 1;
			} else if (xList.get(i) >= cutoff7 && xList.get(i) < cutoff8 && count8 < binSize) {
				// System.out.println("cat 8 " + xList.get(i));
				smallXList.add(xList.get(i));
				smallYList.add(yList.get(i));
				count8 += 1;
			} else if (xList.get(i) >= cutoff8 && xList.get(i) < cutoff9 && count9 < binSize) {
				// System.out.println("cat 9 " + xList.get(i));
				smallXList.add(xList.get(i));
				smallYList.add(yList.get(i));
				count9 += 1;
			} else if (xList.get(i) >= cutoff9 && count10 < binSize) {
				// System.out.println("cat 10 " + xList.get(i));
				smallXList.add(xList.get(i));
				smallYList.add(yList.get(i));
				count10 += 1;
			}
		}

		// System.out.println("count1 " + count1);
		// System.out.println("count2 " + count2);
		// System.out.println("count3 " + count3);
		// System.out.println("count4 " + count4);
		// System.out.println("count5 " + count5);
		// System.out.println("count6 " + count6);
		// System.out.println("count7 " + count7);
		// System.out.println("count8 " + count8);
		// System.out.println("count9 " + count9);
		// System.out.println("count10 " + count10);

		Collections.shuffle(xList);

		int count = 0;
		int amountNeededToFill = Settings.calibrationGraphMaxNumPoints - smallXList.size();
		// fill the list up to the desired size
		for (int i = 0; i < xList.size(); i++) {
			if (!smallXList.contains(xList.get(i)) && count < amountNeededToFill) {
				smallXList.add(xList.get(i));
				smallYList.add(yList.get(i));
				count += 1;
			}
		}

		// System.out.println(smallList.size());
		xyDataList.add(smallXList);
		xyDataList.add(smallYList);

		return xyDataList;
	}

	/**
	 * Returns string describing the number of points plotted.
	 *
	 * @param xDataSize
	 *            the x data size
	 * @param plotAllPoints
	 *            the plot all points
	 * @return the string
	 */
	private String numOfPointsPlottedString(int xDataSize, boolean plotAllPoints) {
		int numPointsPlotted = xDataSize;
		if (xDataSize > Settings.calibrationGraphMaxNumPoints && !plotAllPoints) {
			numPointsPlotted = Settings.calibrationGraphMaxNumPoints;
		}

		return "\n\n" + numPointsPlotted + " points out of " + xDataSize + " plotted.";
	}

	/**
	 * Get x and y data for testing curve input values.
	 *
	 * @param feature
	 *            the feature
	 * @return the feature XY data
	 */
	public ArrayList<HashMap<Locus, ArrayList<Double>>> getFeatureXYData(Feature feature) {
		HashMap<Locus, ArrayList<Double>> xData = null;
		HashMap<Locus, ArrayList<Double>> yData = null;
		ArrayList<HashMap<Locus, ArrayList<Double>>> xyData = new ArrayList<HashMap<Locus, ArrayList<Double>>>();

		switch (feature) {
		case TRUE:
			xData = trueXData;
			yData = trueYData;
			break;
		case RSTUTTER:
			xData = rStutterXData;
			yData = rStutterYData;
			break;
		case FSTUTTER:
			xData = fStutterXData;
			yData = fStutterYData;
			break;
		case NOISE:
			xData = noiseXData;
			yData = noiseYData;
			break;
		case TRUE_DO:
			xData = trueDOXData;
			yData = trueDOYData;
			break;
		case RSTUTTER_DO:
			xData = rStutterDOXData;
			yData = rStutterDOYData;
			break;
		case FSTUTTER_DO:
			xData = fStutterDOXData;
			yData = fStutterDOYData;
			break;
		case NOISE_DO:
			xData = noiseDOXData;
			yData = noiseDOYData;
			break;
		default:
			xData = null;
			yData = null;
			break;
		}

		xyData.add(xData);
		xyData.add(yData);
		return xyData;
	}
}
