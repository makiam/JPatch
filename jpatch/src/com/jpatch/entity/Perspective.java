package com.jpatch.entity;

import com.jpatch.afw.attributes.DoubleAttr;

public abstract class Perspective extends TransformNode {
	private DoubleAttr focalLengthAttr = new DoubleAttr(50);

	protected Perspective() { }
	
	public DoubleAttr getFocalLengthAttribute() {
		return focalLengthAttr;
	}
	
	public double getFocalLength() {
		return focalLengthAttr.getDouble();
	}
}
