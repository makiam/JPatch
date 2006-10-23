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

/**
 * @author sascha
 *
 */
public class LimitTest {

	/*
	 * 1-2-3
	 * 8 0 4
	 * 7-6-5
	 * 
	 * 
	 * 
	 */
	/**
	 * @param args
	 */
	final int k = 4;
	double[][] w = new double [3][3];
	
	public static void main(String[] args) {
		new LimitTest();
	}
	
	private LimitTest() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				w[i][j] = i == j ? 1 : 0;
			}
		}
		
		for (int a = 0; a < 30; a++) {
			for (int i = 0; i < 3; i++) {
				double sum = 0;
				for (int j = 0; j < 3; j++) {
					sum += w[i][j];
					System.out.print(w[i][j] + "\t");
				}
				System.out.println(sum);
			}
			System.out.println();
			sd();
		}
	}
	private void sd() {
		double[][] out = new double[3][3];
		out[0][0] = w[0][0] * (k - 1.75) / k + w[1][0] * (k - 1.75) / k + w[2][0] * (k - 1.75) / k;
		out[0][1] = w[0][1] * 0.25 / k + w[1][1] * 0.25 / k + w[2][1] * 0.25 / k;
		out[0][2] = w[0][2] * 1.5 / k + w[1][2] * 1.5 / k + w[2][2] * 1.5 / k;
		out[1][0] = w[0][0] / 4 + w[1][0] / 4 + w[2][0] / 4;
		out[1][1] = w[0][1] / 4 + w[1][1] / 4 + w[2][1] / 4;
		out[1][2] = w[0][2] / 2 + w[1][2] / 2 + w[2][2] / 2;
		out[2][0] = w[0][0] * 3 / 8 + w[1][0] * 3 / 8 + w[2][0] * 3 / 8;
		out[2][1] = w[0][1] * 2 / 8 + w[1][1] * 2 / 8 + w[2][1] * 2 / 8;
		out[2][2] = w[0][2] * 3 / 8 + w[1][2] * 3 / 8 + w[2][2] * 3 / 8;
		
//		out[0][0] = w[0][0] * (k - 1.75) / k + w[0][1] * (k - 1.75) / k + w[0][2] * (k - 1.75) / k;
//		out[0][1] = w[1][0] * 0.25 / k + w[1][1] * 0.25 / k + w[1][2] * 0.25 / k;
//		out[0][2] = w[2][0] * 1.5 / k + w[2][1] * 1.5 / k + w[2][2] * 1.5 / k;
//		out[1][0] = w[0][1] / 4 + w[0][1] / 4 + w[0][2] / 4;
//		out[1][1] = w[1][1] / 4 + w[1][1] / 4 + w[1][2] / 4;
//		out[1][2] = w[2][1] / 2 + w[2][1] / 2 + w[2][2] / 2;
//		out[2][0] = w[0][2] * 3 / 8 + w[0][1] * 3 / 8 + w[0][2] * 3 / 8;
//		out[2][1] = w[1][2] * 2 / 8 + w[1][1] * 2 / 8 + w[1][2] * 2 / 8;
//		out[2][2] = w[2][2] * 3 / 8 + w[2][1] * 3 / 8 + w[2][2] * 3 / 8;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				w[i][j] = out[i][j];
			}
		}
	}
}
