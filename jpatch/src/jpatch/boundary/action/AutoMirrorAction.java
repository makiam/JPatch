package jpatch.boundary.action;

import java.awt.event.*;
import java.util.Iterator;

import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.entity.OLDSelection;
import jpatch.boundary.*;


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
	
		OLDSelection selection = MainFrame.getInstance().getSelection();
		if (selection != null && !selection.isSingle()) {
			//PointSelection newPs = MainFrame.getInstance().getModel().clone(ps.getControlPointArray());
			//MainFrame.getInstance().setSelection(newPs);
			JPatchActionEdit edit = new JPatchActionEdit("auto mirror");
			edit.addEdit(new CompoundExpandSelection(selection));
			edit.addEdit(new CompoundAutoMirror(selection));
			if (edit.size() > 0) {
				MainFrame.getInstance().getUndoManager().addEdit(edit);
			}
			//MainFrame.getInstance().setSelection(null);
//			MainFrame.getInstance().getModel().computePatches(edit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
