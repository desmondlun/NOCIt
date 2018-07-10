package edu.rutgers.NOCIt.Control;

import java.util.ArrayList;

import edu.rutgers.NOCIt.Data.DegradedCalibrationPeak;

public class BlockCoordModel implements SolverModel {
	private SolverModel baseModel;
	private ArrayList<Integer> varInds = new ArrayList<Integer>();
	private double[] fixedVals;
	
	public BlockCoordModel(SolverModel baseModel, double[] fixedVals) {
		this.baseModel = baseModel;
		this.fixedVals = fixedVals;
		
		for (int i = 0; i < fixedVals.length; i++)
			if (Double.isNaN(fixedVals[i]))
				varInds.add(i);
	}

	@Override
	public int getVarCount() {
		return varInds.size();
	}

	@Override
	public int getConstraintCount() {
		return baseModel.getConstraintCount();
	}

	@Override
	public boolean getBounds(double[] x_l, double[] x_u, double[] g_l, double[] g_u) {
		double[] base_x_l = new double[baseModel.getVarCount()];
		double[] base_x_u = new double[baseModel.getVarCount()];
		
		if (baseModel.getBounds(base_x_l, base_x_u, g_l, g_u)) {
			for (int i = 0; i < varInds.size(); i++) {
				x_l[i] = base_x_l[varInds.get(i)];
				x_u[i] = base_x_u[varInds.get(i)];
			}
			
			return true;
		}
		else
			return false;
	}

	@Override
	public double[] getStartingPoint(double[] x) {
		double[] base_x = new double[baseModel.getVarCount()];
		baseModel.getStartingPoint(base_x);
		for (int i = 0; i < varInds.size(); i++)
			x[i] = base_x[varInds.get(i)];
		
		return x;
	}

	@Override
	public double getObjective(double[] x) {
		for (int i = 0; i < varInds.size(); i++) 
			fixedVals[varInds.get(i)] = x[i];
		return baseModel.getObjective(fixedVals);
	}

	@Override
	public boolean getGradObjective(double[] x, double[] grad_f) {
		for (int i = 0; i < varInds.size(); i++) 
			fixedVals[varInds.get(i)] = x[i];
		double[] base_grad_f = new double[baseModel.getVarCount()];
		if (baseModel.getGradObjective(fixedVals, base_grad_f)) {
			for (int i = 0; i < varInds.size(); i++)
				grad_f[i] = base_grad_f[varInds.get(i)];
			
			return true;
		}
		else
			return false;
	}

	@Override
	public boolean getConstraints(double[] g, double[] x) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getGradConstraints(double[] x, double[] values) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getDataSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setData(ArrayList<DegradedCalibrationPeak> peaks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStartingPoint(double[] x0) {
		baseModel.setStartingPoint(x0);
	}

	@Override
	public double[] getNullSolution() {
		return baseModel.getNullSolution();
	}

	@Override
	public ArrayList<Double> getX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Double> getY() {
		// TODO Auto-generated method stub
		return null;
	}
}
