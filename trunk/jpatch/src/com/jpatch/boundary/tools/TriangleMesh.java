package com.jpatch.boundary.tools;

import javax.vecmath.*;
import jpatch.boundary.*;
import jpatch.entity.GlMaterial;

public class TriangleMesh {
	public static final int AMBIENT = 0;
	public static final int DIFFUSE = 4;
	public static final int SPECULAR = 8;
	public static final int SHININESS = 12;
	
	private static final float S = 0.25f;
	public final Point3d[] v;
	public final Vector3d[] n;
	public final int[] t;
	public final GlMaterial m;
	
	public TriangleMesh(Point3d[] vertices, Vector3d[] normals, int[] triangles, GlMaterial material) {
		v = vertices;
		n = normals;
		t = triangles;
		m = material;
	}
	
	public static TriangleMesh createCone(int coneSections, GlMaterial material) {
		TriangleMesh s = new TriangleMesh(
				new Point3d[coneSections * 2 + 2],
				new Vector3d[coneSections * 2 + 2],
				new int[coneSections * 3 * 2],
				material
		);
		for (int i = 0; i < coneSections; i++) {
			double x = Math.cos(2 * Math.PI / coneSections * i) * S;
			double y = Math.sin(2 * Math.PI / coneSections * i) * S;
			s.v[i] = new Point3d(x, y, 0);
			s.n[i] = new Vector3d(x, y, S * S);
			s.n[i].normalize();
			s.v[i + coneSections] = new Point3d(s.v[i]);
			s.n[i + coneSections] = new Vector3d(0, 0, -1);
		}
		s.v[coneSections * 2] = new Point3d(0, 0, 1);
		s.n[coneSections * 2] = new Vector3d(0, 0, 1);
		s.v[coneSections * 2 + 1] = new Point3d(0, 0, 0);
		s.n[coneSections * 2 + 1] = new Vector3d(0, 0, -1);
		for (int i = 0, n = 0; i < coneSections; i++) {
			int ip = (i + 1) % coneSections;
			s.t[n++] = i;
			s.t[n++] = coneSections * 2;
			s.t[n++] = ip;
			s.t[n++] = i + coneSections;
			s.t[n++] = coneSections * 2 + 1;
			s.t[n++] = ip + coneSections;
		}
		return s;
	}
	
	public void transform(Matrix4d matrix) {
		for (int i = 0; i < v.length; i++) {
			matrix.transform(v[i]);
		}
		for (int i = 0; i < n.length; i++) {
			matrix.transform(n[i]);
		}
	}
}
