package com.jpatch.entity.sds2;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

import com.jpatch.afw.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.AbstractVertex.*;

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
		vertexEdges = new HalfEdge[0];
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

	
	
	/**
	 * This method must be called whenever a face adjacent to this vertex was created or destroyed.
	 * It will sort the edge-array, depending on the type of this vertex:
	 * <ul>
	 * <li>Regular: edge[n + 1].getPrev().getPair() == edge[n]. If possible, old start-edge is used.</li>
	 * <li>Boundary: edge[n + 1].getPrev().getPair() == edge[n] for all but the last edge
	 * <li>Irregular: No particular order
	 * </ul>
	 */
	void organizeEdges() {
		boolean debug = true;
		if (debug) System.out.println(this + " organizeEdges() called...");
		
		if (debug) System.out.println("    edges are: " + Arrays.toString(vertexEdges));
		
		if (vertexEdges.length == 0) {
			return;
		}
		final HalfEdge[] tmp = vertexEdges.clone();
		HalfEdge e = tmp[0];
		System.out.println("e=" + e + ", pairNext=" + e.getPair().getNext() + ", entering loop");
		while(e.getPair().getNext() != null && e.getPair().getNext() != tmp[0]) {
			e = e.getPair().getNext();
			System.out.println("e=" + e + ", pairNext=" + e.getPair().getNext());
		}
		System.out.println("loop done");
		
		if (e.getPair().getNext() != null) {
			boundaryType = BoundaryType.REGULAR;	// regular vertex
			Arrays.sort(tmp);
			e = tmp[0];
		} else {
			boundaryType = BoundaryType.BOUNDARY;	// regular boundary vertex (corner)
		}
		
		
		
		for (int i = 0; i < vertexEdges.length; i++) {
			if (e.getPrev() != null) {
				System.out.println("e=" + e + ", prevPair=" + e.getPrev().getPair());
			} else {
				System.out.println("e=" + e + ", prev=null");
			}
			if (i < vertexEdges.length - 1 && e.getPrev() == null) {
				System.arraycopy(tmp, 0, vertexEdges, 0, vertexEdges.length);
				boundaryType = BoundaryType.IRREGULAR; // irregular boundary vertex, crease edges are edges[0] and edges[edges.length - 1]
				break;
			}
			vertexEdges[i] = e;
			if (i < vertexEdges.length - 1) {
				e = e.getPrev().getPair();
			}
		}
		
		if (debug) System.out.print("    edges are:");
		if (debug) for (HalfEdge ed : vertexEdges) System.out.print(" " + ed);
		if (debug) System.out.println();
		if (debug) System.out.println("    boundaryType = " + boundaryType);
		
		if (vertexPoint != null && vertexPoint.vertexEdges != null) {
		// TODO ??	vertexPoint.organizeEdges();
		}
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
