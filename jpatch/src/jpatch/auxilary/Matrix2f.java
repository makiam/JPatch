package jpatch.auxilary;

public class Matrix2f {
	public float m00;
	public float m01;
	public float m10;
	public float m11;
	
	public Matrix2f() {
		m00 = 0;
		m01 = 0;
		m10 = 0;
		m11 = 0;
	}
	
	public Matrix2f(float[] v) {
		m00 = v[0];
		m01 = v[1];
		m10 = v[2];
		m11 = v[3];
	}
	
	public Matrix2f(float m00, float m01, float m10, float m11) {
		this.m00 = m00;
		this.m01 = m01;
		this.m10 = m10;
		this.m11 = m11;
	}
	
	public float determinant() {
		return m00 * m11 - m01 * m10;
	}
}

