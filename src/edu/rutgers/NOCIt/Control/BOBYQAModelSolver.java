package edu.rutgers.NOCIt.Control;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

public class BOBYQAModelSolver implements ModelSolverInterface
{
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
		
		BOBYQAOptimizer optimizer = new BOBYQAOptimizer(model.getVarCount() + 2);//, , 1e-12);
		
		double[] x_l = new double[model.getVarCount()];
		double[] x_u = new double[model.getVarCount()];
		double[] g_l = new double[model.getConstraintCount()];
		double[] g_u = new double[model.getConstraintCount()];
		model.getBounds(x_l, x_u, g_l, g_u);
		
		double[] points = new double[ model.getVarCount() ];
		try
		{
			PointValuePair vals = optimizer.optimize(MaxEval.unlimited(), MaxIter.unlimited(),
					new ObjectiveFunction(costFunc), new SimpleBounds(x_l, x_u),
					GoalType.MINIMIZE, new InitialGuess(model.getStartingPoint( points )));
			
			res = vals.getPoint();
		}
		catch( Exception e )
		{
			e.printStackTrace();
			
			res = model.getNullSolution();
		}

		return res;
	}
	
	public static double[] staticSolveModel( SolverModel model )
	{
		ModelSolverInterface solver = new BOBYQAModelSolver();
		return solver.solveModel( model );
	}
}
