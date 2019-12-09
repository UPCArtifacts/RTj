package fr.inria.jtanre.rt.core;

import java.util.List;

import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DynamicTestInformation {

	List<SuspiciousCode> allExecutedStatements = null;

	List<String> testExecuted = null;

	public DynamicTestInformation(List<SuspiciousCode> allExecutedStatements, List<String> testExecuted) {
		this.allExecutedStatements = allExecutedStatements;
		this.testExecuted = testExecuted;
	}

	public List<SuspiciousCode> getAllExecutedStatements() {
		return allExecutedStatements;
	}

	public void setAllExecutedStatements(List<SuspiciousCode> allExecutedStatements) {
		this.allExecutedStatements = allExecutedStatements;
	}

	public List<String> getTestExecuted() {
		return testExecuted;
	}

	public void setTestExecuted(List<String> testExecuted) {
		this.testExecuted = testExecuted;
	}

}
