package com.jpatch.boundary.tools;

import static javax.media.opengl.GL.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.*;

import com.jpatch.afw.control.*;

import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class FlipTool implements VisibleTool {
	
	private TransformUtil transformUtil = new TransformUtil();
	private MouseMotionListener[] mouseMotionListeners;
	private MouseListener[] mouseListeners;
	
	
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
			mouseListeners[i] = new FlipMouseListener((ViewportGl) viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
			mouseMotionListeners[i] = new FlipMouseMotionListener((ViewportGl) viewports[i]);
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
		
		GLAutoDrawable glDrawable = (GLAutoDrawable) viewport.getComponent();
		glDrawable.getContext().makeCurrent();
		GL gl = glDrawable.getGL();
		viewport.validateScreenShotTexture();
		viewport.drawScreenShot(0, 0, glDrawable.getWidth(), glDrawable.getHeight(), 1.0f);
		viewport.spatialMode();
		viewport.getViewDef().configureTransformUtil(transformUtil);
		hitObject.node.getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
		viewport.setModelViewMatrix(transformUtil);
			
		viewport.drawSelection(hitSelection, new Color3f(1, 1, 0));

		glDrawable.swapBuffers();
		glDrawable.getContext().release();
	}
	
	private class FlipMouseListener extends MouseAdapter {
		final ViewportGl viewport;
		
		private FlipMouseListener(ViewportGl viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			Sds sds = Main.getInstance().getSelection().getSdsModel().getSds();
			List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>();
			sds.flipFaces(editList, hitSelection.getFaces());
			Main.getInstance().getUndoManager().addEdit("flip surface", editList);
			Main.getInstance().repaintViewports();
		}
//		@Override
//		public void mousePressed(MouseEvent e) {
//			if (hitObject == null) {
//				return;
//			}
//			if (e.getButton() == MouseEvent.BUTTON1) {
//				Selection selection = Main.getInstance().getSelection();
//				updateSelection(selection, hitObject);
//				selection.getTransformable().begin();
//				drag = true;
//				localStart.set(hitObject.screenPosition);
//				transformUtil.projectFromScreen(TransformUtil.LOCAL, localStart, localStart);
//			}
//		}
//
//		@Override
//		public void mouseReleased(MouseEvent e) {
//			if (e.getButton() == MouseEvent.BUTTON1) {
//				Selection selection = Main.getInstance().getSelection();
//				selection.getTransformable().end(new ArrayList<JPatchUndoableEdit>());
//				Main.getInstance().getSelection().clear(null);
//				Main.getInstance().repaintViewports();
//				drag = false;
//			}
//		}		
	}
	
	private void updateSelection(Selection selection, HitObject hitObject) {
		Set<Face> surface = new HashSet<Face>();
		expandSelection(((HitFace) hitObject).face, surface);
		selection.clear(null);
		selection.addFaces(surface, null);
	}
	
	private void expandSelection(Face face, Set<Face> surface) {
		if (!surface.contains(face)) {
			surface.add(face);
			for (HalfEdge faceEdge : face.getEdges()) {
				for (HalfEdge vertexEdge : faceEdge.getVertex().getEdges()) {
					Face neighborFace = vertexEdge.getFace();
					if (neighborFace != null) {
						expandSelection(neighborFace, surface);
					}
				}
			}
		}
	}
	
	private class FlipMouseMotionListener implements MouseMotionListener {
		final ViewportGl viewport;
		Point3d mouse = new Point3d();
		Vector3d vector = new Vector3d();
		
		private FlipMouseMotionListener(ViewportGl viewport) {
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
				int selectionType = Sds.Type.FACE;
				HitObject newHitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), Double.MAX_VALUE, sdsModel, level, selectionType, null);
				if (newHitObject != null && !newHitObject.equals(hitObject)) {
					hitObject = newHitObject;
					updateSelection(hitSelection, hitObject);
					highlightHitObject(viewport);
				}
			}
		}	
	};
	
}
