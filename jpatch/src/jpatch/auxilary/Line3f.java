package jpatch.auxilary;

import javax.vecmath.*;

public class Line3f {
	public Point3f p3;
	public Vector3f v3;
	
	public Line3f(Point3f P, Vector3f V) {
		p3 = P;
		v3 = V;
	}
	
	public Line3f(Point3f A, Point3f B) {
		p3 = A;
		v3 = new Vector3f(B);
		v3.sub(p3);
	}
	
	public Point3f evaluate(float t) {
		Vector3f v = new Vector3f(v3);
		v.scale(t);
		Point3f p = new Point3f(p3);
		p.add(v);
		return p;
	}
	
	public String toString() {
		return "Line from " + p3.toString() + " vector " + v3.toString();
	}
}
