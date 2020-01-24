package fr.inria.jtanre.rt;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.faultlocalization.FaultLocalizationResult;
import fr.inria.astor.core.faultlocalization.FaultLocalizationStrategy;
import fr.inria.astor.core.faultlocalization.cocospoon.CocoFaultLocalization;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalization;
import fr.inria.astor.core.faultlocalization.gzoltar.TestCaseResult;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.astor.core.setup.ConfigurationProperties;
import fr.inria.astor.core.setup.ProjectRepairFacade;
import fr.inria.astor.core.solutionsearch.AstorCoreEngine;
import fr.inria.astor.util.MapList;
import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.core.DynamicTestInformation;
import fr.inria.jtanre.rt.core.ProgramModel;
import fr.inria.jtanre.rt.core.ResultMap;
import fr.inria.jtanre.rt.core.RuntimeInformation;
import fr.inria.jtanre.rt.core.SpoonProgramModel;
import fr.inria.jtanre.rt.core.TestIntermediateAnalysisResult;
import fr.inria.jtanre.rt.core.dynamic.TestCaseExecutor;
import fr.inria.jtanre.rt.core.dynamic.TestExecutorWrapperFaultLocalization;
import fr.inria.jtanre.rt.elements.AsAssertion;
import fr.inria.jtanre.rt.out.JSonResultOriginal;
import fr.inria.jtanre.rt.out.PrinterOutResultOriginal;
import fr.inria.jtanre.rt.out.RefactorOutput;
import fr.inria.jtanre.rt.out.RtOutput;
import fr.inria.jtanre.rt.processors.AnyStatementExecuted;
import fr.inria.jtanre.rt.processors.AssertionProcessor;
import fr.inria.jtanre.rt.processors.AssumeProcessor;
import fr.inria.jtanre.rt.processors.ControlFlowProcessor;
import fr.inria.jtanre.rt.processors.ExpectedExceptionAnnotatedProcessor;
import fr.inria.jtanre.rt.processors.ExpectedExceptionProcessor;
import fr.inria.jtanre.rt.processors.FailProcessor;
import fr.inria.jtanre.rt.processors.HelperAssertionProcessor;
import fr.inria.jtanre.rt.processors.HelperCallProcessor;
import fr.inria.jtanre.rt.processors.MissedFailProcessor;
import fr.inria.jtanre.rt.processors.OtherInvocationsProcessor;
import fr.inria.jtanre.rt.processors.RedundantAssertionProcessor;
import fr.inria.jtanre.rt.processors.SkipProcessor;
import fr.inria.jtanre.rt.processors.SmokeProcessor;
import fr.inria.jtanre.rt.processors.TestAnalyzer;
import fr.inria.main.evolution.ExtensionPoints;
import fr.inria.main.evolution.PlugInLoader;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.declaration.CtClassImpl;

/**
 * 
 * @author Matias Martinez
 *
 */
public class RtEngine extends AstorCoreEngine {

	public List<TestIntermediateAnalysisResult> resultByTest = new ArrayList<>();

	List<String> namespace = Arrays.asList("org.assertj", "org.testng", "org.mockito", "org.spockframework",
			"org.junit", "cucumber", "org.jbehave");

	List<TestAnalyzer> testAnalyzers = new ArrayList<>();
	List<RtOutput> outputs = new ArrayList<>();

	TestCaseExecutor testExecutor = null;
	protected DynamicTestInformation dynamicInfo = null;
	protected ProgramModel model = null;

	Exception exceptionReceived = null;

	public RtEngine(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws Exception {
		super(mutatorExecutor, projFacade);

		setDefaultConfigurationProperties();

		loadTestCasesExecutorForDynamicAnalysis();

		loadDefaultAnalyzers();

		loadDefaulOutputs();

		loadCustomizeAnalyzers();

		loadCustomizeOutputs();

	}

	private void setDefaultConfigurationProperties() {
		ConfigurationProperties.setProperty("canhavezerosusp", "true");
		ConfigurationProperties.setProperty("includeTestInSusp", "true");
		ConfigurationProperties.setProperty("limitbysuspicious", "false");
		ConfigurationProperties.setProperty("maxsuspcandidates", "1");
		ConfigurationProperties.setProperty("regressionforfaultlocalization", "true");
		ConfigurationProperties.setProperty("considerzerovaluesusp", "true");
		ConfigurationProperties.setProperty("disablelog", "false");
		ConfigurationProperties.setProperty("onlympcovered", "false");
		ConfigurationProperties.setProperty("onlympfromtest", "false");
		ConfigurationProperties.setProperty("maxGeneration", "1");
		ConfigurationProperties.setProperty("maxmodificationpoints", "1000000000");
		ConfigurationProperties.setProperty("maxsuspcandidates", "1000000000");
		ConfigurationProperties.setProperty("loglevel", "INFO");
	}

	protected void loadDefaultAnalyzers() {
		testAnalyzers.add(new AssertionProcessor());
		testAnalyzers.add(new HelperCallProcessor());
		testAnalyzers.add(new HelperAssertionProcessor());
		testAnalyzers.add(new AssumeProcessor());

		testAnalyzers.add(new ExpectedExceptionAnnotatedProcessor());
		testAnalyzers.add(new ExpectedExceptionProcessor());
		testAnalyzers.add(new FailProcessor());

		testAnalyzers.add(new MissedFailProcessor());
		testAnalyzers.add(new OtherInvocationsProcessor());
		testAnalyzers.add(new RedundantAssertionProcessor());
		testAnalyzers.add(new SkipProcessor());
		testAnalyzers.add(new SmokeProcessor());
		testAnalyzers.add(new ControlFlowProcessor());
		testAnalyzers.add(new AnyStatementExecuted());
	}

	protected void loadDefaulOutputs() {
		outputs.add(new JSonResultOriginal());
		outputs.add(new RefactorOutput());
		outputs.add(new PrinterOutResultOriginal());
	}

	protected void loadCustomizeOutputs() throws Exception {
		String outputsP = ConfigurationProperties.getProperty("outputs");
		if (outputsP != null && !outputsP.isEmpty()) {
			String[] operators = outputsP.split(File.pathSeparator);
			for (String op : operators) {
				RtOutput aop = (RtOutput) PlugInLoader.loadPlugin(op, RtOutput.class);
				if (aop != null)
					this.outputs.add(aop);
			}
		}
	}

	protected void loadCustomizeAnalyzers() throws Exception {
		String analyzers = ConfigurationProperties.getProperty("analyzers");
		if (analyzers != null && !analyzers.isEmpty()) {
			String[] operators = analyzers.split(File.pathSeparator);
			for (String op : operators) {
				TestAnalyzer aop = (TestAnalyzer) PlugInLoader.loadPlugin(op, TestAnalyzer.class);
				if (aop != null)
					testAnalyzers.add(aop);
			}
		}
	}

	/**
	 * Execute the test cases from the application under analysis
	 * 
	 * @return
	 * @throws Exception
	 */
	public DynamicTestInformation runTests() throws Exception {

		List<String> testsToRun = this.testExecutor.findTestCasesToExecute(projectFacade);

		Set<String> duplicates = testsToRun.stream().filter(i -> Collections.frequency(testsToRun, i) > 1)
				.collect(Collectors.toSet());

		// TODO: remove
		projectFacade.getProperties().setRegressionCases(testsToRun);

		FaultLocalizationResult result = this.testExecutor.runTests(getProjectFacade(), testsToRun);

		List<SuspiciousCode> suspicious = result.getCandidates();

		dynamicInfo = new DynamicTestInformation(suspicious, testsToRun);
		return dynamicInfo;

	}

	protected void loadTestCasesExecutorForDynamicAnalysis() throws Exception {

		String testExecutorStg = ConfigurationProperties.getProperty("testexecutor");
		if (testExecutorStg == null || testExecutorStg.isEmpty()) {

			// TODO:
			testExecutorStg = ConfigurationProperties.getProperty("faultlocalization").toLowerCase();

			// testExecutorStg = FLWrapper.class.getCanonicalName();

		}

		try {
			// Try to load
			testExecutor = (TestCaseExecutor) PlugInLoader.loadPlugin(testExecutorStg, TestCaseExecutor.class);

		} catch (Exception e) {
			// nothing
		}

		if (testExecutor == null) {
			testExecutorStg = ConfigurationProperties.getProperty("faultlocalization").toLowerCase();

			testExecutor = loadFaultLocalization(testExecutorStg);

		}

		//
		if (testExecutor == null) {
			//
			throw new IllegalAccessException("Imposible to configure test executor: " + testExecutorStg);
		}

	}

	protected TestCaseExecutor loadFaultLocalization(String testExecutor) throws Exception {

		FaultLocalizationStrategy fl = null;
		if (testExecutor.equals("gzoltar")) {
			fl = (new GZoltarFaultLocalization());
		} else if (testExecutor.equals("cocospoon")) {
			fl = new CocoFaultLocalization();
		} else {
			try {
				fl = (FaultLocalizationStrategy) PlugInLoader.loadPlugin(testExecutor,
						ExtensionPoints.FAULT_LOCALIZATION._class);

			} catch (Exception e) {
				// The argument is not FL
				return null;
			}
		}

		if (fl != null) {
			TestCaseExecutor tce = new TestExecutorWrapperFaultLocalization(fl);
			return tce;
		}
		return null;
	}

	/**
	 * Creates the model of the application under analysis
	 * 
	 * @return
	 * @throws Exception
	 */
	public ProgramModel createModel() throws Exception {
		this.loadCompiler();
		this.initModel();
		this.model = new SpoonProgramModel();
		return model;
	}

	/**
	 * 
	 * @param model
	 * @param dynamicInfo
	 * @throws Exception
	 */
	public void runTestAnalyzers(ProgramModel model, DynamicTestInformation dynamicInfo) throws Exception {

		if (dynamicInfo.getTestExecuted().isEmpty()) {
			log.error("No test can be found");
			exceptionReceived = new Exception("No test can be found");
		} else {

			try {
				RuntimeInformation ri = computeDynamicInformation(model, dynamicInfo);
				analyzeTestSuiteExecution(model, ri);

			} catch (Exception e) {
				e.printStackTrace();
				log.error(e);
				exceptionReceived = e;
			}

		}
	}

	public RuntimeInformation computeDynamicInformation() throws Exception {
		return computeDynamicInformation(this.model, this.dynamicInfo);
	}

	public RuntimeInformation computeDynamicInformation(ProgramModel model2, DynamicTestInformation dynamicInfo)
			throws Exception {
		List<String> allTestCases = new ArrayList();

		List<String> allTestCasesWithoutParent = this.getProjectFacade().getProperties().getRegressionTestCases();

		for (String tc : allTestCasesWithoutParent) {

			allTestCases.add(tc);
			CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(tc);

			if (aTestModelCtClass == null) {
				log.error("Error: Not ct model for class " + tc);
				continue;
			}

			CtTypeReference supclass = aTestModelCtClass.getSuperclass();
			while (supclass != null) {
				if (!allTestCases.contains(supclass.getQualifiedName()))
					allTestCases.add(supclass.getQualifiedName());
				else {
					// already analyzed
					break;
				}
				supclass = supclass.getSuperclass();
			}

		}

		if (allTestCases.isEmpty()) {
			log.error("No test to execute");
			throw new Exception("No test to execute");

		}

		log.debug("# Test cases: " + allTestCases.size());

		// key is test class, values are method (cases)
		MapList<String, String> passingCoveredTestCaseFromClass = new MapList<>();

		for (SuspiciousCode executed : dynamicInfo.getAllExecutedStatements()) {

			for (TestCaseResult tcr : executed.getCoveredByTests()) {
				String testCaseName = formatTestCaseName(tcr.getTestCaseName());
				if (tcr.isCorrect() && (
				// Test class not analyzed
				!passingCoveredTestCaseFromClass.containsKey(tcr.getTestCaseClass())
						// test method not analyzed

						|| !passingCoveredTestCaseFromClass.get(tcr.getTestCaseClass()) // executed.getClassName()
								.contains(testCaseName)))
					passingCoveredTestCaseFromClass.add(tcr.getTestCaseClass(), testCaseName);
			}
		}

		// Lines of code covered grouped by test. The key is the test class name
		MapList<String, Integer> mapLinesCovered = new MapList<>();
		Map<String, SuspiciousCode> mapCacheSuspicious = new HashMap<>();

		for (SuspiciousCode executedStatement : dynamicInfo.getAllExecutedStatements()) {
			mapLinesCovered.add(executedStatement.getClassName(), executedStatement.getLineNumber());
			mapCacheSuspicious.put(executedStatement.getClassName() + executedStatement.getLineNumber(),
					executedStatement);
		}

		// Check results
		List<String> notexec = new ArrayList<>();
		for (String test : allTestCases) {

			if (!mapLinesCovered.containsKey(test)) {
				log.error("Test " + test + " not executed");
				notexec.add(test);
			}
		}
		if (!notexec.isEmpty()) {
			log.error("nr test not ex " + notexec.size());
		}

		resultByTest = new ArrayList<>();

		log.info("End processing RT");

		RuntimeInformation runtimeinfo = new RuntimeInformation(allTestCases, allTestCasesWithoutParent,
				mapLinesCovered, mapCacheSuspicious, passingCoveredTestCaseFromClass, notexec);
		return runtimeinfo;

	}

	public void analyzeTestSuiteExecution(ProgramModel model, RuntimeInformation runtimeinfo) {
		// For each class name
		for (String aNameOfTestClass : runtimeinfo.allTestCases) {

			if (runtimeinfo.notexec.contains(aNameOfTestClass)) {
				log.debug("Ignoring -not executed line- test: " + aNameOfTestClass);
				continue;
			}
			log.info("*-*-*-*----- Analying TestClass: " + aNameOfTestClass);

			if (!model.existsClass(aNameOfTestClass)) {
				log.error("No class modeled for " + aNameOfTestClass);
				continue;
			}
			CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(aNameOfTestClass);
			// Object clazz = model.getClass(aNameOfTestClass);

			List<String> testMethodsFromClass = runtimeinfo.passingCoveredTestCaseFromClass.get(aNameOfTestClass);

			if (testMethodsFromClass == null || testMethodsFromClass.isEmpty()) {
				log.error("No method executed for class " + aNameOfTestClass);
				continue;
			}

			for (String aTestMethodFromClass : testMethodsFromClass) {

				TestIntermediateAnalysisResult resultTestCase = processTest(model, aTestMethodFromClass,
						aNameOfTestClass, aTestModelCtClass, runtimeinfo);
				if (resultTestCase != null) {
					resultByTest.add(resultTestCase);
				}
			}
		}
	}

	public TestIntermediateAnalysisResult processSingleTest(RuntimeInformation runtimeinfo, String aNameOfTestClass,
			String aTestMethodFromClass) {

		return processSingleTest(getModel(), runtimeinfo, aNameOfTestClass, aTestMethodFromClass);
	}

	public TestIntermediateAnalysisResult processSingleTest(ProgramModel model, RuntimeInformation runtimeinfo,
			String aNameOfTestClass, String aTestMethodFromClass) {

		if (runtimeinfo.notexec.contains(aNameOfTestClass)) {
			log.debug("Ignoring -not executed line- test: " + aNameOfTestClass);
			return null;
		}
		log.info("*-*-*-*----- Analying TestClass: " + aNameOfTestClass);
		CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(aNameOfTestClass);
		// model

		if (aTestModelCtClass == null) {
			log.error("No class modeled for " + aNameOfTestClass);
			return null;
		}

		List<String> testMethodsFromClass = runtimeinfo.passingCoveredTestCaseFromClass.get(aNameOfTestClass);

		if (testMethodsFromClass == null || testMethodsFromClass.isEmpty()) {
			log.error("No method executed for class " + aNameOfTestClass);
			return null;
		}

		TestIntermediateAnalysisResult resultTestCase = processTest(model, aTestMethodFromClass, aNameOfTestClass,
				aTestModelCtClass, runtimeinfo);

		this.resultByTest.add(resultTestCase);

		return resultTestCase;

	}

	@Override
	public void startEvolution() throws Exception {

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TestIntermediateAnalysisResult processTest(ProgramModel model, String aTestMethodFromClass,
			String aNameOfTestClass, CtClass aTestModelCtClass, RuntimeInformation runtimeinfo) {
		log.info("**** Analying TestMethod: " + aTestMethodFromClass);

		ResultMap<List<?>> partialStaticResults = new ResultMap<List<?>>();
		ResultMap<Classification<?>> partialDynamicResults = new ResultMap<Classification<?>>();

		List<CtClass> allClasses = getClasses(aTestModelCtClass);

		Optional<CtExecutableReference<?>> testMethodOp = aTestModelCtClass.getAllExecutables().stream()
				.filter(e -> e.getSimpleName().equals(aTestMethodFromClass)).findFirst();
		if (!testMethodOp.isPresent()) {
			log.error("Problem " + aTestMethodFromClass + " not found in class " + aNameOfTestClass);
			return null;
		}
		CtExecutable testMethodModel = testMethodOp.get().getDeclaration();

		TestIntermediateAnalysisResult resultTestCase = new TestIntermediateAnalysisResult(aNameOfTestClass,
				aTestMethodFromClass, testMethodModel, partialStaticResults, partialDynamicResults);

		if (!runtimeinfo.passingCoveredTestCaseFromClass.containsKey(aNameOfTestClass)
				|| !(runtimeinfo.passingCoveredTestCaseFromClass.get(aNameOfTestClass)
						.contains(aTestMethodFromClass))) {

			log.debug("ignoring test " + aTestMethodFromClass + " from class " + aNameOfTestClass);
			return null;

		}
		// get all statements
		// List<CtStatement> allStmtsFromClass = testMethodModel.getElements(new
		// LineFilter());

		List<?> allStmtsFromClass = model.getStatementsFromMethod(testMethodModel);// testMethodModel.getElements(new
																					// LineFilter());

		for (TestAnalyzer elementProcessor : this.testAnalyzers) {

			List<?> retrievedElements = elementProcessor.findElements(partialStaticResults, allStmtsFromClass,
					testMethodModel, allClasses);
			partialStaticResults.put(elementProcessor.getClass(), retrievedElements);
		}

		// CLassification
		for (TestAnalyzer elementProcessor : this.testAnalyzers) {

			List<?> retrievedElements = partialStaticResults.get(elementProcessor.getClass().getSimpleName());

			Classification<?> classif = elementProcessor.classifyElements(partialDynamicResults, aTestModelCtClass,
					testMethodModel, runtimeinfo.mapCacheSuspicious, aTestModelCtClass, retrievedElements);

			partialDynamicResults.put(elementProcessor.getClass().getSimpleName(), classif);

		}
		/// Labelling

		// CLassification
		for (TestAnalyzer elementProcessor : this.testAnalyzers) {

			List<?> retrievedElements = partialStaticResults.get(elementProcessor.getClass());
			Classification<?> classif = partialDynamicResults.get(elementProcessor.getClass());

			elementProcessor.labelTest(resultTestCase, retrievedElements, null, partialStaticResults,
					partialDynamicResults);

			// Refactor:
			List<ProgramVariant> variantsRefactors = elementProcessor.refactor(model, aTestModelCtClass, resultTestCase,
					retrievedElements, null, partialStaticResults, partialDynamicResults);

			if (variantsRefactors != null) {
				for (ProgramVariant programVariant : variantsRefactors) {
					programVariant.setId(this.solutions.size());
					try {
						this.solutions.add(programVariant);
					} catch (Exception e1) {
						e1.printStackTrace();
						log.error("Problems when saving variant " + programVariant.getId());
					}
				}
			}

		}

		return resultTestCase;

	}

	/**
	 * Marks those assertions inside a try-catch
	 * 
	 * @param executedLines
	 * @param parentClass
	 * @param aMissAssertion
	 */
	public void analyzeMissingAssertionInsideTryCatch(MapList<String, Integer> executedLines, CtClass parentClass,
			AsAssertion aMissAssertion) {

	}

	private String formatTestCaseName(String testCaseName) {
		int i = testCaseName.indexOf("[");
		if (i > 0)
			return testCaseName.substring(0, i);

		else
			return testCaseName;
	}

	public List<CtClass> getClasses(CtClass aTestModelCtClass) {
		List<CtClass> allClasses = new ArrayList();
		allClasses.add(aTestModelCtClass);

		CtTypeReference superclass = ((CtClassImpl) aTestModelCtClass).getSuperclass();
		if (superclass == null)
			return allClasses;
		CtType td = superclass.getTypeDeclaration();
		if (td instanceof CtClassImpl) {
			allClasses.addAll(getClasses((CtClassImpl) td));
		}
		return allClasses;
	}

	public List<TestIntermediateAnalysisResult> getResultByTest() {
		return resultByTest;
	}

	@Override
	public void atEnd() {

		String out = (ConfigurationProperties.getProperty("out") != null) ? ConfigurationProperties.getProperty("out")
				: ConfigurationProperties.getProperty("workingDirectory");

		for (RtOutput rtOutput : outputs) {
			try {
				rtOutput.generateOutput(this, ConfigurationProperties.getProperty("id"), projectFacade, resultByTest,
						this.solutions, exceptionReceived, out);
			} catch (Exception e) {
				System.out.println("Problems printing " + rtOutput.getClass().getCanonicalName());
				e.printStackTrace();
			}
		}

	}

	public DynamicTestInformation getDynamicInfo() {
		return dynamicInfo;
	}

	public void setDynamicInfo(DynamicTestInformation dynamicInfo) {
		this.dynamicInfo = dynamicInfo;
	}

	public ProgramModel getModel() {
		return model;
	}

	public void setModel(ProgramModel model) {
		this.model = model;
	}

	public List<RtOutput> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<RtOutput> outputs) {
		this.outputs = outputs;
	}

	public List<TestAnalyzer> getTestAnalyzers() {
		return testAnalyzers;
	}

	public void setTestAnalyzers(List<TestAnalyzer> testAnalyzers) {
		this.testAnalyzers = testAnalyzers;
	}

	public TestCaseExecutor getTestExecutor() {
		return testExecutor;
	}

	public void setTestExecutor(TestCaseExecutor testExecutor) {
		this.testExecutor = testExecutor;
	}

}
