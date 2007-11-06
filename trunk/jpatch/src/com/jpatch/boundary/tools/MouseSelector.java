package com.jpatch.boundary.tools;

import java.awt.geom.Line2D;

import javax.vecmath.*;

import com.jpatch.afw.attributes.CollectionAttr;
import com.jpatch.afw.vecmath.Transform;
import com.jpatch.afw.vecmath.TransformUtil;
import static com.jpatch.afw.vecmath.TransformUtil.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.SceneGraphNode;
import com.jpatch.entity.SdsModel;
import com.jpatch.entity.sds.*;

public class MouseSelector {
	static final private double MIN_DIST_SQ = 64;
	
	public static Hit getVertexAt(Viewport viewport, int x, int y) {	
		return getVertexAt(viewport, x, y, Main.getInstance().getSceneGraphRoot(), new Hit());
	}
	
	public static Hit getVertexAt(Viewport viewport, int x, int y, SceneGraphNode node) {
		return getVertexAt(viewport, x, y, node, new Hit());
	}
	
	public static Hit getVertexAt(Viewport viewport, int x, int y, Hit hit) {
		return getVertexAt(viewport, x, y, Main.getInstance().getSceneGraphRoot(), hit);
	}
	
	public static Hit getVertexAt(Viewport viewport, int x, int y, SceneGraphNode node, Hit hit) {
		if (node instanceof SdsModel) {
			SdsModel sdsModel = (SdsModel) node;
			getVertexAt(viewport, x, y, sdsModel, hit);
		}
		for (SceneGraphNode child : node.getChildrenAttribute().getElements()) {
			getVertexAt(viewport, x, y, child, hit);
		}
		return hit;
	}
	
	public static void getVertexAt(Viewport viewport, int x, int y, SdsModel sdsModel, Hit hit) {
		ViewDef viewDef = viewport.getViewDef();
//		Matrix4d matrix = new Matrix4d(viewDef.getMatrix(new Matrix4d()));
//		Transform transform = sdsModel.getTransform();
//		transform.mul2(matrix);
		
//		Matrix4d matrix = viewport.getViewDef().getMatrix(new Matrix4d());
		Point3d p = new Point3d();
//		System.out.println("getVertexAt(" + x + ", " + y + ")");
		TransformUtil transformUtil = viewDef.getTransformUtil();
		transformUtil.setLocalTransform(sdsModel.getTransform());
		for (Face face : sdsModel.getSds().faceList) {
			for (HalfEdge edge : face.getEdges()) {
				TopLevelVertex vertex = edge.getFirstVertex();
				vertex.getPos(p);
				transformUtil.projectToScreen(LOCAL, p, p);
				double dx = x - p.x;
				double dy = y - p.y;
				double distanceSq = dx * dx + dy * dy;
				if (distanceSq < hit.distanceSq) {
					hit.node = sdsModel;
					hit.distanceSq = distanceSq;
					hit.object = vertex;
				}
			}
		}
	}
	
	public static void getVertices(Viewport viewport, int x0, int y0, int x1, int y1, SdsModel sdsModel, Selection selection) {
		CollectionAttr<AbstractVertex> selectedVertices = selection.getSelectedVerticesAttribute();
		selectedVertices.clear();
		
		ViewDef viewDef = viewport.getViewDef();
		
		TransformUtil transformUtil = viewDef.getTransformUtil();
		transformUtil.setLocalTransform(sdsModel.getTransform());
		
		/* ensure that x0 is the left side and x1 is the right side (x0 < x1) */
		if (x1 < x0) {
			int tmp = x0;
			x0 = x1;
			x1 = tmp;
		}
		
		/* ensure that y0 is the lower side and y1 is the upper side (y0 < y1) */
		if (y1 < y0) {
			int tmp = y0;
			y0 = y1;
			y1 = tmp;
		}
		
		System.out.println("x:" + x0 + " " + x1 + " y:" + y0 + " " + y1);
		Point3d p = new Point3d();
		for (Face face : sdsModel.getSds().faceList) {
			for (HalfEdge edge : face.getEdges()) {
				TopLevelVertex vertex = edge.getFirstVertex();
				vertex.getPos(p);
				transformUtil.projectToScreen(LOCAL, p, p);
				if (p.x >= x0 && p.x <= x1 && p.y >= y0 && p.y <= y1) {
					selectedVertices.add(vertex);
				}
			}
		}		
	}
	
	public static HalfEdge getEdgeAt(Viewport viewport, int x, int y, SdsModel sdsModel) {
		ViewDef viewDef = viewport.getViewDef();
		
		TransformUtil transformUtil = viewDef.getTransformUtil();
		transformUtil.setLocalTransform(sdsModel.getTransform());
		
//		Matrix4d matrix = viewport.getViewDef().getMatrix(new Matrix4d());
		Point3d p0 = new Point3d();
		Point3d p1 = new Point3d();
		HalfEdge hit = null;
		double min = MIN_DIST_SQ;
		Line2D.Double line = new Line2D.Double();
		for (Face face : sdsModel.getSds().faceList) {
			for (HalfEdge edge : face.getEdges()) {
				if (edge.isPrimary()) {
					edge.getFirstVertex().getPos(p0);
					edge.getSecondVertex().getPos(p1);
					transformUtil.projectToScreen(LOCAL, p0, p0);
					transformUtil.projectToScreen(LOCAL, p1, p1);
					line.setLine(p0.x, p0.y, p1.x, p1.y);
					double distanceSq = line.ptSegDistSq(x, y);
					if (distanceSq < min) {
						min = distanceSq;
						hit = edge;
					}
				}
			}
		}
		return hit;
	}
	
	public static class Hit {
		public SceneGraphNode node = null;
		public Object object = null;
		public double distanceSq = MIN_DIST_SQ;
	}
}
