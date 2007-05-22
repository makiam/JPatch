package com.jpatch.afw.attributes;

import javax.vecmath.*;

public class Tuple2 extends AbstractAttribute<Tuple2d> {
	protected final DoubleAttr xAttr;
	protected final DoubleAttr yAttr;
	private final AttributePostChangeListener listener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			fireAttributeHasChanged();
		}
	};
	
	public Tuple2() {
		this(new DoubleAttr(), new DoubleAttr());
	}
	
	public Tuple2(double x, double y) {
		this(new DoubleAttr(x), new DoubleAttr(y));
	}
	
	public Tuple2(DoubleAttr x, DoubleAttr y) {
		xAttr = x;
		yAttr = y;
		xAttr.addAttributePostChangeListener(listener);
		yAttr.addAttributePostChangeListener(listener);
	}
	
	public DoubleAttr getXAttr() {
		return xAttr;
	}
	
	public DoubleAttr getYAttr() {
		return yAttr;
	}
	
	public double getX() {
		return xAttr.getDouble();
	}
	
	public double getY() {
		return yAttr.getDouble();
	}
	
	public void setX(double x) {
		xAttr.setDouble(x);
	}
	
	public void setY(double y) {
		yAttr.setDouble(y);
	}
	
	public void getTuple(Tuple2d tuple) {
		tuple.x = xAttr.getDouble();
		tuple.y = yAttr.getDouble();
	}
	
	public void getTuple(Tuple2f tuple) {
		tuple.x = (float) xAttr.getDouble();
		tuple.y = (float) yAttr.getDouble();
	}
	
	public void setTuple(Tuple2 tuple) {
		setTuple(tuple.getX(), tuple.getY());
	}
	
	public void setTuple(Tuple2d tuple) {
		setTuple(tuple.x, tuple.y);
	}
	
	public void setTuple(Tuple2f tuple) {
		setTuple(tuple.x, tuple.y);
	}
	
	public void setTuple(double x, double y) {
		double oldX = xAttr.getDouble();
		double oldY = yAttr.getDouble();
		fireEvents = false;													// prevent event notification
		xAttr.setDouble(x);
		yAttr.setDouble(y);
		fireEvents = true;													// enable event notification
		if (xAttr.getDouble() != oldX || yAttr.getDouble() != oldY) {		// only if one of the component values actually has changed
			fireAttributeHasChanged();										// fire events
		}
	}
}
