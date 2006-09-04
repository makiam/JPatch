package jpatch.boundary.newtools;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.vecmath.Point3d;

import jpatch.boundary.*;
import jpatch.control.*;
import jpatch.entity.*;

public class AddCurveTool implements JPatchTool {
	private MouseListener[] mouseListeners;
	
	public void registerListeners(Viewport[] viewports) {
		mouseListeners = new MouseListener[viewports.length];
		for (int i = 0; i < viewports.length; i++) {
			mouseListeners[i] = new AddCurveMouseListener(viewports[i]);
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

	private static class AddCurveMouseListener extends MouseAdapter {
		private Viewport viewport;
		private MouseMotionListener mml;
		private KeyListener kl;
		private List<JPatchUndoableEdit> editList;
		
		AddCurveMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			assert mml == null;
			assert kl == null;
			switch(e.getButton()) {
			case MouseEvent.BUTTON1:
				editList = new ArrayList<JPatchUndoableEdit>();
				ControlPoint weldTo = viewport.getControlPointAt(e.getX(), e.getY(), test.GlTest.model);
				if (weldTo != null) {
					Point3d p3 = new Point3d();
					weldTo.getPos(p3);
					Point p = new Point();
					viewport.get2DPosition(p3, p);
					if (p.x != e.getX() || p.y != e.getY()) {
						SwingUtilities.convertPointToScreen(p, viewport.getComponent());
						try {
							Robot robot = new Robot();	// FIXME use shared robot instance
							robot.mouseMove(p.x, p.y);
							robot.mousePress(InputEvent.BUTTON1_MASK);
							return;
						} catch (AWTException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				mml = new AddCurveMouseMotionListener(viewport, weldTo, e.getX(), e.getY(), editList);
				kl = new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_SPACE) {
							e.consume();
							weld();
						}
					}
				};
				
				viewport.getComponent().addMouseMotionListener(mml);
				viewport.getComponent().addKeyListener(kl);
				break;
			case MouseEvent.BUTTON3:
				if (mml != null) {
					weld();
				}
				break;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (mml != null) {
					Model model = test.GlTest.model; // FIXME
					JPatchUndoManager.getUndoManagerFor(model).addEdit(EditType.ADD_CURVE_SEGMENT, editList);
					removeMotionListener();
				}
			}
		}
		
		private void weld() {
			System.out.println("weld");
			removeMotionListener();
		}
		
		private void removeMotionListener() {
			viewport.getComponent().removeMouseMotionListener(mml);
			viewport.getComponent().removeKeyListener(kl);
			mml = null;
			kl = null;
		}
	}
	
	private static class AddCurveMouseMotionListener extends MouseMotionAdapter {
		private Viewport viewport;
		private final int ox, oy;
		private final Point3d p = new Point3d();
		private ControlPoint weldTo, cp;
		private List<JPatchUndoableEdit> editList;
		
		AddCurveMouseMotionListener(Viewport viewport, ControlPoint weldTo, int x, int y, List<JPatchUndoableEdit> editList) {
			this.viewport = viewport;
			this.weldTo = weldTo;
			ox = x;
			oy = y;
			this.editList = editList;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int mx = e.getX();
			int my = e.getY();
			Model model = test.GlTest.model;	// FIXME
			if (cp == null && (mx != ox || my != my)) {
				ControlPoint startCp = new ControlPoint(model);
				cp = new ControlPoint(model);
				startCp.position.set(viewport.get3DPosition(ox, oy, p));	
				startCp.setNext(cp);
				cp.setPrev(startCp);
				editList.add(EditModel.addCurve(startCp));
			}
			if (cp != null) {
				cp.position.set(viewport.get3DPosition(mx, my, p));
			}
			model.initControlPoints();
			viewport.getComponent().repaint();	// FIXME for synchronized viewports
		}
	}
}
