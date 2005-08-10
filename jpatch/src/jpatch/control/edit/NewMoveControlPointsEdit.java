package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.entity.*;

public class NewMoveControlPointsEdit extends JPatchAbstractUndoableEdit {
	//public static final int TRANSLATE = 1;
	//public static final int ROTATE = 2;
	//public static final int SCALE = 3;
	//public static final int ALIGN = 4;
	//public static final int FLIP = 5;
	
	private ControlPoint[] acp;
	private Point3f[] ap3Position;
	private static Point3f p3Cache = new Point3f();
	
	public NewMoveControlPointsEdit(ControlPoint[] controlPoints) {
		//System.out.println(this);
		//System.out.println("NewMoveControlPointsEdit(" + controlPoints + ")");
		//if (MainFrame.getInstance().getMode() == MainFrame.MESH) {
			this.acp = controlPoints;
			ap3Position = new Point3f[acp.length];
			for (int c = 0; c < acp.length; c++) {
				ap3Position[c] = new Point3f(acp[c].getPosition());
				//acp[c].fixPosition();
			}
		//}
	}
	
	public String name() {
		return ("move controlpoints");
	}
	
	public void undo() {
		//System.out.println("NewMoveControlPointsEdit.undo()");
		swap();
		//MainFrame.getInstance().getJPatchScreen().setTool(new DefaultTool());
	}
	
	public void redo() {
		//System.out.println("NewMoveControlPointsEdit.redo()");
		swap();
		//MainFrame.getInstance().getJPatchScreen().setTool(new DefaultTool());
	}
	
	private void swap() {
		//System.out.println(this);
		//System.out.println("swap() " + acp);
		//if (MainFrame.getInstance().getMode() == MainFrame.MESH) {
			for (int c = 0; c < acp.length; c++) {
				//System.out.println(c);
				p3Cache.set(ap3Position[c]);
				ap3Position[c].set(acp[c].getPosition());
				//System.out.println(ap3Position[c] + " " + p3Cache);
				acp[c].setPosition(p3Cache);
				//acp[c].recomputePosition();
			}
			//MainFrame.getInstance().getJPatchScreen().repaint();
		//}
	}
}

			
