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
			protected void validatePosition() {
				vertex.validateDisplacedPosition();
				pair.vertex.validateDisplacedPosition();
				Point3d e0 = vertex.displacedPosition;
				Point3d e1 = pair.vertex.displacedPosition;
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
		editList.add(new SaveStateEdit());
	}
	
	private class SaveStateEdit extends AbstractSwapEdit {
		private HalfEdge next = HalfEdge.this.next;
		private HalfEdge prev = HalfEdge.this.next;
		private Face face = HalfEdge.this.face;
		private int faceEdgeIndex = HalfEdge.this.faceEdgeIndex;
		private int boundaryType = HalfEdge.this.boundaryType;
		
		private SaveStateEdit() {
			apply(true);
		}
		
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
