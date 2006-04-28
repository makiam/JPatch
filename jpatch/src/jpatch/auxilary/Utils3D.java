package jpatch.auxilary;

import javax.vecmath.*;

public class Utils3D {
	private static final Vector3f v3X = new Vector3f(1,0,0);
//	private static final Vector3f v3Y = new Vector3f(0,1,0);
	private static final Vector3f v3Z = new Vector3f(0,0,1);
//	private static final Vector3f v3X1 = new Vector3f(-1,0,0);
//	private static final Vector3f v3Y1 = new Vector3f(0,-1,0);
//	private static final Vector3f v3Z1 = new Vector3f(0,0,-1);
	
	private static Vector3f v = new Vector3f();
	private static Vector3f w = new Vector3f();
	
	public static Point3f closestPointOnLine(Point3f p0, Point3f p1, Point3f p) {
		v.sub(p1,p0);
		w.sub(p,p0);
		
		float c1 = w.dot(v);
		float c2 = v.dot(v);
		float b = c1/c2;
		b = Math.min(b,1);
		b = Math.max(b,0);
		
		float b1 = 1 - b;
		
		return new Point3f(p0.x * b1 + p1.x * b, p0.y * b1 + p1.y * b, p0.z * b1 + p1.z * b);
	}
	
	public static float positionOnLine(Point3f p0, Point3f p1, Point3f p) {
		v.sub(p1,p0);
		w.sub(p,p0);
		
		float c1 = w.dot(v);
		float c2 = v.dot(v);
		float b = c1/c2;
		return b;
	}
	
	public static Vector3f perpendicularVector(Vector3f v0) {
		Vector3f vn = new Vector3f();
		if (v0.lengthSquared() == 0) {
			return vn;
		} else {
//			System.out.println("perp " + v0);
			float ax = Math.abs(v0.x);
			float ay = Math.abs(v0.y);
			float az = Math.abs(v0.z);
			float dm = Math.max(ax,Math.max(ay,az));
//			System.out.println(dm + " " + Math.signum(ax));
			if (ax == dm) {
				if (v0.x < 0)
					vn.cross(v3Z, v0);
				else
					vn.cross(v0,v3Z);
			} else if (az == dm) {
				if (v0.z < 0)
					vn.cross(v0,v3X);
				else
					vn.cross(v3X, v0);
			} else {
				if (v0.y < 0)
					vn.cross(v0,v3X);
				else
					vn.cross(v3X, v0);
			}
			vn.normalize();
			return vn;
		}
	}
	
	public static boolean perpendicularVector(Vector3f v0, Vector3f vn) {
		if (v0.lengthSquared() == 0) {
			vn.set(0, 0, 0);
			return false;
		} else {
//			System.out.println("perp " + v0);
			float ax = Math.abs(v0.x);
			float ay = Math.abs(v0.y);
			float az = Math.abs(v0.z);
			float dm = Math.max(ax,Math.max(ay,az));
//			System.out.println(dm + " " + Math.signum(ax));
			boolean b = false;
			if (ax == dm) {
				if (Math.signum(v0.x) > 0) {
					vn.cross(v0,v3Z);
					b = true;
				} else {
					vn.cross(v3Z, v0);
				}
			} else if (az == dm) {
				if (Math.signum(v0.z) > 0) {
					vn.cross(v0,v3X);
					b = true;
				} else {
					vn.cross(v3X, v0);
				}
			} else {
				if (Math.signum(v0.y) > 0) {
					vn.cross(v0,v3Z);
					b = true;
				} else {
					vn.cross(v3Z, v0);
				}
			}
			vn.normalize();
			return b;
		}
	}
	
	public static Point3f circumCenter(Point3f A, Point3f B, Point3f C) {
		
		float xba = B.x - A.x;
		float yba = B.y - A.y;
		float zba = B.z - A.z;
		float xca = C.x - A.x;
		float yca = C.y - A.y;
		float zca = C.z - A.z;
		
		Vector3f vBA = new Vector3f(B.x - A.x, B.y - A.y, B.z - A.z);
		Vector3f vCA = new Vector3f(C.x - A.x, C.y - A.y, C.z - A.z);
		float balength = vBA.lengthSquared();
		float calength = vCA.lengthSquared();
		
		Vector3f crossBC = new Vector3f();
		crossBC.cross(vBA, vCA);
		
		float denominator = 0.5f / (crossBC.x * crossBC.x + crossBC.y * crossBC.y + crossBC.z * crossBC.z);
		
		float x = ((balength * yca - calength * yba) * crossBC.z -
				(balength * zca - calength * zba) * crossBC.y) * denominator;
		float y = ((balength * zca - calength * zba) * crossBC.x -
				(balength * xca - calength * xba) * crossBC.z) * denominator;
		float z = ((balength * xca - calength * xba) * crossBC.y -
				(balength * yca - calength * yba) * crossBC.x) * denominator;
		return new Point3f(x + A.x, y + A.y, z + A.z);
	}
	
	/**
	 * Transform (rotate) a Tuple3f using the rotation specified in a unit quaternion
	 * @param t The tuple to be transformed
	 * @param q The qunit uaternion representing the rotation
	 */
	public static void transform(Tuple3f t, Quat4f q) {
		float x = q.y * t.z - t.y * q.z + q.w * t.x;
		float y = q.z * t.x - t.z * q.x + q.w * t.y;
		float z = q.x * t.y - t.x * q.y + q.w * t.z;
		float w = -q.x * t.x - q.y * t.y - q.z * t.z;
		t.x = -y * q.z + q.y * z - w * q.x + q.w * x;
		t.y = -z * q.x + q.z * x - w * q.y + q.w * y;
		t.z = -x * q.y + q.x * y - w * q.z + q.w * z;
	}
}
