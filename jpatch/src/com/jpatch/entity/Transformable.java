package com.jpatch.entity;

import com.jpatch.afw.control.JPatchUndoableEdit;
import com.jpatch.afw.vecmath.TransformUtil;

import java.util.List;

import javax.vecmath.*;

public interface Transformable {
	/* get TransformUtil */
	public TransformUtil getTransformUtil();
	/* snychronize to scenegraph */
	public void sync();
	/* begin manipulation */
	public void begin();
	/* rotate */
	public void rotateTo(Point3d pivot, AxisAngle4d axisAngle);
	/* transform */
	public void transform(Matrix4d matrix);
	/* end manipulation */
	public void end(List<JPatchUndoableEdit> editList);
}
