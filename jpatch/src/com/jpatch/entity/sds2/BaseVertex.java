package com.jpatch.entity.sds2;

public class BaseVertex extends AbstractVertex {
	
	public BaseVertex() {
		this(0, 0, 0);
	}
	
	public BaseVertex(double x, double y, double z) {
		positionAttr.setTuple(x, y, z);
	}
	
	@Override
	void setPosition(double x, double y, double z) {
		System.out.println(this + ".setPosition(" + x + ", " + y + ", " + z + ") called");
		this.referencePosition.set(x, y, z);
		invalidate();
	}
}
