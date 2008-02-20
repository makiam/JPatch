package com.jpatch.boundary.tools;

import java.awt.event.*;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;
import javax.vecmath.*;

import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class AddEdgeTool implements VisibleTool {
	private MouseMotionListener[] mouseMotionListeners;
	private MouseListener[] mouseListeners;
	private TextureUpdater textureUpdater;
	
	private BaseVertex floatingVertex;
	private BaseVertex startVertex;
	private BaseVertex endVertex;
	
	private boolean drag;
	
	public void draw(Viewport viewport) {
		
	}

	public void registerListeners(Viewport[] viewports) {
		mouseListeners = new MouseListener[viewports.length];
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new AddEdgeMouseListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
			mouseMotionListeners[i] = new AddEdgeMouseMotionListener((ViewportGl) viewports[i]);
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
		
		Point3f p = new Point3f();
		gl.glPointSize(6);
		gl.glColor3f(0.5f, 0.5f, 1.0f);
		if (startVertex != null) {
			gl.glBegin(GL_POINTS);
			startVertex.getPosition(p);
			gl.glVertex3f(p.x, p.y, p.z);
			gl.glEnd();
			if (endVertex != null) {
				if (endVertex != floatingVertex) {
					gl.glBegin(GL_POINTS);
					endVertex.getPosition(p);
					gl.glVertex3f(p.x, p.y, p.z);
					gl.glEnd();
				}
				gl.glBegin(GL_LINES);
				startVertex.getPosition(p);
				gl.glVertex3f(p.x, p.y, p.z);
				endVertex.getPosition(p);
				gl.glVertex3f(p.x, p.y, p.z);
				gl.glEnd();
			}
		}
		
		
		Sds sds = Main.getInstance().getSelection().getSdsModel().getSds();
//		System.out.println("startVertex=" + startVertex + " endVertex=" + endVertex);
		if (sds.getStrayVertices().contains(startVertex) && endVertex != null && endVertex != floatingVertex) {
//			System.out.println("is start of chain: " + sds.isStartOfChain(endVertex));
			if (sds.isStartOfChain(endVertex)) {
//				System.out.println("is connected: " + sds.isConnected(startVertex, endVertex));
				if (sds.isConnected(startVertex, endVertex)) {
//					sds.addFace(null, 0, Main.getInstance().getDefaultMaterial(), sds.getLoop(startVertex));
					BaseVertex[] vertices = sds.getLoop(startVertex);
					gl.glEnable(GL_BLEND);
					gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
					gl.glColor4f(0.5f, 0.5f, 1.0f, 0.5f);
					gl.glBegin(GL_TRIANGLE_FAN);
					double mx = 0, my = 0, mz = 0;
					for (BaseVertex vertex : vertices) {
						vertex.getPosition(p);
						mx += p.x; my += p.y; mz += p.z;
					}
					mx /= vertices.length;
					my /= vertices.length;
					mz /= vertices.length;
					gl.glVertex3f((float) mx, (float) my, (float) mz);
					for (BaseVertex vertex : vertices) {
						vertex.getPosition(p);
						gl.glVertex3f(p.x, p.y, p.z);
					}
					vertices[0].getPosition(p);
					gl.glVertex3f(p.x, p.y, p.z);
					gl.glEnd();
					gl.glDisable(GL_BLEND);
				}
			}
		}
//		System.out.println(hitVertex);
		
		gl.glDepthMask(true);
		
		gl.glFlush();
		glDrawable.swapBuffers();
		glDrawable.getContext().release();
	}
	
	private class AddEdgeMouseListener extends MouseAdapter {
		private final ViewportGl viewport;
		
		AddEdgeMouseListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				return;
			}
			if (startVertex == null) {
				startVertex = new BaseVertex();
				TransformUtil transformUtil = new TransformUtil();
				viewport.getViewDef().configureTransformUtil(transformUtil);
				Main.getInstance().getSelection().getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
				Point3d p = new Point3d(e.getX(), e.getY(), 0);
				transformUtil.projectFromScreen(TransformUtil.LOCAL, p, p);
				startVertex.setPosition(p);
			}
			highlight(viewport);
			floatingVertex = new BaseVertex();
			drag = true;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				return;
			}
			
			Sds sds = Main.getInstance().getSelection().getSdsModel().getSds();
			boolean addFace = false;
			if (sds.getStrayVertices().contains(startVertex) && endVertex != null && endVertex != floatingVertex) {
				System.out.println("is start of chain: " + sds.isStartOfChain(endVertex));
				if (sds.isStartOfChain(endVertex)) {
					System.out.println("is connected: " + sds.isConnected(startVertex, endVertex));
					if (sds.isConnected(startVertex, endVertex)) {
						System.out.println("adding face");
						sds.addFace(null, 0, Main.getInstance().getDefaultMaterial(), sds.getLoop(startVertex));
						addFace = true;
//						BaseVertex[] vertices = sds.getLoop(startVertex);
					}
				}
			}
			
			if (!addFace && endVertex != null) {
				sds.addSegment(null, startVertex, endVertex);
			}
			
			startVertex = endVertex;
			endVertex = null;
			Main.getInstance().repaintViewports();
			highlight(viewport);
			
			drag = false;
		}
		
	}

	private class AddEdgeMouseMotionListener implements MouseMotionListener {
		private final ViewportGl viewport;
		
		AddEdgeMouseMotionListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		public void mouseDragged(MouseEvent e) {
			if (!drag) {
				return;
			}
			
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
			HitVertex hitVertex = (HitVertex) MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 64, sdsModel, 0, MouseSelector.Type.VERTEX);
			if (hitVertex != null) {
				endVertex = (BaseVertex) hitVertex.vertex;
			} else {
				endVertex = floatingVertex;
				TransformUtil transformUtil = new TransformUtil();
				viewport.getViewDef().configureTransformUtil(transformUtil);
				Main.getInstance().getSelection().getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
				Point3d p = new Point3d(e.getX(), e.getY(), 0);
				transformUtil.projectFromScreen(TransformUtil.LOCAL, p, p);
				endVertex.setPosition(p);
			}
			
			highlight(viewport);
		}

		public void mouseMoved(MouseEvent e) {
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
			HitVertex hitVertex = (HitVertex) MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 64, sdsModel, 0, MouseSelector.Type.VERTEX);
			if (hitVertex != null) {
				startVertex = (BaseVertex) hitVertex.vertex;
			} else {
				startVertex = null;
			}
			highlight(viewport);
		}
		
	}
}
