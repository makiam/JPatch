package com.jpatch.test;

import com.jpatch.afw.vecmath.Utils3d;

public class Bench {
	private static final int ROUNDS = 1000;
	private static final int COUNT = 10000;
	
	public static void main(String[] args) {
		double deg = -360 * 1e9;
		System.out.println(deg * Math.PI / 180);
		System.out.println(Math.toRadians(deg));
		System.out.println(Math.sin(Math.toRadians(deg)));
		System.out.println(Utils3d.degSin(deg) + " " + Utils3d.degCos(deg));
		for (int i = -720; i <= 720; i += 30) {
			System.out.println(Utils3d.degSin(i) + " " + Utils3d.degCos(i));
		}
		System.exit(0);
		System.out.println("dry run");
		/* dry run */
		test();
		System.out.println("benchmark start");
		long t0 = System.currentTimeMillis();
		/* benchmark run */
		test();
		long t1 = System.currentTimeMillis();
		double d = (t1 - t0) * 0.001 / COUNT / ROUNDS;
		System.out.println(d);
	}
	
	private static void test() {
		sin();
	}
	
	private static void dry( ) {
		for (int i = 0; i < ROUNDS; i++) {
			for (int j = 0; j < COUNT; j++) {
			}
		}
	}
	
	private static void multiply( ) {
		for (int i = 0; i < ROUNDS; i++) {
			double a = (double) Math.random();
			double b = (double) Math.random();
			double c = 0;
			for (int j = 0; j < COUNT; j++) {
				c = a * b;
			}
		}
	}
	
	private static void sin( ) {
		for (int i = 0; i < ROUNDS; i++) {
			double a = Math.random() * 10000000;
			double c = 0;
			for (int j = 0; j < COUNT; j++) {
				c = Math.sin(a);
			}
		}
	}
}
