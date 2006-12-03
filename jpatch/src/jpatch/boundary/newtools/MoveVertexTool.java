package jpatch.boundary.newtools;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.vecmath.Point3d;

import jpatch.boundary.Main;
import jpatch.boundary.Viewport;
import jpatch.entity.ControlPoint;

import sds.*;

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
				Vertex vertex = viewport.getVertexAt(e.getX(), e.getY(), Main.getInstance().getActiveSds());
				if (vertex != null) {
					mouseMotionListener = new MoveVertexMouseMotionListener(viewport, vertex);
					viewport.getComponent().addMouseMotionListener(mouseMotionListener);
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
		private Vertex vertex;
		Point3d p = new Point3d();
		Point3d pos = new Point3d();
		Point3d limit = new Point3d();
		
		MoveVertexMouseMotionListener(Viewport viewport, Vertex vertex) {
			this.viewport = viewport;
			this.vertex = vertex;
			vertex.referencePosition.get(pos);
			vertex.limitPoint.position.get(limit);
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			vertex.limitPoint.position.get(p);
			viewport.getMatrix().transform(p);
			viewport.get3DPosition(e.getX(), e.getY(), p);
			p.sub(limit);
			double n = vertex.valence();
			p.scale((n + 5) / n);
			p.add(pos);
			vertex.referencePosition.set(p);
			Main.getInstance().getActiveSds().rethinkSlates();
			viewport.getComponent().repaint();	// FIXME for synchronized viewports
		}
	}

}
