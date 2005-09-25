package jpatch.control.edit;

import jpatch.entity.*;

/**
 * Use this class for changing morphs
 */

public class AtomicFlipPatch extends JPatchAtomicEdit {
	private Patch patch;
	
	public AtomicFlipPatch(Patch patch) {
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
	
	public int sizeOf() {
		return 8 + 4;
	}
}
