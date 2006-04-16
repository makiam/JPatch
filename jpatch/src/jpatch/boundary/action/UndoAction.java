package jpatch.boundary.action;

import javax.swing.*;
import jpatch.control.edit.*;
import java.awt.event.*;
import jpatch.boundary.*;

public final class UndoAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
		JPatchUndoManager undoManager = MainFrame.getInstance().getUndoManager();
		if (undoManager.canUndo()) {
			undoManager.undo();
			if (MainFrame.getInstance().getAnimation() != null)
				MainFrame.getInstance().getAnimation().rethink();
			MainFrame.getInstance().getJPatchScreen().update_all();
			if (MainFrame.getInstance().getTimelineEditor() != null)
				MainFrame.getInstance().getTimelineEditor().repaint();
		}
	}
}
