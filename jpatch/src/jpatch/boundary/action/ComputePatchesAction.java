package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.control.edit.*;

public final class ComputePatchesAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ComputePatchesAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/gear.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("compute patches"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit("compute Patches");
		MainFrame.getInstance().getModel().computePatches(compoundEdit);
		if (compoundEdit.isValid()) {
			MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}

