package com.jpatch.afw.attributes;

public class AbstractAttribute implements Attribute {
	/**
	 * An array holding the AttributePostChangeListener of this AbstractAttributes.
	 * Can be null (i.e. will be set to null if the list is empty)
	 */
	private AttributePostChangeListener[] attributePostChangeListeners = null;

	/**
	 * Whether or not to fire "attributeHasChanged" events (post-change-notifications)
	 */
	boolean fireEvents = true;
	
	/**
     * Adds an addAttributePostChangeListener to this attribute.
     * <br /><b>Note:</b> The attributeHasChanged method of the specified listener is called immediately in order
     * to notify the listener of the current value of this Attribute.
     * @param l the addAttributePostChangeListener to add
     * @see #removeAttributePostChangeListener
     * @throws NullPointerException if the specified argument is null
     * @throws IllegalArgumentException if the specified addAttributePostChangeListener has already been added to this AbstractAttribute
     */
    public void addAttributePostChangeListener(AttributePostChangeListener l) {
//    	System.out.println("addAttributePostChangeListener " + l);
    	if (l == null) {
    		throw new NullPointerException();
    	}
    	if (attributePostChangeListeners != null) {
    		for (AttributePostChangeListener al : attributePostChangeListeners) {
    			if (al == l) {
    				throw new IllegalArgumentException(l + " has already been added to " + this);
    			}
    		}
		}
    	int i = attributePostChangeListeners == null ? 0 : attributePostChangeListeners.length;
    	AttributePostChangeListener[] tmp = new AttributePostChangeListener[i + 1];
    	if (i > 0) {
    		System.arraycopy(attributePostChangeListeners, 0, tmp, 0, i);
    	}
 	    tmp[i] = l;
 	    attributePostChangeListeners = tmp;
 	    
 	    /*
 	     * notify the new listener about the current value
 	     * TODO: is this really a good idea?
 	     */
 	    fireEvents = false;
 	    l.attributeHasChanged(this);
 	    fireEvents = true;
    }
    
    /**
     * Removes an AttributePostChangeListener from this attribute.
     * If the specified AttributePostChangeListener is not on the listener-list of this AbstractAttribute
     * calling this method has no effect whatsoever.
     * @param l the AttributePostChangeListener to remove
     * @see #addAttributePostChangeListener
     */
    public void removeAttributePostChangeListener(AttributePostChangeListener l) {
//    	System.out.println("removeAttributePostChangeListener " + l);
    	if (attributePostChangeListeners == null) {
    		return; 								// listener list is empty, ignore silently and return
    	}
    	if (attributePostChangeListeners.length == 1 && attributePostChangeListeners[0] == l) {
    		attributePostChangeListeners = null;		// the last listener is being removed, set list to null
    		return;
    	}
    	int i = 0;
    	while (i < attributePostChangeListeners.length && attributePostChangeListeners[i] != l) {
    		i++;
    	}
    	if (i < attributePostChangeListeners.length) {
    		AttributePostChangeListener[] tmp = new AttributePostChangeListener[attributePostChangeListeners.length - 1];
    	    // Copy the list up to i
    	    System.arraycopy(attributePostChangeListeners, 0, tmp, 0, i);
    	    // Copy from one past the index, up to
    	    // the end of tmp (which is one element
    	    // shorter than the old list)
    	    if (i < tmp.length)
    	    	System.arraycopy(attributePostChangeListeners, i + 1, tmp, i, tmp.length - i);
    	    // set the listener array to the new array
    	    attributePostChangeListeners = tmp;
    	}
    }
    
    /**
     * Toggles suppression of change notifications
     * @param suppress true to suppression, false otherwise
     */
    public void suppressChangeNotification(boolean suppress) {
    	fireEvents = !suppress;
    }
    
    /**
     * Calls the attributeHasChanged method of each registered attributePostChangeListener.
     */
    protected final void fireAttributeHasChanged() {
    	if (fireEvents && attributePostChangeListeners != null) {
    		fireEvents = false;
	    	for (int i = 0; i < attributePostChangeListeners.length; i++) {
	    		attributePostChangeListeners[i].attributeHasChanged(this);
		    }
	    	fireEvents = true;
    	}
    }
    
    public void dumpListeners() {
    	System.out.println(this + " postChangeListeners:");
    	if (attributePostChangeListeners != null) {
    		for (AttributePostChangeListener l : attributePostChangeListeners) {
    			System.out.println("    " + l);
    		}
    	}
    }
}
