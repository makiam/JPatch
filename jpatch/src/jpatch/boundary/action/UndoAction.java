package jpatch.boundary.action;

import javax.swing.*;
import jpatch.control.edit.*;
import java.awt.event.*;
import jpatch.boundary.*;

public final class UndoAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public UndoAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/undo.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("undo"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		JPatchUndoManager undoManager = MainFrame.getInstance().getUndoManager();
		if (undoManager.canUndo()) {
			undoManager.undo();
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}