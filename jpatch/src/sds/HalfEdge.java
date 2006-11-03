package sds;

import javax.vecmath.Point3d;

/**
 * 
 */

/**
 * @author sascha
 *
 */
public class HalfEdge {
	private final double[] WEIGHT = new double[] { 0.25, 0.25, 0.25, 0.25 };
	final Vertex vertex;
	final HalfEdge pair;
	Face face;
	HalfEdge prev;
	HalfEdge next;
	Vertex edgePoint;
	
	private HalfEdge(Vertex vertex, HalfEdge neighbor) {
		this.vertex = vertex;
		this.pair = neighbor;
	}
	
	public HalfEdge(Vertex firstVertex, Vertex secondVertex) {
		vertex = firstVertex;
		edgePoint = new Vertex();
		pair = new HalfEdge(secondVertex, this);
		pair.edgePoint = edgePoint;
	}
	
	void bindEdgePoint() {
		final Vertex[] stencil = new Vertex[] { vertex, pair.vertex, face.facePoint, pair.face.facePoint };
		edgePoint.setStencil(stencil, WEIGHT);
	}
	
	public Vertex getFirstVertex() {
		return vertex;
	}
	
	public Vertex getSecondVertex() {
		return pair.vertex;
	}
	
	public Face getRightFace() {
		return face;
	}
	
	public Face getLeftFace() {
		return pair.face;
	}
	
	public boolean isMaster() {
		return face != null && (pair.face == null || vertex.hashCode() < pair.vertex.hashCode());
	}
	
	@Override
	public int hashCode() {
		return (vertex.hashCode() << 1) ^ pair.vertex.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof HalfEdge) {
			HalfEdge e = (HalfEdge) o;
			return vertex == e.vertex && pair.vertex == e.pair.vertex;
		}
		return false;
	}
	
	public String toString() {
		return System.identityHashCode(this) + " " + (isMaster() ? vertex.num + "+" + pair.vertex.num : vertex.num + "-" + pair.vertex.num);
	}
}
