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
	final Vertex vertex;
	final int level;
	final HalfEdge pair;
	Face face;
	HalfEdge prev;
	HalfEdge next;
	Vertex edgePoint;
	Point3d midPoint2;
	
	private HalfEdge(Vertex vertex, HalfEdge neighbor, int level) {
		this.vertex = vertex;
		this.pair = neighbor;
		this.level = level;
	}
	
	public HalfEdge(Vertex firstVertex, Vertex secondVertex, int level) {
		this.vertex = firstVertex;
		this.level = level;
		this.pair = new HalfEdge(secondVertex, this, level);
	}
	
	void computeEdgePoint() {
		assert isMaster();
		edgePoint = new Vertex();
		midPoint2 = new Point3d(vertex.position);
		midPoint2.add(pair.vertex.position);
		edgePoint.position.set(midPoint2);
		if (pair.face != null ) {
			edgePoint.position.add(face.facePoint.position);
			edgePoint.position.add(pair.face.facePoint.position);
			edgePoint.position.scale(0.25);
		} else {
//			if (interpolateBoundary) {
//				edge.edgePoint.position.scale(1.0 / 2);
//			} else {
				edgePoint.position.add(face.facePoint.position);
				edgePoint.position.scale(1.0 / 3);
//			}
		}
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
