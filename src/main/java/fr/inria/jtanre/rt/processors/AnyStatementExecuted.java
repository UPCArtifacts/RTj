package fr.inria.jtanre.rt.processors;

import java.util.Collections;
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

public class AnyStatementExecuted extends ElementProcessor<CtInvocation, Object> {

	private boolean checkAnyStatementExecuted(List<CtStatement> allStmtsFromClass,
			Map<String, SuspiciousCode> mapCacheSuspicious, CtClass aTestModelCtClass, CtExecutable testMethodModel) {

		for (CtStatement statement : allStmtsFromClass) {
			// We pass twice 'aTestModelCtClass' because we check if a test statement is
			// executed
			boolean covered = isCovered(mapCacheSuspicious, statement, aTestModelCtClass, aTestModelCtClass,
					testMethodModel);
			if (covered) {
				return true;
			}
		}
		return false;

	}

	@Override
	public List<CtInvocation> findElements(Map<String, List<?>> previousPartialResults, CtClass aTestModelCtClass,
			List<CtStatement> stmts, CtExecutable testMethodModel, List<CtClass> allClasses) {
		// TODO Auto-generated method stub
		return Collections.EMPTY_LIST;
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
