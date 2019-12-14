package fr.inria.jtanre.rt;

import org.apache.commons.cli.CommandLine;

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

	static {
		options.addOption("outputs", true, "Adds new outputs processors");
		options.addOption("analyzers", true, "Adds new analyzers");
		options.addOption("testexecutor", true, "Adds new analyzers");
		options.addOption("printrottentest", false, "Prints the information of the rotten green test found");
		options.addOption("refactor", false, "Refactor of rotten tests");
	}

	@Override
	public void run(String location, String projectName, String dependencies, String packageToInstrument, double thfl,
			String failing) throws Exception {

		long startT = System.currentTimeMillis();

		initProject(location, projectName, dependencies, packageToInstrument, thfl, failing);

		MutationSupporter mutSupporter = new MutationSupporter();

		String mode = ConfigurationProperties.getProperty("mode").toLowerCase();
		// Creation of the execution mode
		if (mode.equals("rt")) {
			core = new RtEngine(mutSupporter, projectFacade);
		} else {
			core = createEngineFromArgument(mode, mutSupporter, projectFacade);
		}

		// Execution:

		// Model creation

		ProgramModel model = ((RtEngine) core).createModel();

		// Loading extension Points
		DynamicTestInformation dynamicInfo = ((RtEngine) core).runTests();

		if (!ConfigurationProperties.getPropertyBool("skipanalysis")) {

			((RtEngine) core).runTestAnalyzers(model, dynamicInfo);
		}
		((RtEngine) core).atEnd();

		long endT = System.currentTimeMillis();
		System.out.println("Execution Time: " + ((endT - startT) / 1000d) + " seconds");
	}

	@Override
	public void processOtherCommands(CommandLine cmd) {
		if (cmd.hasOption("analyzers")) {
			ConfigurationProperties.properties.setProperty("analyzers", cmd.getOptionValue("analyzers"));
		}
		if (cmd.hasOption("outputs")) {
			ConfigurationProperties.properties.setProperty("outputs", cmd.getOptionValue("outputs"));
		}
		if (!cmd.hasOption("mode")) {
			ConfigurationProperties.properties.setProperty("mode", "rt");
		}

		if (cmd.hasOption("printrottentest")) {
			ConfigurationProperties.properties.setProperty("printrottentest", "true");
		}

		if (!cmd.hasOption("autoconfigure"))
			ConfigurationProperties.properties.setProperty("autoconfigure", "true");

		if (cmd.hasOption("refactor")) {
			ConfigurationProperties.properties.setProperty("refactor", "true");
		}

		if (cmd.hasOption("testexecutor")) {
			ConfigurationProperties.properties.setProperty("testexecutor", cmd.getOptionValue("testexecutor"));
		}
	}

	public static void main(String[] args) throws Exception {
		RtMain m = new RtMain();
		m.execute(args);
	}
}
