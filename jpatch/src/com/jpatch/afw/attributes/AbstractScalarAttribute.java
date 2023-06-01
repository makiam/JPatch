package com.jpatch.afw.attributes;

/**
 * The base class for all Attributes in this package.
 * It provides a full implementation of the <i>Attribute</i> interface, manages
 * the listener lists and provides methods to fire event notifications.
 * 
 * @author sascha
 *
 * @param <T>
 */
public abstract class AbstractScalarAttribute<T> extends AbstractAttribute implements ScalarAttribute {
	/**
	 * An array holding the AttributePreChangeListeners of this AbstractAttributes.
	 * Can be null (i.e. will be set to null if the list is empty)
	 */
	private AttributePreChangeListener<T>[] attributePreChangeListeners = null;
	
	
	/**
     * Adds an AttributePreChangeListener to this attribute.
     *
     * @param l the AttributePreChangeListener to add
     * @see #removeAttributePreChangeListener
     * @throws NullPointerException if the specified argument is null
     * @throws IllegalArgumentException if the specified AttributePreChangeListener has already been added to this AbstractAttribute
     */
        @Override
    public void addAttributePreChangeListener(AttributePreChangeListener l) {
    	if (l == null) {
    		throw new NullPointerException();
    	}
    	if (attributePreChangeListeners != null) {
    		for (AttributePreChangeListener al : attributePreChangeListeners) {
    			if (al == l) {
    				throw new IllegalArgumentException(l + " has already been added to " + this);
    			}
    		}
		}
    	int i = attributePreChangeListeners == null ? 0 : attributePreChangeListeners.length;
    	AttributePreChangeListener[] tmp = new AttributePreChangeListener[i + 1];
    	if (i > 0) {
    		System.arraycopy(attributePreChangeListeners, 0, tmp, 0, i);
    	}
 	    tmp[i] = l;
 	    attributePreChangeListeners = tmp;
    }
    
    
    /**
     * Removes an AttributePreChangeListener from this attribute.
     * If the specified AttributePreChangeListener is not on the listener-list of this AbstractAttribute
     * calling this method has no effect whatsoever.
     * @param l the AttributePreChangeListener to remove
     * @see #addAttributePreChangeListener
     */
        @Override
    public void removeAttributePreChangeListener(AttributePreChangeListener l) {
    	if (attributePreChangeListeners == null) {
    		return; 								// listener list is empty, ignore silently and return
    	}
    	if (attributePreChangeListeners.length == 1 && attributePreChangeListeners[0] == l) {
    		attributePreChangeListeners = null;		// the last listener is being removed, set list to null
    		return;
    	}
    	int i = 0;
    	while (i < attributePreChangeListeners.length && attributePreChangeListeners[i] != l) {
    		i++;
    	}
    	if (i < attributePreChangeListeners.length) {
    		AttributePreChangeListener[] tmp = new AttributePreChangeListener[attributePreChangeListeners.length - 1];
    	    // Copy the list up to i
    	    System.arraycopy(attributePreChangeListeners, 0, tmp, 0, i);
    	    // Copy from one past the index, up to
    	    // the end of tmp (which is one element
    	    // shorter than the old list)
    	    if (i < tmp.length)
    	    	System.arraycopy(attributePreChangeListeners, i + 1, tmp, i, tmp.length - i);
    	    // set the listener array to the new array
    	    attributePreChangeListeners = tmp;
    	}
    }
    
    
    
    
    
    /**
     * Calls the attributeWillChange method of each registered attributePreChangeListener,
     * starting with the specified value. The value returned by the first listener is used
     * when calling the attributeWillChange method of the second listener, and so on.
     * Finally this method returnes the value returned by the last listener.
     * @param value the value this Attribute is about to change to.
     * @return the value after all registered attributePreChangeListener have been asked for veto.
     */
    protected final boolean fireAttributeWillChange(boolean value) {
    	if (attributePreChangeListeners != null) {
    		for (int i = 0; i < attributePreChangeListeners.length; i++) {
    			value = attributePreChangeListeners[i].attributeWillChange(this, value);
    		}
    	}
    	return value;
    }
    
    /**
     * Calls the attributeWillChange method of each registered attributePreChangeListener,
     * starting with the specified value. The value returned by the first listener is used
     * when calling the attributeWillChange method of the second listener, and so on.
     * Finally this method returnes the value returned by the last listener.
     * @param value the value this Attribute is about to change to.
     * @return the value after all registered attributePreChangeListener have been asked for veto.
     */
    protected final  int fireAttributeWillChange(int value) {
    	if (attributePreChangeListeners != null) {
    		for (int i = 0; i < attributePreChangeListeners.length; i++) {
    			value = attributePreChangeListeners[i].attributeWillChange(this, value);
    		}
    	}
    	return value;
    }
    
    /**
     * Calls the attributeWillChange method of each registered attributePreChangeListener,
     * starting with the specified value. The value returned by the first listener is used
     * when calling the attributeWillChange method of the second listener, and so on.
     * Finally this method returnes the value returned by the last listener.
     * @param value the value this Attribute is about to change to.
     * @return the value after all registered attributePreChangeListener have been asked for veto.
     */
    protected final  double fireAttributeWillChange(double value) {
    	if (attributePreChangeListeners != null) {
    		for (int i = 0; i < attributePreChangeListeners.length; i++) {
    			value = attributePreChangeListeners[i].attributeWillChange(this, value);
    		}
    	}
    	return value;
    }
    
    /**
     * Calls the attributeWillChange method of each registered attributePreChangeListener,
     * starting with the specified value. The value returned by the first listener is used
     * when calling the attributeWillChange method of the second listener, and so on.
     * Finally this method returnes the value returned by the last listener.
     * @param value the value this Attribute is about to change to.
     * @return the value after all registered attributePreChangeListener have been asked for veto.
     */
    protected final  T fireAttributeWillChange(T value) {
    	if (attributePreChangeListeners != null) {
    		for (int i = 0; i < attributePreChangeListeners.length; i++) {
    			value = attributePreChangeListeners[i].attributeWillChange(this, value);
    		}
    	}
    	return value;
    }
    
    /**
     * Debugging method
     */
    @Override
    public void dumpListeners() {
    	System.out.println(this + " preChangeListeners:");
    	if (attributePreChangeListeners != null) {
	    	for (AttributePreChangeListener l : attributePreChangeListeners) {
	    		System.out.println("    " + l);
	    	}
    	}
    	super.dumpListeners();
    }
}
