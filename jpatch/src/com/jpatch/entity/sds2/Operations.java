package com.jpatch.entity.sds2;//////**********************************

import com.jpatch.afw.control.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.*;

import java.util.*;
import javax.vecmath.*;

public class Operations {
	
	public static void lathe(Sds sds, BaseVertex[] vertices, Point3d startAxis, Point3d endAxis, double epsilon, int segments, double angle, List<JPatchUndoableEdit> editList) {
		
		
	}
	
	public static void getLathedVertices(Sds sds, BaseVertex[] vertices, Point3d startAxis, Point3d endAxis, double epsilon, int segments, double angle, Point3d[][] lathedPoints) {
		Vector3d axis = new Vector3d();
		axis.sub(endAxis, startAxis);
		AxisAngle4d axisAngle = new AxisAngle4d(axis, 0);
		Matrix4d matrix = new Matrix4d();
		
		for (int segment = 0; segment < segments; segment++) {
			int count = angle == 360 ? segments : segments - 1;
			axisAngle.angle = Math.toRadians(angle / count * segment);
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
			}
		}
	}
	
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
		
		Selection.Type selectionType = selection.getType();
		selection.clear(editList);
		Collection<AbstractVertex> newSelection = new HashSet<AbstractVertex>(innerVertices);
		for (BaseVertex boundaryVertex : boundaryVertices.keySet()) {
			newSelection.add(boundaryVertices.get(boundaryVertex));
		}
		selection.addVertices(newSelection, editList);
		selection.setType(selectionType, editList);
		
		/* change selection */
		
	}
	
	private static void addEdit(List<JPatchUndoableEdit> editList, JPatchUndoableEdit edit) {
		if (editList != null) {
			editList.add(edit);
		}
	}

}
