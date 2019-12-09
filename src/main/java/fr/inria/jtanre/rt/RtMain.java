package fr.inria.jtanre.rt;

import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.jtanre.rt.core.DynamicTestInformation;
import fr.inria.jtanre.rt.core.ProgramModel;
import fr.inria.main.evolution.AstorMain;

/**
 * 
 * @author Matias Martinez
 *
 */
public class RtMain extends AstorMain {

	@Override
	public void run(String location, String projectName, String dependencies, String packageToInstrument, double thfl,
			String failing) throws Exception {

		long startT = System.currentTimeMillis();

		initProject(location, projectName, dependencies, packageToInstrument, thfl, failing);

		MutationSupporter mutSupporter = new MutationSupporter();

		String mode = ConfigurationProperties.getProperty("mode").toLowerCase();
		// Creation of the execution mode
		if (mode.equals("rt")) {
			rtCore = new RtEngine(mutSupporter, projectFacade);
		} else {
			rtCore = createEngineFromArgument(mode, mutSupporter, projectFacade);
		}

		// Execution:

		// Model creation
		ProgramModel<?> model = ((RtEngine) rtCore).createModel();

		// Loading extension Points
		DynamicTestInformation dynamicInfo = ((RtEngine) rtCore).runTests();

		ConfigurationProperties.print();

		((RtEngine) rtCore).runTestAnalyzers(model, dynamicInfo);

		((RtEngine) rtCore).atEnd();

		long endT = System.currentTimeMillis();
		log.info("Time Total(s): " + (endT - startT) / 1000d);
	}

}
