package com.jpatch.entity.sds2;

import com.jpatch.afw.*;

import java.io.*;

public class HierarchicalVertexModification {
	private double dx, dy, dz;
	private double cornerSharpness;
	
	public HierarchicalVertexModification(double dx, double dy, double dz, double cornerSharpness) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		this.cornerSharpness = cornerSharpness;
	}
	
	public void applyTo(DerivedVertex vertex) {
		vertex.setDisplacement(dx, dy, dz);
		vertex.getCornerSharpnessAttribute().setDouble(cornerSharpness);
	}
	
	public void writeXml(XmlWriter xmlWriter, int[] hierarchyId) throws IOException {
		xmlWriter.startElement("hierarchyvertex");
		xmlWriter.startElement("hierarchy");
		xmlWriter.intArray(hierarchyId);
		xmlWriter.endElement();
		if (dx != 0 || dy != 0 || dz != 0) {
			xmlWriter.startElement("displacement");
			xmlWriter.characters(Double.toString(dx));
			xmlWriter.characters(" ");
			xmlWriter.characters(Double.toString(dy));
			xmlWriter.characters(" ");
			xmlWriter.characters(Double.toString(dz));
			xmlWriter.endElement();
		}
		if (cornerSharpness > 0) {
			xmlWriter.startElement("cornersharpness");
			xmlWriter.characters(Double.toString(cornerSharpness));
			xmlWriter.endElement();
		}
		xmlWriter.endElement();
	}
}
