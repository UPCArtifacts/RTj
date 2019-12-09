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

	public DynamicTestInformation(List<SuspiciousCode> allExecutedStatements) {
		this.allExecutedStatements = allExecutedStatements;
	}

	public List<SuspiciousCode> getAllExecutedStatements() {
		return allExecutedStatements;
	}

	public void setAllExecutedStatements(List<SuspiciousCode> allExecutedStatements) {
		this.allExecutedStatements = allExecutedStatements;
	}

}
