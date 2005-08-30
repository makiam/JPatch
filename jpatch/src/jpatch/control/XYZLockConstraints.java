package jpatch.control;

import javax.vecmath.*;
import jpatch.entity.*;

public class XYZLockConstraints {
	boolean bXLock = false;
	boolean bYLock = false;
	boolean bZLock = false;
	
	//private Point3f p3 = new Point3f();
	
	public void setXLock(boolean xLock) {
		bXLock = xLock;
	}
	
	public void setYLock(boolean yLock) {
		bYLock = yLock;
	}
	
	public void setZLock(boolean zLock) {
		bZLock = zLock;
	}
	
	public void toggleXLock() {
		bXLock = !bXLock;
	}
	
	public void toggleYLock() {
		bYLock = !bYLock;
	}
	
	public void toggleZLock() {
		bZLock = !bZLock;
	}
	
	public boolean isXLock() {
		return bXLock;
	}
	
	public boolean isYLock() {
		return bYLock;
	}
	
	public boolean isZLock() {
		return bZLock;
	}
	
	public void setControlPointPosition(ControlPoint cp, Point3f position) {
		Point3f p3Pos = cp.getPosition();
		//Point3f p3 = new Point3f();
		p3Pos.x = bXLock ? p3Pos.x : position.x;
		p3Pos.y = bYLock ? p3Pos.y : position.y;
		p3Pos.z = bZLock ? p3Pos.z : position.z;
		cp.invalidateTangents();
	}
	
	public void setPointPosition(Point3f point, Point3f newPosition) {
		point.x = bXLock ? point.x : newPosition.x;
		point.y = bYLock ? point.y : newPosition.y;
		point.z = bZLock ? point.z : newPosition.z;
	}
	
	public void constrainMatrix(Matrix4f m) {
		if (bXLock) {
			m.m00 = 1;
			m.m01 = 0;
			m.m02 = 0;
			m.m03 = 0;
		}
		if (bYLock) {
			m.m10 = 0;
			m.m11 = 1;
			m.m12 = 0;
			m.m13 = 0;
		}
		if (bZLock) {
			m.m20 = 0;
			m.m21 = 0;
			m.m22 = 1;
			m.m23 = 0;
		}
	}
	//
	//public void correctPointPosition(Point3f point, Point3f newPosition) {
	//	newPosition.x = bXLock ? point.x : newPosition.x;
	//	newPosition.y = bYLock ? point.y : newPosition.y;
	//	newPosition.z = bZLock ? point.z : newPosition.z;
	//}
}
