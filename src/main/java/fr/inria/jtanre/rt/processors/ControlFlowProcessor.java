package fr.inria.jtanre.rt.processors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.core.GenericTestAnalysisResults;
import fr.inria.jtanre.rt.core.ResultMap;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

public class ControlFlowProcessor extends ElementProcessor<Boolean, Object> {

	@Override
	public List<Boolean> findElements(Map<String, List<?>> previousPartialResults, List<CtStatement> stmts,
			CtExecutable testMethodModel, List<CtClass> allClasses) {

		return Arrays.asList(hasControlFlow(testMethodModel));
	}

	@Override
	public Classification<Object> classifyElements(ResultMap<Classification<?>> previousDynamic,
			CtClass testClassdModel, CtExecutable testMethodModel, Map<String, SuspiciousCode> mapCacheSuspicious,
			CtStatement stmts, List<Boolean> retrievedElementsFromStatic) {

		return null;
	}

	public boolean hasControlFlow(CtExecutable testMethodModel) {
		return testMethodModel.getElements(new TypeFilter<>(CtIf.class)).size() > 0
				//
				|| testMethodModel.getElements(new TypeFilter<>(CtWhile.class)).size() > 0
				//
				|| testMethodModel.getElements(new TypeFilter<>(CtFor.class)).size() > 0
				//
				|| testMethodModel.getElements(new TypeFilter<>(CtForEach.class)).size() > 0
				//
				|| testMethodModel.getElements(new TypeFilter<>(CtSwitch.class)).size() > 0
				//
				|| testMethodModel.getElements(new TypeFilter<>(CtDo.class)).size() > 0;

	}

	@Override
	public void labelTest(GenericTestAnalysisResults analysisResult, List<Boolean> staticAnalysis,
			Classification<Object> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {

	}

}
