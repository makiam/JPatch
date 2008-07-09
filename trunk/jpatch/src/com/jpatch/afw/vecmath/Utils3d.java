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
	static final double PI_180 = Math.PI / 180.0;
	public static final double degCos(double alpha){
		alpha = Math.abs(alpha) % 360;
		if (alpha > 295) return  Math.cos((alpha - 360) * PI_180);
		if (alpha > 205) return  Math.sin((alpha - 270) * PI_180);
		if (alpha > 135) return -Math.cos((alpha - 180) * PI_180);
		if (alpha > 45)  return -Math.sin((alpha - 90)  * PI_180);
		return Math.cos(alpha * PI_180);
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
		if (Math.abs(b) < 0.01) {
			return false;
		}
		double distance = a / b;
		intersectionPoint.set(rayDir);
		intersectionPoint.scale(distance);
		intersectionPoint.add(rayOrigin);
		return true;
	}

	public static final double rayTriangleIntersection(Point3d rayOrigin, Vector3d rayDirection, Point3d v0, Point3d v1, Point3d v2) {
		final double EPSILON = 0.000001;
		Vector3d edge1 = new Vector3d();
		Vector3d edge2 = new Vector3d();
		Vector3d vP = new Vector3d();
		Vector3d vQ = new Vector3d();
		Vector3d vT = new Vector3d();
		edge1.sub(v1, v0);
		edge2.sub(v2, v0);
		vP.cross(rayDirection, edge2);
		double det = edge1.dot(vP);
		if (det > -EPSILON && det < EPSILON) {
			return Double.MAX_VALUE;
		}
		double invDet = 1.0 / det;
		vT.sub(rayOrigin, v0);
		double u = vT.dot(vP) * invDet;
		if (u < 0.0 || u > 1.0) {
			return Double.MAX_VALUE;
		}
		vQ.cross(vT, edge1);
		double v = rayDirection.dot(vQ) * invDet;
		if (v < 0.0 || (u + v) > 1.0) {
			return Double.MAX_VALUE;
		}
		double t = edge2.dot(vQ) * invDet;
		return t;
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
	 * Sets the specified transform matrix to a rotation matrix that reorients
	 * the coordinate system such that the z-axis points towards the specified
	 * zVector
	 * @param zVector
	 * @param transform
	 */
	public static void reorientTransform(Vector3d zVector, Matrix3d transform) {
		Vector3d vx = new Vector3d();
		Vector3d vy = new Vector3d();
		Vector3d vz = new Vector3d();
		vz.normalize(zVector);
		perpendicularVector(vz, vx);
		vy.cross(vx, vz);
		vy.normalize();
		transform.m00 = vx.x; transform.m01 = vy.x; transform.m02 = vz.x;
		transform.m10 = vx.y; transform.m11 = vy.y; transform.m12 = vz.y;
		transform.m20 = vx.z; transform.m21 = vy.z; transform.m22 = vz.z;
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
		final double vx = bx - ax;
		final double vy = by - ay;
		final double wx = px - ax;
		final double wy = py - ay;

		final double c1 = wx * vx + wy * vy; // dot product w dot v
		final double c2 = vx * vx + vy * vy; // dot product v dot v
		return c1 / c2;
	}

	/**
	 * Returns factor t such that a(1-t)+bt is the point on line ab (specified by ax, ay, az and bx, by, bz) closest to point p (px, py, pz).
	 * Note that only for 0 &lt; t &lt; 1 the computed point is on the line segment ab.
	 * @param ax x coordinate of point a (start of line segment)
	 * @param ay y coordinate of point a (start of line segment)
	 * @param az z coordinate of point a (start of line segment)
	 * @param bx x coordinate of point b (end of line segment)
	 * @param by y coordinate of point b (end of line segment)
	 * @param bz z coordinate of point a (end of line segment)
	 * @param px x coordinate of point p
	 * @param py y coordinate of point p
	 * @param pz z coordinate of point p
	 * @return factor t such that a(1-t)+bt is the point on line ab closest to p
	 */
	public static double closestPointOnLine(double ax, double ay, double az, double bx, double by, double bz, double px, double py, double pz) {
		final double vx = bx - ax;
		final double vy = by - ay;
		final double vz = bz - az;
		final double wx = px - ax;
		final double wy = py - ay;
		final double wz = pz - az;

		final double c1 = wx * vx + wy * vy + wz * vz; // dot product w dot v
		final double c2 = vx * vx + vy * vy + vz * vz; // dot product v dot v
		return c1 / c2;
	}

	public static double closestPointOnLine(Point3d a, Point3d b, Point3d p) {
		return closestPointOnLine(a.x, a.y, a.z, b.x, b.y, b.z, p.x, p.y, p.z);
	}

	/**
	 * Transforms specified vector v with specified matrix m.
	 * @param m matrix in row major order
	 * @param v
	 * @param result
	 * @return
	 */
	public static double[] transform(double[] m, double[] v, double[] result) {
		int n = v.length;
		if (m.length != n * n) {
			throw new javax.vecmath.MismatchedSizeException();
		}
		int i = 0;
		for (int row = 0; row < n; row++) {
			for (int col = 0; col < n; col++) {
				result[row] += v[col] * m[i++];
			}
		}
		return result;
	}

	public static final void printMatrix(double[] m) {
		final int dim = dim(m);
		int i = 0;
		for (int row = 0; row < dim; row++) {
			for (int col = 0; col < dim; col++) {
				System.out.print(m[i++]);
				System.out.print(" ");
			}
			System.out.println();
		}
	}
	
	private static final int dim(double[] m) {
		switch (m.length) {
		case 1*1: return 1;
		case 2*2: return 2;
		case 3*3: return 3;
		case 4*4: return 4;
		case 5*5: return 5;
		case 6*6: return 6;
		case 7*7: return 7;
		case 8*8: return 8;
		default: throw new MismatchedSizeException();
		}
	}
	
	/**
	 * Invert matrix m (row major)
	 * @param m
	 * @param result
	 * @return result
	 */
	public static final double[] invert(double[] m, double[] result) {
		final int size = m.length;
		final int dim = dim(m);
		final int[] row_perm = new int[dim];
		final int[] even_row_exchange = new int[1];
		
//		Calculate LU decomposition: Is the matrix singular? 
		if (!luDecomposition(dim, m, row_perm, even_row_exchange)) {
			// Matrix has no inverse 
			throw new SingularMatrixException();
		}

//		Perform back substitution on the identity matrix 
		for (int i = 0; i < size; i++) {
			result[i] = 0.0;
		}
		for (int i = 0; i < dim; i++) {
			result[i + i * dim] = 1.0;
		}
		luBacksubstitution(dim, m, row_perm, result);
		return result;
	}

	/**
	 * Solves a set of linear equations.  The input parameters "matrix1",
	 * and "row_perm" come from luDecompostion and do not change
	 * here.  The parameter "matrix2" is a set of column vectors assembled
	 * into a nxn matrix of floating-point values.  The procedure takes each
	 * column of "matrix2" in turn and treats it as the right-hand side of the
	 * matrix equation Ax = LUx = b.  The solution vector replaces the
	 * original column of the matrix.
	 *
	 * If "matrix2" is the identity matrix, the procedure replaces its contents
	 * with the inverse of the matrix from which "matrix1" was originally
	 * derived.
	 */
	//
	// Reference: Press, Flannery, Teukolsky, Vetterling, 
	//	      _Numerical_Recipes_in_C_, Cambridge University Press, 
	//	      1988, pp 44-45.
	//
	static void luBacksubstitution(int dim, double[] matrix1,
			int[] row_perm,
			double[] matrix2) {

		int i, ii, ip, j, k;
		int rp;
		int cv, rv, ri;
		double tt;

		// rp = row_perm;
		rp = 0;

		// For each column vector of matrix2 ... 
		for (k = 0; k < dim; k++) {
			// cv = &(matrix2[0][k]);
			cv = k;
			ii = -1;

			// Forward substitution 
			for (i = 0; i < dim; i++) {
				double sum;

				ip = row_perm[rp+i];
				sum = matrix2[cv+dim*ip];
				matrix2[cv+dim*ip] = matrix2[cv+dim*i];
				if (ii >= 0) {
					// rv = &(matrix1[i][0]);
					rv = i*dim;
					for (j = ii; j <= i-1; j++) {
						sum -= matrix1[rv+j] * matrix2[cv+dim*j];
					}
				}
				else if (sum != 0.0) {
					ii = i;
				}
				matrix2[cv+dim*i] = sum;
			}

			// Backsubstitution 
			for (i = 0; i < dim; i++) {
				ri = (dim-1-i);
				rv = dim*(ri);
				tt = 0.0;
				for(j=1;j<=i;j++) {
					tt += matrix1[rv+dim-j] * matrix2[cv+dim*(dim-j)]; 	  
				}
				matrix2[cv+dim*ri]= (matrix2[cv+dim*ri] - tt) / matrix1[rv+ri];
			}
		}
	}
    
	/**
	 * Given a nxn array "matrix0", this function replaces it with the 
	 * LU decomposition of a row-wise permutation of itself.  The input 
	 * parameters are "matrix0" and "dim".  The array "matrix0" is also 
	 * an output parameter.  The vector "row_perm[]" is an output 
	 * parameter that contains the row permutations resulting from partial 
	 * pivoting.  The output parameter "even_row_xchg" is 1 when the 
	 * number of row exchanges is even, or -1 otherwise.  Assumes data 
	 * type is always double.
	 *
	 * @return true if the matrix is nonsingular, or false otherwise.
	 */
	//
	// Reference: Press, Flannery, Teukolsky, Vetterling, 
	//	      _Numerical_Recipes_in_C_, Cambridge University Press, 
	//	      1988, pp 40-45.
	//
	private static final boolean luDecomposition(int dim, double[] matrix0,	int[] row_perm, int[] even_row_xchg) {

		double row_scale[] = new double[dim];

		// Determine implicit scaling information by looping over rows 
		int i, j;
		int ptr, rs, mtx;
		double big, temp;

		ptr = 0;
		rs = 0;
		even_row_xchg[0] = 1;

		// For each row ... 
		i = dim;
		while (i-- != 0) {
			big = 0.0;

			// For each column, find the largest element in the row 
			j = dim;
			while (j-- != 0) {
				temp = matrix0[ptr++];
				temp = Math.abs(temp);
				if (temp > big) {
					big = temp;
				}
			}

			// Is the matrix singular? 
			if (big == 0.0) {
				return false;
			}
			row_scale[rs++] = 1.0 / big;
		}

		// For all columns, execute Crout's method 
		mtx = 0;
		for (j = 0; j < dim; j++) {
			int imax, k;
			int target, p1, p2;
			double sum;

			// Determine elements of upper diagonal matrix U 
			for (i = 0; i < j; i++) {
				target = mtx + (dim*i) + j;
				sum = matrix0[target];
				k = i;
				p1 = mtx + (dim*i);
				p2 = mtx + j;
				while (k-- != 0) {
					sum -= matrix0[p1] * matrix0[p2];
					p1++;
					p2 += dim;
				}
				matrix0[target] = sum;
			}

			// Search for largest pivot element and calculate
			// intermediate elements of lower diagonal matrix L.
			big = 0.0;
			imax = -1;
			for (i = j; i < dim; i++) {
				target = mtx + (dim*i) + j;
				sum = matrix0[target];
				k = j;
				p1 = mtx + (dim*i);
				p2 = mtx + j;
				while (k-- != 0) {
					sum -= matrix0[p1] * matrix0[p2];
					p1++;
					p2 += dim;
				}
				matrix0[target] = sum;

				// Is this the best pivot so far? 
				if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
					big = temp;
					imax = i;
				}
			}

			if (imax < 0) {
				throw new RuntimeException("Logic error: imax < 0");
			}

			// Is a row exchange necessary? 
			if (j != imax) {
				// Yes: exchange rows 
				k = dim;
				p1 = mtx + (dim*imax);
				p2 = mtx + (dim*j);
				while (k-- != 0) {
					temp = matrix0[p1];
					matrix0[p1++] = matrix0[p2];
					matrix0[p2++] = temp;
				}

				// Record change in scale factor 
				row_scale[imax] = row_scale[j];
				even_row_xchg[0] = -even_row_xchg[0]; // change exchange parity
			}

			// Record row permutation 
			row_perm[j] = imax;

			// Is the matrix singular 
			if (matrix0[(mtx + (dim*j) + j)] == 0.0) {
				return false;
			}

			// Divide elements of lower diagonal matrix L by pivot 
			if (j != (dim-1)) {
				temp = 1.0 / (matrix0[(mtx + (dim*j) + j)]);
				target = mtx + (dim*(j+1)) + j;
				i = (dim-1) - j;
				while (i-- != 0) {
					matrix0[target] *= temp;
					target += dim;
				}
			}
		}
		return true;
	}

	public static void main(String[] args) {
		Point3d v0 = new Point3d(0, 0, 100);
		Point3d v1 = new Point3d(100, 0, 200);
		Point3d v2 = new Point3d(0, 100, 300);
		Point3d origin = new Point3d(1, 0, 0);
		Vector3d direction = new Vector3d(1, 1, 50);
//			direction.normalize();
		double t = rayTriangleIntersection(origin, direction, v0, v1, v2);
		Point3d p = new Point3d(
				origin.x + t * direction.x,
				origin.y + t * direction.y,
				origin.z + t * direction.z
		);
		System.out.println(p);
	}
}
