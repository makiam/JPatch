package com.jpatch.entity.sds2;//////**********************************

import com.jpatch.afw.control.*;
import com.jpatch.entity.*;

import java.util.*;
import javax.vecmath.*;

public class Operations {
	public static void extrude(Sds sds, Collection<Face> faces, List<JPatchUndoableEdit> editList) {
		
		Map<BaseVertex, BaseVertex> boundaryVertices = new HashMap<BaseVertex, BaseVertex>();
		Set<BaseVertex> innerVertices = new HashSet<BaseVertex>();
		Set<HalfEdge> boundaryEdges = new HashSet<HalfEdge>();
		Set<Face> boundaryFaces = new HashSet<Face>();
		Point3d position = new Point3d();
		Vector3d normal = new Vector3d();
		for (Face face : faces) {
			for (HalfEdge edge : face.getEdges()) {
				if (!faces.contains(edge.getPairFace())) {
					boundaryEdges.add(edge);
				}
			}
		}
		
		for (Face face : faces) {
			face.getFacePoint().getNormal(normal);
			for (HalfEdge edge : face.getEdges()) {
				BaseVertex vertex = (BaseVertex) edge.getVertex();
				if (boundaryVertices.containsKey(vertex)) {
					continue;
				}
				vertex.getPosition(position);
//				vertex.getVertexPoint().getNormal(normal);
				position.add(normal);
				boolean isBoundaryVertex = false;
				for(HalfEdge vertexEdge : vertex.getEdges()) {
					Face vertexFace = vertexEdge.getFace();
					if (!faces.contains(vertexFace)) {
						BaseVertex newVertex = new BaseVertex();
						newVertex.setPosition(position);
						boundaryVertices.put(vertex, newVertex);
						boundaryFaces.add(face);
						isBoundaryVertex = true;
						break;
					}		
				}	
				if (!isBoundaryVertex && !innerVertices.contains(vertex)) {
					innerVertices.add(vertex);
					addEdit(editList, AttributeEdit.changeAttribute(vertex.positionAttr, position, true));
				}
			}
		}
		
		AbstractVertex[] edgeVertices = new AbstractVertex[4];
		for (Face boundaryFace : boundaryFaces) {
			HalfEdge[] edges = boundaryFace.getEdges().clone();
			AbstractVertex[] vertices = new AbstractVertex[boundaryFace.getSides()];
			for (int i = 0; i < edges.length; i++) {
				AbstractVertex vertex = edges[i].getVertex();
				AbstractVertex newVertex = boundaryVertices.get(vertex);
				vertices[i] = newVertex == null ? vertex : newVertex;
			}
			sds.removeFace(editList, 0, boundaryFace);
//			addEdit(editList, new RemoveFaceEdit(sds, boundaryFace));
			sds.addFace(editList, 0, boundaryFace.getMaterial(), vertices);
//			addEdit(editList, new AddFaceEdit(sds, boundaryFace.getMaterial(), vertices));
			for (HalfEdge edge : edges) {
				if (boundaryEdges.contains(edge)) {
					edgeVertices[0] = edge.getVertex();
					edgeVertices[1] = edge.getPairVertex();
					edgeVertices[2]  = boundaryVertices.get(edge.getPairVertex());
					edgeVertices[3]  = boundaryVertices.get(edge.getVertex());
//					addEdit(editList, new AddFaceEdit(sds, boundaryFace.getMaterial(), edgeVertices));
					sds.addFace(editList, 0, boundaryFace.getMaterial(), edgeVertices);
				}
			}
		}
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
}
