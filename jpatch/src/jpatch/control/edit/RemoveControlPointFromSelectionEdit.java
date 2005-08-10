package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.selection.*;

/**
 * This class is inteded to be used if a ControlPoint has to be removed from a
 * selection because the ControlPoint has been deleted.
 * @see RemoveControlPointsFromSelectionEdit
 */
 
public class RemoveControlPointFromSelectionEdit extends JPatchAbstractUndoableEdit {
	
	private ControlPoint cp;
	private PointSelection ps;
	
	public RemoveControlPointFromSelectionEdit(ControlPoint cp, PointSelection ps) {
		this.cp = cp;
		this.ps = ps;
		redo();
	}
	
	public String name() {
		return "remove cp from selection";
	}
	
	public void undo() {
		ps.addControlPoint(cp);
	}
	
	public void redo() {
		ps.removeControlPoint(cp);
	}
}
