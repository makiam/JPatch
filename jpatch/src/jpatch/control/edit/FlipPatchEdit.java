package jpatch.control.edit;

import jpatch.entity.*;

/**
 * Use this class for changing morphs
 */

public class FlipPatchEdit extends JPatchAbstractUndoableEdit {
	private Patch patch;
	
	public FlipPatchEdit(Patch patch) {
		this.patch = patch;
		flip();
	}
	
	public void undo() {
		flip(); 
	}
	
	public void redo() {
		flip();
	}
	
	private void flip() {
		patch.flip();
	}
}
