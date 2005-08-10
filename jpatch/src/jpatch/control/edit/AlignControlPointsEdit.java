package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.entity.*;

public class AlignControlPointsEdit extends JPatchCompoundEdit {
	public static final int XPLANE = 1;
	public static final int YPLANE = 2;
	public static final int ZPLANE = 3;
	
	public AlignControlPointsEdit(ControlPoint[] controlPoints, int plane, float value) {
		addEdit(new NewMoveControlPointsEdit(controlPoints));
		for (int i = 0; i < controlPoints.length; i++) {
			Point3f p3 = controlPoints[i].getPosition();
			switch (plane) {
				case XPLANE:
					p3.x = value;
					break;
				case YPLANE:
					p3.y = value;
					break;
				case ZPLANE:
					p3.z = value;
					break;
			}
			controlPoints[i].setPosition(p3);
		}
	}
}

