package com.jpatch.test;

import com.jpatch.afw.vecmath.Rotation3d;

import javax.vecmath.*;

public class MatrixTest {
	public static void main(String[] args) {
		new MatrixTest();
	}
	
	MatrixTest() {
		Matrix4d translateX10 = new Matrix4d();
		translateX10.setIdentity();
		translateX10.setTranslation(new Vector3d(10, 0, 0));
		Matrix4d rotateY45 = new Matrix4d();
		rotateY45.rotY(Math.toRadians(45));
		
		System.out.println("translateX10 = " + translateX10);
		System.out.println("rotateY45 = " + rotateY45);
		
		Matrix4d concat = new Matrix4d();
		concat.set(translateX10);
		concat.mul(rotateY45);
		System.out.println("translateX10 x rotateY45 = " + concat);
		
		concat.set(rotateY45);
		concat.mul(translateX10);
		System.out.println("rotateY45 x translateX10 = " + concat);
		
		concat.set(translateX10);
		Rotation3d rot = new Rotation3d(0, 45, 0);
		System.out.println("rot matrix = " + rot.getRotationMatrix(new Matrix4d()));
		rot.rotateMatrix(concat);
		System.out.println("rot(translateX10) = " + concat);
	}
}
