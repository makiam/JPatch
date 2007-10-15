package com.jpatch.afw.vecmath;

import javax.vecmath.*;

/**
 * Static utility methods for 3d calculations
 */
public class Utils3d {
	private static final Vector3d VX = new Vector3d(1,0,0);
	private static final Vector3d VZ = new Vector3d(0,0,1);
	
	/**
	 * Returns a new 4x4 identity matrix
	 * @return a new 4x4 identity matrix
	 */
	public static final Matrix4d createIdentityMatrix4d() {
		return new Matrix4d(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
	}
	
	/**
	 * Returns a new 3x3 identity matrix
	 * @return a new 3x3 identity matrix
	 */
	public static final Matrix3d createIdentityMatrix3d() {
		return new Matrix3d(1, 0, 0, 0, 1, 0, 0, 0, 1);
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
	public static final boolean raySphereIntersection(Point3d rayOrigin, Vector3d rayDirection, Point3d sphereCenter, double sphereRadius, Tuple3d intersectionPoint, boolean first) {
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
	
	public static final boolean rayPlaneIntersection(Point3d rayOrigin, Vector3d rayDirection, Point3d pointOnPlane, Vector3d planeNormal, Tuple3d intersectionPoint) {
		Vector3d rayDir = new Vector3d(rayDirection);
		rayDir.normalize();
		Vector3d normal = new Vector3d(planeNormal);
		normal.normalize();
		Vector3d v = new Vector3d(pointOnPlane);
		v.sub(rayOrigin);
		double a = v.dot(normal);
		double b = rayDir.dot(normal);
//		System.out.println("b=" + b);
		if (Math.abs(b) < 0.01) {
			return false;
		}
		double distance = a / b;
//		System.out.println("distance=" + distance);
//		if (distance < 0.1) {
//			return false;
//		}
		intersectionPoint.set(rayDir);
		intersectionPoint.scale(distance);
		intersectionPoint.add(rayOrigin);
		return true;
	}
	
	/**
	 * Computes a vector perpendicular to the specified vector. The result is stored in the specified
	 * perpendicularVector
	 * @param vector the vector
	 * @param perpendicularVector will be set to a vector perpendicular to the specified vector
	 * @return the specified perpendicularVector
	 */
	public static Vector3d perpendicularVector(Vector3d vector, Vector3d perpendicularVector) {
		/* check if vector == (0,0,0) */
		if (vector.x == 0 && vector.y == 0 && vector.z == 0) {
			/* return (0, 0, 0) */
			perpendicularVector.set(0, 0, 0);
		} else {
			/* compute perpendicular vector */
			double ax = Math.abs(vector.x);
			double ay = Math.abs(vector.y);
			double az = Math.abs(vector.z);
			double dm = Math.max(ax,Math.max(ay,az));
			if (ax == dm) {
				if (vector.x < 0)
					perpendicularVector.cross(VZ, vector);
				else
					perpendicularVector.cross(vector,VZ);
			} else if (az == dm) {
				if (vector.z < 0)
					perpendicularVector.cross(vector,VX);
				else
					perpendicularVector.cross(VX, vector);
			} else {
				if (vector.y < 0)
					perpendicularVector.cross(vector,VX);
				else
					perpendicularVector.cross(VX, vector);
			}
			perpendicularVector.normalize();
		}
//		System.out.println("vector perpendicular to " + vector + " is " + perpendicularVector);
		return perpendicularVector;
	}
	
	/**
	 * Returns factor t such that a(1-t)+bt is the point on line ab (specified by ax, ay, bx and by) closest to p (specified by px and py).
	 * Note that only for 0 &lt; t &lt; 1 the computed point is on the line segment ab.
	 * @param ax x coordinate of point a (start of line segment)
	 * @param ay y coordinate of point a (start of line segment)
	 * @param bx x coordinate of point b (end of line segment)
	 * @param by y coordinate of point b (end of line segment)
	 * @param px x coordinate of point p
	 * @param py y coordinate of point p
	 * @return factor t such that a(1-t)+bt is the point on line ab closest to p
	 */
	public static double closestPointOnLine(double ax, double ay, double bx, double by, double px, double py) {
		double vx = bx - ax;
		double vy = by - ay;
		double wx = px - ax;
		double wy = py - ay;
		
		double c1 = wx * vx + wy * vy; // dot product w dot v
		double c2 = vx * vx + vy * vy; // dot product v dot v
		return c1 / c2;
	}
}
