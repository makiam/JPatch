package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.selection.*;

/**
 * Use this class for changing selections (with the default tool)
 */

public class ChangeSelectionCPHotEdit extends JPatchAbstractUndoableEdit {
	private ControlPoint cpHot;
	private PointSelection ps;
	
	public ChangeSelectionCPHotEdit(PointSelection ps, ControlPoint cp) {
		this.ps = ps;
		cpHot = cp;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		ControlPoint dummy = cpHot;
		cpHot = ps.getHotCp();
		ps.setHotCp(dummy);
	}
}
