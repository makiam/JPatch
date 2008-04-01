package com.jpatch.boundary.tools;

import static javax.media.opengl.GL.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.swing.*;
import javax.swing.undo.*;
import javax.vecmath.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;

import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class TweakTool implements VisibleTool {
	private enum ToolMode { FREE, NORMAL }
	
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
	private static enum Mode { IDLE, HOVER, MOVE, SELECT, LASSO }
	private static enum Select { ADD, REMOVE }
	
	private Mode mode = Mode.HOVER;
	private Select select = Select.ADD;
	
	private Selection.Type selectionType;
	
	private final BooleanAttr selectLassoAttr = new BooleanAttr();
	private final StateMachine<ToolMode> toolModeAttr = new StateMachine<ToolMode>(ToolMode.class, ToolMode.FREE);
	
	public void draw(Viewport viewport) {
		// TODO Auto-generated method stub

	}

	public BooleanAttr getSelectLassoAttribute() {
		return selectLassoAttr;
	}
	
	public StateMachine<ToolMode> getToolModeAttribute() {
		return toolModeAttr;
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

	private void highlightHitObject(ViewportGl viewport, boolean redraw, boolean validateTexture, boolean strong) {
		
		GLAutoDrawable glDrawable = (GLAutoDrawable) viewport.getComponent();
		glDrawable.getContext().makeCurrent();
		GL gl = glDrawable.getGL();
		if (redraw) {
			gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			viewport.draw();
			if (validateTexture) {
				viewport.validateScreenShotTexture(false);
				viewport.validateDepthBuffer(false);
			}
		} else {
			viewport.validateScreenShotTexture();
			viewport.drawScreenShot(0, 0, glDrawable.getWidth(), glDrawable.getHeight(), 1.0f);
		}
		if (mode == Mode.LASSO) {
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
		
		hitSelection.getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
		viewport.setModelViewMatrix(transformUtil);
		gl.glDisable(GL_DEPTH_TEST);
		
		
//		Selection selection = Main.getInstance().getSelection();
//		if (hitSelection.getNode() != null) {
//			hitSelection.getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
			viewport.setModelViewMatrix(transformUtil);
			gl.glDisable(GL_DEPTH_TEST);
//			viewport.drawSelection(selection, new Color3f(1, 1, 0));
//			if (mode == Mode.HOVER && hitObject != null) {
//				setSelection(hitSelection, hitObject);
				viewport.drawSelection(hitSelection, new Color4f(1, 1, 0, strong ? 1.0f : 0.5f));
//			}
			gl.glEnable(GL_DEPTH_TEST);
//		}
		glDrawable.swapBuffers();
		glDrawable.getContext().release();
	}
	
	private void setLassoMode(final int mx, final int my) {
		mode = Mode.LASSO;
		if (selectLassoAttr.getBoolean()) {
			lassoPolygon.npoints = 0;
			lassoPolygon.invalidate();
			selectionType = Selection.Type.VERTICES;
		} else {
			lassoPolygon.npoints = 4;
			lassoPolygon.xpoints = new int[] { mx, mx, mx, mx };
			lassoPolygon.ypoints = new int[] { my, my, my, my };
			lassoPolygon.invalidate();
			selectionType = Selection.Type.VERTICES;
		}
	}
	
	private void setMoveMode(ViewportGl viewport, Point3d screenPosition) {
		snapPointer(viewport, screenPosition);
		mode = Mode.MOVE;
		hitSelection.getTransformable().begin();
		localStart.set(screenPosition);
		transformUtil.projectFromScreen(TransformUtil.LOCAL, localStart, localStart);
		highlightHitObject(viewport, false, false, true);
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
				final SdsModel sdsModel = hitSelection.getSdsModel();
				final int level = sdsModel.getEditLevelAttribute().getInt();
				
				
				switch (mode) {
				case IDLE:
					System.out.println("selectionType = " + hitSelection.getType());
					Point3d screenPos = new Point3d();
					if (MouseSelector.isHit(viewport, e.getX(), e.getY(), 32 * 32, hitSelection, screenPos)) {
						setMoveMode(viewport, screenPos);
					}
					
//					int selectionFilter = getSelectionFilter(hitSelection.getType());
//					hitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 32 * 32, sdsModel, level, selectionFilter, null);
//					System.out.println("hitObject = " + hitObject);
//					System.out.println("hitSelection = " + hitSelection);
//					if (hitObject != null) {
//						if (hitObject instanceof HitVertex) {
//							if (hitSelection.getVertices().contains(((HitVertex) hitObject).vertex)) {
//								setMoveMode(viewport);
//							}
//						} else if (hitObject instanceof HitEdge) {
//							if (hitSelection.getEdges().contains(((HitEdge) hitObject).halfEdge)) {
//								setMoveMode(viewport);
//							}
//						} else if (hitObject instanceof HitFace) {
//							if (hitSelection.getFaces().contains(((HitFace) hitObject).face)) {
//								setMoveMode(viewport);
//							}
//						}
//					}
					if (mode == Mode.IDLE) {
						hitSelection.clear(null);
						setLassoMode(e.getX(), e.getY());
						highlightHitObject(viewport, false, false, true);
					}
					
//					List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>(1);
//					selection.clear(editList);
//					Main.getInstance().getUndoManager().addEdit("clear selection", editList);
//					highlightHitObject(viewport, false, false);
					break;
				case HOVER:
					hitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 32 * 32, sdsModel, level, STANDARD_SELECTION_TYPE, null);
					if (hitObject != null) {
						if (MouseSelector.isSelectionTrigger(e)) {
							mode = Mode.SELECT;
						} else {
							setSelection(hitSelection, hitObject);
							setMoveMode(viewport, hitObject.screenPosition);
						}
					} else {
						setLassoMode(e.getX(), e.getY());
					}
					break;
				}
				
				
				
//				if (hitObject != null) {
//					if (e.isShiftDown()) {
//						mode = Mode.SELECT;
//						if (hitObject instanceof HitVertex) {
//							selectionType = Selection.Type.VERTICES;
//						} else if (hitObject instanceof HitEdge) {
//							selectionType = Selection.Type.EDGES;
//						} else if (hitObject instanceof HitFace) {
//							selectionType = Selection.Type.FACES;
//						}
//						vertices.clear();
//					} else {
//						mode = Mode.MOVE;
//						snapPointer(viewport);
////						setSelection(tweakSelection, hitObject);
//						
//						Collection<AbstractVertex> hitVertices = new HashSet<AbstractVertex>();
//						hitObject.getVertices(hitVertices);
//						if (hitVertices.size() > 0 && tweakSelection.getVertices().containsAll(hitVertices)) {
////							hitSelection.clear(null);
////							new Selection.State(tweakSelection).copyTo(hitSelection);
//						} else {
//							setSelection(tweakSelection, hitObject);
//						}
//						
//						tweakSelection.getTransformable().begin();
//						localStart.set(hitObject.screenPosition);
//						transformUtil.projectFromScreen(TransformUtil.LOCAL, localStart, localStart);
//					}
//				} else {
//					if (selectLassoAttr.getBoolean()) {
//						mode = Mode.SELECT_LASSO;
//						lassoPolygon.npoints = 0;
//						lassoPolygon.invalidate();
//						selectionType = Selection.Type.VERTICES;
//					} else {
//						mode = Mode.SELECT_RECTANGLE;
//						final int mx = e.getX();
//						final int my = e.getY();
//						lassoPolygon.npoints = 4;
//						lassoPolygon.xpoints = new int[] { mx, mx, mx, mx };
//						lassoPolygon.ypoints = new int[] { my, my, my, my };
//						lassoPolygon.invalidate();
//						selectionType = Selection.Type.VERTICES;
//					}
//				}
//				highlightHitObject(viewport, false);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
//				switch (mode) {
//				case MOVE:
//					List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>(1);
//					hitSelection.getTransformable().end(editList);
//					Main.getInstance().getUndoManager().addEdit("move", editList);
//					highlightHitObject(viewport, true, true);
//					mode = Mode.HOVER;
//					break;
//				case LASSO:
					if (hitSelection.getSize() > 1) {
						mode = Mode.IDLE;
					} else {
						mode = Mode.HOVER;
					}
					highlightHitObject(viewport, true, true, true);
//					break;
//				default:
//					assert false : mode;
//				}
			}
		}		
	}
	
	private void updateSelection(ViewportGl viewport, int mx, int my, SdsModel sdsModel, int level) {
		switch(mode) {
		case LASSO:
			if (selectLassoAttr.getBoolean()) {
				lassoPolygon.addPoint(mx, my);
				lassoPolygon.invalidate();
				MouseSelector.getVerticesUnderLasso(viewport, lassoPolygon, sdsModel, level, false, vertices);
				selectionType = MouseSelector.getBestSelectionType(sdsModel.getSds(), level, vertices);
			} else {
				lassoPolygon.xpoints[1] = lassoPolygon.xpoints[2] = mx;
				lassoPolygon.ypoints[3] = lassoPolygon.ypoints[2] = my;
				lassoPolygon.invalidate();
				MouseSelector.getVerticesUnderLasso(viewport, lassoPolygon, sdsModel, level, false, vertices);
				selectionType = MouseSelector.getBestSelectionType(sdsModel.getSds(), level, vertices);
			}
			break;
		case SELECT:
			final int type = getSelectionFilter(selectionType);
			HitObject hitObject = MouseSelector.getObjectAt(viewport, mx, my, Double.MAX_VALUE, sdsModel, level, type, null);
//			vertices.clear();
			if (hitObject != null) {
				hitObject.getVertices(vertices);
			}
			System.out.println("selectionType=" + selectionType + " hitobject=" + hitObject + " vertices=" + vertices);
			break;
		}
		Selection selection = Main.getInstance().getSelection();
		hitSelection.clear(null);
		hitSelection.setNode(sdsModel, null);
		hitSelection.addVertices(vertices, null);
		hitSelection.setType(selectionType, null);
		highlightHitObject(viewport, false, false, true);
	}
	
	private int getSelectionFilter(Selection.Type type) {
		switch(type) {
		case EDGES:
			return Sds.Type.EDGE;
		case VERTICES:
			return Sds.Type.VERTEX;
		case FACES:
			return Sds.Type.FACE;
		default:
			throw new IllegalArgumentException(type.toString());
		}
	}
	
	private void setSelection(Selection selection, HitObject hitObject) {
		if (hitObject == null) {
			selection.clear(null);
		} else {
			System.out.println("hitobject = " + hitObject + " node = " + hitObject.node);
			selection.setNode(hitObject.node, null);
			if (hitObject instanceof MouseSelector.HitVertex) {
				selection.setVertex(((HitVertex) hitObject).vertex, null);
			} else if (hitObject instanceof MouseSelector.HitEdge) {
				selection.setEdge(((HitEdge) hitObject).halfEdge, null);
			} else if (hitObject instanceof MouseSelector.HitFace) {
				selection.setFace(((HitFace) hitObject).face, null);
			}
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
				mouse.set(e.getX(), e.getY(), localStart.z);
				transformUtil.projectFromScreen(TransformUtil.LOCAL, mouse, mouse);
				vector.sub(mouse, localStart);
				hitSelection.getTransformable().translate(vector);
//				Main.getInstance().syncRepaintViewport(viewport);
				highlightHitObject(viewport, true, false, true);
				break;
//			case SELECT_LASSO:		// fallthrough intended
//			case SELECT_RECTANGLE:	// fallthrough intended
//			case SELECT_PROXIMITY:
			case LASSO:
				final SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
				final int level = sdsModel.getEditLevelAttribute().getInt();
				
				int mx = e.getX();
				int my = e.getY();
				
				updateSelection(viewport, mx, my, sdsModel, level);
				break;
			}
		}

		public void mouseMoved(MouseEvent e) {
//			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
//			if (sdsModel != null) {
//				int level = sdsModel.getEditLevelAttribute().getInt();
//				int selectionType = viewport.getViewDef().getShowControlMeshAttribute().getBoolean() ? STANDARD_SELECTION_TYPE : LIMIT_SELECTION_TYPE;
//				HitObject newHitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 32 * 32, sdsModel, level, selectionType, null);
//				if (newHitObject != null && !newHitObject.equals(hitObject)) {
//					hitObject = newHitObject;
//					Collection<AbstractVertex> hitVertices = new HashSet<AbstractVertex>();
//					hitObject.getVertices(hitVertices);
//					if (hitVertices.size() > 0 && tweakSelection.getVertices().containsAll(hitVertices)) {
//						hitSelection.clear(null);
//						new Selection.State(tweakSelection).copyTo(hitSelection);
//					} else {
//						setSelection(hitSelection, hitObject);
//					}
//					highlightHitObject(viewport, false);
//				} else {
//					hitObject = newHitObject;
//				}
//			}
			final Selection selection = Main.getInstance().getSelection();
			final SdsModel sdsModel = selection.getSdsModel();
			final int level = sdsModel.getEditLevelAttribute().getInt();
			
			switch (mode) {
			case HOVER:
				HitObject newHitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 32 * 32, sdsModel, level, STANDARD_SELECTION_TYPE, null);
				if (newHitObject == null ? hitObject != null : !newHitObject.equals(hitObject)) {
					hitObject = newHitObject;
					setSelection(hitSelection, hitObject);
					highlightHitObject(viewport, false, false, true);
				}
				break;
			case IDLE:
				boolean strong = MouseSelector.isHit(viewport, e.getX(), e.getY(), 32 * 32, hitSelection, new Point3d());
				highlightHitObject(viewport, false, false, strong);
				break;
			}
		}	
	};
	
	private void snapPointer(Viewport viewport, Point3d screenPosition) {
		Point point = new Point((int) Math.round(screenPosition.x), (int) Math.round(screenPosition.y));
		hitPoint = new Point(point);
		SwingUtilities.convertPointToScreen(point, viewport.getComponent());
		Main.getInstance().getRobot().mouseMove(point.x, point.y);
	}
}
