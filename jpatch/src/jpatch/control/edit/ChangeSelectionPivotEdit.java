package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.boundary.selection.*;
import jpatch.boundary.tools.*;
/**
 * Use this class for changing selections (with the default tool)
 */

public class ChangeSelectionPivotEdit extends JPatchAbstractUndoableEdit {
	private Point3f p3Pivot = new Point3f();
	private PointSelection ps;
	private RotateTool rotateTool;
	
	public ChangeSelectionPivotEdit(PointSelection ps, Point3f pivot, RotateTool rotateTool) {
		this.ps = ps;
		this.rotateTool = rotateTool;
		p3Pivot.set(ps.getOldPivot());
		ps.setPivot(pivot);
	}
	
	public String name() {
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
		p3Pivot.set(ps.getPivot());
		ps.setPivot(p);
		if (rotateTool != null) {
			rotateTool.setRadius();
		}
	}
}
