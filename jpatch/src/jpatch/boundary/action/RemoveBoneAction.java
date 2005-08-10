package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public final class RemoveBoneAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RemoveBoneAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/removebone.png")));
		putValue(Action.SHORT_DESCRIPTION,"Remove bone");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && selection.getClass() == BoneSelection.getBoneSelectionClass()) {
			((BoneSelection)selection).getBone().remove();
			MainFrame.getInstance().setSelection(null);
			MainFrame.getInstance().getJPatchScreen().full_update();
		}
	}
}
