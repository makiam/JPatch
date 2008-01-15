package com.jpatch.boundary.tools;

import static com.jpatch.afw.vecmath.TransformUtil.*;

import java.awt.event.*;
import java.util.*;

import static javax.media.opengl.GL.*;
import javax.media.opengl.*;
import javax.vecmath.*;

import com.jpatch.afw.control.*;
import com.jpatch.afw.vecmath.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.MoveVertexTool.*;
import com.jpatch.boundary.tools.TranslateTool.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class NormalTool implements JPatchTool {
	MouseListener[] mouseListeners;
	TransformUtil transformUtil = new TransformUtil();
	Map<BaseVertex, VertexNormal> vertexPos = new HashMap<BaseVertex, VertexNormal>();
	BaseVertex vertex;
	
	public void registerListeners(Viewport[] viewports) {
		if (mouseListeners != null) {
			throw new IllegalStateException("already registered");
		}
		mouseListeners = new MouseListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new MoveVertexMouseListener(viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
		}
	}
	
	public void unregisterListeners(Viewport[] viewports) {
		System.out.println("MoveVertexTool unregisterListeners");
		for (int i = 0; i < viewports.length; i++) {
			viewports[i].getComponent().removeMouseListener(mouseListeners[i]);
		}
		mouseListeners = null;
	}

//	public void draw(Viewport viewport) {
//		if (vertex != null) {
//			viewport.getViewDef().configureTransformUtil(transformUtil);
//			VertexNormal vn = vertexPos.get(vertex);
//			
//			Point3f p = new Point3f();
//			Vector3f n = new Vector3f(vn.pNormal);
//			vertex.getPos(p);
//			float radius = 0.5f * (float) (transformUtil.computeNiceRadius(vertex.projectedPos.z, viewport.getComponent().getWidth(), viewport.getComponent().getHeight()) * transformUtil.getCameraScale());
//			n.scale(radius);
//			p.sub(n);
//			
//			transformUtil.transform(LOCAL, p, CAMERA, p);
//			GL gl = ((ViewportGl) viewport).getGl();
//			gl.glDisable(GL_LIGHTING);
//			gl.glColor3f(1, 1, 0);
//			gl.glBegin(GL_LINES);
//			
//			gl.glVertex3f(p.x, p.y, p.z);
//			p.add(n);
//			p.add(n);
//			gl.glVertex3f(p.x, p.y, p.z);
//			
//			gl.glEnd();
//		}
//	}
	
	private class MoveVertexMouseListener extends MouseAdapter {
		private Viewport viewport;
		private MouseMotionListener mouseMotionListener;
		
		MoveVertexMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				Selection selection = Main.getInstance().getSelection();
				MouseSelector.Hit hit = MouseSelector.getVertexAt(viewport, e.getX(), e.getY(), true);
				if (selection.getSelectedVerticesAttribute().contains(hit.object)) {
					vertex = (BaseVertex) hit.object;
					vertexPos.clear();
					for (BaseVertex vertex : selection.getSelectedVerticesAttribute().getElements()) {
						vertexPos.put(vertex, new VertexNormal(vertex));
					}
					mouseMotionListener = new MoveNormalMouseMotionListener(viewport, (SdsModel) hit.node, vertex);
					viewport.getComponent().addMouseMotionListener(mouseMotionListener);
				} 
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (mouseMotionListener != null) {
					vertex = null;
					viewport.getComponent().removeMouseMotionListener(mouseMotionListener);
					Main.getInstance().syncViewports(viewport);
				}
			}
		}
	}
	
	private class MoveNormalMouseMotionListener extends MouseMotionAdapter {
		private final Viewport viewport;
		private final Point3d p0 = new Point3d();
		private final Point3d p1 = new Point3d();
		private final Point3d p0s = new Point3d();
		private final Point3d p1s = new Point3d();
		private final Point3d p = new Point3d();
		private final TransformUtil transformUtil = new TransformUtil();
		private double delta;
		private int axis;
		
//		Point3d pos = new Point3d();
//		Point3d limit = new Point3d();
		
		MoveNormalMouseMotionListener(Viewport viewport, SdsModel sdsModel, BaseVertex vertex) {
			this.viewport = viewport;
			
			VertexNormal vertexNormal = vertexPos.get(vertex);
			
//			vertex.getPosition(p0);
//			vertex.getVertexPoint().getNormal(p1);
//			p1.add(p0);
			
			p0.set(vertexNormal.pStart);
			p1.add(vertexNormal.pStart, vertexNormal.pNormal);
			
			viewport.getViewDef().configureTransformUtil(transformUtil);
			sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
			
			transformUtil.projectToScreen(LOCAL, p0, p0s);
			transformUtil.projectToScreen(LOCAL, p1, p1s);
			
			double dx = Math.abs(p0.x - p1.x);
			double dy = Math.abs(p0.y - p1.y);
			double dz = Math.abs(p0.z - p1.z);
			
			if (dx > dy) {
				if (dx > dz) {
					axis = 0; // x
					delta = p1.x - p0.x;
				} else {
					axis = 2; // z
					delta = p1.z - p0.z;
				}
			} else {
				if (dy > dz) {
					axis = 1; // y
					delta = p1.y - p0.y;
				} else {
					axis = 2; // z
					delta = p1.z - p0.z;
				}
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			p.interpolate(p0s, p1s, Utils3d.closestPointOnLine(p0s.x, p0s.y, p1s.x, p1s.y, e.getX(), e.getY()));
			transformUtil.projectFromScreen(LOCAL, p, p);
			double factor = 0;
			switch (axis) {
			case 0:	// x
				factor = (p.x - p0.x) / delta;
				break;
			case 1:	// y
				factor = (p.y - p0.y) / delta;
				break;
			case 2:	// z
				factor = (p.z - p0.z) / delta;
				break;
			}
			for (BaseVertex v : vertexPos.keySet()) {
				vertexPos.get(v).setFactor(v, factor);
			}
			
			
				
//			System.out.println(p);
//			System.out.println("Pworld  =" + p);
//			p.sub(limit);
//			double n = vertex.valence();
//			p.scale((n + 5) / n);
//			p.add(pos);
//			vertex.getPosition().setTuple(p);
//			sdsModel.getSds().computeLevel2Vertices();
			Main.getInstance().syncRepaintViewport(viewport);
		}
	}
	
	private static class VertexNormal {
		Point3d pStart = new Point3d();
		Vector3d pNormal = new Vector3d();
		
		VertexNormal(BaseVertex v) {
			v.getPosition(pStart);
			v.getVertexPoint().getNormal(pNormal);
			pNormal.normalize();
		}
		
		void setFactor(BaseVertex v, double f) {
			v.setPosition(pStart.x + pNormal.x * f, pStart.y + pNormal.y * f, pStart.z + pNormal.z * f);
		}
	}

	public void draw(Viewport viewport) {
		// TODO Auto-generated method stub
		
	}
}
