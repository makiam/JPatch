package com.jpatch.entity.sds2;

import static com.jpatch.entity.sds2.SdsWeights.*;

import javax.vecmath.*;

public class Vertex {
	private static int count;
	public final int num = count++;
	
	protected final Point3d position = new Point3d();
	
	private HalfEdge[] edges = new HalfEdge[0];
	private HalfEdge preferredStart;
	
	private DerivedVertex vertexPoint;
	private int boundaryType;				// -1 = irregular, 0 = regular, 1 = boundary
	
	public Vertex() {
		;
	}
	
	public Vertex(double x, double y, double z) {
		position.set(x, y, z);
	}
	
	public void getPosition(Tuple3d tuple) {
		validatePosition();
		tuple.set(position);
	}
	
	public void getPosition(Tuple3f tuple) {
		validatePosition();
		tuple.set(position);
	}
	
	public void getLimit(Tuple3f tuple) {
		;
	}
	
	public void getLimit(Tuple3d tuple) {
		;
	}
	
	public void getNormal(Tuple3f tuple) {
		;
	}
	
	public void getNormal(Tuple3d tuple) {
		;
	}
	
	public DerivedVertex getVertexPoint() {
		if (vertexPoint == null) {
			vertexPoint = new DerivedVertex() {
				@Override
				protected void computePosition() {
					final int valence = edges.length;
					final double rimWeight = VERTEX_LIMIT_RIM_WEIGHTS[valence];
					final double centerWeight = VERTEX_LIMIT_CENTER_WEIGHTS[valence];
					double x = 0, y = 0, z = 0;
					HalfEdge edge = edges[0];
					for (int i = 0; i < valence; i++) {
						Vertex fp = edge.getFace().getFacePoint();
						fp.validatePosition();
						x += fp.position.x;
						y += fp.position.y;
						z += fp.position.z;
						Vertex ep = edge.getPairVertex();
						ep.validatePosition();
						x += ep.position.x;
						y += ep.position.y;
						z += ep.position.z;
						edge = edge.getPrev().getPair();
					}
					position.set(
							x * rimWeight + Vertex.this.position.x * centerWeight,
							y * rimWeight + Vertex.this.position.y * centerWeight,
							z * rimWeight + Vertex.this.position.z * centerWeight
					);
				}
				
				@Override
				protected void computeLimit() {
					validatePosition();
					final int valence = edges.length;
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
						HalfEdge edge = edges[i];
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
				}
				
				public String toString() {
					return "v" + num + "(" + Vertex.this + ")";
				}
			};
		}
		return vertexPoint;
	}
	
	public void validatePosition() {
		;
	}
	
	public void validateLimit() {
		;
	}
	
	/**
	 * Adds the specified HalfEdge to this vertex
	 * asserts that edge.getVertex() == this vertex
	 */
	void addEdge(HalfEdge edge) {
		assert edge.getVertex() == this : "edge.vertex=" + edge.getVertex() + ", must be this vertex (" + this + ")";
		HalfEdge[] tmp = new HalfEdge[edges.length + 1];
		System.arraycopy(edges, 0, tmp, 0, edges.length);
		tmp[edges.length] = edge;
		edges = tmp;
	}
	
	/**
	 * Removes the specified HalfEdge from this vertex
	 * @throws ArrayIndexOutOfBoundsException if the specified HalfEdge is not adjacent to this Vertex
	 */
	void removeEdge(HalfEdge edge) {
		int i = 0;
		while (edges[i] != edge) {	// throws ArrayIndexOutOfBoundsException if edge is not part of edges
			i++;
		}
		final HalfEdge[] tmp = new HalfEdge[edges.length - 1];
		System.arraycopy(edges, 0, tmp, 0, i);
		System.arraycopy(edges, i + 1, tmp, i, tmp.length - i);
		edges = tmp;
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
		HalfEdge[] tmp = edges.clone();
		HalfEdge e = tmp[0];
		while(e.getPair().getNext() != null && e.getPair().getNext() != tmp[0]) {
			e = e.getPair().getNext();
		}
		
		if (e.getPrev() != null) {
			boundaryType = 0;	// regular vertex
			// check if edges contains preferredStart and, if yes, use it as start-edge. Else set preferredStart to current start-edge
			for (int i = 0; i < tmp.length; i++) {
				if (tmp[i] == preferredStart) {
					e = preferredStart;
					break;
				}
			}
			preferredStart = e;
		} else {
			boundaryType = 1;	// regular boundary vertex (corner)
		}
		
		for (int i = 0; i < edges.length; i++) {
			if (e.getPrev() == null) {
				System.arraycopy(tmp, 0, edges, 0, edges.length);
				boundaryType = -1; // irregular boundary vertex, crease edges are edges[0] and edges[edges.length - 1]
				return;
			}
			edges[i] = e;
			e = e.getPrev().getPair();
		}
	}
	
	@Override
	public String toString() {
		return "v" + num;
	}
	
	public static void main(String[] args) {
		Vertex v = new Vertex();
		final int n = 10;
		HalfEdge[] edges = new HalfEdge[n];
		for (int i = 0; i < n; i++) {
			edges[i] = new HalfEdge(v, new Vertex());
		}
		int[] k = new int[] { 3,6,5,4,7,9,8,2,10,1};
		for (int i = 0; i < k.length; i++) {
			int j = k[i] - 1;
			edges[j].appendTo(edges[(j + 1) % n].getPair());
			v.addEdge(edges[j]);
			for (HalfEdge e : v.edges) {
				System.out.print(e + " ");
			}
			System.out.println();
		}
		
		for (int i = 0; i < n; i++) {
			System.out.print(edges[i] + "\t " + edges[i].getPrev() + "\t " + edges[i].getNext() + "\t ");
			System.out.println(edges[i].getPair() + "\t " + edges[i].getPair().getPrev() + "\t " + edges[i].getPair().getNext());
		}
		
		HalfEdge e = v.edges[0];
		do {
			System.out.print(e + " ");
			e = e.getPrev().getPair();
		} while (e != v.edges[0]);
		
	}
}
