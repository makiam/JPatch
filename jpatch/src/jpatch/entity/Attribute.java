/*
 * $Id:$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.entity;

import javax.swing.event.*;

/**
 * @author sascha
 *
 */
public abstract class Attribute {
	private final java.lang.String name;
	private AttributeListener[] attributeListeners = new AttributeListener[0];
	
	Attribute(java.lang.String name) {
		this.name = name;
	}
	
	public java.lang.String getName() {
		return name;
	}
	
	/**
     * Adds a AttributeListener to this attribute.
     *
     * @param l the AttributeListener to add
     * @see #fireAttributeChanged
     * @see #removeAttributeListener
     */
    public void addAttributeListener(AttributeListener l) {
    	for (AttributeListener al : attributeListeners)
    		if (al == l)
    			throw new IllegalArgumentException("AttributeListener " + l + " has already been added to " + this);
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
    	int index = -1;
    	for (int i = 0; i < attributeListeners.length; i++) {
    	    if (attributeListeners[i] == l) {
    	    	index = i;
    	    	break;
    	    }
    	}
    	if (index > -1) {
    		AttributeListener[] tmp = new AttributeListener[attributeListeners.length - 1];
    	    // Copy the list up to index
    	    System.arraycopy(attributeListeners, 0, tmp, 0, index);
    	    // Copy from one past the index, up to
    	    // the end of tmp (which is one element
    	    // shorter than the old list)
    	    if (index < tmp.length)
    	    	System.arraycopy(attributeListeners, index + 1, tmp, index, tmp.length - index);
    	    // set the listener array to the new array or null
    	    attributeListeners = tmp;
        }
    }
    
    /**
     * Call attributeChanged on each listener.
     * @see #addChangeListener
     * @see EventListenerList
     */
    protected void fireAttributeChanged() {
        for (int i = attributeListeners.length - 1; i >= 0; i--) {
        	attributeListeners[i].attributeChanged(this);
        }
    }   
    
	public static class String extends Attribute {
		private java.lang.String string;
		private final boolean useTextArea = false;
		
		public String(java.lang.String name) {
			super(name);
		}
		
		public java.lang.String get() {
			return string;
		}
		
		public void set(java.lang.String string) {
			this.string = string;
		}
		
		public boolean isUseTextArea() {
			return useTextArea;
		}
	}

	public static class Enum extends Attribute {
		private java.lang.Enum enumValue;
		
		public Enum(java.lang.String name) {
			super(name);
		}
		
		public java.lang.Enum get() {
			return enumValue;
		}
		
		public void set(java.lang.Enum enumValue) {
			this.enumValue = enumValue;
		}
	}
	
	public static class Integer extends Attribute {
		private int value;
		
		public Integer(java.lang.String name) {
			super(name);
		}
		
		public void set(int newValue) {
			if (newValue != value) {
				value = newValue;
				fireAttributeChanged();
			}
		}
		
		public int get() {
			return value;
		}
	}
	
	public static class BoundedInteger extends Integer {
		private final int min;
		private final int max;
		
		public BoundedInteger(java.lang.String name, int min, int max) {
			super(name);
			if (min > max)
				throw new IllegalArgumentException("min " + min + " > max " + max);
			this.min = min;
			this.max = max;
		}
		
		@Override
		public void set(int newValue) {
			if (newValue < min)
				newValue = min;
			else if (newValue > max)
				newValue = max;
			super.set(newValue);
		}
		
		public int getMin() {
			return min;
		}
		
		public int getMax() {
			return max;
		}
	}
	
	public static class Limit extends Attribute {
		private double value;
		private boolean enabled;
		
		Limit(Attribute.BoundedDouble attribute, java.lang.String suffix) {
			super(attribute.getName() + " " + suffix);
			addAttributeListener(attribute);
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
			fireAttributeChanged();
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
			fireAttributeChanged();
		}
		
	}
	
	public static class Double extends Attribute {
		private double value;
		
		public Double(java.lang.String name) {
			super(name);
		}
		
		public void set(double newValue) {
			if (newValue != value) {
				value = newValue;
				fireAttributeChanged();
			}
		}
		
		public double get() {
			return value;
		}
	}
	
	public static class BoundedDouble extends Double implements AttributeListener {
		private Limit min, max;
		private MotionCurveNew motionCurve;

		public BoundedDouble(java.lang.String name) {
			super(name);
			min = new Limit(this, "min");
			max = new Limit(this, "max");
		}
		
		public BoundedDouble(java.lang.String name, double value) {
			this(name);
			set(value);
		}
		
		public void set(double newValue) {
			if (min.enabled && newValue < min.value)
				newValue = min.value;
			if (max.enabled && newValue > max.value)
				newValue = max.value;
			super.set(newValue);
		}
	
		public Limit getMin() {
			return min;
		}
		
		public Limit getMax() {
			return max;
		}
		
		public MotionCurveNew getMotionCurve() {
			return motionCurve;
		}

		public void setMotionCurve(MotionCurveNew motionCurve) {
			this.motionCurve = motionCurve;
		}
		
		public boolean isKeyed() {
			return motionCurve != null;
		}
		
		public void update(double position) {
			set(motionCurve.getValueAt(position));
		}
		
		public void attributeChanged(Attribute attribute) {
			set(get());
		}
	}

	public static class Boolean extends Attribute {
		private boolean value;
		
		public Boolean(java.lang.String name) {
			super(name);
		}
		
		public void set(boolean value) {
			this.value = value;
			fireAttributeChanged();
		}
		
		public boolean get() {
			return value;
		}
	}
	
	public static class KeyedBoolean extends Boolean {
		private final Attribute.BoundedDouble doubleAttribute;
		
		public KeyedBoolean(java.lang.String name) {
			super(name);
			doubleAttribute = new Attribute.BoundedDouble(name);
			doubleAttribute.getMin().setEnabled(true);
			doubleAttribute.getMin().setValue(0);
			doubleAttribute.getMax().setEnabled(true);
			doubleAttribute.getMax().setValue(1);
		}
		
		public KeyedBoolean(java.lang.String name, boolean value) {
			this(name);
			set(value);
		}
		
		public MotionCurveNew getMotionCurve() {
			return doubleAttribute.motionCurve;
		}

		public void setMotionCurve(MotionCurveNew motionCurve) {
			doubleAttribute.motionCurve = motionCurve;
		}
		
		public boolean isKeyed() {
			return doubleAttribute.motionCurve != null;
		}
		
		public void update(double position) {
			set(doubleAttribute.motionCurve.getValueAt(position) > 0.5);
		}
	}
}
