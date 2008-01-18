package com.jpatch.boundary.tools;

import java.awt.*;
import java.awt.geom.Line2D;

import javax.vecmath.*;

import com.jpatch.afw.attributes.CollectionAttr;
import com.jpatch.afw.vecmath.Transform;
import com.jpatch.afw.vecmath.TransformUtil;
import static com.jpatch.afw.vecmath.TransformUtil.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.SceneGraphNode;
import com.jpatch.entity.SdsModel;
import com.jpatch.entity.sds2.*;

public class MouseSelector {
	static final private double MIN_DIST_SQ = 64;
	static final TransformUtil transformUtil = new TransformUtil();
	
	public static Hit getVertexAt(Viewport viewport, Point point, boolean noProjection) {	
		return getVertexAt(viewport, point, Main.getInstance().getSceneGraphRoot(), new Hit(), noProjection);
	}
	
	public static Hit getVertexAt(Viewport viewport, Point point, SceneGraphNode node, boolean noProjection) {
		return getVertexAt(viewport, point, node, new Hit(), noProjection);
	}
	
	public static Hit getVertexAt(Viewport viewport, Point point, Hit hit, boolean noProjection) {
		return getVertexAt(viewport, point, Main.getInstance().getSceneGraphRoot(), hit, noProjection);
	}
	
	public static Hit getVertexAt(Viewport viewport, Point point, SceneGraphNode node, Hit hit, boolean noProjection) {
		if (node instanceof SdsModel) {
			SdsModel sdsModel = (SdsModel) node;
			getVertexAt(viewport, point, sdsModel, hit, noProjection);
		}
		for (SceneGraphNode child : node.getChildrenAttribute().getElements()) {
			getVertexAt(viewport, point, child, hit, noProjection);
		}
		return hit;
	}
	
	public static void getVertexAt(Viewport viewport, Point point, SdsModel sdsModel, Hit hit, boolean noProjection) {
		ViewDef viewDef = viewport.getViewDef();
		Point hitPoint = new Point();
//		Matrix4d matrix = new Matrix4d(viewDef.getMatrix(new Matrix4d()));
//		Transform transform = sdsModel.getTransform();
//		transform.mul2(matrix);
		
//		Matrix4d matrix = viewport.getViewDef().getMatrix(new Matrix4d());
		boolean useProjection = !viewport.getViewDef().getShowControlMeshAttribute().getBoolean() && !noProjection;
		Point3d p = new Point3d();
//		System.out.println("getVertexAt(" + x + ", " + y + ")");
		viewDef.configureTransformUtil(transformUtil);
		sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
		for (Face face : sdsModel.getSds().getFaces(Globals.getInstance().getEditLevelAttribute().getInt())) {
			for (HalfEdge edge : face.getEdges()) {
				AbstractVertex vertex = edge.getVertex();
				if (useProjection) {
					vertex.getVertexPoint().getLimit(p);
				} else {
					vertex.getPosition(p);
				}
				transformUtil.projectToScreen(transformUtil.LOCAL, p, p);
				double dx = point.x - p.x;
				double dy = point.y - p.y;
				double distanceSq = dx * dx + dy * dy;
				if (distanceSq < hit.distanceSq) {
					hit.node = sdsModel;
					hit.distanceSq = distanceSq;
					hit.object = vertex;
					hitPoint.setLocation(p.x, p.y);
				}
			}
		}
		point.setLocation(hitPoint);
	}
	
	public static void getVertices(Viewport viewport, int x0, int y0, int x1, int y1, SdsModel sdsModel, Selection selection) {
		CollectionAttr<AbstractVertex> selectedVertices = selection.getSelectedVerticesAttribute();
		selectedVertices.clear();
		
		ViewDef viewDef = viewport.getViewDef();
		
		viewDef.configureTransformUtil(transformUtil);
		sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
		
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
		
		Point3d p = new Point3d();
		boolean useProjection = !viewport.getViewDef().getShowControlMeshAttribute().getBoolean();
		for (Face face : sdsModel.getSds().getFaces(Globals.getInstance().getEditLevelAttribute().getInt())) {
			for (HalfEdge edge : face.getEdges()) {
				AbstractVertex vertex = edge.getVertex();
				if (useProjection) {
					vertex.getVertexPoint().getLimit(p);
				} else {
					vertex.getPosition(p);
				}
				transformUtil.projectToScreen(transformUtil.LOCAL, p, p);
				if (p.x >= x0 && p.x <= x1 && p.y >= y0 && p.y <= y1) {
					selectedVertices.add(vertex);
				}
			}
		}		
	}
	
	public static HalfEdge getEdgeAt(Viewport viewport, int x, int y, SdsModel sdsModel) {
		ViewDef viewDef = viewport.getViewDef();
		
		viewDef.configureTransformUtil(transformUtil);
		sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
		
//		Matrix4d matrix = viewport.getViewDef().getMatrix(new Matrix4d());
		Point3d p0 = new Point3d();
		Point3d p1 = new Point3d();
		HalfEdge hit = null;
		double min = MIN_DIST_SQ;
		Line2D.Double line = new Line2D.Double();
		for (Face face : sdsModel.getSds().getFaces(0)) {
			for (HalfEdge edge : face.getEdges()) {
//				if (edge.isPrimary()) {
					edge.getVertex().getPosition(p0);
					edge.getPairVertex().getPosition(p1);
					transformUtil.projectToScreen(transformUtil.LOCAL, p0, p0);
					transformUtil.projectToScreen(transformUtil.LOCAL, p1, p1);
					line.setLine(p0.x, p0.y, p1.x, p1.y);
					double distanceSq = line.ptSegDistSq(x, y);
					if (distanceSq < min) {
						min = distanceSq;
						hit = edge;
					}
				}
			}
//		}
		return hit;
	}
	
	public static class Hit {
		public SceneGraphNode node = null;
		public Object object = null;
		public double distanceSq = MIN_DIST_SQ;
	}
}
