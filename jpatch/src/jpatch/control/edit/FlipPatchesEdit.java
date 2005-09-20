package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;
import jpatch.boundary.*;


/**
 * Use this class for changing morphs
 */

public class FlipPatchesEdit extends JPatchAbstractUndoableEdit {
	private List listPatches = new ArrayList();
	
	public FlipPatchesEdit(PointSelection ps) {
		for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
			if (patch.isSelected(ps)) listPatches.add(patch);
		}
		flip();
	}
	
	public void undo() {
		flip(); 
	}
	
	public void redo() {
		flip();
	}
	
	private void flip() {
		for (Iterator it = listPatches.iterator(); it.hasNext(); ) {
			((Patch) it.next()).flip();
		}
	}
}
