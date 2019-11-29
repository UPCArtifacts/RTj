package fr.inria.jtanre.rt.processors;

import java.util.List;
import java.util.Map;

import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.core.GenericTestAnalysisResults;
import fr.inria.jtanre.rt.core.ResultMap;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

public class SmokeProcessor extends ElementProcessor<CtInvocation, Object> {

	@Override
	public List<CtInvocation> findElements(Map<String, List<?>> previousPartialResults, List<CtStatement> stmts,
			CtExecutable testMethodModel, List<CtClass> allClasses) {

		List<CtInvocation> allAssertionsFromTest = (List<CtInvocation>) ((ResultMap) previousPartialResults)
				.get(AssertionProcessor.class);
		List<CtInvocation> allFailsFromTest = (List<CtInvocation>) ((ResultMap) previousPartialResults)
				.get(MissedFailProcessor.class);

		// We exclude assertions and fails from the list of other method invocations.
		List<CtInvocation> allMIFromTest = testMethodModel.getBody().getElements(new TypeFilter<>(CtInvocation.class));
		if (allMIFromTest != null && allMIFromTest.size() > 0) {
			if (allAssertionsFromTest != null)
				allMIFromTest.removeAll(allAssertionsFromTest);
			if (allFailsFromTest != null)
				allMIFromTest.removeAll(allFailsFromTest);
		}

		return allMIFromTest;
	}

	@Override
	public Classification<Object> classifyElements(ResultMap<Classification<?>> previousDynamic,
			CtClass testClassdModel, CtExecutable testMethodModel, Map<String, SuspiciousCode> mapCacheSuspicious,
			CtStatement stmts, List<CtInvocation> retrievedElementsFromStatic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void labelTest(GenericTestAnalysisResults analysisResult, List<CtInvocation> staticAnalysis,
			Classification<Object> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {
		// TODO Auto-generated method stub

	}

}
