package com.jpatch.boundary.tools;

import java.awt.geom.Line2D;

import javax.vecmath.*;

import com.jpatch.boundary.*;
import com.jpatch.entity.sds.*;

public class MouseSelector {
	static final private double MIN_DIST_SQ = 64;
	
	public static TopLevelVertex getVertexAt(Viewport viewport, double x, double y, Sds sds) {
		if (sds == null) {
			return null;
		}
		
		ViewDef viewDef = viewport.getViewDef();
//		Matrix4d matrix = viewport.getViewDef().getMatrix(new Matrix4d());
		Point3d p = new Point3d();
		x -= (viewport.getComponent().getWidth() >> 1);
		y = (viewport.getComponent().getHeight() >> 1) - y;
		System.out.println("getVertexAt(" + x + ", " + y + ")");
		TopLevelVertex hit = null;
		double min = MIN_DIST_SQ;
		for (Face face : sds.faceList) {
			for (HalfEdge edge : face.getEdges()) {
				TopLevelVertex vertex = edge.getFirstVertex();
				vertex.getPos(p);
				viewDef.transform(p);
				System.out.println(p);
				double dx = x - p.x;
				double dy = y - p.y;
				double distanceSq = dx * dx + dy * dy;
				if (distanceSq < min) {
					min = distanceSq;
					hit = vertex;
				}
			}
		}
		return hit;
	}
	
	public static HalfEdge getEdgeAt(Viewport viewport, double x, double y, Sds sds) {
		if (sds == null) {
			return null;
		}
		ViewDef viewDef = viewport.getViewDef();
//		Matrix4d matrix = viewport.getViewDef().getMatrix(new Matrix4d());
		Point3d p0 = new Point3d();
		Point3d p1 = new Point3d();
		x -= (viewport.getComponent().getWidth() >> 1);
		y = (viewport.getComponent().getHeight() >> 1) - y;
		HalfEdge hit = null;
		double min = MIN_DIST_SQ;
		Line2D.Double line = new Line2D.Double();
		for (Face face : sds.faceList) {
			for (HalfEdge edge : face.getEdges()) {
				if (edge.isPrimary()) {
					edge.getFirstVertex().getPos(p0);
					edge.getSecondVertex().getPos(p1);
					viewDef.transform(p0);
					viewDef.transform(p1);
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
}
