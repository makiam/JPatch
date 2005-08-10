package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.*;

public class RemovePatchFromModelEdit extends JPatchAbstractUndoableEdit {
	
	private Patch patch;
	
	public RemovePatchFromModelEdit(Patch patch) {
		this.patch = patch;
		patch.remove();
	}
	
	public String name() {
		return "remove patch";
	}
	
	public void undo() {
		MainFrame.getInstance().getModel().addPatch(patch,null);
	}
	
	public void redo() {
		patch.remove();
	}
}
