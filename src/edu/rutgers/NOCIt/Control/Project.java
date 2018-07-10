package edu.rutgers.NOCIt.Control;

import java.util.List;

public class Project {	
	private Calibration calibration = null;
	private List<List<List<String>>> treeTableData = null;
	
	public Calibration getCalibration() {
		return calibration;
	}
	public void setCalibration(Calibration calibration) {
		this.calibration = calibration;
	}
	public List<List<List<String>>> getTreeTableData() {
		return treeTableData;
	}
	public void setTreeTableData(List<List<List<String>>> treeTableData) {
		this.treeTableData = treeTableData;
	}

}
