package com.jpatch.afw.control;

import java.util.*;
import java.util.regex.*;

public final class ObjectRegistry<T> {
	private static final Pattern pattern = Pattern.compile("^(.*)\\((\\d+)\\)$");
	
	private final Map<String, T> nameMap = new HashMap<String, T>();
	private final Map<Object, String> objectMap;
	
	public ObjectRegistry(Map<Object, String> objectMap) {
		this.objectMap = objectMap;
	}
	
	public boolean isOccupied(String name) {
		return nameMap.containsKey(name);
	}
	
	public T getObjectByName(String name) {
		return nameMap.get(name);
	}
	
	public String getObjectName(Object object) {
		return objectMap.get(object);
	}
	
	public String getValidName(String name) {
		if (isOccupied(name)) {
			Matcher matcher = pattern.matcher(name);
			if (matcher.matches()) {
				String prefix = matcher.group(1);
				int number = Integer.parseInt(matcher.group(2));
				while (isOccupied(name)) {
					name = prefix + "(" + (number++) + ")";
				}
			} else {
				return getValidName(name + "(1)");
			}
		}
		return name;
	}
	
	public String addObject(T object, String name) {
		name = getValidName(name);
		add(object, name);
		return name;
	}
	
	private void add(T object, String name) {
		if (objectMap.containsKey(object)) {
			throw new IllegalArgumentException(object + " has already been added to " + this + " (by object named \"" + objectMap.get(object) + "\")");
		}
		if (nameMap.containsKey(name)) {
			throw new IllegalArgumentException(name + " is already occupied in " + this + " (by object " + nameMap.get(name) + ")");
		}
		objectMap.put(object, name);
		nameMap.put(name, object);
	}
	
	public static void main(String[] args) {
		ObjectRegistry or = new ObjectRegistry(new IdentityHashMap<Object, String>());
		or.add("o1", "name a");
		or.add("o2", "name b");
		or.add("o3", "name c");
		or.add("o4", "name c(1)");
		System.out.println(or.getValidName("name"));
		System.out.println(or.getValidName("name c"));
//		System.out.println(or.getValidName("name"));
	}
}