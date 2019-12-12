package fr.inria.jtanre.rt.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.inria.astor.core.faultlocalization.entity.SuspiciousCode;
import fr.inria.jtanre.rt.core.Classification;
import fr.inria.jtanre.rt.elements.AsAssertion;
import fr.inria.jtanre.rt.elements.Helper;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.LineFilter;

public abstract class HelperProcessor extends ElementProcessor<Helper, Helper> {

	@Override
	public List<Helper> findElements(Map<String, List<?>> previousPartialResults, List<CtStatement> stmts,
			CtExecutable testMethodModel, List<CtClass> allClasses) {
		return filterHelper(stmts, new ArrayList());
	}

	/**
	 * A helper must have an invocation
	 * 
	 * @param allStmtsFromClass
	 * @param testMethodModel
	 * @return
	 */
	private List<Helper> filterHelper(List<CtStatement> allStmtsFromClass, List<CtExecutable> calls) {
		List<Helper> helpersMined = new ArrayList<>();
		// for each statement, let's find which one is a helper
		for (CtStatement targetElement : allStmtsFromClass) {
			if (targetElement instanceof CtInvocation) {
				CtInvocation targetInvocation = (CtInvocation) targetElement;
				// a helper must be an Invocation to a something different to assertion
				if (!isAssertion(targetInvocation) && targetInvocation.getExecutable() != null
						&& targetInvocation.getExecutable().getDeclaration() != null) {

					// Let's find the called method to see if it has assertions
					CtExecutable methodDeclaration = targetInvocation.getExecutable().getDeclaration();

					if (methodDeclaration.getBody() == null) {
						continue;
					}

					if (calls.contains(methodDeclaration)) {

						continue;
					}
					// All the statements from the method that is invoked
					List<CtStatement> statementsFromMethod = methodDeclaration.getBody().getElements(new LineFilter());

					// We filter the assertion present in the method body
					List<CtInvocation> assertionsFromMethod = filterInvocation(statementsFromMethod, ASSERT);
					// If the method body has assertions, we add them.
					if (assertionsFromMethod != null && !assertionsFromMethod.isEmpty()) {

						for (CtInvocation assertion : assertionsFromMethod) {
							Helper aHelper = new Helper(new AsAssertion(assertion));
							helpersMined.add(aHelper);
							aHelper.getCalls().add(0, targetInvocation);

						}

					}
					// Now, let's check if the body has a call to another helper.
					try {
						List<CtExecutable> previouscalls = new ArrayList<>(calls);
						previouscalls.add(methodDeclaration);
						// we find if the body calls to another helper, recursively
						List<Helper> helpersFromInvocation = filterHelper(statementsFromMethod, previouscalls);
						// we add to the results
						helpersMined.addAll(helpersFromInvocation);
						// We update the helper to include the calls.
						for (Helper aHelper : helpersFromInvocation) {
							// in the first place to keep the order of the invocation
							aHelper.getCalls().add(0, targetInvocation);
						}
					} catch (Throwable l) {
						System.out.println("error ");
					}
				}
			}

		}
		// Update the distance
		for (Helper aHelper : helpersMined) {

			// Check the distance:

			// Let's get the first invocation from the invocation chain
			CtInvocation statementFirstCall = aHelper.getCalls().get(0);
			// the class where the invocation is written
			CtClass typeInvo = statementFirstCall.getParent(CtClass.class);
			// the class where the invoked method
			CtClass typeMethod = statementFirstCall.getExecutable().getDeclaration().getParent(CtClass.class);

			// if same target, distance is zero
			if (typeInvo == typeMethod) {
				aHelper.setDistance(0);
			} else {
				// if it's subtype, lets count the distance
				if (typeInvo.isSubtypeOf(typeMethod.getReference())) {

					int distance = 0;

					CtType typeToMount = typeInvo;
					while (typeToMount != null && typeToMount != typeMethod) {
						typeToMount = (CtClass) typeToMount.getSuperclass().getDeclaration();
						distance++;
					}
					aHelper.setDistance(distance);
				} else {
					// Not subtype, the method helper is somewhere else.
					aHelper.setDistance(-1);
				}
			}
		}
		return helpersMined;
	}

	protected Classification<Helper> classifyHelpersAssertionExecution(CtClass aTestModelCtClass,
			List<Helper> allHelperInvocationFromTest, Map<String, SuspiciousCode> cacheSuspicious,
			CtExecutable methodTestExecuted, boolean checkAssertion) {

		Classification<Helper> result = new Classification();
		for (Helper aHelper : allHelperInvocationFromTest) {

			CtInvocation assertion = (checkAssertion) ? aHelper.getAssertion().getCtAssertion()
					: aHelper.getCalls().get(0);
			CtClass ctclassFromAssert = assertion.getParent(CtClass.class);

			boolean covered = isCovered(cacheSuspicious, assertion, ctclassFromAssert, aTestModelCtClass,
					methodTestExecuted);

			if (!covered) {
				result.getResultNotExecuted().add(aHelper);
				if (checkAssertion)
					aHelper.unexecutedAssert = true;
				else
					aHelper.unexecutedCall = true;
			} else {
				result.getResultExecuted().add(aHelper);
			}
		}
		return result;
	}

	protected void classifyComplexHelper(List<Helper> resultNotExecutedAssertion, boolean checkAssertion) {
		for (Helper aHelper : resultNotExecutedAssertion) {

			// Helper call
			if (!checkAssertion) {
				// check the first call
				CtInvocation element = aHelper.getCalls().get(0);

				checkInsideIf(aHelper, element);
			} else {
				// first check the assertion
				CtInvocation assertion = aHelper.getAssertion().getCtAssertion();
				boolean assertInsideIf = checkInsideIf(aHelper, assertion);
				if (!assertInsideIf) {
					// check the calls are inside
					for (CtInvocation call : aHelper.getCalls()) {

						boolean callInsideIf = checkInsideIf(aHelper, call);
						if (callInsideIf) {
							return;
						}

					}

				}

			}

		}
	}

	private boolean checkInsideIf(Helper aHelper, CtInvocation element) {
		CtIf parentIf = element.getParent(CtIf.class);
		if (parentIf != null) {
			// complex
			aHelper.setInsideAnIf(true);
			return true;
		} else {
			// not complex
			aHelper.setInsideAnIf(false);
			return false;
		}
	}

}
