
package fr.inria.jtanre;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.Test;

import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.jtanre.extension4test.TestAnalyzerTest;
import fr.inria.jtanre.extension4test.TestRtOutput;
import fr.inria.jtanre.rt.RtEngine;
import fr.inria.jtanre.rt.RtMain;
import fr.inria.jtanre.rt.core.TestIntermediateAnalysisResult;
import fr.inria.main.CommandSummary;

/**
 * 
 * @author Matias Martinez
 *
 */
public class RtExtensionTest {

	public static Logger log = Logger.getLogger(Thread.currentThread().getName());

	@Test
	public void testLoadTestAnalyzer() throws Exception {
		RtEngine etEn = detectRtWithAnalyzer();

		List<TestIntermediateAnalysisResult> resultByTest = etEn.getResultByTest();

		assertNotNull(resultByTest);
		List<TestIntermediateAnalysisResult> tc = resultByTest.stream()
				.filter(e -> e.getNameOfTestClass()
						.equals("RTFRow01HelperExecutedAssertionExecutedContainsHelperContainsAssertion"))
				.collect(Collectors.toList());

		assertTrue(etEn.getTestAnalyzers().stream().filter(e -> e instanceof TestAnalyzerTest).findAny().isPresent());

		assertTrue(tc.isEmpty());
	}

	@Test
	public void testLoadOutput() throws Exception {
		RtEngine etEn = detectRtWithAnalyzer();

		List<TestIntermediateAnalysisResult> resultByTest = etEn.getResultByTest();

		assertNotNull(resultByTest);
		List<TestIntermediateAnalysisResult> tc = resultByTest.stream()
				.filter(e -> e.getNameOfTestClass()
						.equals("RTFRow01HelperExecutedAssertionExecutedContainsHelperContainsAssertion"))
				.collect(Collectors.toList());

		assertTrue(etEn.getOutputs().stream().filter(e -> e instanceof TestRtOutput).findAny().isPresent());

		assertTrue(tc.isEmpty());
	}

	private RtEngine detectRtWithAnalyzer() throws Exception {
		RtMain main1 = new RtMain();

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
		cs.command.put("-analyzers", TestAnalyzerTest.class.getCanonicalName());
		cs.command.put("-outputs", TestRtOutput.class.getCanonicalName());

		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();
		return etEn;
	}

}
