package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.entity.*;


public class FlipEdit extends JPatchCompoundEdit {
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	public FlipEdit(PointSelection ps, int axis, boolean pivot, boolean local) {
		ControlPoint[] acp = ps.getControlPointArray();
		addEdit(new NewMoveControlPointsEdit(acp));
		for (int c = 0; c < acp.length; c++) {
			Point3f position = acp[c].getPosition();
			if (pivot) position.sub(ps.getPivot());
			switch (axis) {
				case X: position.x = -position.x;
					break;
				case Y: position.y = -position.y;
					break;
				case Z: position.z = -position.z;
					break;
			}
			if (pivot) position.add(ps.getPivot());
			acp[c].invalidateTangents();
		}
		//addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.FLIP, acp));
	}
}
