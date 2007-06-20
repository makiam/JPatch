package com.jpatch.boundary.tools;

import com.jpatch.boundary.*;
import com.jpatch.entity.sds.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.media.opengl.GLCanvas;
import javax.vecmath.Point3d;

public class MoveVertexTool implements JPatchTool {
	private MouseListener[] mouseListeners;
	
	public void registerListeners(Viewport[] viewports) {
		mouseListeners = new MouseListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new MoveVertexMouseListener(viewports[i]);
			viewports[i].getComponent().addMouseListener(mouseListeners[i]);
		}
	}

	public void unregisterListeners(Viewport[] viewports) {
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
				TopLevelVertex vertex = MouseSelector.getVertexAt(viewport, e.getX(), e.getY(), Main.getInstance().getActiveSds());
				HalfEdge edge = MouseSelector.getEdgeAt(viewport, e.getX(), e.getY(), Main.getInstance().getActiveSds());
				if (vertex != null) {
					mouseMotionListener = new MoveVertexMouseMotionListener(viewport, vertex);
					viewport.getComponent().addMouseMotionListener(mouseMotionListener);
//					Main.getInstance().setSelectedObject(vertex);
				} else if (edge != null) {
//					Main.getInstance().setSelectedObject(edge);
				} else {
//					Main.getInstance().setSelectedObject(null);
				}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (mouseMotionListener != null) {
					viewport.getComponent().removeMouseMotionListener(mouseMotionListener);
				}
			}
		}
	}
	
	private static class MoveVertexMouseMotionListener extends MouseMotionAdapter {
		private Viewport viewport;
		private TopLevelVertex vertex;
		Point3d p = new Point3d();
//		Point3d pos = new Point3d();
//		Point3d limit = new Point3d();
		
		MoveVertexMouseMotionListener(Viewport viewport, TopLevelVertex vertex) {
			this.viewport = viewport;
			this.vertex = vertex;
			vertex.getPos(p);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			viewport.getMatrix().transform(p);
			viewport.get3DPosition(e.getX(), e.getY(), p);
//			p.sub(limit);
//			double n = vertex.valence();
//			p.scale((n + 5) / n);
//			p.add(pos);
			vertex.getPosition().setTuple(p);
			Main.getInstance().getActiveSds().computeLevel2Vertices();
			((GLCanvas) viewport.getComponent()).display();
		}
	}

}
