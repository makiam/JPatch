package com.jpatch.boundary;

import com.jpatch.afw.attributes.*;
import com.jpatch.entity.Perspective;
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
	
//	final GenericAttr<String> nameAttr = new GenericAttr<String>();
	
	final GenericAttr<String> nameAttr = new GenericAttr<String>();
	final BooleanAttr showControlMeshAttr = new BooleanAttr(true);
	final BooleanAttr showLimitSurfaceAttr = new BooleanAttr(true);
	final BooleanAttr showProjectedMeshAttr = new BooleanAttr(false);
	final BooleanAttr antialiasAttr = new BooleanAttr(false);
	final StateMachine<ViewDirection> viewDirectionAttr;
	
	final int id;
	Component component;
//	Matrix4f modelView = new Matrix4f();
	double zPos;
	double fw;
	static final int maxSubdiv = 10;
	static final float nearClip = 1;
	static final float farClip = 1 << 15;
	static final RealtimeRendererSettings RENDERER_SETTINGS = Settings.getInstance().realtimeRenderer;
	boolean active = false;
	
	ViewDef viewDef;
	
	
	public Viewport(int id, ViewDirection direction, CollectionAttr<ViewDirection> orthoDirections, final JPatchInspector inspector) {
		this.id = id;
		nameAttr.setValue("Viewport " + id);
//		matrix.setIdentity();
		viewDirectionAttr = new StateMachine<ViewDirection>(orthoDirections, direction);
		viewDirectionAttr.getValue().bindViewport(this);
//		viewType.getValue().bindTo(this);
//		viewType.setObject(viewDirection);
//		viewType.addAttributeListener(updateAttributeListener);
		
		/* this will unbind the old ViewDirection when ViewType changes */
		viewDirectionAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<ViewDirection>() {
			@Override
			public ViewDirection attributeWillChange(ScalarAttribute source, ViewDirection value) {
				viewDirectionAttr.getValue().unbindViewport(Viewport.this);
				return value;
			}
		});
		
		/* this will bind the new ViewDirection when ViewType changes */
		viewDirectionAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				viewDirectionAttr.getValue().bindViewport(Viewport.this);
				inspector.setViewport(Viewport.this);
			}
		});
		
//		viewTyp
//		eAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
//			public void attributeHasChanged(Attribute source) {
//				viewTypeAttr.getValue().setViewport(Viewport.this);
//			}
//		});
		
	}

	


	public GenericAttr<String> getNameAttribute() {
		return nameAttr;
	}
	
	public BooleanAttr getShowControlMeshAttribute() {
		return showControlMeshAttr;
	}

	public BooleanAttr getShowLimitSurfaceAttribute() {
		return showLimitSurfaceAttr;
	}

	public BooleanAttr getShowProjectedMeshAttribute() {
		return showProjectedMeshAttr;
	}
	
	public BooleanAttr getAntialiasAttribute() {
		return antialiasAttr;
	}

	public StateMachine<ViewDirection> getViewDirectionAttribute() {
		return viewDirectionAttr;
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
		return "Viewport " + id;// + ": " + viewType.toString();
	}
	
	public abstract void draw();
	
//	public abstract void drawShape(com.jpatch.boundary.tools.Shape shape);
	
	
	public Component getComponent() {
		return component;
	}
	
//	public Point3d get3DPosition(float x, float y, Point3d p) {
//		x -= (component.getWidth() >> 1);
//		y = (component.getHeight() >> 1) - y;
//		p.x = x;
//		p.y = y;
//		inverseMatrix.transform(p);
//		return p;
//	}
//	
//	public Point get2DPosition(Point3d p3d, Point p2d) {
//		p2d.x = component.getWidth() / 2 + (int) (matrix.m00 * p3d.x + matrix.m01 * p3d.y + matrix.m02 * p3d.z + matrix.m03);
//		p2d.y = component.getHeight() / 2 - (int) (matrix.m10 * p3d.x + matrix.m11 * p3d.y + matrix.m12 * p3d.z + matrix.m13);
//		return p2d;
//	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	
	
//	


	public void setActive(boolean active) {
		this.active = active;
	}




	public ViewDef getViewDef() {
		return viewDef;
	}




	public void setViewDef(ViewDef viewDef) {
		this.viewDef = viewDef;
	}
	
}
