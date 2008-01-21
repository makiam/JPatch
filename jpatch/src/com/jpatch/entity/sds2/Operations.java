package com.jpatch.entity.sds2;//////**********************************

import java.util.*;
import javax.vecmath.*;

public class Operations {
	public static void extrude(Sds sds, Collection<Face> faces) {
		
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
			for (HalfEdge edge : face.getEdges()) {
				BaseVertex vertex = (BaseVertex) edge.getVertex();
				if (boundaryVertices.containsKey(vertex)) {
					continue;
				}
				vertex.getPosition(position);
				vertex.getVertexPoint().getNormal(normal);
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
					vertex.setPosition(position);
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
			sds.removeFace(0, boundaryFace);
			sds.addFace(0, boundaryFace.getMaterial(), vertices);
			for (HalfEdge edge : edges) {
				if (boundaryEdges.contains(edge)) {
					edgeVertices[0] = edge.getVertex();
					edgeVertices[1] = edge.getPairVertex();
					edgeVertices[2]  = boundaryVertices.get(edge.getPairVertex());
					edgeVertices[3]  = boundaryVertices.get(edge.getVertex());
					Face face = sds.addFace(0, boundaryFace.getMaterial(), edgeVertices);
					System.out.println("added " + face);
				}
			}
		}
		
		
	}
}
