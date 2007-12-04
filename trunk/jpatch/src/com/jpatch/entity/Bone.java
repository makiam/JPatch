package com.jpatch.entity;

import javax.vecmath.*;

import com.jpatch.afw.attributes.*;

public class Bone extends XFormNode {	
	private final Vector3d extent = new Vector3d();
	protected final Tuple3Attr extentAttr = new Tuple3Attr();
	
	public Bone() {
		extentAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				extentAttr.getTuple(extent);
				invalidateBranch(false);
			}
		});
	}
	
	@Override
	protected void computeWorldMatrices() {
		if (localInvalid || worldInvalid) {
			super.computeWorldMatrices();
			
			/* add bone extent to downstream matrix */
			Matrix4d m = downstream2WorldMatrix;
			m.m03 += m.m00 * extent.x + m.m01 * extent.y + m.m02 * extent.z;
			m.m13 += m.m10 * extent.x + m.m11 * extent.y + m.m12 * extent.z;
			m.m23 += m.m20 * extent.x + m.m21 * extent.y + m.m22 * extent.z;
		}
	}
	
	public DoubleAttr getLengthAttribute() {
		return extentAttr.getZAttr();
	}
	
	public Tuple3Attr getExtentAttribute() {
		return extentAttr;
	}
}
