package com.jpatch.afw.attributes;

public abstract class AbstractAttribute<T> implements Attribute{
	protected AttributeListener<T>[] attributeListeners = new AttributeListener[0];
	protected boolean fireEvents = true;
	
	/**
     * Adds a AttributeListener to this attribute.
     *
     * @param l the AttributeListener to add
     * @see #fireAttributeChanged
     * @see #removeAttributeListener
     */
    public void addAttributeListener(AttributeListener l) {
    	if (l == null) {
    		throw new NullPointerException();
    	}
		for (AttributeListener al : attributeListeners) {
			if (al == l) {
				throw new IllegalArgumentException("AttributeListener " + l + " has already been added to " + this);
			}
		}
    	int i = attributeListeners.length;
    	AttributeListener[] tmp = new AttributeListener[i + 1];
 	    System.arraycopy(attributeListeners, 0, tmp, 0, i);
 	    tmp[i] = l;
 	    attributeListeners = tmp;
    }
    
    /**
     * Removes a AttributeListener from this attribute.
     *
     * @param l the AttributeListener to remove
     * @see #fireAttributeChanged
     * @see #addAttributeListener
     */
    public void removeAttributeListener(AttributeListener l) {
    	int i = 0;
    	while (i < attributeListeners.length && attributeListeners[i] != l) {
    		i++;
    	}
    	if (i < attributeListeners.length) {
    		AttributeListener[] tmp = new AttributeListener[attributeListeners.length - 1];
    	    // Copy the list up to i
    	    System.arraycopy(attributeListeners, 0, tmp, 0, i);
    	    // Copy from one past the index, up to
    	    // the end of tmp (which is one element
    	    // shorter than the old list)
    	    if (i < tmp.length)
    	    	System.arraycopy(attributeListeners, i + 1, tmp, i, tmp.length - i);
    	    // set the listener array to the new array
    	    attributeListeners = tmp;
    	} else {
    		if (l == null) {
    			throw new NullPointerException();
    		}
    	}
    }
    
    public void suppressChangeNotification(boolean suppress) {
    	fireEvents = !suppress;
    }
    
    protected boolean fireAttributeWillChange(boolean value) {
    	for (int i = attributeListeners.length - 1; i >= 0; i--) {
	    	value = attributeListeners[i].attributeWillChange(this, value);
    	}
    	return value;
    }
    
    protected int fireAttributeWillChange(int value) {
    	for (int i = attributeListeners.length - 1; i >= 0; i--) {
	    	value = attributeListeners[i].attributeWillChange(this, value);
    	}
    	return value;
    }
    
    protected double fireAttributeWillChange(double value) {
    	for (int i = attributeListeners.length - 1; i >= 0; i--) {
	    	value = attributeListeners[i].attributeWillChange(this, value);
    	}
    	return value;
    }
    
    protected T fireAttributeWillChange(T value) {
    	for (int i = attributeListeners.length - 1; i >= 0; i--) {
	    	value = attributeListeners[i].attributeWillChange(this, value);
    	}
    	return value;
    }
    
    protected void fireAttributeHasChanged() {
    	if (fireEvents) {
    		fireEvents = false;
	    	for (int i = attributeListeners.length - 1; i >= 0; i--) {
		      	attributeListeners[i].attributeHasChanged(this);
		    }
	    	fireEvents = true;
    	}
    }
}
