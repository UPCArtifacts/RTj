package fr.inria.jtanre.rt.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.inria.astor.approaches.extensions.rt.core.Classification;
import fr.inria.astor.approaches.extensions.rt.core.GenericTestAnalysisResults;
import fr.inria.astor.approaches.extensions.rt.core.ResultMap;
import fr.inria.astor.approaches.extensions.rt.elements.AsAssertion;
import fr.inria.astor.approaches.extensions.rt.elements.Helper;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;

/**
 * must be executed after the calls
 * 
 * @author Matias Martinez
 *
 */
public class HelperAssertionProcessor extends HelperProcessor {
	@Override
	public Classification<Helper> classifyElements(ResultMap<Classification<?>> previousDynamic,
			CtClass testClassdModel, CtExecutable testMethodModel, Map<String, SuspiciousCode> mapCacheSuspicious,
			CtStatement stmts, List<Helper> retrievedElementsFromStatic) {

		Classification<Helper> rHelperAssertion = classifyHelpersAssertionExecution(testClassdModel,
				retrievedElementsFromStatic, mapCacheSuspicious, testMethodModel, true);

		Classification<Helper> rHelperCall = (Classification<Helper>) previousDynamic.get(HelperCallProcessor.class);

		// Removing assertion called from helpers not executed
		ignoringHelperAssertionFromNotExecutedHelper(rHelperAssertion.resultNotExecuted, rHelperCall.resultNotExecuted);

		classifyComplexHelper(rHelperAssertion.resultNotExecuted, true /* assert */);

		return rHelperAssertion;

	}

	private void ignoringHelperAssertionFromNotExecutedHelper(List<Helper> resultNotExecutedHelperAssertion,
			List<Helper> resultNotExecutedHelperCall) {

		List<Helper> assertionsToRemove = new ArrayList<>();

		for (Helper anHelperWithAssertion : resultNotExecutedHelperAssertion) {

			for (CtInvocation aCallToAssertion : anHelperWithAssertion.getCalls()) {
				boolean isAlready = false;
				for (Helper helperCallNotExecuted : resultNotExecutedHelperCall) {
					CtElement call = helperCallNotExecuted.getElement();
					if (call.equals(aCallToAssertion)) {
						isAlready = true;
						break;
					}
				}
				//
				if (isAlready) {
					assertionsToRemove.add(anHelperWithAssertion);
				}

			}

		}
		resultNotExecutedHelperAssertion.removeAll(assertionsToRemove);

	}

	@Override
	public void labelTest(GenericTestAnalysisResults analysisResult, List<Helper> staticAnalysis,
			Classification<Helper> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {

		Classification<AsAssertion> rAssert = (Classification<AsAssertion>) dynamics.get(AssertionProcessor.class);
		Classification<Helper> rHelperCall = (Classification<Helper>) dynamics.get(HelperCallProcessor.class);
		Classification<Helper> rHelperAssertion = (Classification<Helper>) dynamics.get(HelperAssertionProcessor.class);
		checkTwoBranches(rHelperAssertion, rAssert, rHelperCall, rHelperAssertion);

	}

}
