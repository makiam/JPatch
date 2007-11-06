package com.jpatch.afw.vecmath;

import javax.vecmath.*;
import static com.jpatch.afw.vecmath.AbstractTransformUtil.*;

public class TransformUtil2Test {

	public static final int LOCAL = 1;
	public static final int CAMERA = 2;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		AbstractTransformUtil transformUtil = new AbstractTransformUtil("local", "camera");
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
		Matrix4d world2Camera = new Matrix4d();
		local2World.setIdentity();
		world2Camera.setIdentity();
//		local2World.rotX(30);
		local2World.setTranslation(new Vector3d(1, 0, 0));
		world2Camera.setTranslation(new Vector3d(0, 1, 0));
//		world2Camera.setScale(0.5);
//		world2Camera.rotY(30);
		Point3d local = new Point3d(0, 0, 1);
		Point3d world = new Point3d();
		Point3d camera = new Point3d();
		Point3d screen = new Point3d();
		
		transformUtil.setSpace2World(LOCAL, local2World);
		transformUtil.setWorld2Space(CAMERA, world2Camera);
//		transformUtil.setCameraTransform(transform2);
//		
		transformUtil.transform(LOCAL, local, WORLD, world);
		transformUtil.transform(LOCAL, local, CAMERA, camera);
//		transformUtil.local2Screen(local, screen);
//		
		System.out.println("local = \t" + local);
		System.out.println("world = \t" + world);
		System.out.println("camera = \t" + camera);
//		
//		transformUtil.world2Camera(world, camera);
//		System.out.println("camera = \t" + camera + " after world2Camera");
//		
//		System.out.println("screen = \t" + screen);
//		
//		transformUtil.world2Local(world, local);
//		System.out.println("local = \t" + local + " after world2Local");
//		
//		transformUtil.camera2Local(camera, local);
//		System.out.println("local = \t" + local + " after camera2Local");
//		
//		transformUtil.screen2Local(screen, local);
//		System.out.println("local = \t" + local + " after screen2Local");
		
		System.out.println(transformUtil);
	}

}
