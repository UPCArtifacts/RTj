package fr.inria.jtanre.rt.processors;

import java.util.List;
import java.util.Map;

import fr.inria.astor.approaches.extensions.rt.core.Classification;
import fr.inria.astor.approaches.extensions.rt.core.GenericTestAnalysisResults;
import fr.inria.astor.approaches.extensions.rt.core.ResultMap;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;

public class FailProcessor extends ElementProcessor<CtInvocation, Object> {

	private List<CtInvocation> filterFails(List<CtStatement> allStmtsFromClass) {
		return filterInvocation(allStmtsFromClass, FAIL);
	}

	@Override
	public List<CtInvocation> findElements(Map<String, List<?>> previousPartialResults, List<CtStatement> stmts,
			CtExecutable testMethodModel, List<CtClass> allClasses) {
		return filterFails(stmts);
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
