package com.jpatch.boundary.tools;

import static com.jpatch.afw.vecmath.TransformUtil.*;
import static javax.media.opengl.GL.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.swing.*;
import javax.swing.undo.*;
import javax.vecmath.*;

import trashcan.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;

import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.boundary.tools.NormalTool.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class CornerTool implements JPatchTool, ViewportOverlay {
	
	private static final int MAX_DISTANCE_SQ = 32 * 32;
	
//	private TransformUtil transformUtil = new TransformUtil();
	private MouseMotionListener[] mouseMotionListeners;
	private MouseListener[] mouseListeners;
	
	private static int STANDARD_SELECTION_TYPE = Sds.Type.VERTEX | Sds.Type.STRAY_VERTEX;
	private static int LIMIT_SELECTION_TYPE = Sds.Type.LIMIT;
	
	private HitObject hitObject;
	private Selection hitSelection = new Selection();
	private Point hitPoint;
	
	private Point3d localStart = new Point3d();
	
	private final Polygon lassoPolygon = new Polygon();
	private final Set<AbstractVertex> vertices = new HashSet<AbstractVertex>();
	private static enum Mode { IDLE, HOVER, MODIFY, SELECT, LASSO }
	private static enum Select { ADD, REMOVE }
	
	private Mode mode = Mode.HOVER;
	private Select select = Select.ADD;
	
	private Selection.Type selectionType;
	
	private final BooleanAttr selectLassoAttr = new BooleanAttr();
	
	private boolean strong;
	
	public BooleanAttr getSelectLassoAttribute() {
		return selectLassoAttr;
	}
	
	public void registerListeners(Viewport[] viewports) {
		Main.getInstance().getSelection().clear(null);
		Main.getInstance().repaintViewports();
		mouseListeners = new MouseListener[viewports.length];
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new TweakMouseListener(viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
			mouseMotionListeners[i] = new TweakMouseMotionListener(viewports[i]);
			viewports[i].getComponent().addMouseMotionListener(mouseMotionListeners[i]);
			viewports[i].addOverlay(this);
		}
	}

	public void unregisterListeners(Viewport[] viewports) {
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseListener(mouseListeners[i]);
			viewports[i].getComponent().removeMouseMotionListener(mouseMotionListeners[i]);
			viewports[i].removeOverlay(this);
		}
	}

	private void highlightHitObject(Viewport viewport, boolean redraw, boolean validateTexture, boolean strong) {
		this.strong = strong;
		viewport.redrawOverlays();
//		GLAutoDrawable glDrawable = (GLAutoDrawable) viewport.getComponent();
//		glDrawable.getContext().makeCurrent();
//		GL gl = glDrawable.getGL();
//		if (redraw) {
//			gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//			viewport.draw();
//			if (validateTexture) {
//				viewport.validateScreenShotTexture(false);
//				viewport.validateDepthBuffer(false);
//			}
//		} else {
//			viewport.validateScreenShotTexture();
//			viewport.drawScreenShot(0, 0, glDrawable.getWidth(), glDrawable.getHeight(), 1.0f);
//		}
//		if (mode == Mode.LASSO) {
//			gl.glColor3f(1, 1, 0);
//			gl.glLineWidth(1);
//			gl.glBegin(GL_LINE_LOOP);
//			for (int i = 0; i < lassoPolygon.npoints; i++) {
//				gl.glVertex2i(lassoPolygon.xpoints[i], lassoPolygon.ypoints[i]);
//			}
//			gl.glEnd();
//		}
//		
//		viewport.spatialMode();
//		
//		viewport.spatialMode();
//		viewport.getViewDef().configureTransformUtil(transformUtil);
//		
//		hitSelection.getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
//		viewport.setModelViewMatrix(transformUtil);
//		gl.glDisable(GL_DEPTH_TEST);
//		
//		
////		Selection selection = Main.getInstance().getSelection();
////		if (hitSelection.getNode() != null) {
////			hitSelection.getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
//			viewport.setModelViewMatrix(transformUtil);
//			gl.glDisable(GL_DEPTH_TEST);
////			viewport.drawSelection(selection, new Color3f(1, 1, 0));
////			if (mode == Mode.HOVER && hitObject != null) {
////				setSelection(hitSelection, hitObject);
//				viewport.drawSelection(hitSelection, new Color4f(1, 1, 0, strong ? 1.0f : 0.5f));
////			}
//			gl.glEnable(GL_DEPTH_TEST);
////		}
//		glDrawable.swapBuffers();
//		glDrawable.getContext().release();
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
	
	private void setModifyMode(Viewport viewport, HitObject hitObject) {
		snapPointer(viewport, hitObject.screenPosition);
		mode = Mode.MODIFY;
		hitSelection.getTransformable().begin();
		localStart.set(hitObject.screenPosition);
		viewport.projectFromScreen(localStart, localStart);
		highlightHitObject(viewport, false, false, true);
	}
	
	private class TweakMouseListener extends MouseAdapter {
		final Viewport viewport;
		
		private TweakMouseListener(Viewport viewport) {
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
					HitObject hitObject = MouseSelector.isHit(viewport, e.getX(), e.getY(), 32 * 32, hitSelection);
					if (hitObject != null) {
						setModifyMode(viewport, hitObject);
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
					int selectionType = getSelectionType(viewport);
					hitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 32 * 32, sdsModel, level, selectionType, null);
					if (hitObject != null) {
						if (MouseSelector.isSelectionTrigger(e)) {
							mode = Mode.SELECT;
						} else {
							setSelection(hitSelection, hitObject);
							setMoveMode(viewport, hitObject);
						}
					} else if (selectionType == STANDARD_SELECTION_TYPE) {
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
					highlightHitObject(viewport, true, true, false);
//					break;
//				default:
//					assert false : mode;
//				}
			}
		}		
	}
	
	private void updateSelection(Viewport viewport, int mx, int my, SdsModel sdsModel, int level) {
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
			break;
		}
		Selection selection = Main.getInstance().getSelection();
		hitSelection.clear(null);
		hitSelection.setNode(sdsModel, null);
		hitSelection.addVertices(vertices, null);
		hitSelection.setType(selectionType, null);
		highlightHitObject(viewport, false, false, false);
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
			selection.setNode(hitObject.node, null);
			if (hitObject instanceof MouseSelector.HitLimit) {
				selection.setLimit(((HitVertex) hitObject).vertex, null);
			} else if (hitObject instanceof MouseSelector.HitVertex) {
				selection.setVertex(((HitVertex) hitObject).vertex, null);
			} else if (hitObject instanceof MouseSelector.HitEdge) {
				selection.setEdge(((HitEdge) hitObject).halfEdge, null);
			} else if (hitObject instanceof MouseSelector.HitFace) {
				selection.setFace(((HitFace) hitObject).face, null);
			}
		}
	}
	
	private class TweakMouseMotionListener implements MouseMotionListener {
		final Viewport viewport;
		Point3d mouse = new Point3d();
		Vector3d vector = new Vector3d();
		
		private TweakMouseMotionListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		public void mouseDragged(MouseEvent e) {
			switch (mode) {
			case MOVE:
				if (hitPoint != null && hitPoint.x == e.getX() && hitPoint.y == e.getY()) {
					hitPoint = null;
					return;
				}
				if (toolModeAttr.getValue() == ToolMode.NORMAL) {
					normal.mouseDragged(viewport, e.getX(), e.getY());
				} else {
					mouse.set(e.getX(), e.getY(), localStart.z);
					viewport.projectFromScreen(mouse, mouse);
					vector.sub(mouse, localStart);
					if (hitSelection.getType() == Selection.Type.LIMIT) {
						vector.scale(1.0 / hitSelection.getLimit().getLimitFactor());
					}
					hitSelection.getTransformable().translate(vector);
	//				Main.getInstance().syncRepaintViewport(viewport);
				}
				Main.getInstance().repaintViewport(viewport);
//				highlightHitObject(viewport, true, false, true);
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
				int selectionType = getSelectionType(viewport);
				HitObject newHitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 32 * 32, sdsModel, level, selectionType, null);
				if (newHitObject == null ? hitObject != null : !newHitObject.equals(hitObject)) {
					hitObject = newHitObject;
					setSelection(hitSelection, hitObject);
					highlightHitObject(viewport, false, false, false);
				}
				break;
			case IDLE:
				boolean strong = MouseSelector.isHit(viewport, e.getX(), e.getY(), 32 * 32, hitSelection) != null;
				highlightHitObject(viewport, false, false, strong);
				break;
			}
		}	
	};
	
	private int getSelectionType(Viewport viewport) {
		final ViewDef viewDef = viewport.getViewDef();
		final boolean showMesh = viewDef.getShowControlMeshAttribute().getBoolean();
		final boolean showLimit = viewDef.getShowLimitSurfaceAttribute().getBoolean();
		final boolean showProjection = viewDef.getShowProjectedMeshAttribute().getBoolean();
		if (showProjection && showLimit && !showMesh) {
			return LIMIT_SELECTION_TYPE;
		} else if (showMesh) {
			return STANDARD_SELECTION_TYPE;
		}
		return 0;
	}
	
	private void snapPointer(Viewport viewport, Point3d screenPosition) {
		Point point = new Point((int) Math.round(screenPosition.x), (int) Math.round(screenPosition.y));
		hitPoint = new Point(point);
		SwingUtilities.convertPointToScreen(point, viewport.getComponent());
		Main.getInstance().getRobot().mouseMove(point.x, point.y);
	}
	
	private static class VertexNormal {
		Point3d pStart = new Point3d();
		Vector3d pNormal = new Vector3d();
		
		VertexNormal(AbstractVertex v) {
			v.getPosition(pStart);
			v.getVertexPoint().getNormal(pNormal);
			pNormal.normalize();
		}
		
		VertexNormal(AbstractVertex v, Vector3d normal) {
			v.getPosition(pStart);
			pNormal.set(normal);
		}
		
		void setFactor(AbstractVertex v, double f) {
			v.setPosition(pStart.x + pNormal.x * f, pStart.y + pNormal.y * f, pStart.z + pNormal.z * f);
		}
	}

	public void drawOverlay(Viewport viewport) {
		GL gl = viewport.getGL();
		viewport.resetModelviewMatrix(gl);
		gl.glDisable(GL_DEPTH_TEST);
		Viewport.drawSelection(gl, hitSelection, new Color4f(1, 1, 0, strong ? 0.8f : 0.6f));
		if (mode == Mode.LASSO) {
			viewport.rasterMode(gl);
			gl.glColor3f(1, 1, 0);
			gl.glLineWidth(1);
			gl.glBegin(GL_LINE_LOOP);
			for (int i = 0; i < lassoPolygon.npoints; i++) {
				gl.glVertex2i(lassoPolygon.xpoints[i], lassoPolygon.ypoints[i]);
			}
			gl.glEnd();
			viewport.spatialMode(gl);
		}
		gl.glEnable(GL_DEPTH_TEST);
	}
}
