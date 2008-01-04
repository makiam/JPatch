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
	private static int count;
	public final int num = count++;
	
	private final TopLevelVertex vertex;
	private final HalfEdge pair;
	private Face face;
	private HalfEdge prev;
	private HalfEdge next;
	private final Level2Vertex edgePoint;
	private final SlateEdge slateEdge0;
	private final SlateEdge slateEdge1;
	private final DoubleAttr sharpness;
	private final boolean primary;

	public HalfEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex) {
		sharpness = new HardBoundedDoubleAttr(0, 10, 0);
		vertex = firstVertex;
		edgePoint = new Level2Vertex() {
			@Override
			void computeDerivedPosition() {
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
					getFace().getFacePoint().validatePosition();
					Tuple3Attr p2 = getFace().getFacePoint().position;
					getPairFace().getFacePoint().validatePosition();
					Tuple3Attr p3 = getPairFace().getFacePoint().position;
					position.setTuple(
							(p0.getX() + p1.getX() + p2.getX() + p3.getX()) * 0.25,
							(p0.getY() + p1.getY() + p2.getY() + p3.getY()) * 0.25,
							(p0.getZ() + p1.getZ() + p2.getZ() + p3.getZ()) * 0.25
					);
				}
				crease = Math.max(0, edgeSharpness - 1);
			}
			
			@Override
			void computeLimit() {
				double edgeSharpness = HalfEdge.this.creaseSharpness();
				if (edgeSharpness > 0) {
					vertex.getVertexPoint().validatePosition();
					pair.vertex.getVertexPoint().validatePosition();
					Tuple3Attr p1 = vertex.getVertexPoint().position;
					Tuple3Attr p2 = pair.vertex.getVertexPoint().position;
					limit.set(
							position.getX() * CREASE_LIMIT0 + (p1.getX() + p2.getX()) * CREASE_LIMIT1,
							position.getY() * CREASE_LIMIT0 + (p1.getY() + p2.getY()) * CREASE_LIMIT1,
							position.getZ() * CREASE_LIMIT0 + (p1.getZ() + p2.getZ()) * CREASE_LIMIT1
					);
					
					p1 = vertex.position;
					p2 = pair.vertex.position;
					if (face != null) {
						face.getFacePoint().validatePosition();
						Tuple3Attr pf = face.getFacePoint().position;
						uTangent.set(p2.getX() - p1.getX(), p2.getY() - p1.getY(), p2.getZ() - p1.getZ());
						vTangent.set(
								pf.getX() - (p1.getX() + p2.getX()) * 0.5,
								pf.getY() - (p1.getY() + p2.getY()) * 0.5,
								pf.getZ() - (p1.getZ() + p2.getZ()) * 0.5);
					} else {
						pair.face.getFacePoint().validatePosition();
						Tuple3Attr pf = pair.face.getFacePoint().position;
						uTangent.set(p1.getX() - p2.getX(), p1.getY() - p2.getY(), p1.getZ() - p2.getZ());
						vTangent.set(
								pf.getX() - (p1.getX() + p2.getX()) * 0.5,
								pf.getY() - (p1.getY() + p2.getY()) * 0.5,
								pf.getZ() - (p1.getZ() + p2.getZ()) * 0.5);
					}
					normal.cross(uTangent, vTangent);
					normal.normalize();
				} else {
					pair.prev.edgePoint.validatePosition();
					next.edgePoint.validatePosition();
					prev.edgePoint.validatePosition();
					pair.next.edgePoint.validatePosition();
					pair.vertex.getVertexPoint().validatePosition();
					face.getFacePoint().validatePosition();
					vertex.getVertexPoint().validatePosition();
					pair.face.getFacePoint().validatePosition();
					
					Tuple3Attr pf0 = pair.prev.edgePoint.position;
					Tuple3Attr pf1 = next.edgePoint.position;
					Tuple3Attr pf2 = prev.edgePoint.position;
					Tuple3Attr pf3 = pair.next.edgePoint.position;
					Tuple3Attr pe0 = pair.vertex.getVertexPoint().position;
					Tuple3Attr pe1 = face.getFacePoint().position;
					Tuple3Attr pe2 = vertex.getVertexPoint().position;
					Tuple3Attr pe3 = pair.face.getFacePoint().position;
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
	
	private HalfEdge(TopLevelVertex firstVertex, TopLevelVertex secondVertex, HalfEdge neighbor, DoubleAttr sharpness, Level2Vertex edgePoint) {
		this.vertex = firstVertex;
		this.pair = neighbor;
		this.sharpness = sharpness;
		this.edgePoint = edgePoint;
		slateEdge0 = new SlateEdge(firstVertex.getVertexPoint(), edgePoint, this, pair);
		slateEdge1 = new SlateEdge(edgePoint, secondVertex.getVertexPoint(), this, pair);
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
	
	final public HalfEdge getPair() {
		return pair;
	}
	
	final public TopLevelVertex getVertex() {
		return vertex;
	}
	
	final public TopLevelVertex getPairVertex() {
		return pair.vertex;
	}
	
	final public void setFace(Face face) {
		this.face = face;
		edgePoint.invalidate();
	}
	
	final public Face getFace() {
		return face;
	}
	
	final public Face getPairFace() {
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
	
	public HalfEdge getNext() {
		return next;
	}
	
	public HalfEdge getPrev() {
		return prev;
	}
	
	public Level2Vertex getEdgePoint() {
		return edgePoint;
	}
	
	public SlateEdge getSlateEdge0() {
		return slateEdge0;
	}
	
	public SlateEdge getSlateEdge1() {
		return slateEdge1;
	}
	
	public void appendTo(HalfEdge halfEdge) {
		this.prev = halfEdge;
		halfEdge.next = this;
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
