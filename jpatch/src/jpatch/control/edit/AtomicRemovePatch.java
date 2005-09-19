package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.*;

public class AtomicRemovePatch extends JPatchAtomicEdit {
	
	private Patch patch;
	
	public AtomicRemovePatch(Patch patch) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + patch + ")");
		this.patch = patch;
		redo();
	}
	
	public String getName() {
		return "remove patch";
	}
	
	public void undo() {
		MainFrame.getInstance().getModel().addPatch(patch,null);
	}
	
	public void redo() {
		MainFrame.getInstance().getModel().removePatch(patch,null);
	}
	
	public int sizeOf() {
		return 8 + 4; // FIXME: add patch size
	}
}
