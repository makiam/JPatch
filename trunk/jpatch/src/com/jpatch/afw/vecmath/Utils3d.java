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
	public static final Matrix4d translateMatrix(Matrix4d matrix, Tuple3d vector) {
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
	
	/**
	 * Computes the intersection point of the specified ray and the specified sphere. If there is an intersection,
	 * <i>intersectionPoint</i> is set to the point of intersection and true is returned. Otherwise <i>intersectionPoint</i>
	 * is not modified and false is returned. The specified <i>rayOrigin</i>, <i>rayDirection</i> and <i>sphereCenter</i> objects are not modified.
	 * The boolean <i>first</i> parameter is used to specify which of the two intersection points (1st or 2nd) should be computed.
	 * @param rayOrigin	the origin of the ray
	 * @param rayDirection the direction of the ray (it's possible to specify a non-normalized vector)
	 * @param sphereCenter the center of the sphere
	 * @param sphereRadius the radius of the sphere
	 * @param intersectionPoint the intersection point (will be set by this method if there is an intersection)
	 * @param first true to compute the 1st intersection, false to compute the 2nd intersection
	 * @return true if there is an intersection, false otherwise
	 */
	public static final boolean raySphereIntersection(Point3d rayOrigin, Vector3d rayDirection, Point3d sphereCenter, double sphereRadius, Point3d intersectionPoint, boolean first) {
		Vector3d rayDir = new Vector3d(rayDirection);
		rayDir.normalize();
		Vector3d distance = new Vector3d(rayOrigin);
		distance.sub(sphereCenter);
		double b = distance.dot(rayDir);
		double c = distance.dot(distance) - sphereRadius * sphereRadius;
		double d = b * b - c;
		if (d > 0) {
			double t = first ? -b - Math.sqrt(d) : -b + Math.sqrt(d);
			rayDir.scale(t);
			intersectionPoint.set(rayOrigin);
			intersectionPoint.add(rayDir);
			return true;
		}
		return false;
	}
			
}
