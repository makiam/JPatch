package com.jpatch.afw.vecmath;

import javax.vecmath.*;
public class TransformUtilTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TransformUtil transformUtil = new TransformUtil();
		Matrix4d local2World = new Matrix4d();
		Matrix4d camera2World = new Matrix4d();
		local2World.setIdentity();
		camera2World.setIdentity();
		
		local2World.setTranslation(new Vector3d(1, 0, 0));
		camera2World.setTranslation(new Vector3d(0, 1, 0));
		
		Point3d world = new Point3d(0, 0, 1);
		Point3d local = new Point3d();
		Point3d camera = new Point3d();
		Point3d screen = new Point3d();
		
		transformUtil.setPerspectiveProjection(1);
		transformUtil.setViewportDimension(1000, 1000);
		transformUtil.setLocal2World(local2World);
		transformUtil.setCamera2World(camera2World);
		
		transformUtil.world2Local(world, local);
		transformUtil.world2Camera(world, camera);
		transformUtil.world2Screen(world, screen);
		
		System.out.println("world = \t" + world);
		System.out.println("local = \t" + local);
		System.out.println("camera = \t" + camera);
		System.out.println("screen = \t" + screen);
		
		transformUtil.local2World(local, world);
		System.out.println("world = \t" + world + " after local2World");
		
		transformUtil.camera2World(camera, world);
		System.out.println("world = \t" + world + " after camera2World");
	}

}
