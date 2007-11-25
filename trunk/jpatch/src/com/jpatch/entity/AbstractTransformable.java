package com.jpatch.entity;

import javax.vecmath.*;

import com.jpatch.afw.vecmath.TransformUtil;
import static com.jpatch.afw.vecmath.TransformUtil.*;

public abstract class AbstractTransformable implements Transformable {
	public static final int AXIS_ROTATION = 3;
	public static final int START = 4;
	protected final TransformUtil transformUtil = new TransformUtil("axisRotation", "start");
	protected final Matrix4d matrix = new Matrix4d();
	
	public TransformUtil getTransformUtil() {
		return transformUtil;
	}

	public void rotateTo(Point3d pivot, AxisAngle4d axisAngle) {
		/* set matrix to the rotation matrix specified by axisAngle around specivied pivot */
		matrix.set(axisAngle);
		matrix.m03 = matrix.m00 * pivot.x + matrix.m01 * pivot.y + matrix.m02 * pivot.z;
		matrix.m13 = matrix.m10 * pivot.x + matrix.m11 * pivot.y + matrix.m12 * pivot.z;
		matrix.m23 = matrix.m20 * pivot.x + matrix.m21 * pivot.y + matrix.m22 * pivot.z;
		transformUtil.setSpace2World(LOCAL, START, matrix);
	}

	public void transform(Matrix4d matrix) {
		transformUtil.setSpace2World(LOCAL, START, matrix);
	}

}
