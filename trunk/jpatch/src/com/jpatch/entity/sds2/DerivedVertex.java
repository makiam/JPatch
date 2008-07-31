package com.jpatch.entity.sds2;

import com.jpatch.afw.*;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

public abstract class DerivedVertex extends AbstractVertex {
	private HierarchicalVertexModification hierarchicalVertexModification;
	
	DerivedVertex(Sds sds) {
		super(sds);
	}
	
	@Override
	public void setPosition(double x, double y, double z) {
		validateInvDisplacementMatrix();
		if (hierarchicalVertexModification == null) {
			hierarchicalVertexModification = sds.createHierarchyModification(generateId());
		}
		validateInvDisplacementMatrix();
		hierarchicalVertexModification.displacementVector.set(x - worldPosition.x, y - worldPosition.y, z - worldPosition.z);
		invDisplacementMatrix.transform(hierarchicalVertexModification.displacementVector);
		worldPositionValid = true; // will be set to false by invalidate() - if true, invalidate would exit early.
		invalidate();
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
		if (!displacedPositionValid) {
			if (hierarchicalVertexModification != null && hierarchicalVertexModification.isDisplaced()) {
				validateWorldLimit();	// this also validates position
				displacementMatrix.transform(morphDisplacementVector, transformedDisplacementVector);
				displacedPosition.add(worldPosition, transformedDisplacementVector);	
			} else {
				validateWorldPosition();
				displacedPosition.set(worldPosition);
			}
			displacedPositionValid = true;
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
