package test;

import javax.vecmath.*;

public class MatrixTest {
	public static void main(String[] args) {
		Matrix3d m = new Matrix3d(
				2, 0, 0,
				0, 1, 0,
				0, 0, 1
		);
		
		Matrix3d m2 = new Matrix3d();
		m2.invert(m);
		System.out.println(m2);
		m2.transpose();
		System.out.println(m2);
		
		Vector3d v = new Vector3d(1, 2, 3);
		v.normalize();
		
		m2.transform(v);
		System.out.println(v.length());
	}
}
