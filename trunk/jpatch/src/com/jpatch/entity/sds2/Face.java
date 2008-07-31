package com.jpatch.entity.sds2;

import java.nio.*;
import java.util.*;

import com.jpatch.entity.*;
import com.sun.opengl.util.*;

import javax.vecmath.*;

public class Face {
	private static int count;
	private final HalfEdge[] faceEdges;
	
//	private Face[] subFaces;
	private final double oneOverSides;
	private DerivedVertex facePoint;
	private Material material;
	final Face parentFace;
	final int id;
	Point3d midpointPosition = new Point3d();
	Point3d displacedMidpointPosition = new Point3d();
	Vector3d midpointNormal = new Vector3d();
	Vector3d displacedMidpointNormal = new Vector3d();
	private boolean midpointPositionValid;
	private boolean midpointNormalValid;
	private boolean displacedMidpointPositionValid;
	private boolean displacedMidpointNormalValid;
	private boolean limitSurfaceValid;
	private boolean controlSurfaceValid;
	
	private final FloatBuffer limitSurfaceBuffer;
	private final FloatBuffer controlSurfaceBuffer;
	
	public Face(Material material, HalfEdge... edges) {
		this(material, count++, edges);
	}
	
	public Face(Material material, int faceId, HalfEdge... edges) {
		this(material, edges, null, faceId);
	}
	
	public Face(Material material, HalfEdge[] edges, Face parentFace, int edgeIndex) {
//		System.out.println("Face constructor called for edges " + Arrays.toString(edges));
		int sides = edges.length;
		assert sides >= 3 : "edges.length=" + edges.length + ", must be >= 3";
		
		oneOverSides = 1.0 / sides;
		
		this.faceEdges = edges.clone();
		this.parentFace = parentFace;
		this.id = edgeIndex;
		
		limitSurfaceBuffer = BufferUtil.newFloatBuffer(sides * 2 * 12);
		controlSurfaceBuffer = BufferUtil.newFloatBuffer((sides + 2) * 2 * 12);
		
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
		
//		System.out.println("new face is " + this);
	}
	
	public void flip(Set<HalfEdge> edgesToFlip, Set<AbstractVertex> verticesToFlip) {
		HalfEdge[] tmp = faceEdges.clone();
		
		for (int i = 0; i < faceEdges.length; i++) {
			faceEdges[i] = tmp[faceEdges.length - i - 1].getPair();
			edgesToFlip.add(faceEdges[i]);
			verticesToFlip.add(faceEdges[i].getVertex());
		}
		
		if (facePoint != null) {
			for (HalfEdge edge : facePoint.getEdges()) {
				edge.getFace().flip(edgesToFlip, verticesToFlip);
			}
		}
		
		invalidate();
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
	
	public void getMidpointNormal(Tuple3f midPoint) {
		validateDisplacedMidpointNormal();
		midPoint.set(displacedMidpointNormal);
	}
	
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
	
	public FloatBuffer getLimitSurface() {
		if (!limitSurfaceValid) {
			for (HalfEdge edge : faceEdges) {
				AbstractVertex v = edge.getVertex();
				v.validateDisplacedLimit();
				limitSurfaceBuffer.put((float) v.displacedNormal.x);
				limitSurfaceBuffer.put((float) v.displacedNormal.y);
				limitSurfaceBuffer.put((float) v.displacedNormal.z);
				limitSurfaceBuffer.put((float) v.displacedLimit.x);
				limitSurfaceBuffer.put((float) v.displacedLimit.y);
				limitSurfaceBuffer.put((float) v.displacedLimit.z);
			}
			limitSurfaceValid = true;
		}
		limitSurfaceBuffer.rewind();
		return limitSurfaceBuffer;
	}
	
	public FloatBuffer getControlSurface() {
		if (!controlSurfaceValid) {
			controlSurfaceBuffer.rewind();
			validateDisplacedMidpointNormal();
			final float nx = (float) displacedMidpointNormal.x;
			final float ny = (float) displacedMidpointNormal.y;
			final float nz = (float) displacedMidpointNormal.z;
			validateDisplacedMidpointPosition();	// this also validates the displacedPosition of all of the face's vertices
			controlSurfaceBuffer.put(nx);
			controlSurfaceBuffer.put(ny);
			controlSurfaceBuffer.put(nz);
			controlSurfaceBuffer.put((float) displacedMidpointPosition.x);
			controlSurfaceBuffer.put((float) displacedMidpointPosition.y);
			controlSurfaceBuffer.put((float) displacedMidpointPosition.z);
			for (HalfEdge edge : faceEdges) {
				AbstractVertex v = edge.getVertex();
				controlSurfaceBuffer.put(nx);
				controlSurfaceBuffer.put(ny);
				controlSurfaceBuffer.put(nz);
				controlSurfaceBuffer.put((float) v.displacedPosition.x);
				controlSurfaceBuffer.put((float) v.displacedPosition.y);
				controlSurfaceBuffer.put((float) v.displacedPosition.z);
			}
			AbstractVertex v = faceEdges[0].getVertex();
			controlSurfaceBuffer.put(nx);
			controlSurfaceBuffer.put(ny);
			controlSurfaceBuffer.put(nz);
			controlSurfaceBuffer.put((float) v.displacedPosition.x);
			controlSurfaceBuffer.put((float) v.displacedPosition.y);
			controlSurfaceBuffer.put((float) v.displacedPosition.z);
			
			controlSurfaceValid = true;
		}
		controlSurfaceBuffer.rewind();
		return controlSurfaceBuffer;
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
			for (HalfEdge e : edge.getVertex().getEdges()) {
				if (e.getFace() != null) {
					e.getFace().limitSurfaceValid = false;
				}
			}
		}
		midpointPositionValid = false;
		displacedMidpointPositionValid = false;
		midpointNormalValid = false;
		displacedMidpointNormalValid = false;
//		limitSurfaceValid = false;
		controlSurfaceValid = false;
	}
	
	void validateMidpointPosition() {
		if (!midpointPositionValid) {
			/* set midpointPosition to average of this face's corner vertices */
			double x = 0, y = 0, z = 0;
			for (HalfEdge edge : faceEdges) {
				AbstractVertex vertex = edge.getVertex();
				vertex.validateWorldPosition();
				Point3d p = vertex.worldPosition;
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
		validateMidpointPosition();
		double dx, dy, dz;
		double ux = 0, uy = 0, uz = 0;
		double vx = 0, vy = 0, vz = 0;
		final double[] cosinusTable = SdsConstants.COSINUS[faceEdges.length];
		for (int i = 0; i < faceEdges.length; i++) {
			final Point3d p = faceEdges[i].getVertex().worldPosition;
			dx = p.x - midpointPosition.x;
			dy = p.y - midpointPosition.y;
			dz = p.z - midpointPosition.z;
			final double uScale = cosinusTable[i + 1];
			final double vScale = cosinusTable[i];
			ux += dx * uScale;
			uy += dy * uScale;
			uz += dz * uScale;
			vx += dx * vScale;
			vy += dy * vScale;
			vz += dz * vScale;
		}
		midpointNormal.set(
				uy * vz - uz * vy,		// cross product
				uz * vx - ux * vz,
				ux * vy - uy * vx
		);
		midpointNormal.normalize();
		midpointNormalValid = true;
	}
	
	void validateDisplacedMidpointNormal() {
		if (displacedMidpointNormalValid) {
			return;
		}
		validateDisplacedMidpointPosition();
		double dx, dy, dz;
		double ux = 0, uy = 0, uz = 0;
		double vx = 0, vy = 0, vz = 0;
		final double[] cosinusTable = SdsConstants.COSINUS[faceEdges.length];
		for (int i = 0; i < faceEdges.length; i++) {
			final Point3d p = faceEdges[i].getVertex().displacedPosition;
			dx = p.x - displacedMidpointPosition.x;
			dy = p.y - displacedMidpointPosition.y;
			dz = p.z - displacedMidpointPosition.z;
			final double uScale = cosinusTable[i + 1];
			final double vScale = cosinusTable[i];
			ux += dx * uScale;
			uy += dy * uScale;
			uz += dz * uScale;
			vx += dx * vScale;
			vy += dy * vScale;
			vz += dz * vScale;
		}
		displacedMidpointNormal.set(
				uy * vz - uz * vy,		// cross product
				uz * vx - ux * vz,
				ux * vy - uy * vx
		);
		displacedMidpointNormal.normalize();
		displacedMidpointNormalValid = true;
	}
	
	public void disposeFacePoint() {
		facePoint = null;
	}
	
	public DerivedVertex createFacePoint() {
		assert facePoint == null;
		Sds sds = faceEdges[0].getVertex().sds;
		
		facePoint = new DerivedVertex(sds) {
			@Override
			void validateWorldPosition() {
				if (!worldPositionValid) {
					validateDisplacedMidpointPosition();
					worldPosition.set(displacedMidpointPosition);
					worldPositionValid = true;
				}
			}
		};
		return facePoint;
	}
	
	public DerivedVertex getFacePoint() {
		return facePoint;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("f" + Arrays.toString(generateId()) + "{");
		for (int i = 0; i < faceEdges.length; i++) {
			sb.append(faceEdges[i].getVertex());
			if (i < faceEdges.length - 1) {
				sb.append('-');
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
	int addId(int[] id, int index) {
		id[index++] = this.id;
		if (parentFace != null) {
			return parentFace.addId(id, index);
		}
		return index;
	}
	
	public int[] generateId() {
		int[] tmp = new int[SdsConstants.MAX_LEVEL + 1];
		int level = addId(tmp, 0);
		int[] id = new int[level];
		System.arraycopy(tmp, 0, id, 0, level);
		return id;
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
