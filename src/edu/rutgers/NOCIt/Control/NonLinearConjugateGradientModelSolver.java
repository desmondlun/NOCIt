package edu.rutgers.NOCIt.Control;

import java.util.Arrays;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;

public class NonLinearConjugateGradientModelSolver implements ModelSolverInterface {
	private static final double REL_TOL = 1e-9;
	private static final double ABS_TOL = 1e-6;
	private static final int MAX_EVAL = (int)1e+6;
	
	@Override
	public double[] solveModel(SolverModel model) {
		double[] res = null;
		
		MultivariateFunction costFunc = new MultivariateFunction()
		{
			@Override
			public double value( double[] point ) throws IllegalArgumentException
			{
				return model.getObjective( point );
			}
		};
		
		MultivariateVectorFunction gradFunc = new MultivariateVectorFunction()
		{

			@Override
			public double[] value(double[] point) throws IllegalArgumentException {
				double[] grad_f = new double[model.getVarCount()];
				model.getGradObjective(point, grad_f);
				return grad_f;
			}
		};
		double[] x_u= new double[ model.getVarCount() ];
                double[] x_l=new double[ model.getVarCount() ];
                model.getBounds(x_l, x_u, null, null);
                SimpleBounds bnds= new  SimpleBounds(x_l, x_u);
		NonLinearConjugateGradientOptimizer optimizer = 
				new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
						new SimplePointChecker(REL_TOL, ABS_TOL));
		
		double[] points = new double[ model.getVarCount() ];
//                
//                System.out.println("bounds :"+Arrays.toString(bnds.getLower())+";"+Arrays.toString(bnds.getUpper()));
		try
		{
			PointValuePair vals = optimizer.optimize(new MaxEval(MAX_EVAL), new ObjectiveFunction(costFunc), 
//					new ObjectiveFunctionGradient(gradFunc),
                                        GoalType.MINIMIZE, new InitialGuess(model.getStartingPoint( points )));
			
			res = vals.getPoint();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
                System.out.println("NLGrad res "+Arrays.toString(res));
		return res;
	}
	
	public static double[] staticSolveModel( SolverModel model )
	{
		ModelSolverInterface solver = new NonLinearConjugateGradientModelSolver();
		return solver.solveModel( model );
	}
}
