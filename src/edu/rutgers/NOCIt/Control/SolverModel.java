package edu.rutgers.NOCIt.Control;

import java.util.ArrayList;

import edu.rutgers.NOCIt.Data.DegradedCalibrationPeak;

public interface SolverModel
{
	public ArrayList< Double > getX( );
	
	public ArrayList< Double > getY();
	
	public int getVarCount();
	
	public int getConstraintCount();
	
	public boolean getBounds( double x_l[], double x_u[], double g_l[], double g_u[] );
	
	public double[] getStartingPoint( double[] x );
	
	public double getObjective( double[] x );

	public boolean getGradObjective( double[] x, double[] grad_f );
	
	public boolean getConstraints( double[] g, double[] x );
	
	public boolean getGradConstraints( double[] x, double[] values );
	
	public int getDataSize();
	
	public void setData(ArrayList<DegradedCalibrationPeak> peaks);
	
	public void setStartingPoint( double[] x0 );
	
	public double[] getNullSolution();
}
