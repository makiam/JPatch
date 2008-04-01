package com.jpatch.entity.sds2;

public class BaseVertex extends AbstractVertex {
	
	public BaseVertex() {
		this(0, 0, 0);
	}
	
	public BaseVertex(double x, double y, double z) {
		positionAttr.setTuple(x, y, z);
	}
	
	@Override
	void setPos(double x, double y, double z) {
		this.referencePosition.set(x, y, z);
		positionValid = true;
		invalidate();
	}
}
