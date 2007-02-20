package jpatch.entity.attributes;

import java.lang.reflect.Array;
import java.util.*;

public abstract class AbstractAttribute<T extends Constraint> implements AttributeListener {
	private AttributeListener[] attributeListeners;
	private T[] constraints;
	
	/**
     * Adds a AttributeListener to this attribute.
     *
     * @param l the AttributeListener to add
     * @see #fireAttributeChanged
     * @see #removeAttributeListener
     */
    public void addAttributeListener(AttributeListener l) {
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
    		throw new IllegalArgumentException("AttributeListener " + l + " is not bound to " + this);
    	}
    }
    
    public void addConstraint(T c) {
    	int i = 0;
    	while (i < constraints.length && c.priority.getInt() < constraints[i].priority.getInt()) {
    		if (constraints[i] == c) {
    			throw new IllegalArgumentException("Constraint " + c + " has already been added to " + this);
    		}
    		i++;
    	}
    	T[] tmp = (T[]) Array.newInstance(c.getClass() , constraints.length + 1);
    	System.arraycopy(constraints, 0, tmp, 0, i);
    	tmp[i] = c;
    	System.arraycopy(constraints, i, tmp, i + 1, constraints.length - i);
    	constraints = tmp;
    	c.priority.addAttributeListener(this);
    }
    
    public void removeConstraint(T c) {
    	int i = 0;
    	while (i < constraints.length && constraints[i] != c) {
    		i++;
    	}
    	if (i < constraints.length) {
    		T[] tmp = (T[]) Array.newInstance(c.getClass(), constraints.length - 1);
    	    // Copy the list up to i
    	    System.arraycopy(constraints, 0, tmp, 0, i);
    	    // Copy from one past the index, up to
    	    // the end of tmp (which is one element
    	    // shorter than the old list)
    	    if (i < tmp.length)
    	    	System.arraycopy(constraints, i + 1, tmp, i, tmp.length - i);
    	    // set the listener array to the new array
    	    constraints = tmp;
    	} else {
    		throw new IllegalArgumentException("Constraint " + c + " is not bound to " + this);
    	}
    }
    
    public void attributeChanged(AbstractAttribute a) {
    	Arrays.sort(constraints);
    }
    
    protected void fireAttributeChanged() {
    	for (int i = attributeListeners.length - 1; i >= 0; i--) {
	      	attributeListeners[i].attributeChanged(this);
	    }
    }
}
