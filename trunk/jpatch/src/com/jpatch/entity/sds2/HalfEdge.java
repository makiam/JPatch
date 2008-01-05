package com.jpatch.entity.sds2;

import static com.jpatch.entity.sds2.SdsWeights.*;

import javax.vecmath.*;

public class HalfEdge {
	private final Vertex vertex;
	private final HalfEdge pair;
	private HalfEdge next;
	private HalfEdge prev;
	private Face face;
	
	private DerivedVertex edgePoint;
	
	public HalfEdge(Vertex v0, Vertex v1) {
		this.vertex = v0;
		this.pair = new HalfEdge(v1, this);
	}
	
	private HalfEdge(Vertex v, HalfEdge pair) {
		this.vertex = v;
		this.pair = pair;
	}
	
	public Vertex getVertex() {
		return vertex;
	}
	
	public Vertex getPairVertex() {
		return pair.vertex;
	}
	
	public HalfEdge getPair() {
		return pair;
	}
	
	public HalfEdge getNext() {
		return next;
	}
	
	public HalfEdge getPrev() {
		return prev;
	}
	
	public Face getFace() {
		return face;
	}

	public void setFace(Face face) {
		assert (this.face == null && face != null) || (this.face != null && face == null) : this + ".face=" + this.face + ", face=" + face + ", exactly one must be null";
		this.face = face;
	}
	
	public void appendTo(HalfEdge prevEdge) {
		assert prev == null : this + ".prev is " + prev + ", must be null";
		assert prevEdge.next == null : prevEdge + ".next is " + prevEdge.next + ", must be null";
		prevEdge.next = this;
		this.prev = prevEdge;
	}
	
	public DerivedVertex getEdgePoint() {
		return edgePoint;
	}
	
	public void createNextLevel() {
		edgePoint = new DerivedVertex() {

			@Override
			protected void computePosition() {
				Point3d e0 = vertex.position;
				Point3d e1 = pair.vertex.position;
				face.getFacePoint().validatePosition();
				Point3d f0 = face.getFacePoint().position;
				pair.face.getFacePoint().validatePosition();
				Point3d f1 = pair.face.getFacePoint().position;
				position.set(
						(e0.x + e1.x + f0.x + f1.x) * 0.25,
						(e0.y + e1.y + f0.y + f1.y) * 0.25,
						(e0.z + e1.z + f0.z + f1.z) * 0.25
				);
			}
			
			@Override
			protected void computeLimit() {
				validatePosition();
				next.edgePoint.validatePosition();
				prev.edgePoint.validatePosition();
				pair.next.edgePoint.validatePosition();
				pair.prev.edgePoint.validatePosition();
				vertex.getVertexPoint().validatePosition();
				pair.vertex.getVertexPoint().validatePosition();
				
				Point3d c0 = pair.next.edgePoint.position;
				Point3d c1 = pair.prev.edgePoint.position;
				Point3d c2 = prev.edgePoint.position;
				Point3d c3 = next.edgePoint.position;
				
				Point3d e0 = pair.face.getFacePoint().position;
				Point3d e1 = pair.vertex.getVertexPoint().position;
				Point3d e2 = face.getFacePoint().position;
				Point3d e3 = vertex.getVertexPoint().position;
				
				limit.set(
						position.x * LIMIT0 + ((e0.x + e2.x) + (e1.x + e3.x)) * LIMIT1 + ((c0.x + c2.x) + (c1.x + c3.x)) * LIMIT2,
						position.y * LIMIT0 + ((e0.y + e2.y) + (e1.y + e3.y)) * LIMIT1 + ((c0.y + c2.y) + (c1.y + c3.y)) * LIMIT2,
						position.z * LIMIT0 + ((e0.z + e2.z) + (e1.z + e3.z)) * LIMIT1 + ((c0.z + c2.z) + (c1.z + c3.z)) * LIMIT2
				);
				
				vTangent.set(
						(e1.x - e3.x) * 4 + (c1.x - c0.x) + (c2.x - c3.x),
						(e1.y - e3.y) * 4 + (c1.y - c0.y) + (c2.y - c3.y),
						(e1.z - e3.z) * 4 + (c1.z - c0.z) + (c2.z - c3.z)
				);
				
				uTangent.set(
						(e0.x - e2.x) * 4 + (c0.x - c3.x) + (c1.x - c2.x),
						(e0.y - e2.y) * 4 + (c0.y - c3.y) + (c1.y - c2.y),
						(e0.z - e2.z) * 4 + (c0.z - c3.z) + (c1.z - c2.z)
				);
				
				normal.cross(uTangent, vTangent);
			}

		};
		pair.edgePoint = edgePoint;
	}
}
