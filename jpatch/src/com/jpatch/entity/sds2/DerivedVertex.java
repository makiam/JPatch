package com.jpatch.entity.sds2;

import javax.vecmath.*;

public abstract class DerivedVertex extends AbstractVertex {
	protected final Point3d alteredPosition = new Point3d();
	protected final Point3d alteredLimit = new Point3d();
	protected final Vector3d alteredNormal = new Vector3d();
	protected final Point3d limit = new Point3d();
	protected final Matrix3d matrix = new Matrix3d();
	protected final Matrix3d invMatrix = new Matrix3d();
	protected final Vector3d uTangent = new Vector3d();
	protected final Vector3d vTangent = new Vector3d();
	protected final Vector3d normal = new Vector3d();
	private boolean positionValid;
	private boolean limitValid;
	private boolean alteredPositionValid;
	private boolean alteredLimitValid;
	
	public void getLimit(Tuple3f tuple) {
		validateLimit();
		tuple.set(limit);
	}
	
	public void getLimit(Tuple3d tuple) {
		validateLimit();
		tuple.set(limit);
	}
	
	public void getNormal(Tuple3f tuple) {
		validateLimit();
		tuple.set(normal);
	}
	
	public void getNormal(Tuple3d tuple) {
		validateLimit();
		tuple.set(normal);
	}
	
	public void getPosition(Tuple3d position) {
		validateAlteredPosition();
		position.set(alteredPosition);
	}
	
	public void getPosition(Tuple3f position) {
		validateAlteredPosition();
		position.set(alteredPosition);
	}
	
	public void setPosition(Tuple3d position) {
		positionAttr.setTuple(position);
		invalidateAltered();
	}
	
	public void setPosition(double x, double y, double z) {
		positionAttr.setTuple(x, y, z);
		invalidateAltered();
	}
	/**
	 * Computes the position of this DerivedVertex
	 */
	public final void validatePosition() {
		super.validatePosition();
		if (positionValid) {
			return;
		}
		computePosition();
		positionValid = true;
	}
	
	/**
	 * Computes the position of this DerivedVertex
	 */
	public final void validateAlteredPosition() {
		if (alteredPositionValid) {
			return;
		}
		computeAlteredPosition();
		alteredPositionValid = true;
	}
	
	/**
	 * Computes the limit, tangents and normal of this DerivedVertex
	 */
	public final void validateLimit() {
		super.validateLimit();
		if (limitValid) {
			return;
		}
		computeLimit();
		alteredLimitValid = true;
	}
	
	/**
	 * Computes the limit, tangents and normal of this DerivedVertex
	 */
	public final void validateAlteredLimit() {
		if (alteredLimitValid) {
			return;
		}
		computeAlteredLimit();
		alteredLimitValid = true;
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		positionValid = false;
		limitValid = false;
	}
	
	protected void computeMatrix() {
		normal.cross(uTangent, vTangent);
		normal.normalize();
		double nLength = 0.5 * (uTangent.length() + vTangent.length());
		matrix.m00 = uTangent.x; matrix.m01 = vTangent.x; matrix.m02 = normal.x * nLength;
		matrix.m10 = uTangent.y; matrix.m11 = vTangent.y; matrix.m12 = normal.y * nLength;
		matrix.m20 = uTangent.z; matrix.m21 = vTangent.z; matrix.m22 = normal.z * nLength;
		invMatrix.invert(matrix);
	}
	
	protected abstract void computePosition();
	protected abstract void computeLimit();
	protected void computeAlteredPosition() {
		validateLimit();
		positionAttr.getTuple(alteredPosition);
		matrix.transform(alteredPosition);
	}
	protected abstract void computeAlteredLimit();
}
