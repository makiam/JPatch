package com.jpatch.entity.sds2;

import java.nio.*;

import com.jpatch.entity.*;

import javax.vecmath.*;

public class Face {
	private static int count = 0;
	private final HalfEdge[] faceEdges;
//	private Face[] subFaces;
	private final double oneOverSides;
	private DerivedVertex facePoint;
	private Material material;
	private int num = count++;
	Point3d midpointPosition = new Point3d();
	Point3d displacedMidpointPosition = new Point3d();
	Vector3d midpointNormal = new Vector3d();
	Vector3d displacedMidpointNormal = new Vector3d();
	private boolean midpointPositionValid;
	private boolean midpointNormalValid;
	private boolean displacedMidpointPositionValid;
	private boolean displacedMidpointNormalValid;
	
//	private final Face[] children;
	
	public Face(Material material, HalfEdge... edges) {
		int sides = edges.length;
		assert sides >= 3 : "edges.length=" + edges.length + ", must be >= 3";
		
		oneOverSides = 1.0 / sides;
		
		this.faceEdges = edges.clone();
		
//		children = new Face[sides];
		
		// append adges and set their face to this
		int prev = sides - 1;
		for (int i = 0; i < sides; i++) {
			this.faceEdges[i].setFace(this);
			this.faceEdges[i].appendTo(this.faceEdges[prev++]);
			if (prev == sides) {
				prev = 0;
			}
		}
		for (int i = 0; i < sides; i++) {
			this.faceEdges[i].getVertex().organizeEdges();
		}
		this.material = material;
		for (int i = 0; i < edges.length; i++) {
			edges[i].faceEdgeIndex = i;
		}
//		createFacePoint();
	}
	
	public int getSides() {
		return faceEdges.length;
	}
	
	public HalfEdge[] getEdges() {
		return faceEdges;
	}
	
	public Material getMaterial() {
		if (material == null) throw new RuntimeException();
		return material;
	}
	
	public void setMaterial(Material material) {
		if (material == null) throw new RuntimeException();
		this.material = material;
	}
	
	public void getMidpointPosition(Tuple3d midPoint) {
		validateDisplacedMidpointPosition();
		midPoint.set(displacedMidpointPosition);
	}
	
	public void getMidpointNormal(Tuple3d midPoint) {
		validateDisplacedMidpointNormal();
		midPoint.set(displacedMidpointNormal);
	}
	
//	public void setSubFace(int side, Face face) {
//		subFaces[side] = face;
//	}
	
	public void getSubEdges(int side, HalfEdge[] subEdges) {
		assert subEdges.length == 2;

		int nextSide = side + 1;
		if (nextSide == faceEdges.length) {
			nextSide = 0;
		}
		
		int prevSide = side - 1;
		if (prevSide < 0) {
			prevSide = faceEdges.length - 1;
		}
		
		subEdges[0] = facePoint.getEdges()[prevSide].getFace().faceEdges[2];
		subEdges[1] = facePoint.getEdges()[side].getFace().faceEdges[1];
	}
	
	public void getLimitSurface(FloatBuffer buffer) {
		facePoint.validateDisplacedLimit();
		buffer.clear();
		buffer.put((float) facePoint.displacedNormal.x);
		buffer.put((float) facePoint.displacedNormal.y);
		buffer.put((float) facePoint.displacedNormal.z);
		buffer.put((float) facePoint.displacedLimit.x);
		buffer.put((float) facePoint.displacedLimit.y);
		buffer.put((float) facePoint.displacedLimit.z);	
		for (com.jpatch.entity.sds2.HalfEdge edge : faceEdges) {
			DerivedVertex v = edge.getVertex().getVertexPoint();
			v.validateDisplacedLimit();
			buffer.put((float) v.displacedNormal.x);
			buffer.put((float) v.displacedNormal.y);
			buffer.put((float) v.displacedNormal.z);
			buffer.put((float) v.displacedLimit.x);
			buffer.put((float) v.displacedLimit.y);
			buffer.put((float) v.displacedLimit.z);
			
			v = edge.getEdgePoint();
			v.validateDisplacedLimit();
			buffer.put((float) v.displacedNormal.x);
			buffer.put((float) v.displacedNormal.y);
			buffer.put((float) v.displacedNormal.z);
			buffer.put((float) v.displacedLimit.x);
			buffer.put((float) v.displacedLimit.y);
			buffer.put((float) v.displacedLimit.z);
		}
		DerivedVertex v = faceEdges[0].getVertex().getVertexPoint();
		buffer.put((float) v.displacedNormal.x);
		buffer.put((float) v.displacedNormal.y);
		buffer.put((float) v.displacedNormal.z);
		buffer.put((float) v.displacedLimit.x);
		buffer.put((float) v.displacedLimit.y);
		buffer.put((float) v.displacedLimit.z);
		buffer.rewind();
	}
	
	public void getPositionMesh(FloatBuffer buffer) {
		buffer.clear();	
		for (com.jpatch.entity.sds2.HalfEdge edge : faceEdges) {
			AbstractVertex v = edge.getVertex();
			v.validateDisplacedPosition();
			buffer.put((float) v.displacedPosition.x);
			buffer.put((float) v.displacedPosition.y);
			buffer.put((float) v.displacedPosition.z);
		}
		buffer.rewind();
	}
	
	public void getPositionSurface(FloatBuffer buffer) {
		buffer.clear();	
		validateMidpointPosition();
		buffer.put((float) midpointPosition.x);
		buffer.put((float) midpointPosition.y);
		buffer.put((float) midpointPosition.z);
		for (com.jpatch.entity.sds2.HalfEdge edge : faceEdges) {
			AbstractVertex v = edge.getVertex();
			v.validateDisplacedPosition();
			buffer.put((float) v.displacedPosition.x);
			buffer.put((float) v.displacedPosition.y);
			buffer.put((float) v.displacedPosition.z);
		}
		AbstractVertex v = faceEdges[0].getVertex();
		buffer.put((float) v.displacedPosition.x);
		buffer.put((float) v.displacedPosition.y);
		buffer.put((float) v.displacedPosition.z);
		buffer.rewind();
	}
	
	public void getLimitMesh(FloatBuffer buffer) {
		buffer.clear();	
		for (com.jpatch.entity.sds2.HalfEdge edge : faceEdges) {
			AbstractVertex v = edge.getVertex();
			v.validateDisplacedLimit();
			buffer.put((float) v.displacedLimit.x);
			buffer.put((float) v.displacedLimit.y);
			buffer.put((float) v.displacedLimit.z);
		}
		buffer.rewind();
	}
	
	public void invalidate() {
		if (facePoint != null) {
			facePoint.invalidate();
		}
		for (HalfEdge edge : faceEdges) {
			edge.getVertex().displacedLimitValid = false;
			if (edge.getVertex().getVertexPoint() != null) {
				edge.getVertex().getVertexPoint().invalidate();
			}
			if (edge.getEdgePoint() != null) {
				edge.getEdgePoint().invalidate();
			}
		}
		midpointPositionValid = false;
		displacedMidpointPositionValid = false;
		midpointNormalValid = false;
		displacedMidpointNormalValid = false;
	}
	
	void validateMidpointPosition() {
		if (!midpointPositionValid) {
			/* set midpointPosition to average of this face's corner vertices */
			double x = 0, y = 0, z = 0;
			for (HalfEdge edge : faceEdges) {
				AbstractVertex vertex = edge.getVertex();
				vertex.validatePosition();
				Point3d p = vertex.position;
				x += p.x;
				y += p.y;
				z += p.z;
			}
			x *= oneOverSides;
			y *= oneOverSides;
			z *= oneOverSides;
			midpointPosition.set(x, y, z);
			midpointPositionValid = true;
		}
	}
	
	void validateDisplacedMidpointPosition() {
		if (!displacedMidpointPositionValid) {
			/* set midpointPosition to average of this face's corner vertices */
			double x = 0, y = 0, z = 0;
			for (HalfEdge edge : faceEdges) {
				AbstractVertex vertex = edge.getVertex();
				vertex.validateDisplacedPosition();
				Point3d p = vertex.displacedPosition;
				x += p.x;
				y += p.y;
				z += p.z;
			}
			x *= oneOverSides;
			y *= oneOverSides;
			z *= oneOverSides;
			displacedMidpointPosition.set(x, y, z);
			displacedMidpointPositionValid = true;
		}
	}
	
	void validateMidpointNormal() {
		if (midpointNormalValid) {
			return;
		}
		double x = 0, y = 0, z = 0;
		for (HalfEdge edge : faceEdges) {
			Point3d u = edge.getVertex().position;
			double ux = u.x - midpointPosition.x;
			double uy = u.y - midpointPosition.y;
			double uz = u.z - midpointPosition.z;
			Point3d v = edge.getPairVertex().position;
			double vx = v.x - midpointPosition.x;
			double vy = v.y - midpointPosition.y;
			double vz = v.z - midpointPosition.z;
			x += uy * vz - uz * vy;		// cross product
			y += uz * vx - ux * vz;
			z += ux * vy - uy * vx;
		}
		midpointNormal.set(x, y, z);
		midpointNormal.normalize();
		midpointNormalValid = true;
	}
	
	void validateDisplacedMidpointNormal() {
		if (displacedMidpointNormalValid) {
			return;
		}
		double x = 0, y = 0, z = 0;
		for (HalfEdge edge : faceEdges) {
			Point3d u = edge.getVertex().displacedPosition;
			double ux = u.x - displacedMidpointPosition.x;
			double uy = u.y - displacedMidpointPosition.y;
			double uz = u.z - displacedMidpointPosition.z;
			Point3d v = edge.getPairVertex().displacedPosition;
			double vx = v.x - displacedMidpointPosition.x;
			double vy = v.y - displacedMidpointPosition.y;
			double vz = v.z - displacedMidpointPosition.z;
			x += uy * vz - uz * vy;		// cross product
			y += uz * vx - ux * vz;
			z += ux * vy - uy * vx;
		}
		displacedMidpointNormal.set(x, y, z);
		displacedMidpointNormal.normalize();
		displacedMidpointNormalValid = true;
	}
	
//	public void invalidateAltered() {
//		if (facePoint != null) {
//			facePoint.invalidateAltered();
//		}
//		for (HalfEdge edge : faceEdges) {
//			if (edge.getVertex().getVertexPoint() != null) {
//				edge.getVertex().getVertexPoint().invalidateAltered();
//			}
//			if (edge.getEdgePoint() != null) {
//				edge.getEdgePoint().invalidateAltered();
//			}
//		}
//	}
	
	public void disposeFacePoint() {
		facePoint = null;
	}
	
	public DerivedVertex createFacePoint() {
		assert facePoint == null;
		facePoint = new DerivedVertex() {
			@Override
			void validatePosition() {
				if (!positionValid) {
					validateDisplacedMidpointPosition();
					position.set(displacedMidpointPosition);
					positionValid = true;
				}
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
		StringBuilder sb = new StringBuilder("f" + num + "{");
		for (int i = 0; i < faceEdges.length; i++) {
			sb.append(faceEdges[i].getVertex());
			if (i < faceEdges.length - 1) {
				sb.append('-');
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
//	public int hashCode() {
//		return Arrays.hashCode(faceEdges);
//	}
//	
//	public boolean equals(Object o) {
//		if (!(o instanceof Face)) {
//			return false;
//		}
//		return Arrays.equals(faceEdges, ((Face) o).faceEdges);
//	}
}
