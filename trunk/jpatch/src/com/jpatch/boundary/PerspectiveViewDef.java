package com.jpatch.boundary;

import com.jpatch.afw.attributes.DoubleAttr;
import com.jpatch.afw.attributes.Tuple2Attr;
import com.jpatch.afw.attributes.Tuple3Attr;
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
	}

	public Matrix4d getInverseMatrix(Matrix4d inverseMatrix) {
		return perspective.getTransform().getMatrix(inverseMatrix);
	}

	public Matrix4d getMatrix(Matrix4d matrix) {
		return perspective.getTransform().getInverseMatrix(matrix);
	}

	public double getRelativeFocalLength() {
		return perspective.getFocalLength() / filmApertureWidth;
	}
	
	public Point3d transform(Point3d p) {
		perspective.getTransform().invTransform(p);
		double w = -getRelativeFocalLength() * viewport.getComponent().getWidth() / p.z;
		p.x *= w;
		p.y *= w;
		return p;
	}
	
	public Point3d invTransform(Point3d p) {
		double w = -p.z / (getRelativeFocalLength() * viewport.getComponent().getWidth());
		p.x *= w;
		p.y *= w;
		perspective.getTransform().transform(p);
		return p;
	}
}
