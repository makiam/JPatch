package com.jpatch.afw.vecmath;

import javax.vecmath.*;

public class VecmathTest {
	public static void main(String[] args) {
		Point3d p0 = new Point3d(1, 0, 0);
		Point3d p = new Point3d(p0);
		Rotation3d r1 = new Rotation3d(0, 90, 0);
		Matrix4d m1 = Utils3d.createIdentityMatrix();
		Rotation3d r2 = new Rotation3d(90, 0, 0);
		Matrix4d m2 = Utils3d.createIdentityMatrix();
		
		m2.setTranslation(new Vector3d(1, 0, 0));
		r1.getRotationMatrix(m1);
		r2.getRotationMatrix(m2);
		m2.transform(p);
		m1.transform(p);
		System.out.println(p);
		
		p.set(p0);
		m1.mul(m2);
		m1.transform(p);
		System.out.println(p);
		
		p.set(p0);
		r1.getRotationMatrix(m1);
		r2.rotateMatrix(m1);
		Scale3d s = new Scale3d(0, 0, 2);
		s.scaleMatrix(m1);
		m1.transform(p);
		System.out.println(p);
		
//		System.out.println(m1);
//		Matrix4d s = Utils3d.createIdentityMatrix();
//		s.m00 = 2;
////		m1.m10 = 99;
//		s.mul(m1);
//		System.out.println(s);
	}
}
