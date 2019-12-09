package fr.inria.jtanre.rt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.martiansoftware.jsap.JSAPException;

import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
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
import fr.inria.jtanre.rt.elements.AsAssertion;
import fr.inria.jtanre.rt.out.JSonResultOriginal;
import fr.inria.jtanre.rt.processors.AnyStatementExecuted;
import fr.inria.jtanre.rt.processors.AssertionProcessor;
import fr.inria.jtanre.rt.processors.AssumeProcessor;
import fr.inria.jtanre.rt.processors.ControlFlowProcessor;
import fr.inria.jtanre.rt.processors.ElementProcessor;
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
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.LineFilter;
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

	List<ElementProcessor<?, ?>> elementProcessor = new ArrayList<>();

	public RtEngine(MutationSupporter mutatorExecutor, ProjectRepairFacade projFacade) throws JSAPException {
		super(mutatorExecutor, projFacade);

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
		ConfigurationProperties.setProperty("maxsuspcandidates", "100000000");

		elementProcessor.add(new AssertionProcessor());
		elementProcessor.add(new HelperCallProcessor());
		elementProcessor.add(new HelperAssertionProcessor());
		elementProcessor.add(new AssumeProcessor());

		elementProcessor.add(new ExpectedExceptionAnnotatedProcessor());
		elementProcessor.add(new ExpectedExceptionProcessor());
		elementProcessor.add(new FailProcessor());

		elementProcessor.add(new MissedFailProcessor());
		elementProcessor.add(new OtherInvocationsProcessor());
		elementProcessor.add(new RedundantAssertionProcessor());
		elementProcessor.add(new SkipProcessor());
		elementProcessor.add(new SmokeProcessor());
		elementProcessor.add(new ControlFlowProcessor());
		elementProcessor.add(new AnyStatementExecuted());

	}

	Exception exceptionReceived = null;

	/**
	 * Execute the test cases from the application under analysis
	 * 
	 * @return
	 * @throws Exception
	 */
	public DynamicTestInformation runTests() throws Exception {

		this.loadFaultLocalization();

		this.loadValidator();

		List<SuspiciousCode> suspicious = this.calculateSuspicious();

		List<String> testToRun = projectFacade.getProperties().getRegressionTestCases();
		dynamicInfo = new DynamicTestInformation(suspicious, testToRun);
		return dynamicInfo;

	}

	/**
	 * Creates the model of the application under analysis
	 * 
	 * @return
	 * @throws Exception
	 */
	public ProgramModel<?> createModel() throws Exception {
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
	public void runTestAnalyzers(ProgramModel<?> model, DynamicTestInformation dynamicInfo) throws Exception {

		if (dynamicInfo.getTestExecuted().isEmpty()) {
			log.error("No test can be found");
			exceptionReceived = new Exception("No test can be found");
		} else {

			if (!ConfigurationProperties.getPropertyBool("skipanalysis")) {
				try {
					RuntimeInformation ri = computeDynamicInformation(dynamicInfo);
					analyzeTestSuiteExecution(ri);

				} catch (Exception e) {
					e.printStackTrace();
					log.error(e);
					exceptionReceived = e;
				}
			}
		}
	}

	public RuntimeInformation computeDynamicInformation() throws Exception {
		return computeDynamicInformation(this.dynamicInfo);
	}

	public RuntimeInformation computeDynamicInformation(DynamicTestInformation dynamicInfo) throws Exception {
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

	public void analyzeTestSuiteExecution(RuntimeInformation runtimeinfo) {
		// For each class name
		for (String aNameOfTestClass : runtimeinfo.allTestCases) {

			if (runtimeinfo.notexec.contains(aNameOfTestClass)) {
				log.debug("Ignoring -not executed line- test: " + aNameOfTestClass);
				continue;
			}
			log.info("*-*-*-*----- Analying TestClass: " + aNameOfTestClass);
			CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(aNameOfTestClass);
			if (aTestModelCtClass == null) {
				log.error("No class modeled for " + aNameOfTestClass);
				continue;
			}

			List<String> testMethodsFromClass = runtimeinfo.passingCoveredTestCaseFromClass.get(aNameOfTestClass);

			if (testMethodsFromClass == null || testMethodsFromClass.isEmpty()) {
				log.error("No method executed for class " + aNameOfTestClass);
				continue;
			}

			for (String aTestMethodFromClass : testMethodsFromClass) {

				TestIntermediateAnalysisResult resultTestCase = processTest(aTestMethodFromClass, aNameOfTestClass,
						aTestModelCtClass, runtimeinfo);
				if (resultTestCase != null) {
					resultByTest.add(resultTestCase);
				}
			}
		}
	}

	public TestIntermediateAnalysisResult processSingleTest(RuntimeInformation runtimeinfo, String aNameOfTestClass,
			String aTestMethodFromClass) {

		if (runtimeinfo.notexec.contains(aNameOfTestClass)) {
			log.debug("Ignoring -not executed line- test: " + aNameOfTestClass);
			return null;
		}
		log.info("*-*-*-*----- Analying TestClass: " + aNameOfTestClass);
		CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(aNameOfTestClass);
		if (aTestModelCtClass == null) {
			log.error("No class modeled for " + aNameOfTestClass);
			return null;
		}

		List<String> testMethodsFromClass = runtimeinfo.passingCoveredTestCaseFromClass.get(aNameOfTestClass);

		if (testMethodsFromClass == null || testMethodsFromClass.isEmpty()) {
			log.error("No method executed for class " + aNameOfTestClass);
			return null;
		}

		TestIntermediateAnalysisResult resultTestCase = processTest(aTestMethodFromClass, aNameOfTestClass,
				aTestModelCtClass, runtimeinfo);

		return resultTestCase;

	}

	@Override
	public void startEvolution() throws Exception {

	}

	@SuppressWarnings("unchecked")
	public TestIntermediateAnalysisResult processTest(String aTestMethodFromClass, String aNameOfTestClass,
			CtClass aTestModelCtClass, RuntimeInformation runtimeinfo) {
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
		List<CtStatement> allStmtsFromClass = testMethodModel.getElements(new LineFilter());

		for (ElementProcessor elementProcessor : this.elementProcessor) {

			List<?> retrievedElements = elementProcessor.findElements(partialStaticResults, allStmtsFromClass,
					testMethodModel, allClasses);
			partialStaticResults.put(elementProcessor.getClass(), retrievedElements);
		}

		// CLassification
		for (ElementProcessor elementProcessor : this.elementProcessor) {

			List<?> retrievedElements = partialStaticResults.get(elementProcessor.getClass().getSimpleName());

			Classification<?> classif = elementProcessor.classifyElements(partialDynamicResults, aTestModelCtClass,
					testMethodModel, runtimeinfo.mapCacheSuspicious, aTestModelCtClass, retrievedElements);
			// if (classif != null) {
			partialDynamicResults.put(elementProcessor.getClass().getSimpleName(), classif);
			// }

		}
		/// Labelling

		// CLassification
		for (ElementProcessor elementProcessor : this.elementProcessor) {

			List<?> retrievedElements = partialStaticResults.get(elementProcessor.getClass());
			Classification<?> classif = partialDynamicResults.get(elementProcessor.getClass());

			elementProcessor.labelTest(resultTestCase, retrievedElements, null, partialStaticResults,
					partialDynamicResults);

			elementProcessor.refactor(resultTestCase, retrievedElements, null, partialStaticResults,
					partialDynamicResults);

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

		super.atEnd();
		JSonResultOriginal jsoncoverted = new JSonResultOriginal();
		JsonObject json = null;
		if (exceptionReceived == null) {
			json = jsoncoverted.toJson(ConfigurationProperties.getProperty("id"), this.projectFacade,
					this.resultByTest);

		} else {
			json = jsoncoverted.toJsonError(ConfigurationProperties.getProperty("id"), this.projectFacade,
					this.exceptionReceived);
		}

		System.out.println("rtjsonoutput: " + json);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String ppjson = gson.toJson(json);

		String out = (ConfigurationProperties.getProperty("out") != null) ? ConfigurationProperties.getProperty("out")
				: ConfigurationProperties.getProperty("workingDirectory");
		String outpath = out + File.separator + "rt_" + ConfigurationProperties.getProperty("id") + ".json";
		log.info("Saving json at \n" + outpath);
		try {
			FileWriter fw = new FileWriter(new File(outpath));
			fw.write(ppjson);
			fw.flush();
			fw.close();
		} catch (IOException e) {

			e.printStackTrace();
			log.error(e);
		}
	}

	protected DynamicTestInformation dynamicInfo = null;
	protected ProgramModel<?> model = null;

	public DynamicTestInformation getDynamicInfo() {
		return dynamicInfo;
	}

	public void setDynamicInfo(DynamicTestInformation dynamicInfo) {
		this.dynamicInfo = dynamicInfo;
	}

	public ProgramModel<?> getModel() {
		return model;
	}

	public void setModel(ProgramModel<?> model) {
		this.model = model;
	}

}
