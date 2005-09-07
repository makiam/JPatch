package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.control.edit.*;

public final class SelectNoneAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SelectNoneAction() {
		super("select none");
		//putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("add point"));
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		//PointSelection ps = MainFrame.getInstance().getPointSelection();
		//if (ps != null) {
		//	boolean selectCurveOnly = (ps.isSingle() && (ps.isCurve() || ps.getControlPoint().getPrevAttached() == null));
		MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeSelection(null));
		MainFrame.getInstance().getJPatchScreen().update_all();
		//}
	}
	
	//public void actionPerformed(ActionEvent actionEvent) {
	//	PointSelection ps = MainFrame.getInstance().getPointSelection();
	//	JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
	//	if (ps != null) {
	//		Collection pointsToAdd = new ArrayList();
	//		Collection pointsToRemove = new ArrayList();
	//		boolean cont = true;
	//		if (ps.isSingle()) {
	//			ControlPoint cpa = ps.getControlPoint();
	//			if (ps.isCurve() || cpa.getPrevAttached() == null) {
	//				for (ControlPoint cp = cpa.getCurve().getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
	//					pointsToAdd.add(cp.getHead());
	//					//ps.addControlPoint(cp.getHead());
	//				}
	//				if (!cpa.isHead()) {
	//					pointsToRemove.add(cpa);
	//					//ps.removeControlPoint(cpa);
	//				}
	//				cont = false;
	//			}
	//		}
	//		if (cont) {
	//			setPoints = new HashSet();
	//			ControlPoint[] acp = ps.getControlPointArray();
	//			for (int i = 0; i < acp.length; selectPoint(acp[i++]));
	//			//ps.getSelectedControlPoints().addAll(setPoints);
	//			pointsToAdd.addAll(setPoints);
	//			pointsToAdd.removeAll(ps.getSelectedControlPoints());
	//		}
	//		compoundEdit.addEdit(new AddControlPointsToSelectionEdit(ps,pointsToAdd));
	//		compoundEdit.addEdit(new RemoveControlPointsFromSelectionEdit(ps,pointsToRemove));
	//		MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
	//		MainFrame.getInstance().getJPatchScreen().update_all();
	//	}
	//}
}
