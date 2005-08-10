package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public final class CloneAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public CloneAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/clone.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("clone"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null && !ps.isSingle()) {
			//PointSelection newPs = MainFrame.getInstance().getModel().clone(ps.getControlPointArray());
			//MainFrame.getInstance().setSelection(newPs);
			JPatchCompoundEdit compoundEdit = new NewCloneEdit(ps.getControlPointArray());
			if (compoundEdit.size() > 0) {
				MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
			}
			//MainFrame.getInstance().setSelection(null);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
