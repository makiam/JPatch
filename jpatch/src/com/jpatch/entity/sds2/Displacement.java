package com.jpatch.entity.sds2;

import com.jpatch.afw.*;
import com.jpatch.entity.*;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

public final class Displacement {
	double cornerSharpness;
	final int[] hierarchyPath;
	final int hashCode;
	
	final Point3d displacedPosition = new Point3d();
	final Point3d displacedLimit = new Point3d();
	final Vector3d displacedNormal = new Vector3d();
	
	final Matrix3d displacementMatrix = new Matrix3d();
	final Matrix3d invDisplacementMatrix = new Matrix3d();
	
	final Vector3d displacementVector = new Vector3d();
	final Vector3d transformedDisplacementVector = new Vector3d();
	
	final Tuple3Accumulator displacementAccumulator = new Tuple3Accumulator(displacementVector);
	
	boolean displacedPositionValid;
	boolean displacedLimitValid;
	boolean invDisplacementMatrixValid;
	
	public Displacement(int[] hierarchyPath) {
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
		if (o instanceof Displacement) {
			return Arrays.equals(hierarchyPath, ((Displacement) o).hierarchyPath);
		}
		return false;
	}
}
