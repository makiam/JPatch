package com.jpatch.entity.sds2;

import com.jpatch.afw.*;
import com.jpatch.entity.*;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

public abstract class DerivedVertex extends AbstractVertex {
	
	DerivedVertex(Sds sds) {
		super(sds);
	}
	
	@Override
	public void setPosition(double x, double y, double z) {
		if (displacement == null) {
			setDisplacement(sds.createHierarchyModification(generateId()));
		}
		validateInvDisplacementMatrix();
		displacement.transformedDisplacementVector.set(x - worldPosition.x, y - worldPosition.y, z - worldPosition.z);
		displacement.invDisplacementMatrix.transform(displacement.transformedDisplacementVector, displacement.displacementVector);
		System.out.println("local displacement=" + displacement.displacementVector);
		final MorphTarget morphTarget = sds.getActiveMorphTarget();
		Tuple3Accumulator accumulatorValue = (Tuple3Accumulator) morphTarget.getAccumulatorValueFor(displacement.displacementAccumulator, this);
		accumulatorValue.asTuple().sub(displacement.displacementVector, displacement.displacementAccumulator.asTuple());
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
