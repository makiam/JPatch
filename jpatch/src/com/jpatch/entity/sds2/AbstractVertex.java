package com.jpatch.entity.sds2;

import static com.jpatch.entity.sds2.SdsWeights.*;
import com.jpatch.afw.attributes.*;

import javax.vecmath.*;

public class AbstractVertex {
	public static final int IRREGULAR = -1;
	public static final int REGULAR = 0;
	public static final int BOUNDARY = 1;
	
	private static int count;
	public final int num = count++;
	
	protected final Point3d position = new Point3d();
	protected final Point3d alteredPosition;
	protected Point3d alteredLimit;
	
	protected HalfEdge[] vertexEdges = new HalfEdge[0];
	protected HalfEdge preferredStart;
	
	protected DerivedVertex vertexPoint;
	protected int boundaryType;
	
	protected final Tuple3Attr positionAttr = new Tuple3Attr();
	
	private boolean invalid = true;
//	protected boolean alteredInvalid = true;
	
	public AbstractVertex() {
		alteredPosition = position;
	}
	
	AbstractVertex(Point3d alteredPosition) {
		this.alteredPosition = alteredPosition;
	}
	
	public Tuple3Attr getPositionAttribute() {
		return positionAttr;
	}
	
	public void getPosition(Tuple3d position) {
		validatePosition();
		position.set(this.position);
	}
	
	public void getPosition(Tuple3f position) {
		validatePosition();
		position.set(this.position);
	}
	
	public void setPosition(Tuple3d position) {
		positionAttr.setTuple(position);
//		invalid = true;
//		alteredInvalid = true;
		invalidate();
	}
	
	public void setPosition(double x, double y, double z) {
		positionAttr.setTuple(x, y, z);
//		invalid = true;
//		alteredInvalid = true;
		invalidate();
	}
	
	public void getLimit(Tuple3f limit) {
		throw new UnsupportedOperationException();
	}
	
	public void getLimit(Tuple3d limit) {
		throw new UnsupportedOperationException();
	}
	
	public void getNormal(Tuple3f normal) {
		throw new UnsupportedOperationException();
	}
	
	public void getNormal(Tuple3d normal) {
		throw new UnsupportedOperationException();
	}
	
	public DerivedVertex createVertexPoint() {
		assert vertexPoint == null;
		vertexPoint = new DerivedVertex() {
			@Override
			protected void computePosition() {
				AbstractVertex.this.validateAlteredPosition();
				switch (AbstractVertex.this.boundaryType) {
				case REGULAR:
					final int valence = vertexEdges.length;
					final double rimWeight = VERTEX_LIMIT_RIM_WEIGHTS[valence];
					final double centerWeight = VERTEX_LIMIT_CENTER_WEIGHTS[valence];
					double x = 0, y = 0, z = 0;
					HalfEdge edge = AbstractVertex.this.vertexEdges[0];
					for (int i = 0; i < valence; i++) {
						DerivedVertex fp = edge.getFace().getFacePoint();
						fp.validatePosition();
						x += fp.position.x;
						y += fp.position.y;
						z += fp.position.z;
						AbstractVertex ep = edge.getPairVertex();
						ep.validateAlteredPosition();
						x += ep.alteredPosition.x;
						y += ep.alteredPosition.y;
						z += ep.alteredPosition.z;
						edge = edge.getPrev().getPair();
					}
					position.set(
							x * rimWeight + AbstractVertex.this.alteredPosition.x * centerWeight,
							y * rimWeight + AbstractVertex.this.alteredPosition.y * centerWeight,
							z * rimWeight + AbstractVertex.this.alteredPosition.z * centerWeight
					);
					break;
				case BOUNDARY:
					Point3d p0 = AbstractVertex.this.alteredPosition;
					AbstractVertex.this.vertexEdges[0].getPair().getVertex().validateAlteredPosition();
					Point3d p1 = AbstractVertex.this.vertexEdges[0].getPair().getVertex().alteredPosition;
					AbstractVertex.this.vertexEdges[AbstractVertex.this.vertexEdges.length - 1].getPair().getVertex().validateAlteredPosition();
					Point3d p2 = AbstractVertex.this.vertexEdges[AbstractVertex.this.vertexEdges.length - 1].getPair().getVertex().alteredPosition;
					position.set(
							p0.x * CREASE0 + (p1.x + p2.x) * CREASE1,
							p0.y * CREASE0 + (p1.y + p2.y) * CREASE1,
							p0.z * CREASE0 + (p1.z + p2.z) * CREASE1
					);
					break;
				case IRREGULAR:
					position.set(AbstractVertex.this.alteredPosition);
					break;
				default:
					assert false;	// should never get here
				}
//				System.out.println(this + " position=" + position);
			}
			
			@Override
			protected void computeLimit() {
				validatePosition();
				switch (AbstractVertex.this.boundaryType) {
				case REGULAR:
					final int valence = vertexEdges.length;
					final double limitCornerWeight = LIMIT_CORNER_WEIGHTS[valence];
					final double limitEdgeWeight = LIMIT_EDGE_WEIGHTS[valence];
					final double limitCenterWeight = LIMIT_CENTER_WEIGHTS[valence];
					
					final double[] tangentCornerWeights = TANGENT_CORNER_WEIGHTS[valence];
					final double[] tangentEdgeWeights = TANGENT_EDGE_WEIGHTS[valence];
					
					double cx = 0, cy = 0, cz = 0;
					double ex = 0, ey = 0, ez = 0;
					double ux = 0, uy = 0, uz = 0;
					double vx = 0, vy = 0, vz = 0;
					for (int i = 0; i < valence; i++) {
						HalfEdge edge = AbstractVertex.this.vertexEdges[i];
						DerivedVertex cp = edge.getPairFace().getFacePoint();
						cp.validatePosition();
						cx += cp.position.x;
						cy += cp.position.y;
						cz += cp.position.z;
						DerivedVertex ep = edge.getEdgePoint();
						ep.validatePosition();
						ex += ep.position.x;
						ey += ep.position.y;
						ez += ep.position.z;
						
						ux += cp.position.x * tangentCornerWeights[i] + ep.position.x * tangentEdgeWeights[i];
						uy += cp.position.y * tangentCornerWeights[i] + ep.position.y * tangentEdgeWeights[i];
						uz += cp.position.z * tangentCornerWeights[i] + ep.position.z * tangentEdgeWeights[i];
						
						int j = (i > 0) ? i - 1 : valence - 1;	
						vx += cp.position.x * tangentCornerWeights[j] + ep.position.x * tangentEdgeWeights[j];
						vy += cp.position.y * tangentCornerWeights[j] + ep.position.y * tangentEdgeWeights[j];
						vz += cp.position.z * tangentCornerWeights[j] + ep.position.z * tangentEdgeWeights[j];	
					}
					limit.set(
							cx * limitCornerWeight + ex * limitEdgeWeight + position.x * limitCenterWeight,
							cy * limitCornerWeight + ey * limitEdgeWeight + position.y * limitCenterWeight,
							cz * limitCornerWeight + ez * limitEdgeWeight + position.z * limitCenterWeight
					);
					uTangent.set(ux, uy, uz);
					vTangent.set(vx, vy, vz);
					normal.cross(uTangent, vTangent);
					normal.normalize();
					computeMatrix();
					break;
				case BOUNDARY:
					AbstractVertex.this.vertexEdges[0].getEdgePoint().validatePosition();
					AbstractVertex.this.vertexEdges[AbstractVertex.this.vertexEdges.length - 1].getEdgePoint().validatePosition();
					Point3d p1 = AbstractVertex.this.vertexEdges[0].getEdgePoint().position;
					Point3d p2 = AbstractVertex.this.vertexEdges[AbstractVertex.this.vertexEdges.length - 1].getEdgePoint().position;
					limit.set(
							alteredPosition.x * CREASE_LIMIT0 + (p1.x + p2.x) * CREASE_LIMIT1,
							alteredPosition.y * CREASE_LIMIT0 + (p1.y + p2.y) * CREASE_LIMIT1,
							alteredPosition.z * CREASE_LIMIT0 + (p1.z + p2.z) * CREASE_LIMIT1
					);
					computeNormal();
					break;
				case IRREGULAR:
					limit.set(alteredPosition);
					computeNormal();
					break;
				default:
					assert false;	// should never get here
				}
//				System.out.println(this + " limit=" + limit);
			}
			
			/**
			 * Estimate normal by taking the sum of all adjacent face normals
			 */
			private final void computeNormal() {
				normal.set(0, 0, 0);				
				for (int i = 0; i < AbstractVertex.this.vertexEdges.length; i++) {
					Face face = AbstractVertex.this.vertexEdges[i].getFace();
					if (face != null) {
						face.getFacePoint().validateLimit();
						normal.add(face.getFacePoint().normal);
					}
				}
				AbstractVertex vu = AbstractVertex.this.vertexEdges[0].getPairVertex();
				AbstractVertex vv = AbstractVertex.this.vertexEdges[1].getPairVertex();
				AbstractVertex.this.validatePosition();
				vu.validatePosition();
				vv.validatePosition();
				uTangent.sub(vv.position, AbstractVertex.this.position);
				double vlength = uTangent.length();
				vTangent.sub(vu.position, AbstractVertex.this.position);
				double ulength = vTangent.length();
				uTangent.cross(uTangent, normal);
				vTangent.cross(uTangent, normal);
				uTangent.normalize();
				vTangent.normalize();
				normal.normalize();
				uTangent.scale(ulength);
				vTangent.scale(vlength);
				computeMatrix();
			}
			
			/**
			 * Estimate normal by taking the sum of all adjacent face normals
			 */
			private final void computeAlteredNormal() {
				alteredNormal.set(0, 0, 0);				
				for (int i = 0; i < AbstractVertex.this.vertexEdges.length; i++) {
					Face face = AbstractVertex.this.vertexEdges[i].getFace();
					if (face != null) {
						face.getFacePoint().validateAlteredLimit();
						alteredNormal.add(face.getFacePoint().alteredNormal);
					}
				}
				alteredNormal.normalize();
			}
			
			public String toString() {
				return "v" + num + "(" + AbstractVertex.this + ")";
			}

			@Override
			protected void computeAlteredLimit() {
//				System.out.println(this + " computeAlteredLimit()");
				validateAlteredPosition();
				switch (AbstractVertex.this.boundaryType) {
				case REGULAR:
					final int valence = vertexEdges.length;
					final double limitCornerWeight = LIMIT_CORNER_WEIGHTS[valence];
					final double limitEdgeWeight = LIMIT_EDGE_WEIGHTS[valence];
					final double limitCenterWeight = LIMIT_CENTER_WEIGHTS[valence];
					
					final double[] tangentCornerWeights = TANGENT_CORNER_WEIGHTS[valence];
					final double[] tangentEdgeWeights = TANGENT_EDGE_WEIGHTS[valence];
					
					double cx = 0, cy = 0, cz = 0;
					double ex = 0, ey = 0, ez = 0;
					double ux = 0, uy = 0, uz = 0;
					double vx = 0, vy = 0, vz = 0;
					for (int i = 0; i < valence; i++) {
						HalfEdge edge = AbstractVertex.this.vertexEdges[i];
						DerivedVertex cp = edge.getPairFace().getFacePoint();
						cp.validateAlteredPosition();
						cx += cp.alteredPosition.x;
						cy += cp.alteredPosition.y;
						cz += cp.alteredPosition.z;
						DerivedVertex ep = edge.getEdgePoint();
						ep.validateAlteredPosition();
						ex += ep.alteredPosition.x;
						ey += ep.alteredPosition.y;
						ez += ep.alteredPosition.z;
						
						ux += cp.alteredPosition.x * tangentCornerWeights[i] + ep.alteredPosition.x * tangentEdgeWeights[i];
						uy += cp.alteredPosition.y * tangentCornerWeights[i] + ep.alteredPosition.y * tangentEdgeWeights[i];
						uz += cp.alteredPosition.z * tangentCornerWeights[i] + ep.alteredPosition.z * tangentEdgeWeights[i];
						
						int j = (i > 0) ? i - 1 : valence - 1;	
						vx += cp.alteredPosition.x * tangentCornerWeights[j] + ep.alteredPosition.x * tangentEdgeWeights[j];
						vy += cp.alteredPosition.y * tangentCornerWeights[j] + ep.alteredPosition.y * tangentEdgeWeights[j];
						vz += cp.alteredPosition.z * tangentCornerWeights[j] + ep.alteredPosition.z * tangentEdgeWeights[j];	
					}
					alteredLimit.set(
							cx * limitCornerWeight + ex * limitEdgeWeight + alteredPosition.x * limitCenterWeight,
							cy * limitCornerWeight + ey * limitEdgeWeight + alteredPosition.y * limitCenterWeight,
							cz * limitCornerWeight + ez * limitEdgeWeight + alteredPosition.z * limitCenterWeight
					);
					uTangent.set(ux, uy, uz);
					vTangent.set(vx, vy, vz);
					alteredNormal.cross(uTangent, vTangent);
					alteredNormal.normalize();
					break;
				case BOUNDARY:
					AbstractVertex.this.vertexEdges[0].getEdgePoint().validateAlteredPosition();
					AbstractVertex.this.vertexEdges[AbstractVertex.this.vertexEdges.length - 1].getEdgePoint().validateAlteredPosition();
					Point3d p1 = AbstractVertex.this.vertexEdges[0].getEdgePoint().alteredPosition;
					Point3d p2 = AbstractVertex.this.vertexEdges[AbstractVertex.this.vertexEdges.length - 1].getEdgePoint().alteredPosition;
					alteredLimit.set(
							alteredPosition.x * CREASE_LIMIT0 + (p1.x + p2.x) * CREASE_LIMIT1,
							alteredPosition.y * CREASE_LIMIT0 + (p1.y + p2.y) * CREASE_LIMIT1,
							alteredPosition.z * CREASE_LIMIT0 + (p1.z + p2.z) * CREASE_LIMIT1
					);
					computeAlteredNormal();
					break;
				case IRREGULAR:
					alteredLimit.set(alteredPosition);
					computeAlteredNormal();
					break;
				default:
					assert false;	// should never get here
				}
//				System.out.println("    alteredLimit = " + alteredLimit);
			}
		};
		if (alteredLimit == null) {
			alteredLimit = vertexPoint.alteredLimit;
		}
		return vertexPoint;
	}
	
	public double getLimitFactor() {
		switch (boundaryType) {
		case REGULAR:
			return LIMIT_CENTER_WEIGHTS[vertexEdges.length];
		case BOUNDARY:
			return CREASE_LIMIT0;
		case IRREGULAR:
			return 1.0;
		}
		assert false;	// should never get here
		return 0;
	}
	
	public HalfEdge[] getEdges() {
		return vertexEdges;
	}
	
	public DerivedVertex getVertexPoint() {
		return vertexPoint;
	}
	
	public void validatePosition() {
		invalid = false;
	}
	
	public void validateLimit() {
		invalid = false;
	}
	
	public void validateAlteredPosition() {
		invalid = false;
	}
	
	public void validateAlteredLimit() {
		invalid = false;
	}
	
	public void invalidate() {
		if (!invalid) {
			for (HalfEdge edge : vertexEdges) {
				if (edge.getFace() != null) {
					edge.getFace().invalidate();
				}
			}
			invalid = true;
		}
//		invalidateAltered();
	}
	
//	public void invalidateAltered() {
////		System.out.println("AbstractVertex.invalidateAltered() called on object " + this);
//		if (!alteredInvalid) {
//			for (HalfEdge edge : vertexEdges) {
//				if (edge.getFace() != null) {
//					edge.getFace().invalidateAltered();
//				}
//			}
//			alteredInvalid = true;
//		}
//	}
	
	/**
	 * Adds the specified HalfEdge to this vertex
	 * asserts that edge.getVertex() == this vertex
	 */
	void addEdge(HalfEdge edge) {
		assert edge.getVertex() == this : "edge.vertex=" + edge.getVertex() + ", must be this vertex (" + this + ")";
		HalfEdge[] tmp = new HalfEdge[vertexEdges.length + 1];
		System.arraycopy(vertexEdges, 0, tmp, 0, vertexEdges.length);
		tmp[vertexEdges.length] = edge;
		vertexEdges = tmp;
	}
	
	/**
	 * Removes the specified HalfEdge from this vertex
	 * @throws ArrayIndexOutOfBoundsException if the specified HalfEdge is not adjacent to this Vertex
	 */
	void removeEdge(HalfEdge edge) {
		boolean debug = false;
		if (debug) System.out.println("removing edge " + edge + " from vertex " + this);
		if (debug) System.out.print("    edges are:");
		if (debug) for (HalfEdge e : vertexEdges) System.out.print(" " + e);
		if (debug) System.out.println();
		int i = 0;
		while (vertexEdges[i] != edge) {	// throws ArrayIndexOutOfBoundsException if edge is not part of edges
			i++;
		}
		final HalfEdge[] tmp = new HalfEdge[vertexEdges.length - 1];
		System.arraycopy(vertexEdges, 0, tmp, 0, i);
		System.arraycopy(vertexEdges, i + 1, tmp, i, tmp.length - i);
		vertexEdges = tmp;
		if (debug) System.out.print("    edges are:");
		if (debug) for (HalfEdge e : vertexEdges) System.out.print(" " + e);
		if (debug) System.out.println();
	}
	
	/**
	 * This method must be called whenever a face adjacent to this vertex was created or destroyed.
	 * It will sort the edge-array, depending on the type of this vertex:
	 * <ul>
	 * <li>Regular: edge[n + 1].getPrev().getPair() == edge[n]. If possible, old start-edge is used.</li>
	 * <li>Boundary: edge[n + 1].getPrev().getPair() == edge[n] for all but the last edge
	 * <li>Irregular: No particular order
	 * </ul>
	 * TODO: preferredStart method will not work properly with undo/redo
	 */
	void organizeEdges() {
		boolean debug = false;
		if (debug) System.out.println(this + " organizeEdges() called...");
		
		if (debug) System.out.print("    edges are:");
		if (debug) for (HalfEdge e : vertexEdges) System.out.print(" " + e);
		if (debug) System.out.println();
		
		if (vertexEdges.length == 0) {
			return;
		}
		HalfEdge[] tmp = vertexEdges.clone();
		HalfEdge e = tmp[0];
		while(e.getPair().getNext() != null && e.getPair().getNext() != tmp[0]) {
			e = e.getPair().getNext();
		}
		
		if (e.getPair().getNext() != null) {
			boundaryType = REGULAR;	// regular vertex
			// check if edges contains preferredStart and, if yes, use it as start-edge. Else set preferredStart to current start-edge
			for (int i = 0; i < tmp.length; i++) {
				if (tmp[i] == preferredStart) {
					e = preferredStart;
					break;
				}
			}
			preferredStart = e;
		} else {
			boundaryType = BOUNDARY;	// regular boundary vertex (corner)
		}
		
		
		
		for (int i = 0; i < vertexEdges.length; i++) {
			if (i < vertexEdges.length - 1 && e.getPrev() == null) {
				System.arraycopy(tmp, 0, vertexEdges, 0, vertexEdges.length);
				boundaryType = IRREGULAR; // irregular boundary vertex, crease edges are edges[0] and edges[edges.length - 1]
				break;
			}
			vertexEdges[i] = e;
			if (i < vertexEdges.length - 1) {
				e = e.getPrev().getPair();
			}
		}
		
		if (debug) System.out.print("    edges are:");
		if (debug) for (HalfEdge ed : vertexEdges) System.out.print(" " + ed);
		if (debug) System.out.println();
		if (debug) System.out.println("    boundaryType = " + boundaryType);
		
		invalidate();
		
	}
	
	@Override
	public String toString() {
		return "v" + num;
	}
	
	public static void main(String[] args) {
		BaseVertex v = new BaseVertex();
		final int n = 10;
		HalfEdge[] edges = new HalfEdge[n];
		for (int i = 0; i < n; i++) {
			edges[i] = new HalfEdge(v, new BaseVertex());
		}
		int[] k = new int[] { 3,6,5,4,7,9,8,2,10,1};
		for (int i = 0; i < k.length; i++) {
			int j = k[i] - 1;
			edges[j].appendTo(edges[(j + 1) % n].getPair());
			v.addEdge(edges[j]);
			for (HalfEdge e : v.vertexEdges) {
				System.out.print(e + " ");
			}
			System.out.println();
		}
		
		for (int i = 0; i < n; i++) {
			System.out.print(edges[i] + "\t " + edges[i].getPrev() + "\t " + edges[i].getNext() + "\t ");
			System.out.println(edges[i].getPair() + "\t " + edges[i].getPair().getPrev() + "\t " + edges[i].getPair().getNext());
		}
		
		HalfEdge e = v.vertexEdges[0];
		do {
			System.out.print(e + " ");
			e = e.getPrev().getPair();
		} while (e != v.vertexEdges[0]);
		
	}
}
