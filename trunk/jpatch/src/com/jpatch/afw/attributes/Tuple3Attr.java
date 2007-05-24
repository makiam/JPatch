package com.jpatch.afw.attributes;

import javax.vecmath.*;

public class Tuple3Attr extends AbstractAttribute<Tuple3d> {
	protected final DoubleAttr xAttr;
	protected final DoubleAttr yAttr;
	protected final DoubleAttr zAttr;
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
	
	public void getTuple(Tuple3d tuple) {
		tuple.x = xAttr.getDouble();
		tuple.y = yAttr.getDouble();
		tuple.z = zAttr.getDouble();
	}
	
	public void getTuple(Tuple3f tuple) {
		tuple.x = (float) xAttr.getDouble();
		tuple.y = (float) yAttr.getDouble();
		tuple.z = (float) zAttr.getDouble();
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
	
	/**
	 * Trows an UnsupportedOperationException
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void addAttributePreChangeListener(AttributePreChangeListener l) {
		throw new UnsupportedOperationException();
	}
}
