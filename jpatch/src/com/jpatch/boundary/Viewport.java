package com.jpatch.boundary;

import com.jpatch.afw.attributes.*;
import com.jpatch.settings.*;
import com.sun.opengl.impl.GLWorkerThread;

import java.awt.*;
import java.awt.geom.Line2D;
import java.lang.reflect.InvocationTargetException;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLDrawable;
import javax.vecmath.*;


public abstract class Viewport implements NamedObject {
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
	
	final StateMachine<ViewDirection> viewType = new StateMachine<ViewDirection>(standardViewDirections, OrthoViewDirection.FRONT);
	final Tuple2Attr viewRotation = new Tuple2Attr(0, 0);
	final Tuple2Attr viewTranslation = new Tuple2Attr(0, 0);
	final DoubleAttr viewScale = new DoubleAttr(1);
	final Toggle showControlMesh = new Toggle(true);
	final Toggle showLimitSurface = new Toggle(true);
	final Toggle showProjectedMesh = new Toggle(true);
	final Toggle fragmentShader = new Toggle(false);
	final GenericAttr<String> nameAttr = new GenericAttr<String>();
	
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
	boolean active = false;
	
	private AttributePostChangeListener focalLengthAttributeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute attribute) {
//			fw = (float) camera.focalLength.get() / 35 * component.getWidth();
		}
	};
	private AttributePostChangeListener updateAttributeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute attribute) {
			if (getComponent() == null) {
				return;
			}
			if (
					attribute == viewTranslation ||
					attribute == viewRotation ||
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
//			getComponent().update(null);
//			getComponent().repaint();
			((GLAutoDrawable) getComponent()).display();
//			EventQueue.invokeLater(new Runnable() {
//				public void run() {
//					getComponent().paint(null);
//				}
//			});
			
//			((ViewportGl) Viewport.this).drawable.display();
//			((ViewportGl) Viewport.this).drawable.swapBuffers();
			
		}
	};
	
	public Viewport(int id, int viewDir) {
		this.id = id;
		matrix.setIdentity();
		viewType.setValue(standardViewDirections[viewDir]);
		viewType.getValue().bindTo(this);
//		viewType.setObject(viewDirection);
//		viewType.addAttributeListener(updateAttributeListener);
		showControlMesh.addAttributePostChangeListener(updateAttributeListener);
		showLimitSurface.addAttributePostChangeListener(updateAttributeListener);
		showProjectedMesh.addAttributePostChangeListener(updateAttributeListener);
		viewTranslation.addAttributePostChangeListener(updateAttributeListener);
		viewRotation.addAttributePostChangeListener(updateAttributeListener);
		viewScale.addAttributePostChangeListener(updateAttributeListener);
//		name.set("Viewport " + id);
		nameAttr.setValue("Viewport " + id);
	}

	
	
	public BooleanAttr getShowControlMeshAttribute() {
		return showControlMesh;
	}

	public BooleanAttr getShowLimitSurfaceAttribute() {
		return showLimitSurface;
	}

	public BooleanAttr getShowProjectedMeshAttribute() {
		return showProjectedMesh;
	}

	public Tuple2Attr getViewRotationAttribute() {
		return viewRotation;
	}

	public DoubleAttr getViewScaleAttribute() {
		return viewScale;
	}

	public Tuple2Attr getViewTranslationAttribute() {
		return viewTranslation;
	}

	public StateMachine<ViewDirection> getViewTypeAttribute() {
		return viewType;
	}

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
		return "Viewport " + id + ": " + viewType.toString();
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



	public void setActive(boolean active) {
		this.active = active;
	}
	
	public GenericAttr<String> getNameAttribute() {
		return nameAttr;
	}
}
