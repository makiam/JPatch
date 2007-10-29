package com.jpatch.entity;

import com.jpatch.afw.control.JPatchUndoableEdit;

import java.util.List;

import javax.vecmath.*;

public interface Transformable {
	public void begin();
	public void rotateTo(Point3d pivot, AxisAngle4d axisAngle);
	public void transform(Matrix4d matrix);
	public void end(List<JPatchUndoableEdit> editList);
}
