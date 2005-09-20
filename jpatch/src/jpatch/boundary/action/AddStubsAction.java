package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.vecmath.*;

import jpatch.boundary.*;

import jpatch.control.edit.*;
import jpatch.entity.*;

public class AddStubsAction extends AbstractAction {
	
	private static final long serialVersionUID = 1L;
	
	public AddStubsAction() {
		super("add stubs");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		ControlPoint[] acp = ps.getControlPointArray();
		JPatchCompoundEdit edit = new JPatchCompoundEdit();
		ArrayList list = new ArrayList();
		for (int i = 0; i < acp.length; i++) {
			for (ControlPoint cp = acp[i].getHead(); cp != null; cp = cp.getPrevAttached()) {
				if (!cp.isHook() && !cp.isTargetHook() && !cp.isSingle()) {
					ControlPoint cpNext = cp.getNext();
					ControlPoint cpPrev = cp.getPrev();
					Vector3f vector = new Vector3f();
					Point3f pos = new Point3f(cp.getPosition());
					ControlPoint cpNew = new ControlPoint();
					cpNew.setCurve(cp.getCurve());
					if (cpNext == null) {
						vector.sub(cp.getPosition(), cpPrev.getPosition());
						vector.scale(0.5f);
						pos.add(vector);
						cpNew.setPosition(pos);
						edit.addEdit(new AtomicAppendControlPoints(cpNew, cp));
						list.add(cpNew);
					} else if (cpPrev == null) {
						vector.sub(cp.getPosition(), cpNext.getPosition());
						vector.scale(0.5f);
						pos.add(vector);
						cpNew.setPosition(pos);
						edit.addEdit(new AtomicAppendControlPoints(cp, cpNew));
						edit.addEdit(new AtomicChangeCurveStart(cp.getCurve(), cpNew));
						list.add(cpNew);
					}
				}
			}
		}
		if (list.size() > 0) {
			edit.addEdit(new AddControlPointsToSelectionEdit(ps, list));
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
