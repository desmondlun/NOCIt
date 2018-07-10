package edu.rutgers.NOCIt.Control;

public class BlockCoordModelSolver implements ModelSolverInterface {
	private static final double REL_TOL = 1e-9;
	private static final int MAX_ITER = 1000;
	
	private int[][] blockConfig;

	public BlockCoordModelSolver(int[][] blockConfig) {
		this.blockConfig = blockConfig;
	}
	
	@Override
	public double[] solveModel(SolverModel model) {
		ModelSolverInterface baseSolver = new NelderMeadModelSolver();
//		ModelSolverInterface baseSolver = new NonLinearConjugateGradientModelSolver();
		
		double[] fixedVals = new double[model.getVarCount()];
		model.getStartingPoint(fixedVals);
		double prevObj = 0.0;
		double obj = model.getObjective(fixedVals);
//		System.out.println(obj);
		
		double bestObj = Double.POSITIVE_INFINITY;
		double[] bestResult = new double[model.getVarCount()];
		int iter = 0;
		do {
			for (int i = 0; i < blockConfig.length; i++) {
				prevObj = obj;
				for (int j = 0; j < blockConfig[i].length; j++)
					fixedVals[blockConfig[i][j]] = Double.NaN;

				BlockCoordModel blockCoordModel = new BlockCoordModel(model, fixedVals);
				double[] result = baseSolver.solveModel(blockCoordModel);				

				for (int j = 0; j < result.length; j++)
					fixedVals[blockConfig[i][j]] = result[j];
				obj = model.getObjective(fixedVals);
				
				if (obj < bestObj) {
					bestObj = obj;
					System.arraycopy(fixedVals, 0, bestResult, 0, fixedVals.length);	
				}
				
				iter++;

//				System.out.println(obj + " " + Arrays.toString(fixedVals));
			}
		} while (Math.abs(prevObj - obj) >= Math.abs(REL_TOL * obj) && iter < MAX_ITER);
		
		return bestResult;
	}

	public static double[] staticSolveModel( SolverModel model, int[][] blockConfig )
	{
		BlockCoordModelSolver solver = new BlockCoordModelSolver(blockConfig);
		return solver.solveModel( model );
	}
}
