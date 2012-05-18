package com.senstore.alice.utils;

import java.util.Hashtable;

public class Registry {

	private static Registry instance;
	private Hashtable<String, Object> appContext;

	private Registry() {
		appContext = new Hashtable<String, Object>();

	}

	public static Registry instance() {
		if (instance != null) {
			return instance;
		} else {
			instance = new Registry();
			return instance;
		}
	}

	public void put(String key, Object value) {
		appContext.put(key, value);
	}

	public void remove(String key) {
		appContext.remove(key);
	}

	public Object get(String key) {
		return appContext.get(key);
	}

}
