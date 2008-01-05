package com.jpatch.entity.sds;

import java.util.*;

public class Operations {
	public static void extrudeBroken(Sds sds, Collection<Face> faces) {
		/* 
		 * all "boundary" edges of the selected faces, that need to be extruded
		 * an edge is a "boundary" edge iff not both of its faces are part of the
		 * "to be extruded" faces
		 */
		
		sds.dump();
		
		Collection<HalfEdge> edgesToExtrude = new HashSet<HalfEdge>();
		for (Face face : faces) {
			System.out.println("extruding " + face);
			for (HalfEdge edge : face.getEdges()) {
				if (!faces.contains(edge.getPairFace())) {
					edgesToExtrude.add(edge);
				}
			}
		}
		
		for (HalfEdge edge : edgesToExtrude) {
			/*
			 * for each "edge to extrude" create a new 4-sided face with the following edges:
			 * --1
			 *   |\b
			 * A | \1'---
			 *  a|  |    |
			 *   |C |c   |
			 * --0\ | B  |
			 *    d\|    |
			 *      0'---
			 * A: a face at the boundary that wasn't extruded
			 * B: an extruded face
			 * C: The new face, with new edges a, b, c and d
			 * 0, 1: original vertices
			 * 0', 1': extruded vertices
			 */
			
			/* get faces A and B */
			Face faceA = edge.getPairFace();
			Face faceB = edge.getFace();
			
			/* get vertices 0 and 1 */
			TopLevelVertex v0 = edge.getVertex();
			TopLevelVertex v1 = edge.getPairVertex();
			
			/* create extruded vertices 0' and 1' */
			TopLevelVertex v01 = new TopLevelVertex(v0);
			TopLevelVertex v11 = new TopLevelVertex(v1);
			
//			HalfEdge edgeA = new HalfEdge(v0, v1);
//			HalfEdge edgeB = new HalfEdge(v1, v11);
//			HalfEdge edgeC = new HalfEdge(v11, v01);
//			HalfEdge edgeD = new HalfEdge(v01, v0);
//			edgeA.next = edgeB;
//			edgeB.next = edgeC;
//			edgeC.next = edgeD;
//			edgeD.next = edgeA;
//			edgeA.prev = edgeD;
//			edgeB.prev = edgeA;
//			edgeC.prev = edgeB;
//			edgeD.prev = edgeC;
//			Face faceC = new Face(4, edgeA);
			
			/* create face C */
			Face faceC = sds.addFace(new TopLevelVertex[] { v0, v1, v11, v01 });
			
			/* replace edges in face A and B with new edges */
			if (faceA != null) {
				HalfEdge oldEdge = edge.pair;
				int edgeIndexA = faceA.getEdgeIndex(oldEdge);
				HalfEdge newEdge = faceC.getEdges()[0].pair;
				newEdge.next = oldEdge.next;
				newEdge.next.prev = newEdge;
				newEdge.prev = oldEdge.prev;
				newEdge.prev.next = newEdge;
				newEdge.face = faceA;
				faceA.getEdges()[edgeIndexA] = newEdge;
			}		
			HalfEdge oldEdge = edge;
			int edgeIndexB = faceB.getEdgeIndex(oldEdge);
			HalfEdge newEdge = faceC.getEdges()[2].pair;
			newEdge.next = oldEdge.next;
			newEdge.next.prev = newEdge;
			newEdge.prev = oldEdge.prev;
			newEdge.prev.next = newEdge;
			newEdge.face = faceB;
			faceB.getEdges()[edgeIndexB] = newEdge;
			
			v0.removeEdge(edge);
			v1.removeEdge(edge.pair);
		}
		
		
		sds.dump();
		
		sds.validateVertices();
		sds.makeSlates();
		sds.rethinkSlates();
	}
	
	public static void extrude(Sds sds, Collection<Face> extrudedFaces) {
		sds.dump();
		
		for (Face face : extrudedFaces) {
			sds.removeFace(face);
		}
		sds.validateVertices();
		sds.makeSlates();
		sds.rethinkSlates();
		
		sds.dump();
		if (true) return;
		
//		/*
//		 * STEP 1 - split faces:
//		 * for all non-extruded faces (at the boundary):
//		 */
//		/* replace boundary vertices with clones, the clones do not share half-edges that belong to extruded faces */
//		
//		/*
//		 *      
//		 * for all extruded faces (at the boundary):
//		 *     remove all half-edges that belong to non-extruded faces 
//		 */
//		
//		
//		/*
//		 * create nonExtrudedBoundaryFaces which will contain all faces on the boundary that are not extruded
//		 * create extrudedBoundaryFaces which will contain all faces on the boundary that are extruded
//		 * create boundaryVertices which maps vertices on the boundary to newly created clones.
//		 *     the original vertices will be part of the extruded faces
//		 *     on the non-extruded faces, they will be replaced with their clones
//		 * create boundaryEdges which will contain all HalfEdges (on the extruded faces) on the boundary
//		 */
//		Collection<Face> nonExtrudedBoundaryFaces = new HashSet<Face>();
//		Collection<Face> extrudedBoundaryFaces = new HashSet<Face>();
//		Map<TopLevelVertex, BoundaryVertex> boundaryVertices = new HashMap<TopLevelVertex, BoundaryVertex>();
//		Collection<HalfEdge> boundaryEdges = new HashSet<HalfEdge>();
//		for (Face face : extrudedFaces) {
//			for (HalfEdge halfEdge : face.getEdges()) {
//				Face pairFace = halfEdge.getPairFace();
//				if (!extrudedFaces.contains(pairFace)) {
//					if (pairFace != null) {
//						nonExtrudedBoundaryFaces.add(pairFace);
//					}
//					extrudedBoundaryFaces.add(face);
//					if (!boundaryVertices.containsKey(halfEdge.vertex)) {
//						boundaryVertices.put(halfEdge.vertex, new BoundaryVertex(halfEdge.vertex));
//					}
//					if (!boundaryVertices.containsKey(halfEdge.pair.vertex)) {
//						boundaryVertices.put(halfEdge.pair.vertex, new BoundaryVertex(halfEdge.pair.vertex));
//					}
//					boundaryEdges.add(halfEdge);
//				}
//			}
//		}
//		
//		/*
//		 * Loop over boundaryVertices. Remove all halfEdges that don't belong to extruded faces and add them
//		 * to the clone-vertex (don't change the halfEdges' vertex from the original to the clone yet) 
//		 */
////		for (TopLevelVertex original : boundaryVertices.keySet()) {
////			TopLevelVertex clone = boundaryVertices.get(original).clone;
////			for (HalfEdge halfEdge : original.edges.clone()) {
////				if (!extrudedBoundaryFaces.contains(halfEdge.face)) {
////					original.removeEdge(halfEdge);
////					clone.addEdge(halfEdge);
////				}
////			}
////			original.validate();
////			clone.validate();
////		}
//		
//		/*
//		 * STEP 2 - create extrude edges
//		 * for all vertices on the boundary:
//		 *     create a new edge (half-edge, half-edge pair) connecting the original vertex with the cloned one (from step 1)
//		 */
//		
//		
//		/*
//		 * STEP 3 - split boundary edges
//		 * for all boundary edges (from verex A to B, halfedges a, b):
//		 *     create half-edges spA and spB
//		 *     splice half-edge a to spA (removing it from b)
//		 *     splice half-edge b to spB (removing it from b)
//		 *     spB.next = exB'
//		 *     exB'.next = spA
//		 *     spA.next = exA
//		 *     exA.next = spB
//		 *     (same with prev)
//		 *     create new face starting with edge spA
//		 *     add spB to vertex A'
//		 *     add spA to vertex B
//		 */
//		for (HalfEdge halfEdge : boundaryEdges) {
//			TopLevelVertex aOrig = halfEdge.vertex;
//			TopLevelVertex bOrig = halfEdge.pair.vertex;
//			TopLevelVertex aClone = boundaryVertices.get(aOrig).clone;
//			TopLevelVertex bClone = boundaryVertices.get(bOrig).clone;
//			HalfEdge aExtruded = boundaryVertices.get(aOrig).halfEdge;
//			HalfEdge bExtruded = boundaryVertices.get(bOrig).halfEdge;
//			HalfEdge newOriginal = new HalfEdge(aOrig, bOrig);
//			HalfEdge newClone = new HalfEdge(aClone, bClone);
//			
//			connectHalfEdgesToFace(newOriginal.pair, aExtruded, newClone, bExtruded.pair);
//			Face face = new Face(4, newOriginal.pair);
//			replaceHalfEdge(halfEdge, newOriginal);
//			replaceHalfEdge(halfEdge.pair, newClone.pair);
//			
//			sds.faceList.add(face);
//		}
//		
//		for (TopLevelVertex vertex : boundaryVertices.keySet()) {
//			sds.vertexList.add(boundaryVertices.get(vertex).clone);
//		}
//		
//		sds.dump();
//		
//		sds.validateVertices();
//		sds.makeSlates();
//		sds.rethinkSlates();
//		
//		/*
//		 * STEP 2 - add extruded edges to vertices
//		 * for all vertices on the boundary:
//		 *     add new halfedges (from step 2) to both, original and cloned vertex
//		 */
	}
	
	private static void connectHalfEdgesToFace(HalfEdge ... edges) {
		for (int i = 0; i < edges.length; i++) {
			int j = i + 1;
			if (j == edges.length) {
				j = 0;
			}
			edges[i].next = edges[j];
			edges[j].prev = edges[i];
		}
	}
	
	private static void replaceHalfEdge(HalfEdge oldHalfEdge, HalfEdge newHalfEdge) {
		Face face = oldHalfEdge.face;
		if (face != null) {
			face.edges[face.getEdgeIndex(oldHalfEdge)] = newHalfEdge;
		}
		oldHalfEdge.vertex.removeEdge(oldHalfEdge);
		newHalfEdge.vertex.addEdge(newHalfEdge);
	}
	
	private static class BoundaryVertex {
		TopLevelVertex clone;
		HalfEdge halfEdge;
		
		BoundaryVertex(TopLevelVertex original) {
			clone = new TopLevelVertex(original);
			halfEdge = new HalfEdge(original, clone);
		}
	}
	
}
