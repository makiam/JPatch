package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.*;

/**
 * This class is inteded to be used if a ControlPoint has to be removed from a
 * selection because the ControlPoint has been deleted.
 * @see RemoveControlPointsFromSelectionEdit
 */
 
public class RemoveControlPointFromSelectionEdit extends JPatchAbstractUndoableEdit {
	
	private ControlPoint cp;
	private Float weight;
	private NewSelection selection;
	
	public RemoveControlPointFromSelectionEdit(ControlPoint cp, NewSelection selection) {
		this.cp = cp;
		this.selection = selection;
		weight = (Float) selection.getMap().get(cp);
		redo();
	}
	
	public String name() {
		return "remove cp from selection";
	}
	
	public void undo() {
		selection.getMap().put(cp, weight);
	}
	
	public void redo() {
		selection.getMap().remove(cp);
	}
	
	public void dump(String prefix) {
		System.out.println(prefix + getClass().getName() + " \"" + name() + "\" (" + selection + ")");
	}
}
