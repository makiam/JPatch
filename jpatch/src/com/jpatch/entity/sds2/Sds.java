package com.jpatch.entity.sds2;

import java.util.*;

public class Sds {
	private Set<Face>[] levelFaceSets = new Set[SdsConstants.MAX_LEVEL + 1];
	private Map<EdgeKey, HalfEdge> edgeMap = new HashMap<EdgeKey, HalfEdge>();
	
	public Sds() {
		for (int i = 0; i < levelFaceSets.length; i++) {
			levelFaceSets[i] = new HashSet<Face>();
		}
	}
	
	public void addFace(int level, Vertex... vertices) {
//		System.out.print("Adding face at level " + level + " vertices: ");
//		for (Vertex v : vertices) {
//			System.out.print(v + " ");
//		}
		HalfEdge[] edges = new HalfEdge[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			int j = i + 1;
			if (j == vertices.length) {
				j = 0;
			}
			edges[i] = getHalfEdge(vertices[i], vertices[j]);
		}
		Face face = new Face(edges);
		levelFaceSets[level].add(face);
//		System.out.println("    face=" + face);
	}
	
	public Collection<Face> getFaces(int level) {
		return levelFaceSets[level];
	}
	
	public void dumpFaces(int level) {
		for (Face face : levelFaceSets[level]) {
			System.out.println(face);
		}
	}
	
	public void createNextLevel(int currentLevel) {
		for (Face face : levelFaceSets[currentLevel]) {
			for (HalfEdge edge : face.getEdges()) {
				addFace(currentLevel + 1, face.getFacePoint(), edge.getPrev().getEdgePoint(), edge.getVertex().getVertexPoint(), edge.getEdgePoint());
			}
		}
	}
	
	/**
	 * Checks if the halfEdge vertex0->vertex1 already exists. If it exists and it's face is null,
	 * it is returned, if the face is not null, an IllegalStateException is thrown (non-manifold surface,
	 * each HalfEdge can only belong to one face). If it doesn't exist, a new edge is created, added to the SDS
	 * and returned
	 * @param vertex0
	 * @param vertex1
	 * @return
	 */
	private HalfEdge getHalfEdge(Vertex vertex0, Vertex vertex1) {
		/* check if the HalfEdge (v0->v1) already exists */
		HalfEdge edge = edgeMap.get(new EdgeKey(vertex0, vertex1));
		if (edge == null) {
			/* if no edge is found, create a new one and store it in the maps */
			edge = new HalfEdge(vertex0, vertex1);
			addHalfEdge(edge);
		} else {
			if (edge.getFace() != null) {
				throw new IllegalStateException("Surface is non-manifold, edge=" + edge + " face=" + edge.getFace());
			}
		}
		return edge;
	}
	
	/**
	 * Adds specified halfEdge and its pair to edgeMap.
	 * Adds specified halfEdge and its pair to their vertices
	 * @param halfEdge
	 */
	private void addHalfEdge(HalfEdge halfEdge) {
		Vertex v0 = halfEdge.getVertex();
		Vertex v1 = halfEdge.getPairVertex();
		EdgeKey key = new EdgeKey(v0, v1);
		EdgeKey pairKey = new EdgeKey(v1, v0);
		if (edgeMap.containsKey(key) || edgeMap.containsKey(pairKey)) {
			throw new IllegalStateException("HalfEdge " + halfEdge + " already in SDS");
		}
		edgeMap.put(key, halfEdge);
		edgeMap.put(pairKey, halfEdge.getPair());
		v0.addEdge(halfEdge);
		v1.addEdge(halfEdge.getPair());
	}
	
	/**
	 * Removes specified halfEdge and its pair from edgeMap.
	 * Removes specified halfEdge and its pair from their vertices
	 * @param halfEdge
	 */
	private void removeEdge(HalfEdge halfEdge) {
		if (halfEdge.getFace() != null || halfEdge.getPairFace() != null) {
			throw new IllegalStateException("Edge " + halfEdge + " still has faces");
		}
		Vertex v0 = halfEdge.getVertex();
		Vertex v1 = halfEdge.getPairVertex();
		EdgeKey key = new EdgeKey(v0, v1);
		EdgeKey pairKey = new EdgeKey(v1, v0);
		if (!edgeMap.containsKey(key) || !edgeMap.containsKey(pairKey)) {
			throw new IllegalStateException("HalfEdge " + halfEdge + " not in SDS");
		}
		edgeMap.remove(key);
		edgeMap.remove(pairKey);
		v0.removeEdge(halfEdge);
		v1.removeEdge(halfEdge.getPair());
	}
	
	private static final class EdgeKey {
		private final Vertex v0;
		private final Vertex v1;
		private final int hashCode;
		
		private EdgeKey(Vertex v0, Vertex v1) {
			this.v0 = v0;
			this.v1 = v1;
			hashCode = (System.identityHashCode(v0) << 1) ^ System.identityHashCode(v1);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object o) {
			EdgeKey ek = (EdgeKey) o;
			return v0 == ek.v0 && v1 == ek.v1;
		}
	}
}