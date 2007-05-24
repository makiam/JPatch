package com.jpatch.entity;

import javax.vecmath.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.afw.attributes.*;

public class TransformNode extends AbstractTransformNode {
	private static final Matrix4d IDENTITY_MATRIX = Utils3d.createIdentityMatrix();
	private Vector3d translationTuple = new Vector3d();
	private Rotation3d rotationTuple = new Rotation3d();
	private Scale3d scaleTuple = new Scale3d();
	protected Tuple3Attr translationAttr = new Tuple3Attr();
	protected Tuple3Attr rotationAttr = new Tuple3Attr();
	protected Tuple3Attr scaleAttr = new Tuple3Attr();

	/**
	 * Computes the transformation matrix using the translation, rotation and scale attributes.
	 * This implementation sets the invInvalid flag to true.
	 */
	protected void computeMatrix() {
		if (parent != null) {
			parent.getMatrix(matrix);
		} else {
			matrix.set(IDENTITY_MATRIX);
		}
		translationAttr.getTuple(translationTuple);
		rotationAttr.getTuple(rotationTuple);
		scaleAttr.getTuple(scaleTuple);
		scaleTuple.scaleMatrix(matrix);
		rotationTuple.rotateMatrix(matrix);
		Utils3d.translateMatrix(matrix, translationTuple);
		invInvalid = true;
	}
}
