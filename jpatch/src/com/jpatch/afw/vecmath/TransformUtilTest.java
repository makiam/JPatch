package com.jpatch.afw.vecmath;

import javax.vecmath.*;
public class TransformUtilTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TransformUtil transformUtil = new TransformUtil();
		Transform transform1 = new Transform() {
			@Override
			public void computeMatrix() {
				matrix.rotX(45);
			}
		};
		Transform transform2 = new Transform() {
			@Override
			public void computeMatrix() {
				matrix.setScale(10);
			}
		};
		transform1.computeMatrix();
		transform2.computeMatrix();
		
		Matrix4d local2World = new Matrix4d();
		Matrix4d camera2World = new Matrix4d();
		local2World.setIdentity();
		camera2World.setIdentity();
		local2World.rotX(30);
		local2World.setTranslation(new Vector3d(1, 0, 0));
		camera2World.setTranslation(new Vector3d(0, 1, 0));
		camera2World.setScale(0.5);
		camera2World.rotY(30);
		Point3d local = new Point3d(0, 0, 1);
		Point3d world = new Point3d();
		Point3d camera = new Point3d();
		Point3d screen = new Point3d();
		
		transformUtil.setOrthographicProjection();
		transformUtil.setViewportDimension(1000, 1000);
		transformUtil.setLocalTransform(transform1);
		transformUtil.setCameraTransform(transform2);
		
		transformUtil.local2World(local, world);
		transformUtil.local2Camera(local, camera);
		transformUtil.local2Screen(local, screen);
		
		System.out.println("local = \t" + local);
		System.out.println("world = \t" + world);
		System.out.println("camera = \t" + camera);
		
		transformUtil.world2Camera(world, camera);
		System.out.println("camera = \t" + camera + " after world2Camera");
		
		System.out.println("screen = \t" + screen);
		
		transformUtil.world2Local(world, local);
		System.out.println("local = \t" + local + " after world2Local");
		
		transformUtil.camera2Local(camera, local);
		System.out.println("local = \t" + local + " after camera2Local");
		
		transformUtil.screen2Local(screen, local);
		System.out.println("local = \t" + local + " after screen2Local");
		
		System.out.println(transformUtil);
	}

}
