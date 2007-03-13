package sds;

import javax.vecmath.*;

import jpatch.entity.*;

public abstract class Level2Vertex extends BaseVertex {
	public final Attribute.Boolean overridePosition = new Attribute.Boolean(false);
	public final Attribute.Boolean overrideSharpness = new Attribute.Boolean(false);
	public final Point3d limit = new Point3d();
	public final Vector3d uTangent = new Vector3d();
	public final Vector3d vTangent = new Vector3d();
	public final Vector3d normal = new Vector3d();
	
	public final Point3f projectedLimit = new Point3f();
	public final Vector3f projectedNormal = new Vector3f();

	public abstract void computeDerivedPosition();
	public abstract void computeLimit();
	
	@Override
	public void project(Matrix4f matrix) {
		super.project(matrix);
		projectedLimit.set(limit);
		matrix.transform(projectedLimit);
		projectedNormal.set(normal);
		matrix.transform(projectedNormal);
	}
}
