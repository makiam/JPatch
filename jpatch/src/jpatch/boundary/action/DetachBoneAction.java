package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public final class DetachBoneAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DetachBoneAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/detachbone.png")));
		putValue(Action.SHORT_DESCRIPTION,"Detach bone");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && selection.getClass() == BoneSelection.getBoneSelectionClass()) {
			((BoneSelection)selection).getBone().setParent(null);
			MainFrame.getInstance().getJPatchScreen().full_update();
		}
	}
}
