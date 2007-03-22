package jpatch.entity.attributes2;

public abstract class AbstractAttribute implements Attribute {
	protected AttributeListener[] attributeListeners = new AttributeListener[0];
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
    		} else {
    			throw new IllegalArgumentException("AttributeListener " + l + " is not bound to " + this);
    		}
    	}
    }
    
    public void suppressChangeNotification(boolean suppress) {
    	fireEvents = !suppress;
    }
    
    protected void fireAttributeChanged() {
    	if (fireEvents) {
    		fireEvents = false;
	    	for (int i = attributeListeners.length - 1; i >= 0; i--) {
		      	attributeListeners[i].attributeChanged(this);
		    }
	    	fireEvents = true;
    	}
    }
}
