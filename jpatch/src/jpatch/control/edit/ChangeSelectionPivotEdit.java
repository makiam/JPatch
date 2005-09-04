package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.boundary.tools.*;
/**
 * Use this class for changing selections (with the default tool)
 */

public class ChangeSelectionPivotEdit extends JPatchAbstractUndoableEdit {
	private Point3f p3Pivot = new Point3f();
	private NewSelection selection;
	private RotateTool rotateTool;
	
	public ChangeSelectionPivotEdit(NewSelection selection, Point3f pivot, RotateTool rotateTool) {
		this.selection = selection;
		this.rotateTool = rotateTool;
		p3Pivot.set(selection.getPivot());
		selection.getPivot().set(pivot);
	}
	
	public String getName() {
		return "pivot";
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		Point3f p = new Point3f(p3Pivot);
		p3Pivot.set(selection.getPivot());
		selection.getPivot().set(p);
		if (rotateTool != null) {
			rotateTool.setRadius();
		}
	}
}
