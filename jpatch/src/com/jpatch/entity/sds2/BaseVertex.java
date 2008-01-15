package com.jpatch.entity.sds2;

public class BaseVertex extends AbstractVertex {
	
	public BaseVertex() {
		positionAttr.bindTuple(position);
	}
	
	public BaseVertex(double x, double y, double z) {
		this();
		positionAttr.setTuple(x, y, z);
	}
}
