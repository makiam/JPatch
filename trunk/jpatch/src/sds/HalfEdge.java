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
		final AbstractVertex[] stencil = new AbstractVertex[] { vertex, pair.vertex, face.facePoint, pair.face.facePoint };
		edgePoint = new Level2Vertex() {
			@Override
			public void computeDerivedPosition() {
				if (HalfEdge.this.sharpness.get() > 0) {
					position.set(
							(stencil[0].pos.x + stencil[1].pos.x) * 0.5,
							(stencil[0].pos.y + stencil[1].pos.y) * 0.5,
							(stencil[0].pos.z + stencil[1].pos.z) * 0.5
					);
				} else {
					position.set(
							(stencil[0].pos.x + stencil[1].pos.x + stencil[2].pos.x + stencil[3].pos.x) * 0.25,
							(stencil[0].pos.y + stencil[1].pos.y + stencil[2].pos.y + stencil[3].pos.y) * 0.25,
							(stencil[0].pos.z + stencil[1].pos.z + stencil[2].pos.z + stencil[3].pos.z) * 0.25
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
		return face != null && (pair.face == null || vertex.hashCode() < pair.vertex.hashCode());
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
