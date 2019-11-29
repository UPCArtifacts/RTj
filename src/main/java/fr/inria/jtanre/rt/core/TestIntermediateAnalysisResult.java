package fr.inria.jtanre.rt.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.astor.approaches.extensions.rt.elements.AsAssertion;
import fr.inria.astor.approaches.extensions.rt.elements.Assume;
import fr.inria.astor.approaches.extensions.rt.elements.Helper;
import fr.inria.astor.approaches.extensions.rt.elements.Skip;
import fr.inria.astor.approaches.extensions.rt.elements.TestElement;
import fr.inria.astor.approaches.extensions.rt.processors.AssertionProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.AssumeProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.ControlFlowProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.ExpectedExceptionAnnotatedProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.ExpectedExceptionProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.FailProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.HelperAssertionProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.HelperCallProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.MissedFailProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.OtherInvocationsProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.RedundantAssertionProcessor;
import fr.inria.astor.approaches.extensions.rt.processors.SkipProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Classification of a particular test
 * 
 * @author Matias Martinez
 *
 */
public class TestIntermediateAnalysisResult extends GenericTestAnalysisResults {
	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	String nameOfTestClass;
	String testMethodFromClass;
	private CtExecutable testMethodModel;
	private ResultMap<List<?>> staticAnalysis = null;
	private ResultMap<Classification<?>> dynamicAnalysis = null;

	public TestIntermediateAnalysisResult(String nameOfTestClass, String testMethodFromClass,
			CtExecutable testMethodModel, ResultMap<List<?>> staticAnalysis,
			ResultMap<Classification<?>> dynamicAnalysis) {
		super();
		this.nameOfTestClass = nameOfTestClass;
		this.testMethodFromClass = testMethodFromClass;
		this.staticAnalysis = staticAnalysis;
		this.dynamicAnalysis = dynamicAnalysis;
		this.testMethodModel = testMethodModel;

	}

	public Classification<AsAssertion> getClassificationAssert() {
		return (Classification<AsAssertion>) dynamicAnalysis.get(AssertionProcessor.class);
	}

	public Classification<Helper> getClassificationHelperAssertion() {
		return (Classification<Helper>) dynamicAnalysis.get(HelperAssertionProcessor.class);

	}

	public Classification<Helper> getClassificationHelperCall() {
		return (Classification<Helper>) dynamicAnalysis.get(HelperCallProcessor.class);
	}

	public String getNameOfTestClass() {
		return nameOfTestClass;
	}

	public String getTestMethodFromClass() {
		return testMethodFromClass;
	}

	public Classification<AsAssertion> getAllMissedFailFromTest() {
		return (Classification<AsAssertion>) dynamicAnalysis.get(MissedFailProcessor.class);

	}

	public List<CtReturn> getAllSkipFromTest() {
		return (List<CtReturn>) this.staticAnalysis.get(SkipProcessor.class);
	}

	public boolean hasControlFlow() {
		List<Boolean> l = (List<Boolean>) this.staticAnalysis.get(ControlFlowProcessor.class);
		if (l != null && l.size() > 0) {
			return l.get(0);
		}
		return false;
	}

	public boolean isRotten() {
		return !this.getClassificationAssert().getResultNotExecuted().isEmpty()
				|| !this.getClassificationHelperCall().getResultNotExecuted().isEmpty()
				|| !this.getClassificationHelperAssertion().getResultNotExecuted().isEmpty()
				|| (!this.getAllMissedFailFromTest().getResultNotExecuted().isEmpty())
				|| !this.getSkipsExecuted().isEmpty();
	}

	public boolean isSmokeTest() {
		return !isExceptionExpected() && testElementsNotPresentInTest();
	}

	public boolean testElementsNotPresentInTest() {
		return getClassificationAssert().resultExecuted.isEmpty()
				&& getClassificationAssert().resultNotExecuted.isEmpty()
				&& getClassificationHelperCall().resultExecuted.isEmpty()
				&& getClassificationHelperCall().resultNotExecuted.isEmpty()
				&& getClassificationHelperAssertion().resultExecuted.isEmpty()
				&& getClassificationHelperAssertion().resultNotExecuted.isEmpty()
				&& getAllMissedFailFromTest().resultExecuted.isEmpty()
				&& getAllMissedFailFromTest().resultNotExecuted.isEmpty();
	}

	public CtExecutable getTestMethodModel() {
		return testMethodModel;
	}

	public List<String> getExpectException() {
		return (List<String>) this.staticAnalysis.get(ExpectedExceptionAnnotatedProcessor.class);
	}

	public List<CtInvocation> getAllExpectedExceptionFromTest() {
		return (List<CtInvocation>) this.staticAnalysis.get(ExpectedExceptionProcessor.class);
	}

	@Override
	public String toString() {
		return "TestClassificationResult [nameOfTestClass=" + nameOfTestClass + ", testMethodFromClass="
				+ testMethodFromClass + "]";
	}

	public Classification<AsAssertion> getRedundantAssertions() {
		return (Classification<AsAssertion>) this.dynamicAnalysis.get(RedundantAssertionProcessor.class);
	}

	public List<CtInvocation> getAllOtherInvocation() {
		return (List<CtInvocation>) this.staticAnalysis.get(OtherInvocationsProcessor.class);
	}

	public boolean hasHelperCall() {
		return !getClassificationHelperCall().getResultExecuted().isEmpty()
				|| !getClassificationHelperCall().getResultNotExecuted().isEmpty();
	}

	public boolean hasFailInvocation() {
		List fails = this.getAllFailsFromTest();
		return fails != null && fails.size() > 0;
	}

	public boolean hasTryCatch() {
		return testMethodModel.getElements(new TypeFilter<>(CtTry.class)).size() > 0;
	}

	public boolean isExceptionExpected() {

		return ((hasTryCatch() && hasFailInvocation())
				|| (getExpectException().size() > 0 || getAllExpectedExceptionFromTest().size() > 0));
	}

	public boolean isOnlyAssumeExecuted() {

		Classification<Assume> assumes = (Classification<Assume>) this.dynamicAnalysis.get(AssumeProcessor.class);
		if (assumes != null && assumes.getResultExecuted().size() > 0) {
			return assumes.getResultExecuted().get(0).isExecutedAndTrue();
		}

		return false;
	}

	public List<CtInvocation> getAllAssumesFromTest() {
		return (List) this.staticAnalysis.get(AssumeProcessor.class);
	}

	public List<CtInvocation> getAllFailsFromTest() {
		return (List) this.staticAnalysis.get(FailProcessor.class);
	}

	public TestAnalysisResult generateFinalResult() {
		// List<CtReturn> allSkipFromTest2 = this.getAllSkipFromTest();

		List<Helper> notComplexHelperCallComplex = new ArrayList();
		List<Helper> notComplexHelperAssertComplex = new ArrayList();
		List<AsAssertion> notComplexAssertComplex = new ArrayList();
		//
		List<Helper> resultNotExecutedHelperCallComplex = new ArrayList<>();
		List<Helper> resultNotExecutedHelperAssertComplex = new ArrayList<>();
		List<AsAssertion> resultNotExecutedAssertComplex = new ArrayList<>();

		classifyIfComplex(this.getClassificationAssert().resultNotExecuted, notComplexAssertComplex,
				resultNotExecutedAssertComplex);
		classifyIfComplex(this.getClassificationHelperAssertion().resultNotExecuted, notComplexHelperAssertComplex,
				resultNotExecutedHelperAssertComplex);
		classifyIfComplex(this.getClassificationHelperCall().resultNotExecuted, notComplexHelperCallComplex,
				resultNotExecutedHelperCallComplex);

		//
		// List<Helper> resultNotExecutedHelperCall =
		// this.getClassificationHelperCall().getResultNotExecuted();
		// List<Helper> resultNotExecutedHelperAssertion =
		// this.getClassificationHelperAssertion().getResultNotExecuted();
		// List<AsAssertion> resultNotExecutedAssertion =
		// this.getClassificationAssert().getResultNotExecuted();

//		// Skips
//		if (allSkipFromTest2 != null && allSkipFromTest2.size() > 0) {
//			List<Skip> skipss = new ArrayList<>();
//			for (CtReturn aReturn : allSkipFromTest2) {
//				Skip aSkip = new Skip(aReturn);
//				aSkip.getNotExecutedTestElements().addAll(resultNotExecutedHelperCall);
//				aSkip.getNotExecutedTestElements().addAll(resultNotExecutedAssertion);
//
//				skipss.add(aSkip);
//
//			}
//			return new TestAnalysisResult(skipss);
//		}

		boolean smokeTest = isSmokeTest();

//	ok	// checkTwoBranches(rAssert, rAssert, rHelperCall, rHelperAssertion);
//	ok	// checkTwoBranches(rHelperCall, rAssert, rHelperCall, rHelperAssertion);
//	ok	// checkTwoBranches(rHelperAssertion, rAssert, rHelperCall, rHelperAssertion);

		//
//	ok	classifyComplexHelper(notComplexHelperCallComplex, resultNotExecutedHelperCallComplex,
//				resultNotExecutedHelperCall, false /* not assert, a call */);
//	ok	classifyComplexHelper(notComplexHelperAssertComplex, resultNotExecutedHelperAssertComplex,
//				resultNotExecutedHelperAssertion, true /* assert */);
// ok classifyComplexAssert(notComplexAssertComplex,
		// resultNotExecutedAssertComplex, resultNotExecutedAssertion);

		// Executed
		List<AsAssertion> allMissedFail = this.getAllMissedFailFromTest().getAll();

		List<AsAssertion> allRedundant = this.getRedundantAssertions().getAll();

		List<CtInvocation> allOtherMIFromTest = this.getAllOtherInvocation();

		List<Skip> skipsExecutedFromTest = this.getSkipsExecuted();

		return new TestAnalysisResult(notComplexHelperCallComplex, notComplexHelperAssertComplex,
				notComplexAssertComplex, smokeTest, allMissedFail, allRedundant, resultNotExecutedHelperCallComplex,
				resultNotExecutedHelperAssertComplex, resultNotExecutedAssertComplex, allOtherMIFromTest,
				skipsExecutedFromTest);

	}

	private List<Skip> getSkipsExecuted() {

		Classification<?> classification = this.dynamicAnalysis.get(SkipProcessor.class);
		if (classification != null)
			return (List<Skip>) classification.resultExecuted;
		else
			return Collections.EMPTY_LIST;
	}

	public void classifyIfComplex(List<? extends TestElement> source, List notComplexHelperCallComplex,
			List resultNotExecutedHelperCallComplex) {

		for (TestElement testElement : source) {
			if (testElement.isInsideAnIf()) {
				resultNotExecutedHelperCallComplex.add(testElement);
			} else {
				notComplexHelperCallComplex.add(testElement);
			}
		}

	}

}