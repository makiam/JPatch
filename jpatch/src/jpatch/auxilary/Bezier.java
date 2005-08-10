package jpatch.auxilary;
import javax.vecmath.*;

/**
 *  Description of the Class
 *
 * @author     aledinsk
 * @created    18. M�rz 2003
 */
public final class Bezier {
	
	private static Point3f p0;
	private static Point3f p1;
	private static Point3f p2;
	private static Point3f p3;
	/**
	 *  Description of the Method
	 *
	 * @param  alpha  Description of the Parameter
	 * @param  p0     Description of the Parameter
	 * @param  p1     Description of the Parameter
	 * @param  p2     Description of the Parameter
	 * @param  p3     Description of the Parameter
	 * @return        Point3f[7]
	 */
	public static final Point3f[] deCasteljau(Point3f p0, Point3f p1, Point3f p2, Point3f p3, float alpha) {
		Point3f[] result = new Point3f[7];
		Point3f p12;
		p12 = new Point3f(p1);
		p12.interpolate(p2, alpha);
		result[0] = new Point3f(p0);
		result[1] = new Point3f(p0);
		result[1].interpolate(p1, alpha);
		result[2] = new Point3f(result[1]);
		result[2].interpolate(p12, alpha);
		result[6] = new Point3f(p3);
		result[5] = new Point3f(p2);
		result[5].interpolate(p3, alpha);
		result[4] = p12;
		result[4].interpolate(result[5], alpha);
		result[3] = new Point3f(result[2]);
		result[3].interpolate(result[4], alpha);
		return result;
	}
	
	public static final Point3d[] deCasteljau(Point3d p0, Point3d p1, Point3d p2, Point3d p3, double alpha) {
		Point3d[] result = new Point3d[7];
		Point3d p12;
		p12 = new Point3d(p1);
		p12.interpolate(p2, alpha);
		result[0] = new Point3d(p0);
		result[1] = new Point3d(p0);
		result[1].interpolate(p1, alpha);
		result[2] = new Point3d(result[1]);
		result[2].interpolate(p12, alpha);
		result[6] = new Point3d(p3);
		result[5] = new Point3d(p2);
		result[5].interpolate(p3, alpha);
		result[4] = p12;
		result[4].interpolate(result[5], alpha);
		result[3] = new Point3d(result[2]);
		result[3].interpolate(result[4], alpha);
		return result;
	}
	
	public static final void deCasteljau(Point3f p0, Point3f p1, Point3f p2, Point3f p3, float alpha, Point3f[] pointArray, int index) {
		Point3f[] acp = deCasteljau(p0,p1,p2,p3,alpha);
		for (int i = 0; i < 7; i++) {
			pointArray[index + i] = acp[i];
		}
	}

	 public static final void prepare (Point3f A, Point3f B, Point3f C, Point3f D) {
		 p0 = A;
		 p1 = B;
		 p2 = C;
		 p3 = D;
	 }
	 
	 public static final Point3f evaluate(float t) {
		float t1 = 1 - t;
		float t1_2 = t1 * t1;
		float t_2 = t * t;
		float B0 = t1_2 * t1;
		float B1 = 3 * t * t1_2;
		float B2 = 3 * t_2 * t1;
		float B3 = t_2 * t;
		return new Point3f(
			B0 * p0.x + B1 * p1.x + B2 * p2.x + B3 * p3.x,
			B0 * p0.y + B1 * p1.y + B2 * p2.y + B3 * p3.y,
			B0 * p0.z + B1 * p1.z + B2 * p2.z + B3 * p3.z
		);
	 }
	
	
	public static final void evaluate(Point3f p0, Point3f p1, Point3f p2, Point3f p3, Point3f[] result) {
		//result[0].set(p0);
		int num = result.length - 1;
		for (int i = 1; i < num; i++) {
			float t = (float) i / num;
			float t1 = 1 - t;
			float t1_2 = t1 * t1;
			float t_2 = t * t;
			float B0 = t1_2 * t1;
			float B1 = 3 * t * t1_2;
			float B2 = 3 * t_2 * t1;
			float B3 = t_2 * t;
			result[i].set(
				B0 * p0.x + B1 * p1.x + B2 * p2.x + B3 * p3.x,
				B0 * p0.y + B1 * p1.y + B2 * p2.y + B3 * p3.y,
				B0 * p0.z + B1 * p1.z + B2 * p2.z + B3 * p3.z
			);
		}
		result[0].set(p0);
		result[num].set(p3);
	}
	
	public static final void evaluate(Point3d p0, Point3d p1, Point3d p2, Point3d p3, Point3d[] result) {
		//result[0].set(p0);
		int num = result.length - 1;
		for (int i = 1; i < num; i++) {
			double t = (double) i / num;
			double t1 = 1 - t;
			double t1_2 = t1 * t1;
			double t_2 = t * t;
			double B0 = t1_2 * t1;
			double B1 = 3 * t * t1_2;
			double B2 = 3 * t_2 * t1;
			double B3 = t_2 * t;
			result[i].set(
				B0 * p0.x + B1 * p1.x + B2 * p2.x + B3 * p3.x,
				B0 * p0.y + B1 * p1.y + B2 * p2.y + B3 * p3.y,
				B0 * p0.z + B1 * p1.z + B2 * p2.z + B3 * p3.z
			);
		}
		result[0].set(p0);
		result[num].set(p3);
	}
	
	public static final Point3f evaluate(Point3f p0, Point3f p1, Point3f p2, Point3f p3, float t) {
		if (t == 0.5f) {
			return new Point3f(
				0.125f * p0.x + 0.375f * p1.x + 0.375f * p2.x + 0.125f * p3.x,
				0.125f * p0.y + 0.375f * p1.y + 0.375f * p2.y + 0.125f * p3.y,
				0.125f * p0.z + 0.375f * p1.z + 0.375f * p2.z + 0.125f * p3.z
			);
		} else {
			float t1 = 1 - t;
			float t1_2 = t1 * t1;
			float t_2 = t * t;
			float B0 = t1_2 * t1;
			float B1 = 3 * t * t1_2;
			float B2 = 3 * t_2 * t1;
			float B3 = t_2 * t;
			return new Point3f(
				B0 * p0.x + B1 * p1.x + B2 * p2.x + B3 * p3.x,
				B0 * p0.y + B1 * p1.y + B2 * p2.y + B3 * p3.y,
				B0 * p0.z + B1 * p1.z + B2 * p2.z + B3 * p3.z
			);
		}
	}
	
	public static final Point3f[] elevateDegree(Point3f p0, Point3f p1, Point3f p2, Point3f p3) {
		return new Point3f[] {
			new Point3f(p0),
			new Point3f(p0.x * 0.25f + p1.x * 0.75f,p0.y * 0.25f + p1.y * 0.75f,p0.z * 0.25f + p1.z * 0.75f),
			new Point3f(p1.x * 0.5f + p2.x * 0.5f,p1.y * 0.5f + p2.y * 0.5f,p1.z * 0.5f + p2.z * 0.5f),
			new Point3f(p2.x * 0.75f + p3.x * 0.25f,p2.y * 0.75f + p3.y * 0.25f,p2.z * 0.75f + p3.z * 0.25f),
			new Point3f(p3)
		};
	}
}

