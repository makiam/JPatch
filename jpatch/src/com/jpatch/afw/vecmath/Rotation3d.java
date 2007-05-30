package com.jpatch.afw.vecmath;

import javax.vecmath.*;

/**
 * A Tuple3d object that represents a rotation. The x, y and z fields store the clockwise rotation
 * around the positive x, y and z axes respectively, in degrees. The order of rotations defaults to
 * X, Y, Z, but can be set to any other order specified in the Order Enum using the setOrder method.
 */
@SuppressWarnings("serial")
public class Rotation3d extends Tuple3d {
	private double sinX, sinY, sinZ;	// cached sinus values
	private double cosX, cosY, cosZ;	// cached cosinus values
	private double oldX, oldY, oldZ;	// used to check wheter cached sin and cos values are valid
	
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
	}
	
	/**
	 * Sets the specified matrix to a transformation matrix that represents this rotation.
	 * @param m The matrix to modify
	 * @return the specified matrix
	 * @throws NullPointerException if the specified parameter was null
	 */
	public Matrix3d getRotationMatrix(Matrix3d m) {
		computeTrig();
		switch(order) {
		case XYZ:
			m.m00 = cosY * cosZ;
			m.m01 = -cosY * sinZ;
			m.m02 = sinY;
			m.m10 = sinX * sinY * cosZ + cosX * sinZ;
			m.m11 = -sinX * sinY * sinZ + cosX * cosZ;
			m.m12 = -sinX * cosY;
			m.m20 = -cosX * sinY * cosZ + sinX * sinZ;
			m.m21 = cosX * sinY * sinZ + sinX * cosZ;
			m.m22 = cosX * cosY;
			break;
		case XZY:
			m.m00 = cosZ * cosY;
			m.m01 = -sinZ;
			m.m02 = cosZ * sinY;
			m.m10 = cosX * sinZ * cosY + sinX * sinY;
			m.m11 = cosX * cosZ;
			m.m12 = cosX * sinZ * sinY - sinX * cosY;
			m.m20 = sinX * sinZ * cosY - cosX * sinY;
			m.m21 = sinX * cosZ;
			m.m22 = sinX * sinZ * sinY + cosX * cosY;
			break;
		case YXZ:
			m.m00 = cosY * cosZ + sinY * sinX * sinZ;
			m.m01 = -cosY * sinZ + sinY * sinX * cosZ;
			m.m02 = sinY * cosX;
			m.m10 = cosX * sinZ;
			m.m11 = cosX * cosZ;
			m.m12 = -sinX;
			m.m20 = -sinY * cosZ + cosY * sinX * sinZ;
			m.m21 = sinY * sinZ + cosY * sinX * cosZ;
			m.m22 = cosY * cosX;
			break;
		case YZX:
			m.m00 = cosY * cosZ;
			m.m01 = -cosY * sinZ * cosX + sinY * sinX;
			m.m02 = cosY * sinZ * sinX + sinY * cosX;
			m.m10 = sinZ;
			m.m11 = cosZ * cosX;
			m.m12 = -cosZ * sinX;
			m.m20 = -sinY * cosZ;
			m.m21 = sinY * sinZ * cosX + cosY * sinX;
			m.m22 = -sinY * sinZ * sinX + cosY * cosX;
			break;
		case ZXY:
			m.m00 = cosZ * cosY - sinZ * sinX * sinY;
			m.m01 = -sinZ * cosX;
			m.m02 = cosZ * sinY + sinZ * sinX * cosY;
			m.m10 = sinZ * cosY + cosZ * sinX * sinY;
			m.m11 = cosZ * cosX;
			m.m12 = sinZ * sinY - cosZ * sinX * cosY;
			m.m20 = -cosX * sinY;
			m.m21 = sinX;
			m.m22 = cosX * cosY;
			break;
		case ZYX:
			m.m00 = cosZ * cosY;
			m.m01 = -sinZ * cosX + cosZ * sinY * sinX;
			m.m02 = sinZ * sinX + cosZ * sinY * cosX;
			m.m10 = sinZ * cosY;
			m.m11 = cosZ * cosX + sinZ * sinY * sinX;
			m.m12 = -cosZ * sinX + sinZ * sinY * cosX;
			m.m20 = -sinY;
			m.m21 = cosY * sinX;
			m.m22 = cosY * cosX;
			break;
		}
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
		computeTrig();
		switch(order) {
		case XYZ:
			m.m00 = cosY * cosZ;
			m.m01 = -cosY * sinZ;
			m.m02 = sinY;
			m.m10 = sinX * sinY * cosZ + cosX * sinZ;
			m.m11 = -sinX * sinY * sinZ + cosX * cosZ;
			m.m12 = -sinX * cosY;
			m.m20 = -cosX * sinY * cosZ + sinX * sinZ;
			m.m21 = cosX * sinY * sinZ + sinX * cosZ;
			m.m22 = cosX * cosY;
			break;
		case XZY:
			m.m00 = cosZ * cosY;
			m.m01 = -sinZ;
			m.m02 = cosZ * sinY;
			m.m10 = cosX * sinZ * cosY + sinX * sinY;
			m.m11 = cosX * cosZ;
			m.m12 = cosX * sinZ * sinY - sinX * cosY;
			m.m20 = sinX * sinZ * cosY - cosX * sinY;
			m.m21 = sinX * cosZ;
			m.m22 = sinX * sinZ * sinY + cosX * cosY;
			break;
		case YXZ:
			m.m00 = cosY * cosZ + sinY * sinX * sinZ;
			m.m01 = -cosY * sinZ + sinY * sinX * cosZ;
			m.m02 = sinY * cosX;
			m.m10 = cosX * sinZ;
			m.m11 = cosX * cosZ;
			m.m12 = -sinX;
			m.m20 = -sinY * cosZ + cosY * sinX * sinZ;
			m.m21 = sinY * sinZ + cosY * sinX * cosZ;
			m.m22 = cosY * cosX;
			break;
		case YZX:
			m.m00 = cosY * cosZ;
			m.m01 = -cosY * sinZ * cosX + sinY * sinX;
			m.m02 = cosY * sinZ * sinX + sinY * cosX;
			m.m10 = sinZ;
			m.m11 = cosZ * cosX;
			m.m12 = -cosZ * sinX;
			m.m20 = -sinY * cosZ;
			m.m21 = sinY * sinZ * cosX + cosY * sinX;
			m.m22 = -sinY * sinZ * sinX + cosY * cosX;
			break;
		case ZXY:
			m.m00 = cosZ * cosY - sinZ * sinX * sinY;
			m.m01 = -sinZ * cosX;
			m.m02 = cosZ * sinY + sinZ * sinX * cosY;
			m.m10 = sinZ * cosY + cosZ * sinX * sinY;
			m.m11 = cosZ * cosX;
			m.m12 = sinZ * sinY - cosZ * sinX * cosY;
			m.m20 = -cosX * sinY;
			m.m21 = sinX;
			m.m22 = cosX * cosY;
			break;
		case ZYX:
			m.m00 = cosZ * cosY;
			m.m01 = -sinZ * cosX + cosZ * sinY * sinX;
			m.m02 = sinZ * sinX + cosZ * sinY * cosX;
			m.m10 = sinZ * cosY;
			m.m11 = cosZ * cosX + sinZ * sinY * sinX;
			m.m12 = -cosZ * sinX + sinZ * sinY * cosX;
			m.m20 = -sinY;
			m.m21 = cosY * sinX;
			m.m22 = cosY * cosX;
			break;
		}
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
		computeTrig();
		double m00, m01, m02, m10, m11, m12, m20, m21, m22;	// temporarily stores this rotation matrix
		switch(order) {
		case XYZ:
			m00 = cosY * cosZ;
			m01 = -cosY * sinZ;
			m02 = sinY;
			m10 = sinX * sinY * cosZ + cosX * sinZ;
			m11 = -sinX * sinY * sinZ + cosX * cosZ;
			m12 = -sinX * cosY;
			m20 = -cosX * sinY * cosZ + sinX * sinZ;
			m21 = cosX * sinY * sinZ + sinX * cosZ;
			m22 = cosX * cosY;
			break;
		case XZY:
			m00 = cosZ * cosY;
			m01 = -sinZ;
			m02 = cosZ * sinY;
			m10 = cosX * sinZ * cosY + sinX * sinY;
			m11 = cosX * cosZ;
			m12 = cosX * sinZ * sinY - sinX * cosY;
			m20 = sinX * sinZ * cosY - cosX * sinY;
			m21 = sinX * cosZ;
			m22 = sinX * sinZ * sinY + cosX * cosY;
			break;
		case YXZ:
			m00 = cosY * cosZ + sinY * sinX * sinZ;
			m01 = -cosY * sinZ + sinY * sinX * cosZ;
			m02 = sinY * cosX;
			m10 = cosX * sinZ;
			m11 = cosX * cosZ;
			m12 = -sinX;
			m20 = -sinY * cosZ + cosY * sinX * sinZ;
			m21 = sinY * sinZ + cosY * sinX * cosZ;
			m22 = cosY * cosX;
			break;
		case YZX:
			m00 = cosY * cosZ;
			m01 = -cosY * sinZ * cosX + sinY * sinX;
			m02 = cosY * sinZ * sinX + sinY * cosX;
			m10 = sinZ;
			m11 = cosZ * cosX;
			m12 = -cosZ * sinX;
			m20 = -sinY * cosZ;
			m21 = sinY * sinZ * cosX + cosY * sinX;
			m22 = -sinY * sinZ * sinX + cosY * cosX;
			break;
		case ZXY:
			m00 = cosZ * cosY - sinZ * sinX * sinY;
			m01 = -sinZ * cosX;
			m02 = cosZ * sinY + sinZ * sinX * cosY;
			m10 = sinZ * cosY + cosZ * sinX * sinY;
			m11 = cosZ * cosX;
			m12 = sinZ * sinY - cosZ * sinX * cosY;
			m20 = -cosX * sinY;
			m21 = sinX;
			m22 = cosX * cosY;
			break;
		case ZYX:
			m00 = cosZ * cosY;
			m01 = -sinZ * cosX + cosZ * sinY * sinX;
			m02 = sinZ * sinX + cosZ * sinY * cosX;
			m10 = sinZ * cosY;
			m11 = cosZ * cosX + sinZ * sinY * sinX;
			m12 = -cosZ * sinX + sinZ * sinY * cosX;
			m20 = -sinY;
			m21 = cosY * sinX;
			m22 = cosY * cosX;
			break;
		default:
			throw new IllegalStateException();
		}
		
		double t00, t01, t02, t10, t11, t12, t20, t21, t22; // temporarily stores the result of the matrix multiplication
		
		/*
		 * perform matrix multiplication
		 */
		t00 = m.m00 * m00 + m.m01 * m10 + m.m02 * m20;
		t01 = m.m00 * m01 + m.m01 * m11 + m.m02 * m21;
		t02 = m.m00 * m02 + m.m01 * m12 + m.m02 * m22;
		
		t10 = m.m10 * m00 + m.m11 * m10 + m.m12 * m20;
		t11 = m.m10 * m01 + m.m11 * m11 + m.m12 * m21;
		t12 = m.m10 * m02 + m.m11 * m12 + m.m12 * m22;
		
		t20 = m.m20 * m00 + m.m21 * m10 + m.m22 * m20;
		t21 = m.m20 * m01 + m.m21 * m11 + m.m22 * m21;
		t22 = m.m20 * m02 + m.m21 * m12 + m.m22 * m22;
		
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
		computeTrig();
		double m00, m01, m02, m10, m11, m12, m20, m21, m22;	// temporarily stores this rotation matrix
		switch(order) {
		case XYZ:
			m00 = cosY * cosZ;
			m01 = -cosY * sinZ;
			m02 = sinY;
			m10 = sinX * sinY * cosZ + cosX * sinZ;
			m11 = -sinX * sinY * sinZ + cosX * cosZ;
			m12 = -sinX * cosY;
			m20 = -cosX * sinY * cosZ + sinX * sinZ;
			m21 = cosX * sinY * sinZ + sinX * cosZ;
			m22 = cosX * cosY;
			break;
		case XZY:
			m00 = cosZ * cosY;
			m01 = -sinZ;
			m02 = cosZ * sinY;
			m10 = cosX * sinZ * cosY + sinX * sinY;
			m11 = cosX * cosZ;
			m12 = cosX * sinZ * sinY - sinX * cosY;
			m20 = sinX * sinZ * cosY - cosX * sinY;
			m21 = sinX * cosZ;
			m22 = sinX * sinZ * sinY + cosX * cosY;
			break;
		case YXZ:
			m00 = cosY * cosZ + sinY * sinX * sinZ;
			m01 = -cosY * sinZ + sinY * sinX * cosZ;
			m02 = sinY * cosX;
			m10 = cosX * sinZ;
			m11 = cosX * cosZ;
			m12 = -sinX;
			m20 = -sinY * cosZ + cosY * sinX * sinZ;
			m21 = sinY * sinZ + cosY * sinX * cosZ;
			m22 = cosY * cosX;
			break;
		case YZX:
			m00 = cosY * cosZ;
			m01 = -cosY * sinZ * cosX + sinY * sinX;
			m02 = cosY * sinZ * sinX + sinY * cosX;
			m10 = sinZ;
			m11 = cosZ * cosX;
			m12 = -cosZ * sinX;
			m20 = -sinY * cosZ;
			m21 = sinY * sinZ * cosX + cosY * sinX;
			m22 = -sinY * sinZ * sinX + cosY * cosX;
			break;
		case ZXY:
			m00 = cosZ * cosY - sinZ * sinX * sinY;
			m01 = -sinZ * cosX;
			m02 = cosZ * sinY + sinZ * sinX * cosY;
			m10 = sinZ * cosY + cosZ * sinX * sinY;
			m11 = cosZ * cosX;
			m12 = sinZ * sinY - cosZ * sinX * cosY;
			m20 = -cosX * sinY;
			m21 = sinX;
			m22 = cosX * cosY;
			break;
		case ZYX:
			m00 = cosZ * cosY;
			m01 = -sinZ * cosX + cosZ * sinY * sinX;
			m02 = sinZ * sinX + cosZ * sinY * cosX;
			m10 = sinZ * cosY;
			m11 = cosZ * cosX + sinZ * sinY * sinX;
			m12 = -cosZ * sinX + sinZ * sinY * cosX;
			m20 = -sinY;
			m21 = cosY * sinX;
			m22 = cosY * cosX;
			break;
		default:
			throw new IllegalStateException();
		}
		
		double t00, t01, t02, t10, t11, t12, t20, t21, t22, t30, t31, t32; // temporarily stores the result of the matrix multiplication
		
		/*
		 * perform matrix multiplication
		 */
		t00 = m.m00 * m00 + m.m01 * m10 + m.m02 * m20;
		t01 = m.m00 * m01 + m.m01 * m11 + m.m02 * m21;
		t02 = m.m00 * m02 + m.m01 * m12 + m.m02 * m22;
		
		t10 = m.m10 * m00 + m.m11 * m10 + m.m12 * m20;
		t11 = m.m10 * m01 + m.m11 * m11 + m.m12 * m21;
		t12 = m.m10 * m02 + m.m11 * m12 + m.m12 * m22;
		
		t20 = m.m20 * m00 + m.m21 * m10 + m.m22 * m20;
		t21 = m.m20 * m01 + m.m21 * m11 + m.m22 * m21;
		t22 = m.m20 * m02 + m.m21 * m12 + m.m22 * m22;
		
		t30 = m.m30 * m00 + m.m31 * m10 + m.m32 * m20;
		t31 = m.m30 * m01 + m.m31 * m11 + m.m32 * m21;
		t32 = m.m30 * m02 + m.m31 * m12 + m.m32 * m22;
		
		m.m00 = t00; m.m01 = t01; m.m02 = t02;
		m.m10 = t10; m.m11 = t11; m.m12 = t12;
		m.m20 = t20; m.m21 = t21; m.m22 = t22;
		m.m30 = t30; m.m31 = t31; m.m32 = t32;
		
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
	 * If necessary, recompute trig function values and cache
	 * the results (trig functions are slow, especially in Java)
	 */
	private final void computeTrig() {
		if (oldX != x) {
			sinX = Utils3d.degSin(x);
			cosX = Utils3d.degCos(x);
			oldX = x;
		}
		if (oldY != y) {
			sinY = Utils3d.degSin(x);
			cosY = Utils3d.degCos(x);
			oldY = x;
		}
		if (oldZ != z) {
			sinZ = Utils3d.degSin(x);
			cosZ = Utils3d.degCos(x);
			oldZ = z;
		}
	}
}

