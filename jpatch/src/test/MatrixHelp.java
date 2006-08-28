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
public class MatrixHelp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Sum[][] X = new Sum[][] {
				{ new Sum(1), new Sum(), new Sum() },
				{ new Sum(), new Sum("cx"), new Sum(-1, "sx") },
				{ new Sum(), new Sum("sx"), new Sum("cx") }
		};
		
		Sum[][] Y = new Sum[][] {
				{ new Sum("cy"), new Sum(), new Sum("sy") },
				{ new Sum(), new Sum(1), new Sum() },
				{ new Sum(-1, "sy"), new Sum(), new Sum("cy") }
		};
		
		Sum[][] Z = new Sum[][] {
				{ new Sum("cz"), new Sum(-1, "sz"), new Sum() },
				{ new Sum("sz"), new Sum("cz"), new Sum() },
				{ new Sum(), new Sum(), new Sum(1) }
		};


		
		Sum[][] XY = new Sum[3][3];
		Sum[][] XZ= new Sum[3][3];
		Sum[][] YX = new Sum[3][3];
		Sum[][] YZ = new Sum[3][3];
		Sum[][] ZX = new Sum[3][3];
		Sum[][] ZY = new Sum[3][3];
		
		multiply(X, Y, XY);
		multiply(X, Z, XZ);
		multiply(Y, X, YX);
		multiply(Y, Z, YZ);
		multiply(Z, X, ZX);
		multiply(Z, Y, ZY);
		
		Sum[][] XYZ = new Sum[3][3];
		Sum[][] XZY = new Sum[3][3];
		Sum[][] YXZ = new Sum[3][3];
		Sum[][] YZX = new Sum[3][3];
		Sum[][] ZXY = new Sum[3][3];
		Sum[][] ZYX = new Sum[3][3];
		
		multiply(XY, Z, XYZ);
		multiply(XZ, Y, XZY);
		multiply(YX, Z, YXZ);
		multiply(YZ, X, YZX);
		multiply(ZX, Y, ZXY);
		multiply(ZY, X, ZYX);
		
		boolean f1 = false;
		boolean f2 = false;
		
		if (f1)
			System.out.println("float sx, cx, sy, cy, sz, cz;");
		else
			System.out.println("double sx, cx, sy, cy, sz, cz;");
		System.out.println("switch(order) {");
		print("X", f1, f2, X);
		print("Y", f1, f2, Y);
		print("Z", f1, f2, Z);
		print("XY", f1, f2, XY);
		print("XZ", f1, f2, XZ);
		print("YX", f1, f2, YX);
		print("YZ", f1, f2, YZ);
		print("ZX", f1, f2, ZX);
		print("ZY", f1, f2, ZY);
		print("XYZ", f1, f2, XYZ);
		print("XZY", f1, f2, XZY);
		print("YXZ", f1, f2, YXZ);
		print("YZX", f1, f2, YZX);
		print("ZXY", f1, f2, ZXY);
		print("ZYX", f1, f2, ZYX);
		System.out.println("}");
	}
	
	static void multiply(Sum[][] A, Sum[][] B, Sum[][] C) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				Sum s0 = new Sum(A[i][0]);
				s0.multiply(B[0][j]);
				Sum s1 = new Sum(A[i][1]);
				s1.multiply(B[1][j]);
				Sum s2 = new Sum(A[i][2]);
				s2.multiply(B[2][j]);
				C[i][j] = new Sum(s0);
				C[i][j].add(s1);
				C[i][j].add(s2);
			}
		}
	}

	static void print(String c, boolean f1, boolean f2, Sum[][] a) {
		boolean x = c.contains("X");
		boolean y = c.contains("Y");
		boolean z = c.contains("Z");
		System.out.println("case " + c + ":");
		if (x) {
			if (f1) {
				System.out.println("\tsx = (float) Math.sin(x);");
				System.out.println("\tcx = (float) Math.cos(x);");
			} else {
				System.out.println("\tsx = Math.sin(x);");
				System.out.println("\tcx = Math.cos(x);");
			}
		}
		if (y) {
			if (f1) {
				System.out.println("\tsy = (float) Math.sin(y);");
				System.out.println("\tcy = (float) Math.cos(y);");
			} else {
				System.out.println("\tsy = Math.sin(y);");
				System.out.println("\tcy = Math.cos(y);");
			}
		}
		if (z) {
			if (f1) {
				System.out.println("\tsz = (float) Math.sin(z);");
				System.out.println("\tcz = (float) Math.cos(z);");
			} else {
				System.out.println("\tsz = Math.sin(z);");
				System.out.println("\tcz = Math.cos(z);");
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (f2) {
					System.out.println("\tm.m" + i + j + " = (float) (" + a[j][i] + ");");
				} else {
					System.out.println("\tm.m" + i + j + " = " + a[j][i] + ";");
				}
			}
		}
		System.out.println("\tbreak;");
	}
	
//	static String toString(int a, String A) {
//		if (A.equals("") || A)
//			return Integer.toString(a);
//		if (a == -1)
//			return "-" + A;
//		if (a == 1)
//			return A;
//		return "";
//	}
	
	static class Product {
		float f = 1;
		String[] terms = new String[0];
		
		public Product() { }
		
		public Product(float f) {
			this.f = f;
		}
		
		public Product(float f, String term) {
			this.f = f;
			terms = new String[] { term };
		}
		
		public Product(Product product) {
			this(product.f, product.terms);
		}
		
		public Product(String term) {
			this.terms = new String[] { term };
		}
		
		public Product(float f, String[] terms) {
			this.f = f;
			this.terms = new String[terms.length];
			System.arraycopy(terms, 0, this.terms, 0, terms.length);
		}
		
		void multiply(Product product) {
			f *= product.f;
			if (f == 0) {
				terms = new String[0];
				return;
			}
			String[] n = new String[terms.length + product.terms.length];
			System.arraycopy(terms, 0, n, 0, terms.length);
			System.arraycopy(product.terms, 0, n, terms.length, product.terms.length);
			terms = n;
		}
		
		public String toString() {
			return toString(false);
		}
		
		public String toString(boolean absolute) {
			float ff = absolute ? Math.abs(f) : f;
			if (terms.length == 0) {
				if (ff == 0)
					return "0";
				if (ff == 1)
					return "1";
				return Float.toString(f);
			}
			StringBuilder sb = new StringBuilder();
			if (ff == -1)
				sb.append("-");
			else if (ff != 1)
				sb.append(Float.toString(ff)).append(" * ");
			for (int i = 0; i < terms.length; i++) {
				sb.append(terms[i]);
				if (i < terms.length - 1)
					sb.append(" * ");
			}
			return sb.toString();
		}
	}
	
	static class Sum {
		Product[] summands = new Product[0];
		
		public Sum() { }
		
		public Sum(float f) {
			this.summands = new Product[] { new Product(f) };
		}
		
		public Sum(String term) {
			this.summands = new Product[] { new Product(term) };
		}
		
		public Sum(float f, String term) {
			this.summands = new Product[] { new Product(f, term) };
		}
		
		public Sum(Product product) {
			this.summands = new Product[] { product };
		}
		
		public Sum(Product[] summands) {
			this.summands = summands;
		}
		
		public Sum(Sum sum) {
			summands = new Product[sum.summands.length];
			for (int i = 0; i < summands.length; i++) {
				summands[i] = new Product(sum.summands[i]);
			}
		}
		
		public String toString() {
			if (summands.length == 0)
				return "0";
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < summands.length; i++) {
				sb.append(summands[i].toString(i != 0));
				if (i < summands.length - 1) {
					if (summands[i + 1].f > 0)
						sb.append(" + ");
					else
						sb.append(" - ");
				}
			}
			return sb.toString();
		}
		
		void add(Sum sum) {
			Product[] n = new Product[summands.length + sum.summands.length];
			System.arraycopy(summands, 0, n, 0, summands.length);
			System.arraycopy(sum.summands, 0, n, summands.length, sum.summands.length);
			summands = n;
		}
		
		void multiply(Sum sum) {
			Product[] n = new Product[summands.length * sum.summands.length];
			int i = 0;
			for (Product a : summands) {
				for (Product b : sum.summands) {
					n[i] = new Product(a);
					n[i].multiply(b);
					i++;
				}
			}
			summands = n;
		}
	}
}
