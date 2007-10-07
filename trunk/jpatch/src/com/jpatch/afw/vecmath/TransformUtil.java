package com.jpatch.afw.vecmath;

import javax.vecmath.*;

/**
 * <p>
 * Provides methods to transform Point3Ds or Vector3Ds between different coordinate systems (world, local and camera)
 * or to project points from different coordinate systems (world, local and camera) to screen coordinates.
 * </p>
 * <p>
 * Note that in camera space, the screen-center is at [0, 0, 1] with the x-axis pointing right, the y-axis pointing upwards
 * and the z-axis pointing away the viewer, whether in screen space, the upper left corner is at [0, 0, focalLength] and the
 * lower right corner is at [viewportWidth, viewportHeight, focalLength], where focalLength = relativeFocalLength * viewportWidth,
 * with the x-axis pointing right, the y-axis pointing downwards and the z-axis pointing away from the viewer.
 * </p>
 * <h6>Example usage:</h6>
 * <ul>
 *   <li>For an orthographic projection, set the worldToCamera matrix to the view's transformation matrix.</li>
 *   <li>For a perspective projection, set the cameraToWorld matrix to the matrix of the camera's transform-noe.</li>
 *   <li>Set the localToWorld matrix to the matrix of the object's TransformNode.</li>
 * </ul>
 * @author Sascha Ledinsky
 */
public class TransformUtil {
	/** 4x4 identity matrix */
	private static final Matrix4d IDENTITY = new Matrix4d(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
	
	/** Camera to local space transformation matrix */
	private Matrix4d camera2Local = new Matrix4d(IDENTITY);
	
	/** Flag to tell whether camera2Local is invalid, used for lazy evaluation of the camera2Local matrix */
	private boolean camera2LocalInvalid = false;
	
	/**
	 * Camera to world space transformation matrix
	 * If an orthographic projection is used, this matrix is supposed to also
	 * contain a scale component.
	 */
	private Matrix4d camera2World = new Matrix4d(IDENTITY);
	
	/** Flag to tell whether camera2World is invalid, used for lazy evaluation of the camera2World matrix */
	private boolean camera2WorldInvalid = false;
	
	/** Local to camera space transformation matrix */
	private Matrix4d local2Camera = new Matrix4d(IDENTITY);
	
	/** Flag to tell whether local2Camera is invalid, used for lazy evaluation of the local2Camera matrix */
	private boolean local2CameraInvalid = false;
	
	/** Local to world space transformation matrix */
	private Matrix4d local2World = new Matrix4d(IDENTITY);
	
	/** Flag to tell whether local2World is invalid, used for lazy evaluation of the local2World matrix */
	private boolean local2WorldInvalid = false;
	
	/** Flag to tell whether a perspective (true) or orthographic (false) projection is used */
	private boolean perspective = false;
	
	/** The relative focalLength (focalLength / aperture width), used for perspective projections */
	private double relativeFocalLength;
	
	/** The height of the viewport in pixel */
	private double viewportHeight;
	
	/** The width of the viewport in pixel */
	private double viewportWidth;
	
	/**
	 * world to camera space transformation matrix.
	 * If an orthographic projection is used, this matrix is supposed to also
	 * contain a scale component.
	 */
	private Matrix4d world2Camera = new Matrix4d(IDENTITY);
	
	/** Flag to tell whether world2Camera is invalid, used for lazy evaluation of the world2Camera matrix */
	private boolean world2CameraInvalid = false;
	
	/** world to local space transformation matrix */
	private Matrix4d world2Local = new Matrix4d(IDENTITY);
	
	/** Flag to tell whether world2Local is invalid, used for lazy evaluation of the world2Local matrix */
	private boolean world2LocalInvalid = false;
	
	/**
	 * Transforms the <i>in</i> point from camera space to the specified <i>out</i> point in local space.
	 * This method sets the specified <i>out</i> Point3d to the coordinates of the specified <i>in</i> Point3d,
	 * transformed from camera to local space.
	 * @param in the point in camera space
	 * @param out this point will be set to the point in local space. May be the same Point3d object as <i>in</i>
	 */
	public void camera2Local(Point3d in, Point3d out) {
		computeCamera2Local();
		camera2Local.transform(in, out);
	}
	
	/**
	 * Transforms the <i>in</i> vector from camera space to the specified <i>out</i> vector in local space.
	 * This method sets the specified <i>out</i> Vector3d to the coordinates of the specified <i>in</i> Vector3d,
	 * transformed from camera to local space.
	 * @param in the vector in camera space
	 * @param out this vector will be set to the vector in local space. May be the same Vector3d object as <i>in</i>
	 */
	public void camera2Local(Vector3d in, Vector3d out) {
		computeCamera2Local();
		camera2Local.transform(in, out);
	}
	
	/**
	 * Projects the <i>in</i> point from camera space to the specified <i>out</i> point in screen coordinates.
	 * This method sets the specified <i>out</i> Point3d to the coordinates of the specified <i>in</i> Point3d,
	 * projected from camera to screen space. If a perspective projection is used, the perspective devision is performed.
	 * Screen space is defined with 0/0 as the upper left corner and (viewportWidth, viewportHeight) as the lower right
	 * corner.
	 * @param in the point in camera space
	 * @param out this point will be set to the point in screen coordinates. May be the same Point3d object as <i>in</i>
	 */
	public void camera2Screen(Point3d in, Point3d out) {
		if (perspective) {
			double w = viewportWidth * relativeFocalLength / in.z;
			out.x = viewportWidth * 0.5 + in.x * w;
			out.y = viewportHeight * 0.5 - in.y * w;
		} else {
			out.x = viewportWidth * 0.5 + in.x;
			out.y = viewportHeight * 0.5 - in.y;
		}
		out.z = in.z;
	}
	
	/**
	 * Transforms the <i>in</i> point from camera space to the specified <i>out</i> point in world space.
	 * This method sets the specified <i>out</i> Point3d to the coordinates of the specified <i>in</i> Point3d,
	 * transformed from camera to world space.
	 * @param in the point in camera space
	 * @param out this point will be set to the point in world space. May be the same Point3d object as <i>in</i>
	 */
	public void camera2World(Point3d in, Point3d out) {
		computeCamera2World();
		camera2World.transform(in, out);
	}
	
	/**
	 * Transforms the <i>in</i> vector from camera space to the specified <i>out</i> vector in world space.
	 * This method sets the specified <i>out</i> Vector3d to the coordinates of the specified <i>in</i> Vector3d,
	 * transformed from camera to world space.
	 * @param in the vector in camera space
	 * @param out this vector will be set to the vector in world space. May be the same Vector3d object as <i>in</i>
	 */
	public void camera2World(Vector3d in, Vector3d out) {
		computeCamera2World();
		camera2World.transform(in, out);
	}
	
	/**
	 * Transforms the <i>in</i> point from local space to the specified <i>out</i> point in camera space.
	 * This method sets the specified <i>out</i> Point3d to the coordinates of the specified <i>in</i> Point3d,
	 * transformed from local to camera space.
	 * @param in the point in local space
	 * @param out this point will be set to the point in camera space. May be the same Point3d object as <i>in</i>
	 */
	public void local2Camera(Point3d in, Point3d out) {
		computeLocal2Camera();
		local2Camera.transform(in, out);
	}
	
	/**
	 * Transforms the <i>in</i> vector from local space to the specified <i>out</i> vector in camera space.
	 * This method sets the specified <i>out</i> Vector3d to the coordinates of the specified <i>in</i> Vector3d,
	 * transformed from local to camera space.
	 * @param in the vector in local space
	 * @param out this vector will be set to the vector in camera space. May be the same Vector3d object as <i>in</i>
	 */
	public void local2Camera(Vector3d in, Vector3d out) {
		computeLocal2Camera();
		local2Camera.transform(in, out);
	}
	
	/**
	 * Projects the <i>in</i> point from local space to the specified <i>out</i> point in screen coordinates.
	 * This method sets the specified <i>out</i> Point3d to the coordinates of the specified <i>in</i> Point3d,
	 * projected from local to screen space. If a perspective projection is used, the perspective devision is performed.
	 * Screen space is defined with 0/0 as the upper left corner and (viewportWidth, viewportHeight) as the lower right
	 * corner.
	 * @param in the point in local space
	 * @param out this point will be set to the point in screen coordinates. May be the same Point3d object as <i>in</i>
	 */
	public void local2Screen(Point3d in, Point3d out) {
		local2Camera(in, out);
		camera2Screen(out, out);
	}
	
	/**
	 * Transforms the <i>in</i> point from local space to the specified <i>out</i> point in world space.
	 * This method sets the specified <i>out</i> Point3d to the coordinates of the specified <i>in</i> Point3d,
	 * transformed from local to world space.
	 * @param in the point in local space
	 * @param out this point will be set to the point in world space. May be the same Point3d object as <i>in</i>
	 */
	public void local2World(Point3d in, Point3d out) {
		computeLocal2World();
		local2World.transform(in, out);
	}
	
	/**
	 * Transforms the <i>in</i> vector from local space to the specified <i>out</i> vector in world space.
	 * This method sets the specified <i>out</i> Vector3d to the coordinates of the specified <i>in</i> Vector3d,
	 * transformed from local to world space.
	 * @param in the vector in local space
	 * @param out this vector will be set to the vector in world space. May be the same Vector3d object as <i>in</i>
	 */
	public void local2World(Vector3d in, Vector3d out) {
		computeLocal2World();
		local2World.transform(in, out);
	}
	
	/**
	 * Sets an orthographics projection
	 */
	public void setOrthographicProjection() {
		perspective = false;
	}
	
	/**
	 * Sets a perspective projection with the specified relativeFocalLength
	 * @param relativeFocalLength the relative focal length (focal length / aperture width)
	 */
	public void setPerspectiveProjection(double relativeFocalLength) {
		perspective = true;
		this. relativeFocalLength = relativeFocalLength;
	}
	
	/**
	 * Sets the viewport dimension to the specified width and height parameters (in pixel).
	 * @param width the width of the viewport in pixel
	 * @param height the height of the viewport in pixel
	 */
	public void setViewportDimension(int width, int height) {
		this.viewportWidth = width;
		this.viewportHeight = height;
	}
	
	/**
	 * Sets the world to camera space and the world to local space transformation matrices to the values of the specified matrices.
	 * @param world2Local the world to camera space transformation matrix
	 * @param world2Camera the world to camera space transformation matrix
	 */
	public void setTransforms(Matrix4d world2Local, Matrix4d world2Camera) {
		setWorld2Local(world2Local);
		setWorld2Camera(world2Camera);
	}
	
	/**
	 * Sets the world to camera space transformation matrix to the value of the specified matrix.
	 * If an orthographic projection is used, this matrix is supposed to also contain a scale component.
	 * @param world2Camera the world to camera space transformation matrix.
	 */
	public void setWorld2Camera(Matrix4d world2Camera) {
		this.world2Camera.set(world2Camera);
		
		/* set matrix invalidation flags for lazy evaluation */
		world2CameraInvalid = false;
		camera2WorldInvalid = true;
		camera2LocalInvalid = true;
		local2CameraInvalid = true;
	}
	
	/**
	 * Sets the camera to world space transformation matrix to the value of the specified matrix.
	 * If an orthographic projection is used, this matrix is supposed to also contain a scale component.
	 * @param camera2World the camera to world space transformation matrix.
	 */
	public void setCamera2World(Matrix4d camera2World) {
		this.camera2World.set(camera2World);
		
		/* set matrix invalidation flags for lazy evaluation */
		camera2WorldInvalid = false;
		world2CameraInvalid = true;
		camera2LocalInvalid = true;
		local2CameraInvalid = true;
	}
	
	/**
	 * Sets the world to local space transformation matrix to the value of the specified matrix.
	 * @param world2Local the world to local space transformation matrix
	 */
	public void setWorld2Local(Matrix4d world2Local) {
		this.world2Local.set(world2Local);
		world2LocalInvalid = false;
		
		/* set matrix invalidation flags for lazy evaluation */
		local2WorldInvalid = true;
		camera2LocalInvalid = true;
		local2CameraInvalid = true;
	}
	
	/**
	 * Sets the local to world space transformation matrix to the value of the specified matrix.
	 * @param local2World the world to local space transformation matrix
	 */
	public void setLocal2World(Matrix4d local2World) {
		this.local2World.set(local2World);
		local2WorldInvalid = false;
		
		/* set matrix invalidation flags for lazy evaluation */
		world2LocalInvalid = true;
		camera2LocalInvalid = true;
		local2CameraInvalid = true;
	}
	
	/**
	 * Transforms the <i>in</i> point from world space to the specified <i>out</i> point in camera space.
	 * This method sets the specified <i>out</i> Point3d to the coordinates of the specified <i>in</i> Point3d,
	 * transformed from world to camera space.
	 * @param in the point in world space
	 * @param out this point will be set to the point in camera space. May be the same Point3d object as <i>in</i>
	 */
	public void world2Camera(Point3d in, Point3d out) {
		computeWorld2Camera();
		world2Camera.transform(in, out);
	}
	
	/**
	 * Transforms the <i>in</i> vector from world space to the specified <i>out</i> vector in camera space.
	 * This method sets the specified <i>out</i> Vector3d to the coordinates of the specified <i>in</i> Vector3d,
	 * transformed from world to camera space.
	 * @param in the vector in world space
	 * @param out this vector will be set to the vector in camera space. May be the same Vector3d object as <i>in</i>
	 */
	public void world2Camera(Vector3d in, Vector3d out) {
		computeWorld2Camera();
		world2Camera.transform(in, out);
	}
	
	/**
	 * Transforms the <i>in</i> point from world space to the specified <i>out</i> point in local space.
	 * This method sets the specified <i>out</i> Point3d to the coordinates of the specified <i>in</i> Point3d,
	 * transformed from world to local space.
	 * @param in the point in world space
	 * @param out this point will be set to the point in local space. May be the same Point3d object as <i>in</i>
	 */
	public void world2Local(Point3d in, Point3d out) {
		computeWorld2Local();
		world2Local.transform(in, out);
	}
	
	/**
	 * Transforms the <i>in</i> vector from world space to the specified <i>out</i> vector in local space.
	 * This method sets the specified <i>out</i> Vector3d to the coordinates of the specified <i>in</i> Vector3d,
	 * transformed from world to local space.
	 * @param in the vector in world space
	 * @param out this vector will be set to the vector in local space. May be the same Vector3d object as <i>in</i>
	 */
	public void world2Local(Vector3d in, Vector3d out) {
		computeWorld2Local();
		world2Local.transform(in, out);
	}
	
	/**
	 * Projects the <i>in</i> point from world space to the specified <i>out</i> point in screen coordinates.
	 * This method sets the specified <i>out</i> Point3d to the coordinates of the specified <i>in</i> Point3d,
	 * projected from world to screen space. If a perspective projection is used, the perspective devision is performed.
	 * Screen space is defined with 0/0 as the upper left corner and (viewportWidth, viewportHeight) as the lower right
	 * corner.
	 * @param in the point in world space
	 * @param out this point will be set to the point in screen coordinates. May be the same Point3d object as <i>in</i>
	 */
	public void world2Screen(Point3d in, Point3d out) {
		world2Camera(in, out);
		camera2Screen(out, out);
	}
	
	/**
	 * Sets the specified modelView matrix to the local to camera space transformation matrix.
	 * @param modelView the matrix to set
	 * @return the specified matrix
	 */
	public Matrix4f getModelViewMatrix(Matrix4f modelView) {
		computeLocal2Camera();
		modelView.set(local2Camera);
		return modelView;
	}
	
	/**
	 * If world2LocalInvalid is set, computes the world2Local matrix by inverting the local2World matrix and
	 * sets world2LocalInvalid to false.
	 */
	private void computeWorld2Local() {
		if (world2LocalInvalid) {
			if (local2WorldInvalid) {
				throw new RuntimeException("can't compute world2Local matrix when local2World matrix is invalid");
			}
			world2Local.invert(local2World);
			world2LocalInvalid = false;
		}
	}
	
	/**
	 * If local2WorldInvalid is set, computes the local2World matrix by inverting the world2Local matrix and
	 * sets local2WorldInvalid to false.
	 */
	private void computeLocal2World() {
		if (local2WorldInvalid) {
			if (world2LocalInvalid) {
				throw new RuntimeException("can't compute local2World matrix when world2Local matrix is invalid");
			}
			local2World.invert(world2Local);
			local2WorldInvalid = false;
		}
	}
	
	/**
	 * If world2CameraInvalid is set, computes the world2Camera matrix by inverting the camera2World matrix and
	 * sets world2CameraInvalid to false.
	 */
	private void computeWorld2Camera() {
		if (world2CameraInvalid) {
			if (camera2WorldInvalid) {
				throw new RuntimeException("can't compute world2Camera matrix when camera2World matrix is invalid");
			}
			world2Camera.invert(camera2World);
			world2CameraInvalid = false;
		}
	}
	
	/**
	 * If camera2WorldInvalid is set, computes the camera2World matrix by inverting the world2Camera matrix and
	 * sets camera2WorldInvalid to false.
	 */
	private void computeCamera2World() {
		if (camera2WorldInvalid) {
			if (world2CameraInvalid) {
				throw new RuntimeException("can't compute camera2World matrix when world2Camera matrix is invalid");
			}
			camera2World.invert(world2Camera);
			camera2WorldInvalid = false;
		}
	}
	
	/**
	 * If local2CameraInvalidis set, computes the local2Camera matrix by concatenating the world2Camera and the
	 * local2World matrices and sets local2CameraInvalid to false.
	 * computeWorld2Camera() and computeLocal2World() are called beforehand to ensure that both matrices are valid.
	 */
	private void computeLocal2Camera() {
		if (local2CameraInvalid) {
			computeWorld2Camera();
			computeLocal2World();
			local2Camera.set(world2Camera);
			local2Camera.mul(local2World);
			local2CameraInvalid = false;
		}
	}
	
	/**
	 * If camera2LocalInvalid set, computes the camera2Local matrix by concatenating the world2Local and the
	 * camera2World matrices and sets camera2LocalInvalid to false.
	 * computeWorld2Local() and computeCamera2World() are called beforehand to ensure that both matrices are valid.
	 */
	private void computeCamera2Local() {
		if (camera2LocalInvalid) {
			computeWorld2Local();
			computeCamera2World();
			camera2Local.set(world2Local);
			camera2Local.mul(camera2World);
			camera2LocalInvalid = false;
		}
	}
}
