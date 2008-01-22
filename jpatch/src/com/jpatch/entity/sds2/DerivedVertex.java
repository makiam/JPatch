package com.jpatch.entity.sds2;

import com.jpatch.afw.attributes.*;

import javax.vecmath.*;

public abstract class DerivedVertex extends AbstractVertex {
	private final Point3d hierarchyPos = new Point3d();
	protected final Point3d limit = new Point3d();
	protected final Vector3d normal = new Vector3d();
	protected final Vector3d alteredNormal = new Vector3d();
	protected final Matrix3d matrix = new Matrix3d();
	protected final Matrix3d invMatrix = new Matrix3d();
	protected final Vector3d uTangent = new Vector3d();
	protected final Vector3d vTangent = new Vector3d();
	private boolean positionValid;
	private boolean limitValid;
	private boolean alteredPositionValid;
	private boolean alteredLimitValid;
	
	public DerivedVertex() {
//		super();
		super(new Point3d());
		alteredLimit = new Point3d();
		positionAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
//				validatePosition();
				positionAttr.getTuple(hierarchyPos);
				
				hierarchyPos.sub(position);
				invMatrix.transform(hierarchyPos);
//				hierarchyPos.set(0, 0, 0);
			}
		});
	}
	
	public void getLimit(Tuple3f tuple) {
		validateAlteredLimit();
		tuple.set(alteredLimit);
	}
	
	public void getLimit(Tuple3d tuple) {
		validateAlteredLimit();
		tuple.set(alteredLimit);
	}
	
	public void getNormal(Tuple3f tuple) {
		validateAlteredLimit();
		tuple.set(alteredNormal);
	}
	
	public void getNormal(Tuple3d tuple) {
		validateAlteredLimit();
		tuple.set(alteredNormal);
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
		invalidate();
	}
	
	public void setPosition(double x, double y, double z) {
		positionAttr.setTuple(position);
		invalidate();
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
	
//	/**
//	 * Computes the position of this DerivedVertex
//	 */
	public final void validateAlteredPosition() {
		super.validateAlteredPosition();
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
		limitValid = true;
	}
	
	/**
	 * Computes the limit, tangents and normal of this DerivedVertex
	 */
	public final void validateAlteredLimit() {
//		if (true) throw new UnsupportedOperationException();
//		invalid = false;
//		System.out.println(this + ".validateAlteredLimit(), alteredLimitValid = " + alteredLimitValid);
		super.validateAlteredLimit();
		if (alteredLimitValid) {
			return;
		}
//		System.out.println("   calling computeAlteredLimit()");
		computeAlteredLimit();
		alteredLimitValid = true;
		
	}
	
	@Override
	public final void invalidate() {
		super.invalidate();
		positionValid = false;
		limitValid = false;
		alteredPositionValid = false;
		alteredLimitValid = false;
	}
	
//	@Override
//	public final void invalidateAltered() {
////		System.out.println("DerivedVertex.invalidateAltered() called on object " + this);
//		super.invalidateAltered();
//		alteredPositionValid = false;
//		alteredLimitValid = false;
////		System.out.println("    " + this + ".alteredLimitValid = " + alteredLimitValid);
////		System.out.println(this + " invalidateAltered()");
//	}
	
	protected void computeMatrix() {
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
		alteredPosition.set(hierarchyPos);
		matrix.transform(alteredPosition);
		alteredPosition.add(position);
	}
	
	protected abstract void computeAlteredLimit();
}
