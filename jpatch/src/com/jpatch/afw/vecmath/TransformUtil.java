package com.jpatch.afw.vecmath;

import javax.vecmath.*;

public class TransformUtil extends AbstractTransformUtil {
	/** camera space, should only be set by the viewport */
	public final int CAMERA = addSpace("camera");
	/** local object space */
	public final int LOCAL = addSpace("local");
	
	/** perspective projection flag */
	private boolean perspective;
	/** relative focal length (focal length / aperture width) */
	private double relativeFocalLength;
	/** viewport width */
	private int viewportWidth;
	/** viewport height */
	private int viewportHeight;
	/** scale component of the camera matrix */
	private double cameraScale;
	
	
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
		this.relativeFocalLength = relativeFocalLength;
	}
	
	/**
	 * Test if a perspective projection is beign used
	 * @return true if a perspective projection is beign used, false otherwise
	 */
	public boolean isPerspective() {
		return perspective;
	}
	
	/**
	 * Returns the relative focal length (focal length / aperture width)
	 * @return the relative focal length (focal length / aperture width)
	 */
	public double getRelativeFocalLength() {
		return relativeFocalLength;
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
	 * Sets the coordinates of toPoint to the coordinates of fromPoint, transformed from
	 * the specified space to camera space (iff space != camera) and projected to screen
	 * @param space
	 * @param fromPoint
	 * @param toPoint
	 */
	public void projectToScreen(int space, Point3d fromPoint, Point3d toPoint) {
		if (space == CAMERA) {
			project(fromPoint, toPoint);
		} else {
			transform(space, fromPoint, CAMERA, toPoint);
			project(toPoint, toPoint);
		} 
	}
	
	/**
	 * Sets the coordinates of toPoint to the coordinates of fromPoint, projected from screen
	 * to camera space and (iff space != camera space) transformed to the specified space
	 * @param space
	 * @param fromPoint
	 * @param toPoint
	 */
	public void projectFromScreen(int space, Point3d fromPoint, Point3d toPoint) {
		unproject(fromPoint, toPoint);
		if (space != CAMERA) {
			transform(CAMERA, toPoint, space, toPoint);
		} 
	}
	
	/**
	 * Sets the coordinates of toVector to the coordinates of fromVector, projected from screen
	 * to camera space and (iff space != camera space) transformed to the specified space
	 * @param space
	 * @param fromPoint
	 * @param toPoint
	 */
	public void projectFromScreen(int space, Vector3d fromVector, Vector3d toVector) {
		unproject(fromVector, toVector);
		if (space != CAMERA) {
			transform(CAMERA, toVector, space, toVector);
		} 
	}
	
	/**
	 * Sets the world-to-local transformation matrix to the matrix of the specified transform object
	 * @param transform
	 */
	public void setLocalTransform(Transform transform) {
		setWorld2Space(LOCAL, transform.matrix);
	}
	
	/**
	 * Sets the camera-to-world transformation matrix to the matrix of the specified transform object
	 * and flips the z-axis (to make the camera look into the direction of the positive z axis)
	 * @param transform
	 */
	public void setCameraTransform(Transform transform) {
		setSpace2World(CAMERA, transform.matrix);
		/* rotate the camera to look down the positive z axis */
		flipZAxis(CAMERA);
	}
	
	/**
	 * Sets the cameraScale (the scale component of the camera matrix)
	 * @param cameraScale
	 */
	public void setCameraScale(double cameraScale) {
		this.cameraScale = cameraScale;
	}
	
	/**
	 * Returns the cameraScale (the scale component of the camera matrix)
	 * @return the cameraScale (the scale component of the camera matrix), as set by the last call to setCameraScale
	 */
	public double getCameraScale() {
		return cameraScale;
	}
	
	/**
	 * Sets the coordinates of toTuple to the coordinates of fromTuple, projected
	 * from camera to screen space
	 * @param fromTuple
	 * @param toTuple
	 */
	private void project(Tuple3d fromTuple, Tuple3d toTuple) {
		if (perspective) {
			double w = -viewportWidth * relativeFocalLength / fromTuple.z;
			toTuple.x = viewportWidth * 0.5 + fromTuple.x * w;
			toTuple.y = viewportHeight * 0.5 - fromTuple.y * w;
			toTuple.z = -fromTuple.z;	// camera looks down positive(!) z-axis
		} else {
			toTuple.x = viewportWidth * 0.5 + fromTuple.x;
			toTuple.y = viewportHeight * 0.5 - fromTuple.y;
			toTuple.z = fromTuple.z;
		}
	}
	
	/**
	 * Sets the coordinates of toTuple to the coordinates of fromTuple, projected
	 * from screen to camera space
	 */
	private void unproject(Tuple3d fromTuple, Tuple3d toTuple) {
		if (perspective) {
			double w = fromTuple.z / (viewportWidth * relativeFocalLength);
			toTuple.x = (fromTuple.x - viewportWidth * 0.5) * w;
			toTuple.y = (viewportHeight * 0.5 - fromTuple.y) * w;
			toTuple.z = -fromTuple.z;	// camera looks down positive(!) z-axis
		} else {
			toTuple.x = fromTuple.x - viewportWidth * 0.5;
			toTuple.y = viewportHeight * 0.5 - fromTuple.y;
			toTuple.z = fromTuple.z;
		}
	}
}
