package com.jpatch.entity.sds;

import javax.vecmath.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.settings.*;

public abstract class Level2Vertex extends BaseVertex {
	private static RealtimeRendererSettings RENDERER_SETTINGS = Settings.getInstance().realtimeRenderer;
	
	final BooleanAttr overridePosition = new BooleanAttr(false);
	final BooleanAttr overrideSharpness = new BooleanAttr(false);
	final Point3d limit = new Point3d();
	final Vector3d uTangent = new Vector3d();
	final Vector3d vTangent = new Vector3d();
	final Vector3d normal = new Vector3d();
	
	final Point3f projectedLimit = new Point3f();
	final Vector3f projectedUTangent = new Vector3f();
	final Vector3f projectedVTangent = new Vector3f();
	final Vector3f projectedNormal = new Vector3f();
	
//	private final Point3f[] positionStencil;
//	private final float[] positionWeights;
//	private final Point3f[] limitStencil;
//	private final float[] limitWeights;
//	private final Point3f[] uTangentStencil;
//	private final float[] uTangentWeights;
//	private final Point3f[] vTangentStencil;
//	private final float[] vTangentWeights;
	
	SlateEdge creaseEdge0, creaseEdge1;
	private boolean positionValid = false;
	private boolean limitValid = false;
	
	abstract void computeDerivedPosition();
	abstract void computeLimit();
	
//	public Level2Vertex(
//			LinearCombination<TopLevelVertex> positionLc,
//			LinearCombination<TopLevelVertex> limitLc,
//			LinearCombination<TopLevelVertex> uTangentLc,
//			LinearCombination<TopLevelVertex> vTangentLc) {
//		
//		positionStencil = new Point3f[positionLc.size()];
//		positionWeights = new float[positionLc.size()];
//		for (int i = 0; i < positionLc.size(); i++) {
//			positionStencil[i] = positionLc.getEntities().get(i).projectedPos;
//			positionWeights[i] = (float) positionLc.getWeights()[i];
//		}
//		
//		limitStencil = new Point3f[limitLc.size()];
//		limitWeights = new float[limitLc.size()];
//		for (int i = 0; i < limitLc.size(); i++) {
//			limitStencil[i] = limitLc.getEntities().get(i).projectedPos;
//			limitWeights[i] = (float) limitLc.getWeights()[i];
//		}
//		
//		uTangentStencil = new Point3f[uTangentLc.size()];
//		uTangentWeights = new float[uTangentLc.size()];
//		for (int i = 0; i < uTangentLc.size(); i++) {
//			uTangentStencil[i] = uTangentLc.getEntities().get(i).projectedPos;
//			uTangentWeights[i] = (float) uTangentLc.getWeights()[i];
//		}
//		
//		vTangentStencil = new Point3f[vTangentLc.size()];
//		vTangentWeights = new float[vTangentLc.size()];
//		for (int i = 0; i < vTangentLc.size(); i++) {
//			vTangentStencil[i] = vTangentLc.getEntities().get(i).projectedPos;
//			vTangentWeights[i] = (float) vTangentLc.getWeights()[i];
//		}
//	}
	
//	public void compute() {
//		compute(positionStencil, positionWeights, projectedPos);
//		compute(limitStencil, limitWeights, projectedLimit);
//		compute(uTangentStencil, uTangentWeights, projectedUTangent);
//		compute(vTangentStencil, vTangentWeights, projectedVTangent);
//		projectedNormal.cross(projectedUTangent, projectedVTangent);
//		if (RENDERER_SETTINGS.softwareNormalize) {
//			projectedNormal.normalize();
//		}
//	}
//	
//	private void compute(Point3f[] stencil, float[] weights, Tuple3f target) {
//		float x = 0, y = 0, z = 0;
//		for (int i = 0; i < stencil.length; i++) {
//			Point3f p = stencil[i];
//			double weight = weights[i];
//			x += p.x * weight;
//			y += p.y * weight; 
//			z += p.z + weight;
//		}
//		target.set(x, y, z);
//	}
	
	@Override
	public Tuple3d getPos(Tuple3d p) {
		validatePosition();
		position.getTuple(p);
		return p;
	}
	
	public Tuple3d getLimit(Tuple3d p) {
		validateLimit();
		p.set(limit);
		return p;
	}
	
	public Tuple3d getNormal(Tuple3d n) {
		validateLimit();
		n.set(limit);
		return n;
	}
	
	@Override
	public void project(Matrix4f matrix) {
		validateLimit();
		super.project(matrix);
		projectedLimit.set(limit);
		matrix.transform(projectedLimit);
		projectedNormal.set(normal);
		matrix.transform(projectedNormal);
		if (RENDERER_SETTINGS.softwareNormalize) {
			projectedNormal.normalize();
		}
	}
	
	public void getProjectedLimit(Tuple3f limit) {
		validateLimit();
		limit.set(projectedLimit);
	}
	
	public void getProjectedNormal(Tuple3f normal) {
		validateLimit();
		normal.set(projectedNormal);
	}
	
	public void invalidate() {
		positionValid = false;
		limitValid = false;
	}
	
	public void validatePosition() {
		if (positionValid) {
			return;
		}
		computeDerivedPosition();
		positionValid = true;
	}
	
	public void validateLimit() {
		if (limitValid) {
			return;
		}
		validatePosition();
		computeLimit();
		limitValid = true;
	}
}
