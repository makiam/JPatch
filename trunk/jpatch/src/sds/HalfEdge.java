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
	final Level2Vertex edgePoint;
	final SlateEdge slateEdge0;
	final SlateEdge slateEdge1;
	public final Attribute.Integer sharpness;
	
	public HalfEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex) {
		sharpness = new Attribute.Integer(0);
		vertex = firstVertex;
		edgePoint = new Level2Vertex() {
			@Override
			public void computeDerivedPosition() {
				Point3d p0 = HalfEdge.this.vertex.pos;
				Point3d p1 = HalfEdge.this.pair.vertex.pos;
				int edgeSharpness = getSharpness();
				if (edgeSharpness > 0) {
					position.set(
							(p0.x + p1.x) * 0.5,
							(p0.y + p1.y) * 0.5,
							(p0.z + p1.z) * 0.5
					);
				} else {
					Point3d p2 = HalfEdge.this.face.facePoint.pos;
					Point3d p3 = HalfEdge.this.pair.face.facePoint.pos;
					position.set(
							(p0.x + p1.x + p2.x + p3.x) * 0.25,
							(p0.y + p1.y + p2.y + p3.y) * 0.25,
							(p0.z + p1.z + p2.z + p3.z) * 0.25
					);
				}
				crease = Math.max(0, edgeSharpness - 1);
			}
		};
		pair = new SecondaryEdge(secondVertex, firstVertex, this, sharpness, edgePoint);
		slateEdge0 = pair.slateEdge1.pair;
		slateEdge1 = pair.slateEdge0.pair;
	}
	
	private HalfEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex, HalfEdge neighbor, Attribute.Integer sharpness, Level2Vertex edgePoint) {
		this.vertex = firstVertex;
		this.pair = neighbor;
		this.sharpness = sharpness;
		this.edgePoint = edgePoint;
		slateEdge0 = new SlateEdge(firstVertex.vertexPoint, edgePoint, this, pair);
		slateEdge1 = new SlateEdge(edgePoint, secondVertex.vertexPoint, this, pair);
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
	
	public int getSharpness() {
		if (isBoundary()) {
			return Integer.MAX_VALUE;
		} else {
			return sharpness.get();
		}
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
		private SecondaryEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex, HalfEdge neighbor, Attribute.Integer sharpness, Level2Vertex edgePoint) {
			super(firstVertex, secondVertex, neighbor, sharpness, edgePoint);
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
