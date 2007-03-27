package sds;

import java.util.*;

import javax.vecmath.*;

import jpatch.boundary.settings.Settings;
import jpatch.entity.attributes2.*;
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

	HalfEdge creaseEdge0, creaseEdge1;
	
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
				if (!overridePosition.getBoolean()) {
					if (TopLevelVertex.this.corner > 0) {
						position.setTuple(TopLevelVertex.this.position);
					} else if (TopLevelVertex.this.crease > 0) {
						Tuple3 p0 = TopLevelVertex.this.position;
						Tuple3 p1 = TopLevelVertex.this.creaseEdge0.pair.vertex.position;
						Tuple3 p2 = TopLevelVertex.this.creaseEdge1.pair.vertex.position;
						position.setTuple(
								p0.getX() * CREASE0 + (p1.getX() + p2.getX()) * CREASE1,
								p0.getY() * CREASE0 + (p1.getY() + p2.getY()) * CREASE1,
								p0.getZ() * CREASE0 + (p1.getZ() + p2.getZ()) * CREASE1
						);
					}
					if (TopLevelVertex.this.corner < 1 && TopLevelVertex.this.crease < 1) {
						final double k = 1.0 / (valence * valence);
						double w = (valence - 2.0) / valence;
						double x = 0, y = 0, z = 0;
						for (HalfEdge edge : edges) {
							Tuple3 p = edge.face.facePoint.position;
							x += p.getX();
							y += p.getY();
							z += p.getZ();
							p = edge.pair.vertex.position;
							x += p.getX();
							y += p.getY();
							z += p.getZ();
						}
						double smoothX = x * k + TopLevelVertex.this.position.getX() * w;
						double smoothY = y * k + TopLevelVertex.this.position.getY() * w;
						double smoothZ = z * k + TopLevelVertex.this.position.getZ() * w;
						double t = (TopLevelVertex.this.corner > 0) ? TopLevelVertex.this.corner : TopLevelVertex.this.crease;
						double t1 = 1 - t;
						position.setTuple(
								smoothX * t1 + position.getX() * t,
								smoothY * t1 + position.getY() * t,
								smoothZ * t1 + position.getZ() * t
						);
					}
				}
				if (!overrideSharpness.getBoolean()) {
					sharpness.setDouble(Math.max(0, TopLevelVertex.this.sharpness() - 1));
				}
				crease = Math.max(0, TopLevelVertex.this.crease - 1);
				corner = Math.max(0, TopLevelVertex.this.corner - 1);
				creaseEdge0 = TopLevelVertex.this.creaseEdge0 == null ? null : TopLevelVertex.this.creaseEdge0.slateEdge0;
				creaseEdge1 = TopLevelVertex.this.creaseEdge1 == null ? null : TopLevelVertex.this.creaseEdge1.slateEdge0;
			}
			
			@Override
			public void computeLimit() {
				if (TopLevelVertex.this.corner > 0) {
					TopLevelVertex.this.position.getTuple(limit);
				} else if (TopLevelVertex.this.crease > 0) {
					Tuple3 p1 = TopLevelVertex.this.creaseEdge0.edgePoint.position;
					Tuple3 p2 = TopLevelVertex.this.creaseEdge1.edgePoint.position;
					limit.set(
							position.getX() * CREASE_LIMIT0 + (p1.getX() + p2.getX()) * CREASE_LIMIT1,
							position.getY() * CREASE_LIMIT0 + (p1.getY() + p2.getY()) * CREASE_LIMIT1,
							position.getZ() * CREASE_LIMIT0 + (p1.getZ() + p2.getZ()) * CREASE_LIMIT1
					);
				} else {
					double fx = 0, fy = 0, fz = 0;
					double ex = 0, ey = 0, ez = 0;
					for (HalfEdge edge : edges) {
						Tuple3 p = edge.face.facePoint.position;
						fx += p.getX();
						fy += p.getY();
						fz += p.getZ();
						p = edge.edgePoint.position;
						ex += p.getX();
						ey += p.getY();
						ez += p.getZ();
					}
					limit.set(
							fx * VERTEX_FACE_LIMIT[valence] + ex * VERTEX_EDGE_LIMIT[valence] + position.getX() * VERTEX_POINT_LIMIT[valence],
							fy * VERTEX_FACE_LIMIT[valence] + ey * VERTEX_EDGE_LIMIT[valence] + position.getY() * VERTEX_POINT_LIMIT[valence],
							fz * VERTEX_FACE_LIMIT[valence] + ez * VERTEX_EDGE_LIMIT[valence] + position.getZ() * VERTEX_POINT_LIMIT[valence]
					);
				
				
					float ax = 0;
					float ay = 0;
					float az = 0;
					float bx = 0;
					float by = 0;
					float bz = 0;
					for (int i = 0; i < edges.length; i++) {
						int j = (i + 1) % edges.length;
						Tuple3 p0f = edges[i].face.facePoint.position;
						Tuple3 p0e = edges[i].edgePoint.position;
						Tuple3 p1f = edges[j].face.facePoint.position;
						Tuple3 p1e = edges[j].edgePoint.position;
						float ew = TANGENT_EDGE_WEIGHT[valence][i];
						float fw = TANGENT_FACE_WEIGHT[valence][i];
						ax += p1f.getX() * fw;
						ay += p1f.getY() * fw;
						az += p1f.getZ() * fw;
						ax += p1e.getX() * ew;
						ay += p1e.getY() * ew;
						az += p1e.getZ() * ew;
						bx += p0f.getX() * fw;
						by += p0f.getY() * fw;
						bz += p0f.getZ() * fw;
						bx += p0e.getX() * ew;
						by += p0e.getY() * ew;
						bz += p0e.getZ() * ew;
					}
					uTangent.set(bx, by, bz);
					vTangent.set(ax, ay, az);
					normal.cross(uTangent, vTangent);
					normal.normalize();
				}
			}
		};
	}
	
	public TopLevelVertex(double x, double y, double z) {
		this();
		System.out.println("new TopLevelVertex(" + x + ", " + y + ", " + z + ")");
		position.getReferenceTuple3().setTuple(x, y, z);
		System.out.println("p=" + position + " pref=" + position.getReferenceTuple3());
	}
	
	public TopLevelVertex(Point3d p) {
		this(p.x, p.y, p.z);
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
		double sharpestEdgeValue0 = 0, sharpestEdgeValue1 = 0, sharpestEdgeValue2 = 0;
		corner = sharpness.getDouble();
		crease = 0;
		for (HalfEdge edge : getAdjacentEdges()) {
			double edgeSharpness = edge.creaseSharpness();
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
