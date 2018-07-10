package edu.rutgers.NOCIt.Control;

import java.util.ArrayList;

import edu.rutgers.NOCIt.Data.DegradedCalibrationPeak;

public class NoisePeakModel extends LogLikelihoodModel implements SolverModel
{
	final private int a_idx = 0;
	final private int b_idx = 1;
	final private int c_idx = 2;
	final private int d_idx = 3;
	
	final private int n = 4;
	
	private ArrayList< Double > vecA = new ArrayList<>();
	private ArrayList< Double > vecB = new ArrayList<>();
        
	private ArrayList< Double > vecS = new ArrayList<>();
	private ArrayList< Double > vecH = new ArrayList<>();
	
	private double[] x0 = new double[n];
	
	public int getVarCount()
	{
		return n;
	}
	
	public int getConstraintCount()
	{
		return 0;
	}
	
	public boolean getConstraints( double[] g, double[] x )
	{
		return false;
	}

	public boolean getGradObjective( double[] x, double[] grad_f )
	{
		return false;
	}
	
	public boolean getGradConstraints( double[] x, double[] values )
	{
		return false;
	}
	
	@Override
	protected double getU( int idx, double[] x )
	{
		return x[ a_idx ] * vecA.get( idx ) * Math.exp( vecB.get( idx ) * vecS.get( idx )) + x[ b_idx ];
	}

	@Override
	protected double getV( int idx, double[] x )
	{
		return x[ c_idx ] * vecA.get( idx ) * Math.exp( vecB.get( idx ) * vecS.get( idx )) + x[ d_idx ];
	}

	@Override
	public boolean getBounds( double[] x_l, double[] x_u, double[] g_l, double[] g_u )
	{
		// set the bounds
		for( int it = 0; it < n; ++it )
		{
			x_l[ it ] = Double.NEGATIVE_INFINITY;
			x_u[ it ] = Double.POSITIVE_INFINITY;
		}
		
		x_l[ a_idx ] = 0.0; 		
		x_l[ c_idx ] = 0.0;
		x_l[ d_idx ] = 0.0;

//		for( int it = 0; it < getConstraintCount(); ++it )
//		{
//			g_l[ it ] = Double.MIN_VALUE;
//			g_u[ it ] = Double.POSITIVE_INFINITY;
//		}
		
		return true;
	}

	@Override
	public double[] getStartingPoint( double[] x )
	{
		x[ a_idx ] = x0[ a_idx ];
		x[ b_idx ] = x0[ b_idx ];
		x[ c_idx ] = x0[ c_idx ];
		x[ d_idx ] = x0[ d_idx ];

		return x;
	}

	public int getDataSize() {
		return vecH.size();
	}

	public void setData(ArrayList<DegradedCalibrationPeak> peaks) {
		vecA.clear();
		vecB.clear();
		vecS.clear();
		vecH.clear();
		
        for (DegradedCalibrationPeak peak : peaks) {
        	if (peak.getHeight() > 0) {
        		vecA.add(peak.getDNAMass());//= null;
        		vecB.add(-Math.log(peak.getQI()));
        		vecS.add(peak.getSize());// = null;
        		vecH.add(new Double(peak.getHeight()));// = null;
        	}
        }
	}
	
	@Override
	public void setStartingPoint( double[] x0 ) {
		this.x0 = x0;
	}

	@Override
	public double[] getNullSolution() {
		double s1 = 0.0;
		double s2 = 0.0;
		for (int i = 0; i < getY().size(); i++) {
			s1 += getY().get(i);
			s2 += getY().get(i) * getY().get(i);
		}
			
		double meanY = s1 / getY().size();
		double stdY = Math.sqrt((getY().size() * s2 - s1 * s1) / getY().size() / (getY().size() - 1));
		
		double[] x = new double[n];
		x[ a_idx ] = 0.0;
		x[ b_idx ] = meanY;
		x[ c_idx ] = 0.0;
		x[ d_idx ] = stdY;
		
		return x;
	}

	@Override
	public ArrayList<Double> getX() {
		ArrayList<Double> vecXs = new ArrayList<Double>();	
		for (int idx = 0; idx < vecA.size(); idx++) {
			vecXs.add(vecA.get( idx ) * Math.exp( vecB.get( idx ) * vecS.get( idx )));
		}

		return vecXs;
	}

	@Override
	public ArrayList< Double > getY()
	{
		return vecH;
	}	
	
	@Override
	public String toString() 
	{
		return "mu = a * x + b; sigma = c * x + d";
		
	}
}
