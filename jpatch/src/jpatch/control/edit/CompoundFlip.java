package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.entity.*;

public class CompoundFlip extends JPatchCompoundEdit implements JPatchRootEdit {
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	public CompoundFlip(ControlPoint[] acp, Point3f p3pivot, int axis, boolean pivot, boolean local) {
		addEdit(new AtomicMoveControlPoints(acp));
		for (int c = 0; c < acp.length; c++) {
			Point3f position = acp[c].getPosition();
			if (pivot) position.sub(p3pivot);
			switch (axis) {
				case X: position.x = -position.x;
					break;
				case Y: position.y = -position.y;
					break;
				case Z: position.z = -position.z;
					break;
			}
			if (pivot) position.add(p3pivot);
//			acp[c].invalidateTangents();
		}
		//addEdit(new MoveControlPointsEdit(MoveControlPointsEdit.FLIP, acp));
	}
	
	public String getName() {
		return "flip controlpoints";
	}
}
