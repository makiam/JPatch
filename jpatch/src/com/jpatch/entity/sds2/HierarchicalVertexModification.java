package com.jpatch.entity.sds2;

import com.jpatch.afw.*;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

public final class HierarchicalVertexModification {
	Vector3d displacementVector = new Vector3d();
	double cornerSharpness;
	final int[] hierarchyPath;
	final int hashCode;
	
	public HierarchicalVertexModification(int[] hierarchyPath) {
		this.hierarchyPath = hierarchyPath.clone();
		hashCode = Arrays.hashCode(hierarchyPath);
	}
	
	boolean isDisplaced() {
		return displacementVector.x != 0 || displacementVector.y != 0 || displacementVector.z != 0;
	}
	
	public void writeXml(XmlWriter xmlWriter, int[] hierarchyId) throws IOException {
		xmlWriter.startElement("hierarchyVertex");
		xmlWriter.startElement("hierarchyPath");
		xmlWriter.intArray(hierarchyId);
		xmlWriter.endElement();
		if (isDisplaced()) {
			xmlWriter.startElement("displacement");
			xmlWriter.writeTuple(displacementVector);
			xmlWriter.endElement();
		}
		if (cornerSharpness > 0) {
			xmlWriter.startElement("cornerSharpness");
			xmlWriter.characters(Double.toString(cornerSharpness));
			xmlWriter.endElement();
		}
		xmlWriter.endElement();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof HierarchicalVertexModification) {
			return Arrays.equals(hierarchyPath, ((HierarchicalVertexModification) o).hierarchyPath);
		}
		return false;
	}
}
