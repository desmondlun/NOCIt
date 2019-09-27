package edu.rutgers.NOCIt.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import edu.rutgers.NOCIt.Control.Settings;

/**
 *
 * Parses sample file and creates hashmap with peaks and alleles at a locus. OL
 * peaks are removed. DO alleles are added. Sample file is a CSV file generated
 * by GeneMapper.
 *
 *
 */
public class Sample {
    private static final int MIN_HEIGHT = 1;
    private static final int THETA1_SIZE = 200;
    private static final double[] REL_THETA_UNIVERSE = { 1.0, 2.0 };
    
	private HashMap<String, ArrayList<LocusData>> dyeData = new LinkedHashMap<String, ArrayList<LocusData>>();
	private HashMap<Locus, LocusData> lociData = new LinkedHashMap<Locus, LocusData>();
	private HashMap<Locus, HashMap<STRAllele,Double>> heightDists = new HashMap<Locus, HashMap<STRAllele, Double>>();
	private HashMap<Locus, ArrayList<Double>> quantParams = new HashMap<Locus, ArrayList<Double>>();
	private Genotype genotype = null;
    
    private HashMap<Locus, double[]> point0s = new HashMap<>();
    private HashMap<Locus, double[]> point1s = new HashMap<>();

    private double dnaMass = 0.0;

    private boolean filtered = false;
    private String sampleFileName = "";
    public HashMap<String, String> populationCaseNumberMap = new HashMap<String, String>();
    public HashMap<String, String> populationCommentsMap = new HashMap<String, String>();

    public Sample() {
    }

    public Sample(Sample sample) {
        for (String dye : sample.dyeData.keySet()) {
            for (LocusData locusData : sample.dyeData.get(dye)) {
                addData(dye, new LocusData(locusData));
            }
        }

        for (Locus locus : sample.heightDists.keySet()) {
            heightDists.put(locus, new HashMap<STRAllele, Double>());
            for (STRAllele strAllele : sample.heightDists.get(locus).keySet()) {
                heightDists.get(locus).put(strAllele, sample.heightDists.get(locus).get(strAllele));
            }
        }

        for (Locus locus : sample.quantParams.keySet()) {
            quantParams.put(locus, new ArrayList<Double>());
            quantParams.get(locus).addAll(sample.quantParams.get(locus));
        }

        this.genotype = sample.genotype;
        this.dnaMass = sample.dnaMass;
    }

    public void addData(String dye, LocusData locusData) {
        if (!dyeData.containsKey(dye)) {
            dyeData.put(dye, new ArrayList<LocusData>());
        }
        dyeData.get(dye).add(locusData);
        
        locusData.getLocus().setDye(dye);
        lociData.put(locusData.getLocus(), locusData);
    }

    /**
     * Calculates the height distribution of peaks that are not in the AMEL
     * locus.
     *
     * @param freqTable
    */
    public void calcHeightDist(FreqTable freqTable) {
        if (freqTable != null) {
            for (Locus locus : lociData.keySet()) {
                if (!freqTable.getProbDists().containsKey(locus)) {
                    continue;
                }
                HashMap<STRAllele, Double> freqDist = freqTable.getProbDists().get(locus);
                if (!locus.isAMEL()) {
                    double heights_sum = 0;
                    for (STRAllele allele : freqDist.keySet()) {
                        if (lociData.get(locus).getPeaks().containsKey(allele)) {
                            Peak peakobj = lociData.get(locus).getPeaks().get(allele);
                            int height = peakobj.getHeight();
                            heights_sum += height;
                        } else {
                            heights_sum += MIN_HEIGHT;
                        }
                    }

                    double min_ratio = MIN_HEIGHT / heights_sum;
                    min_ratio = Math.round(min_ratio * 10000.0) / 10000.0;

                    heightDists.put(locus, new HashMap<STRAllele, Double>());
                    for (STRAllele allele : freqDist.keySet()) {
                        if (lociData.get(locus).getPeaks().containsKey(allele)) {
                            Peak peakobj = lociData.get(locus).getPeaks().get(allele);
                            int height = peakobj.getHeight();
                            double height_ratio = (double) height / heights_sum;
                            height_ratio = Math.round(height_ratio * 1000.0) / 1000.0;
                            heightDists.get(locus).put(allele, height_ratio);
                        } else {
                            heightDists.get(locus).put(allele, min_ratio);
                        }
                    }
                }
            }
        }
    }

	public void calcQuantParams(Kit kit) { 
		for(Entry<String, ArrayList<LocusData>> dyeLocusData : dyeData.entrySet()){
			try{			
				double minMeanSize = Double.POSITIVE_INFINITY;
				double maxMeanSize = Double.NEGATIVE_INFINITY;
				
				double height0 = Double.NaN;
				double height1 = Double.NaN;				
				for (LocusData locusData : dyeLocusData.getValue()) {
					double meanSize = locusData.getMeanSize(kit);
					double sumHeights = locusData.getSumHeights();
					
					if(!Double.isNaN(meanSize) && !Double.isNaN(sumHeights)) {
						if (meanSize < minMeanSize) {
							minMeanSize = meanSize;
							height0 = sumHeights;
						}
						
						if (meanSize > maxMeanSize) {
							maxMeanSize = meanSize;
							height1 = sumHeights;
						}
					}
				}
				
				double b = (FastMath.log(height1) - FastMath.log(height0)) / (maxMeanSize - minMeanSize);
				double A = height0 / FastMath.exp(b * minMeanSize);
				if (!Double.isNaN(b) && !Double.isNaN(A)) {
					ArrayList<Double> params = new ArrayList<Double>();
					params.add(A);
					params.add(b);

					for (LocusData locusData : dyeLocusData.getValue()) {
						double meanSize = locusData.getMeanSize(kit);

						if(!Double.isNaN(meanSize) && meanSize >= minMeanSize && meanSize <= maxMeanSize) {
							quantParams.put(locusData.getLocus(), params);
							point0s.put(locusData.getLocus(), new double[]{minMeanSize, height0});
							point1s.put(locusData.getLocus(), new double[]{maxMeanSize, height1});
						}
					}									
				}
			} catch( Exception e ) {
				e.printStackTrace();
			}

		}
	}
	
	public void writeFilteredSampleFile(String fileName, String sampleName) {
		ArrayList<String[]> entries = new ArrayList<String[]>();
		String line = "";
		for (Locus locus : getLoci()) {
			line += sampleName + "\t";
			line += locus.toString() + "\t";
			LocusData locusData = getLociData().get(locus);
			ArrayList<Allele> alleleList = new ArrayList<Allele>(locusData.getPeaks().keySet());
			ArrayList<String> sortedAlleleNames = locusData.sortedAllelesByName(alleleList);
			for (int j = 0; j < sortedAlleleNames.size(); j++) {
				for (Allele allele : locusData.getPeaks().keySet()) {
					if (allele.toString().equals(sortedAlleleNames.get(j))) {
						Peak peak = locusData.getPeaks().get(allele);
						line += peak.getAllele() + "\t";
						line += peak.getSize() + "\t";
						line += peak.getHeight() + "\t";
					}
				}
			}
			String[] entry = line.substring(0, line.length() - 1).split("\t");
    		entries.add(entry);
    		line = "";
		}
		
		// Add "filtered_" to filename since Excel will not allow two files of same name to be
		// opened at same time even in different directories. Filtered files need to be compared
		// to unfiltered files so both should be able to be opened at same time.
		CSVFileWriter.write(fileName, filteredSamplesFileHeader(), entries);
	}
	
	private String filteredSamplesFileHeader() {
		int DEFAULT_NUMBER_OF_ALLELE_PEAKs = 100;
    	String headerNames = "";
    	headerNames += "Sample File" + "\t";
		headerNames += "Marker" + "\t";
		for (int j = 1; j <= DEFAULT_NUMBER_OF_ALLELE_PEAKs; j++) {
			headerNames += "Allele " + j + "\t";
			headerNames += "Size " + j + "\t";
			headerNames += "Height " + j + "\t";
		}
		return headerNames;
	}

    public void applyThresholds(HashMap<Locus, Integer> analyticalThresholds) {
        for (String dye : dyeData.keySet()) {
            for (LocusData locusData : dyeData.get(dye)) {
                Iterator<Entry<Allele, Peak>> it = locusData.getPeaks().entrySet().iterator();
                while (it.hasNext()) {
                    Peak peak = it.next().getValue();
                    if (analyticalThresholds.get(locusData.getLocus()) != null) {
                    	if (peak.getHeight() < analyticalThresholds.get(locusData.getLocus())) {
                            System.out.println("Sample.applyThresholds: Remove " + peak + " " + dye);
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * Performs bleed-through filtering removing the next peak if the dye is
     * different and height is less than height percentage and size range
     * specified in Settings, and minus-A filtering removing peaks where the dye
     * is the same and height is less than height percentage and size range
     * specified in Settings.
     */
    public void filter(Kit kit) {
        // Perform bleed-through filtering
        for (String dye : dyeData.keySet()) {
            for (LocusData locusData : dyeData.get(dye)) {
                for (Peak peak : locusData.getPeaks().values()) {
                    for (String dye0 : dyeData.keySet()) {
                        if (!dye0.equals(dye)) {
                            for (LocusData locusData0 : dyeData.get(dye0)) {
                                Iterator<Entry<Allele, Peak>> it = locusData0.getPeaks().entrySet().iterator();
                                while (it.hasNext()) {
                                    Peak peak0 = it.next().getValue();
                                    if (Math.abs(peak0.getSize() - peak.getSize()) < Settings.pullUpSizeRange
                                            && peak0.getHeight() < Settings.pullUpHeightPct / 100 * peak.getHeight()) {
                                        System.out.println("Sample.filter: Remove bleed-through " + peak0 + " " + dye0 + " " + peak + " " + dye);
                                        it.remove();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Perform complex bleed-through filtering
        for (String dye : dyeData.keySet()) {
            for (LocusData locusData : dyeData.get(dye)) {
                for (Peak peak1 : locusData.getPeaks().values()) {
                	for (Peak peak2 : locusData.getPeaks().values()) {
                		if (peak2.getSize() > peak1.getSize()
                				&& peak2.getSize() - peak1.getSize() <= kit.getRepeatLength(locusData.getLocus()) + 2 * Settings.complexPullUpSizeRange
                				&& peak1.getHeight() > 0 && peak2.getHeight() > 0
                				&& peak1.getHeight() >= Settings.complexPullUpSisterHeightPct / 100 * peak2.getHeight()
                				&& peak2.getHeight() >= Settings.complexPullUpSisterHeightPct / 100 * peak1.getHeight()) {
                			for (String dye0 : dyeData.keySet()) {
                                if (!dye0.equals(dye)) {
                                    for (LocusData locusData0 : dyeData.get(dye0)) {
                                        Iterator<Entry<Allele, Peak>> it = locusData0.getPeaks().entrySet().iterator();
                                        while (it.hasNext()) {
                                            Peak peak0 = it.next().getValue();
                                            int minHeight = FastMath.min(peak1.getHeight(), peak2.getHeight());
                                            if (peak0.getSize() > peak1.getSize() - Settings.complexPullUpSizeRange
                                            		&& peak0.getSize() < peak2.getSize() + Settings.complexPullUpSizeRange
                                            		&& peak0.getHeight() < Settings.complexPullUpHeightPct / 100 * minHeight) {
                                                System.out.println("Sample.filter: Remove complex bleed-through " + peak0 + " " + dye0 + " " + peak1 + " " + peak2 + " " + dye);
                                                it.remove();
                                            }
                                        }
                                    }
                                }
                            }
                		}                        
                	}
                }
            }
        }

        // Perform minus-A filtering
        for (String dye : dyeData.keySet()) {
            for (LocusData locusData : dyeData.get(dye)) {
                Iterator<Entry<Allele, Peak>> it = locusData.getPeaks().entrySet().iterator();
                while (it.hasNext()) {
                    Peak peak0 = it.next().getValue();
                    for (Peak peak : locusData.getPeaks().values()) {
                        if (Math.abs(peak0.getSize() - peak.getSize() + 1) < Settings.minusASizeRange
                                && peak0.getHeight() < Settings.minusAHeightPct / 100 * peak.getHeight()) {
                            System.out.println("Sample.filter: Remove minus-A " + peak0 + " " + peak + " " + dye);
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public Genotype getGenotype() {
        return genotype;
    }

    public Set<Locus> getLoci() {
        return lociData.keySet();
    }

    public HashMap<Locus, LocusData> getLociData() {
        return lociData;
    }

    public HashMap<Locus, HashMap<STRAllele, Double>> getHeightDists() {
        return heightDists;
    }

    public HashMap<Locus, ArrayList<Double>> getQuantParams() {
        return quantParams;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

	public String getSampleFileName() {
		return sampleFileName;
	}

	public void setSampleFileName(String sampleFileName) {
		this.sampleFileName = sampleFileName;
	}

	public HashMap<double[], HashMap<Locus, double[][]>> generateDiffDegParams(double[] theta0) {
    	HashMap<double[], HashMap<Locus, double[][]>> relThetaParamsMap = new HashMap<>();
    	
    	int noc = theta0.length;
    	for (int i1 = 0; i1 < FastMath.pow(REL_THETA_UNIVERSE.length, noc); i1++) {
        	double[] relTheta = new double[noc - 1];
    		for (int j = 0; j < noc - 1; j++) 
    			relTheta[j] = REL_THETA_UNIVERSE[(i1 / ((int) FastMath.pow(REL_THETA_UNIVERSE.length, j + 1))) 
    			                                 % REL_THETA_UNIVERSE.length]
    			                                		 / REL_THETA_UNIVERSE[i1 % REL_THETA_UNIVERSE.length];    		

    		boolean allOnes = true;
    		for (int j = 0; j < noc - 1; j++)
    			allOnes &= (relTheta[j] == 1.0);
    		
    		if (allOnes && i1 > 0)
    			continue;
    		
    		HashMap<Locus, double[][]> diffDegParams = new HashMap<>();
            for (Locus locus : point0s.keySet()) {
            	double[] point0 = point0s.get(locus);
            	double[] point1 = point1s.get(locus);

            	double sum = 1.0;
            	for (int i = 1; i < noc; i++)             		
            		sum += theta0[i] / theta0[0] * FastMath.pow(relTheta[i - 1], point0[0] / THETA1_SIZE);	
            	double height0 = point0[1] / sum;

            	sum = 1.0;
            	for (int i = 1; i < noc; i++)
            		sum += theta0[i] / theta0[0] * FastMath.pow(relTheta[i - 1], point1[0] / THETA1_SIZE);
            	double height1 = point1[1] / sum;

            	double b = (FastMath.log(height1) - FastMath.log(height0)) / (point1[0] - point0[0]);
            	double A = height0 / FastMath.exp(b * point0[0]);

            	if (!Double.isNaN(b) && !Double.isNaN(A)) {
            		double[][] params = new double[2][noc];
            		params[0][0] = A;
            		params[1][0] = b;

            		for (int i = 1; i < noc; i++) {
            			params[0][i] = A * theta0[i] / theta0[0];
            			params[1][i] = b + FastMath.log(relTheta[i - 1]) / THETA1_SIZE;
            		}
            		
            		boolean isValid = true;
            		if (!allOnes)
            			for (int i = 0; i < noc; i++) {
            				if (params[0][i] < 0) {
            					isValid = false;
            					break;
            				}

            				if (params[1][i] > FastMath.max(0, quantParams.get(locus).get(1))) {
            					isValid = false;
            					break;
            				}
            			}

            		if (isValid)
            			diffDegParams.put(locus, params);
            	}
            }
            
            if (diffDegParams.size() == point0s.size()) {
            	relThetaParamsMap.put(relTheta, diffDegParams);
            } 
    	}
    	
        return relThetaParamsMap;
    }    
}
