package com.jpatch.boundary;

import com.jpatch.afw.attributes.DoubleAttr;
import com.jpatch.afw.attributes.Tuple2Attr;
import com.jpatch.afw.attributes.Tuple3Attr;
import com.jpatch.afw.vecmath.TransformUtil;
import com.jpatch.entity.Perspective;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import static com.jpatch.afw.vecmath.TransformUtil.*;

public class PerspectiveViewDef extends AbstractViewDef {
	/**
	 * Film aperture width. 36.0 gives 35mm file equivalent focal lengths
	 */
	private final double filmApertureWidth = 36.0;
	
	private final Perspective perspective;
	
	public PerspectiveViewDef(Viewport viewport, Perspective perspective) {
		super(viewport);
		this.perspective = perspective;
	}
	
	public Tuple3Attr getTranslationAttribute() {
		return perspective.getTranslationAttribute();
	}
	
	public Tuple3Attr getRotationAttribute() {
		return perspective.getRotationAttribute();
	}
	
	public DoubleAttr getFocalLengthAttribute() {
		return perspective.getFocalLengthAttribute();
	}
	
//	public void computeMatrix() {
//		perspective.getTransform().computeMatrix();
//		
////		Matrix4d matrix = perspective.getTransform().getMatrix(new Matrix4d());
////		
////		/* rotate the camera to look down the positive z axis */
////		matrix.m00 = -matrix.m00;
////		matrix.m01 = -matrix.m01;
////		matrix.m02 = -matrix.m02;
////		matrix.m20 = -matrix.m20;
////		matrix.m21 = -matrix.m21;
////		matrix.m22 = -matrix.m22;
////		
////		transformUtil.setCamera2World(matrix);
//	}

	public double getRelativeFocalLength() {
		return perspective.getFocalLength() / filmApertureWidth;
	}

	public void configureTransformUtil(TransformUtil transformUtil) {
		perspective.getLocal2WorldTransform(transformUtil, CAMERA);
		transformUtil.flipZAxis(CAMERA);
		transformUtil.setPerspectiveProjection(getRelativeFocalLength());
		transformUtil.setViewportDimension(viewport.getComponent().getWidth(), viewport.getComponent().getHeight());
		transformUtil.setCameraScale(1);
	}
}
