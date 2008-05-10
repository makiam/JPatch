package com.jpatch.entity.sds2;

public abstract class DerivedVertex extends AbstractVertex {
	
	@Override
	public void setPosition(double x, double y, double z) {
		validateInvDisplacementMatrix();
		displacementVector.set(x - worldPosition.x, y - worldPosition.y, z - worldPosition.z);
//		invDisplacementMatrix.transform(displacementVector);
		positionAttr.setTuple(displacementVector);
	}
	
	@Override
	public void setPos(double x, double y, double z) {
		setDisplacement(x, y, z);
	}
}
