package com.jpatch.afw.vecmath;

import javax.vecmath.*;

/**
 * A Tuple3d object that represents a scale transformation. The x, y and z fields store the scale factor for
 * each axis.
 */
@SuppressWarnings("serial")
public class Scale3d extends Tuple3d {
	
	/**
	 * Creates a new Scale3d object with an identity transformation (scale factors 1, 1, 1)
	 */
	public Scale3d() {
		this(1, 1, 1);
	}
	
	/* suplerclass constructor */
	public Scale3d(double x, double y, double z) {
		super(x, y, z);
	}
	
	/* suplerclass constructor */
	public Scale3d(double[] xyz) {
		super(xyz);
	}
	
	/* suplerclass constructor */
	public Scale3d(Tuple3d tuple3d) {
		super(tuple3d);
	}
	
	/* suplerclass constructor */
	public Scale3d(Tuple3f tuple3f) {
		super(tuple3f);
	}
	
	/**
	 * Sets the specified 3x3 matrix to a transformation matrix that represents this scale-transformation
 	 * @param m The matrix to modify
	 * @return the specified matrix
	 * @throws NullPointerException if the specified parameter was null
	 */
	public Matrix3d getScaleMatrix(Matrix3d m) {
		m.m00 = x;
		m.m01 = 0;
		m.m02 = 0;
		m.m10 = 0;
		m.m11 = y;
		m.m12 = 0;
		m.m20 = 0;
		m.m21 = 0;
		m.m22 = z;
		return m;
	}
	
	/**
	 * Sets the 3x3 rotation/scale component of the specified matrix to a transformation matrix that represents this scale-transformation.
	 * The translation part of the specified matrix is not modified! To get a 4x4 matrix that represents this scale-transformation
	 * you must pass an identity 4x4 matrix to this method
 	 * @param m The matrix to modify
	 * @return the specified matrix
	 * @throws NullPointerException if the specified parameter was null
	 */
	public Matrix4d getScaleMatrix(Matrix4d m) {
		m.m00 = x;
		m.m01 = 0;
		m.m02 = 0;
		m.m10 = 0;
		m.m11 = y;
		m.m12 = 0;
		m.m20 = 0;
		m.m21 = 0;
		m.m22 = z;
		return m;
	}
	
	/**
	 * "Scales" the specified matrix. The specified matrix is set to a matrix that is computed by
	 * multiplying the matrix that represents this scale-transformation with the specified matrix
 	 * @param m The matrix to modify
	 * @return the specified matrix
	 * @throws NullPointerException if the specified parameter was null
	 */
	public Matrix3d scaleMatrix(Matrix3d m) {
		m.m00 *= x;
		m.m01 *= x;
		m.m02 *= x;
		m.m10 *= y;
		m.m11 *= y;
		m.m12 *= y;
		m.m20 *= z;
		m.m21 *= z;
		m.m22 *= z;
		return m;
	}
	
	/**
	 * "Scales" the specified matrix. The specified matrix is set to a matrix that is computed by
	 * multiplying the matrix that represents this scale-transformation with the specified matrix
 	 * @param m The matrix to modify
	 * @return the specified matrix
	 * @throws NullPointerException if the specified parameter was null
	 */
	public Matrix4d scaleMatrix(Matrix4d m) {
		m.m00 *= x;
		m.m01 *= x;
		m.m02 *= x;
		m.m03 *= x;
		m.m10 *= y;
		m.m11 *= y;
		m.m12 *= y;
		m.m13 *= y;
		m.m20 *= z;
		m.m21 *= z;
		m.m22 *= z;
		m.m23 *= z;
		return m;
	}
}

