package fr.inria.jtanre.rt.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.inria.astor.approaches.extensions.rt.core.Classification;
import fr.inria.astor.approaches.extensions.rt.core.GenericTestAnalysisResults;
import fr.inria.astor.approaches.extensions.rt.core.ResultMap;
import fr.inria.astor.approaches.extensions.rt.elements.AsAssertion;
import fr.inria.astor.approaches.extensions.rt.elements.Helper;
import fr.inria.astor.approaches.extensions.rt.elements.Skip;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.support.reflect.declaration.CtClassImpl;

public class SkipProcessor extends ElementProcessor<CtReturn, Skip> {

	private List<CtReturn> filterSkips(List<CtStatement> allStmtsFromClass, CtExecutable method,
			// CtClass aTestModelCtClass
			List<CtClass> allClasses) {

		List<CtReturn> skips = new ArrayList<>();

		for (CtStatement aStatement : allStmtsFromClass) {
			if (aStatement instanceof CtReturn) {
				// check the parent class is the test method (discarding elements from anonymous
				// classes)
				CtClassImpl parentClass = (CtClassImpl) aStatement.getParent(CtClassImpl.class);

				if (allClasses.contains(parentClass) &&
				// aTestModelCtClass.equals(parentClass) &&
				// we don't care about returns inside lambda
						aStatement.getParent(CtLambda.class) == null &&
						// check that is not the last statement (if it's the last one it's fine)
						checkNotLast(aStatement, method)

				) {
					skips.add((CtReturn) aStatement);
				}
			}
		}
		return skips;
	}

	@Override
	public List<CtReturn> findElements(Map<String, List<?>> previousPartialResults, List<CtStatement> stmts,
			CtExecutable testMethodModel, List<CtClass> allClasses) {
		return filterSkips(stmts, testMethodModel, allClasses);
	}

	@Test
	public void labelTest(GenericTestAnalysisResults staticAnalysis, List<CtReturn> allSkipFromTest2,
			Classification<Skip> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {
		// Skips
		if (allSkipFromTest2 != null && allSkipFromTest2.size() > 0) {

			Classification<AsAssertion> classifAssertion = (Classification<AsAssertion>) dynamics
					.get(AssertionProcessor.class);
			Classification<Helper> classifHelperCall = (Classification<Helper>) dynamics.get(HelperCallProcessor.class);

			List<Skip> skipss = new ArrayList<>();
			for (CtReturn aReturn : allSkipFromTest2) {
				Skip aSkip = new Skip(aReturn);
				// aSkip.getNotExecutedTestElements().addAll(classifAssertion.resultNotExecuted);
				// aSkip.getNotExecutedTestElements().addAll(classifHelperCall.resultNotExecuted);

				skipss.add(aSkip);

			}
			// return new TestAnalysisResult(skipss);
		}

	}

	@Override
	public Classification<Skip> classifyElements(ResultMap<Classification<?>> previousDynamic, CtClass testClassdModel,
			CtExecutable testMethodModel, Map<String, SuspiciousCode> mapCacheSuspicious, CtStatement stmts,
			List<CtReturn> retrievedElementsFromStatic) {
		Classification<Skip> skips = new Classification<>();

		// For each skip
		for (CtReturn aSkipFromTest : retrievedElementsFromStatic) {

			CtClass ctclassFromAssert = aSkipFromTest.getParent(CtClass.class);

			boolean covered = isCovered(mapCacheSuspicious, aSkipFromTest, ctclassFromAssert, testClassdModel,
					testMethodModel);

			if (!covered) {

				skips.getResultNotExecuted().add(new Skip(aSkipFromTest));

			} else {
				skips.getResultExecuted().add(new Skip(aSkipFromTest));
			}
		}
		return skips;
	}

}
