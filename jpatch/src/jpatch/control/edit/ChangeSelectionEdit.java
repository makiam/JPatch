package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.boundary.selection.*;

/**
 * Use this class for changing selections (with the default tool)
 */

public class ChangeSelectionEdit extends JPatchAbstractUndoableEdit {
	private NewSelection selection;
	
	public ChangeSelectionEdit(NewSelection selection) {
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
		NewSelection dummy = selection;
		selection = MainFrame.getInstance().getSelection();
		MainFrame.getInstance().setSelection(dummy);
	}
	
	public void dump(String prefix) {
		System.out.println(prefix + getClass().getName() + " \"" + name() + "\" (" + selection + ")");
	}
}
