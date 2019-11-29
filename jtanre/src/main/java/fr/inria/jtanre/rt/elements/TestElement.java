package fr.inria.jtanre.rt.elements;

import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class TestElement {

	private boolean fp = false;

	public String label;

	boolean insideAnIf = false;

	public TestElement(String label) {
		super();
		this.label = label;
	}

	public boolean isFp() {
		return fp;
	}

	public void setFp(boolean fp) {
		this.fp = fp;
	}

	public abstract CtElement getElement();

	public boolean isInsideAnIf() {
		return insideAnIf;
	}

	public void setInsideAnIf(boolean insideAnIf) {
		this.insideAnIf = insideAnIf;
	}
}