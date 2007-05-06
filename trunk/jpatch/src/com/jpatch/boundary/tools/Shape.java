package com.jpatch.boundary.tools;

import javax.vecmath.Matrix4d;

public class Shape {
	public final TriangleMesh[] meshes;
	public final WireFrame[] wires;
	public final Matrix4d transform;
	
	public Shape(TriangleMesh[] meshes, WireFrame[] wires) {
		this.meshes = meshes;
		this.wires = wires;
		transform = new Matrix4d();
		transform.setIdentity();
	}
}
