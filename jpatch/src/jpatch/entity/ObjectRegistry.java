package jpatch.entity;

import java.util.*;

public class ObjectRegistry implements AttributeListener {
	private Map<Class, Map<String, AbstractNamedObject>> classNameMap = new HashMap<Class, Map<String, AbstractNamedObject>>();
	private Map<Class, Map<AbstractNamedObject, String>> classObjectMap = new HashMap<Class, Map<AbstractNamedObject, String>>();
	private Map<Attribute.String, AbstractNamedObject> attributeMap = new HashMap<Attribute.String, AbstractNamedObject>();
	
	/**
	 * Adds a JPatchObject to the maps. This method also adds an AttributeListener to
	 * the object's name attribute to track name changes. If the name is already occupied,
	 * an IllegalArgumentException is thrown.
	 * @param object
	 */
	public void add(AbstractNamedObject object) {
		if (object.getObjectRegistry() != null) {
			throw new IllegalStateException();
		}
		insert(object);
		Attribute.String nameAttribute = object.name;
		attributeMap.put(nameAttribute, object);
		nameAttribute.addAttributeListener(this);
	}
	
	/**
	 * Removes the JPatchObject from the maps. This method also removes the AttributeListener on
	 * the object's name attribute.
	 * @param object
	 */
	public void remove(AbstractNamedObject object) {
		removeFromMap(object);
		Attribute.String nameAttribute = object.name;
		nameAttribute.removeAttributeListener(this);
		attributeMap.remove(nameAttribute);
	}
	
	/**
	 * Gets an object by it's class and name.
	 * @param objectClass the class of the requested object
	 * @param name the name of the requested object
	 * @return the object with the passed class and name, or null if it was not found
	 */
	public Object getObject(Class objectClass, String name) {
		Map<String, AbstractNamedObject> nameMap = classNameMap.get(objectClass);
		if (nameMap == null) {
			return null;
		}
		return nameMap.get(name);
	}
	
	/**
	 * Gets an unoccupied name for a class. This method will check if the name &lt;newName&gt;#1 is occupied
	 * and return it if it is not. If it is, it continues to search with &lt;newName&gt;#2, &lt;newName&gt;#3, etc.
	 * until it finds a free name.
	 * @param objectClass the class of the requested object
	 * @param newName the base name to start the search with
	 * @return an unoccupied name for the specified class
	 */
	public String getNextName(Class objectClass, String newName) {
		Map<String, AbstractNamedObject> nameMap = classNameMap.get(objectClass);
		if (nameMap == null) {
			return newName + "#1";
		}
		int i = 1;
		while (nameMap.containsKey(newName + "#" + i)) {
			i++;
		}
		return newName + "#" + i;
	}
	
	/*
	 * (non-Javadoc) AttributeListener implementation.
	 * If the name of an object changes, remove it from the maps and add it again.
	 * @see jpatch.entity.AttributeListener#attributeChanged(jpatch.entity.Attribute)
	 */
	public void attributeHasChanged(Attribute attribute) {
		AbstractNamedObject object = attributeMap.get(attribute);
		removeFromMap(object);
		insert(object);
	}
	
	/*
	 * Check if the name is unoccupied and add the object to the maps.
	 * If no maps for the class exist, create them.
	 * Throws an IllegalArgumentException if the name is already occupied.
	 */
	private void insert(AbstractNamedObject object) {
		Map<String, AbstractNamedObject> nameMap = classNameMap.get(object.getClass());
		Map<AbstractNamedObject, String> objectMap = classObjectMap.get(object.getClass());
		if (nameMap == null) {
			nameMap = new HashMap<String, AbstractNamedObject>();
			objectMap = new HashMap<AbstractNamedObject, String>();
			classNameMap.put(object.getClass(), nameMap);
			classObjectMap.put(object.getClass(), objectMap);
		}
		if (nameMap.containsKey(object.getName())) {
			throw new IllegalArgumentException(object.getClass().getName() + " " + object.getName() + ": an object of that class with the same name already exists");
		} else {
			nameMap.put(object.getName(), object);
			objectMap.put(object, object.getName());
		}
	}

	/*
	 * Removes the object from all maps
	 */
	private void removeFromMap(AbstractNamedObject object) {
		Map<String, AbstractNamedObject> nameMap = classNameMap.get(object.getClass());
		Map<AbstractNamedObject, String> objectMap = classObjectMap.get(object.getClass());
		nameMap.remove(objectMap.get(object));
		objectMap.remove(object);
	}
	
}
