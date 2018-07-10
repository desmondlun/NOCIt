package edu.rutgers.NOCIt.Control;

import java.util.ArrayList;

import edu.rutgers.NOCIt.Data.DegradedCalibrationPeak;

public class StutterModel extends LogLikelihoodModel implements SolverModel
{
	final int a_idx = 0;
	final int b_idx = 1;
	final int c_idx = 2;
	final int d_idx = 3;
//	final int e_idx = 4;
	
	final int n = 4;
	
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
		// set the bounds
		x_l[ a_idx ] = 0.0;
		x_u[ a_idx ] = Double.POSITIVE_INFINITY;
		x_l[ b_idx ] = 0.0; 		
		x_u[ b_idx ] = Double.POSITIVE_INFINITY;
		x_l[ c_idx ] = 0.0;
		x_u[ c_idx ] = Double.POSITIVE_INFINITY;
		x_l[ d_idx ] = 0.0;
		x_u[ d_idx ] = Double.POSITIVE_INFINITY;
//		x_l[ e_idx ] = Double.NEGATIVE_INFINITY;
//		x_u[ e_idx ] = 0.0; 

		return true;
	}

	@Override
	public double[] getStartingPoint( double[] x )
	{
		// set the starting points
		x[ a_idx ] = x0[ a_idx ];
		x[ b_idx ] = x0[ b_idx ];
		x[ c_idx ] = x0[ c_idx ];
		x[ d_idx ] = x0[ d_idx ];
//		x[ e_idx ] = x0[ e_idx ];		
		
		return x;
	}

	@Override
	public boolean getGradObjective( double[] x, double[] grad_f )
	{
//		double sum_partial_x0 = 0.0;
//		double sum_partial_x1 = 0.0;
//		double sum_partial_x2 = 0.0;
//		double sum_partial_x3 = 0.0;
//		double sum_partial_x5 = 0.0;
		double sum_part_X=0.0;
		for( int it = 0; it < vecX.size(); ++it )
		{
			double b = vecX.get( it );
//			double y = vecY.get( it );
//			double e_x1_b = Math.exp( b * x[ e_idx ] );			
//			double num = -e_x1_b * x[0] - x[ b_idx ] + y ;
//			double den = e_x1_b * x[ c_idx ] + x[ d_idx ];
//			
//			sum_partial_x0 += -e_x1_b*num/(den*den);
//			sum_partial_x1 += -b*e_x1_b*x[a_idx]*num/(den*den);
//			sum_partial_x2 += -num/(den*den);
//			sum_partial_x3 += e_x1_b/den - e_x1_b*num*num/(den*den*den);
//			sum_partial_x5 += 1.d / den - num*num/(den*den*den);
			sum_part_X+=b;

		}
		
		grad_f[ a_idx ] = sum_part_X;//sum_partial_x0;
//		grad_f[ e_idx ] = sum_partial_x1;
		grad_f[ b_idx ] = 1.0; //sum_partial_x2;
		grad_f[ c_idx ] = sum_part_X;//sum_partial_x3;
		grad_f[ d_idx ] = 1.0 ;//sum_partial_x5;
		
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

	@Override
	protected double getU( int idx, double[] x )
	{
		return (x[ a_idx ] * vecX.get( idx ) ) + x[ b_idx ];
	}

	@Override
	protected double getV( int idx, double[] x )
	{
		return ( x[ c_idx ] * vecX.get( idx ) ) + x[ d_idx ];
	}

	@Override
	public int getDataSize() {
		return vecY.size();
	}
	
	@Override
    public void setData(ArrayList<DegradedCalibrationPeak> peaks) {
		vecX.clear();
		vecY.clear();
    	for (DegradedCalibrationPeak peak : peaks) {
        	if (peak.getHeight() > 0) {
//        		vecX.add(peak.getDNAMass() * Math.pow(peak.getQI(), -peak.getSize()));
        		vecX.add((double) peak.getAssocHeight());
        		vecY.add(((double) peak.getHeight())) ;;
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
//		x[ e_idx ] = 0.0;	
		
		return x;
	}

	@Override
	public ArrayList<Double> getX() {
		return vecX;
	}

	@Override
	public ArrayList< Double > getY()
	{
		return this.vecY;
	}	
	
	@Override
	public String toString() {
		return "mu = a * x + b; sigma = c * x + d";
	}
}
