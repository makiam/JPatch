package com.jpatch.boundary.tools;

import static javax.media.opengl.GL.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;

import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.actions.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class SelectTool implements VisibleTool {
	public static enum Type {
		RECTANGLE, LASSO, PROXIMITY;
		@Override
		public String toString() {
			return name().charAt(0) + name().substring(1).toLowerCase();
		}
	}
	
	public static enum Mode {
		VISIBLE, ALL, SMART;
		@Override
		public String toString() {
			return name().charAt(0) + name().substring(1).toLowerCase();
		}
	}
	
	private final StateMachine<Type> typeAttr = new StateMachine<Type>(Type.class, Type.RECTANGLE);
	private final StateMachine<Mode> modeAttr = new StateMachine<Mode>(Mode.class, Mode.SMART);
//	private final StateMachine<Type> modeAttr = new StateMachine<Type>(Type.class, Type.RECTANGLE);
	
	private MouseMotionListener[] mouseMotionListeners;
	private MouseListener[] mouseListeners;
	
	private final Polygon lassoPolygon = new Polygon();
	private final Collection<AbstractVertex> vertices = new ArrayList<AbstractVertex>();
	private boolean active;
	private boolean visibleOnly;
	
	public void draw(Viewport viewport) {
		// TODO Auto-generated method stub

	}

	public void registerListeners(Viewport[] viewports) {
//		Main.getInstance().getSelection().clear(null);
//		Main.getInstance().repaintViewports();
		mouseListeners = new MouseListener[viewports.length];
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new SelectMouseListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
			mouseMotionListeners[i] = new SelectMouseMotionListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseMotionListener(mouseMotionListeners[i]);
			ViewportGl viewportGl = (ViewportGl) viewports[i];
			GLAutoDrawable glDrawable = (GLAutoDrawable) viewportGl.getComponent();
			glDrawable.getContext().makeCurrent();
			if (glDrawable.getContext() == GLContext.getCurrent()) {
				viewportGl.validateScreenShotTexture();
				glDrawable.getContext().release();
			}
		}
	}

	public void unregisterListeners(Viewport[] viewports) {
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseListener(mouseListeners[i]);
			viewports[i].getComponent().removeMouseMotionListener(mouseMotionListeners[i]);
		}
	}

	public GenericAttr<Mode> getModeAttribute() {
		return modeAttr;
	}
	
	public GenericAttr<Type> getTypeAttribute() {
		return typeAttr;
	}
	
	private void highlightHitObject(ViewportGl viewport) {
		Selection selection = Main.getInstance().getSelection();
//		System.out.println(selection.getVertices());
//		if (true) return;
		GLAutoDrawable glDrawable = (GLAutoDrawable) viewport.getComponent();
		glDrawable.getContext().makeCurrent();
		GL gl = glDrawable.getGL();
		viewport.validateScreenShotTexture();
		viewport.drawScreenShot(0, 0, glDrawable.getWidth(), glDrawable.getHeight(), 1.0f);
		gl.glColor3f(1, 1, 0);
		gl.glLineWidth(1);
		gl.glBegin(GL_LINE_LOOP);
		for (int i = 0; i < lassoPolygon.npoints; i++) {
			gl.glVertex2i(lassoPolygon.xpoints[i], lassoPolygon.ypoints[i]);
		}
		gl.glEnd();
		viewport.spatialMode();
//		viewport.getViewDef().configureTransformUtil(transformUtil);
		viewport.setModelViewMatrix(selection.getNode());
		
		gl.glEnable(GL_BLEND);
		viewport.drawSelection(selection, new Color3f(1, 1, 0));
		gl.glDisable(GL_BLEND);
		gl.glFlush();
		glDrawable.swapBuffers();
		glDrawable.getContext().release();
	}
	
	private class SelectMouseListener extends MouseAdapter {
		final ViewportGl viewport;
		
		private SelectMouseListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				int mx = e.getX();
				int my = e.getY();
				switch (modeAttr.getValue()) {
				case ALL:
					visibleOnly = false;
					break;
				case VISIBLE:
					visibleOnly = true;
					break;
				case SMART:
					GLAutoDrawable glDrawable = (GLAutoDrawable) viewport.getComponent();
					glDrawable.getContext().makeCurrent();
					visibleOnly = viewport.getDepthAt(mx, my) > (-viewport.farClip - 1);
					System.out.println("depth=" + viewport.getDepthAt(mx, my));
					System.out.println("far=" + (-viewport.farClip - 1));
					glDrawable.getContext().release();
				}
				final SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
				final int level = sdsModel.getEditLevelAttribute().getInt();
				
				vertices.clear();
				Main.getInstance().getSelection().clear(null);
				switch(Main.getInstance().getActions().sdsModeSM.getValue()) {
				case EDGE_MODE:
					Main.getInstance().getSelection().setType(Selection.Type.EDGES, null);
					break;
				case VERTEX_MODE:
					Main.getInstance().getSelection().setType(Selection.Type.VERTICES, null);
					break;
				case FACE_MODE:
					Main.getInstance().getSelection().setType(Selection.Type.FACES, null);
					break;
				default:
					throw new RuntimeException(); // should never get here	
				}
				
				
				switch(typeAttr.getValue()) {
				case LASSO:
					lassoPolygon.npoints = 0;
//					lassoPolygon.xpoints = new int[0];
//					lassoPolygon.ypoints = new int[0];
					lassoPolygon.invalidate();
					break;
				case RECTANGLE:
					lassoPolygon.npoints = 4;
					lassoPolygon.xpoints = new int[] { mx, mx, mx, mx };
					lassoPolygon.ypoints = new int[] { my, my, my, my };
					lassoPolygon.invalidate();
					break;
				}
				updateSelection(viewport, mx, my, sdsModel, level);
				highlightHitObject(viewport);
				active = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
//				Selection selection = Main.getInstance().getSelection();
//				selection.getTransformable().end(new ArrayList<JPatchUndoableEdit>());
//				Main.getInstance().getSelection().clear(null);
//				Main.getInstance().repaintViewports();
				active = false;
			}
		}
		
	}
	
	private void updateSelection() {
		Selection selection = Main.getInstance().getSelection();
		Selection.Type selectionType = selection.getType();
		selection.clear(null);
		selection.addVertices(vertices, null);
		selection.setType(selectionType, null);
	}
	
	private void updateSelection(ViewportGl viewport, int mx, int my, SdsModel sdsModel, int level) {
		switch(typeAttr.getValue()) {
		case LASSO:
			lassoPolygon.addPoint(mx, my);
			lassoPolygon.invalidate();
			MouseSelector.getVerticesUnderLasso(viewport, lassoPolygon, sdsModel, level, visibleOnly, vertices);
			break;
		case RECTANGLE:
			lassoPolygon.xpoints[1] = lassoPolygon.xpoints[2] = mx;
			lassoPolygon.ypoints[3] = lassoPolygon.ypoints[2] = my;
			lassoPolygon.invalidate();
			MouseSelector.getVerticesUnderLasso(viewport, lassoPolygon, sdsModel, level, visibleOnly, vertices);
//			System.out.println(lassoPolygon);
//			System.out.println(vertices);
			break;
		case PROXIMITY:
			final int type;
			switch(Main.getInstance().getActions().sdsModeSM.getValue()) {
			case EDGE_MODE:
				type = MouseSelector.Type.EDGE;
				break;
			case VERTEX_MODE:
				type = MouseSelector.Type.VERTEX;
				break;
			case FACE_MODE:
				type = MouseSelector.Type.FACE;
				break;
			default:
				throw new RuntimeException(); // should never get here	
			}
			HitObject hitObject = MouseSelector.getObjectAt(viewport, mx, my, Double.MAX_VALUE, sdsModel, level, type);
			hitObject.getVertices(vertices);
			break;
		}
		updateSelection();
		highlightHitObject(viewport);
	}
	
	
	private class SelectMouseMotionListener extends MouseMotionAdapter {
		final ViewportGl viewport;
		
		
		private SelectMouseMotionListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (!active) {
				return;
			}
			final SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
			final int level = sdsModel.getEditLevelAttribute().getInt();
			
			int mx = e.getX();
			int my = e.getY();
			
			updateSelection(viewport, mx, my, sdsModel, level);
		}

//		public void mouseMoved(MouseEvent e) {
//			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
//			if (sdsModel != null) {
//				HitObject newHitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), Double.MAX_VALUE, sdsModel, level, selectionType);
//				if (newHitObject != null && !newHitObject.equals(hitObject)) {
//					hitObject = newHitObject;
//					updateSelection(hitSelection, hitObject);
//					highlightHitObject(viewport);
//				}
//			}
//		}	
	};
}
