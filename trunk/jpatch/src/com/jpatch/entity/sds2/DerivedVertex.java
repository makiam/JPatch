package com.jpatch.entity.sds2;

public abstract class DerivedVertex extends AbstractVertex {
	
	@Override
	public void setPosition(double x, double y, double z) {
		setDisplacedPosition(x, y, z);
	}
}
