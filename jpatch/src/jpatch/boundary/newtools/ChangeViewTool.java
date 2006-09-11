package jpatch.boundary.newtools;

import java.awt.event.*;


import jpatch.boundary.*;
import jpatch.entity.*;

public class ChangeViewTool implements JPatchTool {
	private static enum Mode { MOVE, ZOOM, ROTATE }

	private MouseListener[] mouseListeners;
	private Mode mode;
	
	public static ChangeViewTool createMoveViewTool() {
		return new ChangeViewTool(Mode.MOVE);
	}
	
	public static ChangeViewTool createZoomViewTool() {
		return new ChangeViewTool(Mode.ZOOM);
	}
	
	public static ChangeViewTool createRotateViewTool() {
		return new ChangeViewTool(Mode.ROTATE);
	}
	
	private ChangeViewTool(Mode mode) {
		this.mode = mode;
	}
	
	public void registerListeners(Viewport[] viewports) {
		mouseListeners = new MouseListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new ChangeViewMouseListener(viewports[i]);
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
		// TODO Auto-generated method stub
		
	}
	
	private class ChangeViewMouseListener extends MouseAdapter {
		private Viewport viewport;
		private MouseMotionListener mml;
		
		ChangeViewMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			mml = new ChangeViewMouseMotionListener(viewport, e.getX(), e.getY());
			viewport.getComponent().addMouseMotionListener(mml);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			viewport.getComponent().removeMouseMotionListener(mml);
			mml = null;
		}
	}
	
	private class ChangeViewMouseMotionListener extends MouseMotionAdapter {
		
		private Viewport viewport;
		private int x, y;
//		private double x0, y0;
		
		ChangeViewMouseMotionListener(Viewport viewport, int x, int y) {
			this.viewport = viewport;
			this.x = x;
			this.y = y;
//			switch (mode) {
//			case MOVE:
//				x0 = viewport.viewTranslation.x.get();
//				y0 = viewport.viewTranslation.y.get();
//				break;
//			case ROTATE:
//				x0 = viewport.viewRotation.x.get();
//				y0 = viewport.viewRotation.y.get();
//				break;
//			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int dx = e.getX() - x;
			int dy = e.getY() - y;
			x = e.getX();
			y = e.getY();
			viewport.viewType.set(Viewport.View.BIRDS_EYE);
			switch (mode) {
			case MOVE:
				viewport.viewTranslation.x.set(viewport.viewTranslation.x.get() + 0.1 * dx);
				viewport.viewTranslation.y.set(viewport.viewTranslation.y.get() + 0.1 * dy);
				break;
			case ROTATE:
				viewport.viewRotation.x.set(Math.min(Math.max(viewport.viewRotation.x.get() + 0.5 * dy, -90), 90));
				viewport.viewRotation.y.set((viewport.viewRotation.y.get() + 0.5 * dx + 360) % 360);
				break;
			case ZOOM:
				double factor = Math.min(Math.max(1 + (dx - dy) / 200.0, 0.2), 5);
				viewport.viewScale.set(viewport.viewScale.get() * factor);
				break;
			}
		}
	}
}
