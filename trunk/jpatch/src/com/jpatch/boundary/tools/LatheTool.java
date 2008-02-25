package com.jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;

import javax.vecmath.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class LatheTool implements VisibleTool {
	private static final GlMaterial latheMaterial = new GlMaterial(
		new Color4f(0, 0, 0, 0),
		new Color4f(0.5f, 0.5f, 1.0f, 0.25f),
		new Color4f(0, 0, 0, 0),
		new Color4f(0, 0, 0, 0),
		10
	);
	
	private MouseMotionListener[] mouseMotionListeners;
	private MouseListener[] mouseListeners;
	
	private final Point3d axisStart = new Point3d(0, 0, 0);
	private final Point3d axisEnd = new Point3d(0, 1, 0);
	
	private final Tuple3Attr axisStartAttr = new Tuple3Attr(axisStart);
	private final Tuple3Attr axisEndAttr = new Tuple3Attr(axisEnd);
	private final DoubleAttr epsilonAttr = new DoubleAttr(0.01);
	private final IntAttr segmentsAttr = new IntAttr(8);
	private final DoubleAttr angleAttr = new DoubleAttr(360);
	
	private HalfEdge lastStartEdge;
	private HalfEdge startEdge;
	private BaseVertex[] chain;
	private Point3d[][] lathedPoints = new Point3d[0][0];
	
	private int mouseX, mouseY;
	private boolean latheValid;
	
	private final AttributePostChangeListener latheInvalidationListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			latheValid = false;
		}
	};
	
	public LatheTool() {
		axisStartAttr.addAttributePostChangeListener(latheInvalidationListener);
		axisEndAttr.addAttributePostChangeListener(latheInvalidationListener);
		epsilonAttr.addAttributePostChangeListener(latheInvalidationListener);
		segmentsAttr.addAttributePostChangeListener(latheInvalidationListener);
		angleAttr.addAttributePostChangeListener(latheInvalidationListener);
	}
	
	public Tuple3Attr getAxisStartAttribute() {
		return axisStartAttr;
	}
	
	public Tuple3Attr getAxisEndAttribute() {
		return axisEndAttr;
	}
	
	public DoubleAttr getEpsilonAttribute() {
		return epsilonAttr;
	}
	
	public IntAttr getSegmentsAttribute() {
		return segmentsAttr;
	}
	
	public DoubleAttr getAngleAttribute() {
		return angleAttr;
	}
	
	public void draw(Viewport viewport) {
		
	}

	public void registerListeners(Viewport[] viewports) {
		mouseListeners = new MouseListener[viewports.length];
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new LatheMouseListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
			mouseMotionListeners[i] = new LatheMouseMotionListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseMotionListener(mouseMotionListeners[i]);
		}
	}

	public void unregisterListeners(Viewport[] viewports) {
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseListener(mouseListeners[i]);
			viewports[i].getComponent().removeMouseMotionListener(mouseMotionListeners[i]);
		}
	}
	
	private void computeLathedVertices() {
		boolean reuse = true;
		if (lathedPoints.length != segmentsAttr.getInt()) {
			lathedPoints = new Point3d[segmentsAttr.getInt()][chain.length];
			reuse = false;
		}
		if (lathedPoints[0].length != chain.length) {
			lathedPoints = new Point3d[segmentsAttr.getInt()][chain.length];
			reuse = false;
		}
		if (!reuse) {
			for (Point3d[] p : lathedPoints) {
				for (int i = 0; i < p.length; i++) {
					p[i] = new Point3d();
				}
			}
		}
		if (lastStartEdge != startEdge || !reuse || !latheValid) {
			Operations.getLathedVertices(
					Main.getInstance().getSelection().getSdsModel().getSds(),
					chain,
					axisStart,
					axisEnd,
					epsilonAttr.getDouble(),
					segmentsAttr.getInt(),
					angleAttr.getDouble(),
					lathedPoints
				);
			lastStartEdge = startEdge;
			latheValid = true;
		}
	}
	
	private void highlight(ViewportGl viewport) {
//		System.out.println("hitObject = " + hitObject + " distance = " + Math.sqrt(hitObject.distanceSq));
		
		GLAutoDrawable glDrawable = (GLAutoDrawable) viewport.getComponent();
		glDrawable.getContext().makeCurrent();
		GL gl = glDrawable.getGL();
		viewport.validateScreenShotTexture();
		viewport.drawScreenShot(0, 0, glDrawable.getWidth(), glDrawable.getHeight(), 1.0f);
		viewport.spatialMode();
		viewport.setModelViewMatrix(Main.getInstance().getSelection().getNode());
		gl.glDepthMask(false);
		
		gl.glDisable(GL_LIGHTING);
		gl.glColor3f(0.5f, 0.5f, 1.0f);
		gl.glLineWidth(2);
		if (chain != null) {
			
			Point3d p = new Point3d();
			gl.glBegin(GL_LINE_STRIP);
			for (BaseVertex vertex : chain) {
				vertex.getPosition(p);
				gl.glVertex3d(p.x, p.y, p.z);
			}
			gl.glEnd();
			
			computeLathedVertices();
			
			int segments = segmentsAttr.getInt();
			int points = chain.length;
			
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			gl.glColor4f(0.5f, 0.5f, 1.0f, 0.25f);
			gl.glLineWidth(1);
			for (int i = 0; i < segments; i++) {
				gl.glBegin(GL_LINE_STRIP);
				for(int j = 0; j < points; j++) {
					gl.glVertex3d(lathedPoints[i][j].x, lathedPoints[i][j].y, lathedPoints[i][j].z);
				}
				gl.glEnd();
			}
			for (int i = 0; i < points; i++) {
				gl.glBegin(GL_LINE_STRIP);
				for(int j = 0; j < segments; j++) {
					gl.glVertex3d(lathedPoints[j][i].x, lathedPoints[j][i].y, lathedPoints[j][i].z);
				}
				gl.glEnd();
			}
			
			gl.glEnable(GL_LIGHTING);
			
			latheMaterial.applyMaterial(gl, GL_FRONT_AND_BACK);
			Vector3d u = new Vector3d();
			Vector3d v = new Vector3d();
			Vector3d normal = new Vector3d();
			gl.glBegin(GL_QUADS);
			for (int segment = 0; segment < segments; segment++) {
				for (int i = 0; i < points - 1; i++) {
					Point3d p0 = lathedPoints[segment][i];
					Point3d p1 = lathedPoints[(segment + 1) % segments][i];
					Point3d p2 = lathedPoints[(segment + 1) % segments][i + 1];
					Point3d p3 = lathedPoints[segment][i + 1];
					u.sub(p1, p0);
					v.sub(p3, p0);
					normal.cross(u, v);
					normal.normalize();
					gl.glNormal3d(normal.x, normal.y, normal.z);	
					gl.glVertex3d(p0.x, p0.y, p0.z);
					gl.glVertex3d(p1.x, p1.y, p1.z);
					gl.glVertex3d(p2.x, p2.y, p2.z);
					gl.glVertex3d(p3.x, p3.y, p3.z);
				}
			}
			gl.glEnd();
			gl.glDisable(GL_BLEND);
			gl.glDisable(GL_LIGHTING);
			viewport.rasterMode();
			viewport.drawString("click to lathe", mouseX, mouseY);
			viewport.spatialMode();
		}
		gl.glLineWidth(1);
		
		
		
		gl.glDepthMask(true);
		gl.glFlush();
		glDrawable.swapBuffers();
		glDrawable.getContext().release();
	}
	
	private class LatheMouseListener extends MouseAdapter {
		private final ViewportGl viewport;
		
		LatheMouseListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				return;
			}
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				return;
			}
			
		}
	}
	
	private class LatheMouseMotionListener extends MouseMotionAdapter {
		private final ViewportGl viewport;
		
		LatheMouseMotionListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
			HitEdge hitEdge = (HitEdge) MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 64, sdsModel, 0, Sds.Type.STRAY_EDGE);
			if (hitEdge == null) {
				chain = null;
			} else {
				Sds sds = sdsModel.getSds();
				startEdge = sds.getStart(hitEdge.halfEdge);
				chain = sds.getChain((BaseVertex) startEdge.getVertex());
			}
			highlight(viewport);
		}
	}
}
