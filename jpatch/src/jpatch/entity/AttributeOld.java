package jpatch.entity;

import javax.swing.event.*;

public class AttributeOld<T> {
	public static enum Type { BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BOOLEAN, ENUM, OBJECT };
	private final String name;
	private final ChangeEvent changeEvent = new ChangeEvent(this);
	private T oldValue;
	private T value;
	private T minimum;
	private T maximum;
	private ChangeListener[] changeListeners = new ChangeListener[0];
	private MotionCurveNew motionCurve;
	
	public AttributeOld(String name) {
		this.name = name;
	}
	
	public AttributeOld(String name, T value) {
		this(name);
		this.value = value;
		this.oldValue = value;
	}
	
	public AttributeOld(String name, T value, T minimum, T maximum) {
		this(name, value);
		this.minimum = minimum;
		this.maximum = maximum;
	}
	
	public Type getType() {
		if (value instanceof Byte)
			return Type.BYTE;
		else if (value instanceof Short)
			return Type.SHORT;
		else if (value instanceof Integer)
			return Type.INTEGER;
		else if (value instanceof Long)
			return Type.LONG;
		else if (value instanceof Float)
			return Type.FLOAT;
		else if (value instanceof Double)
			return Type.DOUBLE;
		else if (value instanceof Boolean)
			return Type.BOOLEAN;
		else if (value instanceof Enum)
			return Type.BYTE;
		else
			return Type.OBJECT;
	}
	public T getMaximum() {
		return maximum;
	}
	
	public void setMaximum(T maximum) {
		this.maximum = maximum;
	}
	
	public T getMinimum() {
		return minimum;
	}
	
	public void setMinimum(T minimum) {
		this.minimum = minimum;
	}
	
	public MotionCurveNew getMotionCurve() {
		return motionCurve;
	}
	
	public void setMotionCurve(MotionCurveNew motionCurve) {
		this.motionCurve = motionCurve;
	}
	
	public String getName() {
		return name;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		if (!this.value.equals(value)) {
			this.value = value;
			fireStateChanged();
		}
	}
	
	public void update(double position) {
		double value = motionCurve.getValueAt(position);
		if (this.value instanceof Double) {
			setValue((T) new Double(value));
		} else if (this.value instanceof Boolean) {
			setValue ((T) (value > 0.5 ? Boolean.TRUE : Boolean.FALSE));
		}
	}
	
	public void commitModification() {
		if (!oldValue.equals(value)) {
			System.out.println("commit modification from " + oldValue + " to " + value);	// FIXME: Needs to perform actual edit
			oldValue = value;
		}
	}
	
	/**
     * Adds a ChangeListener to this attribute.
     *
     * @param l the ChangeListener to add
     * @see #fireStateChanged
     * @see #removeChangeListener
     */
    public void addChangeListener(ChangeListener l) {
    	for (ChangeListener cl : changeListeners)
    		if (cl == l)
    			throw new IllegalArgumentException("ChangeListener " + l + " has already been added to " + this);
    	int i = changeListeners.length;
    	ChangeListener[] tmp = new ChangeListener[i + 1];
 	    System.arraycopy(changeListeners, 0, tmp, 0, i);
 	    tmp[i] = l;
 	    changeListeners = tmp;
    }


    /**
     * Removes a ChangeListener from this attribute.
     *
     * @param l the ChangeListener to remove
     * @see #fireStateChanged
     * @see #addChangeListener
     */
    public void removeChangeListener(ChangeListener l) {
    	System.out.println("remove");
    	int index = -1;
    	for (int i = 0; i < changeListeners.length; i++) {
    	    if (changeListeners[i] == l) {
    	    	index = i;
    	    	break;
    	    }
    	}
    	if (index > -1) {
    	    ChangeListener[] tmp = new ChangeListener[changeListeners.length - 1];
    	    // Copy the list up to index
    	    System.arraycopy(changeListeners, 0, tmp, 0, index);
    	    // Copy from one past the index, up to
    	    // the end of tmp (which is one element
    	    // shorter than the old list)
    	    if (index < tmp.length)
    	    	System.arraycopy(changeListeners, index + 1, tmp, index, tmp.length - index);
    	    // set the listener array to the new array or null
    	    changeListeners = tmp;
        }
    }
    
    /**
     * Send a ChangeEvent, whose source is this attribute, to
     * each listener.
     * @see #addChangeListener
     * @see EventListenerList
     */
    protected void fireStateChanged() {
        for (int i = changeListeners.length - 1; i >= 0; i--) {
        	changeListeners[i].stateChanged(changeEvent);
        }
    }   
}
