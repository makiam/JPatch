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
		private AddCurveMouseMotionListener mml;
		private KeyListener kl;
		private List<JPatchUndoableEdit> editList;
		
		AddCurveMouseListener(Viewport viewport) {
			this.viewport = viewport;
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			switch(e.getButton()) {
			case MouseEvent.BUTTON1:
				assert mml == null;
				assert kl == null;
				editList = new ArrayList<JPatchUndoableEdit>();
				ControlPoint weldTo = viewport.getControlPointAt(e.getX(), e.getY(), Main.getInstance().getActiveModel(), null);
				if (weldTo != null) {
					Point3d p3 = new Point3d();
					weldTo.getPos(p3);
					Point p = new Point();
					viewport.get2DPosition(p3, p);
					if (p.x != e.getX() || p.y != e.getY()) {
						SwingUtilities.convertPointToScreen(p, viewport.getComponent());
						Robot robot = Main.getInstance().getRobot();
						robot.mouseMove(p.x, p.y);
						robot.mousePress(InputEvent.BUTTON1_MASK);
						return;
					}
				}
				mml = new AddCurveMouseMotionListener(viewport, weldTo, e.getX(), e.getY(), editList);
				kl = new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_SPACE) {
							e.consume();
							weld(mml.mx, mml.my, attachOnly(e));
						}
					}
				};
				
				viewport.getComponent().addMouseMotionListener(mml);
				viewport.getComponent().addKeyListener(kl);
				break;
			case MouseEvent.BUTTON3:
				if (mml != null) {
					weld(e.getX(), e.getY(), attachOnly(e));
				}
				break;
			case MouseEvent.BUTTON2:
				Main.getInstance().getActiveModel().xml(System.out, ">>>");	// FIXME remove this
				break;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (mml != null) {
					Model model = Main.getInstance().getActiveModel();
					JPatchUndoManager.getUndoManagerFor(model).addEdit(EditType.ADD_CURVE_SEGMENT, editList);
					removeMotionListener();
//					model.xml(System.out, "");
				}
			}
		}
		
		private void weld(int x, int y, boolean attachOnly) {
			Model model = Main.getInstance().getActiveModel();
			System.out.println("weld " + attachOnly);
			ControlPoint cp = mml.cp;
			ControlPoint weldTo = viewport.getControlPointAt(x, y, model, cp);
			if (weldTo != null) {
				if (attachOnly) {
					EditModel.attachControlPoint(mml.editList, cp, weldTo);
				} else {
					EditModel.weldControlPoint(mml.editList, cp, weldTo);
				}
				removeMotionListener();
				cp.getModel().initControlPoints();
				viewport.getComponent().repaint();	// FIXME for synchronized viewports
				System.out.println("weld ok");
			}
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
		private int mx, my;
		private final Point3d p = new Point3d();
		private ControlPoint weldTo, cp;
		private List<JPatchUndoableEdit> editList;
		
		AddCurveMouseMotionListener(Viewport viewport, ControlPoint weldTo, int x, int y, List<JPatchUndoableEdit> editList) {
			this.viewport = viewport;
			this.weldTo = weldTo;
			ox = x;
			oy = y;
			mx = x;
			my = y;
			this.editList = editList;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			mx = e.getX();
			my = e.getY();
			Model model = Main.getInstance().getActiveModel();
			int dx = mx - ox;
			int dy = my - oy;
			int d2 = dx * dx + dy * dy;
			if (cp == null && d2 > 9) {
				ControlPoint startCp;
				if (weldTo != null) {
					viewport.get3DPosition(mx, my, p);
					if (attachOnly(e)) {
						startCp = new ControlPoint(model);
						cp = new ControlPoint(model);
						startCp.setNext(cp);
						cp.setPrev(startCp);
						EditModel.attachControlPoint(editList, startCp, weldTo);
						editList.add(EditModel.addCurve(startCp));
					} else {
						cp = EditModel.weldTo(editList, weldTo, p.x, p.y, p.z);
					}
				} else {
					viewport.get3DPosition(ox, oy, p);
					startCp = new ControlPoint(model);
					cp = new ControlPoint(model);
					startCp.position.set(p);	
					startCp.setNext(cp);
					cp.setPrev(startCp);
					editList.add(EditModel.addCurve(startCp));
				}
			}
			if (cp != null) {
				cp.position.set(viewport.get3DPosition(mx, my, p));
			}
			model.initControlPoints();
			viewport.getComponent().repaint();	// FIXME for synchronized viewports
		}
	}
	
	private static boolean attachOnly(InputEvent e) {
		return e.isShiftDown();
	}
}
