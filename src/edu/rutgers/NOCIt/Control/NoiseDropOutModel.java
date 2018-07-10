package edu.rutgers.NOCIt.Control;

import java.util.ArrayList;

import edu.rutgers.NOCIt.Data.DegradedCalibrationPeak;

public class NoiseDropOutModel implements SolverModel
{
	final int a_idx = 0;
	
	final int n = 1;
	
	private ArrayList< Double > vecX = new ArrayList< Double >();
	private ArrayList< Double > vecY = new ArrayList< Double >();

	private double[] x0 = new double[n];
	
	@Override
	public int getVarCount()
	{
		return n;
	}

	@Override
	public int getConstraintCount()
	{
		return 0;
	}

	@Override
	public boolean getBounds( double[] x_l, double[] x_u, double[] g_l, double[] g_u )
	{
		x_l[ a_idx ] = 0.0;
		x_u[ a_idx ] = 1.0;
		
		// log( 1 - p(xi) )  s.t.  1-p(xi) > 0
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
		
		return x;
	}

	@Override
	public double getObjective( double[] x )
	{
		double sum = 0.d;

		for( int it = 0; it < vecY.size(); ++it )
		{
//			double p_i = 1.d - ( 1.d / (1.d + Math.exp( -x[ a_idx ] * (vecXs.get( it ) - x[ b_idx ]) )) );
			double p_i = x[ a_idx ];
			double y_i = vecY.get( it );

			sum += -Math.log(p_i * y_i + (1 - p_i) * (1 - y_i));
		}		

		return sum;
	}

	@Override
	public boolean getGradObjective( double[] x, double[] grad_f )
	{
		double sum_partial_a = 0.d;		

		for( int it = 0; it < vecY.size(); ++it )
		{
//			double a = x[ a_idx ];
//			double b = x[ b_idx ];
//			double x_i = vecXs.get( it );
//			double y_i = vecYs.get( it );
//			double e_axb = Math.exp( -a * (x_i - b) );
//
//			sum_partial_a += (2 * y_i - 1) / (y_i + e_axb * (1 - y_i)) * e_axb / (1 + e_axb) / (b - x_i);
//			sum_partial_b += (2 * y_i - 1) / (y_i + e_axb * (1 - y_i)) * e_axb / (1 + e_axb) / a;
			
			double y_i = vecY.get( it );
			double p_i = x[ a_idx ];
			
			sum_partial_a += (1 - 2 * y_i) / (p_i * y_i + (1 - p_i) * (1 - y_i));		
		}

		grad_f[ a_idx ] = sum_partial_a;

		return true;
	}

	@Override
	public boolean getConstraints( double[] g, double[] x )
	{
		return false;
	}

	@Override
	public boolean getGradConstraints( double[] x, double[] values )
	{
		return false;
	}
	
	public int getDataSize() {
		return vecY.size();
	}
	
	public void setData(ArrayList<DegradedCalibrationPeak> peaks) 
	{	
		vecX.clear();
		vecY.clear();
		
		for (DegradedCalibrationPeak peak : peaks) {			
			if (!Double.isNaN(peak.getSize())) {
				vecX.add(peak.getDNAMass() * Math.pow(peak.getQI(), -peak.getSize()));
//				vecXs.add((double) peak.getAssocHeight());
				if (peak.getHeight() > 0)
					vecY.add(0.0);
				else
					vecY.add(1.0);
			}
		}
	}

	@Override
	public void setStartingPoint(double[] x0) {
		this.x0 = x0;
	}

	@Override
	public double[] getNullSolution() {
		double meanY = 0.0;
		for (Double y : vecY)
			meanY += y;
		meanY /= vecY.size();				
		
		double[] x = new double[n];
		x[ a_idx ] = meanY;
		
		return x;
	}

	@Override
	public ArrayList<Double> getX() {
		return vecX;
	}

	@Override
	public ArrayList<Double> getY() {
		return vecY;
	}
	
	@Override
	public String toString() {
		return "a";
	}
}
