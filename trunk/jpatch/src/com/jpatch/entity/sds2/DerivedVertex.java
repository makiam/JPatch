package com.jpatch.entity.sds2;

import javax.vecmath.*;

public abstract class DerivedVertex extends AbstractVertex {
	
	@Override
	public void setPosition(Point3d p) {
		validateInvDisplacementMatrix();
		displacementVector.sub(p, position);
//		invDisplacementMatrix.transform(displacementVector);
		positionAttr.setTuple(displacementVector);
	}
	
	@Override
	public void setPos(double x, double y, double z) {
		setDisplacement(x, y, z);
	}
}
