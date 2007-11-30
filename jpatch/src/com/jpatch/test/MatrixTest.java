package com.jpatch.test;

import com.jpatch.afw.vecmath.Rotation3d;

import javax.vecmath.*;

public class MatrixTest {
	public static void main(String[] args) {
		Matrix3d m = new Matrix3d();
		m.rotY(Math.PI);
		
		Matrix3d mm = new Matrix3d(1, 1, 1, 1, 1, 1, 1, 1, 1);
		
		m.mul(mm);
		System.out.println(m);
		System.exit(0);
		new MatrixTest();
	}
	
	MatrixTest() {
//		Matrix4d translateX10 = new Matrix4d();
//		translateX10.setIdentity();
//		translateX10.setTranslation(new Vector3d(10, 0, 0));
//		Matrix4d rotateY45 = new Matrix4d();
//		rotateY45.rotY(Math.toRadians(45));
//		
//		System.out.println("translateX10 = " + translateX10);
//		System.out.println("rotateY45 = " + rotateY45);
//		
//		Matrix4d concat = new Matrix4d();
//		concat.set(translateX10);
//		concat.mul(rotateY45);
//		System.out.println("translateX10 x rotateY45 = " + concat);
//		
//		concat.set(rotateY45);
//		concat.mul(translateX10);
//		System.out.println("rotateY45 x translateX10 = " + concat);
//		
//		concat.set(translateX10);
//		Rotation3d rot = new Rotation3d(0, 45, 0);
//		System.out.println("rot matrix = " + rot.getRotationMatrix(new Matrix4d()));
//		rot.rotateMatrix(concat);
//		System.out.println("rot(translateX10) = " + concat);
		
		Matrix4d r = new Matrix4d();
		Matrix4d o = new Matrix4d();
		Matrix4d n = new Matrix4d();
		Matrix4d n1 = new Matrix4d();
		Matrix4d r2 = new Matrix4d();
		Matrix4d tmp = new Matrix4d();
		
		randomize(r);
		randomize(o);
		randomize(n);
		n1.invert(n);
		
		r2.mul(r, o);
		r2.mul(n1);
		
		tmp.mul(r, o);
		System.out.println("rot x old = " + tmp);
		tmp.mul(r2, n);
		System.out.println("rot2 x new" + tmp);
		
		
		/*
		 * AB = A x B
		 * A = AB x B'
		 * B = A' x AB
		 */
	}
	
	void randomize(Matrix4d m) {
		Vector3d v = new Vector3d(Math.random(), Math.random(), Math.random());
		v.normalize();
		m.set(new AxisAngle4d(v, Math.random()));
		m.m33 = 1;
//		m.m00 = Math.random();
//		m.m01 = Math.random();
//		m.m02 = Math.random();
//		m.m03 = Math.random();
//		m.m10 = Math.random();
//		m.m11 = Math.random();
//		m.m12 = Math.random();
//		m.m13 = Math.random();
//		m.m20 = Math.random();
//		m.m21 = Math.random();
//		m.m22 = Math.random();
//		m.m23 = Math.random();
	}
}
