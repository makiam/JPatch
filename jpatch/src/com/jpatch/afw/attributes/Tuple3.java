package com.jpatch.afw.attributes;

import javax.vecmath.*;

public class Tuple3 {
	protected final DoubleAttr xAttr;
	protected final DoubleAttr yAttr;
	protected final DoubleAttr zAttr;
	
	public Tuple3() {
		this(new DoubleAttr(), new DoubleAttr(), new DoubleAttr());
	}
	
	public Tuple3(double x, double y, double z) {
		this(new DoubleAttr(x), new DoubleAttr(y), new DoubleAttr(z));
	}
	
	public Tuple3(DoubleAttr x, DoubleAttr y, DoubleAttr z) {
		xAttr = x;
		yAttr = y;
		zAttr = z;
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
	
	public void setTuple(Tuple3 tuple) {
		setTuple(tuple.getX(), tuple.getY(), tuple.getZ());
	}
	
	public void setTuple(Tuple3d tuple) {
		setTuple(tuple.x, tuple.y, tuple.z);
	}
	
	public void setTuple(Tuple3f tuple) {
		setTuple(tuple.x, tuple.y, tuple.z);
	}
	
	public void setTuple(double x, double y, double z) {
		xAttr.setDouble(x);
		yAttr.setDouble(y);
		zAttr.setDouble(z);
	}
	
	@Override
	public String toString() {
		return "(" + xAttr.getDouble() + ", " + yAttr.getDouble() + ", " + zAttr.getDouble() + ")";
	}
}
