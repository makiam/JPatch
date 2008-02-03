package com.jpatch.entity.sds2;

import static com.jpatch.entity.sds2.SdsWeights.*;

import java.util.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;

import javax.vecmath.*;

public class HalfEdge {
	public static final int REGULAR = 0;
	public static final int BOUNDARY = 1;
	
	private final AbstractVertex vertex;
	private final HalfEdge pair;
	private final boolean primary;
	
	private HalfEdge next;
	private HalfEdge prev;
	private Face face;
	int faceEdgeIndex;
	private int boundaryType;
	private DerivedVertex edgePoint;
	
	public HalfEdge(AbstractVertex v0, AbstractVertex v1) {
		this.vertex = v0;
		this.pair = new HalfEdge(v1, this);
		this.primary = true;
	}
	
	private HalfEdge(AbstractVertex v, HalfEdge pair) {
		this.vertex = v;
		this.pair = pair;
		this.primary = false;
	}
	
	public AbstractVertex getVertex() {
		return vertex;
	}
	
	public AbstractVertex getPairVertex() {
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

	public Face getPairFace() {
		return pair.face;
	}
	
	public void setFace(Face face) {
		assert (this.face == null && face != null) || (this.face != null && face == null) : this + ".face=" + this.face + ", face=" + face + ", exactly one must be null";
		this.face = face;
		if (face != null && pair.face != null) {
			boundaryType = pair.boundaryType = REGULAR;
		} else {
			boundaryType = pair.boundaryType = BOUNDARY;
		}
		if (face == null) {
			assert next != null;
			assert prev != null;
			next = null;
			prev = null;
		}
	}
	
	public int getFaceEdgeIndex() {
		return faceEdgeIndex;
	}
	
//	private void computeFaceEdgeIndex() {
//		HalfEdge[] faceEdges = face.getEdges();
//		for (int i = 0; i < faceEdges.length; i++) {
//			if (faceEdges[i] == this) {
//				faceEdgeIndex = i;
//			}
//		}
//		assert false; // should never get here
//	}
	
	public void appendTo(HalfEdge prevEdge) {
		assert prev == null : this + ".prev is " + prev + ", must be null";
		assert prevEdge.next == null : prevEdge + ".next is " + prevEdge.next + ", must be null";
		assert vertex == prevEdge.pair.vertex : "prevEdge.pair.vertex is " + prevEdge.pair.vertex + ", this.vertex = " + vertex + ", must be equal";
		prevEdge.next = this;
		this.prev = prevEdge;
	}
	
	public boolean isPrimary() {
		return primary;
	}
	
	public HalfEdge getPrimary() {
		return primary ? this : pair;
	}
	
	public void disposeEdgePoint() {
		edgePoint = null;
	}
	
	public DerivedVertex createEdgePoint() {
		assert edgePoint == null;
		edgePoint = new DerivedVertex() {

			@Override
			protected void computePosition() {
				vertex.validateAlteredPosition();
				pair.vertex.validateAlteredPosition();
				Point3d e0 = vertex.alteredPosition;
				Point3d e1 = pair.vertex.alteredPosition;
				switch (boundaryType) {
				case REGULAR:
					face.getFacePoint().validatePosition();
					Point3d f0 = face.getFacePoint().position;
					pair.face.getFacePoint().validatePosition();
					Point3d f1 = pair.face.getFacePoint().position;
					position.set(
							(e0.x + e1.x + f0.x + f1.x) * 0.25,
							(e0.y + e1.y + f0.y + f1.y) * 0.25,
							(e0.z + e1.z + f0.z + f1.z) * 0.25
					);
					break;
				case BOUNDARY:
					position.set(
							(e0.x + e1.x) * 0.5,
							(e0.y + e1.y) * 0.5,
							(e0.z + e1.z) * 0.5
					);
					break;
				default:
					assert false;	// should never get here
				}			
			}
			
			@Override
			protected void computeLimit() {
				switch (boundaryType) {
				case REGULAR:
					validatePosition();
					next.edgePoint.validatePosition();
					prev.edgePoint.validatePosition();
					pair.next.edgePoint.validatePosition();
					pair.prev.edgePoint.validatePosition();
					vertex.getVertexPoint().validatePosition();
					pair.vertex.getVertexPoint().validatePosition();
					pair.face.getFacePoint().validatePosition();
					face.getFacePoint().validatePosition();
					
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
					normal.normalize();
					computeMatrix();
				break;
				case BOUNDARY:
					validatePosition();
					vertex.getVertexPoint().validatePosition();
					pair.vertex.getVertexPoint().validatePosition();
					Point3d p1 = vertex.getVertexPoint().position;
					Point3d p2 = pair.vertex.getVertexPoint().position;
					limit.set(
							position.x * CREASE_LIMIT0 + (p1.x + p2.x) * CREASE_LIMIT1,
							position.y * CREASE_LIMIT0 + (p1.y + p2.y) * CREASE_LIMIT1,
							position.z * CREASE_LIMIT0 + (p1.z + p2.z) * CREASE_LIMIT1
					);
					
//					p1 = vertex.position;
//					p2 = pair.vertex.position;
					if (face != null) {
						face.getFacePoint().validatePosition();
						Point3d pf = face.getFacePoint().position;
						uTangent.set(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
						vTangent.set(
								pf.x - (p1.x + p2.x) * 0.5,
								pf.y - (p1.y + p2.y) * 0.5,
								pf.z - (p1.z + p2.z) * 0.5);
					} else {
						pair.face.getFacePoint().validatePosition();
						Point3d pf = pair.face.getFacePoint().position;
						uTangent.set(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
						vTangent.set(
								pf.x - (p1.x + p2.x) * 0.5,
								pf.y - (p1.y + p2.y) * 0.5,
								pf.z - (p1.z + p2.z) * 0.5);
					}
					normal.cross(uTangent, vTangent);
					normal.normalize();
					computeMatrix();
					break;
				default:
					assert false;	// should never get here
				}
			}

			@Override
			protected void computeAlteredLimit() {
//				System.out.println(this + " computeAlteredLimit()");
				switch (boundaryType) {
				case REGULAR:
					validateAlteredPosition();
					next.edgePoint.validateAlteredPosition();
					prev.edgePoint.validateAlteredPosition();
					pair.next.edgePoint.validateAlteredPosition();
					pair.prev.edgePoint.validateAlteredPosition();
					vertex.getVertexPoint().validateAlteredPosition();
					pair.vertex.getVertexPoint().validateAlteredPosition();
					pair.face.getFacePoint().validateAlteredPosition();
					face.getFacePoint().validateAlteredPosition();
					
					Point3d c0 = pair.next.edgePoint.alteredPosition;
					Point3d c1 = pair.prev.edgePoint.alteredPosition;
					Point3d c2 = prev.edgePoint.alteredPosition;
					Point3d c3 = next.edgePoint.alteredPosition;
					
					Point3d e0 = pair.face.getFacePoint().alteredPosition;
					Point3d e1 = pair.vertex.getVertexPoint().alteredPosition;
					Point3d e2 = face.getFacePoint().alteredPosition;
					Point3d e3 = vertex.getVertexPoint().alteredPosition;
					
					alteredLimit.set(
							alteredPosition.x * LIMIT0 + ((e0.x + e2.x) + (e1.x + e3.x)) * LIMIT1 + ((c0.x + c2.x) + (c1.x + c3.x)) * LIMIT2,
							alteredPosition.y * LIMIT0 + ((e0.y + e2.y) + (e1.y + e3.y)) * LIMIT1 + ((c0.y + c2.y) + (c1.y + c3.y)) * LIMIT2,
							alteredPosition.z * LIMIT0 + ((e0.z + e2.z) + (e1.z + e3.z)) * LIMIT1 + ((c0.z + c2.z) + (c1.z + c3.z)) * LIMIT2
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
					
					alteredNormal.cross(uTangent, vTangent);
					alteredNormal.normalize();
				break;
				case BOUNDARY:
					validateAlteredPosition();
					vertex.getVertexPoint().validateAlteredPosition();
					pair.vertex.getVertexPoint().validateAlteredPosition();
					Point3d p1 = vertex.getVertexPoint().alteredPosition;
					Point3d p2 = pair.vertex.getVertexPoint().alteredPosition;
					alteredLimit.set(
							alteredPosition.x * CREASE_LIMIT0 + (p1.x + p2.x) * CREASE_LIMIT1,
							alteredPosition.y * CREASE_LIMIT0 + (p1.y + p2.y) * CREASE_LIMIT1,
							alteredPosition.z * CREASE_LIMIT0 + (p1.z + p2.z) * CREASE_LIMIT1
					);
					
//					p1 = vertex.position;
//					p2 = pair.vertex.position;
					if (face != null) {
						face.getFacePoint().validateAlteredPosition();
						Point3d pf = face.getFacePoint().alteredPosition;
						uTangent.set(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
						vTangent.set(
								pf.x - (p1.x + p2.x) * 0.5,
								pf.y - (p1.y + p2.y) * 0.5,
								pf.z - (p1.z + p2.z) * 0.5);
					} else {
						pair.face.getFacePoint().validateAlteredPosition();
						Point3d pf = pair.face.getFacePoint().alteredPosition;
						uTangent.set(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
						vTangent.set(
								pf.x - (p1.x + p2.x) * 0.5,
								pf.y - (p1.y + p2.y) * 0.5,
								pf.z - (p1.z + p2.z) * 0.5);
					}
					alteredNormal.cross(uTangent, vTangent);
					alteredNormal.normalize();
					break;
				default:
					assert false;	// should never get here
				}
//				System.out.println(this + ".alteredLimit = " + alteredLimit);
//				alteredLimit.set(limit);
//				alteredNormal.set(normal);
//				System.out.println("    alteredLimit = " + alteredLimit);
			}
			
			public String toString() {
				return "v" + num + "(" + HalfEdge.this + ")";
			}
			
		};
		pair.edgePoint = edgePoint;
		return edgePoint;
	}
	
	public DerivedVertex getEdgePoint() {
		return edgePoint;
	}
	
	public String toString() {
		return "e" + vertex.num + "-" + pair.vertex.num;
	}
	
	public void saveState(List<JPatchUndoableEdit> editList) {
		editList.add(new AbstractSwapEdit() {
			private HalfEdge next = HalfEdge.this.next;
			private HalfEdge prev = HalfEdge.this.next;
			private Face face = HalfEdge.this.face;
			private int faceEdgeIndex = HalfEdge.this.faceEdgeIndex;
			private int boundaryType = HalfEdge.this.boundaryType;
			
			@Override
			protected void swap() {
				HalfEdge tmpEdge;
				Face tmpFace;
				int tmpInt;
				
				/* swap state */
				tmpEdge = HalfEdge.this.next; HalfEdge.this.next = next; next = tmpEdge;
				tmpEdge = HalfEdge.this.prev; HalfEdge.this.prev = prev; prev = tmpEdge;
				tmpFace = HalfEdge.this.face; HalfEdge.this.face = face; face = tmpFace;
				tmpInt = HalfEdge.this.faceEdgeIndex; HalfEdge.this.faceEdgeIndex = faceEdgeIndex; faceEdgeIndex = tmpInt;
				tmpInt = HalfEdge.this.boundaryType; HalfEdge.this.boundaryType = boundaryType; boundaryType = tmpInt;
			}
		});
	}
	
//	public int hashCode() {
//		return vertex.hashCode() ^ pair.vertex.hashCode();
//	}
//	
//	public boolean equals(Object o) {
//		if (!(o instanceof HalfEdge)) {
//			return false;
//		}
//		HalfEdge e = (HalfEdge) o;
//		return vertex.equals(e.vertex) && pair.vertex.equals(e.pair.vertex);
//	}
}
