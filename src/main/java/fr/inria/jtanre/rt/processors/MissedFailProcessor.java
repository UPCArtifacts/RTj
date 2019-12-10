package fr.inria.jtanre.rt.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.inria.astor.approaches.jgenprog.operators.InsertBeforeOp;
import fr.inria.astor.approaches.jgenprog.operators.ReplaceOp;
import fr.inria.astor.core.entities.ModificationPoint;
import fr.inria.astor.core.entities.OperatorInstance;
import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.entities.StatementOperatorInstance;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.manipulation.MutationSupporter;
import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.core.GenericTestAnalysisResults;
import fr.inria.jtanre.rt.core.ProgramModel;
import fr.inria.jtanre.rt.core.ResultMap;
import fr.inria.jtanre.rt.elements.AsAssertion;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtComment.CommentType;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;

public class MissedFailProcessor extends AssertionProcessor {

	private List<CtInvocation> filterMissedFail(List<CtInvocation> allAssertionsFromTest) {

		List<CtInvocation> missedFails = new ArrayList<>();

		for (CtInvocation anInvocation : allAssertionsFromTest) {
			filterMissingFailAssertion(missedFails, anInvocation);

		}
		return missedFails;
	}

	protected void filterMissingFailAssertion(List<CtInvocation> redundantAssertion, CtInvocation anInvocation) {
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
			if (isInvWithName(anInvocation, "assertFalse")) {
				// Now, we case expecting false and passing True
				if (contentArgumentLC.equals("\"true\"") || contentArgumentLC.equals("true")
						|| contentArgumentLC.equals("boolean.true"))
					redundantAssertion.add((anInvocation));

			} else if (isInvWithName(anInvocation, "assertTrue")) {
				if (contentArgumentLC.equals("\"false\"") || contentArgumentLC.equals("false")
						|| contentArgumentLC.equals("boolean.false"))
					// Now, we find for a false parameter:
					redundantAssertion.add((anInvocation));

			}
		}
	}

	@Override
	public List<CtInvocation> findElements(Map<String, List<?>> previousPartialResults, List<CtStatement> stmts,
			CtExecutable testMethodModel, List<CtClass> allClasses) {

		List<CtInvocation> assertions = (List<CtInvocation>) ((ResultMap) previousPartialResults)
				.get((AssertionProcessor.class));
		if (assertions != null) {
			List<CtInvocation> missingfails = filterMissedFail(assertions);

			assertions.removeAll(missingfails);

			return missingfails;

		} else
			return Collections.EMPTY_LIST;
	}

	@Override
	public Classification<AsAssertion> classifyElements(ResultMap<Classification<?>> previousDynamic,
			CtClass testClassdModel, CtExecutable testMethodModel, Map<String, SuspiciousCode> mapCacheSuspicious,
			CtStatement stmts, List<CtInvocation> retrievedElementsFromStatic) {

		Classification<AsAssertion> rFailMissing = classifyAssertions(testMethodModel, mapCacheSuspicious,
				testClassdModel, retrievedElementsFromStatic);

		chechInsideTry(rFailMissing.resultExecuted, mapCacheSuspicious, testClassdModel, testMethodModel);
		chechInsideTry(rFailMissing.resultNotExecuted, mapCacheSuspicious, testClassdModel, testMethodModel);

		return rFailMissing;
	}

	@Override
	public void labelTest(GenericTestAnalysisResults analysisResult, List<CtInvocation> staticAnalysis,
			Classification<AsAssertion> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {
		super.labelTest(analysisResult, staticAnalysis, dynamic, statics, dynamics);
	}

	@Override
	public List<ProgramVariant> refactor(ProgramModel model, CtClass aTestModelCtClass,
			GenericTestAnalysisResults analysisResult, List<CtInvocation> staticAnalysis,
			Classification<AsAssertion> dynamic, ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {

		List<ProgramVariant> res = new ArrayList<>();
		for (CtInvocation inv : staticAnalysis) {

			ProgramVariant pv = new ProgramVariant();
			pv.getBuiltClasses().put(aTestModelCtClass.getQualifiedName(), aTestModelCtClass);
			ModificationPoint mp = new ModificationPoint();
			mp.setCodeElement(inv);
			mp.setProgramVariant(pv);
			mp.setCtClass(aTestModelCtClass);
			pv.getModificationPoints().add(mp);

			///
			CtComment comment = MutationSupporter.getFactory().createComment(
					"TODO: Rotten Refactored:\n" + inv.toString() + "\nProposed refactor:\n", CommentType.BLOCK);
			CtStatement fail = MutationSupporter.getFactory().createCodeSnippetStatement("org.junit.Assert.fail()");
			InsertBeforeOp insertBefore = new InsertBeforeOp();
			OperatorInstance commentOpInstance = new StatementOperatorInstance(mp, insertBefore, inv, comment);
			//
			ReplaceOp rep = new ReplaceOp();

			OperatorInstance replaceMissingOpInstance = new StatementOperatorInstance(mp, rep, inv, fail);

			pv.getOperations(1).add(commentOpInstance);
			pv.getOperations(1).add(replaceMissingOpInstance);
			res.add(pv);
		}
		return res;
	}

}
