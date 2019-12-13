package fr.inria.jtanre.extension4test;

import java.util.List;
import java.util.Map;

import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.core.GenericTestAnalysisResults;
import fr.inria.jtanre.rt.core.ProgramModel;
import fr.inria.jtanre.rt.core.ResultMap;
import fr.inria.jtanre.rt.processors.TestAnalyzer;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;

public class TestAnalyzerTest<T, C, MC> implements TestAnalyzer {

	public TestAnalyzerTest() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List findElements(Map previousPartialResults, List stmts, CtExecutable testMethodModel, List allClasses) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Classification classifyElements(ResultMap previousDynamic, Object testClassdModel,
			CtExecutable testMethodModel, Map mapCacheSuspicious, CtStatement stmts, List retrievedElementsFromStatic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void labelTest(GenericTestAnalysisResults analysisResult, List staticAnalysis, Classification dynamic,
			ResultMap statics, ResultMap dynamics) {
		// TODO Auto-generated method stub

	}

	@Override
	public List refactor(ProgramModel model, CtClass aTestModelCtClass, GenericTestAnalysisResults analysisResult,
			List staticAnalysis, Classification dynamic, ResultMap statics, ResultMap dynamics) {
		// TODO Auto-generated method stub
		return null;
	}

}
