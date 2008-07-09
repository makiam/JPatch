package test;

import com.jpatch.afw.vecmath.*;

import java.util.*;

import javax.vecmath.*;

public class GMatrixTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double[] m = new double[16];
		Matrix4d m4 = new Matrix4d();
		m4.rotX(1);
		m4.setTranslation(new Vector3d(-1, -2, -3));
		int i = 0;
		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				m[i++] = m4.getElement(row, col);
			}
		}
		m4.invert();
		System.out.println(m4);
		Point4d p = new Point4d(1, 2, 3, 4);
		m4.transform(p);
		double[] v = new double[] { 1, 2, 3, 4 };
		double[] inv = Utils3d.invertGeneral(m, new double[16]);
		double[] result = Utils3d.transform(inv, v, new double[4]);
		Utils3d.printMatrix(inv);
		System.out.println(Arrays.toString(result));
		System.out.println(p);
	}
}
