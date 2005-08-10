package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public final class ExtrudeAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ExtrudeAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/extrude.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("extrude"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null && !ps.isSingle()) {
			if (CloneCommonEdit.checkForHooks(ps.getControlPointArray())) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "Extrude operation can not be performed bacause the selection contains hooks", "Can't extrude", JOptionPane.ERROR_MESSAGE);
			} else {
				//PointSelection newPs = MainFrame.getInstance().getModel().clone(ps.getControlPointArray());
				//MainFrame.getInstance().setSelection(newPs);
				JPatchCompoundEdit compoundEdit = new NewExtrudeEdit(ps.getControlPointArray());
				if (compoundEdit.size() > 0) {
					MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
				}
				//MainFrame.getInstance().setSelection(null);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		}
	}
}
