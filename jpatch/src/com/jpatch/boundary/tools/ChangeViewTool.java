package com.jpatch.boundary.tools;

import com.jpatch.afw.vecmath.Rotation3d;
import com.jpatch.boundary.*;

import java.awt.event.*;

import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.*;

//import jpatch.entity.*;

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
			
//			viewport.setBirdsEyeView();
			ViewDef viewDef = viewport.getViewDef();
			if (viewDef instanceof OrthoViewDef) {
				OrthoViewDef orthoViewDef = (OrthoViewDef) viewDef;
				double w = viewport.getComponent().getWidth() / 20 * orthoViewDef.getScaleAttribute().getDouble();
				switch (mode) {
				case MOVE:
					orthoViewDef.getTranslationAttribute().setTuple(
							orthoViewDef.getTranslationAttribute().getX() + dx / w,
							orthoViewDef.getTranslationAttribute().getY() - dy / w
					);
					break;
				case ROTATE:
					orthoViewDef.getRotationAttribute().setTuple(
							Math.min(Math.max(orthoViewDef.getRotationAttribute().getX() + 0.25 * dy, -90), 90),
							(orthoViewDef.getRotationAttribute().getY() + 0.25 * dx + 360) % 360
					);
					break;
				case ZOOM:
					double factor = Math.min(Math.max(1 + (dx - dy) / 200.0, 0.2), 5);
					orthoViewDef.getScaleAttribute().setDouble(orthoViewDef.getScaleAttribute().getDouble() * factor);
					break;
				}
			} else if (viewDef instanceof PerspectiveViewDef) {
				PerspectiveViewDef prespectiveViewDef = (PerspectiveViewDef) viewDef;
				switch (mode) {
				case MOVE:
					double f = prespectiveViewDef.getRelativeFocalLength() * viewport.getComponent().getWidth();
					double sx0 = x - viewport.getComponent().getWidth() / 2.0;
					double sy0 = y - viewport.getComponent().getHeight() / 2.0;
					double sx1 = (x - dx) - viewport.getComponent().getWidth() / 2.0;
					double sy1 = (y - dy) - viewport.getComponent().getHeight() / 2.0;
					double l0 = Math.sqrt(sx0 * sx0 + sy0 * sy0 + f * f);
					double l1 = Math.sqrt(sx1 * sx1 + sy1 * sy1 + f * f);
					double x0 = sx0 / l0;
					double y0 = sy0 / l0;
					double x1 = sx1 / l1;
					double y1 = sy1 / l1;
					
					double ax0 = Math.asin(y0);
					double ay0 = Math.asin(x0 / Math.cos(ax0));
					double ax1 = Math.asin(y1);
					double ay1 = Math.asin(x1 / Math.cos(ax1));
					double ax = Math.toDegrees(ax0 - ax1);
					double ay = Math.toDegrees(ay0 - ay1);
//					System.out.println("screen: " + x0 + "/" + y0 + " sphere: " + Math.toDegrees(ax0) + "/" + Math.toDegrees(ay0));
					prespectiveViewDef.getRotationAttribute().setTuple(
							prespectiveViewDef.getRotationAttribute().getX() + ax,
							prespectiveViewDef.getRotationAttribute().getY() + ay,
							prespectiveViewDef.getRotationAttribute().getZ()
					);
					break;
				case ZOOM:
					double factor = Math.min(Math.max(1 + (dx - dy) / 200.0, 0.2), 5);
					prespectiveViewDef.getFocalLengthAttribute().setDouble(prespectiveViewDef.getFocalLengthAttribute().getDouble() * factor);
					break;
				}
			}
			Main.getInstance().repaintViewport(viewport);
		}
	}
}
