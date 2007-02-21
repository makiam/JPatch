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
	
	public HalfEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex) {
		sharpness = new Attribute.Integer(0);
		vertex = firstVertex;
		pair = new SecondaryEdge(secondVertex, this, sharpness);
		pair.edgePoint = edgePoint;
	}
	
	private HalfEdge(TopLevelVertex vertex, HalfEdge neighbor, Attribute.Integer sharpness) {
		this.vertex = vertex;
		this.pair = neighbor;
		this.sharpness = sharpness;
	}
	
	final void bindEdgePoint() {
		edgePoint = new Level2Vertex() {
			@Override
			public void computeDerivedPosition() {
				Point3d p0 = vertex.pos;
				Point3d p1 = pair.vertex.pos;
				if (HalfEdge.this.sharpness.get() > 0 || isBoundary()) {
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
	
	final public TopLevelVertex getFirstVertex() {
		return vertex;
	}
	
	final public TopLevelVertex getSecondVertex() {
		return pair.vertex;
	}
	
	final public Face getRightFace() {
		return face;
	}
	
	final public Face getLeftFace() {
		return pair.face;
	}
	
	public boolean isPrimary() {
		return true;
	}
	
	public HalfEdge getPrimary() {
		return this;
	}
	
	public boolean isBoundary() {
		return face == null || pair.face == null;
	}
	
//	@Override
//	final public int hashCode() {
//		return (System.identityHashCode(vertex) << 1) ^ System.identityHashCode(pair.vertex);
//	}
//	
//	@Override
//	final public boolean equals(Object o) {
//		if (o instanceof HalfEdge) {
//			HalfEdge e = (HalfEdge) o;
//			return vertex == e.vertex && pair.vertex == e.pair.vertex;
//		}
//		return false;
//	}
	
	private static final class SecondaryEdge extends HalfEdge {
		private SecondaryEdge(TopLevelVertex vertex, HalfEdge neighbor, Attribute.Integer sharpness) {
			super(vertex, neighbor, sharpness);
		}
		
		@Override
		public boolean isPrimary() {
			return false;
		}
		
		@Override
		public HalfEdge getPrimary() {
			return pair;
		}
	}
}
