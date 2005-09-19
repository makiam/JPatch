package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.*;

public class AtomicAddPatch extends JPatchAtomicEdit {
	
	private Patch patch;
	
	public AtomicAddPatch(Patch patch) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + patch + ")");
		this.patch = patch;
		MainFrame.getInstance().getModel().addPatch(patch,null);
	}
	
	public String getPresentationName() {
		return "add patch";
	}
	
	public void undo() {
		MainFrame.getInstance().getModel().removePatch(patch,null);
	}
	
	public void redo() {
		MainFrame.getInstance().getModel().addPatch(patch,null);
	}
	
	public int sizeOf() {
		return 8 + 4; // FIXME: add patch size
	}
}
