package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public final class DeleteBoneAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DeleteBoneAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/deletebone.png")));
		putValue(Action.SHORT_DESCRIPTION,"Delete bone");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && selection.getClass() == BoneSelection.getBoneSelectionClass()) {
			((BoneSelection)selection).getBone().delete();
			MainFrame.getInstance().setSelection(null);
			MainFrame.getInstance().getJPatchScreen().full_update();
		}
	}
}
