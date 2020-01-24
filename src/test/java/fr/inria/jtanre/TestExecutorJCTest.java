package fr.inria.jtanre;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.Coverage;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.jtanre.rt.RtEngine;
import fr.inria.jtanre.rt.RtMain;
import fr.inria.main.CommandSummary;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TestExecutorJCTest {

	public TestExecutorJCTest(ProjectRepairFacade facade) throws TimeoutException {

		String classpath = facade.getProperties().getDependenciesString();
		String targetProjectClasses = toS(facade.getProperties().getOriginalAppBinDir()) + File.pathSeparator
				+ toS(facade.getProperties().getOriginalTestBinDir());

		List<String> itemList = facade.getProperties().getRegressionTestCases();

		String[] fullQualifiedNameOfTestClasses = new String[itemList.size()];

		fullQualifiedNameOfTestClasses = itemList.toArray(fullQualifiedNameOfTestClasses);

		Coverage coverage = EntryPoint.runCoverage(classpath, targetProjectClasses, fullQualifiedNameOfTestClasses);
		System.out.println(coverage);

	}

	@Test
	public void testLoadTest2() throws Exception {
	}

	@Test
	public void testLoadTest1() throws Exception {
		RtMain main1 = new RtMain();
		CommandSummary cs = command1();

		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();

	}

	private CommandSummary command1() throws Exception {

		String dep1 = new File("./examples/libs/junit-4.12.jar").getAbsolutePath();
		String dep2 = new File("./examples/libs/hamcrest-core-1.3.jar").getAbsolutePath();
		File out = new File(ConfigurationProperties.getProperty("workingDirectory"));

		String[] args = new String[] { "-dependencies", (dep1 + File.pathSeparator + dep2), "-javacompliancelevel", "7",

				"-out", out.getAbsolutePath(),

		};
		CommandSummary cs = new CommandSummary(args);
		cs.command.put("-loglevel", "DEBUG");
		cs.command.put("-location", new File("./examples/rt-project/").getAbsolutePath());
		cs.command.put("-mode", "rt");
		cs.command.put("-autoconfigure", "false");
		cs.command.put("-parameters", "skipanalysis:true");

		// cs.command.put("-analyzers", TestAnalyzerTest.class.getCanonicalName());
		// cs.command.put("-outputs", TestRtOutput.class.getCanonicalName());
		return cs;
	}

	private String toS(List<?> d) {
		String s = "";
		for (Object object : d) {
			s += object.toString() + File.pathSeparator;
		}

		return (s.length() > 0) ? s.substring(0, s.length() - 1) : s;
	}
}
