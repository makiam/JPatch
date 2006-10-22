package test;

import java.util.*;

public class ArrayTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		int[][] a;
//		int test = 0;
//		
//		for (int run = 0; run < 5; run++) {
//			a = new int[10000][100];
//			long t = System.currentTimeMillis();
//			for (int c = 0; c < 100; c++) {
//				for (int i = 0; i < 10000; i++) {
//					for (int j = 0; j < 100; j++) {
//						test += a[i][j];
//					}
//				}
//			}
//			System.out.println("1: " + (System.currentTimeMillis() - t));
//			
//			a = new int[10000][];
//			List<Integer> l = new ArrayList<Integer>();
//			for (int i = 0; i < 10000; i++) {
//				l.add(i);
//			}
//			Random rnd = new Random();
//			Collections.shuffle(l);
//			for (int i = 0; i < 10000; i++) {
//				a[l.get(i)] = new int[100];
//			}
//			
//			t = System.currentTimeMillis();
//			for (int c = 0; c < 100; c++) {
//				for (int i = 0; i < 10000; i++) {
//					for (int j = 0; j < 100; j++) {
//						test += a[i][j];
//					}
//				}
//			}
//			System.out.println("2: " + (System.currentTimeMillis() - t));
//			
//			a = new int[1000][1000];
//			t = System.currentTimeMillis();
//			for (int c = 0; c < 100; c++) {
//				for (int i = 0; i < 1000; i++) {
//					for (int j = 0; j < 1000; j++) {
//						test += a[i][999 - j];
//					}
//				}
//			}
//			System.out.println("3: " + (System.currentTimeMillis() - t));
//			
//			a = new int[1000][1000];
//			t = System.currentTimeMillis();
//			for (int c = 0; c < 100; c++) {
//				for (int i = 0; i < 1000; i++) {
//					for (int j = 0; j < 1000; j++) {
//						test += a[999 - i][j];
//					}
//				}
//			}
//			System.out.println("4: " + (System.currentTimeMillis() - t));
//		}
		for (int row = 0; row < 7; row++) {
			for (int column = 0; column < 7; column++) {
				System.out.print("\t" + (column - row));
			}
			System.out.println();
		}
		System.out.println();
		for (int row = 0; row < 7; row++) {
			for (int column = 0; column < 7; column++) {
				System.out.print("\t" + (5 - column - row));
			}
			System.out.println();
		}
		System.out.println();
		for (int row = 0; row < 11; row++) {
			for (int column = 0; column < 11; column++) {
				int a = column - row;
				int b = 9 - column - row;
				int q = (a < 0) ? (b < 0) ? 2 : 3 : (b < 0) ? 1 : 0;
//				if (a < 0) {
//					if (b < 0) q = 2;
//					else q = 3;
//				} else {
//					if (b < 0) q = 1;
//					else q = 0;
//				}
				System.out.print("\t" + q);
			}
			System.out.println();
		}
	}
}
