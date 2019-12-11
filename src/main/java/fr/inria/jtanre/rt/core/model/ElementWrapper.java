package fr.inria.jtanre.rt.core.model;

public class ElementWrapper<T> {

	T element = null;

	public ElementWrapper(T element) {
		this.element = element;
	}

	public T getElement() {
		return element;
	}

	public void setElement(T element) {
		this.element = element;
	}

}
