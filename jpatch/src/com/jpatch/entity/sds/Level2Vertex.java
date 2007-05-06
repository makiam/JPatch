package com.jpatch.entity.sds;

import javax.vecmath.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.settings.*;

public abstract class Level2Vertex extends BaseVertex {
	private static RealtimeRendererSettings RENDERER_SETTINGS = Settings.getInstance().realtimeRenderer;
	
	public final BooleanAttr overridePosition = new BooleanAttr(false);
	public final BooleanAttr overrideSharpness = new BooleanAttr(false);
	public final Point3d limit = new Point3d();
	public final Vector3d uTangent = new Vector3d();
	public final Vector3d vTangent = new Vector3d();
	public final Vector3d normal = new Vector3d();
	
	public final Point3f projectedLimit = new Point3f();
	public final Vector3f projectedNormal = new Vector3f();

	public abstract void computeDerivedPosition();
	public abstract void computeLimit();
	
	SlateEdge creaseEdge0, creaseEdge1;
	
	@Override
	public void project(Matrix4f matrix) {
		super.project(matrix);
		projectedLimit.set(limit);
		matrix.transform(projectedLimit);
		projectedNormal.set(normal);
		matrix.transform(projectedNormal);
		if (RENDERER_SETTINGS.softwareNormalize) {
			projectedNormal.normalize();
		}
	}
}
