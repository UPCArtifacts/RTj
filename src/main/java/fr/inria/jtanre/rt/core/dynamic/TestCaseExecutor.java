package fr.inria.jtanre.rt.core.dynamic;

import java.util.List;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.extension.AstorExtensionPoint;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface TestCaseExecutor extends AstorExtensionPoint {

	public FaultLocalizationResult runTests(ProjectRepairFacade projectToRepair, List<String> testToRun)
			throws Exception;

	public abstract List<String> findTestCasesToExecute(ProjectRepairFacade projectFacade);

}
