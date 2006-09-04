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

import java.io.PrintStream;

import javax.swing.event.*;

/**
 * @author sascha
 *
 */
public abstract class Attribute {
	AttributeListener[] attributeListeners = new AttributeListener[0];
	boolean valueAdjusting;
	boolean locked;
	
	public java.lang.String getName() {
		return "";
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
	
	public boolean isLocked() {
		return locked;
	}
	
	public void setLocked(boolean lock) {
		locked = lock;
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
    final void fireAttributeChanged() {
    	valueAdjusting = true;
	    for (int i = attributeListeners.length - 1; i >= 0; i--) {
	      	attributeListeners[i].attributeChanged(this);
//	      	System.out.println("fire " + this + " " + i + " " + attributeListeners[i]);
	    }
	    valueAdjusting = false;
    }   
    
	public static class String extends Attribute {
		private java.lang.String string = "";
		private final boolean useTextArea = false;
		
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

	public static class Enum<E extends java.lang.Enum> extends Attribute {
		private E enumValue;
		
		public Enum(E enumValue) {
			this.enumValue = enumValue;
		}
		
		public E get() {
			return enumValue;
		}
		
		public void set(E enumValue) {
			if (!this.enumValue.equals(enumValue) && !valueAdjusting) {
				this.enumValue = enumValue;
				fireAttributeChanged();
			}
		}
		
		public void xml(PrintStream out) {
			out.append("\"").append(enumValue.name().toLowerCase()).append("\"");
		}
	}
	
	public static class Integer extends Attribute {
		private int value;
		
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
		
		public BoundedInteger(int min, int max) {
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
		
		Limit(Attribute.BoundedDouble attribute) {
			enabled = new Attribute.Boolean();
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
		public Attribute.Boolean locked = new Boolean();
		
		public Double() { }
		
		public Double(double value) {
			this.value = value;
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
		
		public void xml(PrintStream out) {
			out.append("\"").append(java.lang.Double.toString(value)).append("\"");
		}
	}
	
	public static class BoundedDouble extends Double implements AttributeListener {
		private final java.lang.String name;
		public Limit min = new Limit(this);
		public Limit max = new Limit(this);
		public Attribute.Boolean keyed = new Boolean();
		private MotionCurveNew motionCurve;

		public BoundedDouble(java.lang.String name) {
			this.name = name;
		}
		
		public BoundedDouble(java.lang.String name, double value) {
			this(name);
			set(value);
		}
		
		@Override
		public java.lang.String getName() {
			return name;
		}
		
		@Override
		public void set(double newValue) {
			if (min.enabled.get() && newValue < min.get())
				newValue = min.get();
			if (max.enabled.get() && newValue > max.get())
				newValue = max.get();
			super.set(newValue);
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

	public static class Tuple2 extends Attribute implements AttributeListener {
		public Double x, y;
		private boolean keyable;
		
		public Tuple2(java.lang.String name, javax.vecmath.Tuple2d tuple, boolean keyable) {
			this(name, tuple.x, tuple.y, keyable);
		}
		
		public Tuple2(java.lang.String name, javax.vecmath.Tuple2f tuple, boolean keyable) {
			this(name, tuple.x, tuple.y, keyable);
		}
		
		public Tuple2(java.lang.String name, double x, double y, boolean keyable) {
			if (keyable) {
				this.x = new BoundedDouble(name + ".x");
				this.y = new BoundedDouble(name + ".y");
			} else {
				this.x = new Double();
				this.y = new Double();
			}
			set(x, y);
			this.x.addAttributeListener(this);
			this.y.addAttributeListener(this);
			this.keyable = keyable;
			if (keyable) {
				((BoundedDouble) this.x).keyed.set(true);
				((BoundedDouble) this.y).keyed.set(true);
			}
		}
		
		public void get(javax.vecmath.Tuple3d tuple) {
			tuple.x = x.get();
			tuple.y = y.get();
		}
		
		public void get(javax.vecmath.Tuple3f tuple) {
			tuple.x = (float) x.get();
			tuple.y = (float) y.get();
		}
		
		public void set(javax.vecmath.Tuple2d tuple) {
			set(tuple.x, tuple.y);
		}
		
		public void set(javax.vecmath.Tuple2f tuple) {
			set(tuple.x, tuple.y);
		}
		
		public void set(double x, double y) {
			if (!valueAdjusting) {
				valueAdjusting = true;
				this.x.set(x);
				this.y.set(y);
				fireAttributeChanged();
			}
		}

		@Override
		public boolean isKeyable() {
			return keyable;
		}
		
		public void attributeChanged(Attribute attribute) {
			if (!valueAdjusting)
				fireAttributeChanged();
		}
		
		public void xml(PrintStream out) {
			out.append("x=");
			x.xml(out);
			out.append(" y=");
			y.xml(out);
		}
	}
	
	public static class Tuple3 extends Attribute implements AttributeListener {
		public Double x, y, z;
		private boolean keyable;
		
		public Tuple3(java.lang.String name, javax.vecmath.Tuple3d tuple, boolean keyable) {
			this(name, tuple.x, tuple.y, tuple.z, keyable);
		}
		
		public Tuple3(java.lang.String name, javax.vecmath.Tuple3f tuple, boolean keyable) {
			this(name, tuple.x, tuple.y, tuple.z, keyable);
		}
		
		public Tuple3(java.lang.String name, double x, double y, double z, boolean keyable) {
			if (keyable) {
				this.x = new BoundedDouble(name + ".x");
				this.y = new BoundedDouble(name + ".y");
				this.z = new BoundedDouble(name + ".z");
			} else {
				this.x = new Double();
				this.y = new Double();
				this.z = new Double();
			}
			set(x, y, z);
			this.x.addAttributeListener(this);
			this.y.addAttributeListener(this);
			this.z.addAttributeListener(this);
			this.keyable = keyable;
			if (keyable) {
				((BoundedDouble) this.x).keyed.set(true);
				((BoundedDouble) this.y).keyed.set(true);
				((BoundedDouble) this.z).keyed.set(true);
			}
		}
		
		public void get(javax.vecmath.Tuple3d tuple) {
			tuple.x = x.get();
			tuple.y = y.get();
			tuple.z = z.get();
		}
		
		public void get(javax.vecmath.Tuple3f tuple) {
			tuple.x = (float) x.get();
			tuple.y = (float) y.get();
			tuple.z = (float) z.get();
		}
		
		public void set(javax.vecmath.Tuple3d tuple) {
			set(tuple.x, tuple.y, tuple.z);
		}
		
		public void set(javax.vecmath.Tuple3f tuple) {
			set(tuple.x, tuple.y, tuple.z);
		}
		
		public void set(double x, double y, double z) {
			if (!valueAdjusting) {
				valueAdjusting = true;
				this.x.set(x);
				this.y.set(y);
				this.z.set(z);
				fireAttributeChanged();
			}
		}

		@Override
		public boolean isKeyable() {
			return keyable;
		}
		
		public void attributeChanged(Attribute attribute) {
			if (!valueAdjusting)
				fireAttributeChanged();
		}
		
		public void xml(PrintStream out) {
			out.append("x=");
			x.xml(out);
			out.append(" y=");
			y.xml(out);
			out.append(" z=");
			z.xml(out);
		}
	}
	
	public static class Boolean extends Attribute {
		private boolean value;
		
		public Boolean() { }
		
		public Boolean(boolean value) {
			this.value = value;
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
		public Attribute.Boolean keyed;
		public Attribute.Boolean locked;
		public KeyedBoolean(java.lang.String name) {
			doubleAttribute = new Attribute.BoundedDouble(name);
			doubleAttribute.min.enabled.set(true);
			doubleAttribute.max.set(0);
			doubleAttribute.min.enabled.set(true);
			doubleAttribute.max.set(1);
			keyed = doubleAttribute.keyed;
			locked = doubleAttribute.locked;
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
