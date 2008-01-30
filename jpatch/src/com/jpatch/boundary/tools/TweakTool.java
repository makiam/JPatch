package com.jpatch.boundary.tools;

import static javax.media.opengl.GL.*;

import java.awt.*;
import java.awt.event.*;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.*;

import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class TweakTool implements VisibleTool {
	private static final int MAX_DISTANCE_SQ = 32 * 32;
	
	private TransformUtil transformUtil = new TransformUtil();
	private MouseMotionListener[] mouseMotionListeners;
	private MouseListener[] mouseListeners;
	private double[] modelView = new double[16];
	
	private static int STANDARD_SELECTION_TYPE = MouseSelector.Type.EDGE | MouseSelector.Type.FACE | MouseSelector.Type.VERTEX;
	private static int LIMIT_SELECTION_TYPE = MouseSelector.Type.LIMIT;
	
	private HitObject hitObject;
	
	public void draw(Viewport viewport) {
		// TODO Auto-generated method stub

	}

	public void registerListeners(Viewport[] viewports) {
		mouseListeners = new MouseListener[viewports.length];
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new TweakMouseListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
			mouseMotionListeners[i] = new TweakMouseMotionListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseMotionListener(mouseMotionListeners[i]);
		}
	}

	public void unregisterListeners(Viewport[] viewports) {
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseMotionListener(mouseMotionListeners[i]);
		}
	}

	private void highlightHitObject(ViewportGl viewport) {
//		System.out.println("hitObject = " + hitObject + " distance = " + Math.sqrt(hitObject.distanceSq));
		
		GLAutoDrawable glDrawable = (GLAutoDrawable) viewport.getComponent();
		glDrawable.getContext().makeCurrent();
		GL gl = glDrawable.getGL();
		viewport.validateScreenShotTexture();
		viewport.drawScreenShot(0, 0, glDrawable.getWidth(), glDrawable.getHeight(), 1.0f);
		viewport.spatialMode();
		viewport.getViewDef().configureTransformUtil(transformUtil);
		hitObject.node.getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
		transformUtil.getMatrix(TransformUtil.LOCAL, TransformUtil.CAMERA, modelView);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadMatrixd(modelView, 0);
		gl.glEnable(GL_BLEND);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glPointSize(6.0f);
		gl.glLineWidth(4.0f);
		gl.glColor4f(1, 1, 0, 1.0f);
		Point3d p = new Point3d();
		if (hitObject instanceof HitVertex) {
			HitVertex hitVertex = (HitVertex) hitObject;
			if (viewport.getViewDef().getShowControlMeshAttribute().getBoolean()) {
				hitVertex.vertex.getPosition(p);
			} else {
				hitVertex.vertex.getLimit(p);
			}
			gl.glBegin(GL_POINTS);
			gl.glVertex3d(p.x, p.y, p.z);
			gl.glEnd();
		} else if (hitObject instanceof HitEdge) {
			HitEdge hitEgde = (HitEdge) hitObject;
			gl.glBegin(GL_LINES);
			hitEgde.halfEdge.getVertex().getPosition(p);
			gl.glVertex3d(p.x, p.y, p.z);
			hitEgde.halfEdge.getPairVertex().getPosition(p);
			gl.glVertex3d(p.x, p.y, p.z);
			gl.glEnd();
		} else if (hitObject instanceof HitFace) {
			HitFace hitFace = (HitFace) hitObject;
			gl.glColor4f(1, 1, 0, 1.0f);
			for (int pass = 0; pass < 2; pass++) {
				switch (pass) {
				case 0:
					gl.glColor4f(1, 1, 0, 0.5f);
					gl.glBegin(GL_TRIANGLE_FAN);
					hitFace.face.getMidpointPosition(p);
					gl.glVertex3d(p.x, p.y, p.z);
					break;
				case 1:
					gl.glLineWidth(2.0f);
					gl.glColor4f(1, 1, 0, 1.0f);
					gl.glBegin(GL_LINE_LOOP);
				}
				for (HalfEdge edge : hitFace.face.getEdges()) {
					edge.getVertex().getPosition(p);
					gl.glVertex3d(p.x, p.y, p.z);
				}
				hitFace.face.getEdges()[0].getVertex().getPosition(p);
				gl.glVertex3d(p.x, p.y, p.z);
				gl.glEnd();
			}
		}
		gl.glPointSize(3.0f);
		gl.glLineWidth(1.0f);
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_DEPTH_TEST);
		glDrawable.swapBuffers();
		glDrawable.getContext().release();
	}
	
	private class TweakMouseListener extends MouseAdapter {
		final ViewportGl viewport;
		
		private TweakMouseListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent arg0) {
			if (hitObject == null) {
				return;
			}
			snapPointer(viewport);
			Selection selection = Main.getInstance().getSelection();
			if (hitObject instanceof MouseSelector.HitVertex) {
				selection.setVertex(((HitVertex) hitObject).vertex, null);
			} else if (hitObject instanceof MouseSelector.HitEdge) {
				selection.setEdge(((HitEdge) hitObject).halfEdge, null);
			} else if (hitObject instanceof MouseSelector.HitFace) {
				selection.setFace(((HitFace) hitObject).face, null);
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class TweakMouseMotionListener implements MouseMotionListener {
		final ViewportGl viewport;
		
		private TweakMouseMotionListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		public void mouseDragged(MouseEvent e) {

		}

		public void mouseMoved(MouseEvent e) {
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
			if (sdsModel != null) {
				int level = Globals.getInstance().getEditLevelAttribute().getInt();
				int selectionType = viewport.getViewDef().getShowControlMeshAttribute().getBoolean() ? STANDARD_SELECTION_TYPE : LIMIT_SELECTION_TYPE;
				hitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), Double.MAX_VALUE, sdsModel, level, selectionType);
				if (hitObject != null) {
					highlightHitObject(viewport);
				}
			}
		}	
	};
	
	private void snapPointer(Viewport viewport) {
		Point point = new Point(hitObject.screenX, hitObject.screenY);
		SwingUtilities.convertPointToScreen(point, viewport.getComponent());
		Main.getInstance().getRobot().mouseMove(point.x, point.y);
	}
}
