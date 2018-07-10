package edu.rutgers.NOCIt.Control;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.MersenneTwister;

public class CMAESModelSolver implements ModelSolverInterface
{
	private static final double REL_TOL = 1e-9;
	private static final double ABS_TOL = 1e-9;
	private static final int MAX_EVAL = 1000000;
	
	private double[] sigma;
	private int restarts;
	
	public CMAESModelSolver(double[] sigma, int restarts) {
		this.sigma = sigma;
		this.restarts = restarts;
	}
	
	public double[] solveModel( SolverModel model )
	{
		double[] res = new double[model.getVarCount()];
                        res=model.getStartingPoint(res);

		MultivariateFunction costFunc = new MultivariateFunction()
		{
			@Override
			public double value( double[] point ) throws IllegalArgumentException
			{
				return model.getObjective( point );
			}
		};
		
		final int populationSize = (int) (4 + Math.floor(3 * Math.log(model.getVarCount())));
		final int maxIter = (int) (1e3 * Math.pow(model.getVarCount() + 5, 2.0) / Math.sqrt(populationSize));		
		
		CMAESOptimizer optimizer = new CMAESOptimizer(maxIter, Double.NEGATIVE_INFINITY, false, 0, 0,
				new MersenneTwister(), false, new SimplePointChecker(REL_TOL, ABS_TOL));
		
		double[] x_l = new double[model.getVarCount()];
		double[] x_u = new double[model.getVarCount()];
		double[] g_l = new double[model.getConstraintCount()];
		double[] g_u = new double[model.getConstraintCount()];
		model.getBounds(x_l, x_u, g_l, g_u);
//		          System.out.println("Bounds :"+Arrays.toString(x_l)+" ; "+Arrays.toString(x_u));

//                System.out.println("optim started with :"+Arrays.toString(res));
		double bestOpt = Double.POSITIVE_INFINITY;
		for (int i = 0; i < restarts; i++) {
//                    System.out.println("i= "+i+" ; Optf "+bestOpt+" par ="+Arrays.toString(res));
			double[] points = new double[ model.getVarCount() ];
			try
			{
				PointValuePair vals = optimizer.optimize(new MaxEval(MAX_EVAL), new ObjectiveFunction(costFunc), 
						GoalType.MINIMIZE, new InitialGuess(model.getStartingPoint(points)),//model.getStartingPoint( points )), 
                                                new SimpleBounds(x_l, x_u), 
						new CMAESOptimizer.PopulationSize(populationSize),
						new CMAESOptimizer.Sigma(sigma));

				if (vals.getValue() < bestOpt) {
					bestOpt = vals.getValue();
					res = vals.getPoint();
				}
//                                System.out.println("new optim :"+Arrays.toString(vals.getPoint())+"with cost "+vals.getValue()/model.getDataSize());
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
                
		return res;
	}
	
	public static double[] staticSolveModel( SolverModel model, double[] sigma, int restarts )
	{
		ModelSolverInterface solver = new CMAESModelSolver(sigma, restarts);
		return solver.solveModel( model );
	}
}
