package com.jpatch.boundary.tools;

import java.awt.*;
import java.awt.geom.*;
import java.nio.*;
import java.util.*;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;
import javax.vecmath.*;

import com.jpatch.afw.vecmath.*;
import static com.jpatch.afw.vecmath.TransformUtil.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;
import static com.jpatch.entity.sds2.Sds.*;
import com.sun.opengl.util.*;


public class MouseSelector {
	static final private double MIN_DIST_SQ = 64;
	static final TransformUtil transformUtil = new TransformUtil();
	static final FloatBuffer buffer = BufferUtil.newFloatBuffer(1);
	
	static final ObjectFilter ACCEPT_ANYTHING = new ObjectFilter() {
		public boolean accept(Object o) {
			return true;
		}
	};
	
//	public static Selection.State getVertices(Viewport viewport, int x0, int y0, int x1, int y1, SdsModel sdsModel, int level) {
//		Selection selection = new Selection();
//		selection.switchType(Selection.Type.VERTICES, null);
//		viewport.getViewDef().configureTransformUtil(transformUtil);
//		sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
//		Point3d p = new Point3d();
//		for (Face face : sdsModel.getSds().getFaces(level)) {
//			for (HalfEdge edge : face.getEdges()) {
//				AbstractVertex vertex = edge.getVertex();
//				vertex.getPosition(p);
//				transformUtil.projectToScreen(LOCAL, p, p);
//				if (x0 <= p.x && p.x <= x1 && y0 <= p.y && p.y <= y1) {
//					selection.addVertex(vertex, null);
//				}
//			}
//		}
//		return new Selection.State(selection);
//	}
	
	public static void getVerticesUnderLasso(ViewportGl viewport, Polygon lasso, SdsModel sdsModel, int level, boolean visibleOnly, Collection<AbstractVertex> vertices) {
		vertices.clear();
		viewport.getViewDef().configureTransformUtil(transformUtil);
		sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
		Point3d p = new Point3d();
		for (AbstractVertex vertex : sdsModel.getSds().getVertices(level, true)) {
			vertex.getPosition(p);
			transformUtil.projectToScreen(LOCAL, p, p);
			if (lasso.contains(p.x, p.y)) {
				if(!visibleOnly || viewport.getDepthAt((int) p.x, (int) p.y) < p.z) {
					vertices.add(vertex);
				}
			}	
		}
	}
	
	public static HitObject getObjectAt(Viewport viewport, int mouseX, int mouseY, double initMaxDistSq, SdsModel sdsModel, int level, int type, ObjectFilter objectFilter) {
		if (objectFilter == null) {
			objectFilter = ACCEPT_ANYTHING;
		}
		double maxDistSq = initMaxDistSq;
		HitObject hitObject = null;
		viewport.getViewDef().configureTransformUtil(transformUtil);
		sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
		Point3d p0 = new Point3d();
		Point3d p1 = new Point3d();
		Point3d pem = new Point3d();
		Point3d pec = new Point3d();
		
		ViewportGl viewportGl = (ViewportGl) viewport;
		GLAutoDrawable glDrawable = (GLAutoDrawable) viewportGl.getComponent();
		glDrawable.getContext().makeCurrent();
		GL gl = glDrawable.getGL();
		gl.glReadBuffer(GL_FRONT);
		
//		System.out.println(mouseX + "," + mouseY + " depth = " + viewportGl.getDepthAt(mouseX, mouseY));
		Sds sds = sdsModel.getSds();
		
		if ((type & Type.FACE) != 0) {
			for (Face face :sds.getFaces(level)) {
				if (objectFilter.accept(face)) {
					face.getMidpointPosition(p0);
					transformUtil.projectToScreen(LOCAL, p0, p0);
					if(viewportGl.getDepthAt((int) p0.x, (int) p0.y) < p0.z) {
						double distSq = distSq(mouseX, mouseY, p0);
						if ((hitObject != null && distSq < hitObject.distanceSq) || (hitObject == null && distSq < maxDistSq)) {
							hitObject = new HitFace(sdsModel, distSq, p0, face);
						}
					}
				}
			}
		}
		
		if ((type & (Type.EDGE | Type.STRAY_EDGE)) != 0) {
			Iterable<HalfEdge> edges;
			if ((type & Type.EDGE) == 0) {
				edges = sds.getStrayEdges();
			} else {
				edges = sds.getEdges(level, (type & Type.STRAY_EDGE) != 0);
			}
			for (HalfEdge edge : edges) {
				if (objectFilter.accept(edge)) {
					edge.getVertex().getPosition(p0);
					transformUtil.projectToScreen(TransformUtil.LOCAL, p0, p0);
					edge.getPairVertex().getPosition(p1);
					transformUtil.projectToScreen(TransformUtil.LOCAL, p1, p1);
				
					double t = Utils3d.closestPointOnLine(p0.x, p0.y, p1.x, p1.y, mouseX, mouseY);
					t = Math.max(0, Math.min(t, 1));
					pec.interpolate(p0, p1, t);
					t = Math.max(0.1, Math.min(t, 0.9));
					pem.interpolate(p0, p1, 0.5);
			
					if(viewportGl.getDepthAt((int) pec.x, (int) pec.y) < pec.z) {
						double cDistSq = distSq(mouseX, mouseY, pec);
						double mDistSq = distSq(mouseX, mouseY, pem);
						
						if ((hitObject != null && mDistSq < hitObject.distanceSq) || (hitObject == null && cDistSq < maxDistSq)) {
							hitObject = new HitEdge(sdsModel, mDistSq, pem, edge.getPrimary());
						}
					}
				}
			}
		}
		if ((type & (Type.VERTEX | Type.STRAY_VERTEX | Type.LIMIT)) != 0) {
			Iterable<? extends AbstractVertex> vertices;
			if ((type & (Type.VERTEX | Type.LIMIT)) == 0) {
				vertices = sds.getStrayVertices();
			} else {
				vertices = sds.getVertices(level, (type & Type.STRAY_VERTEX) != 0);
			}
			for (AbstractVertex vertex : vertices) {
				if (objectFilter.accept(vertex)) {
					if ((type & (Type.VERTEX | Type.STRAY_VERTEX)) != 0) {
						vertex.getPosition(p0);
							
						transformUtil.projectToScreen(TransformUtil.LOCAL, p0, p0);
						if(viewportGl.getDepthAt((int) p0.x, (int) p0.y) < p0.z) {
							double distSq = distSq(mouseX, mouseY, p0);
							if ((hitObject != null && distSq < hitObject.distanceSq) || (hitObject == null && distSq < maxDistSq)) {
								hitObject = new HitVertex(sdsModel, distSq, p0, vertex);
							}
						}
					}
					if ((type & Type.LIMIT) != 0) {
						vertex.getLimit(p0);
						transformUtil.projectToScreen(TransformUtil.LOCAL, p0, p0);
						if(viewportGl.getDepthAt((int) p0.x, (int) p0.y) < p0.z) {
							double distSq = distSq(mouseX, mouseY, p0);
							if ((hitObject != null && distSq < hitObject.distanceSq) || (hitObject == null && distSq < maxDistSq)) {
								hitObject = new HitVertex(sdsModel, distSq, p0, vertex);
							}
						}
					}	
				}
			}
		}
		glDrawable.getContext().release();
		return hitObject;
	}
	
	
	
	private static double distSq(int mouseX, int mouseY, Point3d p) {
		double dx = p.x - mouseX;
		double dy = p.y - mouseY;
		return dx * dx + dy * dy;
	}
	
	public abstract static class HitObject {
		public final XFormNode node;
		public final double distanceSq;
		public final Point3d screenPosition;
		private HitObject(XFormNode node, double distanceSq, Point3d screenPosition) {
			this.node = node;
			this.distanceSq = distanceSq;
			this.screenPosition = new Point3d(screenPosition);
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof HitObject)) {
				return false;
			}
			HitObject other = (HitObject) o;
			return node == other.node;
		}
		
		public abstract void getVertices(Collection<AbstractVertex> vertices);
	}
	
	
	public static class HitVertex extends HitObject {
		public final AbstractVertex vertex;
		private HitVertex(XFormNode node, double distanceSq, Point3d screenPosition, AbstractVertex vertex) {
			super(node, distanceSq, screenPosition);
			this.vertex = vertex;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof HitVertex)) {
				return false;
			}
			HitVertex other = (HitVertex) o;
			return super.equals(o) && vertex == other.vertex;
		}
		
		@Override
		public void getVertices(Collection<AbstractVertex> vertices) {
			vertices.add(vertex);
		}
	}
	
	public static class HitEdge extends HitObject {
		public final HalfEdge halfEdge;
		private HitEdge(XFormNode node, double distanceSq, Point3d screenPosition, HalfEdge halfEdge) {
			super(node, distanceSq, screenPosition);
			this.halfEdge = halfEdge;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof HitEdge)) {
				return false;
			}
			HitEdge other = (HitEdge) o;
			return super.equals(o) && halfEdge == other.halfEdge;
		}
		
		@Override
		public void getVertices(Collection<AbstractVertex> vertices) {
			vertices.add(halfEdge.getVertex());
			vertices.add(halfEdge.getPairVertex());
		}
	}
	
	public static class HitFace extends HitObject {
		public final Face face;
		private HitFace(XFormNode node, double distanceSq, Point3d screenPosition, Face face) {
			super(node, distanceSq, screenPosition);
			this.face = face;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof HitFace)) {
				return false;
			}
			HitFace other = (HitFace) o;
			return super.equals(o) && face == other.face;
		}
		
		@Override
		public void getVertices(Collection<AbstractVertex> vertices) {
			for (HalfEdge edge : face.getEdges()) {
				vertices.add(edge.getVertex());
			}
		}
	}
	
	

	
}
