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

import jpatch.auxilary.Utils3D;
/**
 * @author sascha
 *
 */
public class QuaternionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Vector3f vx = new Vector3f(1,2,3);
		Vector3f vy = new Vector3f(-1,1,0);
		Vector3f vz = new Vector3f(0,2,1);
		AxisAngle4f a = new AxisAngle4f(1, 2, 3, (float) Math.PI / 3);
		
		Matrix3f m = new Matrix3f();
		m.set(a);
		Quat4f q = new Quat4f();
		q.set(a);
		
		transform(m, vx);
		transform(m, vy);
		transform(m, vz);
		transform(q, vx);
		transform(q, vy);
		transform(q, vz);
		Utils3D.transform(vx, q); System.out.println("utils : " + vx);
		Utils3D.transform(vy, q); System.out.println("utils : " + vy);
		Utils3D.transform(vz, q); System.out.println("utils : " + vz);
	}
	
	static void transform(Matrix3f m, Vector3f v) {
		Vector3f n = new Vector3f(v);
		m.transform(n);
		System.out.println("matrix : " + n);
	}
	
	static void transform(Quat4f q, Vector3f v) {
		Vector3f n = new Vector3f(v);
		Quat4f q0 = new Quat4f(q);
		Quat4f q1 = new Quat4f(v.x, v.y, v.z, 0);
		q0.mul(q1);
//		System.out.println("\t" + q0);
		q0.mulInverse(q);
		System.out.println("quaternion : " + q0);
	}
}
