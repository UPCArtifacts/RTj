package fr.inria.jtanre.rt.processors;

import java.util.List;
import java.util.Map;

import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.core.GenericTestAnalysisResults;
import fr.inria.jtanre.rt.core.ResultMap;
import fr.inria.jtanre.rt.elements.AsAssertion;
import fr.inria.jtanre.rt.elements.Helper;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;

public class AssertionProcessor extends ElementProcessor<CtInvocation, AsAssertion> {

	/**
	 * Retrieve the assertions from a list of statements. It does not analyze beyond
	 * the first level (no recursive)
	 * 
	 * @param allStmtsFromClass
	 * @return
	 */
	public List<CtInvocation> filterAssertions(List<CtStatement> allStmtsFromClass) {
		return filterInvocation(allStmtsFromClass, ASSERT);
	}

	@Override
	public List<CtInvocation> findElements(Map<String, List<?>> previousPartialResults, CtClass aTestModelCtClass,
			List<CtStatement> stmts, CtExecutable testMethodModel, List<CtClass> allClasses) {
		return filterAssertions(stmts);
	}

	@Override
	public Classification<AsAssertion> classifyElements(ResultMap<Classification<?>> previousDynamic,
			CtClass testClassdModel, CtExecutable testMethodModel, Map<String, SuspiciousCode> mapCacheSuspicious,
			CtStatement stmts, List<CtInvocation> retrievedElementsFromStatic) {

		Classification<AsAssertion> rAssert = classifyAssertions(testMethodModel, mapCacheSuspicious, testClassdModel,
				retrievedElementsFromStatic);

		classifyComplexAssert(rAssert.resultNotExecuted);

		return rAssert;
	}

	public Classification<AsAssertion> classifyAssertions(CtExecutable methodOfAssertment,
			Map<String, SuspiciousCode> mapCacheSuspicious, CtClass aTestModelCtClass,
			List<CtInvocation> allAssertionsFromTest) {
		Classification<AsAssertion> result = new Classification();

		if (allAssertionsFromTest == null || allAssertionsFromTest.isEmpty()) {
			return result;
		}

		// For each assert
		for (CtInvocation anAssertFromTest : allAssertionsFromTest) {

			CtClass ctclassFromAssert = anAssertFromTest.getParent(CtClass.class);

			boolean covered = isCovered(mapCacheSuspicious, anAssertFromTest, ctclassFromAssert, aTestModelCtClass,
					methodOfAssertment);

			if (!covered) {

				result.getResultNotExecuted().add(new AsAssertion(anAssertFromTest));
				log.info("Not covered: " + anAssertFromTest + " at " + aTestModelCtClass.getQualifiedName());
			} else {
				result.getResultExecuted().add(new AsAssertion(anAssertFromTest));
			}
		}

		return result;
	}

	/**
	 * If the element has an IF parent, then goes to complex list, otherwise to the
	 * no complex.
	 * 
	 * @param notComplex
	 * @param resultNotExecutedHelperCallComplex
	 * @param resultNotExecutedAssertion
	 */
	public void classifyComplexAssert(List<AsAssertion> resultNotExecutedAssertion) {
		for (AsAssertion testElement : resultNotExecutedAssertion) {

			CtIf parentIf = testElement.getElement().getParent(CtIf.class);
			if (parentIf != null) {
				// complex
				testElement.setInsideAnIf(true);
			} else {
				// not complex
				testElement.setInsideAnIf(false);
			}
		}
	}

	@Override
	public void labelTest(GenericTestAnalysisResults analysisResult, List<CtInvocation> staticAnalysis,
			Classification<AsAssertion> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {

		Classification<AsAssertion> rAssert = (Classification<AsAssertion>) dynamics.get(AssertionProcessor.class);
		Classification<Helper> rHelperCall = (Classification<Helper>) dynamics.get(HelperCallProcessor.class);
		Classification<Helper> rHelperAssertion = (Classification<Helper>) dynamics.get(HelperAssertionProcessor.class);
		checkTwoBranches(rAssert, rAssert, rHelperCall, rHelperAssertion);

	}

}
