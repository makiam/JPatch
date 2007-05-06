package com.jpatch.boundary.tools;

import javax.vecmath.*;

public class WireFrame {
	public final Point3d[] v;
	public final int[] l;
	public final Color4f c;
	public final float[] array = new float[4];
	
	public WireFrame(Point3d[] vertices, int[] lines, Color3f color) {
		this(vertices, lines, new Color4f(color.x, color.y, color.z, 1.0f));
	}
	
	public WireFrame(Point3d[] vertices, int[] lines, Color4f color) {
		v = vertices;
		l = lines;
		c = color;
		array[0] = color.x;
		array[1] = color.y;
		array[2] = color.z;
		array[3] = color.w;
	}
	
	public void transform(Matrix4d matrix) {
		for (int i = 0; i < v.length; i++) {
			matrix.transform(v[i]);
		}
	}
}
