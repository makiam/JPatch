package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.control.edit.*;

public final class RedoAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RedoAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/redo.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("redo"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		JPatchUndoManager undoManager = MainFrame.getInstance().getUndoManager();
		if (undoManager.canRedo()) {
			undoManager.redo();
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
