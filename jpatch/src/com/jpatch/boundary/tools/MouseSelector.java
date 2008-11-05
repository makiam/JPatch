package com.jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.nio.*;
import java.util.*;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;
import javax.vecmath.*;

import trashcan.*;

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
	
	public static Selection.Type getBestSelectionType(Sds sds, int level, Set<AbstractVertex> vertices) {
		if (vertices.size() == 0) {
			return Selection.Type.VERTICES;
		}
		
		for (Face face : sds.getFaces(level)) {
			boolean faceContained = true;
			for (HalfEdge faceEdge : face.getEdges()) {
				if (!vertices.contains(faceEdge.getVertex())) {
					faceContained = false;
					break;
				}
			}
			if (faceContained) {
				return Selection.Type.FACES;
			}
		}
		for (HalfEdge edge : sds.getEdges(level, true)) {
			AbstractVertex v0 = edge.getVertex();
			AbstractVertex v1 = edge.getPairVertex();
			if (vertices.contains(v0) && vertices.contains(v1)) {
				return Selection.Type.EDGES;
			}
		}
		
		
		return Selection.Type.VERTICES;
	}
	
	public static void getVerticesUnderLasso(Viewport viewport, Polygon lasso, SdsModel sdsModel, int level, boolean visibleOnly, Collection<AbstractVertex> vertices) {
		vertices.clear();
		viewport.getViewDef().configureTransformUtil(transformUtil);
		sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
		Point3d p = new Point3d();
		for (AbstractVertex vertex : sdsModel.getSds().getVertices(level, true)) {
			vertex.getPosition(p);
			transformUtil.projectToScreen(WORLD, p, p);
			if (lasso.contains(p.x, p.y)) {
				if(!visibleOnly || viewport.getDepthAt((int) p.x, (int) p.y) < p.z) {
					vertices.add(vertex);
				}
			}	
		}
	}
	
	public static HitObject isHit(Viewport viewport, int mouseX, int mouseY, final double maxDistSq, Selection selection) {
		viewport.getViewDef().configureTransformUtil(transformUtil);
		selection.getNode().getLocal2WorldTransform(transformUtil, LOCAL);
		Point3d p0 = new Point3d();
		Point3d p1 = new Point3d();
		switch (selection.getType()) {
		case VERTICES:
			for (AbstractVertex vertex : selection.getVertices()) {
				vertex.getPosition(p0);
				transformUtil.projectToScreen(WORLD, p0, p0);
				double distSq = distSq(mouseX, mouseY, p0.x, p0.y);
				if (distSq < maxDistSq) {
					return new HitVertex(selection.getNode(), distSq, p0, vertex);
				}
			}
			break;
		case EDGES:
			for (HalfEdge edge : selection.getEdges()) {
				edge.getVertex().getPosition(p0);
				transformUtil.projectToScreen(WORLD, p0, p0);
				edge.getPairVertex().getPosition(p1);
				transformUtil.projectToScreen(WORLD, p1, p1);
				
				double t = Utils3d.closestPointOnLine(p0.x, p0.y, p1.x, p1.y, mouseX, mouseY);
				t = Math.max(0, Math.min(t, 1));
				p0.interpolate(p0, p1, t);
				double distSq = distSq(mouseX, mouseY, p0.x, p0.y);
				if (distSq < maxDistSq) {
					return new HitEdge(selection.getNode(), distSq, distSq, t, p0, edge);
				}
			}
			break;
		case FACES:
			for (Face face : selection.getFaces()) {
				HalfEdge[] egdes = face.getEdges();
				int n = face.getSides();
				Polygon polygon = new Polygon(new int[n], new int[n], n);
				for (int i = 0; i < n; i++) {
					egdes[i].getVertex().getPosition(p0);
					transformUtil.projectToScreen(WORLD, p0, p0);
					polygon.xpoints[i] = (int) Math.round(p0.x);
					polygon.ypoints[i] = (int) Math.round(p0.y);
				}
				if (polygon.contains(mouseX, mouseY)) {
					Point3d rayOrigin = new Point3d();
					Vector3d rayDirection = new Vector3d();
					setupCameraRay(rayOrigin, rayDirection, mouseX, mouseY);
					p0.set(mouseX, mouseY, getFaceHitZ(rayOrigin, rayDirection, face));
					return new HitFace(selection.getNode(), 0, p0, face);
				}
			}
			break;
		default:
			throw new RuntimeException(); // should never get here
		}
		return null;
	}
	
	private static double distSq(double x0, double y0, double x1, double y1) {
		double dx = x0 - x1;
		double dy = y0 - y1;
		return (dx * dx + dy * dy);
	}
	
	private static void setupCameraRay(Point3d rayOrigin, Vector3d rayDirection, int mouseX, int mouseY) {
		if (transformUtil.isPerspective()) {
			rayOrigin.set(mouseX, mouseY, 1);
			transformUtil.projectFromScreen(CAMERA, rayOrigin, rayOrigin);
			rayDirection.set(rayOrigin.x, rayOrigin.y, -rayOrigin.z);	// TODO: why -z ?!?
			rayOrigin.set(0, 0, 0);
		} else {
			rayOrigin.set(mouseX, mouseY, 0);
			transformUtil.projectFromScreen(CAMERA, rayOrigin, rayOrigin);
			rayDirection.set(0, 0, -1);
		}
	}
	
	public static HitObject getObjectAt(Viewport viewport, int mouseX, int mouseY, final double initMaxDistSq, SdsModel sdsModel, int level, int type, ObjectFilter objectFilter) {
		if (objectFilter == null) {
			objectFilter = ACCEPT_ANYTHING;
		}
		HitObject hitObject = new HitNull(initMaxDistSq);
		viewport.getViewDef().configureTransformUtil(transformUtil);
		sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
		Point3d p0 = new Point3d();
		Point3d p1 = new Point3d();
		Point3d pem = new Point3d();
		Point3d pec = new Point3d();
		
//		GLAutoDrawable glDrawable = (GLAutoDrawable) viewport.getComponent();
//		glDrawable.getContext().makeCurrent();
//		GL gl = glDrawable.getGL();
//		gl.glReadBuffer(GL_FRONT);
		
//		System.out.println(mouseX + "," + mouseY + " depth = " + viewportGl.getDepthAt(mouseX, mouseY));
		System.out.println("MouseSelector type=" + type);
		Sds sds = sdsModel.getSds();
		
		
		
		
		if ((type & Type.FACE) != 0) {
			Point3d rayOrigin = new Point3d();
			Vector3d rayDirection = new Vector3d();
			setupCameraRay(rayOrigin, rayDirection, mouseX, mouseY);
			
			for (Face face :sds.getFaces(level)) {
				if (objectFilter.accept(face)) {
					double z = getFaceHitZ(rayOrigin, rayDirection, face);
					if (z < Double.MAX_VALUE) {
						face.getMidpointPosition(p0);
						transformUtil.projectToScreen(WORLD, p0, p0);
						if(viewport.getDepthAt((int) p0.x, (int) p0.y) < z) {
							double distSq = distSq(mouseX, mouseY, p0);
							HitObject hitFace = new HitFace(sdsModel, distSq, new Point3d(mouseX, mouseY, 0), face);
							if (hitFace.isCloserThan(hitObject)) {
								hitObject = hitFace;
							}
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
							
						transformUtil.projectToScreen(WORLD, p0, p0);
						if(viewport.getDepthAt((int) p0.x, (int) p0.y) < p0.z) {
							double distSq = distSq(mouseX, mouseY, p0);
							HitObject hitVertex = new HitVertex(sdsModel, distSq, p0, vertex);
							if (hitVertex.isCloserThan(hitObject)) {
								hitObject = hitVertex;
							}
						}
					}
					if ((type & Type.LIMIT) != 0) {
						vertex.getLimit(p0);
						transformUtil.projectToScreen(WORLD, p0, p0);
						if(viewport.getDepthAt((int) p0.x, (int) p0.y) < p0.z) {
							double distSq = distSq(mouseX, mouseY, p0);
							HitObject hitVertex = new HitLimit(sdsModel, distSq, p0, vertex);
							if (hitVertex.isCloserThan(hitObject)) {
								hitObject = hitVertex;
							}
						}
					}	
				}
			}
		}
		
		if ((type & (Type.EDGE | Type.STRAY_EDGE | Type.BOUNDARY_EDGE)) != 0) {
			Iterable<HalfEdge> edges;
			if ((type & (Type.EDGE | Type.BOUNDARY_EDGE)) == 0) {
				edges = sds.getStrayEdges();
			} else {
				edges = sds.getEdges(level, (type & Type.STRAY_EDGE) != 0);
			}
			boolean boundaryOnly = ((type & Type.EDGE) == 0);
			boolean stray = ((type & Type.STRAY_EDGE) == 0);
			
			
			for (HalfEdge edge : edges) {
				System.out.println("edge: " + edge);
				if (edge.isPrimary() && objectFilter.accept(edge)) {
					if (!boundaryOnly || edge.isBoundary() || (stray || edge.isStray())) {
						edge.getVertex().getPosition(p0);
						transformUtil.projectToScreen(WORLD, p0, p0);
						edge.getPairVertex().getPosition(p1);
						transformUtil.projectToScreen(WORLD, p1, p1);
					
						double t = Utils3d.closestPointOnLine(p0.x, p0.y, p1.x, p1.y, mouseX, mouseY);
						t = Math.max(0, Math.min(t, 1));
						pec.interpolate(p0, p1, t);
						pem.interpolate(p0, p1, 0.5);
				
						if(viewport.getDepthAt((int) pec.x, (int) pec.y) < pec.z) {
							double cDistSq = distSq(mouseX, mouseY, pec);
							double mDistSq = distSq(mouseX, mouseY, pem);
							HitObject hitEdge = new HitEdge(sdsModel, mDistSq, cDistSq, t, pec, edge.getPrimary());
							if (hitEdge.isCloserThan(hitObject)) {
								hitObject = hitEdge;
							}
						}
					}
				}
			}
		}
//		glDrawable.getContext().release();
		return hitObject instanceof HitNull ? null : hitObject;
	}
	
	private static Point3d faceMidpoint = new Point3d();
	private static Point3d faceP0 = new Point3d();
	private static Point3d faceP1 = new Point3d();
	
	private static double getFaceHitZ(Point3d rayOrigin, Vector3d rayDirection, Face face) {
		face.getMidpointPosition(faceMidpoint);
		transformUtil.transform(WORLD, faceMidpoint, CAMERA, faceMidpoint);
		for (HalfEdge faceEdge : face.getEdges()) {
			faceEdge.getVertex().getPosition(faceP0);
			transformUtil.transform(WORLD, faceP0, CAMERA, faceP0);
			faceEdge.getPairVertex().getPosition(faceP1);
			transformUtil.transform(WORLD, faceP1, CAMERA, faceP1);
			double t = Utils3d.rayTriangleIntersection(rayOrigin, rayDirection, faceMidpoint, faceP0, faceP1);
			if (t < Double.MAX_VALUE) {
				double z = rayOrigin.z + rayDirection.z * t;
				return z;
			}
		}
		return Double.MAX_VALUE;
	}
	
	private static double distSq(int mouseX, int mouseY, Point3d p) {
		double dx = p.x - mouseX;
		double dy = p.y - mouseY;
		return dx * dx + dy * dy;
	}
	
	public abstract static class HitObject {
		public final XFormNode node;
		public final double centerDistSq;
		public final double edgeDistSq;
		public final Point3d screenPosition;
		private HitObject(XFormNode node, double centerDistSq, double edgeDistSq, Point3d screenPosition) {
			assert edgeDistSq <= centerDistSq;
			this.node = node;
			this.centerDistSq = centerDistSq;
			this.edgeDistSq = edgeDistSq;
			this.screenPosition = screenPosition == null ? null : new Point3d(screenPosition.x, screenPosition.y, 0);
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof HitObject)) {
				return false;
			}
			HitObject other = (HitObject) o;
			return node == other.node;
		}
		
		public abstract Selection.Type getType();
		
		public abstract void getVertices(Collection<AbstractVertex> vertices);
		
		public abstract boolean isCloserThan(HitObject o);
	}
	
	public static class HitNull extends HitObject {

		private HitNull(double distanceSq) {
			super(null, distanceSq, distanceSq, null);
		}
		
		@Override
		public void getVertices(Collection<AbstractVertex> vertices) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isCloserThan(HitObject o) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean equals(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public com.jpatch.boundary.Selection.Type getType() {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class HitVertex extends HitObject {
		public final AbstractVertex vertex;
		private HitVertex(XFormNode node, double distanceSq, Point3d screenPosition, AbstractVertex vertex) {
			super(node, distanceSq, distanceSq, screenPosition);
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

		@Override
		public boolean isCloserThan(HitObject o) {
			if (o instanceof HitVertex) {
				return edgeDistSq < o.edgeDistSq;
			} else if (o instanceof HitEdge) {
				return edgeDistSq < o.centerDistSq;
			} else if (o instanceof HitFace) {
				return edgeDistSq < o.centerDistSq;
			} else if (o instanceof HitNull) {
				return edgeDistSq < o.edgeDistSq;
			} else {
				throw new AssertionError();
			}
		}
		
		@Override
		public com.jpatch.boundary.Selection.Type getType() {
			return Selection.Type.VERTICES;
		}
	}
	
	public static class HitLimit extends HitVertex {
		private HitLimit(XFormNode node, double distanceSq, Point3d screenPosition, AbstractVertex vertex) {
			super(node, distanceSq, screenPosition,vertex);
		}
	}
	
	public static class HitEdge extends HitObject {
		public final HalfEdge halfEdge;
		public final Double position;
		
		private HitEdge(XFormNode node, double centerDistSq, double edgeDistSq, double position, Point3d screenPosition, HalfEdge halfEdge) {
			super(node, centerDistSq, edgeDistSq, screenPosition);
			this.halfEdge = halfEdge;
			this.position = position;
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
		
		@Override
		public boolean isCloserThan(HitObject o) {
			if (o instanceof HitVertex) {
				return centerDistSq < o.edgeDistSq;
			} else if (o instanceof HitEdge) {
				return edgeDistSq < o.edgeDistSq;
			} else if (o instanceof HitFace) {
				return edgeDistSq < o.centerDistSq;
			} else if (o instanceof HitNull) {
				return edgeDistSq < o.edgeDistSq;
			} else {
				throw new AssertionError();
			}
		}
		
		@Override
		public com.jpatch.boundary.Selection.Type getType() {
			return Selection.Type.EDGES;
		}
	}
	
	public static class HitFace extends HitObject {
		public final Face face;
		private HitFace(XFormNode node, double distanceSq, Point3d screenPosition, Face face) {
			super(node, distanceSq, 0, screenPosition);
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
		
		@Override
		public boolean isCloserThan(HitObject o) {
			if (o instanceof HitVertex) {
				return centerDistSq < o.edgeDistSq;
			} else if (o instanceof HitEdge) {
				return centerDistSq < o.edgeDistSq;
			} else if (o instanceof HitFace) {
				return centerDistSq < o.centerDistSq;
			} else if (o instanceof HitNull) {
				return edgeDistSq < o.edgeDistSq;
			} else {
				throw new AssertionError();
			}
		}
		
		@Override
		public com.jpatch.boundary.Selection.Type getType() {
			return Selection.Type.FACES;
		}
		
		@Override
		public String toString() {
			return "Hitface:" + face;
		}
	}
	
	public static boolean isSelectionTrigger(MouseEvent event) {
		return event.isShiftDown();
	}

	
}
