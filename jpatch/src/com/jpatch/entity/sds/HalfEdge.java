package com.jpatch.entity.sds;

import static com.jpatch.entity.sds.SdsWeights.*;
import trashcan.HardBoundedDoubleAttr;

import com.jpatch.afw.attributes.*;

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
	public final HardBoundedDoubleAttr sharpness;
	final boolean primary;

	public HalfEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex) {
		sharpness = new HardBoundedDoubleAttr(0, 10, 0);
		vertex = firstVertex;
		edgePoint = new Level2Vertex() {
			@Override
			public void computeDerivedPosition() {
				Tuple3Attr p0 = HalfEdge.this.vertex.position;
				Tuple3Attr p1 = HalfEdge.this.pair.vertex.position;
				double edgeSharpness = creaseSharpness();
//				System.out.println("edge sharpness = " + edgeSharpness);
				if (edgeSharpness > 0) {
					position.setTuple(
							(p0.getX() + p1.getX()) * 0.5,
							(p0.getY() + p1.getY()) * 0.5,
							(p0.getZ() + p1.getZ()) * 0.5
					);
				} else {
					Tuple3Attr p2 = HalfEdge.this.face.facePoint.position;
					Tuple3Attr p3 = HalfEdge.this.pair.face.facePoint.position;
					position.setTuple(
							(p0.getX() + p1.getX() + p2.getX() + p3.getX()) * 0.25,
							(p0.getY() + p1.getY() + p2.getY() + p3.getY()) * 0.25,
							(p0.getZ() + p1.getZ() + p2.getZ() + p3.getZ()) * 0.25
					);
				}
				crease = Math.max(0, edgeSharpness - 1);
			}
			
			@Override
			public void computeLimit() {
				double edgeSharpness = HalfEdge.this.creaseSharpness();
				if (edgeSharpness > 0) {
					Tuple3Attr p1 = vertex.vertexPoint.position;
					Tuple3Attr p2 = pair.vertex.vertexPoint.position;
					limit.set(
							position.getX() * CREASE_LIMIT0 + (p1.getX() + p2.getX()) * CREASE_LIMIT1,
							position.getY() * CREASE_LIMIT0 + (p1.getY() + p2.getY()) * CREASE_LIMIT1,
							position.getZ() * CREASE_LIMIT0 + (p1.getZ() + p2.getZ()) * CREASE_LIMIT1
					);
				} else {
					Tuple3Attr pf0 = pair.prev.edgePoint.position;
					Tuple3Attr pf1 = next.edgePoint.position;
					Tuple3Attr pf2 = prev.edgePoint.position;
					Tuple3Attr pf3 = pair.next.edgePoint.position;
					Tuple3Attr pe0 = pair.vertex.vertexPoint.position;
					Tuple3Attr pe1 = face.facePoint.position;
					Tuple3Attr pe2 = vertex.vertexPoint.position;
					Tuple3Attr pe3 = pair.face.facePoint.position;
					limit.set(
							position.getX() * LIMIT0 + ((pf0.getX() + pf2.getX()) + (pf1.getX() + pf3.getX())) * LIMIT2 + ((pe0.getX() + pe2.getX()) + (pe1.getX() + pe3.getX())) * LIMIT1,
							position.getY() * LIMIT0 + ((pf0.getY() + pf2.getY()) + (pf1.getY() + pf3.getY())) * LIMIT2 + ((pe0.getY() + pe2.getY()) + (pe1.getY() + pe3.getY())) * LIMIT1,
							position.getZ() * LIMIT0 + ((pf0.getZ() + pf2.getZ()) + (pf1.getZ() + pf3.getZ())) * LIMIT2 + ((pe0.getZ() + pe2.getZ()) + (pe1.getZ() + pe3.getZ())) * LIMIT1
					);
					
					vTangent.set(
							(pe1.getX() - pe3.getX()) * 4 + (pf1.getX() - pf0.getX()) + (pf2.getX() - pf3.getX()),
							(pe1.getY() - pe3.getY()) * 4 + (pf1.getY() - pf0.getY()) + (pf2.getY() - pf3.getY()),
							(pe1.getZ() - pe3.getZ()) * 4 + (pf1.getZ() - pf0.getZ()) + (pf2.getZ() - pf3.getZ())
					);
					
					uTangent.set(
							(pe0.getX() - pe2.getX()) * 4 + (pf0.getX() - pf3.getX()) + (pf1.getX() - pf2.getX()),
							(pe0.getY() - pe2.getY()) * 4 + (pf0.getY() - pf3.getY()) + (pf1.getY() - pf2.getY()),
							(pe0.getZ() - pe2.getZ()) * 4 + (pf0.getZ() - pf3.getZ()) + (pf1.getZ() - pf2.getZ())
					);
					
					normal.cross(uTangent, vTangent);
					normal.normalize();
				}
			}
		};
		pair = new HalfEdge(secondVertex, firstVertex, this, sharpness, edgePoint);
		slateEdge0 = pair.slateEdge1.pair;
		slateEdge1 = pair.slateEdge0.pair;
		edgePoint.creaseEdge0 = slateEdge0;
		edgePoint.creaseEdge1 = slateEdge1;
		primary = true;
	}
	
	private HalfEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex, HalfEdge neighbor, HardBoundedDoubleAttr sharpness, Level2Vertex edgePoint) {
		this.vertex = firstVertex;
		this.pair = neighbor;
		this.sharpness = sharpness;
		this.edgePoint = edgePoint;
		slateEdge0 = new SlateEdge(firstVertex.vertexPoint, edgePoint, this, pair);
		slateEdge1 = new SlateEdge(edgePoint, secondVertex.vertexPoint, this, pair);
		primary = false;
	}
	
	public LinearCombination<TopLevelVertex> getEdgePointLc() {
		LinearCombination<TopLevelVertex> lc = new LinearCombination<TopLevelVertex>();
		if (creaseSharpness() > 0) {
			lc.add(vertex, 0.5);
			lc.add(pair.vertex, 0.5);
		} else {
			lc.add(vertex, 0.25);
			lc.add(pair.vertex, 0.25);
			lc.addScaled(face.getFacePointLc(), 0.25);
			lc.addScaled(pair.face.getFacePointLc(), 0.25);
		}
		return lc;
	}
	
	public LinearCombination<TopLevelVertex> getMidPointLc() {
		LinearCombination<TopLevelVertex> lc = new LinearCombination<TopLevelVertex>();
		lc.add(vertex, 0.5);
		lc.add(pair.vertex, 0.5);
		return lc;
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
	
	public DoubleAttr getSharpness() {
		return sharpness;
	}
	
	public double creaseSharpness() {
		if (isBoundary()) {
			return 10.0;
		} else {
			return sharpness.getDouble();
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
