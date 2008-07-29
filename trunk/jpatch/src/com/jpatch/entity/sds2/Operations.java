package com.jpatch.entity.sds2;//////**********************************

import com.jpatch.afw.control.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.*;

import java.util.*;

import javax.vecmath.*;

public class Operations {
	
	public static void lathe(SdsModel sdsModel, Point3d[][] lathedPoints, BaseVertex[][] lathedVertices, Material material, List<JPatchUndoableEdit> editList) {
		Sds sds = sdsModel.getSds();
		Map<Point3d, BaseVertex> vertexMap = new HashMap<Point3d, BaseVertex>();
		for (Point3d[] segment : lathedPoints) {
			for (Point3d point : segment) {
				if (!vertexMap.containsKey(point)) {
					vertexMap.put(point, new BaseVertex(sdsModel, point.x, point.y, point.z));
				}
			}
		}
//		for (Point3d[] segment : lathedPoints) {
//			for (Point3d point : segment) {
//				System.out.println(point + " => " + vertexMap.get(point));
//			}
//		}
		int points = lathedPoints[0].length;
		for (int segment = 0; segment < lathedPoints.length - 1; segment++) {
			for (int i = 0; i < points - 1; i++) {
				BaseVertex v0 = vertexMap.get(lathedPoints[segment][i]);
				BaseVertex v1 = vertexMap.get(lathedPoints[segment][i + 1]);
				BaseVertex v2 = vertexMap.get(lathedPoints[segment + 1][i + 1]);
				BaseVertex v3 = vertexMap.get(lathedPoints[segment + 1][i]);
				lathedVertices[segment][i] = v0;
				lathedVertices[segment][i + 1] = v1;
				lathedVertices[segment + 1][i + 1] = v2;
				lathedVertices[segment + 1][i] = v3;
				if (v0 == v3) {
					sds.addFace(editList, 0, material, v0, v1, v2);
				} else if (v1 == v2) {
					sds.addFace(editList, 0, material, v0, v1, v3);
				} else {
					sds.addFace(editList, 0, material, v0, v1, v2, v3);
				}
			}
		}
	}
	
	public static void getLathedVertices(Sds sds, BaseVertex[] vertices, Point3d startAxis, Point3d endAxis, double epsilon, double angle, Point3d[][] lathedPoints, boolean[] snap) {
		Vector3d axis = new Vector3d();
		axis.sub(endAxis, startAxis);
		AxisAngle4d axisAngle = new AxisAngle4d(axis, 0);
		Matrix4d matrix = new Matrix4d();
		
		Point3d pc = new Point3d();
		double epsilonSquared = epsilon * epsilon;
		for (int segment = 0; segment < lathedPoints.length; segment++) {
			if (segment == 0 || (angle == 360 && segment == lathedPoints.length - 1)) {
				axisAngle.angle = 0;
			} else {
				axisAngle.angle = Math.toRadians(angle / (lathedPoints.length - 1) * segment);
			}
			matrix.set(axisAngle);
			
//			lathe.set(translate0);
//			lathe.mul(rotate);
//			lathe.mul(translate1);
			
			
			for (int i = 0; i < vertices.length; i++) {
				Point3d p = lathedPoints[segment][i];
				vertices[i].getPosition(p);
				p.sub(startAxis);
				matrix.transform(p);
				p.add(startAxis);
				
				/* check epsilon for first and last points */
				if ((i == 0 || i == vertices.length - 1) && vertices[i].getEdges().length == 1) {
					int idx = (i == 0) ? 0 : 1;
					vertices[i].getPosition(pc);
					/* set pc to the point on the axis closest to p */
					double t = Utils3d.closestPointOnLine(startAxis, endAxis, pc);
					pc.interpolate(startAxis, endAxis, t);
					
					/* if closer than epsilon, project p to axis */
					if (p.distanceSquared(pc) <= epsilonSquared) {
						p.set(pc);
						snap[idx] = true;
					} else {
						snap[idx] = false;
					}
				}
			}
		}
	}
	
	public static Map<BaseVertex, BaseVertex> extrude(Selection selection, List<JPatchUndoableEdit> editList) {
		/* check if faces are to be extruded */
		Collection<Face> selectedFaces = selection.getFaces();
		if (selectedFaces.size() > 0) {
			return extrudeFaces(selection, selectedFaces, editList);
		} 
		
		/* if not, check if edges are to be extruded */
		Collection<HalfEdge> selectedEdges = selection.getEdges();
		if (selectedEdges.size() > 0) {
			return extrudeEdges(selection, selectedEdges, editList);
		}
		
		/* if not, do nothing and return empty map */
		return new HashMap<BaseVertex, BaseVertex>();
	}
	
	private static Map<BaseVertex, BaseVertex> extrudeEdges(Selection selection, Collection<HalfEdge> selectedEdges, List<JPatchUndoableEdit> editList) {
		Sds sds = selection.getSdsModel().getSds();
		Map<BaseVertex, BaseVertex> boundaryVertices = new HashMap<BaseVertex, BaseVertex>();
		Point3d position = new Point3d();
		BaseVertex[] edgeVertices = new BaseVertex[2];
		BaseVertex[] faceVertices = new BaseVertex[4];
		Material material = Main.getInstance().getDefaultMaterial();
		for (HalfEdge edge : selectedEdges) {
			if (edge.getFace() != null) {
				material = edge.getFace().getMaterial();
				edge = edge.getPair();
			}
			if (!edge.isBoundary() && !edge.isStray()) {
				throw new IllegalArgumentException("can't extrude non-boundary-edge " + edge);
			}
			for (AbstractVertex vertex : edge.getVertices(edgeVertices)) {
				if (!boundaryVertices.containsKey(vertex)) {
					vertex.getPosition(position);
					BaseVertex extrudedVertex = new BaseVertex(selection.getSdsModel());
					extrudedVertex.setPosition(position);
					boundaryVertices.put((BaseVertex) vertex, extrudedVertex);
				}
			}
			faceVertices[0] = edgeVertices[0];
			faceVertices[1] = edgeVertices[1];
			faceVertices[2] = boundaryVertices.get(edgeVertices[1]);
			faceVertices[3] = boundaryVertices.get(edgeVertices[0]);
			System.out.println(Arrays.toString(faceVertices));
			sds.addFace(editList, 0, material, faceVertices);
		}
		
		/* change selection */
		Selection.Type selectionType = selection.getType();
		selection.clear(editList);
		Collection<AbstractVertex> newSelection = new HashSet<AbstractVertex>();
		for (BaseVertex boundaryVertex : boundaryVertices.keySet()) {
			newSelection.add(boundaryVertices.get(boundaryVertex));
		}
		selection.addVertices(newSelection, editList);
		selection.setType(selectionType, editList);
		
		return boundaryVertices;
	}
	
	private static Map<BaseVertex, BaseVertex> extrudeFaces(Selection selection, Collection<Face> selectedFaces, List<JPatchUndoableEdit> editList) {
		Sds sds = selection.getSdsModel().getSds();
		
		Map<BaseVertex, BaseVertex> boundaryVertices = new HashMap<BaseVertex, BaseVertex>();
		Set<BaseVertex> innerVertices = new HashSet<BaseVertex>((Collection<BaseVertex>) selection.getVertices());
		Map<HalfEdge, Material> boundaryEdges = new HashMap<HalfEdge, Material>();
		Set<Face> boundaryFaces = new HashSet<Face>();
		Set<Face> innerFaces = new HashSet<Face>();
		
		boolean singleFace = false;
		if (selectedFaces.size() == 1) {
			singleFace = true;
			for (HalfEdge edge : selectedFaces.iterator().next().getEdges()) {
				if (edge.getPairFace() != null) {
					singleFace = false;
					break;
				}
			}
		}
		
		/* 
		 * all faces that have a neighbor that isn't selected (including faces at mesh boundaries)
		 * are added to the boundaryFaces set. The edges on these boundaries are added to the
		 * boundaryEdge set.
		 */
		for (Face face : selectedFaces) {
			for (HalfEdge edge : face.getEdges()) {
				if (!selectedFaces.contains(edge.getPairFace())) {
					boundaryEdges.put(edge, face.getMaterial());
					boundaryFaces.add(face);
					innerVertices.remove(edge.getVertex());
					innerVertices.remove(edge.getPairVertex());
				}
			}
		}
		/*
		 * create a set innerFaces containing all selected faces that are not boundary faces
		 */
		innerFaces.addAll(selectedFaces);
		innerFaces.removeAll(boundaryFaces);
		
//		/*
//		 * move vertices on inner faces
//		 */
//		for (Face innerFace : innerFaces) {
//			for (HalfEdge edge : innerFace.getEdges()) {
//				BaseVertex innerVertex = (BaseVertex) edge.getVertex();
//				assert innerVertex != null;
//				innerVertices.add(innerVertex);
////				innerVertex.getPosition(position);
////				innerVertex.getVertexPoint().getNormal(normal);
////				position.add(normal);
////				addEdit(editList, AttributeEdit.changeAttribute(innerVertex.positionAttr, position, true));
//			}
//		}
		
		/* create new extruded vertices for all boundary edges */
		BaseVertex[] edgeVertices = new BaseVertex[2];
		
		Point3d position = new Point3d();
		for (HalfEdge boundaryEdge : boundaryEdges.keySet()) {
			boundaryEdge.getVertices(edgeVertices);
			for (AbstractVertex edgeVertex : edgeVertices) {
				assert edgeVertex != null;
				if (!boundaryVertices.containsKey(edgeVertex)) {
					edgeVertex.getPosition(position);
//					edgeVertex.getVertexPoint().getNormal(normal);
//					position.add(normal);
					BaseVertex extrudedVertex = new BaseVertex(selection.getSdsModel());
					extrudedVertex.setPosition(position);
					boundaryVertices.put((BaseVertex) edgeVertex, extrudedVertex);
				}
			}
		}
		
		/* delete and recreate boundary faces */
		for (Face boundaryFace : boundaryFaces) {
			AbstractVertex[] newVertices = new AbstractVertex[boundaryFace.getSides()];
			for (int i = 0; i < boundaryFace.getSides(); i++) {
				AbstractVertex vertex = boundaryFace.getEdges()[i].getVertex();
				AbstractVertex newVertex = boundaryVertices.get(vertex);
				//if this vertex hasn't been extruded, use the old vertex instead
				newVertices[i] = newVertex == null ? vertex : newVertex;
			}
			
			if (singleFace) {
				/* reverse the face */
				AbstractVertex[] vertices = new AbstractVertex[boundaryFace.getSides()];
				HalfEdge[] faceEdges = boundaryFace.getEdges();
				for (int i = 0; i < vertices.length; i++) {
					vertices[i] = faceEdges[vertices.length - i - 1].getVertex();
				}
				Material material = boundaryFace.getMaterial();
				sds.removeFace(editList, 0, boundaryFace);
				sds.addFace(editList, 0, material, vertices);
			} else {
				sds.removeFace(editList, 0, boundaryFace);
			}
			sds.addFace(editList, 0, boundaryFace.getMaterial(), newVertices);
		}
		
		/* create extrude-faces */
		AbstractVertex[] newVertices = new AbstractVertex[4];
		for (HalfEdge boundaryEdge : boundaryEdges.keySet()) {
			newVertices[0] = boundaryEdge.getVertex();
			newVertices[1] = boundaryEdge.getPairVertex();
			newVertices[2]  = boundaryVertices.get(boundaryEdge.getPairVertex());
			newVertices[3]  = boundaryVertices.get(boundaryEdge.getVertex());
			sds.addFace(editList, 0, boundaryEdges.get(boundaryEdge), newVertices);
		}
		
		/* change selection */
		Selection.Type selectionType = selection.getType();
		selection.clear(editList);
		Collection<AbstractVertex> newSelection = new HashSet<AbstractVertex>(innerVertices);
		for (BaseVertex boundaryVertex : boundaryVertices.keySet()) {
			newSelection.add(boundaryVertices.get(boundaryVertex));
		}
		selection.addVertices(newSelection, editList);
		selection.setType(selectionType, editList);
		
		/* return old-to-new vertex mapping */
		for (BaseVertex v : innerVertices) {
			boundaryVertices.put(v, v);
		}
		return boundaryVertices;
	}
	
	private static void addEdit(List<JPatchUndoableEdit> editList, JPatchUndoableEdit edit) {
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	public static boolean canWeldEdges(HalfEdge source, Collection<HalfEdge> selectedEdges, HalfEdge target) {
		if (!source.isBoundary() || !target.isBoundary()) {
			return false;
		}
		source = source.faceEdge();
		target = target.faceEdge();
		
		final List<HalfEdge> sourceEdges = HalfEdge.continguousEdges(source, selectedEdges);
		final List<HalfEdge> targetEdges = HalfEdge.continguousEdges(target, null);
//		final boolean sourceLooped = HalfEdge.isLooped(sourceEdges);
//		final boolean targetLooped = HalfEdge.isLooped(targetEdges);
		final int sourceIndex = sourceEdges.indexOf(source);
		final int targetIndex = targetEdges.indexOf(target);
		System.out.println("WELD EDGES");
		System.out.println("source=" + source + " -> " + sourceEdges);
		System.out.println("target=" + target + " -> " + targetEdges);
		System.out.println("Source: size=" + sourceEdges.size() + " index=" + sourceIndex);
		System.out.println("Target: size=" + targetEdges.size() + " index=" + targetIndex);
		Point3d p = new Point3d();
		if (targetEdges.size() > sourceEdges.size()) {
			int targetStart = targetEdges.size() + sourceIndex - 1;
			for (int i = 0; i < sourceEdges.size(); i++) {
				HalfEdge sourceEdge = sourceEdges.get(i);
				HalfEdge targetEdge = targetEdges.get((targetStart + targetEdges.size() - i) % targetEdges.size());
				targetEdge.getPairVertex().getPosition(p);
				sourceEdge.getVertex().setPosition(p);
				targetEdge.getVertex().getPosition(p);
				sourceEdge.getPairVertex().setPosition(p);
			}
		}
		return false;
	}
	
	
	
//	private static final class DoubleLinkedListElement<T> implements Iterable<T> {
//		private final T element;
//		private DoubleLinkedListElement<T> prev;
//		private DoubleLinkedListElement<T> next;
//		
//		private DoubleLinkedListElement(T element) {
//			this.element = element;
//		}
//		
//		void prepend(DoubleLinkedListElement<T> doubleLinkedListElement) {
//			if (prev != null) {
//				assert prev.next == this;
//				prev.next = doubleLinkedListElement;
//			}
//			doubleLinkedListElement.prev = prev;
//			doubleLinkedListElement.next = this;
//			prev = doubleLinkedListElement;
//		}
//		
//		void append(DoubleLinkedListElement<T> doubleLinkedListElement) {
//			if (next != null) {
//				assert next.prev == this;
//				next.prev = doubleLinkedListElement;
//			}
//			doubleLinkedListElement.prev = this;
//			doubleLinkedListElement.next = next;
//			next = doubleLinkedListElement;
//		}
//		
//		DoubleLinkedListElement<T> getFirstListElement() {
//			return (prev == null) ? this : prev.getFirstListElement();
//		}
//		
//		DoubleLinkedListElement<T> getLastListElement() {
//			return (next == null) ? this : next.getFirstListElement();
//		}
//
//		public Iterator<T> iterator() {
//			if (prev != null) {
//				throw new IllegalStateException(DoubleLinkedListElement.this + " is not the first element in the list");
//			}
//			return new Iterator<T>() {
//				private DoubleLinkedListElement<T> dlle = DoubleLinkedListElement.this;
//				public boolean hasNext() {
//					return dlle != null;
//				}
//
//				public T next() {
//					T tmp = dlle.element;
//					dlle = dlle.next;
//					return tmp;
//				}
//
//				public void remove() {
//					throw new UnsupportedOperationException();
//				}
//			};
//		}
//	}
	
	
//	private static final class EdgeNeighbors {
//		final HalfEdge edge;
//		final EdgeNeighbors neighbors[] = new EdgeNeighbors[2];
//		
//		EdgeNeighbors(HalfEdge edge) {
//			this.edge = edge;
//		}
//		
//		EdgeNeighbors walk(int direction) {
//			if (neighbors[direction] == null) {
//				return this;
//			}
//			EdgeNeighbors neighbor = neighbors[direction];
//			if (neighbor.neighbors[0] == this) {
//				direction = 1;
//			} else if (neighbor.neighbors[1] == this) {
//				direction = 0;
//			} else {
//				throw new RuntimeException();
//			}
//			return neighbor.walk(direction);
//		}
//		
//		public Iterator<HalfEdge> iterator() {
//			final int direction;
//			if (neighbors[0] == null) {
//				direction = 1;
//			} else if (neighbors[1] == null) {
//				direction = 0;
//			} else {
//				throw new IllegalStateException(EdgeNeighbors.this + " is not an endpoint");
//			}
//			return new Iterator<HalfEdge>() {
//				int dir = direction;
//				private EdgeNeighbors current = EdgeNeighbors.this;
//				public boolean hasNext() {
//					return current != null;
//				}
//
//				public HalfEdge next() {
//					HalfEdge tmp = current.edge;
//					EdgeNeighbors next = current.neighbors[dir];
//					if (next.neighbors[0] == EdgeNeighbors.this) {
//						dir = 1;
//					} else if (next.neighbors[1] == EdgeNeighbors.this) {
//						dir = 0;
//					} else {
//						throw new RuntimeException();
//					}
//					return tmp;
//				}
//
//				public void remove() {
//					throw new UnsupportedOperationException();
//				}
//			};
//		}
//	}
}
