package com.jpatch.boundary.tools;

import static javax.media.opengl.GL.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.*;

import com.jpatch.afw.attributes.*;

import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public abstract class AbstractBasicTool implements JPatchTool, ViewportOverlay {
	
	
	private MouseMotionListener[] mouseMotionListeners;
	private MouseListener[] mouseListeners;
	
	protected EnumSet<Sds.Type> STANDARD_SELECTION_TYPE = EnumSet.of(Sds.Type.EDGE, Sds.Type.FACE, Sds.Type.VERTEX, Sds.Type.STRAY_VERTEX, Sds.Type.STRAY_EDGE);
	protected EnumSet<Sds.Type> LIMIT_SELECTION_TYPE = EnumSet.of(Sds.Type.LIMIT);
	protected final EnumSet<Sds.Type> NULL_SELECTION_TYPE = EnumSet.noneOf(Sds.Type.class);
	
	protected HitObject hitObject;
	protected HalfEdge hitEdge;
	final ObjectFilter hitEdgeFilter = new ObjectFilter() {
		public boolean accept(Object o) {
			if (o != hitEdge && o instanceof HalfEdge) {
				return ((HalfEdge) o).isBoundary();
			}
			return false;
		}
	};
	
	protected Selection hitSelection = new Selection();
	protected Point hitPoint;
	
	protected Point3d localStart = new Point3d();
	
	private final Polygon lassoPolygon = new Polygon();
	private final Set<AbstractVertex> vertices = new HashSet<AbstractVertex>();
	protected static enum Mode { IDLE, HOVER, SELECT, LASSO, ACTION }
	private static enum Select { ADD, REMOVE }
	
	protected Mode mode = Mode.HOVER;
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
			mouseListeners[i] = new SelectMouseListener(viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
			mouseMotionListeners[i] = new SelectMouseMotionListener(viewports[i]);
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
	
	protected void setActionMode(Viewport viewport, HitObject hitObject, MouseEvent event) {
		viewport.freezeDepthBuffer(true);
		snapPointer(viewport, hitObject.screenPosition);
		mode = Mode.ACTION;
		hitSelection.getTransformable().begin();
		localStart.set(hitObject.screenPosition);
		viewport.projectFromScreen(localStart, localStart);
		highlightHitObject(viewport, false, false, true);
	}
	
	private class SelectMouseListener extends MouseAdapter {
		final Viewport viewport;
		
		private SelectMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
//			if (hitObject == null) {
//				return;
//			}
			if (e.getButton() == MouseEvent.BUTTON1) {
				final SdsModel sdsModel = hitSelection.getSdsModel();
				if (sdsModel == null) {
					return;
				}
				final int level = sdsModel.getEditLevelAttribute().getInt();
				
				
				switch (mode) {
				case IDLE:
					HitObject hitObject = MouseSelector.isHit(viewport, e.getX(), e.getY(), 32 * 32, hitSelection);
					if (hitObject != null) {
						if (hitObject instanceof HitEdge) {
							hitEdge = ((HitEdge) hitObject).halfEdge;
						}
						setActionMode(viewport, hitObject, e);
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
					EnumSet<Sds.Type> selectionType = getSelectionType(viewport);
					hitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 32 * 32, sdsModel, level, selectionType, null);
					if (hitObject != null) {
						if (MouseSelector.isSelectionTrigger(e)) {
							mode = Mode.SELECT;
						} else {
							if (hitObject instanceof HitEdge) {
								hitEdge = ((HitEdge) hitObject).halfEdge;
							}
							setSelection(hitSelection, hitObject);
							setActionMode(viewport, hitObject, e);
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
					hitEdge = null;
					viewport.freezeDepthBuffer(false);
					highlightHitObject(viewport, true, true, false);
//					break;
//				default:
//					assert false : mode;
//				}
			}
		}		
	}
	
	private void updateSelection(Viewport viewport, int mx, int my, SdsModel sdsModel, int level, EnumSet<Sds.Type> selectionFilter) {
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
			final EnumSet<Sds.Type> type = getSelectionFilter(selectionType);
			HitObject hitObject = MouseSelector.getObjectAt(viewport, mx, my, Double.MAX_VALUE, sdsModel, level, type, null);
//			vertices.clear();
			if (hitObject != null) {
				hitObject.getVertices(vertices);
			}
			break;
		}
		
		/* if we don't want faces, select just the edges */
		if (selectionType == Selection.Type.FACES && !selectionFilter.contains(Sds.Type.FACE)) {
			selectionType = Selection.Type.EDGES;
		}
		/* if we don't want edges, select just the vertices */
		if (selectionType == Selection.Type.EDGES && !selectionFilter.contains(Sds.Type.EDGE) && !selectionFilter.contains(Sds.Type.STRAY_EDGE) && !selectionFilter.contains(Sds.Type.BOUNDARY_EDGE)) {
			selectionType = Selection.Type.VERTICES;
		}
		
		/* configure hotSelection */
		hitSelection.clear(null);
		hitSelection.setNode(sdsModel, null);
		hitSelection.addVertices(vertices, null);
		hitSelection.setType(selectionType, null);
		
		/* filter selection */
		switch (selectionType) {
		case EDGES:
			
			final boolean acceptRegular = selectionFilter.contains(Sds.Type.EDGE);
			final boolean acceptBoundary = selectionFilter.contains(Sds.Type.BOUNDARY_EDGE);
			final boolean acceptStray = selectionFilter.contains(Sds.Type.STRAY_EDGE);
			Set<HalfEdge> edgesToRemove = new HashSet<HalfEdge>();
			for (HalfEdge edge : hitSelection.getEdges()) {
				if (edge.isStray() && !acceptStray) {
					edgesToRemove.add(edge);
				}
				if (!acceptRegular && (!edge.isBoundary() || !acceptBoundary)) {
					edgesToRemove.add(edge);
				}
			}
			hitSelection.removeEdges(edgesToRemove, null);
			
			break;
		case VERTICES:
			break;
		}
		highlightHitObject(viewport, false, false, false);
		Main.getInstance().getSelection().set(hitSelection);
	}
	
	private EnumSet<Sds.Type> getSelectionFilter(Selection.Type type) {
		switch(type) {
		case EDGES:
			return EnumSet.of(Sds.Type.EDGE);
		case VERTICES:
			return EnumSet.of(Sds.Type.VERTEX);
		case FACES:
			return EnumSet.of(Sds.Type.FACE);
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
		Main.getInstance().getSelection().set(selection);
	}
	
	private class SelectMouseMotionListener implements MouseMotionListener {
		final Viewport viewport;
		Point3d mouse = new Point3d();
		Vector3d vector = new Vector3d();
		
		private SelectMouseMotionListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		public void mouseDragged(MouseEvent e) {
			switch (mode) {
			case LASSO:
				final SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
				final int level = sdsModel.getEditLevelAttribute().getInt();
				
				int mx = e.getX();
				int my = e.getY();
				
				updateSelection(viewport, mx, my, sdsModel, level, STANDARD_SELECTION_TYPE);
				break;
			}
		}

		public void mouseMoved(MouseEvent e) {
			final Selection selection = Main.getInstance().getSelection();
			final SdsModel sdsModel = selection.getSdsModel();
			final int level = sdsModel.getEditLevelAttribute().getInt();
			
			switch (mode) {
			case HOVER:
				EnumSet<Sds.Type> selectionType = getSelectionType(viewport);
				HitObject newHitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 32 * 32, sdsModel, level, selectionType, null);
				if (newHitObject == null ? hitObject != null : !newHitObject.equals(hitObject)) {
					hitObject = newHitObject;
					setSelection(hitSelection, hitObject);
					highlightHitObject(viewport, false, false, false);
					System.out.println(hitObject);
					
					if (hitObject instanceof HitVertex) {
						Main.getInstance().getExplorer().goTo(((HitVertex) hitObject).vertex);
					} else if (hitObject instanceof HitEdge) {
						Main.getInstance().getExplorer().goTo(((HitEdge) hitObject).halfEdge);
					} else if (hitObject instanceof HitFace) {
						Main.getInstance().getExplorer().goTo(((HitFace) hitObject).face);
					}
				}
				break;
			case IDLE:
				boolean strong = MouseSelector.isHit(viewport, e.getX(), e.getY(), 32 * 32, hitSelection) != null;
				highlightHitObject(viewport, false, false, strong);
				break;
			}
		}	
	};
	
	private EnumSet<Sds.Type> getSelectionType(Viewport viewport) {
		final ViewDef viewDef = viewport.getViewDef();
		final boolean showMesh = viewDef.getShowControlMeshAttribute().getBoolean();
		final boolean showLimit = viewDef.getShowLimitSurfaceAttribute().getBoolean();
		final boolean showProjection = viewDef.getShowProjectedMeshAttribute().getBoolean();
		if (showProjection && showLimit && !showMesh) {
			return LIMIT_SELECTION_TYPE;
		} else if (showMesh) {
			return STANDARD_SELECTION_TYPE;
		}
		return NULL_SELECTION_TYPE;
	}
	
	protected void snapPointer(Viewport viewport, Point3d screenPosition) {
		Point point = new Point((int) Math.round(screenPosition.x), (int) Math.round(screenPosition.y));
		hitPoint = new Point(point);
		SwingUtilities.convertPointToScreen(point, viewport.getComponent());
		Main.getInstance().getRobot().mouseMove(point.x, point.y);
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
