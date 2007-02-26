package sds;

import java.util.Iterator;
import java.util.Random;

import javax.vecmath.*;

import jpatch.entity.Attribute;
import jpatch.entity.AttributeListener;
import jpatch.entity.Constants;

/**
 * 
 */

/**
 * @author sascha
 *
 */
public class TopLevelVertex extends BaseVertex {
	private static final float CREASE0 = 3.0f / 4.0f;
	private static final float CREASE1 = 1.0f / 8.0f;
	
	HalfEdge edge;
	public final Level2Vertex vertexPoint;
	int valence;

	
	final Iterable<Face> faceIterable = new Iterable<Face>() {
		public Iterator<Face> iterator() {
			return new Iterator<Face>() {
				private HalfEdge e = edge.pair;
				boolean hasNext = true;
				
				public boolean hasNext() {
					return hasNext;
				}
				
				public Face next() {
					HalfEdge tmp = e;
					e = e.next.pair;
					hasNext = (e.next != null && e != edge.pair);
					return tmp.face;
				}
				
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	};
	final Iterable<HalfEdge> edgeIterable = new Iterable<HalfEdge>() {
		public Iterator<HalfEdge> iterator() {
			return new Iterator<HalfEdge>() {
				private HalfEdge e = edge;
				boolean hasNext = true;
				
				public boolean hasNext() {
					return hasNext;
				}
				
				public HalfEdge next() {
					HalfEdge tmp = e;
					e = e.pair.next;
					hasNext = (e != null && e != edge);
					return tmp;
				}
				
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	};
	
	public TopLevelVertex() {
		super();
		vertexPoint = new Level2Vertex() {
			@Override
			public void computeDerivedPosition() {
				if (!overridePosition.get()) {
					if (TopLevelVertex.this.corner > 0) {
						position.set(TopLevelVertex.this.pos);
					} else if (crease > 0) {
						Point3d p0 = TopLevelVertex.this.pos;
						Point3d p1 = TopLevelVertex.this.creaseEdge0.pair.vertex.pos;
						Point3d p2 = TopLevelVertex.this.creaseEdge1.pair.vertex.pos;
						position.set(
								p0.x * CREASE0 + (p1.x + p2.x) * CREASE1,
								p0.y * CREASE0 + (p1.y + p2.y) * CREASE1,
								p0.z * CREASE0 + (p1.z + p2.z) * CREASE1
						);
					} else {
						final double k = 1.0 / (valence * valence);
						double w = (valence - 2.0) / valence;
						double x = 0, y = 0, z = 0;
						for (HalfEdge edge : getAdjacentEdges()) {
							Point3d p = edge.face.facePoint.pos;
							x += p.x;
							y += p.y;
							z += p.z;
							p = edge.pair.vertex.pos;
							x += p.x;
							y += p.y;
							z += p.z;
						}
//						for (int i = 1; i < stencil.length; i++) {
//							x += stencil[i].pos.x;
//							y += stencil[i].pos.y;
//							z += stencil[i].pos.z;
//						}
						position.set(x * k + TopLevelVertex.this.pos.x * w, y * k + TopLevelVertex.this.pos.y * w, z * k + TopLevelVertex.this.pos.z * w);
					}
				}
				if (!overrideSharpness.get()) {
					sharpness.set(Math.max(0, TopLevelVertex.this.getSharpness() - 1));
				}
				crease = Math.max(0, TopLevelVertex.this.crease - 1);
				corner = Math.max(0, TopLevelVertex.this.corner - 1);
				creaseEdge0 = TopLevelVertex.this.creaseEdge0;
				creaseEdge1 = TopLevelVertex.this.creaseEdge1;
				creaseEdgeIndex0 = TopLevelVertex.this.creaseEdgeIndex0;
				creaseEdgeIndex1 = TopLevelVertex.this.creaseEdgeIndex1;
			}
		};
	}
	
	public TopLevelVertex(double x, double y, double z) {
		this();
		referencePosition.set(x, y, z);
	}
	
	public TopLevelVertex(Point3d p) {
		this();
		referencePosition.set(p);
	}
	
	public Iterable<HalfEdge> getAdjacentEdges() {
		return edgeIterable;
	}
	
	public Iterable<Face> getAdjacentFaces() {
		return faceIterable;
	}
	
	public int valence() {
		int i = 1; 
		HalfEdge e = edge.pair;
		while (e.next != null && e.next != edge) {
			e = e.next.pair;
			i++;
		}
		return i;
	}
	
	public void analyzeEdges() {
		int i = 0;
		int sharpestEdgeValue0 = 0, sharpestEdgeValue1 = 0, sharpestEdgeValue2 = 0;
		corner = sharpness.get();
		crease = 0;
		for (HalfEdge edge : getAdjacentEdges()) {
			int edgeSharpness = edge.getSharpness();
			if (edgeSharpness > 0) {
				if (edgeSharpness > sharpestEdgeValue0) {
					sharpestEdgeValue2 = sharpestEdgeValue1;
					sharpestEdgeValue1 = sharpestEdgeValue0;
					sharpestEdgeValue0 = edgeSharpness;
					creaseEdgeIndex1 = creaseEdgeIndex0;
					creaseEdgeIndex0 = i;
					creaseEdge1 = creaseEdge0;
					creaseEdge0 = edge;
				} else if (edgeSharpness > sharpestEdgeValue1) {
					sharpestEdgeValue2 = sharpestEdgeValue1;
					sharpestEdgeValue1 = edgeSharpness;
					creaseEdgeIndex1 = i;
					creaseEdge1 = edge;
				} else if (edgeSharpness > sharpestEdgeValue2) {
					sharpestEdgeValue2 = edgeSharpness;
				}
			}
		}
		if (sharpestEdgeValue1 > 0) {
			// there are at least two crease edges, set this
			// crease value to the crease value of the sharpest edge and
			// set the creaseEdgeIndexes to the indexes of the two
			// sharpest edges
			crease = sharpestEdgeValue0;
			if (sharpestEdgeValue2 > corner) {
				// there are more than two crease edges, set this
				// corner value to at least the crease value of the 3rd sharpest edge
				corner = sharpestEdgeValue2;
			}
		}
	}
	
	void validate() {
		HalfEdge e = edge;
		while (edge.prev != null && edge.prev.pair != e) {
			edge = edge.prev.pair;
		}
		valence = valence();
	}
}
