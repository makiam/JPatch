package com.jpatch.entity.sds2;

import static com.jpatch.entity.sds2.SdsWeights.*;

import java.util.*;

import com.jpatch.afw.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.entity.sds2.AbstractVertex.*;

import javax.vecmath.*;

public class HalfEdge implements Comparable<HalfEdge>{
	private static enum BoundaryType { REGULAR, BOUNDARY, STRAY }
	
	private final AbstractVertex vertex;
	private final HalfEdge pair;
	private final boolean primary;
	
//	private HalfEdge next;
//	private HalfEdge prev;
//	private HalfEdge subEdge;
	private Face face;
	int faceEdgeIndex = -1;
	private BoundaryType boundaryType = BoundaryType.STRAY;
	private DerivedVertex edgePoint;
	
	/**
	 * Returns a HalfEdge connecting the specified vertices if such an edge has already been created and added to v0, null otherwise.
	 * @param v0 the first vertex
	 * @param v1 the second vertex
	 * @return the HalfEdge connecting the specified vertices
	 */
	public static HalfEdge get(AbstractVertex v0, AbstractVertex v1) {
		assert v0 != null && v1 != null;
		if (v0.getEdges() != null) {
			for (HalfEdge edge : v0.getEdges()) {
				if (edge != null && edge.pair.vertex == v1) {
					return edge;
				}
			}
		}
		if (v1.getEdges() != null) {
			for (HalfEdge edge : v1.getEdges()) {
				if (edge != null && edge.pair.vertex == v0) {
					return edge.pair;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns a HalfEdge connecting the specified vertices if such an edge has already been created and added to v0.
	 * Otherwise a new edge is created and returned.
	 * If it's a new edge, it also gets added to the BaseVertices.
	 * @param v0 the first vertex
	 * @param v1 the second vertex
	 * @return the HalfEdge connecting the specified vertices
	 */
	public static HalfEdge getOrCreate(AbstractVertex v0, AbstractVertex v1, List<JPatchUndoableEdit> editList) {
		HalfEdge halfEdge = get(v0, v1);
		if (halfEdge == null) {
			halfEdge = new HalfEdge(v0, v1);
			v0.addEdge(halfEdge, editList);
			v1.addEdge(halfEdge.pair, editList);
		}
		return halfEdge;
	}
	
	/**
	 * Constructor for creating the primary HalfEdge
	 */
	private HalfEdge(AbstractVertex v0, AbstractVertex v1) {
		assert v0 != v1;
		this.vertex = v0;
		this.pair = new HalfEdge(v1, this);
		this.primary = true;
	}
	
	/**
	 * Constructor for creating the secondary HalfEdge (always called from primary HalfEdge constructor)
	 */
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
	
	public AbstractVertex[] getVertices(AbstractVertex[] vertices) {
		assert vertices.length == 2;
		vertices[0] = vertex;
		vertices[1] = pair.vertex;
		return vertices;
	}
	
	public HalfEdge getPair() {
		return pair;
	}
	
	public BoundaryType getBoundaryType() {
		return boundaryType;
	}
	
	public HalfEdge getNext() {
		if (face != null) {
			assert face.getEdges()[faceEdgeIndex] == this : "index=" + faceEdgeIndex + " faceEdges=" + Arrays.toString(face.getEdges());
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
			assert face.getEdges()[faceEdgeIndex] == this : "index=" + faceEdgeIndex + " faceEdges=" + Arrays.toString(face.getEdges());
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
	
	/**
	 * Clears this halfEdge's face. If the pair's face also is null, it will also
	 * dispose this edge (and thus dispose the edgePoint, if it exists, and removes
	 * both halfEdges from their vertices).
	 * @param editList
	 */
	public void clearFace(List<JPatchUndoableEdit> editList) {
		assert face != null : this + ": can't clear face, face already is null";
		changeFace(null, -1, editList);
		if (pair.face == null) {
			dispose(editList);
		}
	}
	
	public void setFace(Face face, int faceEdgeIndex, List<JPatchUndoableEdit> editList) {
		assert face != null : this + ": can't set face to " + face + ", face was already set to " + this.face;
		assert face.getEdges()[faceEdgeIndex] == this : "index=" + faceEdgeIndex + " faceEdges=" + Arrays.toString(face.getEdges());
		changeFace(face, faceEdgeIndex, editList);
	}
	
	/**
	 * Disposes this edge. Disposes the edgePoint if it exists and removes
	 * both halfEdges from their vertices.
	 * @param editList
	 */
	private void dispose(List<JPatchUndoableEdit> editList) {
		if (edgePoint != null) {
			removeEdgePoint(editList);
		}
		vertex.removeEdge(this, editList);
		pair.vertex.removeEdge(pair, editList);
	}
	
	private void changeFace(Face face, int faceEdgeIndex, List<JPatchUndoableEdit> editList) {
		System.out.println(this + ".changeFace(" + face + ", " + faceEdgeIndex + ")");
		
		FaceEdit edit = new FaceEdit(face, faceEdgeIndex);
		if (editList != null) {
			editList.add(edit);
		}
		
		if (edgePoint != null) {
			if (editList != null) {
				editList.add(edgePoint.new VertexEdgesEdit());
				editList.add(edgePoint.new BoundaryTypeEdit());
			}
	// TODO ???		edgePoint.organizeEdges();
		}
	}
	
	public int getFaceEdgeIndex() {
		assert face == null || face.getEdges()[faceEdgeIndex] == this : "index=" + faceEdgeIndex + " faceEdges=" + Arrays.toString(face.getEdges());
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
	
	/**
	 * removes the edge-point from this edge;
	 * @param editList
	 */
	private void removeEdgePoint(List<JPatchUndoableEdit> editList) {
		EdgePointEdit edit = new EdgePointEdit(null);
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	public DerivedVertex createEdgePoint(List<JPatchUndoableEdit> editList) {
		assert edgePoint == null;
		if (!isPrimary()) {
			return pair.createEdgePoint(editList);
		}
		
		Sds sds = vertex.sds;
		
		DerivedVertex newEdgePoint = new DerivedVertex(sds) {
			@Override
			protected void validateWorldPosition() {
				if (!worldPositionValid) {
					vertex.validateDisplacedPosition();
					pair.vertex.validateDisplacedPosition();
					Point3d e0 = vertex.getPos();
					Point3d e1 = pair.vertex.getPos();
					if (face != null && pair.face != null) {
						boundaryType = BoundaryType.REGULAR;
						face.getFacePoint().validateWorldPosition();
						Point3d f0 = face.getFacePoint().worldPosition;
						pair.face.getFacePoint().validateWorldPosition();
						Point3d f1 = pair.face.getFacePoint().worldPosition;
						worldPosition.set(
								(e0.x + e1.x + f0.x + f1.x) * 0.25,
								(e0.y + e1.y + f0.y + f1.y) * 0.25,
								(e0.z + e1.z + f0.z + f1.z) * 0.25
						);
					} else {
						boundaryType = BoundaryType.BOUNDARY;
						worldPosition.set(
								(e0.x + e1.x) * 0.5,
								(e0.y + e1.y) * 0.5,
								(e0.z + e1.z) * 0.5
						);
					}
					worldPositionValid = true;
				}
			}
			
			@Override
			void organizeEdges() {
//				boundaryType = BoundaryType.HELPER;
				final DerivedVertex vertexPoint = vertex.getVertexPoint();
				final DerivedVertex pairVertexPoint = pair.vertex.getVertexPoint();
				if (vertexPoint != null && pairVertexPoint != null) {
					if (face != null) {
						final DerivedVertex facePoint = face.getFacePoint();
						if (facePoint != null) {
							if (pair.face != null) {
								/* REGULAR */
								final DerivedVertex pairFacePoint = pair.face.getFacePoint();
								if (pairFacePoint != null) {
//									boundaryType = BoundaryType.REGULAR;
									if (vertexEdges.length == 4) {
										vertexEdges[0] = HalfEdge.get(this, pairVertexPoint);
										vertexEdges[1] = HalfEdge.get(this, facePoint);
										vertexEdges[2] = HalfEdge.get(this, vertexPoint);
										vertexEdges[3] = HalfEdge.get(this, pairFacePoint);
									}
								}
							} else {
								/* BOUNDARY (right side) */
//								boundaryType = BoundaryType.BOUNDARY;
								if (vertexEdges.length == 3) {
									vertexEdges[0] = HalfEdge.get(this, pairVertexPoint);
									vertexEdges[1] = HalfEdge.get(this, facePoint);
									vertexEdges[2] = HalfEdge.get(this, vertexPoint);
								}
							}
						}
					} else {
						if (pair.face != null) {
							final DerivedVertex pairFacePoint = pair.face.getFacePoint();
							if (pairFacePoint != null) {
								/* BOUNDARY (left side) */
//								boundaryType = BoundaryType.BOUNDARY;
								if (vertexEdges.length == 3) {
									vertexEdges[0] = HalfEdge.get(this, vertexPoint);
									vertexEdges[1] = HalfEdge.get(this, pairFacePoint);
									vertexEdges[2] = HalfEdge.get(this, pairVertexPoint);
								}
							}
						} else {
							throw new AssertionError("should never get here");
						}
					}
				}
			}
		};
		EdgePointEdit edit = new EdgePointEdit(newEdgePoint);
		if (editList != null) {
			editList.add(edit);
		}
		edgePoint.vertexId = new VertexId.EdgePointId(this);
		return edgePoint;
	}
	
//	private void setEdgePointBoundaryType() {
//		if (face == null) {
//			if (pair.face == null) {
//				edgePoint.boundaryType = AbstractVertex.BoundaryType.STRAY;
//			} else {
//				edgePoint.boundaryType = BoundaryType.STRAY;
//			}
//		}
//	}
	
	public DerivedVertex getEdgePoint() {
		return edgePoint;
	}
	
	public final DerivedVertex getOrCreateEdgePoint(List<JPatchUndoableEdit> editList) {
		return edgePoint != null ? edgePoint : createEdgePoint(editList);
	}
	
//	public HalfEdge getSubEdge() {
//		return subEdge;
//	}
	
	public String toString() {
		return "e" + vertex + "-" + pair.vertex;// + "(" + (face == null ? "null" : (face.id + ":" + faceEdgeIndex)) + "){" + boundaryType + "}[" + isPrimary() + "]";
	}
	
//	public void saveState(List<JPatchUndoableEdit> editList) {
//		editList.add(new SaveStateEdit());
//	}
	
	void flip() {
		
		int tmpFaceEdgeIndex = faceEdgeIndex;
		faceEdgeIndex = pair.face != null ? pair.face.getSides() - 1 - pair.faceEdgeIndex : -1;
		pair.faceEdgeIndex = face != null ? face.getSides() - 1 - tmpFaceEdgeIndex : -1;
		
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
	
//	private class SaveStateEdit extends AbstractSwapEdit {
////		private HalfEdge next = HalfEdge.this.next;
////		private HalfEdge prev = HalfEdge.this.next;
//		private Face face = HalfEdge.this.face;
////		private Face pairFace = HalfEdge.this.pair.face;
//		private int faceEdgeIndex = HalfEdge.this.faceEdgeIndex;
//		private BoundaryType boundaryType = HalfEdge.this.boundaryType;
//		
//		private SaveStateEdit() {
//			apply(true);
//		}
//		
//		@Override
//		protected void swap() {
//			HalfEdge tmpEdge;
//			Face tmpFace;
//			int tmpInt;
//			BoundaryType tmpBoundaryType;
//			
//			/* swap state */
////			tmpEdge = HalfEdge.this.next; HalfEdge.this.next = next; next = tmpEdge;
////			tmpEdge = HalfEdge.this.prev; HalfEdge.this.prev = prev; prev = tmpEdge;
//			tmpFace = HalfEdge.this.face; HalfEdge.this.face = face; face = tmpFace;
////			tmpFace = HalfEdge.this.pair.face; HalfEdge.this.pair.face = pairFace; pairFace = tmpFace;
//			tmpInt = HalfEdge.this.faceEdgeIndex; HalfEdge.this.faceEdgeIndex = faceEdgeIndex; faceEdgeIndex = tmpInt;
//			tmpBoundaryType = HalfEdge.this.boundaryType; HalfEdge.this.boundaryType = boundaryType; boundaryType = tmpBoundaryType;
//		}
//	}
	
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

	public int compareTo(HalfEdge other) {
		return pair.vertex.vertexId.compareTo(other.pair.vertex.vertexId);
	}
	
	/**
	 * Modifies edgePoint
	 * Usage: Instanciate the edit with the new edgePoint value, it will immediately set the HalfEdge's edgePont to the new value.
	 * @author sascha
	 */
	private class EdgePointEdit extends AbstractSwapEdit {
		private DerivedVertex xEdgePoint;

		EdgePointEdit(DerivedVertex edgePoint) {
			xEdgePoint = edgePoint;
			apply(true);
		}
		
		@Override
		protected void swap() {
			DerivedVertex tmp = edgePoint;
			edgePoint = pair.edgePoint = xEdgePoint;
			xEdgePoint = tmp;
		}
	}
	
	/**
	 * Modifies face
	 * Usage: Instanciate the edit with the new face value, it will immediately set the HalfEdge's face to the new value
	 * @author sascha
	 */
	private class FaceEdit extends AbstractSwapEdit {
		private Face xFace;
		private int xFaceEdgeIndex;
		
		FaceEdit(Face face, int faceEdgeIndex) {
			xFace = face;
			xFaceEdgeIndex = faceEdgeIndex;
			apply(true);
		}
		
		@Override
		protected void swap() {
			final Face tmpFace = face;
			face = xFace;
			xFace = tmpFace;
			
			final int tmpFaceEdgeIndex = faceEdgeIndex;
			faceEdgeIndex = xFaceEdgeIndex;
			xFaceEdgeIndex = tmpFaceEdgeIndex;
			
			/* update boundary-type */
			if (face != null) {
				/* invalidate vertices */
				for (HalfEdge edge : face.getEdges()) {
					edge.getVertex().invalidateAll();
				}
				if (pair.face != null) {
					/* invalidate vertices */
					for (HalfEdge edge : pair.face.getEdges()) {
						edge.getVertex().invalidateAll();
					}
					boundaryType = pair.boundaryType = BoundaryType.REGULAR;
				} else {
					boundaryType = pair.boundaryType = BoundaryType.BOUNDARY;
				}
			} else {
				if (pair.face != null) {
					/* invalidate vertices */
					for (HalfEdge edge : pair.face.getEdges()) {
						edge.getVertex().invalidateAll();
					}
					boundaryType = pair.boundaryType = BoundaryType.BOUNDARY;
				} else {
					boundaryType = pair.boundaryType = BoundaryType.STRAY;
				}
			}
		}
	}
}
