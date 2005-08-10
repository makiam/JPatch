package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public final class AutoMirrorAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5212458132874240831L;
	public AutoMirrorAction() {
		super("auto mirror");
		//putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("clone"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null && !ps.isSingle()) {
			//PointSelection newPs = MainFrame.getInstance().getModel().clone(ps.getControlPointArray());
			//MainFrame.getInstance().setSelection(newPs);
			JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
			compoundEdit.addEdit(new ExtendSelectionEdit(ps, true));
			compoundEdit.addEdit(new AutoMirrorEdit(ps));
			if (compoundEdit.size() > 0) {
				MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
			}
			//MainFrame.getInstance().setSelection(null);
			MainFrame.getInstance().getModel().computePatches(compoundEdit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
