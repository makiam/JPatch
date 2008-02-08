package com.jpatch.entity.sds2;//////**********************************

import com.jpatch.afw.control.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.*;

import java.util.*;
import javax.vecmath.*;

public class Operations {
	public static void extrude(Sds sds, Selection selection, List<JPatchUndoableEdit> editList) {
//		System.out.println("extrude " + faces.size() + " faces:");
//		System.out.println(faces);
		
		Map<BaseVertex, BaseVertex> boundaryVertices = new HashMap<BaseVertex, BaseVertex>();
		Set<BaseVertex> innerVertices = new HashSet<BaseVertex>();
		Map<HalfEdge, Material> boundaryEdges = new HashMap<HalfEdge, Material>();
		Set<Face> boundaryFaces = new HashSet<Face>();
		Set<Face> innerFaces = new HashSet<Face>();
		
//		Point3d position = new Point3d();
//		Vector3d normal = new Vector3d();
		
		Collection<Face> selectedFaces = selection.getFaces();
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
					System.out.println("edge=" + edge + " face=" + face);
				}
			}
		}
		/*
		 * create a set innerFaces containing all selected faces that are not boundary faces
		 */
		innerFaces.addAll(selectedFaces);
		innerFaces.removeAll(boundaryFaces);
		
		/*
		 * move vertices on inner faces
		 */
		for (Face innerFace : innerFaces) {
			for (HalfEdge edge : innerFace.getEdges()) {
				BaseVertex innerVertex = (BaseVertex) edge.getVertex();
				innerVertices.add(innerVertex);
//				innerVertex.getPosition(position);
//				innerVertex.getVertexPoint().getNormal(normal);
//				position.add(normal);
//				addEdit(editList, AttributeEdit.changeAttribute(innerVertex.positionAttr, position, true));
			}
		}
		
		/* create new extruded vertices for all boundary edges */
		BaseVertex[] edgeVertices = new BaseVertex[2];
		Point3d position = new Point3d();
		for (HalfEdge boundaryEdge : boundaryEdges.keySet()) {
			edgeVertices[0] = (BaseVertex) boundaryEdge.getVertex();
			edgeVertices[1] = (BaseVertex) boundaryEdge.getPairVertex();
			for (AbstractVertex edgeVertex : edgeVertices) {
				if (!boundaryVertices.containsKey(edgeVertex)) {
					edgeVertex.getPosition(position);
//					edgeVertex.getVertexPoint().getNormal(normal);
//					position.add(normal);
					BaseVertex extrudedVertex = new BaseVertex();
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
			sds.removeFace(editList, 0, boundaryFace);
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
		
		Selection.Type selectionType = selection.getType();
		selection.clear(editList);
		Collection<AbstractVertex> newSelection = new HashSet<AbstractVertex>(innerVertices);
		for (BaseVertex boundaryVertex : boundaryVertices.keySet()) {
			newSelection.add(boundaryVertices.get(boundaryVertex));
		}
		selection.addVertices(newSelection, editList);
		selection.switchType(selectionType, editList);
		
		/* change selection */
		
//		System.out.println("boundary edges:");
//		System.out.println(boundaryEdges);
//		
//		for (Face face : faces) {
//			face.getFacePoint().getNormal(normal);
//			for (HalfEdge edge : face.getEdges()) {
//				BaseVertex vertex = (BaseVertex) edge.getVertex();
//				if (!boundaryVertices.containsKey(vertex)) {
//					vertex.getPosition(position);
////					vertex.getVertexPoint().getNormal(normal);
//					position.add(normal);
//				}
//				
//				boolean isBoundaryVertex = false;
//				for(HalfEdge vertexEdge : vertex.getEdges()) {
//					Face vertexFace = vertexEdge.getFace();
//					if (!faces.contains(vertexFace)) {
//						BaseVertex newVertex = new BaseVertex();
//						newVertex.setPosition(position);
//						boundaryVertices.put(vertex, newVertex);
//						boundaryFaces.add(face);
//						isBoundaryVertex = true;
//						break;
//					}		
//				}	
//				if (!isBoundaryVertex && !innerVertices.contains(vertex)) {
//					innerVertices.add(vertex);
//					addEdit(editList, AttributeEdit.changeAttribute(vertex.positionAttr, position, true));
//				}
//			}
//		}
//		
//		System.out.println("boundary faces:");
//		System.out.println(boundaryFaces);
//		
//		AbstractVertex[] edgeVertices = new AbstractVertex[4];
//		for (Face boundaryFace : boundaryFaces) {
//			HalfEdge[] edges = boundaryFace.getEdges().clone();
//			AbstractVertex[] vertices = new AbstractVertex[boundaryFace.getSides()];
//			for (int i = 0; i < edges.length; i++) {
//				AbstractVertex vertex = edges[i].getVertex();
//				AbstractVertex newVertex = boundaryVertices.get(vertex);
//				vertices[i] = newVertex == null ? vertex : newVertex;
//			}
//			sds.removeFace(editList, 0, boundaryFace);
////			addEdit(editList, new RemoveFaceEdit(sds, boundaryFace));
//			sds.addFace(editList, 0, boundaryFace.getMaterial(), vertices);
////			addEdit(editList, new AddFaceEdit(sds, boundaryFace.getMaterial(), vertices));
//			for (HalfEdge edge : edges) {
//				if (boundaryEdges.contains(edge)) {
//					edgeVertices[0] = edge.getVertex();
//					edgeVertices[1] = edge.getPairVertex();
//					edgeVertices[2]  = boundaryVertices.get(edge.getPairVertex());
//					edgeVertices[3]  = boundaryVertices.get(edge.getVertex());
////					addEdit(editList, new AddFaceEdit(sds, boundaryFace.getMaterial(), edgeVertices));
//					sds.addFace(editList, 0, boundaryFace.getMaterial(), edgeVertices);
//				}
//			}
//		}
	}
	
	private static void addEdit(List<JPatchUndoableEdit> editList, JPatchUndoableEdit edit) {
		if (editList != null) {
			editList.add(edit);
		}
	}
	
//	private static abstract class AbstractAddRemoveFaceEdit extends AbstractUndoableEdit {
//		Sds sds;
//		Face face;
//		Material material;
//		AbstractVertex[] vertices;
//		
//		void addFace() {
//			face = sds.addFace(0, material, vertices);
//			System.out.println("added face " + face);
//		}
//		
//		void removeFace() {
//			System.out.println("removing face " + face);
//			sds.removeFace(0, face);
//		}
//	}
//	
//	private static class RemoveFaceEdit extends AbstractAddRemoveFaceEdit {
//		
//		RemoveFaceEdit(Sds sds, Face face) {
//			this.sds = sds;
//			this.face = face;
//			material = face.getMaterial();
//			vertices = new AbstractVertex[face.getSides()];
//			for (int i = 0, n = face.getSides(); i < n; i++) {
//				vertices[i] = face.getEdges()[i].getVertex();
//			}
//			redo();
//		}
//		
//		public void undo() {
//			super.undo();
//			addFace();
//		}
//		
//		public void redo() {
//			super.redo();
//			removeFace();
//		}
//	}
//	
//	private static class AddFaceEdit extends AbstractAddRemoveFaceEdit {
//		AddFaceEdit(Sds sds, Material material, AbstractVertex... vertices) {
//			this.sds = sds;
//			this.material = material;
//			this.vertices = vertices.clone();
//			redo();
//		}
//		
//		public void undo() {
//			super.undo();
//			removeFace();
//		}
//		
//		public void redo() {
//			super.redo();
//			addFace();
//		}
//	}
	
//	static abstract class ExtrudeStrategy {
//		abstract void extrude(BaseVertex originalVertex, BaseVertex extrudedVertex, List<JPatchUndoableEdit> editList);
//	}
//	
//	static abstract class ExtrudeAlongNormalStrategy extends ExtrudeStrategy {
//		private final Point3d position = new Point3d();
//		private final Vector3d normal = new Vector3d();
//		
//		void extrude(BaseVertex originalVertex, BaseVertex extrudedVertex, List<JPatchUndoableEdit> editList) {
//			originalVertex.getPosition(position);
//			originalVertex.getNormal(normal);
//			addEdit(editList, AttributeEdit.changeAttribute(extrudedVertex.positionAttr, position, true));
//		}
//	}
}
