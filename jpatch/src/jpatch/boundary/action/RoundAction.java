package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.entity.*;

public final class RoundAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RoundAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/round.png")));
		putValue(Action.SHORT_DESCRIPTION,"Round");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null && ps.isCurve()) {
			MainFrame.getInstance().getUndoManager().addEdit(new ChangeControlPointTangentModeEdit(ps.getControlPoint(),ControlPoint.JPATCH_G1));
			MainFrame.getInstance().getJPatchScreen().update_all();
		} else if (ps != null) {
			JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
			ControlPoint[] acp = ps.getControlPointArray();
			for (int i = 0; i < acp.length; i++) {
				ControlPoint[] stack = acp[i].getStack();
				for (int j = 0; j < stack.length; j++) {
					compoundEdit.addEdit(new ChangeControlPointTangentModeEdit(stack[j],ControlPoint.JPATCH_G1));
				}
			}
			MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
		ControlPoint.setDefaultMode(ControlPoint.JPATCH_G1);
	}
}
