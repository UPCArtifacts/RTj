package fr.inria.jtanre.rt.out;

import java.util.List;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.jtanre.rt.RtEngine;
import fr.inria.jtanre.rt.core.TestIntermediateAnalysisResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public interface RtOutput {

	Object generateOutput(RtEngine engine, String id, ProjectRepairFacade projectFacade2,
			List<TestIntermediateAnalysisResult> resultByTest, List<ProgramVariant> refactors,
			Exception exceptionReceived, String out) throws Exception;

}
