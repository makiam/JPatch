package com.jpatch.entity.sds2;


import static java.lang.Math.*;
import java.util.*;
import com.jpatch.afw.control.*;
import javax.vecmath.*;

public class AbstractVertex {
	private static final int MAX_VALENCE = 64;
	private static final double[][] TANGENT_FACE_WEIGHTS = new double[MAX_VALENCE + 1][];			// [valence][index]
	private static final double[][] TANGENT_PAIR_WEIGHTS = new double[MAX_VALENCE + 1][];
	private static final double[][] IRREGULAR_TANGENT_WEIGHTS = new double[MAX_VALENCE + 1][];
	private static final double[][] BOUNDARY_TANGENT_WEIGHTS = new double[MAX_VALENCE + 1][];
	private static final double CREASE0 = 3.0f / 4.0f;
	private static final double CREASE1 = 1.0f / 8.0f;
	private static final double CREASE_LIMIT0 = 2.0f / 3.0f;
	private static final double CREASE_LIMIT1 = 1.0f / 6.0f;
	private static final double[] VERTEX_LIMIT_RIM_WEIGHTS = new double[MAX_VALENCE + 1];
	private static final double[] VERTEX_LIMIT_CENTER_WEIGHTS = new double[MAX_VALENCE + 1];
	
	static {
		for (int valence = 3; valence <= MAX_VALENCE; valence++) {
			TANGENT_FACE_WEIGHTS[valence] = new double[valence];
			TANGENT_PAIR_WEIGHTS[valence] = new double[valence];
			double An = 1 + cos(2 * PI / valence) + cos(PI / valence) * sqrt(2 * (9 + cos(2 * PI / valence)));
			for (int j = 0; j < valence; j++) {
				TANGENT_PAIR_WEIGHTS[valence][j] = (cos(2 * PI * j / valence) + cos(2 * PI * (j + 1) / valence)) / 4.0;
			}
			for (int j = 0; j < valence; j++) {
				int prev = j - 1;
				if (prev < 0) {
					prev = valence - 1;
				}
				int next = j + 1;
				if (next >= valence) {
					next = 0;
				}
				TANGENT_FACE_WEIGHTS[valence][j] = An * cos(2 * PI  * j / valence)
					+ TANGENT_PAIR_WEIGHTS[valence][prev]
					+ TANGENT_PAIR_WEIGHTS[valence][next];
			}
			
			IRREGULAR_TANGENT_WEIGHTS[valence] = new double[valence];
			BOUNDARY_TANGENT_WEIGHTS[valence] = new double[valence];
			for (int i = 0; i < valence; i++) {
				IRREGULAR_TANGENT_WEIGHTS[valence][i] = cos(2 * PI * i / valence);
				BOUNDARY_TANGENT_WEIGHTS[valence][i] = sin(PI * i / valence);
			}
			
			VERTEX_LIMIT_RIM_WEIGHTS[valence] = 1.0 / (valence * valence);
			VERTEX_LIMIT_CENTER_WEIGHTS[valence] = (valence - 2.0) / valence;
		}
	}
	
	public static final int IRREGULAR = -1;
	public static final int REGULAR = 0;
	public static final int BOUNDARY = 1;
	
	private static int count;
	public final int num = count++;
	
	final Point3d position = new Point3d();
	final Point3d displacedPosition = new Point3d();
	final Point3d limit = new Point3d();
	final Point3d displacedLimit = new Point3d();
	final Vector3d uTangent = new Vector3d();
	final Vector3d displacedUTangent = new Vector3d();
	final Vector3d vTangent = new Vector3d();
	final Vector3d displacedVTangent = new Vector3d();
	final Vector3d normal = new Vector3d();
	final Vector3d displacedNormal = new Vector3d();
	final Matrix4d displacementMatrix = new Matrix4d();
	final Matrix4d invDisplacementMatrix = new Matrix4d();
	
	HalfEdge[] vertexEdges = new HalfEdge[0];
	HalfEdge preferredStart;
	
	private AbstractVertex vertexPoint;
	private int boundaryType;
	
	private boolean isDisplaced;
	
	boolean positionValid;
	boolean displacedPositionValid;
	boolean limitValid;
	boolean displacedLimitValid;
	boolean displacementMatrixValid;
	boolean invDisplacementMatrixValid;
	
	public void getPosition(Tuple3d position) {
		validateDisplacedPosition();
		position.set(this.displacedPosition);
	}
	
	public void getPosition(Tuple3f position) {
		validateDisplacedPosition();
		position.set(this.displacedPosition);
	}
	
	public void setPosition(Tuple3d position) {
		this.position.set(position);
		invalidate();
	}
	
	public void setPosition(double x, double y, double z) {
		position.set(x, y, z);
		invalidate();
	}
	
	public void getLimit(Tuple3f limit) {
		validateDisplacedLimit();
		limit.set(this.displacedLimit);
	}
	
	public void getLimit(Tuple3d limit) {
		validateDisplacedLimit();
		limit.set(this.displacedLimit);
	}
	
	public void getNormal(Tuple3f normal) {
		validateDisplacedLimit();
		normal.set(this.displacedNormal);
	}
	
	public void getNormal(Tuple3d normal) {
		validateDisplacedLimit();
		normal.set(this.displacedNormal);
	}
	
	public void disposeVertexPoint() {
		vertexPoint = null;
	}
	
	public AbstractVertex createVertexPoint() {
		assert vertexPoint == null;
		vertexPoint = new AbstractVertex() {
			@Override
			protected void validatePosition() {
				if (!positionValid) {
					AbstractVertex.this.validateDisplacedPosition();
					switch (AbstractVertex.this.boundaryType) {
					case REGULAR:
						final int valence = vertexEdges.length;
						final double rimWeight = VERTEX_LIMIT_RIM_WEIGHTS[valence];
						final double centerWeight = VERTEX_LIMIT_CENTER_WEIGHTS[valence];
						double x = 0, y = 0, z = 0;
						for (HalfEdge edge : AbstractVertex.this.vertexEdges) {
							Face face = edge.getFace();
							face.validateDisplacedMidpointPosition();
							Point3d fp = face.displacedMidpointPosition;
							x += fp.x;
							y += fp.y;
							z += fp.z;
							AbstractVertex pair = edge.getPairVertex();
							pair.validateDisplacedPosition();
							Point3d ep = pair.displacedPosition;
							x += ep.x;
							y += ep.y;
							z += ep.z;
						}
						position.set(
								x * rimWeight + AbstractVertex.this.displacedPosition.x * centerWeight,
								y * rimWeight + AbstractVertex.this.displacedPosition.y * centerWeight,
								z * rimWeight + AbstractVertex.this.displacedPosition.z * centerWeight
						);
						break;
					case BOUNDARY:
						AbstractVertex v0 = AbstractVertex.this;
						AbstractVertex v1 = AbstractVertex.this.vertexEdges[0].getPairVertex();
						AbstractVertex v2 = AbstractVertex.this.vertexEdges[AbstractVertex.this.vertexEdges.length - 1].getPairVertex();
						Point3d p0 = v0.displacedPosition;
						v1.validateDisplacedPosition();
						Point3d p1 = v1.displacedPosition;
						v2.validateDisplacedPosition();
						Point3d p2 = v2.displacedPosition;
						
						position.set(
								p0.x * CREASE0 + (p1.x + p2.x) * CREASE1,
								p0.y * CREASE0 + (p1.y + p2.y) * CREASE1,
								p0.z * CREASE0 + (p1.z + p2.z) * CREASE1
						);
						break;
					case IRREGULAR:
						position.set(AbstractVertex.this.displacedPosition);
						break;
					default:
						assert false;	// should never get here
					}
					positionValid = true;
				}
			}
			
			public String toString() {
				return "v" + num + "(" + AbstractVertex.this + ")";
			}
		};
		return vertexPoint;
	}
	
	public double getLimitFactor() {
		switch (boundaryType) {
		case REGULAR:
			return vertexEdges.length / (double) (vertexEdges.length + 5);
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
	
	public AbstractVertex getVertexPoint() {
		return vertexPoint;
	}
	
	void validatePosition() {
		if (!positionValid) {
			// TODO
			positionValid = true;
		}
	}
	
	void validateDisplacedPosition() {
		if (!displacedPositionValid) {
			if (isDisplaced) {
				validateDisplacementMatrix();	// this also validates position
				displacementMatrix.transform(position, displacedPosition);
			} else {
				validatePosition();
				displacedPosition.set(position);
			}
			displacedPositionValid = true;
		}
	}
	
	void validateLimit() {
		if (!limitValid) {
			validatePosition();
			final int valence = vertexEdges.length;
			switch (boundaryType) {
			case REGULAR:
				final double limitPairWeight = 1.0 / (valence * (valence + 5));
				final double limitFaceWeight = 3.0 * limitPairWeight;
				final double limitCenterWeight = (valence + 1.0) / (valence + 5);
				
				final double[] tangentPairWeight = TANGENT_PAIR_WEIGHTS[valence];
				final double[] tangentFaceWeight = TANGENT_FACE_WEIGHTS[valence];
				
				double fx = 0, fy = 0, fz = 0;
				double px = 0, py = 0, pz = 0;
				double ux = 0, uy = 0, uz = 0;
				double vx = 0, vy = 0, vz = 0;
				for (int i = 0; i < valence; i++) {
					HalfEdge edge = vertexEdges[i];
			
					Face face = edge.getPairFace();
					face.validateMidpointPosition();
					Point3d faceMidpoint = edge.getPairFace().midpointPosition;
					fx += faceMidpoint.x;
					fy += faceMidpoint.y;
					fz += faceMidpoint.z;
					
					AbstractVertex pair = edge.getPairVertex();
					pair.validatePosition();
					Point3d edgePairPoint = edge.getPairVertex().position;
					px += edgePairPoint.x;
					py += edgePairPoint.y;
					pz += edgePairPoint.z;
					
					ux += faceMidpoint.x * tangentFaceWeight[i] + edgePairPoint.x * tangentPairWeight[i];
					uy += faceMidpoint.y * tangentFaceWeight[i] + edgePairPoint.y * tangentPairWeight[i];
					uz += faceMidpoint.z * tangentFaceWeight[i] + edgePairPoint.z * tangentPairWeight[i];
					
					int j = (i > 0) ? i - 1 : valence - 1;	
					vx += faceMidpoint.x * tangentFaceWeight[j] + edgePairPoint.x * tangentPairWeight[j];
					vy += faceMidpoint.y * tangentFaceWeight[j] + edgePairPoint.y * tangentPairWeight[j];
					vz += faceMidpoint.z * tangentFaceWeight[j] + edgePairPoint.z * tangentPairWeight[j];	
				}
				limit.set(
						fx * limitFaceWeight + px * limitPairWeight + position.x * limitCenterWeight,
						fy * limitFaceWeight + py * limitPairWeight + position.y * limitCenterWeight,
						fz * limitFaceWeight + pz * limitPairWeight + position.z * limitCenterWeight
				);
				uTangent.set(ux, uy, uz);
				vTangent.set(vx, vy, vz);			
				break;
			case BOUNDARY:
				AbstractVertex vertex0 = vertexEdges[0].getPairVertex();
				AbstractVertex vertex1 = vertexEdges[vertexEdges.length - 1].getPairVertex();
				vertex0.validatePosition();
				vertex1.validatePosition();
				Point3d p0 = vertex0.position;
				Point3d p1 = vertex1.position;
				limit.set(
						position.x * CREASE_LIMIT0 + (p0.x + p1.x) * CREASE_LIMIT1,
						position.y * CREASE_LIMIT0 + (p0.y + p1.y) * CREASE_LIMIT1,
						position.z * CREASE_LIMIT0 + (p0.z + p1.z) * CREASE_LIMIT1
				);
				uTangent.set(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
				vx = 0; vy = 0; vz = 0;
				for (int i = 1; i < vertexEdges.length - 2; i++) {
					AbstractVertex v = vertexEdges[i].getPairVertex();
					v.validatePosition();
					vx += v.position.x * BOUNDARY_TANGENT_WEIGHTS[valence][i];
					vy += v.position.y * BOUNDARY_TANGENT_WEIGHTS[valence][i];
					vz += v.position.z * BOUNDARY_TANGENT_WEIGHTS[valence][i];
				}
				double f = 1.0 / (vertexEdges.length - 2);
				vx *= f;
				vy *= f;
				vz *= f;
				vTangent.set(vx - position.x, vy - position.y, vz - position.z);
				break;
			case IRREGULAR:
				limit.set(position);
				ux = 0; uy = 0; uz = 0;
				vx = 0; vy = 0; vz = 0;
				
				for (int i = 0; i < valence; i++) {
					int next = i + 1;
					if (next == valence) {
						next = 0;
					}
					double uWeight = IRREGULAR_TANGENT_WEIGHTS[valence][i];
					double vWeight = IRREGULAR_TANGENT_WEIGHTS[valence][next];
					
					HalfEdge edge = vertexEdges[i];
					AbstractVertex pair = edge.getPairVertex();
					pair.validatePosition();
					Point3d edgePairPoint = edge.getPairVertex().position;
					
					ux += edgePairPoint.x * uWeight; uy += edgePairPoint.y * uWeight; uz += edgePairPoint.z * uWeight;
					vx += edgePairPoint.x * vWeight; vy += edgePairPoint.y * vWeight; vz += edgePairPoint.z * vWeight;
				}
				uTangent.set(ux, uy, uz);
				vTangent.set(vx, vy, vz);
				break;
			default:
				assert false;	// should never get here
			}
			
			normal.cross(uTangent, vTangent);
			normal.normalize();

			limitValid = true;
		}
	}
	
	void validateDisplacedLimit() {
		if (!displacedLimitValid) {
			if (isDisplaced) {
				validateDisplacedPosition();
				final int valence = vertexEdges.length;
				switch (boundaryType) {
				case REGULAR:
					final double limitPairWeight = 1.0 / (valence * (valence + 5));
					final double limitFaceWeight = 3.0 * limitPairWeight;
					final double limitCenterWeight = (valence + 1.0) / (valence + 5);
					
					final double[] tangentPairWeight = TANGENT_PAIR_WEIGHTS[valence];
					final double[] tangentFaceWeight = TANGENT_FACE_WEIGHTS[valence];
					
					double fx = 0, fy = 0, fz = 0;
					double px = 0, py = 0, pz = 0;
					double ux = 0, uy = 0, uz = 0;
					double vx = 0, vy = 0, vz = 0;
					for (int i = 0; i < valence; i++) {
						HalfEdge edge = vertexEdges[i];
				
						Face face = edge.getPairFace();
						face.validateDisplacedMidpointPosition();
						Point3d faceMidpoint = edge.getPairFace().displacedMidpointPosition;
						fx += faceMidpoint.x;
						fy += faceMidpoint.y;
						fz += faceMidpoint.z;
						
						AbstractVertex pair = edge.getPairVertex();
						pair.validateDisplacedPosition();
						Point3d edgePairPoint = edge.getPairVertex().displacedPosition;
						px += edgePairPoint.x;
						py += edgePairPoint.y;
						pz += edgePairPoint.z;
						
						ux += faceMidpoint.x * tangentFaceWeight[i] + edgePairPoint.x * tangentPairWeight[i];
						uy += faceMidpoint.y * tangentFaceWeight[i] + edgePairPoint.y * tangentPairWeight[i];
						uz += faceMidpoint.z * tangentFaceWeight[i] + edgePairPoint.z * tangentPairWeight[i];
						
						int j = (i > 0) ? i - 1 : valence - 1;	
						vx += faceMidpoint.x * tangentFaceWeight[j] + edgePairPoint.x * tangentPairWeight[j];
						vy += faceMidpoint.y * tangentFaceWeight[j] + edgePairPoint.y * tangentPairWeight[j];
						vz += faceMidpoint.z * tangentFaceWeight[j] + edgePairPoint.z * tangentPairWeight[j];	
					}
					limit.set(
							fx * limitFaceWeight + px * limitPairWeight + displacedPosition.x * limitCenterWeight,
							fy * limitFaceWeight + py * limitPairWeight + displacedPosition.y * limitCenterWeight,
							fz * limitFaceWeight + pz * limitPairWeight + displacedPosition.z * limitCenterWeight
					);
					displacedUTangent.set(ux, uy, uz);
					displacedVTangent.set(vx, vy, vz);		
					break;
				case BOUNDARY:
					AbstractVertex vertex0 = vertexEdges[0].getPairVertex();
					AbstractVertex vertex1 = vertexEdges[vertexEdges.length - 1].getPairVertex();
					vertex0.validateDisplacedPosition();
					vertex1.validateDisplacedPosition();
					Point3d p0 = vertex0.displacedPosition;
					Point3d p1 = vertex1.displacedPosition;
					limit.set(
							displacedPosition.x * CREASE_LIMIT0 + (p0.x + p1.x) * CREASE_LIMIT1,
							displacedPosition.y * CREASE_LIMIT0 + (p0.y + p1.y) * CREASE_LIMIT1,
							displacedPosition.z * CREASE_LIMIT0 + (p0.z + p1.z) * CREASE_LIMIT1
					);
					displacedUTangent.set(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
					vx = 0; vy = 0; vz = 0;
					double summaryWeight = 0;
					for (int i = 1; i < vertexEdges.length - 2; i++) {
						AbstractVertex v = vertexEdges[i].getPairVertex();
						v.validateDisplacedPosition();
						vx += v.displacedPosition.x * BOUNDARY_TANGENT_WEIGHTS[valence][i];
						vy += v.displacedPosition.y * BOUNDARY_TANGENT_WEIGHTS[valence][i];
						vz += v.displacedPosition.z * BOUNDARY_TANGENT_WEIGHTS[valence][i];
						summaryWeight += BOUNDARY_TANGENT_WEIGHTS[valence][i];
					}
					double f = 1.0 / summaryWeight;
					vx *= f;
					vy *= f;
					vz *= f;
					displacedVTangent.set(vx - displacedPosition.x, vy - displacedPosition.y, vz - displacedPosition.z);
					break;
				case IRREGULAR:
					displacedLimit.set(displacedPosition);
					ux = 0; uy = 0; uz = 0;
					vx = 0; vy = 0; vz = 0;
					
					for (int i = 0; i < valence; i++) {
						int next = i + 1;
						if (next == valence) {
							next = 0;
						}
						double uWeight = IRREGULAR_TANGENT_WEIGHTS[valence][i];
						double vWeight = IRREGULAR_TANGENT_WEIGHTS[valence][next];
						
						HalfEdge edge = vertexEdges[i];
						AbstractVertex pair = edge.getPairVertex();
						pair.validateDisplacedPosition();
						Point3d edgePairPoint = edge.getPairVertex().displacedPosition;
						
						ux += edgePairPoint.x * uWeight; uy += edgePairPoint.y * uWeight; uz += edgePairPoint.z * uWeight;
						vx += edgePairPoint.x * vWeight; vy += edgePairPoint.y * vWeight; vz += edgePairPoint.z * vWeight;
					}
					displacedUTangent.set(ux, uy, uz);
					displacedVTangent.set(vx, vy, vz);
					break;
				default:
					assert false;	// should never get here
				}	
				displacedNormal.cross(displacedUTangent, displacedVTangent);
				displacedNormal.normalize();
			}
		} else {
			validateLimit();
			displacedLimit.set(limit);
			displacedUTangent.set(uTangent);
			displacedVTangent.set(vTangent);
			displacedNormal.set(normal);
		}
		displacedLimitValid = true;
	}
	
	void validateDisplacementMatrix() {
		if (!displacementMatrixValid && isDisplaced) {
			validateLimit();
			double nLength = 0.5 * (uTangent.length() + vTangent.length());
			displacementMatrix.m00 = uTangent.x; displacementMatrix.m01 = vTangent.x; displacementMatrix.m02 = normal.x * nLength;
			displacementMatrix.m10 = uTangent.y; displacementMatrix.m11 = vTangent.y; displacementMatrix.m12 = normal.y * nLength;
			displacementMatrix.m20 = uTangent.z; displacementMatrix.m21 = vTangent.z; displacementMatrix.m22 = normal.z * nLength;
			displacementMatrixValid = true;
		}
	}
	
	void validateInvDisplacementMatrix() {
		if (!invDisplacementMatrixValid && isDisplaced) {
			validateDisplacementMatrix();
			invDisplacementMatrix.invert(displacementMatrix);
			invDisplacementMatrixValid = true;
		}
	}
	
	public void invalidate() {
		if (positionValid) {
			for (HalfEdge edge : vertexEdges) {
				if (edge.getFace() != null) {
					edge.getFace().invalidate();
				}
			}
			positionValid = false;
			displacedPositionValid = false;
			limitValid = false;
			displacedLimitValid = false;
			displacementMatrixValid = false;
			invDisplacementMatrixValid = false;
		}
	}
	
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
	
	void saveEdges(List<JPatchUndoableEdit> editList) {
		JPatchUndoableEdit edit = new SaveEdgesEdit();
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	private class SaveEdgesEdit extends AbstractSwapEdit {
		private HalfEdge[] edges = vertexEdges.clone();
		private int boundaryType = AbstractVertex.this.boundaryType;
		
		private SaveEdgesEdit() {
			apply(true);
		}
		
		@Override
		protected void swap() {
			HalfEdge[] tmpEdges = vertexEdges.clone();
			vertexEdges = edges;
			edges = tmpEdges;
			int tmpBoundaryType = AbstractVertex.this.boundaryType;
			AbstractVertex.this.boundaryType = boundaryType;
			boundaryType = tmpBoundaryType;
			invalidate();
		}
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
