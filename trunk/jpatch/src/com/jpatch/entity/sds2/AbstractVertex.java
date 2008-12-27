package com.jpatch.entity.sds2;

import static com.jpatch.entity.sds2.SdsWeights.*;

import java.util.*;

import com.jpatch.afw.control.*;
import com.jpatch.afw.testing.*;
import com.jpatch.entity.*;

import javax.vecmath.*;

public abstract class AbstractVertex implements Comparable<AbstractVertex> {
	static enum BoundaryType { REGULAR, BOUNDARY, IRREGULAR }
	
	
	
//	final Tuple3Attr positionAttr = new Tuple3Attr();
//	final DoubleAttr cornerSharpnessAttr = AttributeManager.getInstance().createBoundedDoubleAttr(0, 0, 100);
	
	final Point3d worldPosition = new Point3d();		// position in world space
	final Point3d worldLimit = new Point3d();			// limit in world space
	final Vector3d worldNormal = new Vector3d();		// normal in world space
	
//	final Tuple3Accumulator displacementAccumulator = new Tuple3Accumulator();
	
	Displacement displacement;
//	final Point3d displacedPosition = new Point3d();
//	final Point3d displacedLimit = new Point3d();
//	final Vector3d displacedNormal = new Vector3d();
//	
//	final Matrix3d displacementMatrix = new Matrix3d();
//	final Matrix3d invDisplacementMatrix = new Matrix3d();
	
//	final Vector3d morphDisplacementVector = new Vector3d();
//	final Vector3d transformedDisplacementVector = new Vector3d();
	
	HalfEdge[] vertexEdges = new HalfEdge[0];
	
	private DerivedVertex vertexPoint;
	BoundaryType boundaryType;
	
	boolean worldPositionValid;
//	boolean displacedPositionValid;
	
	boolean worldLimitValid;
//	boolean displacedLimitValid;
	
	
//	private boolean invDisplacementMatrixValid;
	double sharpnessValue;
	
	final Sds sds;
	
	VertexId vertexId;
	
	AbstractVertex(Sds sds) {
		this.sds = sds;
	}
	
//		positionAttr.suppressChangeNotification(true);
//		positionAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
//			public void attributeHasChanged(Attribute source) {
//				setPos(positionAttr.getX(), positionAttr.getY(), positionAttr.getZ());
//			}
//		});
//		positionAttr.suppressChangeNotification(false);
//		
//		cornerSharpnessAttr.suppressChangeNotification(true);
//		cornerSharpnessAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
//			public void attributeHasChanged(Attribute source) {
////				sharpnessValue = Math.tan(cornerSharpnessAttr.getDouble() * 0.005 * Math.PI);
//				sharpnessValue = Math.exp(cornerSharpnessAttr.getDouble() * 0.024) - 1;
//				worldPositionValid = true; // will be set to false by invalidate() - if true, invalidate would exit early.
//				invalidate();
//				System.out.println("sharpness=" + cornerSharpnessAttr.getDouble() + " " + sharpnessValue);
//			}
//		});
//		cornerSharpnessAttr.suppressChangeNotification(false);
//	}
	
//	public final Tuple3Attr getPositionAttribute() {
//		return positionAttr;
//	}
	
	public final void getPosition(Tuple3d position) {
		validateDisplacedPosition();
		position.set(displacement == null ? worldPosition : displacement.displacedPosition);
	}
	
	public final void getPosition(Tuple3f position) {
		validateDisplacedPosition();
		position.set(displacement == null ? worldPosition : displacement.displacedPosition);
	}
	
	public final void setPosition(Point3d p) {
		setPosition(p.x, p.y, p.z);
	}
	
	public Displacement getDisplacement() {
		return displacement;
	}
	
	public abstract void setPosition(double x, double y, double z);
	
//	public abstract void setPos(double x, double y, double z);
//	
//	public abstract void getPos(Tuple3d pos);
	
	
	
	public double getCornerSharpness() {
		return sharpnessValue;
	}
	
//	public final DoubleAttr getCornerSharpnessAttribute() {
//		return cornerSharpnessAttr;
//	}
	
	public final void getLimit(Tuple3f limit) {
		validateDisplacedLimit();
		limit.set(displacement == null ? worldLimit : displacement.displacedLimit);
	}
	
	public final void getLimit(Tuple3d limit) {
		validateDisplacedLimit();
		limit.set(displacement == null ? worldLimit : displacement.displacedLimit);
	}
	
	public final void getNormal(Tuple3f normal) {
		validateDisplacedLimit();
		normal.set(displacement == null ? worldNormal : displacement.displacedNormal);
	}
	
	public final void getNormal(Tuple3d normal) {
		validateDisplacedLimit();
		normal.set(displacement == null ? worldNormal : displacement.displacedNormal);
	}
	
	public final void disposeVertexPoint() {
		vertexPoint = null;
	}
	
	final Point3d getPos() {
		return displacement == null ? worldPosition : displacement.displacedPosition;
	}
	
	final Point3d getLimit() {
		return displacement == null ? worldLimit : displacement.displacedLimit;
	}
	
	final Vector3d getNormal() {
		return displacement == null ? worldNormal : displacement.displacedNormal;
	}
	
	public final DerivedVertex createVertexPoint() {
		assert vertexPoint == null;
		vertexPoint = new DerivedVertex(sds) {
		
			@Override
			public double getCornerSharpness() {
				AbstractVertex parentVertex = AbstractVertex.this;
				return Math.max(0, parentVertex.getCornerSharpness() - 1) + sharpnessValue;
			}
			
			@Override
			void validateWorldPosition() {
				if (!worldPositionValid) {
					AbstractVertex parentVertex = AbstractVertex.this;
					parentVertex.validateDisplacedPosition();
					double cornerSharpness = parentVertex.getCornerSharpness();
					if (cornerSharpness > 1 || parentVertex.boundaryType == BoundaryType.IRREGULAR) {
						worldPosition.set(parentVertex.getPos());
					} else {
						switch (parentVertex.boundaryType) {
						case REGULAR:
							final int valence = vertexEdges.length;
							final double rimWeight = VERTEX_LIMIT_RIM_WEIGHTS[valence];
							final double centerWeight = VERTEX_LIMIT_CENTER_WEIGHTS[valence];
//							final double rimWeight = 1.0/ valence;
//							final double centerWeight = (2 * valence - 3) / valence;
							double x = 0, y = 0, z = 0;
							for (HalfEdge edge : parentVertex.vertexEdges) {
								Face face = edge.getFace();
								face.validateDisplacedMidpointPosition();
								Point3d fp = face.displacedMidpointPosition;
								x += fp.x;
								y += fp.y;
								z += fp.z;
								AbstractVertex pair = edge.getPairVertex();
								pair.validateDisplacedPosition();
								Point3d ep = pair.getPos();
								x += ep.x;
								y += ep.y;
								z += ep.z;
							}
							worldPosition.set(
									x * rimWeight + parentVertex.getPos().x * centerWeight,
									y * rimWeight + parentVertex.getPos().y * centerWeight,
									z * rimWeight + parentVertex.getPos().z * centerWeight
							);
							break;
						case BOUNDARY:
							AbstractVertex v0 = parentVertex;
							AbstractVertex v1 = parentVertex.vertexEdges[0].getPairVertex();
							AbstractVertex v2 = parentVertex.vertexEdges[parentVertex.vertexEdges.length - 1].getPairVertex();
							Point3d p0 = v0.getPos();
							v1.validateDisplacedPosition();
							Point3d p1 = v1.getPos();
							v2.validateDisplacedPosition();
							Point3d p2 = v2.getPos();
							
							worldPosition.set(
									p0.x * CREASE0 + (p1.x + p2.x) * CREASE1,
									p0.y * CREASE0 + (p1.y + p2.y) * CREASE1,
									p0.z * CREASE0 + (p1.z + p2.z) * CREASE1
							);
							break;
						default:
							assert false;	// should never get here
						}
					}
					if (cornerSharpness > 0) {
						worldPosition.interpolate(parentVertex.getPos(), cornerSharpness);
						System.out.println("corner");
					}
					worldPositionValid = true;
				}
			}
			
//			@Override
//			void organizeEdges() {
//				AbstractVertex.this.organizeEdges();
//				final HalfEdge[] parentEdges = AbstractVertex.this.vertexEdges;
//				if (vertexEdges.length == parentEdges.length) {
//					for (int i = 0; i < parentEdges.length; i++) {
//						vertexEdges[i] = HalfEdge.getOrCreate(this, parentEdges[i].getOrCreateEdgePoint());
//					}
//					boundaryType = AbstractVertex.this.boundaryType;
//				} else {
//					boundaryType = BoundaryType.IRREGULAR;
//				}
//			}
		};
		vertexPoint.vertexId = new VertexId.VertexPointId(vertexId);
		createVertexPointEdges();
		return vertexPoint;
	}
	
	void createVertexPointEdges() {
		if (vertexPoint.vertexEdges == null || vertexPoint.vertexEdges.length != vertexEdges.length) {
			vertexPoint.vertexEdges = new HalfEdge[vertexEdges.length];
		}
		for (int i = 0; i < vertexEdges.length; i++) {
			vertexPoint.vertexEdges[i] = HalfEdge.getOrCreate(vertexPoint, vertexEdges[i].getOrCreateEdgePoint());
		}
		vertexPoint.boundaryType = boundaryType;
	}
	
	public final double getLimitFactor() {
		switch (boundaryType) {
		case REGULAR:
			return (vertexEdges.length) / (double) (vertexEdges.length + 5);
		case BOUNDARY:
			return CREASE_LIMIT0;
		case IRREGULAR:
			return 1.0;
		default:
			assert false;	// should never get here
		}
		return 0;
	}
	
	public final HalfEdge[] getEdges() {
		return vertexEdges;
	}
	
	public final DerivedVertex getVertexPoint() {
		return vertexPoint;
	}
	
	public final DerivedVertex getOrCreateVertexPoint() {
		return vertexPoint != null ? vertexPoint : createVertexPoint();
	}
	
	abstract void validateWorldPosition();
//		if (!worldPositionValid) {
//			worldPosition.set(worldPosition);
//			worldPositionValid = true;
//		}
//	}
	
	final void validateDisplacedPosition() {
		if (displacement != null && !displacement.displacedPositionValid) {
			if (displacement.isDisplaced()) {
				validateWorldLimit();	// this also validates position
				displacement.displacementMatrix.transform(displacement.displacementVector, displacement.transformedDisplacementVector);
				displacement.displacedPosition.add(worldPosition, displacement.transformedDisplacementVector);	
			} else {
				validateWorldPosition();
				displacement.displacedPosition.set(worldPosition);
			}
			displacement.displacedPositionValid = true;
		} else {
			validateWorldPosition();
		}
	}
	
//	public void invalidateDisplacedPosition() {
//		displacement.displacedPositionValid = false;
//		displacement.displacedLimitValid = false;
//	}
	
	final void validateWorldLimit() {
		if (!worldLimitValid) {
			validateWorldPosition();
			final int valence = vertexEdges.length;
			double ux = 0, uy = 0, uz = 0;	// u tangent
			double vx = 0, vy = 0, vz = 0;	// v tangent
			switch (boundaryType) {
			case REGULAR:
				final double div = 1.0 / (5 + valence);
				final double div2 = div / valence;
				final double limitCenterWeight = (valence - 1) * div;
				final double limitPairWeight = 2 * div2;
				final double limitFaceWeight = 4 * div2;
				
				final double[] uTangentPairWeight = U_TANGENT_PAIR_WEIGHTS[valence];
				final double[] uTangentFaceWeight = U_TANGENT_FACE_WEIGHTS[valence];
				final double[] vTangentPairWeight = V_TANGENT_PAIR_WEIGHTS[valence];
				final double[] vTangentFaceWeight = V_TANGENT_FACE_WEIGHTS[valence];
				
				double fx = 0, fy = 0, fz = 0;
				double px = 0, py = 0, pz = 0;
				for (int i = 0; i < valence; i++) {
					HalfEdge edge = vertexEdges[i];
			
					Face face = edge.getPairFace();
					face.validateMidpointPosition();
					Point3d faceMidpoint = face.midpointPosition;
					fx += faceMidpoint.x;
					fy += faceMidpoint.y;
					fz += faceMidpoint.z;
					
					AbstractVertex pair = edge.getPairVertex();
					pair.validateWorldPosition();
					Point3d edgePairPoint = edge.getPairVertex().worldPosition;
					px += edgePairPoint.x;
					py += edgePairPoint.y;
					pz += edgePairPoint.z;
					
					ux += faceMidpoint.x * uTangentFaceWeight[i] + edgePairPoint.x * uTangentPairWeight[i];
					uy += faceMidpoint.y * uTangentFaceWeight[i] + edgePairPoint.y * uTangentPairWeight[i];
					uz += faceMidpoint.z * uTangentFaceWeight[i] + edgePairPoint.z * uTangentPairWeight[i];
					
					vx += faceMidpoint.x * vTangentFaceWeight[i] + edgePairPoint.x * vTangentPairWeight[i];
					vy += faceMidpoint.y * vTangentFaceWeight[i] + edgePairPoint.y * vTangentPairWeight[i];
					vz += faceMidpoint.z * vTangentFaceWeight[i] + edgePairPoint.z * vTangentPairWeight[i];	
				}
				worldLimit.set(
						fx * limitFaceWeight + px * limitPairWeight + worldPosition.x * limitCenterWeight,
						fy * limitFaceWeight + py * limitPairWeight + worldPosition.y * limitCenterWeight,
						fz * limitFaceWeight + pz * limitPairWeight + worldPosition.z * limitCenterWeight
				);		
				break;
			case BOUNDARY:
				AbstractVertex vertex0 = vertexEdges[0].getPairVertex();
				AbstractVertex vertex1 = vertexEdges[vertexEdges.length - 1].getPairVertex();
				vertex0.validateWorldPosition();
				vertex1.validateWorldPosition();
				Point3d p0 = vertex0.worldPosition;
				Point3d p1 = vertex1.worldPosition;
				worldLimit.set(
						worldPosition.x * CREASE_LIMIT0 + (p0.x + p1.x) * CREASE_LIMIT1,
						worldPosition.y * CREASE_LIMIT0 + (p0.y + p1.y) * CREASE_LIMIT1,
						worldPosition.z * CREASE_LIMIT0 + (p0.z + p1.z) * CREASE_LIMIT1
				);
				ux = p0.x - p1.x;
				uy = p0.y - p1.y;
				uz = p0.z - p1.z;
				
				vx = 0; vy = 0; vz = 0;
				for (int i = 1; i < valence; i++) {
					HalfEdge edge = vertexEdges[i];
			
					Face face = edge.getPairFace();
					face.validateMidpointPosition();
					Point3d faceMidpoint = face.midpointPosition;
					
					AbstractVertex pair = edge.getPairVertex();
					pair.validateWorldPosition();
					Point3d edgePairPoint = edge.getPairVertex().worldPosition;
					
					vx += faceMidpoint.x;
					vy += faceMidpoint.y;
					vz += faceMidpoint.z;
					if (i < valence - 1) {
						vx += edgePairPoint.x;
						vy += edgePairPoint.y;
						vz += edgePairPoint.z;	
					}
				}
				double f = 1.0 / (valence * 2 - 3);
				vx = vx * f - worldLimit.x;
				vy = vy * f - worldLimit.y;
				vz = vz * f - worldLimit.z;
				break;
			case IRREGULAR:
				worldLimit.set(worldPosition);
				for (int i = 0; i < valence; i++) {
					int next = i + 1;
					if (next == valence) {
						next = 0;
					}
					double uWeight = IRREGULAR_TANGENT_WEIGHTS[valence][i];
					double vWeight = IRREGULAR_TANGENT_WEIGHTS[valence][next];
					
					HalfEdge edge = vertexEdges[i];
					AbstractVertex pair = edge.getPairVertex();
					pair.validateWorldPosition();
					Point3d edgePairPoint = edge.getPairVertex().worldPosition;
					
					ux += edgePairPoint.x * uWeight; uy += edgePairPoint.y * uWeight; uz += edgePairPoint.z * uWeight;
					vx += edgePairPoint.x * vWeight; vy += edgePairPoint.y * vWeight; vz += edgePairPoint.z * vWeight;
				}
				break;
			default:
				assert false;	// should never get here
			}
			
			double cornerSharpness = Math.max(0, getCornerSharpness() - 1);
			if (false && cornerSharpness > 0) {
				double alpha = 1 - Math.pow(getLimitFactor(), cornerSharpness);
				worldLimit.interpolate(worldPosition, alpha);
			}
			
			/* normal = u cross v */
			worldNormal.set(uy*vz - uz*vy, uz*vx - ux*vz, ux*vy - uy*vx);
			worldNormal.normalize();

			double uLength = Math.sqrt(ux * ux + uy * uy + uz * uz);
			double vLength = Math.sqrt(vx * vx + vy * vy + vz * vz);
			double nLength = 0.5 * (uLength + vLength);
			if (displacement != null) {
				Matrix3d displacementMatrix = displacement.displacementMatrix;
				displacementMatrix.m00 = ux; displacementMatrix.m01 = vx; displacementMatrix.m02 = worldNormal.x * nLength;
				displacementMatrix.m10 = uy; displacementMatrix.m11 = vy; displacementMatrix.m12 = worldNormal.y * nLength;
				displacementMatrix.m20 = uz; displacementMatrix.m21 = vz; displacementMatrix.m22 = worldNormal.z * nLength;
			}
			worldLimitValid = true;
		}
	}
	
	final void validateDisplacedLimit() {
		if (displacement != null && !displacement.displacedLimitValid) {
			validateDisplacedPosition();
			final int valence = vertexEdges.length;
			double ux = 0, uy = 0, uz = 0;	// u tangent
			double vx = 0, vy = 0, vz = 0;	// v tangent
			
			switch (boundaryType) {
			case REGULAR:
//				final double limitPairWeight = 1.0 / (valence * (valence + 5.0));
//				final double limitFaceWeight = 3.0 * limitPairWeight;
//				final double limitCenterWeight = (valence + 1.0) / (valence + 5.0);
				
				final double div = 1.0 / (5 + valence);
				final double div2 = div / valence;
				final double limitCenterWeight = (valence - 1) * div;
				final double limitPairWeight = 2 * div2;
				final double limitFaceWeight = 4 * div2;
					
				final double[] uTangentPairWeight = U_TANGENT_PAIR_WEIGHTS[valence];
				final double[] uTangentFaceWeight = U_TANGENT_FACE_WEIGHTS[valence];
				final double[] vTangentPairWeight = V_TANGENT_PAIR_WEIGHTS[valence];
				final double[] vTangentFaceWeight = V_TANGENT_FACE_WEIGHTS[valence];
				double fx = 0, fy = 0, fz = 0;
				double px = 0, py = 0, pz = 0;
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
					Point3d edgePairPoint = edge.getPairVertex().getPos();
					px += edgePairPoint.x;
					py += edgePairPoint.y;
					pz += edgePairPoint.z;
					
					ux += faceMidpoint.x * uTangentFaceWeight[i] + edgePairPoint.x * uTangentPairWeight[i];
					uy += faceMidpoint.y * uTangentFaceWeight[i] + edgePairPoint.y * uTangentPairWeight[i];
					uz += faceMidpoint.z * uTangentFaceWeight[i] + edgePairPoint.z * uTangentPairWeight[i];
					
					vx += faceMidpoint.x * vTangentFaceWeight[i] + edgePairPoint.x * vTangentPairWeight[i];
					vy += faceMidpoint.y * vTangentFaceWeight[i] + edgePairPoint.y * vTangentPairWeight[i];
					vz += faceMidpoint.z * vTangentFaceWeight[i] + edgePairPoint.z * vTangentPairWeight[i];	
				}
				displacement.displacedLimit.set(
						fx * limitFaceWeight + px * limitPairWeight + displacement.displacedPosition.x * limitCenterWeight,
						fy * limitFaceWeight + py * limitPairWeight + displacement.displacedPosition.y * limitCenterWeight,
						fz * limitFaceWeight + pz * limitPairWeight + displacement.displacedPosition.z * limitCenterWeight
				);		
				break;
			case BOUNDARY:
				AbstractVertex vertex0 = vertexEdges[0].getPairVertex();
				AbstractVertex vertex1 = vertexEdges[vertexEdges.length - 1].getPairVertex();
				vertex0.validateDisplacedPosition();
				vertex1.validateDisplacedPosition();
				Point3d p0 = vertex0.getPos();
				Point3d p1 = vertex1.getPos();
				displacement.displacedLimit.set(
						displacement.displacedPosition.x * CREASE_LIMIT0 + (p0.x + p1.x) * CREASE_LIMIT1,
						displacement.displacedPosition.y * CREASE_LIMIT0 + (p0.y + p1.y) * CREASE_LIMIT1,
						displacement.displacedPosition.z * CREASE_LIMIT0 + (p0.z + p1.z) * CREASE_LIMIT1
				);
				ux = p0.x - p1.x;
				uy = p0.y - p1.y;
				uz = p0.z - p1.z;
				
				vx = 0; vy = 0; vz = 0;
				for (int i = 1; i < valence; i++) {
					HalfEdge edge = vertexEdges[i];
			
					Face face = edge.getPairFace();
					if (face == null) {
						System.err.println(this + ": no pair face!!!");
						break; // FIXME
					}
					face.validateDisplacedMidpointPosition();
					Point3d faceMidpoint = face.displacedMidpointPosition;
					
					AbstractVertex pair = edge.getPairVertex();
					pair.validateDisplacedPosition();
					Point3d edgePairPoint = edge.getPairVertex().getPos();
					
					vx += faceMidpoint.x;
					vy += faceMidpoint.y;
					vz += faceMidpoint.z;
					if (i < valence - 1) {
						vx += edgePairPoint.x;
						vy += edgePairPoint.y;
						vz += edgePairPoint.z;	
					}
				}
				double f = 1.0 / (valence * 2 - 3);
				vx = vx * f - displacement.displacedLimit.x;
				vy = vy * f - displacement.displacedLimit.y;
				vz = vz * f - displacement.displacedLimit.z;
				break;
			case IRREGULAR:
				displacement.displacedLimit.set(getPos());
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
					Point3d edgePairPoint = edge.getPairVertex().getPos();
					
					ux += edgePairPoint.x * uWeight; uy += edgePairPoint.y * uWeight; uz += edgePairPoint.z * uWeight;
					vx += edgePairPoint.x * vWeight; vy += edgePairPoint.y * vWeight; vz += edgePairPoint.z * vWeight;
				}
				break;
			default:
				assert false;	// should never get here
			}
			
			double cornerSharpness = getCornerSharpness();
			if (cornerSharpness > 0) {
				if (cornerSharpness < 10) {
					/* FIXME: This is an ad-hoc approximation with no background and needs to be replaced by a correct solution */
					double alpha = Math.min(Math.pow(0.12 * cornerSharpness, 0.5), 1);
					displacement.displacedLimit.interpolate(displacement.displacedPosition, alpha);
				} else {
					displacement.displacedLimit.set(displacement.displacedPosition);
				}
			}
			
			displacement.displacedNormal.set(uy*vz - uz*vy, uz*vx - ux*vz, ux*vy - uy*vx);
			displacement.displacedNormal.normalize();
			displacement.displacedLimitValid = true;
		} else {
			validateWorldLimit();
		}
	}
	
	final void validateInvDisplacementMatrix() {
		if (displacement != null && !displacement.invDisplacementMatrixValid) {
			validateWorldLimit();
			displacement.invDisplacementMatrix.invert(displacement.displacementMatrix);
			displacement.invDisplacementMatrixValid = true;
		}
	}
	
	public void invalidateAll() {
		worldPositionValid = true; // will be invalidated by invalidate() method
		invalidate();
	}
	
//	public void invalidateEdgeOrder() {
//		edgeOrderValid = false;
//	}
	
	final void invalidate() {
		if (worldPositionValid) {
			worldPositionValid = false;
			for (HalfEdge edge : vertexEdges) {
				if (edge.getFace() != null) {
					edge.getFace().invalidate();
				}
			}
		}
		worldLimitValid = false;
		if (displacement != null) {
			System.out.println(this + " invalidate displacement");
			displacement.displacedPositionValid = false;
			displacement.displacedLimitValid = false;
			displacement.invDisplacementMatrixValid = false;
		}
	}
	
	
	
	final void saveEdges(List<JPatchUndoableEdit> editList) {
		JPatchUndoableEdit edit = new SaveEdgesEdit();
		if (editList != null) {
			editList.add(edit);
		}
	}
	
	private final class SaveEdgesEdit extends AbstractSwapEdit {
		private HalfEdge[] edges = vertexEdges.clone();
		private BoundaryType boundaryType = AbstractVertex.this.boundaryType;
		
		private SaveEdgesEdit() {
			apply(false);
//			System.out.print("    SaveEdgesEdit created: " + AbstractVertex.this + " edges are:");
//			dumpEdges();
//			System.out.println();
		}
		
		@Override
		protected void swap() {
//			System.out.print("      " + AbstractVertex.this + " was "); dumpEdges();
			HalfEdge[] tmpEdges = vertexEdges.clone();
			vertexEdges = edges;
			edges = tmpEdges;
			BoundaryType tmpBoundaryType = AbstractVertex.this.boundaryType;
			AbstractVertex.this.boundaryType = boundaryType;
			boundaryType = tmpBoundaryType;
			worldPositionValid = true;
			invalidate();
//			System.out.print(" is now "); dumpEdges();System.out.println();
		}
		
		private void dumpEdges() {
			for (HalfEdge e : vertexEdges) System.out.print(e + " ");
		}
	}
	
	
	void flip() {
		HalfEdge[] tmp = vertexEdges.clone();
		for (int i = 0; i < vertexEdges.length; i++) {
			vertexEdges[i] = tmp[vertexEdges.length - i - 1];
		}
	}
	
	public Face getPrimaryFace() {
		for (HalfEdge edge : vertexEdges) {
			Face face = edge.getFace();
			if (face != null) {
				return face;
			}
		}
		return null;
	}
	
	public class ChangePositionEdit extends AbstractSwapEdit {
		private final Tuple3d pos = new Point3d();
		public ChangePositionEdit(Tuple3d pos, boolean changeNow) {
			this.pos.set(pos);
			apply(changeNow);
		}
		
		@Override
		protected void swap() {
			double x = pos.x;
			double y = pos.y;
			double z = pos.z;
//			getPos(pos);
//			setPos(x, y, z);
		}
	}
	
	BoundaryType boundaryType() {
		return boundaryType;
	}
	
	@Override
	public String toString() {
		return vertexId.toString();
	}
	
	public int compareTo(AbstractVertex other) {
		return vertexId.compareTo(other.vertexId);
	}

	
	
	@TestSuit
	public static class Tests {
		private static final Material MATERIAL = new BasicMaterial(new Color3f(1, 1, 1));
		private final Sds sds = new Sds(null);
		private final SdsModel sdsModel = new SdsModel(sds);
		private final AbstractVertex[] v = new AbstractVertex[13];
//		private final AbstractVertex[] v1 = new AbstractVertex[13];
		
		public Tests() {
			for (int i = 0; i < v.length; i++) {
				v[i] = new BaseVertex(sdsModel);
			}
		}
		
//		@TestCase //FIXME
		public TestResult test() {
			
			
			makeFace(0, 1, 2);
			if (!checkBoundary(0, 1, 2)) {
				return TestResult.error("Bad edges: " + Arrays.toString(v[0].getEdges()));
			}
			makeFace(2, 3, 0);
			if (!checkBoundary(0, 1, 3)) {
				return TestResult.error("Bad edges: " + Arrays.toString(v[0].getEdges()));
			}
			makeFace(1, 0, 12);
			if (!checkBoundary(0, 12, 3)) {
				return TestResult.error("Bad edges: " + Arrays.toString(v[0].getEdges()));
			}
			makeFace(0, 10, 11);
			if (v[0].boundaryType != BoundaryType.IRREGULAR) {
				return TestResult.error("Bad boundary-Type: " + v[0].boundaryType);
			}
			makeFace(9, 10, 0);
			if (v[0].boundaryType != BoundaryType.IRREGULAR) {
				return TestResult.error("Bad boundary-Type: " + v[0].boundaryType);
			}
			makeFace(12, 0, 11);
			if (!checkBoundary(0, 9, 3)) {
				return TestResult.error("Bad edges: " + Arrays.toString(v[0].getEdges()));
			}
			makeFace(6, 0, 5);
			if (v[0].boundaryType != BoundaryType.IRREGULAR) {
				return TestResult.error("Bad boundary-Type: " + v[0].boundaryType);
			}
			makeFace(4, 5, 0);
			if (v[0].boundaryType != BoundaryType.IRREGULAR) {
				return TestResult.error("Bad boundary-Type: " + v[0].boundaryType);
			}
			makeFace(0, 3, 4);
			if (!checkBoundary(0, 9, 6)) {
				return TestResult.error("Bad edges: " + Arrays.toString(v[0].getEdges()));
			}
			makeFace(6, 7, 0);
			if (!checkBoundary(0, 9, 7)) {
				return TestResult.error("Bad edges: " + Arrays.toString(v[0].getEdges()));
			}
			makeFace(8, 9, 0);
			if (!checkBoundary(0, 8, 7)) {
				return TestResult.error("Bad edges: " + Arrays.toString(v[0].getEdges()));
			}
			makeFace(7, 8, 0);
			if (v[0].boundaryType != BoundaryType.REGULAR) {
				return TestResult.error("Bad boundary-Type: " + v[0].boundaryType);
			}
			
			return TestResult.success();
		}
		
		private boolean checkBoundary(int vertex, int first, int last) {
			HalfEdge[] edges = v[vertex].getEdges();
			HalfEdge[] subEdges = v[vertex].vertexPoint.getEdges();
			boolean boundaryOk = v[vertex].boundaryType == BoundaryType.BOUNDARY && v[vertex].vertexPoint.boundaryType() == BoundaryType.BOUNDARY;
			boolean edgesOk = edges[0].getPairVertex() == v[first] && edges[edges.length - 1].getPairVertex() == v[last];
			boolean subEdgesOk = subEdges[0].getPairVertex() == v[first].getEdges()[0].getEdgePoint() && subEdges[subEdges.length - 1].getPairVertex() == v[last].getEdges()[0].getEdgePoint();
			return boundaryOk && edgesOk;// && subEdgesOk;
		}
		
		private void makeFace(int ... indices) {
			AbstractVertex[] av = new AbstractVertex[indices.length];
			for (int i = 0; i < indices.length; i++) {
				av[i] = v[indices[i]];
			}
			sds.getOrCreateFace(av);
//			for (int i = 0; i < indices.length; i++) {
//				int index = indices[i];
//				if (index == 0) {
//					v1[index] = v[index].vertexPoint;
//				} else {
//					v1[index] = v[index].getEdges()[0].getEdgePoint();
//				}
//			}
		}
	}
}
