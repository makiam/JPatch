package com.jpatch.afw.attributes;

import javax.vecmath.*;

public class Tuple3Attr extends AbstractAttribute {
	private final DoubleAttr xAttr;
	private final DoubleAttr yAttr;
	private final DoubleAttr zAttr;
	private final AttributePostChangeListener listener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			fireAttributeHasChanged();
		}
	};
	
	public Tuple3Attr() {
		this(new DoubleAttr(), new DoubleAttr(), new DoubleAttr());
	}
	
	public Tuple3Attr(double x, double y, double z) {
		this(new DoubleAttr(x), new DoubleAttr(y), new DoubleAttr(z));
	}
	
	public Tuple3Attr(DoubleAttr x, DoubleAttr y, DoubleAttr z) {
		xAttr = x;
		yAttr = y;
		zAttr = z;
		xAttr.addAttributePostChangeListener(listener);
		yAttr.addAttributePostChangeListener(listener);
		zAttr.addAttributePostChangeListener(listener);
	}
	
	public Tuple3Attr(Tuple3d tuple) {
		this(tuple.x, tuple.y, tuple.z);
		bindTuple(tuple);
	}
	
	public Tuple3Attr(Tuple3d tuple, double x, double y, double z) {
		this(x, y, z);
		bindTuple(tuple);
	}
	
	public Tuple3Attr(Tuple3d tuple, DoubleAttr x, DoubleAttr y, DoubleAttr z) {
		this(x, y, z);
		bindTuple(tuple);
	}
	
	public Tuple3Attr(Tuple3f tuple) {
		this(tuple.x, tuple.y, tuple.z);
		bindTuple(tuple);
	}
	
	public Tuple3Attr(Tuple3f tuple, double x, double y, double z) {
		this(x, y, z);
		bindTuple(tuple);
	}
	
	public Tuple3Attr(Tuple3f tuple, DoubleAttr x, DoubleAttr y, DoubleAttr z) {
		this(x, y, z);
		bindTuple(tuple);
	}
	
	public DoubleAttr getXAttr() {
		return xAttr;
	}
	
	public DoubleAttr getYAttr() {
		return yAttr;
	}
	
	public DoubleAttr getZAttr() {
		return zAttr;
	}
	
	public double getX() {
		return xAttr.getDouble();
	}
	
	public double getY() {
		return yAttr.getDouble();
	}
	
	public double getZ() {
		return zAttr.getDouble();
	}
	
	public Tuple3d getTuple(Tuple3d tuple) {
		tuple.x = xAttr.getDouble();
		tuple.y = yAttr.getDouble();
		tuple.z = zAttr.getDouble();
		return tuple;
	}
	
	public Tuple3f getTuple(Tuple3f tuple) {
		tuple.x = (float) xAttr.getDouble();
		tuple.y = (float) yAttr.getDouble();
		tuple.z = (float) zAttr.getDouble();
		return tuple;
	}
	
	public void setTuple(Tuple3Attr tuple) {
		setTuple(tuple.getX(), tuple.getY(), tuple.getZ());
	}
	
	public void setTuple(Tuple3d tuple) {
		setTuple(tuple.x, tuple.y, tuple.z);
	}
	
	public void setTuple(Tuple3f tuple) {
		setTuple(tuple.x, tuple.y, tuple.z);
	}
	
	public void setTuple(double x, double y, double z) {
		double oldX = xAttr.getDouble();
		double oldY = yAttr.getDouble();
		double oldZ = zAttr.getDouble();
		fireEvents = false;													// prevent event notification
		xAttr.setDouble(x);
		yAttr.setDouble(y);
		zAttr.setDouble(z);
		fireEvents = true;													// enable event notification
		if (xAttr.getDouble() != oldX || yAttr.getDouble() != oldY || zAttr.getDouble() != oldZ) {		// only if one of the component values actually has changed
			fireAttributeHasChanged();										// fire events
		}
	}
	
	@Override
	public String toString() {
		return "(" + xAttr.getDouble() + ", " + yAttr.getDouble() + ", " + zAttr.getDouble() + ")";
	}
	
	public void bindTuple(Tuple3f tuple) {
		addAttributePostChangeListener(new BindTuple3fListener(tuple));
	}
	
	public void bindTuple(Tuple3d tuple) {
		addAttributePostChangeListener(new BindTuple3dListener(tuple));
	}
	
	private static class BindTuple3dListener implements AttributePostChangeListener {
		private final Tuple3d tuple;
		
		private BindTuple3dListener(Tuple3d tuple) {
			this.tuple = tuple;
		}
		
		public void attributeHasChanged(Attribute source) {
			Tuple3Attr tuple3Attr = (Tuple3Attr) source;
			tuple3Attr.getTuple(tuple);
		}
	}
	
	private static class BindTuple3fListener implements AttributePostChangeListener {
		private final Tuple3f tuple;
		
		private BindTuple3fListener(Tuple3f tuple) {
			this.tuple = tuple;
		}
		
		public void attributeHasChanged(Attribute source) {
			Tuple3Attr tuple3Attr = (Tuple3Attr) source;
			tuple3Attr.getTuple(tuple);
		}
	}
}
