package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.*;

public class AddPatchEdit extends JPatchAbstractUndoableEdit {
	
	private Patch patch;
	
	public AddPatchEdit(Patch patch) {
		this.patch = patch;
		MainFrame.getInstance().getModel().addPatch(patch,null);
	}
	
	public String getPresentationName() {
		return "add patch";
	}
	
	public void undo() {
		patch.remove();
	}
	
	public void redo() {
		MainFrame.getInstance().getModel().addPatch(patch,null);
	}
}
