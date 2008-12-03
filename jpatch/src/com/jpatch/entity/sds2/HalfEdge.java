package com.jpatch.entity.sds2;

import static com.jpatch.entity.sds2.SdsWeights.*;

import java.util.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;

import javax.vecmath.*;

public class HalfEdge {
	private static enum BoundaryType { REGULAR, BOUNDARY, STRAY }
	
	private final AbstractVertex vertex;
	private final HalfEdge pair;
	private final boolean primary;
	
//	private HalfEdge next;
//	private HalfEdge prev;
	private HalfEdge subEdge;
	private Face face;
	int faceEdgeIndex;
	private BoundaryType boundaryType = BoundaryType.STRAY;
	private DerivedVertex edgePoint;
	
	public HalfEdge(AbstractVertex v0, AbstractVertex v1) {
		assert v0 != v1;
		this.vertex = v0;
		this.pair = new HalfEdge(v1, this);
		this.primary = true;
		v0.addEdge(this);
	}
	
	private HalfEdge(AbstractVertex v, HalfEdge pair) {
		this.vertex = v;
		this.pair = pair;
		this.primary = false;
		v.addEdge(this);
	}
	
	public void dispose() {
		vertex.removeEdge(this);
		pair.dispose();
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
		if (face != null) {
			int i = faceEdgeIndex + 1;
			if (i >= face.getSides()) {
				i = 0;
			}
			return face.getEdges()[i];
		} else {
			return null;
		}
	}
	
	public HalfEdge getPrev() {
		if (face != null) {
			int i = faceEdgeIndex - 1;
			if (i < 0) {
				i = face.getSides() - 1;
			}
			return face.getEdges()[i];
		} else {
			return null;
		}
	}
	
	public Face getFace() {
		return face;
	}

	public Face getPairFace() {
		return pair.face;
	}
	
	public AbstractVertex[] getVertices(AbstractVertex[] vertices) {
		assert vertices.length == 2;
		vertices[0] = vertex;
		vertices[1] = pair.vertex;
		return vertices;
	}
	
	public void setFace(Face face) {
		assert (this.face == null && face != null) || (this.face != null && face == null) : this + ".face=" + this.face + ", face=" + face + ", exactly one must be null";
		this.face = face;
		if (face != null && pair.face != null) {
			boundaryType = pair.boundaryType = BoundaryType.REGULAR;
		} else if (face == null && pair.face == null) {
			boundaryType = pair.boundaryType = BoundaryType.STRAY;
		} else {
			boundaryType = pair.boundaryType = BoundaryType.BOUNDARY;
		}
//		if (face == null) {
//			assert next != null;
//			assert prev != null;
//			next = null;
//			prev = null;
//		}
	}
	
	public int getFaceEdgeIndex() {
		assert face.getEdges()[faceEdgeIndex] == this : "index=" + faceEdgeIndex + " faceEdges=" + Arrays.toString(face.getEdges());
		return faceEdgeIndex;
	}
	
	public boolean isBoundary() {
		return boundaryType == BoundaryType.BOUNDARY;
	}
	
	public boolean isStray() {
		return boundaryType == BoundaryType.STRAY;
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
	
//	public void appendTo(HalfEdge prevEdge) {
//		assert prev == null : this + ".prev is " + prev + ", must be null";
//		assert prevEdge.next == null : prevEdge + ".next is " + prevEdge.next + ", must be null";
//		assert vertex == prevEdge.pair.vertex : "prevEdge.pair.vertex is " + prevEdge.pair.vertex + ", this.vertex = " + vertex + ", must be equal";
//		prevEdge.next = this;
//		this.prev = prevEdge;
//	}
	
	public boolean isPrimary() {
		return primary;
	}
	
	public HalfEdge getPrimary() {
		return primary ? this : pair;
	}
	
	public void disposeEdgePoint() {
		edgePoint = null;
		subEdge = null;
		pair.subEdge = null;
	}
	
	public DerivedVertex createEdgePoint() {
		assert edgePoint == null;
		assert subEdge == null;
		assert pair.subEdge == null;
		
		Sds sds = vertex.sds;
		edgePoint = new DerivedVertex(sds) {

			@Override
			protected void validateWorldPosition() {
				vertex.validateDisplacedPosition();
				pair.vertex.validateDisplacedPosition();
				Point3d e0 = vertex.getPos();
				Point3d e1 = pair.vertex.getPos();
				switch (boundaryType) {
				case REGULAR:
					face.getFacePoint().validateWorldPosition();
					Point3d f0 = face.getFacePoint().worldPosition;
					pair.face.getFacePoint().validateWorldPosition();
					Point3d f1 = pair.face.getFacePoint().worldPosition;
					worldPosition.set(
							(e0.x + e1.x + f0.x + f1.x) * 0.25,
							(e0.y + e1.y + f0.y + f1.y) * 0.25,
							(e0.z + e1.z + f0.z + f1.z) * 0.25
					);
					break;
				case BOUNDARY:
					worldPosition.set(
							(e0.x + e1.x) * 0.5,
							(e0.y + e1.y) * 0.5,
							(e0.z + e1.z) * 0.5
					);
					break;
				default:
					assert false;	// should never get here
				}			
			}
		};
		pair.edgePoint = edgePoint;
		
		subEdge = new HalfEdge(vertex.getVertexPoint(), edgePoint);
		pair.subEdge = new HalfEdge(pair.vertex.getVertexPoint(), edgePoint);
		
		edgePoint.vertexId = new VertexId.EdgePointId(vertex.vertexId, pair.vertex.vertexId);
		return edgePoint;
	}
	
	public DerivedVertex getEdgePoint() {
		return edgePoint;
	}
	
	public final DerivedVertex getOrCreateEdgePoint() {
		return edgePoint != null ? edgePoint : createEdgePoint();
	}
	
	public HalfEdge getSubEdge() {
		return subEdge;
	}
	
	public String toString() {
		return "e" + vertex + "-" + pair.vertex;// + "(" + (face == null ? "null" : (face.id + ":" + faceEdgeIndex)) + "){" + boundaryType + "}[" + isPrimary() + "]";
	}
	
	public void saveState(List<JPatchUndoableEdit> editList) {
		editList.add(new SaveStateEdit());
	}
	
	void flip() {
		Face tmpFace = face;
		face = pair.face;
		pair.face = tmpFace;
//		HalfEdge tmpNext = next;
//		HalfEdge tmpPrev = prev;
//		next = (pair.prev == null) ? null : pair.prev.pair;
//		prev = (pair.next == null) ? null : pair.next.pair;
//		pair.next = (tmpPrev == null) ? null : tmpPrev.pair;
//		pair.prev = (tmpNext == null) ? null : tmpNext.pair;
	}
	
	private class SaveStateEdit extends AbstractSwapEdit {
//		private HalfEdge next = HalfEdge.this.next;
//		private HalfEdge prev = HalfEdge.this.next;
		private Face face = HalfEdge.this.face;
//		private Face pairFace = HalfEdge.this.pair.face;
		private int faceEdgeIndex = HalfEdge.this.faceEdgeIndex;
		private BoundaryType boundaryType = HalfEdge.this.boundaryType;
		
		private SaveStateEdit() {
			apply(true);
		}
		
		@Override
		protected void swap() {
			HalfEdge tmpEdge;
			Face tmpFace;
			int tmpInt;
			BoundaryType tmpBoundaryType;
			
			/* swap state */
//			tmpEdge = HalfEdge.this.next; HalfEdge.this.next = next; next = tmpEdge;
//			tmpEdge = HalfEdge.this.prev; HalfEdge.this.prev = prev; prev = tmpEdge;
			tmpFace = HalfEdge.this.face; HalfEdge.this.face = face; face = tmpFace;
//			tmpFace = HalfEdge.this.pair.face; HalfEdge.this.pair.face = pairFace; pairFace = tmpFace;
			tmpInt = HalfEdge.this.faceEdgeIndex; HalfEdge.this.faceEdgeIndex = faceEdgeIndex; faceEdgeIndex = tmpInt;
			tmpBoundaryType = HalfEdge.this.boundaryType; HalfEdge.this.boundaryType = boundaryType; boundaryType = tmpBoundaryType;
		}
	}
	
	private HalfEdge getPrevBoundaryEdge() {
		assert boundaryType == BoundaryType.BOUNDARY;
		for (HalfEdge e : vertex.getEdges()) {
			if (e != this && e.boundaryType == BoundaryType.BOUNDARY) {
				return e.pair;
			}
		}
		return null;
	}
	
	private HalfEdge getNextBoundaryEdge() {
		assert boundaryType == BoundaryType.BOUNDARY;
		for (HalfEdge e : pair.vertex.getEdges()) {
			if (e.boundaryType == BoundaryType.BOUNDARY) {
				return e;
			}
		}
		return null;
	}
	
	public HalfEdge faceEdge() {
		assert isBoundary();
		return (getFace() != null) ? this : pair;
	}
	
	public static List<HalfEdge> continguousEdges(HalfEdge edge, Collection<HalfEdge> edges) {
		assert edge.getFace() != null && edge.getPairFace() == null;
		final HalfEdge start = getStartBoundaryEdge(edge, edge, edges);
		edge = start;
		System.out.println("start is " + edge);
		List<HalfEdge> edgeList = new ArrayList<HalfEdge>(edges == null ? 10 : edges.size());
		do {
			System.out.print("adding " + edge + ", ");
			edgeList.add(edge);
			edge = edge.getNextBoundaryEdge();
			System.out.println("next would be " + edge);
		} while (edge != null && edge != start && (edges == null || edges.contains(edge)));
		return edgeList;
	}
	
//	public static boolean isLooped(List<HalfEdge> continguousEdges) {
//		HalfEdge first = continguousEdges.get(0);
//		HalfEdge last = continguousEdges.get(continguousEdges.size() - 1);
//		if (first.getPrevBoundaryEdge() == last) {
//			assert last.getNextBoundaryEdge() == first;
//			return true;
//		}
//		return false;
//	}
	
	private static HalfEdge getStartBoundaryEdge(HalfEdge edge, HalfEdge start, Collection<HalfEdge> edges) {
		HalfEdge prev = edge.getPrevBoundaryEdge();
		System.out.println("edge=" + edge + " prev=" + prev + " start=" + start + " edges=" + edges);
		return (prev == null || prev == start || (edges != null && !edges.contains(prev))) ? edge : getStartBoundaryEdge(prev, start, edges);
	}
	
	public int hashCode() {
		return vertex.hashCode() + 37 * pair.vertex.hashCode();
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof HalfEdge)) {
			return false;
		}
		HalfEdge e = (HalfEdge) o;
		return vertex == e.vertex && pair.vertex == e.pair.vertex;
	}
}
