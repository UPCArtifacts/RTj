package fr.inria.jtanre.rt.core.dynamic;

import java.util.List;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.FaultLocalizationStrategy;
import fr.inria.astor.core.setup.ProjectRepairFacade;

/**
 * Wrapper of Fault localization
 * 
 * @author Matias Martinez
 *
 */
public class TestExecutorWrapperFaultLocalization implements TestCaseExecutor {

	FaultLocalizationStrategy faultLocalization = null;

	public TestExecutorWrapperFaultLocalization(FaultLocalizationStrategy fl) {
		this.faultLocalization = fl;
	}

	@Override
	public FaultLocalizationResult runTests(ProjectRepairFacade projectToRepair, List<String> testToRun)
			throws Exception {
		return this.faultLocalization.searchSuspicious(projectToRepair, testToRun);
	}

	@Override
	public List<String> findTestCasesToExecute(ProjectRepairFacade projectFacade) {
		return this.faultLocalization.findTestCasesToExecute(projectFacade);
	}

	public FaultLocalizationStrategy getFaultLocalization() {
		return faultLocalization;
	}

	public void setFaultLocalization(FaultLocalizationStrategy faultLocalization) {
		this.faultLocalization = faultLocalization;
	}

}
