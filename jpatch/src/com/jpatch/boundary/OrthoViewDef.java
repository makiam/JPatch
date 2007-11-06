package com.jpatch.boundary;

import javax.vecmath.*;

import com.jpatch.afw.Utils;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.vecmath.*;

public class OrthoViewDef extends AbstractViewDef {
	private final Matrix4d matrix = Utils3d.createIdentityMatrix4d();
	private final Tuple2Attr translationAttr = new Tuple2Attr(0.0, 0.0);
	private final Tuple2Attr rotationAttr = new Tuple2Attr();
	private final DoubleAttr scaleAttr = new DoubleAttr(1.0);
	private final TransformUtil transformUtil = new TransformUtil();
	
	public OrthoViewDef(Viewport viewport, OrthoViewParams orthoView) {
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
	
	public void computeMatrix() {
		int width = viewport.getComponent().getWidth();
		int height = viewport.getComponent().getHeight();
		double viewScale = scaleAttr.getDouble() / 20 * width;
		
		double sx = Utils3d.degSin(rotationAttr.getX());
		double cx = Utils3d.degCos(rotationAttr.getX());
		double sy = Utils3d.degSin(rotationAttr.getY());
		double cy = Utils3d.degCos(rotationAttr.getY());
		
		matrix.m00 = cy * viewScale;
		matrix.m01 = 0;
		matrix.m02 = sy * viewScale;
		matrix.m03 = translationAttr.getX() * viewScale;
		matrix.m10 = sx * sy * viewScale;
		matrix.m11 = cx * viewScale;
		matrix.m12 = -sx * cy * viewScale;
		matrix.m13 = translationAttr.getY() * viewScale;
		matrix.m20 = -cx * sy * viewScale;
		matrix.m21 = sx * viewScale;
		matrix.m22 = cx * cy * viewScale;
		matrix.m23 = 0;
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = 0;
		matrix.m33 = 1;
		
//		matrix.m00 = cy * viewScale;
//		matrix.m01 = 0;
//		matrix.m02 = -sy * viewScale;
//		matrix.m03 = 0;
//		matrix.m10 = sy * sx * viewScale;
//		matrix.m11 = cx * viewScale;
//		matrix.m12 = cy * sx * viewScale;
//		matrix.m20 = -sy * cx * viewScale;
//		matrix.m21 = sx * viewScale;
//		matrix.m22 = -cy * cx * viewScale;
//		matrix.m03 = translationAttr.getX() * viewScale;
//		matrix.m13 = translationAttr.getY() * viewScale;
//		matrix.m23 = 0;
//		matrix.m30 = 0;
//		matrix.m31 = 0;
//		matrix.m32 = 0;
//		matrix.m33 = 1;
		
//		System.out.println(matrix);
		transformUtil.setOrthographicProjection();
		transformUtil.setWorld2Space(TransformUtil.CAMERA, matrix);
		transformUtil.setCameraScale(viewScale);
		transformUtil.setViewportDimension(width, height);
	}

	public TransformUtil getTransformUtil() {
		return transformUtil;
	}

}
