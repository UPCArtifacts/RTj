package fr.inria.jtanre.rt.core;

import java.util.List;

import fr.inria.astor.core.manipulation.MutationSupporter;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.LineFilter;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SpoonProgramModel extends ProgramModel<CtType<?>, CtExecutable<?>, CtStatement> {

	@Override
	public List<CtType<?>> getAllClasses() {
		return MutationSupporter.getFactory().Class().getAll();

	}

	@Override
	public boolean existsClass(String aNameOfTestClass) {
		CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(aNameOfTestClass);
		return aTestModelCtClass != null;
	}

	@Override
	public CtType<?> getClass(String aNameOfTestClass) {
		CtClass aTestModelCtClass = MutationSupporter.getFactory().Class().get(aNameOfTestClass);
		return aTestModelCtClass;
	}

	@Override
	public CtExecutable<?> getTestMethod(CtType<?> testClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CtStatement> getStatementsFromMethod(CtExecutable<?> testMethodModel) {
		List<CtStatement> allStmtsFromClass = testMethodModel.getElements(new LineFilter());
		return allStmtsFromClass;
	}

}
