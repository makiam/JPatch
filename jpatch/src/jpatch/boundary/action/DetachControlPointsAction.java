package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.control.edit.*;
import jpatch.entity.*;

public final class DetachControlPointsAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DetachControlPointsAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/detach.png")));
		putValue(Action.SHORT_DESCRIPTION,"Detach points");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null && ps.isSingle()) {
			if (ps.isCurve()) {
				//MainFrame.getInstance().getUndoManager().addEdit(new FullyDetachControlPointEdit(ps.getControlPoint()));
				MainFrame.getInstance().getUndoManager().addEdit(CorrectSelectionsEdit.detachPoint(ps.getControlPoint()));
				MainFrame.getInstance().getJPatchScreen().update_all();
			} else {
				ControlPoint[] acp = ps.getControlPoint().getStack();
				JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit("detach");
				for (int i = 0; i < acp.length; i++) {
					//compoundEdit.addEdit(new FullyDetachControlPointEdit(acp[i]));
					compoundEdit.addEdit(CorrectSelectionsEdit.detachPoint(ps.getControlPoint()));
				}
				MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		}
	}
}