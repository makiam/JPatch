package com.jpatch.afw.vecmath;

import static com.jpatch.afw.vecmath.AbstractTransformUtil.WORLD;
import static com.jpatch.afw.vecmath.TransformUtil.CAMERA;

import java.awt.Dimension;

import javax.vecmath.*;

public class TransformUtil extends AbstractTransformUtil {
	/** camera space, should only be set by the viewport */
	public static final int CAMERA = 1;
	/** local object space, should be set by scene-graph nodes */
	public static final int LOCAL = 2;
	
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
	
	public TransformUtil(String... additionalSpaceNames) {
		super(concatenateSpaceNames(new String[] { "camera", "local" }, additionalSpaceNames));
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
	
//	/**
//	 * Sets the coordinates of toVector to the coordinates of fromVector, projected from screen
//	 * to camera space and (iff space != camera space) transformed to the specified space
//	 * @param space
//	 * @param fromPoint
//	 * @param toPoint
//	 */
//	public void projectFromScreen(int space, Vector3d fromVector, Vector3d toVector) {
////		unproject(fromVector, toVector);
//		if (space != CAMERA) {
//			transform(CAMERA, toVector, space, toVector);
//		} 
//	}
	
	/**
	 * Sets the world-to-space transformation matrix to the matrix of the specified transform object
	 * @param transform
	 */
	public void setTransform(int space, Transform transform) {
		setSpace2World(space, transform.matrix);
		if (space == CAMERA) {
			flipZAxis(CAMERA);
		}
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
	 * Cumputes the radius of the sphere at the specified distance from the viewing plane
	 * to occupy about 1/4th of the viewport specified by viewportWidth and viewportHeight
	 * @param zDistance the distance of the sphere center from the viewing plane
	 * @param viewportWidth the width of the viewport in pixel
	 * @param viewportHeight the height of the viewport in pixel
	 * @return radius of the sphere such that the sphere will occupy about 1/4th of the viewport area
	 */
	public double computeNiceRadius(double zDistance, int viewportWidth, int viewportHeight) {
//		System.out.println("computeNiceRadius(" + zDistance + ", " + viewportWidth + ", " + viewportHeight + ")");
		if (!isPerspective()) {
//			System.out.println("niceRadius = " + Math.min(viewportWidth, viewportHeight) / getCameraScale() * 0.2);
			return Math.min(viewportWidth, viewportHeight) / getCameraScale() * 0.2;
		} else {
			return zDistance / getRelativeFocalLength() * 0.2;
		}
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
