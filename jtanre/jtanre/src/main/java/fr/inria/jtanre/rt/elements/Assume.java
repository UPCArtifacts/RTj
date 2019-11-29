package fr.inria.jtanre.rt.elements;

/**
 * 
 */
import spoon.reflect.code.CtInvocation;

public class Assume extends AsAssertion {

	boolean executedAndTrue = false;

	public Assume(CtInvocation assertion) {
		super("Assume", assertion);

	}

	public boolean isExecutedAndTrue() {
		return executedAndTrue;
	}

	public void setExecutedAndTrue(boolean executedAndTrue) {
		this.executedAndTrue = executedAndTrue;
	}
}