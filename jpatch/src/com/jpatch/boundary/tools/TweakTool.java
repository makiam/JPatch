package com.jpatch.boundary.tools;

import static javax.media.opengl.GL.*;

import java.awt.event.*;

import javax.media.opengl.*;
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
	
	private int selectionType = MouseSelector.Type.EDGE | MouseSelector.Type.FACE | MouseSelector.Type.VERTEX;
	
	public void draw(Viewport viewport) {
		// TODO Auto-generated method stub

	}

	public void registerListeners(Viewport[] viewports) {
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseMotionListeners[i] = new TweakMouseMotionListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseMotionListener(mouseMotionListeners[i]);
		}
	}

	public void unregisterListeners(Viewport[] viewports) {
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseMotionListener(mouseMotionListeners[i]);
		}
	}

	private void highlightHitObject(ViewportGl viewport, HitObject hitObject) {
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
		gl.glColor4f(1, 1, 0, 0.5f);
		gl.glPointSize(10.0f);
		gl.glLineWidth(5.0f);
		Point3d p = new Point3d();
		if (hitObject instanceof HitVertex) {
			HitVertex hitVertex = (HitVertex) hitObject;
			hitVertex.vertex.getPosition(p);
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
			gl.glBegin(GL_TRIANGLE_FAN);
			hitFace.face.getMidpoint(p);
			gl.glVertex3d(p.x, p.y, p.z);
			for (HalfEdge edge : hitFace.face.getEdges()) {
				edge.getVertex().getPosition(p);
				gl.glVertex3d(p.x, p.y, p.z);
			}
			hitFace.face.getEdges()[0].getVertex().getPosition(p);
			gl.glVertex3d(p.x, p.y, p.z);
			gl.glEnd();
		}
		gl.glPointSize(3.0f);
		gl.glLineWidth(1.0f);
		gl.glDisable(GL_BLEND);
		gl.glEnable(GL_DEPTH_TEST);
		glDrawable.swapBuffers();
		glDrawable.getContext().release();
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
				HitObject hitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), Double.MAX_VALUE, sdsModel, level, selectionType);
				if (hitObject != null) {
					highlightHitObject(viewport, hitObject);
				}
			}
		}	
	};
}
