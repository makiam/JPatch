package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.entity.*;

public final class ExtrudeAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && !selection.isSingle()) {
			ControlPoint[] acp = selection.getControlPointArray();
			if (AbstractClone.checkForHooks(acp)) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "Extrude operation can not be performed bacause the selection contains hooks", "Can't extrude", JOptionPane.ERROR_MESSAGE);
			} else {
				//PointSelection newPs = MainFrame.getInstance().getModel().clone(ps.getControlPointArray());
				//MainFrame.getInstance().setSelection(newPs);
				CompoundExtrude edit = new CompoundExtrude(selection.getControlPointArray());
				if (edit.size() > 0) {
					MainFrame.getInstance().getUndoManager().addEdit(edit);
				}
				//MainFrame.getInstance().setSelection(null);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		}
	}
}
