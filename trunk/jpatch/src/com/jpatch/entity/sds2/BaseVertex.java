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
	
	private final Tuple3Accumulator positionAccumulator = new Tuple3Accumulator(localPosition);
	
	public BaseVertex(SdsModel sdsModel) {
		this(sdsModel, 0, 0, 0);
	}
	
	public BaseVertex(SdsModel sdsModel, double x, double y, double z) {
		super(sdsModel.getSds());
		vertexId = new VertexId.BaseVertexId(this);
		this.xformNode = sdsModel;
		xformNode.addXFormListener(this);
		xformNode.getLocal2WorldTransform(transformMatrix);
		invTransformMatrix.invert(transformMatrix);
		invTransformMatrixValid = true;
		setPosition(x, y, z);
	}
	
	@Override
	public void setPosition(double x, double y, double z) {
		validateInvTransformMatrix();
		worldPosition.set(x, y, z);
		invTransformMatrix.transform(worldPosition, localPosition);
		final MorphTarget morphTarget = sds.getActiveMorphTarget();
		Tuple3d value = morphTarget.getValueFor(positionAccumulator, this);
		positionAccumulator.applyTo(value);
		worldPositionValid = true; // will be set to false by invalidate() - if true, invalidate would exit early.
		invalidate();
	}
	
//	@Override
//	public void getPos(Tuple3d pos) {
//		pos.set(worldPosition);
//	}
	
//	public void validateLocalPosition(Accumulator accumulator) {
//		localPosition.add(positionAccumulator, (Tuple3Accumulator) accumulator);
//		invalidate();
//	}

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
		xmlWriter.writeTuple(localPosition);
		xmlWriter.endElement();
//		if (cornerSharpnessAttr.getDouble() > 0) {
//			xmlWriter.startElement("cornersharpness");
//			xmlWriter.characters(Double.toString(cornerSharpnessAttr.getDouble()));
//			xmlWriter.endElement();
//		}
		xmlWriter.endElement();
	}

//	@Override
//	public String toString() {
//		return "v" + num + "{" + boundaryType() + "}";
//	}
}
