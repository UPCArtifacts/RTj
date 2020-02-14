
package fr.inria.jtanre;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.Test;

import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalization;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.jtanre.extension4test.TestAnalyzerTest;
import fr.inria.jtanre.extension4test.TestExecutor4Test;
import fr.inria.jtanre.extension4test.TestRtOutput;
import fr.inria.jtanre.rt.RtEngine;
import fr.inria.jtanre.rt.RtMain;
import fr.inria.jtanre.rt.core.RuntimeInformation;
import fr.inria.jtanre.rt.core.TestIntermediateAnalysisResult;
import fr.inria.jtanre.rt.core.coverage.CoverageResult;
import fr.inria.jtanre.rt.core.coverage.FLWrapper;
import fr.inria.jtanre.rt.core.dynamic.TestExecutorWrapperFaultLocalization;
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

	@Test
	public void testLoadTestExecutor() throws Exception {
		RtMain main1 = new RtMain();
		CommandSummary cs = command1();

		cs.command.put("-testexecutor", "gzoltar");

		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();

		assertTrue(etEn.getTestExecutor() instanceof TestExecutorWrapperFaultLocalization);
		assertNull(etEn.getFaultLocalization());

		assertTrue(((TestExecutorWrapperFaultLocalization) etEn.getTestExecutor())
				.getFaultLocalization() instanceof GZoltarFaultLocalization);

		RuntimeInformation dynInf = etEn.computeDynamicInformation();

		//// 4th case
		TestIntermediateAnalysisResult rottenTest4 = etEn.processSingleTest(dynInf,
				"RottenTestsFinder.FakePaperTests.RTFRow29Context", "test5");

		assertNotNull(rottenTest4);
	}

	@Test
	public void testLoadTestExecutorFail() throws Exception {
		RtMain main1 = new RtMain();
		CommandSummary cs = command1();

		///
		try {
			cs.command.put("-testexecutor", "null");

			main1.execute(cs.flat());
			fail();
		} catch (Exception e) {
			// ok, must fail
		}

	}

	@Test
	public void testLoadTestExecutorCustom() throws Exception {
		RtMain main1 = new RtMain();
		CommandSummary cs = command1();

		cs.command.put("-testexecutor", TestExecutor4Test.class.getCanonicalName());

		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();

		assertTrue(etEn.getTestExecutor() instanceof TestExecutor4Test);
		assertNull(etEn.getFaultLocalization());

	}

	private RtEngine detectRtWithAnalyzer() throws Exception {

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
		RtMain main1 = new RtMain();

		main1.execute(cs.flat());
		RtEngine etEn = (RtEngine) main1.getEngine();
		return etEn;
	}

	private CommandSummary command1() throws Exception {

		String dep1 = new File("./examples/libs/junit-4.12.jar").getAbsolutePath();
		String dep2 = new File("./examples/libs/hamcrest-core-1.3.jar").getAbsolutePath();

		String dep3 = new File("./examples/libs/gson-2.8.2.jar").getAbsolutePath();
		String dep4 = new File("./examples/libs/opencsv-3.3.jar").getAbsolutePath();

		File out = new File(ConfigurationProperties.getProperty("workingDirectory"));

		String[] args = new String[] { "-dependencies",
				(dep1 + File.pathSeparator + dep2 + File.pathSeparator + dep3 + File.pathSeparator + dep4),
				"-javacompliancelevel", "7",

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

	@Test
	public void testLoadTestFaultLocalizationNewImplementation() throws Exception {
		RtMain main1 = new RtMain();
		CommandSummary cs = command1();

		cs.command.put("-testexecutor", FLWrapper.class.getCanonicalName());

		main1.execute(cs.flat());

		RtEngine etEn = (RtEngine) main1.getEngine();

		assertTrue(etEn.getTestExecutor() instanceof FLWrapper);

		FLWrapper fl = (FLWrapper) etEn.getTestExecutor();

		FaultLocalizationResult result = fl.runTests(etEn.getProjectFacade(), null);

		List<SuspiciousCode> covered = result.getCandidates();
		System.out.println("--> covered: " + covered);
		for (SuspiciousCode suspiciousCode : covered) {
			System.out.println(
					"-> " + suspiciousCode.getClassName() + " line " + suspiciousCode.getLineNumber() + " covered by: ("
							+ suspiciousCode.getCoveredByTests().size() + ") " + suspiciousCode.getCoveredByTests());
		}

		assertTrue(result instanceof CoverageResult);

		CoverageResult cr = (CoverageResult) result;

		String linePerson18 = cr.getMatrix().getLineKey("testcore/Person", 18);

		assertNotNull(linePerson18);
		assertTrue(linePerson18.length() > 0);

		assertTrue(cr.getMatrix().getResultExecution().containsKey(linePerson18));

		Set<Integer> executed = cr.getMatrix().getResultExecution().get(linePerson18);

		assertTrue(executed.size() == 2);

		// Test with dependency that is in the classpath
		String lineT3418 = cr.getMatrix().getLineKey("RottenTestsFinder/FakePaperTests/RTFRow34Dependency", 18);

		try {
			Class c = Class.forName("com.google.gson.JsonObject");
			assertNotNull(c);
		} catch (Exception e) {
			fail("class must  be in classpath");
		}

		assertNotNull(lineT3418);
		assertTrue(lineT3418.length() > 0);

		assertTrue(cr.getMatrix().getResultExecution().containsKey(lineT3418));

		Set<Integer> executed3418 = cr.getMatrix().getResultExecution().get(lineT3418);

		assertTrue(executed3418.size() == 1);

		// Not covered

		String lineT3417 = cr.getMatrix().getLineKey("RottenTestsFinder/FakePaperTests/RTFRow34Dependency", 17);

		assertNotNull(lineT3417);
		assertTrue(lineT3417.length() > 0);

		assertFalse(cr.getMatrix().getResultExecution().containsKey(lineT3417));

		// Set<Integer> executed3415 =
		// cr.getMatrix().getResultExecution().get(lineT3415);

		// assertTrue(executed3415.size() == 0);

		// Dependency not in the classpath:

		// check if the class is in the classpath
		try {
			Class c = Class.forName("com.opencsv.CSVWriter");
			fail("class must not be in classpath");
		} catch (Exception e) {
			// must fail
		}

		String lineT3428 = cr.getMatrix().getLineKey("RottenTestsFinder/FakePaperTests/RTFRow34Dependency", 28);

		assertNotNull(lineT3428);
		assertTrue(lineT3428.length() > 0);

		System.out.println(cr.getMatrix().getResultExecution());
		assertTrue(cr.getMatrix().getResultExecution().containsKey(lineT3428));

		Set<Integer> executed3428 = cr.getMatrix().getResultExecution().get(lineT3428);

		assertEquals(1, executed3428.size());
		// The test with the dependency does not fail
		assertTrue(cr.getMatrix().getTestResult().get(executed3428.stream().findFirst().get()));

	}

}
