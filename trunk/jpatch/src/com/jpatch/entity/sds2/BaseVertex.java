package com.jpatch.entity.sds2;

import javax.vecmath.*;

import com.jpatch.entity.*;

public class BaseVertex extends AbstractVertex implements XFormListener {
	private XFormNode xformNode;
	private final Matrix4d transformMatrix = new Matrix4d();
	private final Matrix4d invTransformMatrix = new Matrix4d();
	private boolean transformMatrixValid;
	private boolean invTransformMatrixValid;
	private final Point3d localPosition = new Point3d();
	
	public BaseVertex(XFormNode node) {
		this(node, 0, 0, 0);
	}
	
	public BaseVertex(XFormNode node, double x, double y, double z) {
		if (node == null) {
			throw new NullPointerException();
		}
		this.xformNode = node;
		xformNode.addXFormListener(this);
		xformNode.getLocal2WorldTransform(transformMatrix);
		invTransformMatrix.invert(transformMatrix);
		invTransformMatrixValid = true;
		positionAttr.setTuple(x, y, z);
	}
	
	@Override
	void setPos(double x, double y, double z) {
		validateInvTransformMatrix();
		worldPosition.set(x, y, z);
		invTransformMatrix.transform(worldPosition, localPosition);
		worldPositionValid = true;
		invalidate();
	}
	
	void validateWorldPosition() {
		if (!transformMatrixValid) {
			xformNode.getLocal2WorldTransform(transformMatrix);
			transformMatrixValid = true;
			invTransformMatrixValid = false;
			transformMatrix.transform(localPosition, worldPosition);
			worldPositionValid = true;
		} else if (!worldPositionValid) {
			transformMatrix.transform(localPosition, worldPosition);
			worldPositionValid = true;
		}
	}
	
	private void validateInvTransformMatrix() {
		if (!transformMatrixValid) {
			xformNode.getLocal2WorldTransform(transformMatrix);
			invTransformMatrix.invert(transformMatrix);
			transformMatrixValid = true;
			invTransformMatrixValid = true;
		} else if (!invTransformMatrixValid) {
			invTransformMatrix.invert(transformMatrix);
			invTransformMatrixValid = true;
		}
	}

	public void invalidateTransformation() {
		transformMatrixValid = false;
		invalidate();
	}
}
