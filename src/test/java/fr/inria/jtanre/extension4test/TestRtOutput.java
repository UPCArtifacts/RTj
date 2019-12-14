package fr.inria.jtanre.extension4test;

import java.util.List;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.jtanre.rt.RtEngine;
import fr.inria.jtanre.rt.core.TestIntermediateAnalysisResult;
import fr.inria.jtanre.rt.out.RtOutput;

/**
 * * Used only for test
 * 
 * @author Matias Martinez
 *
 */
public class TestRtOutput implements RtOutput {

	@Override
	public Object generateOutput(RtEngine engine, String id, ProjectRepairFacade projectFacade2,
			List<TestIntermediateAnalysisResult> resultByTest, List<ProgramVariant> refactors,
			Exception exceptionReceived, String out) throws Exception {
		return null;
	}

}
