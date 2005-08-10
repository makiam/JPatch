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
		p3Pivot.set(pivot);
		this.rotateTool = rotateTool;
		//swap();
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
		float x = p3Pivot.x;
		float y = p3Pivot.y;
		float z = p3Pivot.z;
		Point3f p3 = ps.getPivot();
		p3Pivot.set(p3);
		p3.set(x,y,z);
		if (rotateTool != null) {
			rotateTool.setRadius();
		}
	}
}
