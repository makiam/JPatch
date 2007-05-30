package com.jpatch.afw.vecmath;

import javax.vecmath.*;

/**
 * Static utility methods for 3d calculations
 */
public class Utils3d {
	
	/**
	 * Returns a new 4x4 identity matrix
	 * @return a new 4x4 identity matrix
	 */
	public static final Matrix4d createIdentityMatrix() {
		return new Matrix4d( 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
	}
	
	/**
	 * "Translates" the specified 4x4 transformation matrix by the specified vector by
	 * adding the vector to the rightmost column.
	 * @param matrix the matrix to modify
	 * @param vector the translation vector
	 * @return the specified matrix
	 */
	public static final Matrix4d translateMatrix(Matrix4d matrix, Vector3d vector) {
		matrix.m03 += vector.x;
		matrix.m13 += vector.y;
		matrix.m23 += vector.z;
		return matrix;
	}
	
	/**
	 * Returns the cosinus of the specified angle.
	 * This functions works around the performance problems of Java trigonometry functions
	 * on the x86 platform and the inaccuracy involved when converting large angles
	 * from degrees to radians.
	 * @param alpha the angle in degrees
	 * @return the cosinus of the specified angle
	 */
	public static final double degCos(double alpha){
		alpha = Math.abs(alpha) % 360;
		if (alpha > 295) return  Math.cos((alpha - 360) / 180 * Math.PI);
		if (alpha > 205) return  Math.sin((alpha - 270) / 180 * Math.PI);
		if (alpha > 135) return -Math.cos((alpha - 180) / 180 * Math.PI);
		if (alpha > 45)  return -Math.sin((alpha - 90)  / 180 * Math.PI);
		return Math.cos(alpha / 180 * Math.PI);
	}
	
	/**
	 * Returns the sinus of the specified angle.
	 * This functions works around the performance problems of Java trigonometry functions
	 * on the x86 platform and the inaccuracy involved when converting large angles
	 * from degrees to radians.
	 * @param alpha the angle in degrees
	 * @return the sinus of the specified angle
	 */
	public static final double degSin(double alpha){
		return degCos(alpha - 90);
	}
}
