package com.jpatch.afw.vecmath;

import javax.vecmath.*;

/**
 * A Tuple3d object that represents a rotation. The x, y and z fields store the clockwise rotation
 * around the positive x, y and z axes respectively, in degrees. The order of rotations defaults to
 * X, Y, Z, but can be set to any other order specified in the order enum using the setOrder method.
 */
@SuppressWarnings("serial")
public class Rotation3d extends Tuple3d {
	private double m00, m01, m02;			// used to...
	private double m10, m11, m12;			// ...cache the...
	private double m20, m21, m22;			// ...rotation matrix
	private double oldX, oldY, oldZ;		// used to check wheter cached matrix is valid
	private boolean matrixInvalid = true;	// flag to force recomputation of cached matrix
	
	/**
	 * Enum to encapsulate the order of rotation
	 */
	public static enum Order {
		XYZ, XZY, YXZ, YZX, ZXY, ZYX;
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	};
	
	/**
	 * The order of rotation
	 */
	private Order order = Order.XYZ;
	
	/* suplerclass constructor */
	public Rotation3d() {
		super();
	}
	
	/* suplerclass constructor */
	public Rotation3d(double x, double y, double z) {
		super(x, y, z);
	}
	
	/* suplerclass constructor */
	public Rotation3d(double[] xyz) {
		super(xyz);
	}
	
	/* suplerclass constructor */
	public Rotation3d(Tuple3d tuple3d) {
		super(tuple3d);
	}
	
	/* suplerclass constructor */
	public Rotation3d(Tuple3f tuple3f) {
		super(tuple3f);
	}
	
	/**
	 * Returns the order of rotation
	 * @return the order of rotation
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * Sets the order of rotation
	 * @param order the order of rotation
	 */
	public void setOrder(Order order) {
		this.order = order;
		matrixInvalid = true;
	}
	
	/**
	 * Sets the specified matrix to a transformation matrix that represents this rotation.
	 * @param m The matrix to modify
	 * @return the specified matrix
	 * @throws NullPointerException if the specified parameter was null
	 */
	public Matrix3d getRotationMatrix(Matrix3d m) {
		computeMatrix();
		m.m00 = m00;
		m.m01 = m01;
		m.m02 = m02;
		m.m10 = m10;
		m.m11 = m11;
		m.m12 = m12;
		m.m20 = m20;
		m.m21 = m21;
		m.m22 = m22;
		return m;
	}
	
	
	/**
	 * Sets the 3x3 rotation/scale component of the specified matrix to a transformation matrix that represents this rotation.
	 * The translation part of the specified matrix is not modified! To get a 4x4 matrix that represents this rotation
	 * you must pass an identity 4x4 matrix to this method.
 	 * @param m The matrix to modify
	 * @return the specified matrix
	 * @throws NullPointerException if the specified parameter was null
	 */
	public Matrix4d getRotationMatrix(Matrix4d m) {
		computeMatrix();
		m.m00 = m00;
		m.m01 = m01;
		m.m02 = m02;
		m.m10 = m10;
		m.m11 = m11;
		m.m12 = m12;
		m.m20 = m20;
		m.m21 = m21;
		m.m22 = m22;
		return m;
	}
	
	/**
	 * "Rotates" the specified matrix. The specified matrix is set to a matrix thats computed by
	 * multiplying the matrix that represents this rotation with the specified matrix
 	 * @param m The matrix to modify
	 * @return the specified matrix
	 * @throws NullPointerException if the specified parameter was null
	 */
	public Matrix3d rotateMatrix(Matrix3d m) {
		computeMatrix();
		
		/*
		 * perform matrix multiplication
		 */
		double t00, t01, t02, t10, t11, t12, t20, t21, t22; // temporarily stores the result of the matrix multiplication
		t00 = m00 * m.m00 + m01 * m.m10 + m02 * m.m20;
		t01 = m00 * m.m01 + m01 * m.m11 + m02 * m.m21;
		t02 = m00 * m.m02 + m01 * m.m12 + m02 * m.m22;
		
		t10 = m10 * m.m00 + m11 * m.m10 + m12 * m.m20;
		t11 = m10 * m.m01 + m11 * m.m11 + m12 * m.m21;
		t12 = m10 * m.m02 + m11 * m.m12 + m12 * m.m22;
		
		t20 = m20 * m.m00 + m21 * m.m10 + m22 * m.m20;
		t21 = m20 * m.m01 + m21 * m.m11 + m22 * m.m21;
		t22 = m20 * m.m02 + m21 * m.m12 + m22 * m.m22;
		
		m.m00 = t00; m.m01 = t01; m.m02 = t02;
		m.m10 = t10; m.m11 = t11; m.m12 = t12;
		m.m20 = t20; m.m21 = t21; m.m22 = t22;
		
		return m;
	}
	
	/**
	 * "Rotates" the specified matrix. The specified matrix is set to a matrix that is computed by
	 * multiplying the matrix that represents this rotation with the specified matrix
 	 * @param m The matrix to modify
	 * @return the specified matrix
	 * @throws NullPointerException if the specified parameter was null
	 */
	public Matrix4d rotateMatrix(Matrix4d m) {
		computeMatrix();
		
		/*
		 * perform matrix multiplication
		 */
		double t00, t01, t02, t03, t10, t11, t12, t13, t20, t21, t22, t23; // temporarily stores the result of the matrix multiplication
		t00 = m00 * m.m00 + m01 * m.m10 + m02 * m.m20;
		t01 = m00 * m.m01 + m01 * m.m11 + m02 * m.m21;
		t02 = m00 * m.m02 + m01 * m.m12 + m02 * m.m22;
		t03 = m00 * m.m03 + m01 * m.m13 + m02 * m.m23;
		
		t10 = m10 * m.m00 + m11 * m.m10 + m12 * m.m20;
		t11 = m10 * m.m01 + m11 * m.m11 + m12 * m.m21;
		t12 = m10 * m.m02 + m11 * m.m12 + m12 * m.m22;
		t13 = m10 * m.m03 + m11 * m.m13 + m12 * m.m23;
		
		t20 = m20 * m.m00 + m21 * m.m10 + m22 * m.m20;
		t21 = m20 * m.m01 + m21 * m.m11 + m22 * m.m21;
		t22 = m20 * m.m02 + m21 * m.m12 + m22 * m.m22;
		t23 = m20 * m.m03 + m21 * m.m13 + m22 * m.m23;
		
		m.m00 = t00; m.m01 = t01; m.m02 = t02; m.m03 = t03;
		m.m10 = t10; m.m11 = t11; m.m12 = t12; m.m13 = t13;
		m.m20 = t20; m.m21 = t21; m.m22 = t22; m.m23 = t23;
		
		return m;
	}
	
	/**
	 * Sets this rotation to the rotation represented by matrix m
	 * @param m a rotation matrix
	 */
	public void setRotation(Matrix3d m) {
		switch(order) {
		case XYZ:
			x = Math.toDegrees(Math.atan2(-m.m12, m.m22));
			y = Math.toDegrees(Math.asin(m.m02));
			z = Math.toDegrees(Math.atan2(-m.m01, m.m00));
			if (z > 90) {
				z -= 180;
				x -= 180;
				y = 180 - y;
			} else if (z < -90) {
				z += 180;
				x -= 180;
				y = -180 - y;
			}
			break;
		case XZY:
			x = Math.toDegrees(Math.atan2(m.m21, m.m11));
			y = Math.toDegrees(Math.atan2(m.m02, m.m00));
			z = Math.toDegrees(Math.asin(-m.m01));
			if (y > 90) {
				y -= 180;
				x -= 180;
				z = 180 - z;
			} else if (y < -90) {
				y += 180;
				x -= 180;
				z = -180 - z;
			}
			break;
		case YXZ:
			x = Math.toDegrees(Math.asin(-m.m12));
			y = Math.toDegrees(Math.atan2(m.m02, m.m22));
			z = Math.toDegrees(Math.atan2(m.m10, m.m11));
			if (z > 90) {
				z -= 180;
				y -= 180;
				x = 180 - x;
			} else if (z < -90) {
				z += 180;
				y -= 180;
				x = -180 - x;
			}
			break;
		case YZX:
			x = Math.toDegrees(Math.atan2(-m.m12, m.m11));
			y = Math.toDegrees(Math.atan2(-m.m20, m.m00));
			z = Math.toDegrees(Math.asin(m.m10));
			if (x > 90) {
				x -= 180;
				y -= 180;
				z = 180 - z;
			} else if (x < -90) {
				x += 180;
				y -= 180;
				z = -180 - z;
			}
			break;
		case ZXY:
			x = Math.toDegrees(Math.asin(m.m21));
			y = Math.toDegrees(Math.atan2(-m.m20, m.m22));
			z = Math.toDegrees(Math.atan2(-m.m01, m.m11));
			if (y > 90) {
				y -= 180;
				z -= 180;
				x = 180 - z;
			} else if (y < -90) {
				y += 180;
				z -= 180;
				x = -180 - z;
			}
			break;
		case ZYX:
			x = Math.toDegrees(Math.atan2(m.m21, m.m22));
			y = Math.toDegrees(Math.asin(-m.m20));
			z = Math.toDegrees(Math.atan2(m.m10, m.m00));
			if (x > 90) {
				x -= 180;
				z -= 180;
				y = 180 - y;
			} else if (x < -90) {
				x += 180;
				z -= 180;
				y = -180 - y;
			}
			break;
		}
		if (x < -180)
			x += 360;
		else if (x > 180)
			x -= 360;
		if (y < -180)
			y += 360;
		else if (y > 180)
			y -= 360;
		if (z < -180)
			z += 360;
		else if (z > 180)
			z -= 360;
	}
	
	/**
	 * If necessary, recompute the matrix (trig functions are slow, especially in Java)
	 */
	private final void computeMatrix() {
		if (oldX != x || oldY != y || oldZ != z || matrixInvalid) {
			oldX = x;
			oldY = y;
			oldZ = z;
			matrixInvalid = false;
			double sx = Utils3d.degSin(x);
			double cx = Utils3d.degCos(x);
			double sy = Utils3d.degSin(y);
			double cy = Utils3d.degCos(y);
			double sz = Utils3d.degSin(z);
			double cz = Utils3d.degCos(z);
			switch(order) {
			case XYZ:
				m00 = cy * cz;
				m01 = -cy * sz;
				m02 = sy;
				m10 = sx * sy * cz + cx * sz;
				m11 = -sx * sy * sz + cx * cz;
				m12 = -sx * cy;
				m20 = -cx * sy * cz + sx * sz;
				m21 = cx * sy * sz + sx * cz;
				m22 = cx * cy;
				break;
			case XZY:
				m00 = cz * cy;
				m01 = -sz;
				m02 = cz * sy;
				m10 = cx * sz * cy + sx * sy;
				m11 = cx * cz;
				m12 = cx * sz * sy - sx * cy;
				m20 = sx * sz * cy - cx * sy;
				m21 = sx * cz;
				m22 = sx * sz * sy + cx * cy;
				break;
			case YXZ:
				m00 = cy * cz + sy * sx * sz;
				m01 = -cy * sz + sy * sx * cz;
				m02 = sy * cx;
				m10 = cx * sz;
				m11 = cx * cz;
				m12 = -sx;
				m20 = -sy * cz + cy * sx * sz;
				m21 = sy * sz + cy * sx * cz;
				m22 = cy * cx;
				break;
			case YZX:
				m00 = cy * cz;
				m01 = -cy * sz * cx + sy * sx;
				m02 = cy * sz * sx + sy * cx;
				m10 = sz;
				m11 = cz * cx;
				m12 = -cz * sx;
				m20 = -sy * cz;
				m21 = sy * sz * cx + cy * sx;
				m22 = -sy * sz * sx + cy * cx;
				break;
			case ZXY:
				m00 = cz * cy - sz * sx * sy;
				m01 = -sz * cx;
				m02 = cz * sy + sz * sx * cy;
				m10 = sz * cy + cz * sx * sy;
				m11 = cz * cx;
				m12 = sz * sy - cz * sx * cy;
				m20 = -cx * sy;
				m21 = sx;
				m22 = cx * cy;
				break;
			case ZYX:
				m00 = cz * cy;
				m01 = -sz * cx + cz * sy * sx;
				m02 = sz * sx + cz * sy * cx;
				m10 = sz * cy;
				m11 = cz * cx + sz * sy * sx;
				m12 = -cz * sx + sz * sy * cx;
				m20 = -sy;
				m21 = cy * sx;
				m22 = cy * cx;
				break;
			}
		}
	}
}

