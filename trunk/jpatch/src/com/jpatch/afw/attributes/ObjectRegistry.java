package com.jpatch.afw.attributes;

import com.jpatch.afw.attributes.*;

import java.util.*;
import java.util.regex.*;

/**
 * Manages <i>NamedObjects</i> and ensures that all objects managed by a
 * certain <i>ObjectRegistry</i> always have unique names. It also provieds
 * name to object resulution with the <i>getObjectByName</i> method.
 * @param <T> the class of objects to be managed (must be an implementation of <i>NamedObject</i>)
 */
public final class ObjectRegistry<T extends NamedObject> {
	/**
	 * Pattern for matching sequence numbers of otherwise equal names
	 */
	private static final Pattern pattern = Pattern.compile("^(.*)\\((\\d+)\\)$");
	
	/**
	 * Stores name to object mappings
	 */
	private final Map<String, T> nameMap = new HashMap<String, T>();
	
	/**
	 * Stores (Name)Attribute to object mappings
	 */
	private final Map<GenericAttr<String>, T> attributeMap = new HashMap<GenericAttr<String>, T>();

	/**
	 * Ensures that an object can't change its name to an already occupied name.
	 * This Listener is added to all managed objects (upon calling the add(T, String) method).
	 */
	private final AttributePreChangeListener<String> preChangeListener = new AttributePreChangeAdapter<String>() {
		@Override
		public String attributeWillChange(ScalarAttribute source, String value) {
			/* 
			 * Remove the name from the name map. It will be added again by the
			 * postChangeListener below
			 */
			nameMap.remove(((GenericAttr<String>) source).getValue());
			return getValidName(value);
		}
	};
	
	/**
	 * Updates the nameMap whenever an object changes its name.
	 * This Listener is added to all managed objects (upon calling the add(T, String) method).
	 */
	private final AttributePostChangeListener postChangeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			GenericAttr<String> nameAttr = (GenericAttr<String>) source;
			T object = attributeMap.get(source);
			nameMap.put(nameAttr.getValue(), object);
		}
	};
	
	/**
	 * Returns true if the specified name is occupied, false otherwise
	 * @param name the name to check
	 * @return true if the specified name is occupied, false otherwise
	 */
	public boolean isOccupied(String name) {
		return nameMap.containsKey(name);
	}
	
	/**
	 * Returns the object with the specified name, or null if no object with such a name is managed by
	 * this ObjectRegistry.
	 * @param name the name of the requested object 
	 * @return the object with the specified name, or null if no object with such a name is managed by
	 * this ObjectRegistry.
	 */
	public T getObjectByName(String name) {
		return nameMap.get(name);
	}
	
	/**
	 * Returns a valid name. If the specified name is not occupied, it is returned. Otherwise it's suffixed
	 * with (1), unless this name is also occupied, in which case it's suffixed with (2), and so on
	 * @param name the name to check
	 * @return a valid name, based on the specified name
	 */
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
	
	/**
	 * Adds the specified object to this ObjectRegistry.
	 * If the name of the specified object is already occupied by another managed object,
	 * it will be changed to a valid name.
	 * @param object the object to add.
	 * @return the name of the object (which might have been changed be this method)
	 */
	public String addObject(T object) {
		String validName = getValidName(object.getNameAttribute().getValue());
		object.getNameAttribute().setValue(validName);
		add(object, validName);
		return validName;
	}
	
	/**
	 * Removes the specified object from this ObjectRegistry. All listeners that have been added
	 * to this object <i>by this ObjectRegistry</i> will be removed.
	 * @param object the object to remove.
	 */
	public void removeObject(T object) {
		nameMap.remove(object.getNameAttribute().getValue());
		attributeMap.remove(object.getNameAttribute());
		object.getNameAttribute().removeAttributePreChangeListener(preChangeListener);
		object.getNameAttribute().removeAttributePostChangeListener(postChangeListener);
	}
	
	/**
	 * Checks wheter the name is not occupied, and if not adds the object to the nameMap and
	 * adds pre- and postChangeListeners to it.
	 * @param object the object to add
	 * @param name the name of the object
	 * @throws IllegalArgumentException if the name is already occupied
	 */
	private void add(T object, String name) {
		if (nameMap.containsKey(name)) {
			throw new IllegalArgumentException(name + " is already occupied in " + this + " (by object " + nameMap.get(name) + ")");
		}
		nameMap.put(name, object);
		object.getNameAttribute().addAttributePreChangeListener(preChangeListener);
		object.getNameAttribute().addAttributePostChangeListener(postChangeListener);
	}
}
