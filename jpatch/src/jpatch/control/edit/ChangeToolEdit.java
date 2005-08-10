package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.boundary.tools.*;

/**
 * Changes the tool
 */

public class ChangeToolEdit extends JPatchAbstractUndoableEdit {
	private JPatchTool tool;
	
	public ChangeToolEdit(JPatchTool tool) {
		this.tool = tool;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		JPatchTool dummy = tool;
		tool = MainFrame.getInstance().getJPatchScreen().getTool();
		MainFrame.getInstance().getJPatchScreen().setTool(dummy);
		if (dummy != null) {
			MainFrame.getInstance().getMeshToolBar().selectButton(dummy.getButton());
		} else {
			//MainFrame.getInstance().getMeshToolBar().reset();
		}
	}
}
