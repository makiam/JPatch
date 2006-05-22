package jpatch.control.edit;

import jpatch.boundary.*;

public class CompoundAddKeys extends JPatchCompoundEdit implements JPatchRootEdit {
	public String getName() {
		return "Add keys";
	}

	public void redo() {
		super.redo();
		MainFrame.getInstance().getJPatchScreen().update_all();
		MainFrame.getInstance().getTimelineEditor().getViewport().getView().repaint();
	}
	
	public void undo() {
		super.undo();
		MainFrame.getInstance().getJPatchScreen().update_all();
		MainFrame.getInstance().getTimelineEditor().getViewport().getView().repaint();
	}
	
	public void addEdit(JPatchUndoableEdit edit) {
		super.addEdit(edit);
	}
}
