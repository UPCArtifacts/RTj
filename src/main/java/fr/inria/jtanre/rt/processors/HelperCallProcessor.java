package fr.inria.jtanre.rt.processors;

import java.util.List;
import java.util.Map;

import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.core.GenericTestAnalysisResults;
import fr.inria.jtanre.rt.core.ResultMap;
import fr.inria.jtanre.rt.elements.AsAssertion;
import fr.inria.jtanre.rt.elements.Helper;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;

public class HelperCallProcessor extends HelperProcessor {

	@Override
	public Classification<Helper> classifyElements(ResultMap<Classification<?>> previousDynamic,
			CtClass testClassdModel, CtExecutable testMethodModel, Map<String, SuspiciousCode> mapCacheSuspicious,
			CtStatement stmts, List<Helper> retrievedElementsFromStatic) {

		Classification<Helper> rHelperCall = classifyHelpersAssertionExecution(testClassdModel,
				retrievedElementsFromStatic, mapCacheSuspicious, testMethodModel, false);

		classifyComplexHelper(rHelperCall.resultNotExecuted, false /* not assert, a call */);

		return rHelperCall;

	}

	@Override
	public void labelTest(GenericTestAnalysisResults analysisResult, List<Helper> staticAnalysis,
			Classification<Helper> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {

		Classification<AsAssertion> rAssert = (Classification<AsAssertion>) dynamics.get(AssertionProcessor.class);
		Classification<Helper> rHelperCall = (Classification<Helper>) dynamics.get(HelperCallProcessor.class);
		Classification<Helper> rHelperAssertion = (Classification<Helper>) dynamics.get(HelperAssertionProcessor.class);
		checkTwoBranches(rHelperCall, rAssert, rHelperCall, rHelperAssertion);

	}

}
