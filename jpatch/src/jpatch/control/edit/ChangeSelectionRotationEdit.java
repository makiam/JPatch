package jpatch.control.edit;

import javax.vecmath.*;

import jpatch.boundary.tools.*;

/**
 * Use this class for changing selections (with the default tool)
 */

public class ChangeSelectionRotationEdit extends JPatchAbstractUndoableEdit {
	private Matrix3f m3Rot = new Matrix3f();
	private float fAlpha;
	private PointSelection ps;
	private static Matrix3f m3Dummy = new Matrix3f();
	private RotateTool rotateTool;
	
	//public ChangeSelectionRotationEdit(PointSelection ps, Matrix3f rotation) {
	//	this(ps,rotation,null);
	//}
	
	public ChangeSelectionRotationEdit(PointSelection ps, Matrix3f rotation, float alpha, RotateTool rotateTool) {
		//System.out.println("ChangeSelectionRotationEdit");
		this.ps = ps;
		m3Rot.set(rotation);
		fAlpha = alpha;
		this.rotateTool = rotateTool;
		if (rotateTool != null) {
			float dummy = rotateTool.getAlpha();
			rotateTool.setAlpha(fAlpha);
			fAlpha = dummy;
		}
		swap();
	}
	
	public String getName() {
		return "rotate";
	}
	
	public void undo() {
		swap();
		//System.out.println("undo rotate selection");
		//MainFrame.getInstance().getJPatchScreen().setTool(new RotateTool());
	}
	
	public void redo() {
		swap();
		//System.out.println("redo rotate selection");
		//MainFrame.getInstance().getJPatchScreen().setTool(new RotateTool());
	}
	
	private void swap() {
		Matrix3f r = ps.getRotation();
		m3Dummy.set(r);
		r.set(m3Rot);
		m3Rot.set(m3Dummy);
		//System.out.println(r);
		//System.out.println(m3Rot);
		if (rotateTool != null) {
			float dummy = rotateTool.getAlpha();
			rotateTool.setAlpha(fAlpha);
			//System.out.println(fAlpha + " " + dummy);
			fAlpha = dummy;
			rotateTool.reInit(ps);
		}
	}
}
