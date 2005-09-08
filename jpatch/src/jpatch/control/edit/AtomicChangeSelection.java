package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.boundary.selection.*;

/**
 * Use this class for changing selections (with the default tool)
 */

public class AtomicChangeSelection extends JPatchAtomicEdit implements JPatchRootEdit {
	private NewSelection selection;
	
	public AtomicChangeSelection(NewSelection selection) {
		this.selection = selection;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	public int sizeOf() {
		return 8 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * selection.getMap().size() * 2);
	}
	
	public String getName() {
		return "change selection";
	}
	
	private void swap() {
		NewSelection dummy = selection;
		selection = MainFrame.getInstance().getSelection();
		MainFrame.getInstance().setSelection(dummy);
	}
}
