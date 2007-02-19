package sds;

import javax.vecmath.Point3d;

import jpatch.entity.Attribute;

/**
 * 
 */

/**
 * @author sascha
 *
 */
public class HalfEdge {
	final TopLevelVertex vertex;
	final HalfEdge pair;
	Face face;
	HalfEdge prev;
	HalfEdge next;
	Level2Vertex edgePoint;
	public final Attribute.Integer sharpness;
	
	private HalfEdge(TopLevelVertex vertex, HalfEdge neighbor, Attribute.Integer sharpness) {
		this.vertex = vertex;
		this.pair = neighbor;
		this.sharpness = sharpness;
	}
	
	public HalfEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex) {
		sharpness = new Attribute.Integer(0);
		vertex = firstVertex;
		pair = new HalfEdge(secondVertex, this, sharpness);
		pair.edgePoint = edgePoint;
	}
	
	void bindEdgePoint() {
		edgePoint = new Level2Vertex() {
			@Override
			public void computeDerivedPosition() {
				Point3d p0 = vertex.pos;
				Point3d p1 = pair.vertex.pos;
				if (HalfEdge.this.sharpness.get() > 0) {
					position.set(
							(p0.x + p1.x) * 0.5,
							(p0.y + p1.y) * 0.5,
							(p0.z + p1.z) * 0.5
					);
				} else {
					Point3d p2 = face.facePoint.pos;
					Point3d p3 = pair.face.facePoint.pos;
					position.set(
							(p0.x + p1.x + p2.x + p3.x) * 0.25,
							(p0.y + p1.y + p2.y + p3.y) * 0.25,
							(p0.z + p1.z + p2.z + p3.z) * 0.25
					);
				}
			}
		};
		pair.edgePoint = this.edgePoint;
	}
	
	public TopLevelVertex getFirstVertex() {
		return vertex;
	}
	
	public TopLevelVertex getSecondVertex() {
		return pair.vertex;
	}
	
	public Face getRightFace() {
		return face;
	}
	
	public Face getLeftFace() {
		return pair.face;
	}
	
	public boolean isMaster() {
		return vertex.hashCode() < pair.vertex.hashCode();
	}
	
	public HalfEdge getMaster() {
		return isMaster() ? this : pair;
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
	
//	public String toString() {
//		return System.identityHashCode(this) + " " + (isMaster() ? vertex.num + "+" + pair.vertex.num : vertex.num + "-" + pair.vertex.num + " s=" + sharpness);
//	}
}
