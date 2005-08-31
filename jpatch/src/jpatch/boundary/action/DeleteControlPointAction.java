package jpatch.boundary.action;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.entity.*;

public final class DeleteControlPointAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DeleteControlPointAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/deletepoint.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("delete points"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		NewSelection selection = MainFrame.getInstance().getSelection();
		if (selection != null) {
			JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
			if (selection.getDirection() != 0) {
				return;
				//ControlPoint cp = ps.getControlPoint();
				//if (cp.getNextAttached() != null || cp.getPrevAttached() != null) {
				//	compoundEdit.addEdit(new DetachControlPointEdit(cp));
				//	compoundEdit.addEdit(new RemoveControlPointFromSelectionsEdit(cp));
				//	ArrayList patches = MainFrame.getInstance().getModel().getPatchesContaining(cp);
				//	for (Iterator it = patches.iterator(); it.hasNext();) {
				//		Patch patch = (Patch) it.next();
				//		if (patch.getModel() != null) {
				//			compoundEdit.addEdit(new RemovePatchFromModelEdit(patch));
				//		}
				//	}
				//}
			} else {
				for (Iterator itObj = selection.getObjects().iterator(); itObj.hasNext(); ) {
					Object object = itObj.next();
					if (!(object instanceof ControlPoint))
						continue;
					ControlPoint cp = (ControlPoint) object;
					if (!cp.isHook()) {
						ControlPoint[] acpStack = cp.getStack();
						for (int t = 0; t < acpStack.length; t++) {
							if (acpStack[t].getCurve() != null) {
								/*
								 * check if we have to remove a hook patch
								 */
								ControlPoint hook = acpStack[t].getChildHook();
								while(hook != null) {
									ArrayList patches = MainFrame.getInstance().getModel().getPatchesContaining(hook);
									for (Iterator it = patches.iterator(); it.hasNext();) {
										Patch patch = (Patch) it.next();
										if (patch.getModel() != null) {
											compoundEdit.addEdit(new RemovePatchFromModelEdit(patch));
										}
									}
									hook = hook.getNext();
								}
								if (acpStack[t].getPrev() != null) {
									hook = acpStack[t].getPrev().getChildHook();
									while(hook != null) {
										ArrayList patches = MainFrame.getInstance().getModel().getPatchesContaining(hook);
										for (Iterator it = patches.iterator(); it.hasNext();) {
											Patch patch = (Patch) it.next();
											if (patch.getModel() != null) {
												compoundEdit.addEdit(new RemovePatchFromModelEdit(patch));
											}
										}
										hook = hook.getNext();
									}
								}
								//System.out.println("DeleteControlPointAction deleting point " + acpStack[t].number());
								compoundEdit.addEdit(new DeleteControlPointEdit(acpStack[t]));
								ArrayList patches = MainFrame.getInstance().getModel().getPatchesContaining(acpStack[t]);
								for (Iterator it = patches.iterator(); it.hasNext();) {
									Patch patch = (Patch) it.next();
									if (patch.getModel() != null) {
										compoundEdit.addEdit(new RemovePatchFromModelEdit(patch));
									}
								}
							}
						}
					}
				}
			}
			//if (compoundEdit.size() > 0) {
				compoundEdit.addEdit(new ChangeSelectionEdit(null));
				MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
			//}
			//MainFrame.getInstance().setSelection(null);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}

