package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.tools.*;
import jpatch.boundary.ui.LockingButtonGroup;
import jpatch.entity.*;

public final class RotateAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
//		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.ROTATE) {
			/*
			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
			JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
			RotateDialog rotateDialog = new RotateDialog(compoundEdit);
			MainFrame.getInstance().setDialog(rotateDialog);
			MainFrame.getInstance().getJPatchScreen().addMouseListeners(new RotateMouseAdapter(rotateDialog, compoundEdit));
			*/
			Selection selection = MainFrame.getInstance().getSelection();
			if (selection != null && (selection.getMap().size() > 1 || selection.getHotObject() instanceof AnimObject)) {
				//MainFrame.getInstance().getJPatchScreen().setTool(new RotateTool());
				MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTool(Tools.rotateTool));
				((LockingButtonGroup) Actions.getInstance().getButtonGroup("mode")).beginTemporaryAction();
//				MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.ROTATE);
			}
//			} else {
//				MainFrame.getInstance().getMeshToolBar().reset();
//			}
//		} else {
//			MainFrame.getInstance().getMeshToolBar().reset();
//		}
	}
}

