package fr.inria.jtanre.rt.processors;

import java.util.List;
import java.util.Map;

import fr.inria.astor.approaches.extensions.rt.core.Classification;
import fr.inria.astor.approaches.extensions.rt.core.GenericTestAnalysisResults;
import fr.inria.astor.approaches.extensions.rt.core.ResultMap;
import fr.inria.astor.approaches.extensions.rt.elements.AsAssertion;
import fr.inria.astor.approaches.extensions.rt.elements.Assume;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.LineFilter;

/**
 * 
 * @author Matias Martinez
 *
 */
public class AssumeProcessor extends AssertionProcessor {

	private List<CtInvocation> filterAssume(List<CtStatement> allStmtsFromClass) {
		return filterInvocation(allStmtsFromClass, ASSUME);
	}

	@Override
	public List<CtInvocation> findElements(Map<String, List<?>> previousPartialResults, List<CtStatement> stmts,
			CtExecutable testMethodModel, List<CtClass> allClasses) {

		return filterAssume(stmts);
	}

	@Override
	public Classification<AsAssertion> classifyElements(ResultMap<Classification<?>> previousDynamic,
			CtClass testClassdModel, CtExecutable testMethodModel, Map<String, SuspiciousCode> mapCacheSuspicious,
			CtStatement stmts, List<CtInvocation> retrievedElementsFromStatic) {

		List<CtStatement> allStmtsFromClass = testMethodModel.getElements(new LineFilter());

		boolean onlyAssumeExecuted = checkOnlyAssumeExecuted(allStmtsFromClass, mapCacheSuspicious, testClassdModel,
				retrievedElementsFromStatic, testMethodModel);

		Classification<AsAssertion> assertion = classifyAssertions(testMethodModel, mapCacheSuspicious, testClassdModel,
				retrievedElementsFromStatic);

		Classification<AsAssertion> assumes = new Classification<>();

		for (AsAssertion ctStatement : assertion.resultExecuted) {
			Assume assume = new Assume(ctStatement.getCtAssertion());
			assumes.resultExecuted.add(assume);
			assume.setExecutedAndTrue(onlyAssumeExecuted);
			assumes.resultNotExecuted.add(assume);
		}

		for (AsAssertion ctStatement : assertion.resultNotExecuted) {
			Assume assume = new Assume(ctStatement.getCtAssertion());
			assume.setExecutedAndTrue(onlyAssumeExecuted);
		}

		return assumes;
	}

	private boolean checkOnlyAssumeExecuted(List<CtStatement> allStmtsFromClass,
			Map<String, SuspiciousCode> mapCacheSuspicious, CtClass aTestModelCtClass, List<CtInvocation> invocations,
			CtExecutable testMethodModel) {

		if (invocations == null || invocations.isEmpty()) {
			return false;
		}
		// Put true once we analyze the assertion
		boolean assureAnalyzed = false;
		for (CtInvocation invocation : invocations) {
			boolean invocationExecuted = false;
			boolean otherStatementExecutedAfter = false;
			for (CtStatement statement : allStmtsFromClass) {
				boolean covered = isCovered(mapCacheSuspicious, statement, aTestModelCtClass, aTestModelCtClass,
						testMethodModel);
				if (covered) {
					if (invocation == statement) {
						invocationExecuted = true;
						assureAnalyzed = true;
					} else {
						// Once we analyze the assure
						if (assureAnalyzed)
							otherStatementExecutedAfter = true;
					}
				}
			}
			return invocationExecuted && !otherStatementExecutedAfter;
		}
		return false;
	}

	@Override
	public void labelTest(GenericTestAnalysisResults analysisResult, List<CtInvocation> staticAnalysis,
			Classification<AsAssertion> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {

	}

}
