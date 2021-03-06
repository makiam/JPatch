package com.jpatch.entity.sds2;

import java.nio.*;
import java.util.*;

import com.jpatch.afw.control.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.AbstractVertex.*;
import com.sun.opengl.util.*;

import javax.vecmath.*;

public class Face {
	public static enum SubdivStatus { NOT_SUBDIVIDED, HELPER, AUTO_SUBDIVIDED, USER_SUBDIVIDED }
	private final HalfEdge[] faceEdges;
	
	private final double oneOverSides;
	private DerivedVertex facePoint;
	Material material;

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

	private SubdivStatus subdivStatus = SubdivStatus.NOT_SUBDIVIDED;
	/* number of neighbor-faces that need this face as a helper */
	private int dependentFaces;
	
	private static int count = 0;
	private final int num = count++;
	
	Face nextFace;
	Face prevFace;
	
	public static Face create(Material material, HalfEdge[] edges, List<JPatchUndoableEdit> editList) {
		Face face = new Face(material, edges);
		
		for (int i = 0; i < edges.length; i++) {
			edges[i].setFace(face, i, editList);
		}

		for (int i = 0; i < edges.length; i++) {
			(face.faceEdges[i].getVertex()).organizeEdges();
		}

		face.invalidate();
		
		return face;
	}
	
	private Face(Material material, HalfEdge[] edges) {

		System.out.println("Face constructor called for edges " + Arrays.toString(edges));
		int sides = edges.length;
		assert sides >= 3 : "edges.length=" + edges.length + ", must be >= 3";
		
		oneOverSides = 1.0 / sides;
		
		this.faceEdges = edges.clone();

		
//		limitSurfaceBuffer = BufferUtil.newFloatBuffer(sides * 2 * 3);
		limitSurfaceBuffer = FloatBuffer.allocate(sides * 2 * 3);
//		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(sides * 2 * 3 * 4);
//		byteBuffer.order(ByteOrder.nativeOrder());
//		limitSurfaceBuffer = byteBuffer.asFloatBuffer();
		
//		limitSurface = new float[sides * 2 * 3];
//		limitSurfaceBuffer = FloatBuffer.wrap(limitSurface);
//		System.out.println(limitSurfaceBuffer.isDirect());
//		controlSurfaceBuffer = BufferUtil.newFloatBuffer((sides + 2) * 2 * 3);
		controlSurfaceBuffer = FloatBuffer.allocate((sides + 2) * 2 * 3);
		
		this.material = material;
		
		
//		System.out.println("new face is " + this);
	}
	
	void insertBefore(Face face) {
		prevFace = face.prevFace;
		nextFace = face;
		face.prevFace = this;
		if (prevFace != null) {
			prevFace.nextFace = this;
		}
	}
	
	void insertAfter(Face face) {
		prevFace = face;
		nextFace = face.nextFace;
		face.nextFace = this;
		if (nextFace != null) {
			nextFace.prevFace = this;
		}
	}
	
	void remove() {
		if (prevFace != null) {
			prevFace.nextFace = nextFace;
		}
		if (nextFace != null) {
			nextFace.prevFace = prevFace;
		}
		nextFace = null;
		prevFace = null;
	}
	
	public Face next() {
		return nextFace;
	}
	
	void flip(Set<HalfEdge> edgesToFlip, Set<AbstractVertex> verticesToFlip) {
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
//		if (material == null) throw new RuntimeException();
		return material;
	}
	
	void setMaterial(Material material) {
//		if (material == null) throw new RuntimeException();
		this.material = material;
	}
	
	public SubdivStatus getSubdivStatus() {
		return subdivStatus;
	}
	
	public void increaseDependentFaceCount() {
		dependentFaces++;
	}
	
	public void setDependentFaceCount(int count) {
		assert count > 0;
		dependentFaces = count;
	}
	
	public int decreaseDependentFaceCount() {
		assert dependentFaces > 0;
		return dependentFaces--;
	}
	
	void setSubdivStatus(SubdivStatus subdivStatus, List<JPatchUndoableEdit> editList) {
		assert subdivStatus == SubdivStatus.NOT_SUBDIVIDED || facePoint != null;
		if (editList != null) {
			editList.add(new SubdivStatusEdit());
		}
		this.subdivStatus = subdivStatus;
	}
	
	public boolean isDrawable() {
//		if (material == null) {
//			return false;
//		}
//		if (facePoint != null) {
//			if (subdivided && facePoint.getEdges()[0].getFace().getMaterial() != null) {
//				return false;
//			}
//		}
//		return true;
//		if (true) return false;
	
		return material != null && (subdivStatus == SubdivStatus.NOT_SUBDIVIDED || subdivStatus == SubdivStatus.HELPER);
	}
	
	public void dispose(List<JPatchUndoableEdit> editList) {
		for (HalfEdge edge : faceEdges) {
			edge.clearFace(editList);
		}
		if (facePoint != null) {
			for (HalfEdge edge : facePoint.getEdges()) {
				edge.getFace().dispose(editList);
			}
		}
		disposeFacePoint(editList);
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
				limitSurfaceBuffer.put((float) v.getNormal().x);
				limitSurfaceBuffer.put((float) v.getNormal().y);
				limitSurfaceBuffer.put((float) v.getNormal().z);
				limitSurfaceBuffer.put((float) v.getLimit().x);
				limitSurfaceBuffer.put((float) v.getLimit().y);
				limitSurfaceBuffer.put((float) v.getLimit().z);
			}
			limitSurfaceValid = true;
		}
		limitSurfaceBuffer.rewind();
		return limitSurfaceBuffer;
	}
	
	public void getLimitSurface(FloatBuffer limitSurfaceBuffer) {
		limitSurfaceBuffer.rewind();
		for (HalfEdge edge : faceEdges) {
			AbstractVertex v = edge.getVertex();
			v.validateDisplacedLimit();
			limitSurfaceBuffer.put((float) v.getNormal().x);
			limitSurfaceBuffer.put((float) v.getNormal().y);
			limitSurfaceBuffer.put((float) v.getNormal().z);
			limitSurfaceBuffer.put((float) v.getLimit().x);
			limitSurfaceBuffer.put((float) v.getLimit().y);
			limitSurfaceBuffer.put((float) v.getLimit().z);
		}
		limitSurfaceBuffer.rewind();
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
				controlSurfaceBuffer.put((float) v.getPos().x);
				controlSurfaceBuffer.put((float) v.getPos().y);
				controlSurfaceBuffer.put((float) v.getPos().z);
			}
			AbstractVertex v = faceEdges[0].getVertex();
			controlSurfaceBuffer.put(nx);
			controlSurfaceBuffer.put(ny);
			controlSurfaceBuffer.put(nz);
			controlSurfaceBuffer.put((float) v.getPos().x);
			controlSurfaceBuffer.put((float) v.getPos().y);
			controlSurfaceBuffer.put((float) v.getPos().z);
			
			controlSurfaceValid = true;
		}
		controlSurfaceBuffer.rewind();
		return controlSurfaceBuffer;
	}
	
	public void invalidate() {
		if (facePoint != null) {
			facePoint.invalidateAll();
		}
		for (HalfEdge edge : faceEdges) {
			if (edge.getVertex().displacement != null) {
				edge.getVertex().displacement.displacedLimitValid = false;
			}
			if (edge.getVertex().getVertexPoint() != null) {
				edge.getVertex().getVertexPoint().invalidate();
			}
			if (edge.getEdgePoint() != null) {
				edge.getEdgePoint().invalidate();
			}
			if (edge.getVertex().getEdges() != null) {
				for (HalfEdge e : edge.getVertex().getEdges()) {
					if (e.getFace() != null) {
						e.getFace().limitSurfaceValid = false;
					}
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
				Point3d p = vertex.getPos();
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
			final Point3d p = faceEdges[i].getVertex().getPos();
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
	
	public void disposeFacePoint(List<JPatchUndoableEdit> editList) {
		if (editList != null) {
			editList.add(new FacePointEdit());
		}
		facePoint = null;
	}
	
	public DerivedVertex createFacePoint(List<JPatchUndoableEdit> editList) {
		assert facePoint == null;
		if (editList != null) {
			editList.add(new FacePointEdit());
		}
		
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
			
			@Override
			void organizeEdges() {
				if (vertexEdges.length != faceEdges.length) {
					return;
				}
				for (int i = 0; i < faceEdges.length; i++) {
					if (faceEdges[i].getEdgePoint() == null) {
						return;
					}
				}
				/* create facePoint edges */
				for (int i = 0; i < faceEdges.length; i++) {
					vertexEdges[i] = HalfEdge.get(this, faceEdges[i].getEdgePoint());
				}
			}
		};
		
		facePoint.boundaryType = BoundaryType.REGULAR;
		facePoint.vertexId = new VertexId.FacePointId(this);
		
		System.out.println("*** facepoint " + facePoint + " created, BT = " + facePoint.boundaryType);
		return facePoint;
	}
	
	public DerivedVertex getFacePoint() {
		return facePoint;
	}
	

	public final DerivedVertex getOrCreateFacePoint(List<JPatchUndoableEdit> editList) {
		return facePoint != null ? facePoint : createFacePoint(editList);
	}

//	int addId(int[] id, int index) {
//		id[index++] = this.id;
//		if (parentFace != null) {
//			return parentFace.addId(id, index);
//		}
//		return index;
//	}
//	
//	public int[] generateId() {
//		int[] tmp = new int[SdsConstants.MAX_LEVEL + 1];
//		int level = addId(tmp, 0);
//		int[] id = new int[level];
//		System.arraycopy(tmp, 0, id, 0, level);
//		return id;
//	}

//	public String toString() {
//		StringBuilder sb = new StringBuilder("f" + Arrays.toString(generateId()) + "{");
//		for (int i = 0; i < faceEdges.length; i++) {
//			sb.append(faceEdges[i].getVertex());
//			if (i < faceEdges.length - 1) {
//				sb.append('-');
//			}
//		}
//		sb.append("}");
//		return sb.toString();
//	}
	
//	int addId(int[] id, int index) {
//		id[index++] = this.id;
//		if (parentFace != null) {
//			return parentFace.addId(id, index);
//		}
//		return index;
//	}
//	
//	public int[] generateId() {
//		int[] tmp = new int[SdsConstants.MAX_LEVEL + 1];
//		int level = addId(tmp, 0);
//		int[] id = new int[level];
//		System.arraycopy(tmp, 0, id, 0, level);
//		return id;
//	}

	
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
	
	@Override
	public String toString() {
		return "f" + num;
	}
	
	public boolean isHelper() {
		return material == null;
	}
	
	/**
	 * Stores facePoint state
	 * Usage: Instanciate the edit, <i>then</i> set the facePoint of this Face to its new value.
	 * @author sascha
	 */
	private class FacePointEdit extends AbstractSwapEdit {
		private DerivedVertex facePoint;

		FacePointEdit() {
			facePoint = Face.this.facePoint;
			apply(false);
		}
		
		@Override
		protected void swap() {
			DerivedVertex tmp = Face.this.facePoint;
			Face.this.facePoint = facePoint;
			facePoint = tmp;
		}
	}
	
	/**
	 * Stores facePoint state
	 * Usage: Instanciate the edit, <i>then</i> set the facePoint of this Face to its new value.
	 * @author sascha
	 */
	public class SubdivStatusEdit extends AbstractSwapEdit {
		private SubdivStatus subdivStatus;
		
		SubdivStatusEdit() {
			subdivStatus = Face.this.subdivStatus;
			apply(false);
		}
		
		@Override
		protected void swap() {
			SubdivStatus tmp = Face.this.subdivStatus;
			Face.this.subdivStatus = subdivStatus;
			subdivStatus = tmp;
		}
	}
}
