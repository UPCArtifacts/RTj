package fr.inria.jtanre.rt.core;

import java.util.List;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class ProgramModel<T, M, S> {

	public abstract java.util.List<T> getAllClasses();

	public abstract boolean existsClass(String aNameOfTestClass);

	public abstract T getClass(String aNameOfTestClass);

	public abstract M getTestMethod(T testClass);

	public abstract List<S> getStatementsFromMethod(M testClass);
}
