package fr.inria.jtanre.extension4test;

import java.util.Collections;
import java.util.List;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.jtanre.rt.core.dynamic.TestCaseExecutor;

/**
 * Used only for test
 * 
 * @author Matias Martinez
 *
 */
public class TestExecutor4Test implements TestCaseExecutor {

	public TestExecutor4Test() {
	}

	@Override
	public FaultLocalizationResult runTests(ProjectRepairFacade projectToRepair, List<String> testToRun)
			throws Exception {
		return new FaultLocalizationResult(null, null);
	}

	@Override
	public List<String> findTestCasesToExecute(ProjectRepairFacade projectFacade) {
		return Collections.EMPTY_LIST;
	}

}
