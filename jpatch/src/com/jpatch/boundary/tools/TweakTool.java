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
	
	private static int STANDARD_SELECTION_TYPE = Sds.Type.EDGE | Sds.Type.FACE | Sds.Type.VERTEX | Sds.Type.STRAY_VERTEX | Sds.Type.STRAY_EDGE;
	private static int LIMIT_SELECTION_TYPE = Sds.Type.LIMIT;
	
	private HitObject hitObject;
	private Selection hitSelection = new Selection();
	private Point hitPoint;
	private Point3d localStart = new Point3d();
	
	private final Polygon lassoPolygon = new Polygon();
	private final Set<AbstractVertex> vertices = new HashSet<AbstractVertex>();
	private static enum Mode { IDLE, MOVE, SELECT_RECTANGLE, SELECT_LASSO, SELECT_PROXIMITY }
	
	private Mode mode = Mode.IDLE;
	private Selection.Type selectionType;
	
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
//		textureUpdater = new TextureUpdater(viewports);
//		textureUpdater.start();
	}

	public void unregisterListeners(Viewport[] viewports) {
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseListener(mouseListeners[i]);
			viewports[i].getComponent().removeMouseMotionListener(mouseMotionListeners[i]);
		}
//		textureUpdater.stop();
	}

	private void highlightHitObject(ViewportGl viewport) {
		
		GLAutoDrawable glDrawable = (GLAutoDrawable) viewport.getComponent();
		glDrawable.getContext().makeCurrent();
		GL gl = glDrawable.getGL();
		viewport.validateScreenShotTexture();
		viewport.drawScreenShot(0, 0, glDrawable.getWidth(), glDrawable.getHeight(), 1.0f);
		
		if (mode == Mode.SELECT_LASSO || mode == Mode.SELECT_RECTANGLE) {
			gl.glColor3f(1, 1, 0);
			gl.glLineWidth(1);
			gl.glBegin(GL_LINE_LOOP);
			for (int i = 0; i < lassoPolygon.npoints; i++) {
				gl.glVertex2i(lassoPolygon.xpoints[i], lassoPolygon.ypoints[i]);
			}
			gl.glEnd();
		}
		
		viewport.spatialMode();
		
		viewport.spatialMode();
		viewport.getViewDef().configureTransformUtil(transformUtil);
		if (hitSelection.getNode() != null) {
			hitSelection.getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
			viewport.setModelViewMatrix(transformUtil);
			gl.glDisable(GL_DEPTH_TEST);
			viewport.drawSelection(hitSelection, new Color3f(1, 1, 0));
			gl.glEnable(GL_DEPTH_TEST);
		}
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
//			if (hitObject == null) {
//				return;
//			}
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (hitObject != null) {
					if (e.isShiftDown()) {
						mode = Mode.SELECT_PROXIMITY;
					} else {
						mode = Mode.MOVE;
						snapPointer(viewport);
						Selection selection = Main.getInstance().getSelection();
						updateSelection(selection, hitObject);
						selection.getTransformable().begin();
						localStart.set(hitObject.screenPosition);
						transformUtil.projectFromScreen(TransformUtil.LOCAL, localStart, localStart);
					}
				} else {
					if (e.isShiftDown()) {
						mode = Mode.SELECT_LASSO;
						lassoPolygon.npoints = 0;
						lassoPolygon.invalidate();
						selectionType = Selection.Type.VERTICES;
					} else {
						mode = Mode.SELECT_RECTANGLE;
						final int mx = e.getX();
						final int my = e.getY();
						lassoPolygon.npoints = 4;
						lassoPolygon.xpoints = new int[] { mx, mx, mx, mx };
						lassoPolygon.ypoints = new int[] { my, my, my, my };
						lassoPolygon.invalidate();
						selectionType = Selection.Type.VERTICES;
					}
				}
				System.out.println(mode);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				Selection selection = Main.getInstance().getSelection();
				selection.getTransformable().end(new ArrayList<JPatchUndoableEdit>());
				Main.getInstance().getSelection().clear(null);
				Main.getInstance().repaintViewports();
				mode = Mode.IDLE;
			}
		}		
	}
	
	private void promoteSelectionType(Selection.Type newType) {
//		switch (selectionType) {
//		case VERTICES:
//			selectionType = newType;
//			break;
//		case EDGES:
//			if (newType == Selection.Type.FACES) {
//				selectionType = newType;
//			}
//			break;
//		}
		selectionType = newType;
	}
	private void updateSelection(ViewportGl viewport, int mx, int my, SdsModel sdsModel, int level) {
		switch(mode) {
		case SELECT_LASSO:
			lassoPolygon.addPoint(mx, my);
			lassoPolygon.invalidate();
			MouseSelector.getVerticesUnderLasso(viewport, lassoPolygon, sdsModel, level, false, vertices);
			promoteSelectionType(MouseSelector.getBestSelectionType(sdsModel.getSds(), level, vertices));
			break;
		case SELECT_RECTANGLE:
			lassoPolygon.xpoints[1] = lassoPolygon.xpoints[2] = mx;
			lassoPolygon.ypoints[3] = lassoPolygon.ypoints[2] = my;
			lassoPolygon.invalidate();
			MouseSelector.getVerticesUnderLasso(viewport, lassoPolygon, sdsModel, level, false, vertices);
			promoteSelectionType(MouseSelector.getBestSelectionType(sdsModel.getSds(), level, vertices));
			break;
		case SELECT_PROXIMITY:
			final int type;
			System.out.println(Main.getInstance().getActions().sdsModeSM.getValue());
			switch(Main.getInstance().getActions().sdsModeSM.getValue()) {
			case EDGE_MODE:
				type = Sds.Type.EDGE;
				break;
			case VERTEX_MODE:
				type = Sds.Type.VERTEX;
				break;
			case FACE_MODE:
				type = Sds.Type.FACE;
				break;
			default:
				throw new RuntimeException(); // should never get here	
			}
			HitObject hitObject = MouseSelector.getObjectAt(viewport, mx, my, Double.MAX_VALUE, sdsModel, level, type, null);
			hitObject.getVertices(vertices);
			break;
		}
		hitSelection.setNode(Main.getInstance().getSelection().getNode(), null);
		hitSelection.clear(null);
		hitSelection.addVertices(vertices, null);
		hitSelection.setType(selectionType, null);
		highlightHitObject(viewport);
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
			switch (mode) {
			case MOVE:
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
				break;
			case SELECT_LASSO:		// fallthrough intended
			case SELECT_RECTANGLE:	// fallthrough intended
			case SELECT_PROXIMITY:
				final SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
				final int level = sdsModel.getEditLevelAttribute().getInt();
				
				int mx = e.getX();
				int my = e.getY();
				
				updateSelection(viewport, mx, my, sdsModel, level);
				break;
			}
		}

		public void mouseMoved(MouseEvent e) {
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
			if (sdsModel != null) {
				int level = sdsModel.getEditLevelAttribute().getInt();
				int selectionType = viewport.getViewDef().getShowControlMeshAttribute().getBoolean() ? STANDARD_SELECTION_TYPE : LIMIT_SELECTION_TYPE;
				HitObject newHitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 32 * 32, sdsModel, level, selectionType, null);
				if (newHitObject != null && !newHitObject.equals(hitObject)) {
					hitObject = newHitObject;
					updateSelection(hitSelection, hitObject);
					highlightHitObject(viewport);
				} else {
					hitObject = newHitObject;
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
