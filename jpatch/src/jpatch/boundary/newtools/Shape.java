package jpatch.boundary.newtools;

import javax.vecmath.*;

public class Shape {
	private static final int CONE_SECTIONS = 16;
	private static final float S = 0.25f;
	public Point3f[] v;
	public Vector3f[] n;
	public int[] f;
	
	public static Shape createCone() {
		Shape s = new Shape();
		s.v = new Point3f[CONE_SECTIONS * 3 + 1];
		s.n = new Vector3f[CONE_SECTIONS * 3 + 1];
		s.f = new int[(CONE_SECTIONS + 1) * 6];
		
		for (int i = 0; i < CONE_SECTIONS; i++) {
			float x = (float) Math.cos(2 * Math.PI / CONE_SECTIONS * i) * S;
			float y = (float) Math.sin(2 * Math.PI / CONE_SECTIONS * i) * S;
			s.v[i] = new Point3f(x, y, 0);
			s.n[i] = new Vector3f(x, y, S * S);
			s.n[i].normalize();
			s.v[i + CONE_SECTIONS] = new Point3f(0, 0, 1);
			s.n[i + CONE_SECTIONS] = new Vector3f(s.n[i]);
			s.v[i + CONE_SECTIONS * 2] = new Point3f(s.v[i]);
			s.n[i + CONE_SECTIONS * 2] = new Vector3f(0, 0, -1);
		}
		s.v[CONE_SECTIONS * 3] = new Point3f(0, 0, 0);
		s.n[CONE_SECTIONS * 3] = new Vector3f(0, 0, -1);
		
		for (int i = 0, n = 0; i <= CONE_SECTIONS; i++) {
			int ip = (i + 1) % CONE_SECTIONS;
			s.f[n++] = i;
			s.f[n++] = i + CONE_SECTIONS;
			s.f[n++] = ip;
			s.f[n++] = i + CONE_SECTIONS * 2;
			s.f[n++] = ip + CONE_SECTIONS * 2;
			s.f[n++] = CONE_SECTIONS * 3;
		}
		
		return s;
	}
}
