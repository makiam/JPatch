package jpatch.auxilary;

import javax.vecmath.*;

public class Utils3D {
	private static final Vector3f v3X = new Vector3f(1,0,0);
	private static final Vector3f v3Y = new Vector3f(0,1,0);
	private static final Vector3f v3Z = new Vector3f(0,0,1);
	
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
		
		Point3f P = new Point3f(p0);
		v.scale(b);
		P.add(v);
		return P;
	}
	
	public static Vector3f perpendicularVector(Vector3f v0) {
		Vector3f vn = new Vector3f();
		if (v0.lengthSquared() == 0) {
			return vn;
		} else {
			float ax = Math.abs(v0.x);
			float ay = Math.abs(v0.y);
			float az = Math.abs(v0.z);
			float dm = Math.min(ax,Math.min(ay,az));
			if (ax == dm) {
				vn.cross(v0,v3X);
			} else if (ay == dm) {
				vn.cross(v0,v3Y);
			} else {
				vn.cross(v0,v3Z);
			}
			vn.normalize();
			return vn;
		}
	}
}
