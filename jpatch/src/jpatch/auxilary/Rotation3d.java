/*
 * $Id:$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.auxilary;

import javax.vecmath.*;

/**
 * @author sascha
 *
 */
@SuppressWarnings("serial")
public class Rotation3d extends Tuple3d {
	
	public static enum Order { //X, Y, Z, XY, XZ, YX, YZ, ZX, ZY, 
		XYZ, XZY, YXZ, YZX, ZXY, ZYX };
	public Order order = Order.XYZ;
	
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
	
	public void setMatrixRotation(Matrix3d m) {
		double sx = Math.sin(Math.toRadians(x));
		double cx = Math.cos(Math.toRadians(x));
		double sy = Math.sin(Math.toRadians(y));
		double cy = Math.cos(Math.toRadians(y));
		double sz = Math.sin(Math.toRadians(z));
		double cz = Math.cos(Math.toRadians(z));
		switch(order) {
		case XYZ:
			m.m00 = cy * cz;
			m.m01 = -cy * sz;
			m.m02 = sy;
			m.m10 = sx * sy * cz + cx * sz;
			m.m11 = -sx * sy * sz + cx * cz;
			m.m12 = -sx * cy;
			m.m20 = -cx * sy * cz + sx * sz;
			m.m21 = cx * sy * sz + sx * cz;
			m.m22 = cx * cy;
			break;
		case XZY:
			m.m00 = cz * cy;
			m.m01 = -sz;
			m.m02 = cz * sy;
			m.m10 = cx * sz * cy + sx * sy;
			m.m11 = cx * cz;
			m.m12 = cx * sz * sy - sx * cy;
			m.m20 = sx * sz * cy - cx * sy;
			m.m21 = sx * cz;
			m.m22 = sx * sz * sy + cx * cy;
			break;
		case YXZ:
			m.m00 = cy * cz + sy * sx * sz;
			m.m01 = -cy * sz + sy * sx * cz;
			m.m02 = sy * cx;
			m.m10 = cx * sz;
			m.m11 = cx * cz;
			m.m12 = -sx;
			m.m20 = -sy * cz + cy * sx * sz;
			m.m21 = sy * sz + cy * sx * cz;
			m.m22 = cy * cx;
			break;
		case YZX:
			m.m00 = cy * cz;
			m.m01 = -cy * sz * cx + sy * sx;
			m.m02 = cy * sz * sx + sy * cx;
			m.m10 = sz;
			m.m11 = cz * cx;
			m.m12 = -cz * sx;
			m.m20 = -sy * cz;
			m.m21 = sy * sz * cx + cy * sx;
			m.m22 = -sy * sz * sx + cy * cx;
			break;
		case ZXY:
			m.m00 = cz * cy - sz * sx * sy;
			m.m01 = -sz * cx;
			m.m02 = cz * sy + sz * sx * cy;
			m.m10 = sz * cy + cz * sx * sy;
			m.m11 = cz * cx;
			m.m12 = sz * sy - cz * sx * cy;
			m.m20 = -cx * sy;
			m.m21 = sx;
			m.m22 = cx * cy;
			break;
		case ZYX:
			m.m00 = cz * cy;
			m.m01 = -sz * cx + cz * sy * sx;
			m.m02 = sz * sx + cz * sy * cx;
			m.m10 = sz * cy;
			m.m11 = cz * cx + sz * sy * sx;
			m.m12 = -cz * sx + sz * sy * cx;
			m.m20 = -sy;
			m.m21 = cy * sx;
			m.m22 = cy * cx;
			break;
		}
	}
	
	public void setMatrixRotation(Matrix4d m) {
		double sx = Math.sin(Math.toRadians(x));
		double cx = Math.cos(Math.toRadians(x));
		double sy = Math.sin(Math.toRadians(y));
		double cy = Math.cos(Math.toRadians(y));
		double sz = Math.sin(Math.toRadians(z));
		double cz = Math.cos(Math.toRadians(z));
		switch(order) {
		case XYZ:
			m.m00 = cy * cz;
			m.m01 = -cy * sz;
			m.m02 = sy;
			m.m10 = sx * sy * cz + cx * sz;
			m.m11 = -sx * sy * sz + cx * cz;
			m.m12 = -sx * cy;
			m.m20 = -cx * sy * cz + sx * sz;
			m.m21 = cx * sy * sz + sx * cz;
			m.m22 = cx * cy;
			break;
		case XZY:
			m.m00 = cz * cy;
			m.m01 = -sz;
			m.m02 = cz * sy;
			m.m10 = cx * sz * cy + sx * sy;
			m.m11 = cx * cz;
			m.m12 = cx * sz * sy - sx * cy;
			m.m20 = sx * sz * cy - cx * sy;
			m.m21 = sx * cz;
			m.m22 = sx * sz * sy + cx * cy;
			break;
		case YXZ:
			m.m00 = cy * cz + sy * sx * sz;
			m.m01 = -cy * sz + sy * sx * cz;
			m.m02 = sy * cx;
			m.m10 = cx * sz;
			m.m11 = cx * cz;
			m.m12 = -sx;
			m.m20 = -sy * cz + cy * sx * sz;
			m.m21 = sy * sz + cy * sx * cz;
			m.m22 = cy * cx;
			break;
		case YZX:
			m.m00 = cy * cz;
			m.m01 = -cy * sz * cx + sy * sx;
			m.m02 = cy * sz * sx + sy * cx;
			m.m10 = sz;
			m.m11 = cz * cx;
			m.m12 = -cz * sx;
			m.m20 = -sy * cz;
			m.m21 = sy * sz * cx + cy * sx;
			m.m22 = -sy * sz * sx + cy * cx;
			break;
		case ZXY:
			m.m00 = cz * cy - sz * sx * sy;
			m.m01 = -sz * cx;
			m.m02 = cz * sy + sz * sx * cy;
			m.m10 = sz * cy + cz * sx * sy;
			m.m11 = cz * cx;
			m.m12 = sz * sy - cz * sx * cy;
			m.m20 = -cx * sy;
			m.m21 = sx;
			m.m22 = cx * cy;
			break;
		case ZYX:
			m.m00 = cz * cy;
			m.m01 = -sz * cx + cz * sy * sx;
			m.m02 = sz * sx + cz * sy * cx;
			m.m10 = sz * cy;
			m.m11 = cz * cx + sz * sy * sx;
			m.m12 = -cz * sx + sz * sy * cx;
			m.m20 = -sy;
			m.m21 = cy * sx;
			m.m22 = cy * cx;
			break;
		}
	}
	
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
}
