package com.jpatch.entity.sds2;

import com.jpatch.afw.*;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

public abstract class DerivedVertex extends AbstractVertex {
	
	DerivedVertex(Sds sds) {
		super(sds);
	}
	
	@Override
	public void setPosition(double x, double y, double z) {
		validateInvDisplacementMatrix();
		if (displacement == null) {
			setDisplacement(sds.createHierarchyModification(generateId()));
		}
		validateInvDisplacementMatrix();
		displacement.displacementVector.set(x - worldPosition.x, y - worldPosition.y, z - worldPosition.z);
		displacement.invDisplacementMatrix.transform(displacement.displacementVector);
		worldPositionValid = true; // will be set to false by invalidate() - if true, invalidate would exit early.
		invalidate();
	}
	
	public void discardDisplacement() {
		assert displacement != null;
		sds.discardHierarchicalVertexModification(displacement);
		displacement = null;
	}
	
	public void setDisplacement(Displacement displacement) {
		assert Arrays.equals(generateId(), displacement.hierarchyPath);
		this.displacement = displacement;
		worldLimitValid = false;
	}
	
//	@Override
//	public void setPos(double x, double y, double z) {
//		setDisplacement(x, y, z);
//	}
//	
//	@Override
//	public void getPos(Tuple3d pos) {
//		//TODO: implement
//	}
	void validateDisplacedPosition() {
		if (displacement != null && !displacement.displacedPositionValid) {
//			System.out.println(this + ".validateDisplacedPosition() called, displacedPositionValid=" + displacedPositionValid);
//			System.out.println("hierarchicalVertexModification=" + hierarchicalVertexModification);
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

	public int[] generateId() {
		for (HalfEdge edge : vertexEdges) {
			if (edge.getFace() != null) {
				int[] tmp = new int[SdsConstants.MAX_LEVEL + 2];
				tmp[0] = edge.getFaceEdgeIndex();
				int level = edge.getFace().addId(tmp, 1);
				int[] id = new int[level];
				System.arraycopy(tmp, 0, id, 0, level);
				return id;
			}
		}
		return null;
	}
	
	public String toString() {
		return "hv" + Arrays.toString(generateId());
	}
}
