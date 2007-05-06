package com.jpatch.boundary;

import com.jpatch.afw.attributes.*;
import com.jpatch.settings.*;

import java.awt.*;
import java.awt.geom.Line2D;

import javax.vecmath.*;


public abstract class Viewport {
	public static final double MIN_DIST_SQ = 64;
	public ViewDirection[] standardViewDirections = new ViewDirection[] {
			OrthoViewDirection.FRONT,
			OrthoViewDirection.BACK,
			OrthoViewDirection.TOP,
			OrthoViewDirection.BOTTOM,
			OrthoViewDirection.LEFT,
			OrthoViewDirection.RIGHT,
			new OrthoViewDirection.BirdsEye()
	};
	
//	final ArrayAttr<ViewDirection> viewType = new ArrayAttr<ViewDirection>(standardViewDirections);
	final Tuple2 viewRotation = new Tuple2(0, 0);
	final Tuple2 viewTranslation = new Tuple2(0, 0);
	final DoubleAttr viewScale = new DoubleAttr(1);
	final BooleanAttr showControlMesh = new BooleanAttr(true);
	final BooleanAttr showLimitSurface = new BooleanAttr(true);
	final BooleanAttr showProjectedMesh = new BooleanAttr(true);
	final BooleanAttr fragmentShader = new BooleanAttr(false);
	
	final int id;
	Component component;
	Matrix4d matrix = new Matrix4d();
	Matrix4d inverseMatrix = new Matrix4d();
	Matrix4f modelView = new Matrix4f();
	double zPos;
	double fw;
	static final int maxSubdiv = 10;
	static final float nearClip = 1;
	static final float farClip = 1 << 15;
	static final RealtimeRendererSettings RENDERER_SETTINGS = Settings.getInstance().realtimeRenderer;
	
	protected ViewDirection viewDirection;
	
	private AttributeListener focalLengthAttributeListener = new AttributeAdapter() {
		public void attributeHasChanged(Attribute attribute) {
//			fw = (float) camera.focalLength.get() / 35 * component.getWidth();
		}
	};
	private AttributeListener updateAttributeListener = new AttributeAdapter() {
		public void attributeHasChanged(Attribute attribute) {
			if (
					attribute == viewTranslation.getXAttr() ||
					attribute == viewTranslation.getYAttr() ||
					attribute == viewRotation.getXAttr() ||
					attribute == viewRotation.getYAttr() ||
					attribute == viewScale
			) {
				computeMatrices();
//			} else if (attribute == viewType) {
//				viewDirection.unbind(Viewport.this);
//				viewDirection = viewType.getObject();
////				viewRotation.suppressChangeNotification(true);
//				viewDirection.bindTo(Viewport.this);
////				viewRotation.setValueAdjusting(false);
//				computeMatrices();
			}
			component.repaint();
		}
	};
	
	public Viewport(int id, int viewDir) {
		this.id = id;
		matrix.setIdentity();
		viewDirection = standardViewDirections[viewDir];
//		viewType.setObject(viewDirection);
		viewDirection.bindTo(this);
//		viewType.addAttributeListener(updateAttributeListener);
		showControlMesh.addAttributeListener(updateAttributeListener);
		showLimitSurface.addAttributeListener(updateAttributeListener);
		showProjectedMesh.addAttributeListener(updateAttributeListener);
		viewTranslation.getXAttr().addAttributeListener(updateAttributeListener);
		viewTranslation.getYAttr().addAttributeListener(updateAttributeListener);
		viewRotation.getXAttr().addAttributeListener(updateAttributeListener);
		viewRotation.getYAttr().addAttributeListener(updateAttributeListener);
		viewScale.addAttributeListener(updateAttributeListener);
//		name.set("Viewport " + id);
	}

	
	
	public BooleanAttr getShowControlMesh() {
		return showControlMesh;
	}

	public BooleanAttr getShowLimitSurface() {
		return showLimitSurface;
	}

	public BooleanAttr getShowProjectedMesh() {
		return showProjectedMesh;
	}

	public Tuple2 getViewRotation() {
		return viewRotation;
	}

	public DoubleAttr getViewScale() {
		return viewScale;
	}

	public Tuple2 getViewTranslation() {
		return viewTranslation;
	}

//	public ArrayAttr<ViewDirection> getViewType() {
//		return viewType;
//	}

	public BooleanAttr getFragmentShader() {
		return fragmentShader;
	}


	public String getName() {
		return "Viewport " + id;
	}


//	public void setParent(JPatchObject parent) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public ObjectRegistry getObjectRegistry() {
//		throw new UnsupportedOperationException();
//	}

	public String getInfo() {
		return "Viewport " + id + ": " + viewDirection;
	}
	
	public abstract void draw();
	
	public abstract void drawShape(com.jpatch.boundary.tools.Shape shape);
	
	public Matrix4d getMatrix() {
		return matrix;
	}
	
	public void setBirdsEyeView() {
		standardViewDirections[6].unbind(this);
//		viewType.setObject(standardViewDirections[6]);
	}
	
	public Component getComponent() {
		return component;
	}
	
	public Point3d get3DPosition(float x, float y, Point3d p) {
		x -= (component.getWidth() >> 1);
		y = (component.getHeight() >> 1) - y;
		p.x = x;
		p.y = y;
		inverseMatrix.transform(p);
		return p;
	}
	
	public Point get2DPosition(Point3d p3d, Point p2d) {
		p2d.x = component.getWidth() / 2 + (int) (matrix.m00 * p3d.x + matrix.m01 * p3d.y + matrix.m02 * p3d.z + matrix.m03);
		p2d.y = component.getHeight() / 2 - (int) (matrix.m10 * p3d.x + matrix.m11 * p3d.y + matrix.m12 * p3d.z + matrix.m13);
		return p2d;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	protected void computeMatrices() {
		double scale = viewScale.getDouble() / 20 * component.getWidth();
		double x = Math.toRadians(viewRotation.getX());
		double y = Math.toRadians(viewRotation.getY());
		double sx = Math.sin(x);
		double cx = Math.cos(x);
		double sy = Math.sin(y);
		double cy = Math.cos(y);
		
		matrix.m00 = cy * scale;
		matrix.m01 = 0;
		matrix.m02 = -sy * scale;
		matrix.m03 = 0;
		matrix.m10 = sy * sx * scale;
		matrix.m11 = cx * scale;
		matrix.m12 = cy * sx * scale;
		matrix.m20 = sy * cx * scale;
		matrix.m21 = -sx * scale;
		matrix.m22 = cy * cx * scale;
		matrix.m03 = viewTranslation.getX() * scale;
		matrix.m13 = viewTranslation.getY() * scale;
		matrix.m23 = 0;
		
		inverseMatrix.invert(matrix);
		modelView.set(matrix);
	}
	
	protected abstract void drawGrid();
	protected abstract void drawOrigin();
	
	protected abstract void drawInfo();
}
