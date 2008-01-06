package com.jpatch.entity.sds2;

import static com.jpatch.entity.sds2.SdsWeights.*;

import java.util.*;

import javax.vecmath.*;

public class Vertex {
	private static int count;
	public final int num = count++;
	
	protected final Point3d position = new Point3d();
	
	private HalfEdge[] edges = new HalfEdge[0];
	
	private DerivedVertex vertexPoint;
	private int boundaryType;
	
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
//					HalfEdge edge = edges[0];
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
						edge = edge.getPrev().getPair();
					}
					limit.set(
							cx * limitCornerWeight + ex * limitEdgeWeight + position.x * limitCenterWeight,
							cy * limitCornerWeight + ey * limitEdgeWeight + position.y * limitCenterWeight,
							cz * limitCornerWeight + ez * limitEdgeWeight + position.z * limitCenterWeight
					);
					uTangent.set(ux, uy, uz);
					vTangent.set(vx, vy, vz);
					normal.cross(uTangent, vTangent);
//					normal.set(0,0,0);
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
	
	public void addEdge(HalfEdge edge) {
		assert edge.getVertex() == this : "edge.vertex=" + edge.getVertex() + ", must be this vertex (" + this + ")";
		HalfEdge[] tmp = new HalfEdge[edges.length + 1];
		System.arraycopy(edges, 0, tmp, 0, edges.length);
		tmp[edges.length] = edge;
		organizeEdges(tmp);
	}
	
	public void removeEdge(HalfEdge edge) {
		int i = 0;
		while (edges[i] != edge) {	// throws ArrayIndexOutOfBoundsException if edge is not part of edges
			i++;
		}
		final HalfEdge[] tmp = new HalfEdge[edges.length - 1];
		System.arraycopy(edges, 0, tmp, 0, i);
		System.arraycopy(edges, i + 1, tmp, i, tmp.length - i);
		organizeEdges(tmp);
	}
	
	private void organizeEdges(HalfEdge[] newEdges) {
		edges = new HalfEdge[newEdges.length];
		HalfEdge e = newEdges[0];
		while(e.getPair().getNext() != null && e.getPair().getNext() != newEdges[0]) {
			e = e.getPair().getNext();
		}
		if (e.getNext() != null) {
			boundaryType = 0;	// regular vertex
		}
		for (int i = 0; i < edges.length; i++) {
			if (e.getPrev() == null) {
				System.arraycopy(newEdges, 0, edges, 0, edges.length);
				boundaryType = -1; // irregular boundary vertex, crease edges are edges[0] and edges[edges.length - 1]
				return;
			}
			edges[i] = e;
			e = e.getPrev().getPair();
		}
		boundaryType = 1;	// regular boundary vertex (corner)
	}
	
	@Override
	public String toString() {
		return "v" + num;
	}
}
