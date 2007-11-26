package com.jpatch.entity;

import com.jpatch.afw.control.JPatchUndoableEdit;
import com.jpatch.afw.vecmath.*;

import java.util.List;

import javax.vecmath.*;

public interface Transformable {
	/* snychronize to scenegraph */
	public void sync();
	/* begin manipulation */
	public void begin();
	/* rotate */
	public void rotate(Point3d pivot, AxisAngle4d axisAngle);
	/* translate */
	public void translate(Vector3d vector);
	/* scale */
	public void scale(Scale3d scale);
	/* end manipulation */
	public void end(List<JPatchUndoableEdit> editList);
}
