package com.jpatch.afw.ui;

import java.util.*;

import com.jpatch.afw.attributes.*;

public class AttributeManager {
	private final Set<Attribute> locks = new HashSet<Attribute>();
	private final Map<DoubleAttr, DoubleMaximum> upperLimits = new HashMap<DoubleAttr, DoubleMaximum>();
	private final Map<DoubleAttr, DoubleMaximum> lowerLimits = new HashMap<DoubleAttr, DoubleMaximum>();
	
	/**
	 * Locks the specified attribute
	 * @param attr the attribute to be locked
	 * @throws IllegalArgumentException if the specified attribute has already been locked
	 */
	public void lock(Attribute attr) {
		attr.addAttributePreChangeListener(Lock.getInstance());		// throws IllegalArgumentException if already locked
		locks.add(attr);
	}
	
	/**
	 * Unlocks the specified attribute
	 * @param attr the attribute to be unlocked
	 * @throws IllegalArgumentException if the specified attribute has not been locked
	 */
	public void unlock(Attribute attr) {
		attr.removeAttributePreChangeListener(Lock.getInstance());	// throws IllegalArgumentException if already locked
		locks.remove(attr);
	}
	
	/**
	 * Returns true if the specified attribute is locked, false otherwise
	 * @param attr the attribute to check for a lock
	 * @return true if the specified attribute is locked, false otherwise
	 */
	public boolean isLocked(Attribute attr) {
		return locks.contains(attr);
	}
	
	/**
	 * Sets the upper limit for the specified attribute
	 * @param attr the attribute to be bounded
	 * @param limit the upper limit for the specified attribute
	 * @return the upper limit for the specified attribute
	 * @throws IllegalArgumentException if the specified attribute already has an upper limit set
	 */
	public DoubleMaximum setUpperLimit(DoubleAttr attr, DoubleMaximum limit) {
		if (upperLimits.containsKey(attr)) {
			throw new IllegalStateException(attr + " has already set an upper limit: " + upperLimits.get(attr));
		}
		attr.addAttributePreChangeListener(limit);
		return limit;
	}
	
	/**
	 * Clears the upper limit for the specified attribute
	 * @param attr the attribute whose upper limit should be cleared
	 * * @throws IllegalArgumentException if the specified attribute does not have an upper limit set
	 */
	public void clearUpperLimit(DoubleAttr attr) {
		DoubleLimit limit = upperLimits.get(attr);
		if (limit == null) {
			throw new IllegalStateException(attr + " has not set an upper limit");
		}
		attr.removeAttributePreChangeListener(limit);
	}
	
	/**
	 * Sets the lower limit for the specified attribute
	 * @param attr the attribute to be bounded
	 * @param limit the lower limit for the specified attribute
	 * @return the lower limit for the specified attribute
	 * @throws IllegalArgumentException if the specified attribute already has an lower limit set
	 */
	public DoubleMaximum setLowerLimit(DoubleAttr attr, DoubleMaximum limit) {
		if (lowerLimits.containsKey(attr)) {
			throw new IllegalStateException(attr + " has already set an lower limit: " + lowerLimits.get(attr));
		}
		attr.addAttributePreChangeListener(limit);
		return limit;
	}
	
	/**
	 * Clears the lower limit for the specified attribute
	 * @param attr the attribute whose lower limit should be cleared
	 * * @throws IllegalArgumentException if the specified attribute does not have an lower limit set
	 */
	public void clearLowerLimit(DoubleAttr attr) {
		DoubleLimit limit = lowerLimits.get(attr);
		if (limit == null) {
			throw new IllegalStateException(attr + " has not set an lower limit");
		}
		attr.removeAttributePreChangeListener(limit);
	}
}
