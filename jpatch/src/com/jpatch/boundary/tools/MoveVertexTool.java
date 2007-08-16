package com.jpatch.boundary.tools;

import com.jpatch.boundary.*;
import com.jpatch.entity.SdsModel;
import com.jpatch.entity.sds.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.media.opengl.GLCanvas;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

public class MoveVertexTool implements JPatchTool {
	private MouseListener[] mouseListeners;
	
	public void registerListeners(Viewport[] viewports) {
		System.out.println("MoveVertexTool registerListeners");
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

	public void draw(Viewport viewport) {
//		viewport.drawShape(cone);
	}

	private static class MoveVertexMouseListener extends MouseAdapter {
		private Viewport viewport;
		private MouseMotionListener mouseMotionListener;
		
		MoveVertexMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				MouseSelector.Hit hit = MouseSelector.getVertexAt(viewport, e.getX(), e.getY());
//				HalfEdge edge = MouseSelector.getEdgeAt(viewport, e.getX(), e.getY(), Main.getInstance().getActiveSds());
				if (hit.object != null) {
					mouseMotionListener = new MoveVertexMouseMotionListener(viewport, (SdsModel) hit.node, (TopLevelVertex) hit.object);
					viewport.getComponent().addMouseMotionListener(mouseMotionListener);
//					Main.getInstance().setSelectedObject(vertex);
//				} else if (edge != null) {
//					Main.getInstance().setSelectedObject(edge);
//				} else {
//					Main.getInstance().setSelectedObject(null);
				}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (mouseMotionListener != null) {
					viewport.getComponent().removeMouseMotionListener(mouseMotionListener);
					Main.getInstance().syncViewports(viewport);
				}
			}
		}
	}
	
	private static class MoveVertexMouseMotionListener extends MouseMotionAdapter {
		private final Viewport viewport;
		private final TopLevelVertex vertex;
		private final Point3d p = new Point3d();
		private final SdsModel sdsModel;
		double z;
//		Point3d pos = new Point3d();
//		Point3d limit = new Point3d();
		
		MoveVertexMouseMotionListener(Viewport viewport, SdsModel sdsModel, TopLevelVertex vertex) {
			this.viewport = viewport;
			this.vertex = vertex;
			this.sdsModel = sdsModel;
			vertex.getPos(p);
			sdsModel.getTransform().transform(p);
			viewport.getViewDef().transform(p);
			z = p.z;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			p.x = e.getX() - viewport.getComponent().getWidth() / 2;
			p.y = viewport.getComponent().getHeight() / 2 - e.getY();
			p.z = z;
//			System.out.println("Pscreen =" + p);
			viewport.getViewDef().invTransform(p);
			sdsModel.getTransform().invTransform(p);
//			System.out.println("Pworld  =" + p);
//			p.sub(limit);
//			double n = vertex.valence();
//			p.scale((n + 5) / n);
//			p.add(pos);
			vertex.getPosition().setTuple(p);
			sdsModel.getSds().computeLevel2Vertices();
			Main.getInstance().syncRepaintViewport(viewport);
		}
	}
}
