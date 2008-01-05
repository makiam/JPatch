package com.jpatch.entity.sds2;

import javax.vecmath.*;

public abstract class DerivedVertex extends Vertex {
	protected final Point3d limit = new Point3d();
	protected final Vector3d uTangent = new Vector3d();
	protected final Vector3d vTangent = new Vector3d();
	protected final Vector3d normal = new Vector3d();
	private boolean positionValid;
	private boolean limitValid;
	
	/**
	 * Computes the position of this DerivedVertex
	 */
	public final void validatePosition() {
		if (positionValid) {
			return;
		}
		computePosition();
		positionValid = true;
	}
	
	/**
	 * Computes the limit, tangents and normal of this DerivedVertex
	 */
	public final void validateLimit() {
		if (limitValid) {
			return;
		}
		computeLimit();
		limitValid = true;
	}
	
	protected abstract void computePosition();
	protected abstract void computeLimit();
}
