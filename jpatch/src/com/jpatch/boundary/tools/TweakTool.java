package com.jpatch.boundary.tools;

import static javax.media.opengl.GL.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.*;

import com.jpatch.afw.control.*;

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
	
	private static int STANDARD_SELECTION_TYPE = MouseSelector.Type.EDGE | MouseSelector.Type.FACE | MouseSelector.Type.VERTEX;
	private static int LIMIT_SELECTION_TYPE = MouseSelector.Type.LIMIT;
	
	private HitObject hitObject;
	private Selection hitSelection = new Selection();
	private boolean drag;
	private Point hitPoint;
	private Point3d localStart = new Point3d();
	
	private TextureUpdater textureUpdater;
	
	public void draw(Viewport viewport) {
		// TODO Auto-generated method stub

	}

	public void registerListeners(Viewport[] viewports) {
		Main.getInstance().getSelection().clear(null);
		Main.getInstance().repaintViewports();
		mouseListeners = new MouseListener[viewports.length];
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new TweakMouseListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
			mouseMotionListeners[i] = new TweakMouseMotionListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseMotionListener(mouseMotionListeners[i]);
		}
		textureUpdater = new TextureUpdater(viewports);
		textureUpdater.start();
	}

	public void unregisterListeners(Viewport[] viewports) {
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseListener(mouseListeners[i]);
			viewports[i].getComponent().removeMouseMotionListener(mouseMotionListeners[i]);
		}
		textureUpdater.stop();
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
		viewport.setModelViewMatrix(transformUtil);
		
//		gl.glEnable(GL_BLEND);
//		gl.glDisable(GL_DEPTH_TEST);
		
		viewport.drawSelection(hitSelection, new Color3f(1, 1, 0));
//		gl.glColor4f(1, 1, 0, 1.0f);
//		Point3d p = new Point3d();
//		if (hitObject instanceof HitVertex) {
//			HitVertex hitVertex = (HitVertex) hitObject;
//			if (viewport.getViewDef().getShowControlMeshAttribute().getBoolean()) {
//				hitVertex.vertex.getPosition(p);
//			} else {
//				hitVertex.vertex.getLimit(p);
//			}
//			gl.glPointSize(6.0f);
//			gl.glBegin(GL_POINTS);
//			gl.glVertex3d(p.x, p.y, p.z);
//			gl.glEnd();
//		} else if (hitObject instanceof HitEdge) {
//			HitEdge hitEgde = (HitEdge) hitObject;
//			gl.glLineWidth(3.0f);
//			gl.glBegin(GL_LINES);
//			hitEgde.halfEdge.getVertex().getPosition(p);
//			gl.glVertex3d(p.x, p.y, p.z);
//			hitEgde.halfEdge.getPairVertex().getPosition(p);
//			gl.glVertex3d(p.x, p.y, p.z);
//			gl.glEnd();
//			gl.glPointSize(4.0f);
//			gl.glBegin(GL_POINTS);
//			hitEgde.halfEdge.getVertex().getPosition(p);
//			gl.glVertex3d(p.x, p.y, p.z);
//			hitEgde.halfEdge.getPairVertex().getPosition(p);
//			gl.glVertex3d(p.x, p.y, p.z);
//			gl.glEnd();
//		} else if (hitObject instanceof HitFace) {
//			HitFace hitFace = (HitFace) hitObject;
//			gl.glColor4f(1, 1, 0, 1.0f);
//			for (int pass = 0; pass < 2; pass++) {
//				switch (pass) {
//				case 0:
//					gl.glColor4f(1, 1, 0, 0.5f);
//					gl.glBegin(GL_TRIANGLE_FAN);
//					hitFace.face.getMidpointPosition(p);
//					gl.glVertex3d(p.x, p.y, p.z);
//					break;
//				case 1:
//					gl.glLineWidth(2.0f);
//					gl.glColor4f(1, 1, 0, 1.0f);
//					gl.glBegin(GL_LINE_LOOP);
//				}
//				for (HalfEdge edge : hitFace.face.getEdges()) {
//					edge.getVertex().getPosition(p);
//					gl.glVertex3d(p.x, p.y, p.z);
//				}
//				hitFace.face.getEdges()[0].getVertex().getPosition(p);
//				gl.glVertex3d(p.x, p.y, p.z);
//				gl.glEnd();
//			}
//			gl.glPointSize(4.0f);
//			hitFace.face.getMidpointPosition(p);
//			gl.glBegin(GL_POINTS);
//			gl.glVertex3d(p.x, p.y, p.z);
//			gl.glEnd();
//		}
//		gl.glPointSize(3.0f);
//		gl.glLineWidth(1.0f);
//		gl.glDisable(GL_BLEND);
//		gl.glEnable(GL_DEPTH_TEST);
		glDrawable.swapBuffers();
		glDrawable.getContext().release();
	}
	
	private class TweakMouseListener extends MouseAdapter {
		final ViewportGl viewport;
		
		private TweakMouseListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (hitObject == null) {
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON1) {
				snapPointer(viewport);
				Selection selection = Main.getInstance().getSelection();
				updateSelection(selection, hitObject);
				selection.getTransformable().begin();
				drag = true;
				localStart.set(hitObject.screenPosition);
				transformUtil.projectFromScreen(TransformUtil.LOCAL, localStart, localStart);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				Selection selection = Main.getInstance().getSelection();
				selection.getTransformable().end(new ArrayList<JPatchUndoableEdit>());
				Main.getInstance().getSelection().clear(null);
				Main.getInstance().repaintViewports();
				drag = false;
			}
		}
		
	}
	
	private void updateSelection(Selection selection, HitObject hitObject) {
		if (hitObject instanceof MouseSelector.HitVertex) {
			selection.setVertex(((HitVertex) hitObject).vertex, null);
		} else if (hitObject instanceof MouseSelector.HitEdge) {
			selection.setEdge(((HitEdge) hitObject).halfEdge, null);
		} else if (hitObject instanceof MouseSelector.HitFace) {
			selection.setFace(((HitFace) hitObject).face, null);
		}
	}
	
	private class TweakMouseMotionListener implements MouseMotionListener {
		final ViewportGl viewport;
		Point3d mouse = new Point3d();
		Vector3d vector = new Vector3d();
		
		private TweakMouseMotionListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		public void mouseDragged(MouseEvent e) {
			if (drag) {
				if (hitPoint != null && hitPoint.x == e.getX() && hitPoint.y == e.getY()) {
					hitPoint = null;
					return;
				}
				mouse.set(e.getX(), e.getY(), hitObject.screenPosition.z);
				transformUtil.projectFromScreen(TransformUtil.LOCAL, mouse, mouse);
				vector.sub(mouse, localStart);
				Selection selection = Main.getInstance().getSelection();
				selection.getTransformable().translate(vector);
				Main.getInstance().syncRepaintViewport(viewport);
			}
		}

		public void mouseMoved(MouseEvent e) {
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
			if (sdsModel != null) {
				int level = sdsModel.getEditLevelAttribute().getInt();
				int selectionType = viewport.getViewDef().getShowControlMeshAttribute().getBoolean() ? STANDARD_SELECTION_TYPE : LIMIT_SELECTION_TYPE;
				HitObject newHitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), Double.MAX_VALUE, sdsModel, level, selectionType);
				if (newHitObject != null && !newHitObject.equals(hitObject)) {
					hitObject = newHitObject;
					updateSelection(hitSelection, hitObject);
					highlightHitObject(viewport);
				}
			}
		}	
	};
	
	private void snapPointer(Viewport viewport) {
		Point point = new Point((int) Math.round(hitObject.screenPosition.x), (int) Math.round(hitObject.screenPosition.y));
		hitPoint = new Point(point);
		SwingUtilities.convertPointToScreen(point, viewport.getComponent());
		Main.getInstance().getRobot().mouseMove(point.x, point.y);
	}
}
