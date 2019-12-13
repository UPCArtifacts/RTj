package fr.inria.jtanre.rt.out;

import java.util.List;

import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.util.PatchDiffCalculator;
import fr.inria.jtanre.rt.RtEngine;
import fr.inria.jtanre.rt.core.TestIntermediateAnalysisResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public class RefactorOutput implements RtOutput {

	@Override
	public Object generateOutput(RtEngine engine, String id, ProjectRepairFacade projectFacade,
			List<TestIntermediateAnalysisResult> resultByTest, List<ProgramVariant> refactors,
			Exception exceptionReceived, String out) throws Exception {
		if (ConfigurationProperties.getPropertyBool("refactor")) {
			for (ProgramVariant programVariant : refactors) {
				saveRtVariant(engine, programVariant, projectFacade);
			}
		}
		return null;
	}

	private void saveRtVariant(RtEngine engine, ProgramVariant programVariant, ProjectRepairFacade projectFacade)
			throws Exception {

		for (OperatorInstance oi : programVariant.getAllOperations()) {
			oi.applyModification();
		}

		final boolean codeFormated = true;
		savePatchDiff(engine, programVariant, projectFacade, codeFormated);

		// Finally, reverse the changes done by the child
		engine.reverseOperationInModel(programVariant, 1);

	}

	private void savePatchDiff(RtEngine engine, ProgramVariant programVariant, ProjectRepairFacade projectFacade,
			boolean format) throws Exception {

		boolean originalValue = ConfigurationProperties.getPropertyBool("preservelinenumbers");

		final String suffix = format ? PatchDiffCalculator.DIFF_SUFFIX : "";
		String srcOutput = projectFacade.getInDirWithPrefix(programVariant.currentMutatorIdentifier()) + suffix;

		System.out.println("\n-Saving Refactored test #" + programVariant.getId() + " at " + srcOutput);
		ConfigurationProperties.setProperty("preservelinenumbers", Boolean.toString(!format));
		engine.getMutatorSupporter().saveSourceCodeOnDiskProgramVariant(programVariant, srcOutput);

		ConfigurationProperties.setProperty("preservelinenumbers", Boolean.toString(originalValue));
	}

}
