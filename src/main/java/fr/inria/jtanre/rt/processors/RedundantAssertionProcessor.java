package fr.inria.jtanre.rt.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.core.GenericTestAnalysisResults;
import fr.inria.jtanre.rt.core.ResultMap;
import fr.inria.jtanre.rt.elements.AsAssertion;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;

public class RedundantAssertionProcessor extends AssertionProcessor {

	/**
	 * Tree cases: AssertEquals (x,x) AssertTrue(True) AssertFalse(False)
	 * 
	 * @param allAssertionsFromTest
	 * @return
	 */
	private List<CtInvocation> filterRedundantAssertions(List<CtInvocation> allAssertionsFromTest) {

		List<CtInvocation> redundantAssertion = new ArrayList<>();

		for (CtInvocation anInvocation : allAssertionsFromTest) {

			filterCaseEquals(anInvocation, redundantAssertion);

			filterCaseAssertRedundant(redundantAssertion, anInvocation);

		}
		return redundantAssertion;
	}

	@Override
	public List<CtInvocation> findElements(Map<String, List<?>> previousPartialResults, List<CtStatement> stmts,
			CtExecutable testMethodModel, List<CtClass> allClasses) {

		List<CtInvocation> assertions = (List<CtInvocation>) previousPartialResults
				.get(AssertionProcessor.class.getSimpleName());
		if (assertions != null) {
			return filterRedundantAssertions(assertions);
		} else
			return null;
	}

	@Override
	public Classification<AsAssertion> classifyElements(ResultMap<Classification<?>> previousDynamic,
			CtClass testClassdModel, CtExecutable testMethodModel, Map<String, SuspiciousCode> mapCacheSuspicious,
			CtStatement stmts, List<CtInvocation> retrievedElementsFromStatic) {

		Classification<AsAssertion> rFailMissing = classifyAssertions(testMethodModel, mapCacheSuspicious,
				testClassdModel, retrievedElementsFromStatic);

		// to adapt
		chechInsideTry(rFailMissing.resultExecuted, mapCacheSuspicious, testClassdModel, testMethodModel);
		chechInsideTry(rFailMissing.resultNotExecuted, mapCacheSuspicious, testClassdModel, testMethodModel);

		return rFailMissing;
	}

	private void filterCaseAssertRedundant(List<CtInvocation> redundantAssertion, CtInvocation anInvocation) {
		CtElement argument = null;
		// case having a single argument
		if (anInvocation.getArguments().size() == 1) {
			argument = (CtElement) anInvocation.getArguments().get(0);
			// case having a message as first arg
		} else if (anInvocation.getArguments().size() == 2) {
			argument = (CtElement) anInvocation.getArguments().get(1);
		}
		if (argument != null) {
			String contentArgumentLC = argument.toString().toLowerCase();
			if (isInvWithName(anInvocation, "assertTrue")) {
				// Now, we case expecting false and passing True
				if (contentArgumentLC.equals("\"true\"") || contentArgumentLC.equals("true")
						|| contentArgumentLC.equals("boolean.true"))
					redundantAssertion.add((anInvocation));

			} else if (isInvWithName(anInvocation, "assertFalse")) {
				if (contentArgumentLC.equals("\"false\"") || contentArgumentLC.equals("false")
						|| contentArgumentLC.equals("boolean.false"))
					// Now, we find for a false parameter:
					redundantAssertion.add((anInvocation));

			}
		}
	}

	private void filterCaseEquals(CtInvocation anInvocation, List<CtInvocation> redundantAssertion) {
		CtElement argument1 = null, argument2 = null;

		if (isInvWithName(anInvocation, "assertEquals")) {

			// case having a single argument
			if (anInvocation.getArguments().size() == 3) {
				argument1 = (CtElement) anInvocation.getArguments().get(1);
				argument2 = (CtElement) anInvocation.getArguments().get(2);
				// case having a message as first arg
			} else if (anInvocation.getArguments().size() == 2) {
				argument1 = (CtElement) anInvocation.getArguments().get(0);
				argument2 = (CtElement) anInvocation.getArguments().get(1);
			}

			if (argument1 != null && argument2 != null && argument1.toString().equals(argument2.toString())) {
				redundantAssertion.add(anInvocation);
			}

		}
	}

	@Override
	public void labelTest(GenericTestAnalysisResults analysisResult, List<CtInvocation> staticAnalysis,
			Classification<AsAssertion> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {
		// TODO Auto-generated method stub
		super.labelTest(analysisResult, staticAnalysis, dynamic, statics, dynamics);
	}

}
