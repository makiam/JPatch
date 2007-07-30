package com.jpatch.boundary;

import javax.vecmath.*;
import com.jpatch.afw.attributes.*;

public class OrthoViewDef extends AbstractViewDef {
	private final Matrix4d matrix = new Matrix4d();
	private final Matrix4d inverseMatrix = new Matrix4d();
	private final Tuple2Attr translationAttr = new Tuple2Attr(0.0, 0.0);
	private final Tuple2Attr rotationAttr = new Tuple2Attr();
	private final DoubleAttr scaleAttr = new DoubleAttr(1.0);
	
	public OrthoViewDef(Viewport viewport, OrthoView orthoView) {
		super(viewport);
		orthoView.updateViewdef(this);
	}
	
	public Tuple2Attr getTranslationAttribute() {
		return translationAttr;
	}
	
	public Tuple2Attr getRotationAttribute() {
		return rotationAttr;
	}
	
	public DoubleAttr getScaleAttribute() {
		return scaleAttr;
	}

	public Matrix4d getMatrix() {
		return matrix;
	}
	
	public Matrix4d getInverseMatrix() {
		return inverseMatrix;
	}
	
	public void computeMatrix() {
		double viewScale = scaleAttr.getDouble() / 20 * viewport.getComponent().getWidth();
		double x = Math.toRadians(rotationAttr.getX());
		double y = Math.toRadians(rotationAttr.getY());
		double sx = Math.sin(x);
		double cx = Math.cos(x);
		double sy = Math.sin(y);
		double cy = Math.cos(y);
		
		matrix.m00 = cy * viewScale;
		matrix.m01 = 0;
		matrix.m02 = -sy * viewScale;
		matrix.m03 = 0;
		matrix.m10 = sy * sx * viewScale;
		matrix.m11 = cx * viewScale;
		matrix.m12 = cy * sx * viewScale;
		matrix.m20 = sy * cx * viewScale;
		matrix.m21 = -sx * viewScale;
		matrix.m22 = cy * cx * viewScale;
		matrix.m03 = translationAttr.getX() * viewScale;
		matrix.m13 = translationAttr.getY() * viewScale;
		matrix.m23 = 0;
		
		inverseMatrix.invert(matrix);
	}
}
