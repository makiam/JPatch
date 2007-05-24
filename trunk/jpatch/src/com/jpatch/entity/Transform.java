package com.jpatch.entity;

import com.jpatch.afw.vecmath.*;
import javax.vecmath.*;

public class Transform {
	/**
	 * The transformation matrix
	 */
	protected final Matrix4d matrix = Utils3d.createIdentityMatrix();
	/**
	 * The invertex transformation matrix
	 */
	protected final Matrix4d invMatrix = Utils3d.createIdentityMatrix();
	/**
	 * Flag to tell if the invertex matrix is invalid (true) or valid (false)
	 */
	protected boolean invInvalid = false;
	
	/**
	 * Transforms the specified normal vector using this transformation matrix
	 * @param normal the normal vector to be transformed
	 * @return the specified normal vector (after the transformation)
	 */
	public Vector3d transform(Vector3d normal) {
		matrix.transform(normal);
		return normal;
	}
	
	/**
	 * Transforms the specified normal vector using the inverse of this transformation matrix
	 * @param normal the normal vector to be transformed
	 * @return the specified normal vector (after the transformation)
	 */
	public Vector3d invTransform(Vector3d normal) {
		if (invInvalid) {
			computeInverse();
		}
		invMatrix.transform(normal);
		return normal;
	}
	
	/**
	 * Transforms the specified point using this transformation matrix
	 * @param normal the point to be transformed
	 * @return the specified point (after the transformation)
	 */
	public Point3d transform(Point3d point) {
		matrix.transform(point);
		return point;
	}
	
	/**
	 * Transforms the specified point using the inverse of this transformation matrix
	 * @param normal the point to be transformed
	 * @return the specified point (after the transformation)
	 */
	public Point3d invTransform(Point3d point) {
		if (invInvalid) {
			computeInverse();
		}
		invMatrix.transform(point);
		return point;
	}
	
	/**
	 * Sets this transformation matrix's elements to that of the specified matrix.
	 * This implementation sets the invInvalid flag to true.
	 * @param matrix the matrix to set this transformation matrix's elements to
	 */
	public void setMatrix(Matrix4d matrix) {
		this.matrix.set(matrix);
		invInvalid = true;
	}
	
	/**
	 * Sets the elements of the specified matrix to that of this transformation matrix and returns
	 * the specified matrix
	 * @param matrix A matrix4d object
	 * @return the specified matrix
	 */
	public Matrix4d getMatrix(Matrix4d matrix) {
		matrix.set(this.matrix);
		return matrix;
	}
	
	/**
	 * Sets invMatrix to the inverse of matrix and clears the invInvalid flag
	 */
	private void computeInverse() {
		invMatrix.invert(matrix);
		invInvalid = false;
	}
}
