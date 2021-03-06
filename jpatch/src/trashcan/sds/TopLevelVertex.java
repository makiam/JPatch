package trashcan.sds;

import java.util.*;

import javax.vecmath.*;

import com.jpatch.afw.attributes.*;

import static trashcan.sds.SdsWeights.*;

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
	Face[] faces = new Face[0];
	private final Level2Vertex vertexPoint;
	int valence = -1;

	HalfEdge creaseEdge0, creaseEdge1;
	double limitFactor;
	
//	final Iterable<Face> faceIterable = new Iterable<Face>() {
//		public Iterator<Face> iterator() {
//			return new Iterator<Face>() {
//				private int i = 0;
//				
//				public boolean hasNext() {
//					return i < edges.length;
//				}
//				
//				public Face next() {
//					return edges[i++].face;
//				}
//				
//				public void remove() {
//					throw new UnsupportedOperationException();
//				}
//			};
//		}
//	};
//	final Iterable<HalfEdge> edgeIterable = new Iterable<HalfEdge>() {
//		public Iterator<HalfEdge> iterator() {
//			return new Iterator<HalfEdge>() {
//				private int i = 0;
//				
//				public boolean hasNext() {
//					return i < edges.length;
//				}
//				
//				public HalfEdge next() {
//					return edges[i++];
//				}
//				
//				public void remove() {
//					throw new UnsupportedOperationException();
//				}
//			};
//		}
//	};
	
	private final AttributePostChangeListener level2Invalidator = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			invalidateLevel2Verices();
		}
	};
	
	public TopLevelVertex() {
		super();
		
		
//		vertexPoint = new Level2Vertex(getVertexPointLc(), getLimitLc(), getTangentLc(0), getTangentLc(1));
		vertexPoint = new Level2Vertex() {
			
			@Override
			void computeDerivedPosition() {
				if (!overridePosition.getBoolean()) {
					if (TopLevelVertex.this.corner > 0) {
						position.setTuple(TopLevelVertex.this.position);
						limitFactor = 1.0;
					} else if (TopLevelVertex.this.crease > 0) {
						Tuple3Attr p0 = TopLevelVertex.this.position;
						Tuple3Attr p1 = TopLevelVertex.this.creaseEdge0.getPair().getVertex().position;
						Tuple3Attr p2 = TopLevelVertex.this.creaseEdge1.getPair().getVertex().position;
						position.setTuple(
								p0.getX() * CREASE0 + (p1.getX() + p2.getX()) * CREASE1,
								p0.getY() * CREASE0 + (p1.getY() + p2.getY()) * CREASE1,
								p0.getZ() * CREASE0 + (p1.getZ() + p2.getZ()) * CREASE1
						);
						limitFactor = CREASE_LIMIT0;
					}
					if (TopLevelVertex.this.corner < 1 && TopLevelVertex.this.crease < 1) {
						final double k = 1.0 / (valence * valence);
						double w = (valence - 2.0) / valence;
						double x = 0, y = 0, z = 0;
						for (HalfEdge edge : edges) {
							edge.getFace().getFacePoint().validatePosition();
							Tuple3Attr p = edge.getFace().getFacePoint().position;
							x += p.getX();
							y += p.getY();
							z += p.getZ();
							p = edge.getPairVertex().position;
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
						limitFactor = (VERTEX_POINT_LIMIT[valence] * t1 + t);
					}
				}
				if (!overrideSharpness.getBoolean()) {
					sharpness.setDouble(Math.max(0, TopLevelVertex.this.sharpness() - 1));
				}
				crease = Math.max(0, TopLevelVertex.this.crease - 1);
				corner = Math.max(0, TopLevelVertex.this.corner - 1);
				creaseEdge0 = TopLevelVertex.this.creaseEdge0 == null ? null : TopLevelVertex.this.creaseEdge0.getSlateEdge0();
				creaseEdge1 = TopLevelVertex.this.creaseEdge1 == null ? null : TopLevelVertex.this.creaseEdge1.getSlateEdge0();			
			}
			
			@Override
			void computeLimit() {
				if (TopLevelVertex.this.corner > 0) {
					TopLevelVertex.this.position.getTuple(limit);
					computeNormal();
				} else if (TopLevelVertex.this.crease > 0) {
					TopLevelVertex.this.creaseEdge0.getEdgePoint().validatePosition();
					TopLevelVertex.this.creaseEdge1.getEdgePoint().validatePosition();
					Tuple3Attr p1 = TopLevelVertex.this.creaseEdge0.getEdgePoint().position;
					Tuple3Attr p2 = TopLevelVertex.this.creaseEdge1.getEdgePoint().position;
					limit.set(
							position.getX() * CREASE_LIMIT0 + (p1.getX() + p2.getX()) * CREASE_LIMIT1,
							position.getY() * CREASE_LIMIT0 + (p1.getY() + p2.getY()) * CREASE_LIMIT1,
							position.getZ() * CREASE_LIMIT0 + (p1.getZ() + p2.getZ()) * CREASE_LIMIT1
					);
					computeNormal();
				} else {
					double fx = 0, fy = 0, fz = 0;
					double ex = 0, ey = 0, ez = 0;
					for (HalfEdge edge : edges) {
						edge.getFace().getFacePoint().validatePosition();
						Tuple3Attr p = edge.getFace().getFacePoint().position;
						fx += p.getX();
						fy += p.getY();
						fz += p.getZ();
						edge.getEdgePoint().validatePosition();
						p = edge.getEdgePoint().position;
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
						Tuple3Attr p0f = edges[i].getFace().getFacePoint().position;
						Tuple3Attr p0e = edges[i].getEdgePoint().position;
						Tuple3Attr p1f = edges[j].getFace().getFacePoint().position;
						Tuple3Attr p1e = edges[j].getEdgePoint().position;
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
			
			private void computeNormal() {
				normal.set(0, 0, 0);
				
				for (int i = 0; i < faces.length; i++) {
					Face face = faces[i];
//					System.out.println(i + " " + face);
					if (face == null) {
						continue;
					}
//					System.out.println(face + " " + face.facePoint.normal);
					face.getFacePoint().validateLimit();
					normal.add(face.getFacePoint().normal);
				}
				
				normal.normalize();
				
			}
		};
		
		position.addAttributePostChangeListener(level2Invalidator);
		sharpness.addAttributePostChangeListener(level2Invalidator);
	}
	
	public TopLevelVertex(TopLevelVertex vertex) {
		this();
		position.getReferenceTuple3().setTuple(vertex.position);
	}
	
	public TopLevelVertex(double x, double y, double z) {
		this();
//		System.out.println("new TopLevelVertex(" + x + ", " + y + ", " + z + ")");
		position.getReferenceTuple3().setTuple(x, y, z);
//		System.out.println("p=" + position + " pref=" + position.getReferenceTuple3());
	}
	
	public TopLevelVertex(Point3d p) {
		this(p.x, p.y, p.z);
	}
	
	public HalfEdge[] getAdjacentEdges() {
		return edges;
	}
	
	public Face[] getAdjacentFaces() {
		return faces;
	}
	
	public int getValence() {
		return valence;
	}
	
	public double getLimitFactor() {
		return limitFactor;
	}
	
	public Level2Vertex getVertexPoint() {
		return vertexPoint;
	}
	
	public LinearCombination<TopLevelVertex> getVertexPointLc() {
		LinearCombination<TopLevelVertex> lc = new LinearCombination<TopLevelVertex>();
		if (corner > 0) {
			lc.add(this, 1.0);
		} else if (crease > 0) {
			lc.add(this, CREASE0);
			lc.add(creaseEdge0.getPairVertex(), CREASE1);
			lc.add(creaseEdge1.getPairVertex(), CREASE1);
		} else {
			double n = edges.length;
			for (HalfEdge edge : edges) {
				lc.addScaled(edge.getFace().getFacePointLc(), 1.0 / n);
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
				lc.addScaled(edge.getFace().getFacePointLc(), VERTEX_FACE_LIMIT[valence]);
				lc.addScaled(edge.getEdgePointLc(), VERTEX_EDGE_LIMIT[valence]);
			}
		}
		return lc;
	}
	
	public LinearCombination<TopLevelVertex> getTangentLc(int direction) {
		LinearCombination<TopLevelVertex> lc = new LinearCombination<TopLevelVertex>();
		if (crease == 0 && corner == 0) {
			for (int i = 0; i < edges.length; i++) {
				switch(direction) {
				case 0:					// u direction
					lc.addScaled(edges[i].getFace().getFacePointLc(), TANGENT_FACE_WEIGHT[valence][i]);
					lc.addScaled(edges[i].getEdgePointLc(), TANGENT_FACE_WEIGHT[valence][i]);
					lc.scale(1 / edges.length / 10);
					break;
				case 1:					// v direction
					lc.addScaled(edges[(i + 1) % edges.length].getFace().getFacePointLc(), TANGENT_FACE_WEIGHT[valence][i]);
					lc.addScaled(edges[(i + 1) % edges.length].getEdgePointLc(), TANGENT_FACE_WEIGHT[valence][i]);
					lc.scale(1 / edges.length / 10);
					break;
				}
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
		System.out.println("vertex=" + this + " edge=" + edge + " edge-pair=" + edge.getPair());
		for (HalfEdge ed : getAdjacentEdges()) {
			System.out.println("\tedge=" + ed + " vertex=" + ed.getVertex());
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
					creaseEdge1 = creaseEdge0;
					creaseEdge0 = edge;
				} else if (edgeSharpness > sharpestEdgeValue1) {
					sharpestEdgeValue2 = sharpestEdgeValue1;
					sharpestEdgeValue1 = edgeSharpness;
					creaseEdge1 = edge;
				} else if (edgeSharpness > sharpestEdgeValue2) {
					sharpestEdgeValue2 = edgeSharpness;
				}
			}
		}
		
//		System.out.println("edge: " + sharpestEdgeValue0 + " " + sharpestEdgeValue1 + " " + sharpestEdgeValue2);
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
			prev = e.getPair().getNext();
		} while (prev != null && prev != edge);
		return e;
	}
	
	public void invalidateLevel2Verices() {
		vertexPoint.invalidate();
		for (HalfEdge edge : edges) {
			edge.getEdgePoint().invalidate();
			Face face = edge.getFace();
			if (face != null) {
				face.getFacePoint().invalidate();
				HalfEdge e = edge.getNext();
				e.getEdgePoint().invalidate();
				for (int i = 0, n = face.getSides() - 2; i < n; i++) {
					TopLevelVertex v = e.getVertex();
					v.getVertexPoint().invalidate();
					for (HalfEdge e2 : v.edges) {
						e2.getEdgePoint().invalidate();
						Face f = e2.getFace();
						if (f != null) {
							f.getFacePoint().invalidate();
						}
					}
					e = e.getNext();
				}
			}
		}
	}
	
	void validate() {
		final int n = edges.length;
		final HalfEdge[] tmp = new HalfEdge[n];
		final Set<HalfEdge> addedEdges = new HashSet<HalfEdge>();
		int j = 0;
		for (int i = 0; i < n; i++) {
			HalfEdge start = getStartEdge(edges[i]);
			System.out.println("validate " + this + " checking " + edges[i]);
			HalfEdge e = null, next = start;
			do {
				e = next;
				next = e.getPrev() == null ? null : e.getPrev().getPair();
				if (!addedEdges.contains(e)) {
					System.out.println("validate " + this + " adding " + e);
					addedEdges.add(e);
					tmp[j++] = e;
				}
			} while (next != null && next != start);
		}
		if (j != n) {
			throw new IllegalStateException();
		}
		edges = tmp;
		for (int i = 0; i < edges.length; i++) {
			faces[i] = edges[i].getFace();
		}
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
		
		final Face[] tmpFace = new Face[faces.length + 1];
		System.arraycopy(faces, 0, tmpFace, 0, faces.length);
		tmpFace[faces.length] = edge.getFace();
		faces = tmpFace;
	}
	
	void removeEdge(HalfEdge edge) {
		int index = getEdgeIndex(edge);
		final HalfEdge[] tmp = new HalfEdge[edges.length - 1];
		System.arraycopy(edges, 0, tmp, 0, index);
		System.arraycopy(edges, index + 1, tmp, index, tmp.length - index);
		edges = tmp;
		
		final Face[] tmpFace = new Face[faces.length - 1];
		System.arraycopy(faces, 0, tmpFace, 0, index);
		System.arraycopy(faces, index + 1, tmpFace, index, tmpFace.length - index);
		faces = tmpFace;
	}
	
	public String toString() {
		return "v" + num;
	}
}
