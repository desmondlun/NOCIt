package edu.rutgers.NOCIt.Control;

import java.util.ArrayList;

import edu.rutgers.NOCIt.Data.DegradedCalibrationPeak ;

/** an abstract class for the log likelihood models */
public abstract class LogLikelihoodModel implements SolverModel
{
	protected abstract double getU( int idx, double[] x );
	
	protected abstract double getV( int idx, double[] x );
        
	public abstract void setData(ArrayList< DegradedCalibrationPeak > peaks);
	
	public double getObjective( double[] x )
	{
		double sum = 0.5 * getY().size() * Math.log(2 * Math.PI);

		for( int it = 0; it < getY().size(); ++it )
		{
			double u_i = this.getU( it, x );
			double v_i = this.getV( it, x );

			sum += Math.log( v_i ) + 0.5 * Math.pow( ( getY().get( it ) - u_i ) / v_i, 2.0 );
		}		

		return sum;		
	}	
}
