package fr.inria.jtanre.rt.processors;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.core.GenericTestAnalysisResults;
import fr.inria.jtanre.rt.core.ResultMap;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;

public class ExpectedExceptionAnnotatedProcessor extends ElementProcessor<String, Object> {

	@Override
	public List<String> findElements(Map<String, List<?>> previousPartialResults, CtClass aTestModelCtClass,
			List<CtStatement> stmts, CtExecutable testMethodModel, List<CtClass> allClasses) {
		List<String> expectException = expectEx(testMethodModel);

		return expectException;
	}

	private List<String> expectEx(CtExecutable testMethodModel) {

		return testMethodModel.getAnnotations().stream()
				.filter(e -> e.getType().getSimpleName().equals("Test") && e.getValues().containsKey("expected"))
				.map(e -> e.getValues().get("expected").toString()).collect(Collectors.toList());

	}

	@Override
	public Classification<Object> classifyElements(ResultMap<Classification<?>> previousDynamic,
			CtClass testClassdModel, CtExecutable testMethodModel, Map<String, SuspiciousCode> mapCacheSuspicious,
			CtStatement stmts, List<String> retrievedElementsFromStatic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void labelTest(GenericTestAnalysisResults analysisResult, List<String> staticAnalysis,
			Classification<Object> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {
		// TODO Auto-generated method stub

	}

}
