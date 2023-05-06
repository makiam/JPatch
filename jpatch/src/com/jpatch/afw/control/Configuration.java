package com.jpatch.afw.control;

import java.util.HashMap;
import java.util.Map;

public class Configuration {
	private static Configuration INSTANCE;
	
	private final Map<String, Object> map = new HashMap<>();
	
	public static Configuration getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Configuration();
		}
		return INSTANCE;
	}
	
	public void put(String key, Object object) {
		map.put(key, object);
	}
	
	public Object get(String key) {
		return map.get(key);
	}
	
	public String getString(String key) {
		return (String) map.get(key);
	}
}
