package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;

public final class DumpUndoStackAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DumpUndoStackAction() {
		super("Dump undoStack");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getUndoManager().dump();
	}
}

