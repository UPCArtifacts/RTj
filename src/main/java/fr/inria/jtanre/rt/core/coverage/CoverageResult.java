package fr.inria.jtanre.rt.core.coverage;

import java.util.List;
import java.util.Map;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.spoonlabs.spfl.MatrixCoverage;

/**
 * 
 * @author Matias Martinez
 *
 */
public class CoverageResult extends FaultLocalizationResult {
	MatrixCoverage matrix;
	Map<String, Double> susp;

	public CoverageResult(List<SuspiciousCode> candidates, List<String> failingTestCases, MatrixCoverage matrix,
			Map<String, Double> susp) {
		super(candidates, failingTestCases);
		this.matrix = matrix;
		this.susp = susp;
	}

	public MatrixCoverage getMatrix() {
		return matrix;
	}

	public void setMatrix(MatrixCoverage matrix) {
		this.matrix = matrix;
	}

	public Map<String, Double> getSusp() {
		return susp;
	}

	public void setSusp(Map<String, Double> susp) {
		this.susp = susp;
	}

}
