package com.jpatch.entity.sds2;

import java.io.*;

import javax.vecmath.*;

import com.jpatch.afw.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.entity.*;

public class BaseVertex extends AbstractVertex implements XFormListener {
	private XFormNode xformNode;
	private final Matrix4d transformMatrix = new Matrix4d();
	private final Matrix4d invTransformMatrix = new Matrix4d();
	private boolean transformMatrixValid;
	private boolean invTransformMatrixValid;
	private final Point3d localPosition = new Point3d();
	private static int count;
	final int num = count++;
	
	public BaseVertex(XFormNode node) {
		this(node, 0, 0, 0);
	}
	
	public BaseVertex(XFormNode node, double x, double y, double z) {
		if (node == null) {
			throw new NullPointerException();
		}
		this.xformNode = node;
		xformNode.addXFormListener(this);
		xformNode.getLocal2WorldTransform(transformMatrix);
		invTransformMatrix.invert(transformMatrix);
		invTransformMatrixValid = true;
		positionAttr.setTuple(x, y, z);
		
		cornerSharpnessAttr.setDouble(0);
	}
	
	@Override
	void setPos(double x, double y, double z) {
		validateInvTransformMatrix();
		worldPosition.set(x, y, z);
		invTransformMatrix.transform(worldPosition, localPosition);
		worldPositionValid = true;
		invalidate();
	}
	
	void validateWorldPosition() {
		if (!transformMatrixValid) {
			xformNode.getLocal2WorldTransform(transformMatrix);
			transformMatrixValid = true;
			invTransformMatrixValid = false;
			transformMatrix.transform(localPosition, worldPosition);
			worldPositionValid = true;
		} else if (!worldPositionValid) {
			transformMatrix.transform(localPosition, worldPosition);
			worldPositionValid = true;
		}
	}
	
	private void validateInvTransformMatrix() {
		if (!transformMatrixValid) {
			xformNode.getLocal2WorldTransform(transformMatrix);
			invTransformMatrix.invert(transformMatrix);
			transformMatrixValid = true;
			invTransformMatrixValid = true;
		} else if (!invTransformMatrixValid) {
			invTransformMatrix.invert(transformMatrix);
			invTransformMatrixValid = true;
		}
	}

	public void invalidateTransformation() {
		transformMatrixValid = false;
		invalidate();
	}
	
	public void writeXml(XmlWriter xmlWriter) throws IOException {
		xmlWriter.startElement("vertex");
		xmlWriter.startElement("id");
		xmlWriter.characters(Integer.toString(num));
		xmlWriter.endElement();
		xmlWriter.startElement("position");
		xmlWriter.characters(Double.toString(positionAttr.getX()));
		xmlWriter.characters(" ");
		xmlWriter.characters(Double.toString(positionAttr.getY()));
		xmlWriter.characters(" ");
		xmlWriter.characters(Double.toString(positionAttr.getZ()));
		xmlWriter.endElement();
		if (cornerSharpnessAttr.getDouble() > 0) {
			xmlWriter.startElement("cornersharpness");
			xmlWriter.characters(Double.toString(cornerSharpnessAttr.getDouble()));
			xmlWriter.endElement();
		}
		xmlWriter.endElement();
	}

	@Override
	public String toString() {
		return "v" + num;
	}
}
