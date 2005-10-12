package jpatch.boundary.action;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;

import jpatch.entity.*;
import jpatch.auxilary.*;

public final class InsertControlPointAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public InsertControlPointAction() {
		super("insert point");
		//putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("add point"));
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
//		PointSelection ps = MainFrame.getInstance().getPointSelection();
//		if (ps.isCurve()) {
//			ControlPoint scp = (ps.getDirection()) ? ps.getControlPoint() : ps.getControlPoint().getPrev();
//			if (scp != null && scp.getNext() != null && scp.getChildHook() == null) {
//				ControlPoint cp = new ControlPoint(Bezier.evaluate(scp.getPosition(), scp.getOutTangent(), scp.getNext().getInTangent(), scp.getNext().getPosition(), 0.5f));
//				JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
//				ArrayList patches1 = MainFrame.getInstance().getModel().getPatchesContaining(scp);
//				ArrayList patches2 = MainFrame.getInstance().getModel().getPatchesContaining(scp.getNext());
//				for (Iterator it = patches1.iterator(); it.hasNext();) {
//					Patch patch = (Patch) it.next();
//					if (patches2.contains(patch) && patch.getModel() != null) {
//						compoundEdit.addEdit(new AtomicRemovePatch(patch));
//					}
//				}
//				compoundEdit.addEdit(new AtomicInsertControlPoint(cp, scp));
//				MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
//				MainFrame.getInstance().getJPatchScreen().update_all();
//			}
//		}
		Selection selection = MainFrame.getInstance().getSelection();
		int dir = selection.getDirection();
		if (dir == 0)
			return;
		JPatchActionEdit edit = new JPatchActionEdit("insert point");
		ControlPoint cpA, cpB, cpNew;
		if (dir == 1) {
			cpA = (ControlPoint) selection.getHotObject();
			cpB = cpA.getNext();
		} else {
			// dir == -1
			cpB = (ControlPoint) selection.getHotObject();
			cpA = cpB.getPrev();
		}
		cpNew = new ControlPoint(Bezier.evaluate(cpA.getPosition(), cpA.getOutTangent(), cpB.getInTangent(), cpB.getPosition(), 0.5f));
		edit.addEdit(new AtomicInsertControlPoint(cpNew, cpA));
		for (Iterator it = (new HashSet(MainFrame.getInstance().getModel().getPatchSet())).iterator(); it.hasNext();) {
			Patch patch = (Patch) it.next();
			if (patch.contains(cpA) && patch.contains(cpB))
				edit.addEdit(new AtomicRemovePatch(patch));
		}
		MainFrame.getInstance().getUndoManager().addEdit(edit);
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}

