package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.control.edit.*;

public final class ComputePatchesAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
		JPatchActionEdit edit = new JPatchActionEdit("compute patches");
		MainFrame.getInstance().getModel().computePatches(edit);
		if (edit.isValid()) {
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}

