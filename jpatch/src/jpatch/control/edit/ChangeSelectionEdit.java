package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.boundary.selection.*;

/**
 * Use this class for changing selections (with the default tool)
 */

public class ChangeSelectionEdit extends JPatchAbstractUndoableEdit {
	private Selection selection;
	
	public ChangeSelectionEdit(Selection selection) {
		this.selection = selection;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		Selection dummy = selection;
		selection = MainFrame.getInstance().getSelection();
		MainFrame.getInstance().setSelection(dummy);
	}
}
