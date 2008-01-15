package com.jpatch.entity.sds2;

import static com.jpatch.entity.sds2.SdsWeights.*;

import java.nio.*;

import com.jpatch.entity.*;

import javax.vecmath.*;

public class Face {
	private final HalfEdge[] edges;
	private final double oneOverSides;
	private DerivedVertex facePoint;
	private Material material;
//	private final Face[] children;
	
	public Face(HalfEdge... edges) {
		int sides = edges.length;
		assert sides >= 3 : "edges.length=" + edges.length + ", must be >= 3";
		
		oneOverSides = 1.0 / sides;
		
		this.edges = edges.clone();
//		children = new Face[sides];
		
		// append adges and set their face to this
		int prev = sides - 1;
		for (int i = 0; i < sides; i++) {
			this.edges[i].setFace(this);
			this.edges[i].appendTo(this.edges[prev++]);
			if (prev == sides) {
				prev = 0;
			}
		}
		for (int i = 0; i < sides; i++) {
			this.edges[i].getVertex().organizeEdges();
		}
	}
	
	public int getSides() {
		return edges.length;
	}
	
	public HalfEdge[] getEdges() {
		return edges;
	}
	
	public Material getMaterial() {
		if (material == null) throw new RuntimeException();
		return material;
	}
	
	public void setMaterial(Material material) {
		if (material == null) throw new RuntimeException();
		this.material = material;
	}
	
//	public Face[] getChildren() {
//		return children;
//	}
	
	public void fillArrayLimit(FloatBuffer buffer) {
		facePoint.validateAlteredLimit();
		buffer.clear();
		buffer.put((float) facePoint.alteredNormal.x);
		buffer.put((float) facePoint.alteredNormal.y);
		buffer.put((float) facePoint.alteredNormal.z);
		buffer.put((float) facePoint.alteredLimit.x);
		buffer.put((float) facePoint.alteredLimit.y);
		buffer.put((float) facePoint.alteredLimit.z);
		
		for (com.jpatch.entity.sds2.HalfEdge edge : edges) {
			DerivedVertex v = edge.getVertex().getVertexPoint();
			v.validateAlteredLimit();
			buffer.put((float) v.alteredNormal.x);
			buffer.put((float) v.alteredNormal.y);
			buffer.put((float) v.alteredNormal.z);
			buffer.put((float) v.alteredLimit.x);
			buffer.put((float) v.alteredLimit.y);
			buffer.put((float) v.alteredLimit.z);
			
			v = edge.getEdgePoint();
			v.validateAlteredLimit();
			buffer.put((float) v.alteredNormal.x);
			buffer.put((float) v.alteredNormal.y);
			buffer.put((float) v.alteredNormal.z);
			buffer.put((float) v.alteredLimit.x);
			buffer.put((float) v.alteredLimit.y);
			buffer.put((float) v.alteredLimit.z);
		}
		DerivedVertex v = edges[0].getVertex().getVertexPoint();
		buffer.put((float) v.alteredNormal.x);
		buffer.put((float) v.alteredNormal.y);
		buffer.put((float) v.alteredNormal.z);
		buffer.put((float) v.alteredLimit.x);
		buffer.put((float) v.alteredLimit.y);
		buffer.put((float) v.alteredLimit.z);
		buffer.rewind();
	}
	
	public void fillArrayPosition(FloatBuffer buffer) {
		facePoint.validatePosition();
		buffer.clear();
		buffer.put((float) facePoint.normal.x);
		buffer.put((float) facePoint.normal.y);
		buffer.put((float) facePoint.normal.z);
		buffer.put((float) facePoint.position.x);
		buffer.put((float) facePoint.position.y);
		buffer.put((float) facePoint.position.z);
		
		for (com.jpatch.entity.sds2.HalfEdge edge : edges) {
			AbstractVertex v = edge.getVertex();
			DerivedVertex lv = v.getVertexPoint();
			v.validatePosition();
			buffer.put((float) lv.normal.x);
			buffer.put((float) lv.normal.y);
			buffer.put((float) lv.normal.z);
			buffer.put((float) v.position.x);
			buffer.put((float) v.position.y);
			buffer.put((float) v.position.z);
		}
		AbstractVertex v = edges[0].getVertex();
		DerivedVertex lv = v.getVertexPoint();
		buffer.put((float) lv.normal.x);
		buffer.put((float) lv.normal.y);
		buffer.put((float) lv.normal.z);
		buffer.put((float) v.position.x);
		buffer.put((float) v.position.y);
		buffer.put((float) v.position.z);
		buffer.rewind();
	}
	
	public void invalidate() {
		if (facePoint != null) {
			facePoint.invalidate();
		}
		for (HalfEdge edge : edges) {
			if (edge.getVertex().getVertexPoint() != null) {
				edge.getVertex().getVertexPoint().invalidate();
			}
			if (edge.getEdgePoint() != null) {
				edge.getEdgePoint().invalidate();
			}
		}
		invalidateAltered();
	}
	
	public void invalidateAltered() {
		if (facePoint != null) {
			facePoint.invalidateAltered();
		}
		for (HalfEdge edge : edges) {
			if (edge.getVertex().getVertexPoint() != null) {
				edge.getVertex().getVertexPoint().invalidateAltered();
			}
			if (edge.getEdgePoint() != null) {
				edge.getEdgePoint().invalidateAltered();
			}
		}
	}
	
	public DerivedVertex createFacePoint() {
		assert facePoint == null;
		facePoint = new DerivedVertex() {
			@Override
			protected void computePosition() {
				double x = 0, y = 0, z = 0;
				for (HalfEdge edge : edges) {
					edge.getVertex().validatePosition();
					Point3d p = edge.getVertex().position;
					x += p.x;
					y += p.y;
					z += p.z;
				}
				x *= oneOverSides;
				y *= oneOverSides;
				z *= oneOverSides;
				position.set(x, y, z);
			}
			
			@Override
			protected void computeLimit() {
				validatePosition();
				
				final int sides = edges.length;
				
				final double limitCornerWeight = LIMIT_CORNER_WEIGHTS[sides];
				final double limitEdgeWeight = LIMIT_EDGE_WEIGHTS[sides];
				final double limitCenterWeight = LIMIT_CENTER_WEIGHTS[sides];
				
				final double[] tangentCornerWeights = TANGENT_CORNER_WEIGHTS[sides];
				final double[] tangentEdgeWeights = TANGENT_EDGE_WEIGHTS[sides];
				
				double cx = 0, cy = 0, cz = 0;
				double ex = 0, ey = 0, ez = 0;
				double ux = 0, uy = 0, uz = 0;
				double vx = 0, vy = 0, vz = 0;
				for (int i = 0; i < sides; i++) {
					HalfEdge edge = edges[i];
					DerivedVertex cp = edge.getVertex().getVertexPoint();
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
					
					int j = (i > 0) ? i - 1 : sides - 1;		
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
				computeMatrix();
//				normal.set(0,0,0);
			}
			
			@Override
			protected void computeAlteredLimit() {
				if (true) {
					alteredLimit.set(limit);
					alteredNormal.set(normal);
					return;
				}
				validateAlteredPosition();
				
				final int sides = edges.length;
				
				final double limitCornerWeight = LIMIT_CORNER_WEIGHTS[sides];
				final double limitEdgeWeight = LIMIT_EDGE_WEIGHTS[sides];
				final double limitCenterWeight = LIMIT_CENTER_WEIGHTS[sides];
				
				final double[] tangentCornerWeights = TANGENT_CORNER_WEIGHTS[sides];
				final double[] tangentEdgeWeights = TANGENT_EDGE_WEIGHTS[sides];
				
				double cx = 0, cy = 0, cz = 0;
				double ex = 0, ey = 0, ez = 0;
				double ux = 0, uy = 0, uz = 0;
				double vx = 0, vy = 0, vz = 0;
				for (int i = 0; i < sides; i++) {
					HalfEdge edge = edges[i];
					DerivedVertex cp = edge.getVertex().getVertexPoint();
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
					
					int j = (i > 0) ? i - 1 : sides - 1;		
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
//				normal.set(0,0,0);
			}
			
			public String toString() {
				return "v" + num + "(" + Face.this + ")";
			}
		};
		return facePoint;
	}
	
	public DerivedVertex getFacePoint() {
		return facePoint;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("f{");
		for (int i = 0; i < edges.length; i++) {
			sb.append(edges[i].getVertex());
			if (i < edges.length - 1) {
				sb.append('-');
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
