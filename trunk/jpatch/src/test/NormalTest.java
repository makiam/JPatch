package test;

import javax.vecmath.*;

public class NormalTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Matrix3d matrix = new Matrix3d();
		Vector3d uTangent = new Vector3d(1, 0, 0);
		Vector3d vTangent = new Vector3d(1, 1, 0);
		Vector3d normal = new Vector3d();
		normal.cross(uTangent, vTangent);
		normal.normalize();
		System.out.println(normal);
		double nLength = 0.5 * (uTangent.length() + vTangent.length());
		matrix.m00 = uTangent.x; matrix.m01 = vTangent.x; matrix.m02 = normal.x * nLength;
		matrix.m10 = uTangent.y; matrix.m11 = vTangent.y; matrix.m12 = normal.y * nLength;
		matrix.m20 = uTangent.z; matrix.m21 = vTangent.z; matrix.m22 = normal.z * nLength;
		System.out.println(matrix);
		Point3d p = new Point3d(-1, 2, 3);
		System.out.println(p);
		matrix.transform(p);
		System.out.println(p);
		matrix.invert();
		matrix.transform(p);
		System.out.println(p);
	}

}
