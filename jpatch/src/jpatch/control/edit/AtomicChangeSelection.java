package jpatch.control.edit;

import jpatch.boundary.*;

/**
 * Use this class for changing selections (with the default tool)
 */

public class AtomicChangeSelection extends JPatchAtomicEdit implements JPatchRootEdit {
	private NewSelection selection;
	
	public AtomicChangeSelection(NewSelection selection) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + selection + ")");
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
		return 8 + 4 + selectionSize(selection);
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
