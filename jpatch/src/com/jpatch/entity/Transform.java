package com.jpatch.entity;

import com.jpatch.afw.attributes.GenericAttr;
import com.jpatch.afw.vecmath.*;
import javax.vecmath.*;

public abstract class Transform {

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
		computeInverse();
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
		computeInverse();
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
	 * Sets the elements of the specified matrix to that of the inverse of this transformation matrix and returns
	 * the specified matrix
	 * @param matrix A matrix4d object
	 * @return the specified matrix
	 */
	public Matrix4d getInverseMatrix(Matrix4d inverseMatrix) {
		inverseMatrix.set(this.invMatrix);
		return inverseMatrix;
	}
	
	/**
	 * Computes this matrix. Non abstract subclasses must implement this method and either compute the inverse too
	 * or set the invInvalid flag to true.
	 */
	public abstract void computeMatrix();
	
	/**
	 * Computes all transformed values
	 */
	public void computeTransformedValues() { }
	
	/**
	 * Calls computeMatrix() followed by computeTransformedValues() on this obeject. Subclasses that are part of a
	 * transform hierarchy and have child objects should call this method on their superclass or directly invoke
	 * computeMatrix() and computeTransformedValues() <i>and</i> call computeBranch() on all of their children.
	 */
	public void computeBranch() {
		computeMatrix();
		computeTransformedValues();
	}
	
	/**
	 * Computes invMatrix (sets invMatrox to the inverse of matrix).
	 * This implementation checks wheter the invInvalid flag is set and
	 * computes the matrix only if so. Once the invMatrix has been
	 * computed, the invInvalid flag is cleared.
	 */
	private void computeInverse() {
		if (invInvalid) {
			invMatrix.invert(matrix);
			invInvalid = false;
		}
	}
}
