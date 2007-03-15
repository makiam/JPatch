package sds;

import java.util.*;

import javax.vecmath.*;

import jpatch.boundary.settings.Settings;
import jpatch.entity.Attribute;
import jpatch.entity.AttributeListener;
import jpatch.entity.Constants;

import static sds.SdsWeights.*;

/**
 * 
 */

/**
 * @author sascha
 *
 */
public class TopLevelVertex extends BaseVertex {
	
	static int count;
	final int num = count++;
	
	HalfEdge[] edges = new HalfEdge[0];
	public final Level2Vertex vertexPoint;
	int valence = -1;

	
	final Iterable<Face> faceIterable = new Iterable<Face>() {
		public Iterator<Face> iterator() {
			return new Iterator<Face>() {
				private int i = 0;
				
				public boolean hasNext() {
					return i < edges.length;
				}
				
				public Face next() {
					return edges[i++].face;
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
				private int i = 0;
				
				public boolean hasNext() {
					return i < edges.length;
				}
				
				public HalfEdge next() {
					return edges[i++];
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
					} else if (TopLevelVertex.this.crease > 0) {
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
						for (HalfEdge edge : edges) {
							Point3d p = edge.face.facePoint.pos;
							x += p.x;
							y += p.y;
							z += p.z;
							p = edge.pair.vertex.pos;
							x += p.x;
							y += p.y;
							z += p.z;
						}
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
			}
			
			@Override
			public void computeLimit() {
				if (TopLevelVertex.this.corner > 0) {
					limit.set(TopLevelVertex.this.pos);
				} else if (TopLevelVertex.this.crease > 0) {
					Point3d p1 = TopLevelVertex.this.creaseEdge0.edgePoint.pos;
					Point3d p2 = TopLevelVertex.this.creaseEdge1.edgePoint.pos;
					limit.set(
							pos.x * CREASE_LIMIT0 + (p1.x + p2.x) * CREASE_LIMIT1,
							pos.y * CREASE_LIMIT0 + (p1.y + p2.y) * CREASE_LIMIT1,
							pos.z * CREASE_LIMIT0 + (p1.z + p2.z) * CREASE_LIMIT1
					);
				} else {
					double fx = 0, fy = 0, fz = 0;
					double ex = 0, ey = 0, ez = 0;
					for (HalfEdge edge : edges) {
						Point3d p = edge.face.facePoint.pos;
						fx += p.x;
						fy += p.y;
						fz += p.z;
						p = edge.edgePoint.pos;
						ex += p.x;
						ey += p.y;
						ez += p.z;
					}
					limit.set(
							fx * VERTEX_FACE_LIMIT[valence] + ex * VERTEX_EDGE_LIMIT[valence] + pos.x * VERTEX_POINT_LIMIT[valence],
							fy * VERTEX_FACE_LIMIT[valence] + ey * VERTEX_EDGE_LIMIT[valence] + pos.y * VERTEX_POINT_LIMIT[valence],
							fz * VERTEX_FACE_LIMIT[valence] + ez * VERTEX_EDGE_LIMIT[valence] + pos.z * VERTEX_POINT_LIMIT[valence]
					);
				
				
					float ax = 0;
					float ay = 0;
					float az = 0;
					float bx = 0;
					float by = 0;
					float bz = 0;
					for (int i = 0; i < edges.length; i++) {
						int j = (i + 1) % edges.length;
						Point3d p0f = edges[i].face.facePoint.pos;
						Point3d p0e = edges[i].edgePoint.pos;
						Point3d p1f = edges[j].face.facePoint.pos;
						Point3d p1e = edges[j].edgePoint.pos;
						float ew = TANGENT_EDGE_WEIGHT[valence][i];
						float fw = TANGENT_FACE_WEIGHT[valence][i];
						ax += p1f.x * fw;
						ay += p1f.y * fw;
						az += p1f.z * fw;
						ax += p1e.x * ew;
						ay += p1e.y * ew;
						az += p1e.z * ew;
						bx += p0f.x * fw;
						by += p0f.y * fw;
						bz += p0f.z * fw;
						bx += p0e.x * ew;
						by += p0e.y * ew;
						bz += p0e.z * ew;
					}
					uTangent.set(bx, by, bz);
					vTangent.set(ax, ay, az);
					normal.cross(uTangent, vTangent);
				}
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
	
	public int getValence() {
		return valence;
	}
	
	public LinearCombination<TopLevelVertex> getVertexPointLc() {
		LinearCombination<TopLevelVertex> lc = new LinearCombination<TopLevelVertex>();
		if (corner > 0) {
			lc.add(this, 1.0);
		} else if (crease > 0) {
			lc.add(this, CREASE0);
			lc.add(creaseEdge0.pair.vertex, CREASE1);
			lc.add(creaseEdge1.pair.vertex, CREASE1);
		} else {
			double n = edges.length;
			for (HalfEdge edge : edges) {
				lc.addScaled(edge.face.getFacePointLc(), 1.0 / n);
				lc.addScaled(edge.getMidPointLc(), 2.0 / n);
			}
			lc.add(this, (n - 3));
			lc.scale(1.0 / n);
		}
		return lc;
	}
	
	public LinearCombination<TopLevelVertex> getLimitLc() {
		LinearCombination<TopLevelVertex> lc = new LinearCombination<TopLevelVertex>();
		if (corner > 0) {
			lc.addScaled(getVertexPointLc(), 1.0);
		} else if (crease > 0) {
			lc.addScaled(getVertexPointLc(), CREASE_LIMIT0);
			lc.addScaled(creaseEdge0.getEdgePointLc(), CREASE_LIMIT1);
			lc.addScaled(creaseEdge1.getEdgePointLc(), CREASE_LIMIT1);
		} else {
			lc.addScaled(getVertexPointLc(), VERTEX_POINT_LIMIT[valence]);
			for (HalfEdge edge : edges) {
				lc.addScaled(edge.face.getFacePointLc(), VERTEX_FACE_LIMIT[valence]);
				lc.addScaled(edge.getEdgePointLc(), VERTEX_EDGE_LIMIT[valence]);
			}
		}
		return lc;
	}
	
	int getEdgeIndex(HalfEdge edge) {
		for (int i = 0; i < edges.length; i++) {
			if (edge == edges[i]) {
				return i;
			}
		}
		System.out.println("vertex=" + this + " edge=" + edge + " edge-pair=" + edge.pair);
		for (HalfEdge ed : getAdjacentEdges()) {
			System.out.println("\tedge=" + ed + " vertex=" + ed.vertex);
		}
		throw new IllegalArgumentException(edge.toString());
	}
	
//	private void computeValence() {
//		valence = 1; 
//		HalfEdge e = edge.prev;
//		while (e.pair != edge) {
//			e = e.pair.prev;
//			valence++;
//			if (e == null) {
//				break;
//			}
//		}
//	}
	
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
//					creaseEdgeIndex1 = creaseEdgeIndex0;
//					creaseEdgeIndex0 = i;
					creaseEdge1 = creaseEdge0;
					creaseEdge0 = edge;
				} else if (edgeSharpness > sharpestEdgeValue1) {
					sharpestEdgeValue2 = sharpestEdgeValue1;
					sharpestEdgeValue1 = edgeSharpness;
//					creaseEdgeIndex1 = i;
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
	
	private HalfEdge getStartEdge(HalfEdge edge) {
		HalfEdge e, prev = edge;
		do {
			e = prev;
			prev = e.pair.next;
		} while (prev != null && prev != edge);
		return e;
	}
	
	void validate() {
		final int n = edges.length;
		final HalfEdge[] tmp = new HalfEdge[n];
		final Set<HalfEdge> addedEdges = new HashSet<HalfEdge>();
		int j = 0;
		for (int i = 0; i < n; i++) {
			HalfEdge start = getStartEdge(edges[i]);
			HalfEdge e = null, next = start;
			do {
				e = next;
				next = e.prev == null ? null : e.prev.pair;
				if (!addedEdges.contains(e)) {
					addedEdges.add(e);
					tmp[j++] = e;
				}
			} while (next != null && next != start);
		}
		if (j != n) {
			throw new IllegalStateException();
		}
		edges = tmp;
		valence = n;
	}
	
	void addEdge(HalfEdge edge) {
		for (HalfEdge e : edges) {
			if (edge == e) {
				throw new IllegalArgumentException(edge + " has already been added to vertex " + this);
			}
		}
		final HalfEdge[] tmp = new HalfEdge[edges.length + 1];
		System.arraycopy(edges, 0, tmp, 0, edges.length);
		tmp[edges.length] = edge;
		edges = tmp;
	}
	
	public String toString() {
		return "v" + num;
	}
}
