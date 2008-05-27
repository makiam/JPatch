package com.jpatch.boundary.tools;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;

import static javax.media.opengl.GL.*;

import javax.swing.*;
import javax.vecmath.*;

import trashcan.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.afw.ui.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class LatheTool implements JPatchTool, ViewportOverlay {
	private static final BasicMaterial latheMaterial = new BasicMaterial(
		new Color4f(0, 0, 0, 0),
		new Color4f(0.5f, 0.5f, 1.0f, 0.33f),
		new Color4f(0, 0, 0, 0),
		new Color4f(0, 0, 0, 0),
		0
	);
	private static final BasicMaterial lineMaterial = new BasicMaterial(
		new Color4f(0.25f, 0.25f, 0.5f, 0.33f),
		new Color4f(0.5f, 0.5f, 1.0f, 0.33f),
		new Color4f(0, 0, 0, 0),
		new Color4f(0, 0, 0, 0),
		0
	);
	private static final Color4f lineColor = new Color4f(0.5f, 0.5f, 1.0f, 0.33f);
	
	private MouseMotionListener[] mouseMotionListeners;
	private MouseListener[] mouseListeners;
	
	private final Point3d axisStart = new Point3d(0, 0, 0);
	private final Point3d axisEnd = new Point3d(0, 1, 0);
	
	private final Tuple3Attr axisStartAttr = new Tuple3Attr(axisStart);
	private final Tuple3Attr axisEndAttr = new Tuple3Attr(axisEnd);
	private final DoubleAttr epsilonAttr = AttributeManager.getInstance().createBoundedDoubleAttr(0.1, 0.001, 100);
	private final IntAttr segmentsAttr = AttributeManager.getInstance().createBoundedIntAttr(8, 3, 32);
	private final DoubleAttr angleAttr = AttributeManager.getInstance().createBoundedDoubleAttr(360, 0.0, 360);
	private final BooleanAttr previewLimitAttr = new BooleanAttr(true);
	
	private HalfEdge lastStartEdge;
	private HalfEdge startEdge;
	private BaseVertex[] chain;
	private Point3d[][] lathedPoints = new Point3d[0][0];
	
	private int mouseX, mouseY;
	private boolean latheValid;
	
	private HitObject hitObject;
	private Selection hitSelection = new Selection();
	private boolean drag;
	private boolean wasDragged;
	
	private Point hitPoint;
	private Point3d localStart = new Point3d();
	
//	private TransformUtil transformUtil = new TransformUtil();
	
	private SdsModel lathedSds;
	private BaseVertex[][] lathedVertices = new BaseVertex[0][0];
	private boolean[] snap = new boolean[2];
	private boolean[] lastSnap = new boolean[2];
	private long clickTime;
	private long doubleClickTime = 500;	// ms
	
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
	
	public BooleanAttr getPreviewLimitAttribute() {
		return previewLimitAttr;
	}
	
	public void draw(Viewport viewport) {
		
	}

	public void registerListeners(Viewport[] viewports) {
		mouseListeners = new MouseListener[viewports.length];
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new LatheMouseListener(viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
			mouseMotionListeners[i] = new LatheMouseMotionListener(viewports[i]);
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
	
	private int getSegmentCount() {
		return segmentsAttr.getInt() + 1;
	}
	
	private void computeLathedVertices() {
		boolean reuse = true;
		if (lathedPoints.length != getSegmentCount()) {
			lathedPoints = new Point3d[getSegmentCount()][chain.length];
			lathedVertices = new BaseVertex[getSegmentCount()][chain.length];
			reuse = false;
		}
		if (lathedPoints[0].length != chain.length) {
			lathedPoints = new Point3d[getSegmentCount()][chain.length];
			lathedVertices = new BaseVertex[getSegmentCount()][chain.length];
			reuse = false;
		}
		if (!reuse) {
			for (Point3d[] p : lathedPoints) {
				for (int i = 0; i < p.length; i++) {
					p[i] = new Point3d();
				}
			}
			for (BaseVertex[] v : lathedVertices) {
				for (int i = 0; i < v.length; i++) {
					v[i] = new BaseVertex(Main.getInstance().getActiveModel());
				}
			}
		}
		
		lastSnap[0] = snap[0];
		lastSnap[1] = snap[1];
		Operations.getLathedVertices(
				Main.getInstance().getSelection().getSdsModel().getSds(),
				chain,
				axisStart,
				axisEnd,
				epsilonAttr.getDouble(),
				angleAttr.getDouble(),
				lathedPoints,
				snap
			);
		
		
		if (lastStartEdge != startEdge || lastSnap[0] != snap[0] || lastSnap[1] != snap[1]) {
			lathedSds = new SdsModel(new Sds(null));
			Operations.lathe(lathedSds, lathedPoints, lathedVertices, latheMaterial, null);
		} else {
			for (int i = 0; i < lathedPoints.length; i++) {
				for (int j = 0; j < lathedPoints[i].length; j++) {
					lathedVertices[i][j].setPosition(lathedPoints[i][j]);
				}
			}
		}
		
		lastStartEdge = startEdge;
		latheValid = true;
		
		
	}
	
	public void drawOverlay(Viewport viewport) {
		
		GL gl = viewport.getGL();
		viewport.spatialMode(gl);
		
		if (hitObject != null) {
			
			viewport.resetModelviewMatrix(gl);
			gl.glDisable(GL_DEPTH_TEST);
			gl.glEnable(GL_CULL_FACE);
			gl.glDepthMask(false);
			
//			viewport.getViewDef().configureTransformUtil(transformUtil);
//			hitObject.node.getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
//			viewport.setModelViewMatrix(transformUtil);
				
			viewport.drawSelection(gl, hitSelection, new Color4f(0.5f, 0.5f, 1.0f, 0.5f));
	
			gl.glDisable(GL_LIGHTING);
			gl.glLineWidth(1);
			gl.glEnable(GL_LINE_STIPPLE);
			gl.glLineStipple(2, (short) 0xfe10);
			gl.glColor3f(1, 1, 0);
			gl.glBegin(GL_LINES);
			int axisScale = 10;
			gl.glVertex3d(
				axisScale * axisEnd.x - (axisScale - 1) * axisStart.x,
				axisScale * axisEnd.y - (axisScale - 1) * axisStart.y,
				axisScale * axisEnd.z - (axisScale - 1) * axisStart.z
			);
			gl.glVertex3d(
				axisScale * axisStart.x - (axisScale - 1) * axisEnd.x,
				axisScale * axisStart.y - (axisScale - 1) * axisEnd.y,
				axisScale * axisStart.z - (axisScale - 1) * axisEnd.z
			);gl.glEnd();
			gl.glDisable(GL_LINE_STIPPLE);
			gl.glColor3f(0.5f, 0.5f, 1.0f);
			gl.glLineWidth(1);
			if (chain != null) {
				
				Point3d p = new Point3d();
				gl.glBegin(GL_LINE_STRIP);
				for (BaseVertex vertex : chain) {
					vertex.getPosition(p);
					gl.glVertex3d(p.x, p.y, p.z);
				}
				gl.glEnd();
				
				computeLathedVertices();
				
				int segments = getSegmentCount();
				int points = chain.length;
				
				gl.glEnable(GL_BLEND);
				gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				
				if (!previewLimitAttr.getBoolean()) {
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
				}
				gl.glEnable(GL_LIGHTING);
				
				viewport.setMaterial(gl, GL_FRONT, latheMaterial.getGlMaterial());
				
				if (previewLimitAttr.getBoolean()) {
					viewport.drawSds(gl, lathedSds.getSds(), false, true, true, 1, 0, lineColor, lineMaterial.getGlMaterial());
				} else {
					viewport.drawSds(gl, lathedSds.getSds(), true, false, false, 0, 0, lineColor, lineMaterial.getGlMaterial());
				}
				gl.glDisable(GL_BLEND);
				gl.glDisable(GL_LIGHTING);
				if (!drag) {
					viewport.rasterMode(gl);
					viewport.drawString(gl, "doubleclick to lathe", mouseX, mouseY);
					viewport.spatialMode(gl);
				}
			}
			gl.glLineWidth(1);
			
			gl.glDisable(GL_CULL_FACE);
			gl.glEnable(GL_DEPTH_TEST);
			gl.glDepthMask(true);
		}
	}
	
	private void updateSelection(Selection selection, HitObject hitObject) {
		if (hitObject instanceof MouseSelector.HitVertex) {
			selection.setVertex(((HitVertex) hitObject).vertex, null);
		} else if (hitObject instanceof MouseSelector.HitEdge) {
			selection.setEdge(((HitEdge) hitObject).halfEdge, null);
		} 
	}
	
	private class LatheMouseListener extends MouseAdapter {
		private final Viewport viewport;
		
		LatheMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (hitObject == null) {
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON1) {
				long t = System.currentTimeMillis();
				boolean doubleClick = (t - clickTime < doubleClickTime);
				clickTime = t;
				if (!wasDragged && doubleClick) {
					lathedPoints = new Point3d[0][0];
					lathedVertices = new BaseVertex[0][0];
					computeLathedVertices();
					SdsModel sdsModel = Main.getInstance().getActiveModel();
					List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>();
					Main.getInstance().getSelection().clear(editList);
					Operations.lathe(sdsModel, lathedPoints, lathedVertices, Main.getInstance().getDefaultMaterial(), editList);
					
					HalfEdge strayEdge = startEdge;
					while (strayEdge != null) {
						HalfEdge nextEdge = sdsModel.getSds().getNextStrayEdge(strayEdge);
						sdsModel.getSds().removeSegment(editList, strayEdge);
						strayEdge = nextEdge;
						if (strayEdge == startEdge) {
							break;
						}
					}
					Main.getInstance().getUndoManager().addEdit("lathe", editList);
					Main.getInstance().repaintViewports();
					startEdge = null;
					lastStartEdge = null;
					chain = null;
					latheValid = false;
					drag = false;
				} else {
					snapPointer(viewport);
					Selection selection = Main.getInstance().getSelection();
					updateSelection(selection, hitObject);
					selection.getTransformable().begin();
					drag = true;
					wasDragged = false;
					localStart.set(hitObject.screenPosition);
					viewport.projectFromScreen(localStart, localStart);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 && chain != null) {
				if (wasDragged) {
					List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>();
					Selection selection = Main.getInstance().getSelection();
					selection.getTransformable().end(editList);
					Main.getInstance().getSelection().clear(editList);
					Main.getInstance().getUndoManager().addEdit("move vertices", editList);
					Main.getInstance().repaintViewports();
					drag = false;
				}
			}
		}		
		
//		@Override
//		public void mouseClicked(MouseEvent e) {
//			if (e.getButton() != MouseEvent.BUTTON1) {
//				return;
//			}
//			
//		}
	}
	
	private class LatheMouseMotionListener extends MouseMotionAdapter {
		private final Viewport viewport;
		Point3d mouse = new Point3d();
		Vector3d vector = new Vector3d();
		
		LatheMouseMotionListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (drag) {
				System.out.println("m: " + mouseX + "," + mouseY + " e:" + e.getX() + "," + e.getY());
				if (hitPoint != null && hitPoint.x == e.getX() && hitPoint.y == e.getY()) {
					hitPoint = null;
					System.out.println("# " + wasDragged);
					return;
				}
				if (e.getX() == mouseX && e.getY() == mouseY) {
					System.out.println("* " + wasDragged);
					return;
				}
				mouse.set(e.getX(), e.getY(), hitObject.screenPosition.z);
				viewport.projectFromScreen(mouse, mouse);
				vector.sub(mouse, localStart);
				Selection selection = Main.getInstance().getSelection();
				selection.getTransformable().translate(vector);
//				Main.getInstance().syncRepaintViewport(viewport);
				computeLathedVertices();
				viewport.redrawOverlays();
				wasDragged = true;
			}
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
//			HitEdge hitEdge = (HitEdge) MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 64, sdsModel, 0, Sds.Type.STRAY_EDGE);
//			if (hitEdge == null) {
//				chain = null;
//			} else {
//				Sds sds = sdsModel.getSds();
//				startEdge = sds.getStart(hitEdge.halfEdge);
//				chain = sds.getChain((BaseVertex) startEdge.getVertex());
//			}
//			highlight(viewport);
			
			Sds sds = sdsModel.getSds();
			
			if (sdsModel != null) {
				int level = sdsModel.getEditLevelAttribute().getInt();
				int selectionType = Sds.Type.STRAY_VERTEX | Sds.Type.STRAY_EDGE;
				hitObject = MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 64, sdsModel, level, selectionType, null);
				if (hitObject != null) {
					
						HalfEdge hitEdge = null;
						if (hitObject instanceof HitVertex) {
							AbstractVertex v = ((HitVertex) hitObject).vertex;
							hitEdge = v.getEdges()[v.getEdges().length - 1];
						} else {
							hitEdge = ((HitEdge) hitObject).halfEdge;
						}
						updateSelection(hitSelection, hitObject);
						startEdge = sds.getStart(hitEdge);
						chain = sds.getChain((BaseVertex) startEdge.getVertex());
					
				} else {
					chain = null;
				}
			}
			viewport.redrawOverlays();
		}
	}
	
	private void snapPointer(Viewport viewport) {
		Point point = new Point((int) Math.round(hitObject.screenPosition.x), (int) Math.round(hitObject.screenPosition.y));
		mouseX = point.x;
		mouseY = point.y;
		hitPoint = new Point(point);
		SwingUtilities.convertPointToScreen(point, viewport.getComponent());
		Main.getInstance().getRobot().mouseMove(point.x, point.y);
	}
}
