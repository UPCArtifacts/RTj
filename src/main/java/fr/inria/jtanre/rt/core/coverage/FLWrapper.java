package fr.inria.jtanre.rt.core.coverage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.TestCaseResult;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.jtanre.rt.core.coverage.test_framework.TestDetector;
import fr.inria.jtanre.rt.core.coverage.test_framework.TestTuple;
import fr.inria.jtanre.rt.core.dynamic.TestCaseExecutor;
import fr.spoonlabs.spfl.CoverageCalculator;
import fr.spoonlabs.spfl.FLCalculation;
import fr.spoonlabs.spfl.MatrixCoverage;
import fr.spoonlabs.spfl.OchiaiFormula;

/**
 * 
 * @author Matias Martinez
 *
 */
public class FLWrapper implements TestCaseExecutor {
	CoverageCalculator coverageCalculator = new CoverageCalculator();
	TestDetector testDetector = new TestDetector();

	public FLWrapper() {
	}

	@Override
	public FaultLocalizationResult runTests(ProjectRepairFacade projectToRepair, List<String> testClassesToRun)
			throws Exception {

		List<TestTuple> tests = testDetector.findTest(MutationSupporter.getFactory());
		if (testClassesToRun != null && testClassesToRun.size() > 0) {
			tests = tests.stream().filter(e -> testClassesToRun.contains(e.testClassToBeAmplified))
					.collect(Collectors.toList());

		}

		MatrixCoverage matrix = coverageCalculator.getCoverageMatrix(
				projectToRepair.getProperties().getDependenciesString(),
				// projectToRepair.getProperties().getOriginalAppBinDir().get(0),
				projectToRepair.getOutDirWithPrefix(ProgramVariant.DEFAULT_ORIGINAL_VARIANT).replace("//", "/"),
				// projectToRepair.getProperties().getOriginalTestBinDir().get(0)
				projectToRepair.getOutDirWithPrefix(ProgramVariant.DEFAULT_ORIGINAL_VARIANT).replace("//", "/"), tests);

		FLCalculation flcalc = new FLCalculation();

		Map<String, Double> susp = flcalc.calculateSuspicious(matrix, new OchiaiFormula(), true);

		List<SuspiciousCode> candidates = new ArrayList<>();

		for (String line : susp.keySet()) {
			double suspvalue = susp.get(line);

			String[] sp = line.split(MatrixCoverage.JOIN);
			String className = sp[0].replace(File.separator, ".");
			Integer lineNumber = new Integer(sp[1]);

			SuspiciousCode sc = new SuspiciousCode(className, null, lineNumber, suspvalue, null);
			candidates.add(sc);

			Set<Integer> testExecuted = matrix.getResultExecution().get(line);

			List<TestCaseResult> testsNames = new ArrayList();
			for (Integer itest : testExecuted) {
				String testName = matrix.getTests().get(itest);
				String[] ts = testName.split(MatrixCoverage.JOIN);
				boolean testResult = matrix.getTestResult().get(itest);
				String testNameSingle = ts[1];
				String testClass = ts[0];
				TestCaseResult trc = new TestCaseResult(testName, testNameSingle, testClass, testResult);

				testsNames.add(trc);
			}
			sc.setCoveredByTests(testsNames);

		}

		CoverageResult result = new CoverageResult(candidates, matrix.getFailingTestCases(), matrix, susp);
		return result;
	}

	@Override
	public List<String> findTestCasesToExecute(ProjectRepairFacade projectFacade) {
		List<TestTuple> tests = testDetector.findTest(MutationSupporter.getFactory());

		return tests.parallelStream().map(e -> e.testClassToBeAmplified).collect(Collectors.toList());
	}

}
