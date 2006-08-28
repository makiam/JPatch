package jpatch.boundary.newtools;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import jpatch.boundary.*;
import jpatch.control.*;
import jpatch.entity.*;

public class AddCurveTool implements JPatchTool {
	private MouseListener ml = new AddCurveMouseListener();
	
	public void registerListeners(Component[] components) {
		for (Component c : components) {
			c.addMouseListener(ml);
		}
	}

	public void unregisterListeners(Component[] components) {
		for (Component c : components) {
			c.removeMouseListener(ml);
		}
	}

	public void draw(ViewDefinition viewDef) {
		// TODO Auto-generated method stub
		
	}

	private static class AddCurveMouseListener extends MouseAdapter {
		private Component component;
		private MouseMotionListener mml;
		private List<JPatchUndoableEdit> editList;
		
		@Override
		public void mousePressed(MouseEvent e) {
			assert component == null;
			assert mml == null;
			if (e.getButton() == MouseEvent.BUTTON1) {
				component = e.getComponent();
				editList = new ArrayList<JPatchUndoableEdit>();
				
				mml = new AddCurveMouseMotionListener(e.getX(), e.getY(), editList);
				component.addMouseMotionListener(mml);
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (component != null) {
					assert mml != null;
					Model model = MainFrame.getInstance().getModel();
					JPatchUndoManager.getUndoManagerFor(model).addEdit(EditType.ADD_CURVE_SEGMENT, editList);
					component.removeMouseMotionListener(mml);
					component = null;
				}
			}
		}
		
	}
	
	private static class AddCurveMouseMotionListener extends MouseMotionAdapter {
		private final int ox, oy;
		private ControlPoint cp;
		private List<JPatchUndoableEdit> editList;
		
		AddCurveMouseMotionListener(int x, int y, List<JPatchUndoableEdit> editList) {
			ox = x;
			oy = y;
			this.editList = editList;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int mx = e.getX();
			int my = e.getY();
			ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) e.getSource());
			if (cp == null && (mx != ox || my != my)) {
				Model model = MainFrame.getInstance().getModel();
				ControlPoint startCp = new ControlPoint(model);
				cp = new ControlPoint(model);
				startCp.position.set(viewDef.get3DPosition(ox, oy));
				
				startCp.setNext(cp);
				cp.setPrev(startCp);
				editList.add(EditModel.addCurve(startCp));
				
			}
			if (cp != null) {
				cp.position.set(viewDef.get3DPosition(mx, my));
				MainFrame.getInstance().getJPatchScreen().single_update(e.getComponent());
			}
		}
	}
}
