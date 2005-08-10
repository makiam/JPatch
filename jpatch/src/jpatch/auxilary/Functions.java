package jpatch.auxilary;

import javax.vecmath.*;

public class Functions {
	private static Vector3f v3AxB = new Vector3f();
	private static Vector3f v3CxB = new Vector3f();
	private static Vector3f v3CxA = new Vector3f();
	
	public static float optimumCurvature(int segments) {
		double theta = Math.PI/(double)segments;
		double alpha = 2 * theta;
		double x0 = Math.cos(theta);
		double y0 = Math.sin(theta);
		double x1 = (4-Math.cos(theta))/3;
		double y1 = ((1-Math.cos(theta))*(Math.cos(theta)-3))/3/Math.sin(theta);
		double d = Math.sqrt((x1-x0)*(x1-x0)+(y1-y0)*(y1-y0));
		double l = 4*Math.sin(theta);
		double c = d/l;
		
		double l_jp = Math.sin(alpha) / 3;
		double l_sp = Math.sqrt((1 - Math.cos(alpha)) * (1 - Math.cos(alpha)) + (Math.sin(alpha) * Math.sin(alpha))) / 3;
		
		//System.out.println("c=" + c + " j_lp=" + l_jp + " l_sp=" + l_sp);
		return (float) (c / l_jp * l_sp * 3);
	}
	
	public static final Vector3f vector(Point3f A, Point3f B) {
		Vector3f v = new Vector3f(B);
		v.sub(A);
		return v;
	}
	
	public static final Vector3f scaledvector(Point3f A, Point3f B, float scale) {
		Vector3f v = new Vector3f(B);
		v.sub(A);
		v.scale(scale);
		return v;
	}
	
	public static final Vector3d vector(Point3d A, Point3d B) {
		Vector3d v = new Vector3d(B);
		v.sub(A);
		return v;
	}
	
	public static final Vector3d scaledvector(Point3d A, Point3d B, double scale) {
		Vector3d v = new Vector3d(B);
		v.sub(A);
		v.scale(scale);
		return v;
	}
	
	public static final Vector3f vector(Vector3f A, Vector3f B, float alpha) {
		Vector3f v = new Vector3f();
		v.interpolate(A,B,alpha);
		return v;
	}
	
	public static final Point3f parallelogram(Tuple3f a, Tuple3f b, Tuple3f c) {
		return new Point3f(
			b.x + c.x - a.x,
			b.y + c.y - a.y,
			b.z + c.z - a.z
		);
	}
	
	public static final Point3f average(Tuple3f a, Tuple3f b) {
		return new Point3f(
			(a.x + b.x) * 0.5f,
			(a.y + b.y) * 0.5f,
			(a.z + b.z) * 0.5f
		);
	}
	
	public static final Vector3f vaverage(Tuple3f a, Tuple3f b) {
		return new Vector3f(
			(a.x + b.x) * 0.5f,
			(a.y + b.y) * 0.5f,
			(a.z + b.z) * 0.5f
		);
	}
	
	public static final Point3f average(Tuple3f a, Tuple3f b, Tuple3f c) {
		return new Point3f(
			(a.x + b.x + c.x) * 1f/3f,
			(a.y + b.y + c.y) * 1f/3f,
			(a.z + b.z + c.z) * 1f/3f
		);
	}
	
	public static final Vector3f vaverage(Tuple3f a, Tuple3f b, Tuple3f c) {
		return new Vector3f(
			(a.x + b.x + c.x) * 1f/3f,
			(a.y + b.y + c.y) * 1f/3f,
			(a.z + b.z + c.z) * 1f/3f
		);
	}
	
	public static final Point3f average(Tuple3f a, Tuple3f b, Tuple3f c, Tuple3f d) {
		return new Point3f(
			(a.x + b.x + c.x + d.x) * 0.25f,
			(a.y + b.y + c.y + d.y) * 0.25f,
			(a.z + b.z + c.z + d.z) * 0.25f
		);
	}
	
	public static final Vector3f vaverage(Tuple3f a, Tuple3f b, Tuple3f c, Tuple3f d) {
		return new Vector3f(
			(a.x + b.x + c.x + d.x) * 0.25f,
			(a.y + b.y + c.y + d.y) * 0.25f,
			(a.z + b.z + c.z + d.z) * 0.25f
		);
	}
	
	public static final Point3f average(Tuple3f a, Tuple3f b, Tuple3f c, Tuple3f d, Tuple3f e) {
		return new Point3f(
			(a.x + b.x + c.x + d.x + e.x) * 0.2f,
			(a.y + b.y + c.y + d.y + e.y) * 0.2f,
			(a.z + b.z + c.z + d.z + e.z) * 0.2f
		);
	}
	
	public static final Vector3f vaverage(Tuple3f a, Tuple3f b, Tuple3f c, Tuple3f d, Tuple3f e) {
		return new Vector3f(
			(a.x + b.x + c.x + d.x + e.x) * 0.2f,
			(a.y + b.y + c.y + d.y + e.y) * 0.2f,
			(a.z + b.z + c.z + d.z + e.z) * 0.2f
		);
	}
	
	
	
	public static final Point3d parallelogram(Tuple3d a, Tuple3d b, Tuple3d c) {
		return new Point3d(
			b.x + c.x - a.x,
			b.y + c.y - a.y,
			b.z + c.z - a.z
		);
	}
	
	public static final Point3d average(Tuple3d a, Tuple3d b) {
		return new Point3d(
			(a.x + b.x) * 0.5,
			(a.y + b.y) * 0.5,
			(a.z + b.z) * 0.5
		);
	}
	
	public static final Vector3d vaverage(Tuple3d a, Tuple3d b) {
		return new Vector3d(
			(a.x + b.x) * 0.5,
			(a.y + b.y) * 0.5,
			(a.z + b.z) * 0.5
		);
	}
	
	public static final Point3d average(Tuple3d a, Tuple3d b, Tuple3d c) {
		return new Point3d(
			(a.x + b.x + c.x) * 1/3,
			(a.y + b.y + c.y) * 1/3,
			(a.z + b.z + c.z) * 1/3
		);
	}
	
	public static final Vector3d vaverage(Tuple3d a, Tuple3d b, Tuple3d c) {
		return new Vector3d(
			(a.x + b.x + c.x) * 1/3,
			(a.y + b.y + c.y) * 1/3,
			(a.z + b.z + c.z) * 1/3
		);
	}
	
	public static final Point3d average(Tuple3d a, Tuple3d b, Tuple3d c, Tuple3d d) {
		return new Point3d(
			(a.x + b.x + c.x + d.x) * 0.25,
			(a.y + b.y + c.y + d.y) * 0.25,
			(a.z + b.z + c.z + d.z) * 0.25
		);
	}
	
	public static final Vector3d vaverage(Tuple3d a, Tuple3d b, Tuple3d c, Tuple3d d) {
		return new Vector3d(
			(a.x + b.x + c.x + d.x) * 0.25,
			(a.y + b.y + c.y + d.y) * 0.25,
			(a.z + b.z + c.z + d.z) * 0.25
		);
	}
	
	public static final Point3d average(Tuple3d a, Tuple3d b, Tuple3d c, Tuple3d d, Tuple3d e) {
		return new Point3d(
			(a.x + b.x + c.x + d.x + e.x) * 0.2,
			(a.y + b.y + c.y + d.y + e.y) * 0.2,
			(a.z + b.z + c.z + d.z + e.z) * 0.2
		);
	}
	
	public static final Vector3d vaverage(Tuple3d a, Tuple3d b, Tuple3d c, Tuple3d d, Tuple3d e) {
		return new Vector3d(
			(a.x + b.x + c.x + d.x + e.x) * 0.2,
			(a.y + b.y + c.y + d.y + e.y) * 0.2,
			(a.z + b.z + c.z + d.z + e.z) * 0.2
		);
	}
	
	/**
	* Computes the point of intersections of two lines, (1-s)*x1 + s*x2 and (1-t)*x3 + t*(x4), assuming
	* that x1,x2,x3,x4 are coplanar. Returns a Tuple2f of (s,t)
	**/
	public static final Tuple2f lineLineIntersection(Point3f x1, Point3f x2, Point3f x3, Point3f x4) {
		Vector3f a = vector(x1,x2);
		Vector3f b = vector(x3,x4);
		Vector3f c = vector(x1,x3);
		v3AxB.cross(a,b);
		v3CxB.cross(c,b);
		v3CxA.cross(c,a);
		float l = v3AxB.lengthSquared();
		float s = v3CxB.dot(v3AxB) / l;
		float t = v3CxA.dot(v3AxB) / l;
		return new Vector2f(s,t);
	}
		
	/**
	* Solves a system of linear equations Au = b where A is the coefficient matrix
	* and b are 3d vectors using Gauss elimination.
	* It takes the extended matrix of the form e.g.:
	* a0,0 a0,1 a0,2 b0.x, b0.y, b0.z
	* a1,0 a1,1 a1,2 b1.x, b1.y, b1.z
	* a2,0 a2,1 a2,2 b2.x, b2.y, b2.z
	* as input and returns an array of Point3f containing the unknowns u
	**/
	public static final Point3f[] solve(float[][] A) {
		int rows = A.length;
		int cols = A[0].length;
		for (int j = 0; j < cols - 3; j++) {
			float amax = Math.abs(A[j][j]);
			int imax = j;
			for (int i = j + 1; i < rows; i++) {
				if (Math.abs(A[i][j]) > amax) {
					imax = i;
					amax = Math.abs(A[i][j]);
				}
			}
			if (imax != j) {
				for (int i = 0; i < cols; i++) {
					amax = A[imax][i];
					A[imax][i] = A[j][i];
					A[j][i] = amax;
				}
			}
			for (int i = j + 1; i < rows; i++) {
				A[i][j] /= A[j][j];
				for (int k = j + 1; k < cols; k++) {
					A[i][k] = A[i][k] - A[i][j] * A[j][k];
				}
			}
		}
		Point3f[] u = new Point3f[rows];
		u[rows - 1] = new Point3f(A[rows - 1][cols - 3] / A[rows - 1][cols - 4],A[rows - 1][cols - 2] / A[rows - 1][cols - 4],A[rows - 1][cols - 1] / A[rows - 1][cols - 4]);
		for (int j = rows - 2; j >= 0; j--) {
			u[j] = new Point3f(A[j][cols - 3],A[j][cols - 2],A[j][cols - 1]);
			for (int i = j + 1; i < cols - 3; i++) {
				u[j].x -= A[j][i] * u[i].x;
				u[j].y -= A[j][i] * u[i].y;
				u[j].z -= A[j][i] * u[i].z;
			}
			u[j].x /= A[j][j];
			u[j].y /= A[j][j];
			u[j].z /= A[j][j];
		}
		return u;
	}
	
	///**
	//* a test routine for linelineIntersection
	//**/
	//public static void main(String[] args) {
	//	Point3f x1 = new Point3f(-2,-1,0);
	//	Point3f x2 = new Point3f(2,3,0);
	//	Point3f x3 = new Point3f(-2,4,0);
	//	Point3f x4 = new Point3f(4,0,0);
	//	System.out.println(lineLineIntersection(x1,x2,x3,x4));
	//}
	
	///**
	//* a test routine for the linear equation solver above
	//**/
	public static void main(String[] args) {
		float[][] A = new float[][] {
		//	{1,1,0,0,0,0,0,0,0,1,2,3},
		//	{1,1,1,1,0,0,0,0,0,2,3,4},
		//	{0,0,1,1,0,0,0,0,0,-2,3,2},
		//	{0,0,0,0,1,1,0,0,0,1,3,0},
		//	{0,0,1,0,1,1,1,0,0,3,1,-2},
		//	{0,0,1,0,0,0,1,0,0,2,2,2},
		//	{0,0,0,0,0,0,0,1,1,3,3,3},
		//	{0,0,0,1,0,0,1,1,1,1,2,4},
		//	{0,0,0,1,0,0,1,0,0,2,3,4}
			{1,0,0,0,1,0,0},
			{0,0,1,1,7,0,0},
			{1,0,1,0,4,0,0},
			{0,1,0,1,6,0,0},
		};
		Point3f[] u = solve(A);
		for (int i = 0; i < u.length; i++) {
			System.out.println(u[i]);
		}
	}
	
	//	int o = 18;
	//	float[][] A = new float[o][o + 3];
	//	Point3f[] u = new Point3f[o];
	//	for (int i = 0; i < o; i++) {
	//		u[i] = new Point3f((float) (Math.random() * Math.random() * 10),(float) (Math.random() * Math.random() * 10),(float) (Math.random() * Math.random() * 10));
	//		for (int j = 0; j < o; j++) {
	//			A[i][j] = (float) (Math.random() * 10);
	//		}
	//	}
	//	for (int i = 0; i < o; i++) {
	//		Point3f b = new Point3f();
	//		for (int j = 0; j < o; j++) {
	//			b.x += u[j].x * A[i][j];
	//			b.y += u[j].y * A[i][j];
	//			b.z += u[j].z * A[i][j];
	//		}
	//		A[i][o] = b.x;
	//		A[i][o + 1] = b.y;
	//		A[i][o + 2] = b.z;
	//	}
	//	//long start = System.currentTimeMillis();
	//	//Point3f[] u2 = null;
	//	//for (int l = 0; l < 1000; l++) {
	//	Point3f[] u2 = solve(A);
	//	//}
	//	//System.out.println(System.currentTimeMillis() - start + " microseconds");
	//	for (int i = 0; i < u.length; i++) {
	//		u2[i].sub(u[i]);
	//		System.out.println(u2[i]);
	//	}
	//}
}
