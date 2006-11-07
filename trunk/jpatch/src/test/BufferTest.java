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
package test;

import java.nio.*;

/**
 * @author sascha
 *
 */
public class BufferTest {
	public static void main(String[] args) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 10000);
		FloatBuffer fb = buffer.asFloatBuffer();
		float[] fa = new float[fb.capacity()];
		float[] fa2 = new float[fb.capacity()];
//		for (int i = 0; i < 3; i++) {
//			arrayCopy(fa, fa2);
//			copy(fa, fb);
//		}
		long t;
		
		copy(fa, fb);
		put(fb);
		
		t = System.currentTimeMillis();
		copy(fa, fb);
		System.out.println(System.currentTimeMillis() - t);
		
		t = System.currentTimeMillis();
		put(fb);
		System.out.println(System.currentTimeMillis() - t);
		
//		t = System.currentTimeMillis();
//		arrayCopy(fa, fa2);
//		System.out.println(System.currentTimeMillis() - t);
		
	}
	
	private static void put(FloatBuffer fb) {
		for (int i = 0; i < 1000; i++) {
			fb.rewind();
			for (int j = 0; j < 10000; j++) {
				fb.put(j);
			}
		}
	}
	
//	private static void arrayCopy(float[] fa, float[] fb) {
//		for (int i = 0; i < 1for (int i = 0; i < 3; i++) {
//	arrayCopy(fa, fa2);
//	copy(fa, fb);
//}000; i++) {
//			System.arraycopy(fa, 0, fb, 0, fa.length);
//		}
//	}
	
	private static void copy(float[] fa, FloatBuffer fb) {
		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 10000; j++) {
				fa[j] = j;
			}
			fb.rewind();
			fb.put(fa);
		}
	}
}
