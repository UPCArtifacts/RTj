package fr.inria.jtanre.rt.core;

import java.util.HashMap;

public class ResultMap<V> extends HashMap<String, V> {

	public V get(Class xclass) {

		String key = xclass.getSimpleName();
		return super.get(key);
	}

	public void put(Class xclass, V value) {

		String key = xclass.getSimpleName();
		super.put(key, value);
	}
}
