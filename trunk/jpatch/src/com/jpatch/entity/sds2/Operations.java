package com.jpatch.entity.sds2;//////**********************************

import com.jpatch.afw.control.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.entity.*;

import java.util.*;
import javax.vecmath.*;

public class Operations {
	
	public static void lathe(Sds sds, Point3d[][] lathedPoints, List<JPatchUndoableEdit> editList) {
		Map<Point3d, BaseVertex> vertexMap = new HashMap<Point3d, BaseVertex>();
		for (Point3d[] segment : lathedPoints) {
			for (Point3d point : segment) {
				if (!vertexMap.containsKey(point)) {
					vertexMap.put(point, new BaseVertex(point.x, point.y, point.z));
				}
			}
		}
		for (Point3d[] segment : lathedPoints) {
			for (Point3d point : segment) {
				System.out.println(point + " => " + vertexMap.get(point));
			}
		}
		int points = lathedPoints[0].length;
		Material material = Main.getInstance().getDefaultMaterial();
		for (int segment = 0; segment < lathedPoints.length - 1; segment++) {
			for (int i = 0; i < points - 1; i++) {
				BaseVertex v0 = vertexMap.get(lathedPoints[segment][i]);
				BaseVertex v1 = vertexMap.get(lathedPoints[segment][i + 1]);
				BaseVertex v2 = vertexMap.get(lathedPoints[segment + 1][i + 1]);
				BaseVertex v3 = vertexMap.get(lathedPoints[segment + 1][i]);
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
	
	public static void getLathedVertices(Sds sds, BaseVertex[] vertices, Point3d startAxis, Point3d endAxis, double epsilon, double angle, Point3d[][] lathedPoints) {
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
					vertices[i].getPosition(pc);
					/* set pc to the point on the axis closest to p */
					double t = Utils3d.closestPointOnLine(startAxis, endAxis, pc);
					pc.interpolate(startAxis, endAxis, t);
					
					/* if closer than epsilon, project p to axis */
					if (p.distanceSquared(pc) <= epsilonSquared) {
						p.set(pc);
					}
				}
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