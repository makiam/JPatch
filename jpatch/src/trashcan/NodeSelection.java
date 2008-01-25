package trashcan;

import static com.jpatch.afw.vecmath.TransformUtil.LOCAL;

import java.util.List;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import trashcan.*;

import com.jpatch.afw.control.JPatchUndoableEdit;
import com.jpatch.afw.vecmath.TransformUtil;
import com.jpatch.entity.Transformable;

public class NodeSelection implements Transformable {
	public static final int START = 3;
	public static final int AXIS_ROTATION = 4;
	private final TransformNode transformNode;
	private final TransformUtil transformUtil = new TransformUtil("start", "axisRotation");
	private final Matrix4d matrix = new Matrix4d();
	
	public NodeSelection(TransformNode transformNode) {
		this.transformNode = transformNode;
		
	}
	
	
	
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
		transformNode.getTransform().setMatrix(matrix)
	}

	public void transform(Matrix4d matrix) {
		transformUtil.setSpace2World(LOCAL, START, matrix);
	}
	
	public void begin() {
		transformUtil.setTransform(START, transformNode.getTransform());
		transformUtil.setTransform(LOCAL, transformNode.getTransform());
	}

	public void end(List<JPatchUndoableEdit> editList) {
		// TODO Auto-generated method stub

	}

	

	
	public void sync() {
		transformUtil.setTransform(START, transformNode.getTransform());
		transformUtil.setTransform(LOCAL, transformNode.getTransform());
	}

	

}
