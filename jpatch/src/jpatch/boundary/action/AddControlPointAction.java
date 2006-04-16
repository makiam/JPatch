package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;
import jpatch.boundary.tools.*;
import jpatch.boundary.ui.LockingButtonGroup;
import jpatch.control.edit.*;

public final class AddControlPointAction extends AbstractAction {
	 public void actionPerformed(ActionEvent actionEvent) {
		//MainFrame.getInstance().getJPatchScreen().setTool(null);
		
//		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.ADD) {
			if (MainFrame.getInstance().getSelection() != null) {
				JPatchActionEdit edit = new JPatchActionEdit("Clear selection, clear tool");
				edit.addEdit(new AtomicChangeSelection(null));
				edit.addEdit(new AtomicChangeTool(null));
				MainFrame.getInstance().getUndoManager().addEdit(edit, true);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
//			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
			MainFrame.getInstance().getJPatchScreen().setMouseListener(new AddControlPointMouseAdapter());
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
			MainFrame.getInstance().clearDialog();
			((LockingButtonGroup) Actions.getInstance().getButtonGroup("mode")).beginTemporaryAction();
//			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.ADD);
//		} else {
//			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTool(new DefaultTool()), true);
//			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.DEFAULT);
//		}
	}
}

