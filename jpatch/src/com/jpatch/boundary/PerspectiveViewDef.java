package com.jpatch.boundary;

import com.jpatch.afw.attributes.DoubleAttr;
import com.jpatch.afw.attributes.Tuple2Attr;
import com.jpatch.afw.attributes.Tuple3Attr;
import com.jpatch.afw.vecmath.TransformUtil;
import com.jpatch.entity.Perspective;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

public class PerspectiveViewDef extends AbstractViewDef {
	/**
	 * Film aperture width. 36.0 gives 35mm file equivalent focal lengths
	 */
	private final double filmApertureWidth = 36.0;
	
	private final Perspective perspective;
	
	private final TransformUtil transformUtil = new TransformUtil();
	
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
	
	public void computeMatrix() {
		perspective.getTransform().computeMatrix();
//		transformUtil.setCameraTransform(perspective.getTransform());
		transformUtil.setPerspectiveProjection(getRelativeFocalLength());
		Matrix4d matrix = perspective.getTransform().getMatrix(new Matrix4d());
		transformUtil.setWorld2Camera(matrix);
	}

	public double getRelativeFocalLength() {
		return perspective.getFocalLength() / filmApertureWidth;
	}

	public TransformUtil getTransformUtil() {
		return transformUtil;
	}
}
