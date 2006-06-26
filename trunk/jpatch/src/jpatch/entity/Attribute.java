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
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * @author sascha
 *
 */
public abstract class Attribute {
	private final java.lang.String name;
	AttributeListener[] attributeListeners = new AttributeListener[0];
	boolean valueAdjusting;
	
	Attribute(java.lang.String name) {
		this.name = name;
	}
	
	public java.lang.String getName() {
		return name;
	}
	
	public boolean isValueAdjusting() {
		return valueAdjusting;
	}
	
	public void setValueAdjusting(boolean b) {
		this.valueAdjusting = b;
	}
	
	public boolean isKeyable() {
		return false;
	}
	
	public boolean isKeyed() {
		return false;
	}
	
	/**
     * Adds a AttributeListener to this attribute.
     *
     * @param l the AttributeListener to add
     * @see #fireAttributeChanged
     * @see #removeAttributeListener
     */
    public void addAttributeListener(AttributeListener l) {
//    	System.out.println(this + " add listener " + l);
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
    	System.out.println(this + " remove listener " + l);
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
    	valueAdjusting = true;
	    for (int i = attributeListeners.length - 1; i >= 0; i--) {
	      	attributeListeners[i].attributeChanged(this);
	      	System.out.println(this + " " + i + " " + attributeListeners[i]);
	    }
	    valueAdjusting = false;
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
			if (!this.string.equals(string) && !valueAdjusting) {
				this.string = string;
				fireAttributeChanged();
			}
		}
		
		public boolean isUseTextArea() {
			return useTextArea;
		}
	}

	public static class Enum extends Attribute {
		private java.lang.Enum enumValue;
		
		public Enum(java.lang.String name, java.lang.Enum enumValue) {
			super(name);
			this.enumValue = enumValue;
		}
		
		public java.lang.Enum get() {
			return enumValue;
		}
		
		public void set(java.lang.Enum enumValue) {
			if (!this.enumValue.equals(enumValue) && !valueAdjusting) {
				this.enumValue = enumValue;
				fireAttributeChanged();
			}
		}
	}
	
	public static class Integer extends Attribute {
		private int value;
		
		public Integer(java.lang.String name) {
			super(name);
		}
		
		public void set(int newValue) {
			if (newValue != value && !valueAdjusting) {
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
	
	public static class Limit extends Attribute.Double {
		public Attribute.Boolean enabled;
		
		Limit(Attribute.BoundedDouble attribute, java.lang.String suffix) {
			super(attribute.getName() + " " + suffix);
			enabled = new Attribute.Boolean(attribute.getName() + " " + suffix + " enabled");
			addAttributeListener(attribute);
			enabled.addAttributeListener(attribute);
		}

		public Attribute.Boolean getEnableAttribute() {
			return enabled;
		}
		
		public Attribute.BoundedDouble getTarget() {
			return (Attribute.BoundedDouble) attributeListeners[0];
		}
	}
	
	public static class Double extends Attribute {
		private double value;
		
		public Double(java.lang.String name) {
			super(name);
		}
		
		public void set(double newValue) {
			if (value != newValue && !valueAdjusting) {
				value = newValue;
				fireAttributeChanged();
			}
		}
		
		public double get() {
			return value;
		}
	}
	
	public static class BoundedDouble extends Double implements AttributeListener {
		public Limit min, max;
		public Boolean keyed;
		private MotionCurveNew motionCurve;

		public BoundedDouble(java.lang.String name) {
			super(name);
			min = new Limit(this, "lower limit");
			max = new Limit(this, "upper limit");
			keyed = new Boolean(name + " keyed");
		}
		
		public BoundedDouble(java.lang.String name, double value) {
			this(name);
			set(value);
		}
		
		@Override
		public void set(double newValue) {
			if (min.enabled.get() && newValue < min.get())
				newValue = min.get();
			if (max.enabled.get() && newValue > max.get())
				newValue = max.get();
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
		
		@Override
		public boolean isKeyable() {
			return true;
		}
		
		@Override
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

	public static class Tuple<T extends Tuple3d> extends Attribute implements AttributeListener {
		public BoundedDouble x, y, z;
		private T tuple;
		private boolean keyable;
		
		public Tuple(java.lang.String name, T tuple, boolean keyable) {
			super(name);
			x = new BoundedDouble(name + ".X");
			y = new BoundedDouble(name + ".Y");
			z = new BoundedDouble(name + ".Z");
			x.addAttributeListener(this);
			y.addAttributeListener(this);
			z.addAttributeListener(this);
			this.tuple = tuple;
			this.keyable = keyable;
			if (keyable) {
				x.keyed.set(true);
				y.keyed.set(true);
				z.keyed.set(true);
			}
		}
		
		public T get() {
			return tuple;
		}
		
		public void set(Tuple3d v) {
			set(v.x, v.y, v.z);
		}
		
		public void set(double x, double y, double z) {
			this.x.set(x);
			this.y.set(y);
			this.z.set(z);
		}

		@Override
		public boolean isKeyable() {
			return keyable;
		}
		
		public void attributeChanged(Attribute attribute) {
			if (tuple.x != x.get() || tuple.y != y.get() || tuple.z != z.get()) {
				tuple.set(x.get(), y.get(), z.get());
				fireAttributeChanged();
			}
		}
	}
	
	public static class Boolean extends Attribute {
		private boolean value;
		
		public Boolean(java.lang.String name) {
			super(name);
		}
		
		public void set(boolean newValue) {
			if (value != newValue && !valueAdjusting) {
				value = newValue;
				fireAttributeChanged();
			}
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
			doubleAttribute.getMin().enabled.set(true);
			doubleAttribute.getMin().set(0);
			doubleAttribute.getMax().enabled.set(true);
			doubleAttribute.getMax().set(1);
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
		
		@Override
		public boolean isKeyable() {
			return true;
		}
		
		@Override
		public boolean isKeyed() {
			return doubleAttribute.motionCurve != null;
		}
		
		public void update(double position) {
			set(doubleAttribute.motionCurve.getValueAt(position) > 0.5);
		}
	}
}
