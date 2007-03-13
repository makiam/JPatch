package sds;

import static sds.SdsWeights.*;

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
	static int count;
	final int num = count++;
	
	final TopLevelVertex vertex;
	final HalfEdge pair;
	Face face;
	HalfEdge prev;
	HalfEdge next;
	public final Level2Vertex edgePoint;
	final SlateEdge slateEdge0;
	final SlateEdge slateEdge1;
	public final Attribute.Integer sharpness;
	final boolean primary;
	
	public HalfEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex) {
		sharpness = new Attribute.Integer(0);
		vertex = firstVertex;
		edgePoint = new Level2Vertex() {
			@Override
			public void computeDerivedPosition() {
				Point3d p0 = HalfEdge.this.vertex.pos;
				Point3d p1 = HalfEdge.this.pair.vertex.pos;
				int edgeSharpness = HalfEdge.this.getSharpness();
//				System.out.println("edge sharpness = " + edgeSharpness);
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
			
			@Override
			public void computeLimit() {
				int edgeSharpness = HalfEdge.this.getSharpness();
				if (edgeSharpness > 0) {
					Point3d p1 = vertex.vertexPoint.pos;
					Point3d p2 = pair.vertex.vertexPoint.pos;
					limit.set(
							pos.x * CREASE_LIMIT0 + (p1.x + p2.x) * CREASE_LIMIT1,
							pos.y * CREASE_LIMIT0 + (p1.y + p2.y) * CREASE_LIMIT1,
							pos.z * CREASE_LIMIT0 + (p1.z + p2.z) * CREASE_LIMIT1
					);
				} else {
					Point3d pf0 = pair.prev.edgePoint.pos;
					Point3d pf1 = next.edgePoint.pos;
					Point3d pf2 = prev.edgePoint.pos;
					Point3d pf3 = pair.next.edgePoint.pos;
					Point3d pe0 = pair.vertex.vertexPoint.pos;
					Point3d pe1 = face.facePoint.pos;
					Point3d pe2 = vertex.vertexPoint.pos;
					Point3d pe3 = pair.face.facePoint.pos;
					limit.set(
							pos.x * LIMIT0 + ((pf0.x + pf2.x) + (pf1.x + pf3.x)) * LIMIT2 + ((pe0.x + pe2.x) + (pe1.x + pe3.x)) * LIMIT1,
							pos.y * LIMIT0 + ((pf0.y + pf2.y) + (pf1.y + pf3.y)) * LIMIT2 + ((pe0.y + pe2.y) + (pe1.y + pe3.y)) * LIMIT1,
							pos.z * LIMIT0 + ((pf0.z + pf2.z) + (pf1.z + pf3.z)) * LIMIT2 + ((pe0.z + pe2.z) + (pe1.z + pe3.z)) * LIMIT1
					);
					
					vTangent.set(
							(pe1.x - pe3.x) * 4 + (pf1.x - pf0.x) + (pf2.x - pf3.x),
							(pe1.y - pe3.y) * 4 + (pf1.y - pf0.y) + (pf2.y - pf3.y),
							(pe1.z - pe3.z) * 4 + (pf1.z - pf0.z) + (pf2.z - pf3.z)
					);
					
					uTangent.set(
							(pe0.x - pe2.x) * 4 + (pf0.x - pf3.x) + (pf1.x - pf2.x),
							(pe0.y - pe2.y) * 4 + (pf0.y - pf3.y) + (pf1.y - pf2.y),
							(pe0.z - pe2.z) * 4 + (pf0.z - pf3.z) + (pf1.z - pf2.z)
					);
					
					normal.cross(uTangent, vTangent);
				}
			}
		};
		pair = new HalfEdge(secondVertex, firstVertex, this, sharpness, edgePoint);
		slateEdge0 = pair.slateEdge1.pair;
		slateEdge1 = pair.slateEdge0.pair;
		primary = true;
	}
	
	private HalfEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex, HalfEdge neighbor, Attribute.Integer sharpness, Level2Vertex edgePoint) {
		this.vertex = firstVertex;
		this.pair = neighbor;
		this.sharpness = sharpness;
		this.edgePoint = edgePoint;
		slateEdge0 = new SlateEdge(firstVertex.vertexPoint, edgePoint, this, pair);
		slateEdge1 = new SlateEdge(edgePoint, secondVertex.vertexPoint, this, pair);
		primary = false;
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
		return primary;
	}
	
	public HalfEdge getPrimary() {
		return primary ? this : pair;
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
	
	public String toString() {
		return "e" + num + "/" + pair.num;
	}
}
