package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;


public final class CloneAction extends AbstractAction {

	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && !selection.isSingle()) {
			//PointSelection newPs = MainFrame.getInstance().getModel().clone(ps.getControlPointArray());
			//MainFrame.getInstance().setSelection(newPs);
			CompoundClone edit = new CompoundClone(selection.getControlPointArray());
			if (edit.size() > 0) {
				MainFrame.getInstance().getUndoManager().addEdit(edit);
			}
			//MainFrame.getInstance().setSelection(null);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
