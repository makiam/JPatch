package com.jpatch.entity;

import javax.vecmath.*;

public interface Transformable {
	public void begin();
	public void rotateTo(Point3d pivot, AxisAngle4d axisAngle);
	public void transform(Matrix4d matrix);
	public void end();
}
