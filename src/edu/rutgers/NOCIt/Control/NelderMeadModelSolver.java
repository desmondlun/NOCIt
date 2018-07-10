package edu.rutgers.NOCIt.Control;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

public class NelderMeadModelSolver implements ModelSolverInterface
{
	private static final double REL_TOL = 1e-9;
	private static final double ABS_TOL = 1e-9;
	private static final int MAX_EVAL = Settings.nelderMeadModelSolverMaxEval;
	
	public double[] solveModel( SolverModel model )
	{
		double[] res = null;

		MultivariateFunction costFunc = new MultivariateFunction()
		{
			@Override
			public double value( double[] point ) throws IllegalArgumentException
			{
				return model.getObjective( point );
			}
		};
		
		SimplexOptimizer optimizer = new SimplexOptimizer(REL_TOL, ABS_TOL);
		
		double[] x_l = new double[model.getVarCount()];
		double[] x_u = new double[model.getVarCount()];
		double[] g_l = new double[model.getConstraintCount()];
		double[] g_u = new double[model.getConstraintCount()];
		model.getBounds(x_l, x_u, g_l, g_u);
		
		double[] points = new double[ model.getVarCount() ];
		try
		{
			PointValuePair vals = optimizer.optimize(new MaxEval(MAX_EVAL), 
					new ObjectiveFunction(costFunc), 
					GoalType.MINIMIZE, new InitialGuess(model.getStartingPoint( points )),
					new NelderMeadSimplex(model.getVarCount()));//, new SimpleBounds(x_l,x_u));
			
			res = new double[vals.getPoint().length];
			for( int i = 0; i < vals.getPoint().length; i++ ) {
				double d = vals.getPoint()[i];
				if (d > x_u[i]) d = x_u[i];
				if (d < x_l[i]) d = x_l[i];
				res[i] = d;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		return res;
	}
	
	public static double[] staticSolveModel( SolverModel model )
	{
		ModelSolverInterface solver = new NelderMeadModelSolver();
		return solver.solveModel( model );
	}
}
