/*
 * $Id$
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
package test;

import javax.vecmath.*;

/**
 * @author sascha
 *
 */
public class Quaternion {

	private float x, y, z, w;
	
	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		normalize();
	}
	
	public Quaternion(Quaternion q) {
		x = q.x;
		y = q.y;
		z = q.z;
		w = q.w;
	}
	
	public Quaternion(Quat4f q) {
		x = q.x;
		y = q.y;
		z = q.z;
		w = q.w;
	}
	
	public void normalize() {
		float n = (float) Math.sqrt(x * x + y * y + z * z);
		x /= n;
		y /= n;
		z /= n;
	}
	
	public void normalize(Quaternion q) {
		float n = (float) Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
		x = q.x / n;
		y = q.y / n;
		z = q.z / n;
	}
	
	public void mul(Quaternion q) {
		float xn = x * q.z - q.y * z + w * q.x + q.w * x;
		float yn = z * q.x - q.z * x + w * q.y + q.w * y;
		float zn = x * q.y - q.x * y + w * q.z + q.w * z;
		float wn = w * q.w - x * q.x - y * q.y - z * q.z;
		x = xn;
		y = yn;
		z = zn;
		w = wn;
	}
	
	public void mul(Tuple3f v) {
		float xn = x * v.z - v.y * z + w * v.x;
		float yn = z * v.x - v.z * x + w * v.y;
		float zn = x * v.y - v.x * y + w * v.z;
		float wn = -x * v.x - y * v.y - z * v.z;
		x = xn;
		y = yn;
		z = zn;
		w = wn;
	}
	
	public void mulInv(Quaternion q) {
		float xn = -x * q.z + q.y * z - w * q.x + q.w * x;
		float yn = -z * q.x + q.z * x - w * q.y + q.w * y;
		float zn = -x * q.y + q.x * y - w * q.z + q.w * z;
		float wn = w * q.w + x * q.x + y * q.y + z * q.z;
		x = xn;
		y = yn;
		z = zn;
		w = wn;
	}
	
	public void transform(Tuple3f v) {
		float xn = x * v.z - v.y * z + w * v.x;
		float yn = z * v.x - v.z * x + w * v.y;
		float zn = x * v.y - v.x * y + w * v.z;
		float wn = -x * v.x - y * v.y - z * v.z;
		float xnn = -xn * z + y * zn - wn * x + w * xn;
		float ynn = -zn * x + z * xn - wn * y + w * yn;
		float znn = -xn * y + x * yn - wn * z + w * zn;
		float wnn = wn * w + xn * x + yn * y + z * z;
		System.out.println(xnn + " " + ynn + " " + znn + " " + wnn);
	}

}
