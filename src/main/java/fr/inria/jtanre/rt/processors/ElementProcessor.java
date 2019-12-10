package fr.inria.jtanre.rt.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.inria.astor.core.entities.ProgramVariant;
import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.astor.core.faultlocalization.gzoltar.TestCaseResult;
import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.core.GenericTestAnalysisResults;
import fr.inria.jtanre.rt.core.ProgramModel;
import fr.inria.jtanre.rt.core.ResultMap;
import fr.inria.jtanre.rt.elements.AsAssertion;
import fr.inria.jtanre.rt.elements.Helper;
import fr.inria.jtanre.rt.elements.TestElement;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.filter.LineFilter;

/**
 * 
 * @author Matias Martinez
 *
 * @param <T>
 */
public abstract class ElementProcessor<T, C> implements TestAnalyzer<T, C, CtClass> {
	protected static Logger log = Logger.getLogger(Thread.currentThread().getName());

	protected static final String ASSUME = "assume";

	protected static final String FAIL = "fail";

	protected static final String ASSERT = "assert";

	@Override
	public List<ProgramVariant> refactor(ProgramModel model, CtClass aTestModelCtClass,
			GenericTestAnalysisResults analysisResult, List<T> staticAnalysis, Classification<C> dynamic,
			ResultMap<List<?>> statics, ResultMap<Classification<?>> dynamics) {
		return Collections.EMPTY_LIST;
	};

	protected List<CtInvocation> filterInvocation(List<CtStatement> allStmtsFromClass, String filterName) {
		List<CtInvocation> assertions = new ArrayList<>();
		for (CtStatement targetElement : allStmtsFromClass) {
			if (targetElement instanceof CtInvocation) {
				CtInvocation targetInvocation = (CtInvocation) targetElement;
				if (isInvWithName(targetInvocation, filterName)) {
					assertions.add(targetInvocation);
				}
			}
		}
		return assertions;
	}

	protected boolean isAssertion(CtInvocation targetInvocation) {
		return isInvWithName(targetInvocation, ASSERT);
	}

	/**
	 * Return if the invocation is an assertion
	 * 
	 * @param targetInvocation
	 * @return
	 */
	protected boolean isInvWithName(CtInvocation targetInvocation, String methodName) {

		String package1 = (targetInvocation.getTarget() != null) ? targetInvocation.getTarget().toString() : null;

		if (package1 != null && !package1.startsWith("org.junit") && !package1.startsWith("junit.framework"))
			return false;

		boolean isAssert = targetInvocation.getExecutable().getSimpleName().toLowerCase()
				.startsWith(methodName.toLowerCase());
		if (isAssert) {
			return true;
		}
		try {
			if (targetInvocation.getExecutable() != null
					&& targetInvocation.getExecutable().getDeclaringType() != null) {
				String name = targetInvocation.getExecutable().getDeclaringType().getQualifiedName();
				// TODO: disable for the moment
				// Optional<String> testnm = this.namespace.stream().filter(e ->
				// name.startsWith(e)).findFirst();
				// if (testnm.isPresent()) {
				// log.debug("assert " + targetInvocation.getExecutable().getSimpleName() + "
				// found in " + testnm.get());
				// return true;
				// }
			}

			if (targetInvocation.getTarget() != null && targetInvocation.getTarget() instanceof CtInvocation) {
				CtInvocation targetInv = (CtInvocation) targetInvocation.getTarget();
				return isInvWithName(targetInv, methodName);
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			log.error("Continue executing after " + e.getMessage());
		}
		return false;
	}

	/**
	 * Return true if it's not last
	 * 
	 * @param aStatement
	 * @param method
	 * @return
	 */
	protected boolean checkNotLast(CtStatement aStatement, CtExecutable method) {

		CtStatement lastStatement = method.getBody().getLastStatement();

		if (lastStatement instanceof CtInvocation) {
			CtInvocation targetInvocation = (CtInvocation) lastStatement;
			if (isInvWithName(targetInvocation, FAIL)) {
				return false;
			}
		}

		// It's not the last one in the method
		return !lastStatement.equals(aStatement)
				// check that statement is not inside an element that is the last one
				&& !lastStatement.equals(aStatement.getParent(new LineFilter()));
	}

	protected void checkTwoBranches(Classification<? extends TestElement> elementsToClassify,
			Map<String, SuspiciousCode> cacheSuspicious, CtClass ctclassFromElementToCheck, CtClass aTestModelCtClass) {

		for (TestElement target : elementsToClassify.resultNotExecuted) {

			CtElement invocation = (target instanceof Helper && ((Helper) target).unexecutedAssert)
					? ((Helper) target).getAssertion().getElement()
					: target.getElement();
			CtIf parentif = null;
			boolean inThen = false;
			// Let's retrieve the parent if (I dont use getParent because I want the
			// Immediate parent)
			if (invocation.getParent() instanceof CtIf) {
				parentif = (CtIf) invocation.getParent();
				inThen = invocation.getRoleInParent().equals(CtRole.THEN);
			} else {

				if (invocation.getParent() instanceof CtBlock
						&& (invocation.getParent().getRoleInParent().equals(CtRole.THEN)
								|| invocation.getParent().getRoleInParent().equals(CtRole.ELSE))) {

					parentif = (CtIf) invocation.getParent().getParent();
					inThen = invocation.getParent().getRoleInParent().equals(CtRole.THEN);
				}
			}
			//
			if (parentif != null) {
				CtStatement toAnalyze = inThen ? parentif.getElseStatement() : parentif.getThenStatement();

				// other statements in the other branch
				List<CtStatement> stms = (toAnalyze instanceof CtBlock) ? ((CtBlock) toAnalyze).getStatements()
						: Collections.singletonList(toAnalyze);

				// let's check if exist

				for (CtStatement anStatement : stms) {
					// let's check if the other branch has executed assertions/helpers
					boolean exist = isCovered(cacheSuspicious, anStatement, ctclassFromElementToCheck,
							aTestModelCtClass);

					if (exist) {
						target.setFp(true);
						log.debug("Found executed in the other branch");
						break;
					}
				}

			}
		}

	}

	protected void checkTwoBranches(Classification<? extends TestElement> elementsToClassify,
			Classification<? extends TestElement> rAssertions, Classification<? extends TestElement> rHelperCall,
			Classification<Helper> rHelperAssertion) {

		for (TestElement target : elementsToClassify.resultNotExecuted) {

			CtElement invocation = (target instanceof Helper && ((Helper) target).unexecutedAssert)
					? ((Helper) target).getAssertion().getElement()
					: target.getElement();
			CtIf parentif = null;
			boolean inThen = false;
			// Let's retrieve the parent if (I dont use getParent because I want the
			// Immediate parent)
			if (invocation.getParent() instanceof CtIf) {
				parentif = (CtIf) invocation.getParent();
				inThen = invocation.getRoleInParent().equals(CtRole.THEN);
			} else {

				if (invocation.getParent() instanceof CtBlock
						&& (invocation.getParent().getRoleInParent().equals(CtRole.THEN)
								|| invocation.getParent().getRoleInParent().equals(CtRole.ELSE))) {

					parentif = (CtIf) invocation.getParent().getParent();
					inThen = invocation.getParent().getRoleInParent().equals(CtRole.THEN);
				}
			}
			//
			if (parentif != null) {
				CtStatement toAnalyze = inThen ? parentif.getElseStatement() : parentif.getThenStatement();

				// other statements in the other branch
				List<CtStatement> stms = (toAnalyze instanceof CtBlock) ? ((CtBlock) toAnalyze).getStatements()
						: Collections.singletonList(toAnalyze);

				// let's check if exist

				for (CtStatement anStatement : stms) {
					// let's check if the other branch has executed assertions/helpers
					boolean exist = rAssertions.getResultExecuted().stream().filter(e -> e.getElement() == anStatement)
							.findFirst().isPresent();

					// Assertion executed by a helper
					exist = exist || rHelperAssertion.getResultExecuted().stream()
							.filter(e -> e.getAssertion().getCtAssertion() == anStatement).findFirst().isPresent();

					exist = exist || checkStatementInCallStack(rHelperAssertion, anStatement);

					if (exist) {
						target.setFp(true);
						log.debug("Found executed in the other branch");
						break;
					}
				}

			}
		}

	}

	private boolean checkStatementInCallStack(Classification<Helper> rHelperAssertion, CtStatement anStatement) {

		for (Helper helperAssertion : rHelperAssertion.getResultExecuted()) {

			for (CtInvocation invocationInCall : helperAssertion.getCalls()) {
				if (invocationInCall == anStatement)
					return true;
			}

		}
		return false;
	}

	protected boolean isCovered(Map<String, SuspiciousCode> cacheSuspicious, CtElement elementToCheck,
			CtClass ctclassFromElementToCheck, CtClass aTestModelCtClass, CtExecutable testMethodModel) {
		try {
			// the location of the assertion contained in the helper
			int init = elementToCheck.getPosition().getLine();
			int end = elementToCheck.getPosition().getEndLine();
			// check if cover in one range of locations
			for (int i = init; i <= end; i++) {

				String keyLocationAssertion = getClassName(ctclassFromElementToCheck) + i;

				if (isCoverSingleLine(cacheSuspicious, aTestModelCtClass, testMethodModel, keyLocationAssertion))
					return true;

			}
		} catch (Exception e) {
			log.error("Error getting position of element");
			e.printStackTrace();
		}
		return false;

	}

	private String getClassName(CtType mclass) {
		if (mclass.isAnonymous()) {
			return getClassName(mclass.getDeclaringType());
		} else {
			return mclass.getQualifiedName();
		}
	}

	private boolean isCoverSingleLine(Map<String, SuspiciousCode> cacheSuspicious, CtClass aTestModelCtClass,
			CtExecutable testMethodModel, String keyLocationAssertion) {
		if (cacheSuspicious.containsKey(keyLocationAssertion)) {
			// Assertion was covered, let's check if by the current test case
			SuspiciousCode cover = cacheSuspicious.get(keyLocationAssertion);
			for (TestCaseResult tr : cover.getCoveredByTests()) {
				if (tr.getTestCaseClass().equals(aTestModelCtClass.getQualifiedName())
						&& (testMethodModel == null || tr.getTestCaseName().equals(testMethodModel.getSimpleName()))) {
					return true;
				}
			}

		}
		return false;
	}

	public static boolean isCovered(Map<String, SuspiciousCode> cacheSuspicious, CtElement elementToCheck,
			CtClass ctclassFromElementToCheck, CtClass aTestModelCtClass) {
		return isCovered(cacheSuspicious, elementToCheck, ctclassFromElementToCheck, aTestModelCtClass);
	}

	protected String keySignatureExecuted(SuspiciousCode e) {
		return keySignatureExecuted(e.getClassName(), getTestCaseMethodName(e));
	}

	protected String getTestCaseMethodName(SuspiciousCode e) {
		// We only consider a method name, which, at least in JUnit, must be unique (we
		// cannot have two test methods with the same name)
		return e.getMethodName().substring(0, e.getMethodName().indexOf("("));
	}

	protected String keySignatureExecuted(String classname, String methodName) {
		return classname + "-" + methodName;
	}

	/**
	 * Check if a missing assertion is inside a try-catch
	 * 
	 * @param allMissedFailFromTest
	 * @param executedLines
	 * @param mapCacheSuspicious
	 * @param parentClass
	 * @param testMethodModel
	 */
	protected void chechInsideTry(List<AsAssertion> allMissedFailFromTest,
			Map<String, SuspiciousCode> mapCacheSuspicious, CtClass parentClass, CtExecutable testMethodModel) {

		for (AsAssertion aMissAssertion : allMissedFailFromTest) {

			CtTry parentTry = aMissAssertion.getCtAssertion().getParent(CtTry.class);
			if (parentTry != null) {
				for (CtCatch aCatch : parentTry.getCatchers()) {
					CtBlock block = aCatch.getBody();
					if (block != null && block.getStatements().size() > 0) {
						for (CtStatement anStatementInBlock : block.getStatements()) {

							boolean covered = isCovered(mapCacheSuspicious, anStatementInBlock, parentClass,
									parentClass, testMethodModel);
							if (covered) {
								aMissAssertion.setFp(true);
								continue;
							}

						}
					} else {
						CtBlock pblock = parentTry.getParent(CtBlock.class);
						int indexTry = pblock.getStatements().indexOf(parentTry);
						if (indexTry >= 0 && indexTry + 1 < pblock.getStatements().size()) {
							CtStatement stNext = pblock.getStatements().get(indexTry + 1);
							boolean covered = isCovered(mapCacheSuspicious, stNext, parentClass, parentClass,
									testMethodModel);
							if (covered) {
								aMissAssertion.setFp(true);
								continue;
							}
						}

					}
				}
			}
		}

	}
}