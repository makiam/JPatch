package com.jpatch.entity.sds2;

import com.jpatch.afw.*;

import java.io.*;
import java.util.*;

public abstract class DerivedVertex extends AbstractVertex {
	
	@Override
	public void setPosition(double x, double y, double z) {
		validateInvDisplacementMatrix();
		displacementVector.set(x - worldPosition.x, y - worldPosition.y, z - worldPosition.z);
//		invDisplacementMatrix.transform(displacementVector);
		positionAttr.setTuple(displacementVector);
	}
	
	@Override
	public void setPos(double x, double y, double z) {
		setDisplacement(x, y, z);
	}
	
	
	public void writeXml(XmlWriter xmlWriter) throws IOException {
		if (isDisplaced || cornerSharpnessAttr.getDouble() != 0) {
			xmlWriter.startElement("hierarchyvertex");
			xmlWriter.startElement("hierarchy");
			xmlWriter.intArray(generateId());
			xmlWriter.endElement();
			if (isDisplaced) {
				xmlWriter.startElement("displacement");
				xmlWriter.characters(Double.toString(positionAttr.getX()));
				xmlWriter.characters(" ");
				xmlWriter.characters(Double.toString(positionAttr.getY()));
				xmlWriter.characters(" ");
				xmlWriter.characters(Double.toString(positionAttr.getZ()));
				xmlWriter.endElement();
			}
			if (cornerSharpnessAttr.getDouble() > 0) {
				xmlWriter.startElement("cornersharpness");
				xmlWriter.characters(Double.toString(cornerSharpnessAttr.getDouble()));
				xmlWriter.endElement();
			}
			xmlWriter.endElement();
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
