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

import com.jpatch.afw.control.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MouseSelector.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class AddEdgeTool implements JPatchTool, ViewportOverlay {
	private static ObjectFilter ENDVERTEX_FILTER = new ObjectFilter() {
		public boolean accept(Object o) {
			try {
				return ((AbstractVertex) o).getEdges().length == 1;
			} catch (ClassCastException e) {
				return false;
			}
		}
	};
	
	private static EnumSet<Sds.Type> VERTEX_STRAY_VERTEX = EnumSet.of(Sds.Type.VERTEX, Sds.Type.STRAY_VERTEX);
	private MouseMotionListener[] mouseMotionListeners;
	private MouseListener[] mouseListeners;
	
	private BaseVertex floatingVertex;
	private BaseVertex startVertex;
	private BaseVertex endVertex;
	
	private BaseVertex[] strayFace;
	
	private boolean drag;
	
	private int mouseX, mouseY;
	
	public void draw(Viewport viewport) {
		
	}

	public void registerListeners(Viewport[] viewports) {
		mouseListeners = new MouseListener[viewports.length];
		mouseMotionListeners = new MouseMotionListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new AddEdgeMouseListener(viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
			mouseMotionListeners[i] = new AddEdgeMouseMotionListener(viewports[i]);
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
	
	public void drawOverlay(Viewport viewport) {
//		System.out.println("hitObject = " + hitObject + " distance = " + Math.sqrt(hitObject.distanceSq));
		
		GL gl = viewport.getGL();
		viewport.spatialMode(gl);
		viewport.resetModelviewMatrix(gl);
		
		gl.glDepthMask(false);
		gl.glLineWidth(1);
		
		Point3f p = new Point3f();
		gl.glPointSize(6);
		gl.glColor3f(0.5f, 0.5f, 1.0f);
		if (startVertex != null && (endVertex != null || strayFace == null)) {
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
		
		if (sds.getStrayVertices().contains(startVertex) && endVertex != null && endVertex != floatingVertex) {
			System.out.println("startVertex=" + startVertex + " endVertex=" + endVertex);
			System.out.println("is start of chain: " + sds.isStartOfChain(endVertex));
			if (sds.isStartOfChain(endVertex)) {
				System.out.println("is connected: " + sds.isConnected(startVertex, endVertex));
				if (sds.isConnected(startVertex, endVertex)) {
//					sds.addFace(null, 0, Main.getInstance().getDefaultMaterial(), sds.getLoop(startVertex));
					BaseVertex[] vertices = sds.getChain(startVertex);
					gl.glLineWidth(2);
					gl.glBegin(GL_LINE_LOOP);
					for (BaseVertex vertex : vertices) {
						vertex.getPosition(p);
						gl.glVertex3f(p.x, p.y, p.z);
					}
					gl.glEnd();
					gl.glLineWidth(1);
				}
			}
		}
		
		if (strayFace != null) {
//			gl.glEnable(GL_BLEND);
//			gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			gl.glColor4f(0.5f, 0.5f, 1.0f, 0.5f);
			drawStrayFace(gl, strayFace, p);
			gl.glDisable(GL_BLEND);
			
			viewport.rasterMode(gl);
			viewport.drawString(gl, "doubleclick to add face", mouseX, mouseY);
			viewport.spatialMode(gl);
		}
//		System.out.println(hitVertex);
		
		gl.glDepthMask(true);
	}
	
	private void snapPointer(Viewport viewport, Point3d screenPosition) {
		Point point = new Point((int) Math.round(screenPosition.x), (int) Math.round(screenPosition.y));
		SwingUtilities.convertPointToScreen(point, viewport.getComponent());
		Main.getInstance().getRobot().mouseMove(point.x, point.y);
	}
	
	private void drawStrayFace(GL gl, BaseVertex[] vertices, Point3f p) {
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
		gl.glLineWidth(2);
		gl.glDisable(GL_BLEND);
		gl.glBegin(GL_LINE_LOOP);
		for (BaseVertex vertex : vertices) {
			vertex.getPosition(p);
			gl.glVertex3f(p.x, p.y, p.z);
		}
		gl.glEnd();
		gl.glDisable(GL_BLEND);
	}
	
	private class AddEdgeMouseListener extends MouseAdapter {
		private final Viewport viewport;
		
		AddEdgeMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				return;
			}
			
			if (startVertex == null) {
				startVertex = new BaseVertex(Main.getInstance().getActiveModel());
				TransformUtil transformUtil = new TransformUtil();
				viewport.getViewDef().configureTransformUtil(transformUtil);
//				Main.getInstance().getSelection().getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
				Point3d p = new Point3d(e.getX(), e.getY(), 0);
				transformUtil.projectFromScreen(TransformUtil.WORLD, p, p);
				startVertex.setPosition(p);
			} else {
				Point3d p = new Point3d();
				startVertex.getPosition(p);
				TransformUtil transformUtil = new TransformUtil();
				viewport.getViewDef().configureTransformUtil(transformUtil);
				transformUtil.projectToScreen(TransformUtil.WORLD, p, p);
				snapPointer(viewport, p);
			}
			viewport.redrawOverlays();
			floatingVertex = new BaseVertex(Main.getInstance().getActiveModel());
			drag = true;
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				return;
			}
			
			if (strayFace != null) {
				if (e.getClickCount() == 2) {
			
					Sds sds = Main.getInstance().getSelection().getSdsModel().getSds();
					List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>();
					System.out.println("Dumping faces *BEFORE*");
//					sds.dumpFaces(0);
					System.out.println("---");
					sds.removeStrayFace(editList, strayFace);
					for (int i = 0; i < strayFace.length; i++) {
						int j = (i == 0) ? strayFace.length - 1 : i - 1;
						sds.removeSegment(editList, strayFace[i].getEdge(strayFace[j]));
//						sds.removeStrayEdge(editList, vertex.getEdges()[1]);
					}
					Face face = sds.addFace(editList, sds.getCurrentMaterial(), strayFace);
					System.out.println("Dumping faces *AFTER*");
//					sds.dumpFaces(0);
					Vector3d normal = new Vector3d();
					face.getMidpointNormal(normal);
					TransformUtil transformUtil = new TransformUtil();
					viewport.getViewDef().configureTransformUtil(transformUtil);
//					Main.getInstance().getSelection().getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
					transformUtil.transform(TransformUtil.WORLD, normal, TransformUtil.CAMERA, normal);
					if (normal.z < 0) {
						Collection<Face> faces = new ArrayList<Face>(1);
						faces.add(face);
						sds.flipFaces(editList, faces);
					}
					Main.getInstance().getUndoManager().addEdit("create face", editList);
				} else {
					return;
				}
				
			} else {
			
				Sds sds = Main.getInstance().getSelection().getSdsModel().getSds();
				
				
				
				boolean addFace = false;
				if (sds.getStrayVertices().contains(startVertex) && endVertex != null && endVertex != floatingVertex) {
					System.out.println("is start of chain: " + sds.isStartOfChain(endVertex));
					if (sds.isStartOfChain(endVertex)) {
						System.out.println("is connected: " + sds.isConnected(startVertex, endVertex));
						if (sds.isConnected(startVertex, endVertex)) {
	//						System.out.println("adding face");
							
	//						sds.addFace(null, 0, Main.getInstance().getDefaultMaterial(), sds.getLoop(startVertex));
							addFace = true;
	//						BaseVertex[] vertices = sds.getLoop(startVertex);
						}
					}
				}
				
				List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>();
				if (endVertex != null) {
					sds.addSegment(editList, startVertex, endVertex);
				}
				
				if (addFace) {
					BaseVertex[] chain = sds.getChain(startVertex);
					BaseVertex[] loop = new BaseVertex[chain.length - 1];
					System.arraycopy(chain, 0, loop, 0, loop.length);
					sds.addStrayFace(editList, loop);
				}
				
				if (editList.size() > 0) {
					Main.getInstance().getUndoManager().addEdit("add edge", editList);
				}
//				sds.dumpFaces(0);
				
			}
			startVertex = endVertex;
			endVertex = null;
			strayFace = null;
			Main.getInstance().repaintViewports();
//			viewport.redrawOverlays();
			
			drag = false;
		}
		
	}

	private class AddEdgeMouseMotionListener implements MouseMotionListener {
		private final Viewport viewport;
		
		AddEdgeMouseMotionListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		public void mouseDragged(MouseEvent e) {
			if (!drag) {
				return;
			}
			strayFace = null;
			
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
			HitVertex hitVertex = (HitVertex) MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 64, sdsModel, 0, VERTEX_STRAY_VERTEX, ENDVERTEX_FILTER);
			if (hitVertex != null) {
				endVertex = (BaseVertex) hitVertex.vertex;
			} else {
				endVertex = floatingVertex;
				TransformUtil transformUtil = new TransformUtil();
				viewport.getViewDef().configureTransformUtil(transformUtil);
//				Main.getInstance().getSelection().getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
				Point3d p = new Point3d(e.getX(), e.getY(), 0);
				transformUtil.projectFromScreen(TransformUtil.WORLD, p, p);
				endVertex.setPosition(p);
			}
			
			viewport.redrawOverlays();
		}

		public void mouseMoved(MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
			HitVertex hitVertex = (HitVertex) MouseSelector.getObjectAt(viewport, e.getX(), e.getY(), 64, sdsModel, 0, VERTEX_STRAY_VERTEX, ENDVERTEX_FILTER);
			if (hitVertex != null) {
				startVertex = (BaseVertex) hitVertex.vertex;
			} else {
				startVertex = null;
			}
			
			TransformUtil transformUtil = new TransformUtil();
			viewport.getViewDef().configureTransformUtil(transformUtil);
//			Main.getInstance().getSelection().getNode().getLocal2WorldTransform(transformUtil, TransformUtil.LOCAL);
			Point3d p = new Point3d();
			Polygon polygon = new Polygon();
			polygon.xpoints = new int[256];
			polygon.ypoints = new int[256];
			polygon.npoints = 0;
			double minDistSq = Double.MAX_VALUE;
			strayFace = null;
			for (BaseVertex[] vertices : sdsModel.getSds().getStrayFaces()) {
				double midX = 0, midY = 0;
				for (int i = 0; i < vertices.length; i++) {
					vertices[i].getPosition(p);
					transformUtil.projectToScreen(TransformUtil.WORLD, p, p);
					midX += p.x;
					midY += p.y;
					polygon.xpoints[i] = (int) Math.round(p.x);
					polygon.ypoints[i] = (int) Math.round(p.y);
				}
				polygon.npoints = vertices.length;
				polygon.invalidate();
				if (polygon.contains(e.getX(), e.getY())) {
					midX /= vertices.length;
					midY /= vertices.length;
					double dx = midX - e.getX();
					double dy = midY - e.getY();
					double distSq = dx * dx + dy * dy;
					if (distSq < minDistSq) {
						minDistSq = distSq;
						strayFace = vertices;
					}
				}
			}
			viewport.redrawOverlays();
		}
		
	}
}
