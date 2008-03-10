package trashcan.sds;

public class MathTest {
	Vertex[] top;
	
	public static void main(String[] args) {
		new MathTest();
//		int count = 0;
//		for (int i = 3; i <= 16; i++) {
//			int num = i * (i - 1) / 2;
//			count += num;
//			System.out.println(i + ":\t" + num);
//		}
//		System.out.println(count);
//		int n = 0;
//		for (int i = 0; i < 16; i++) {
//			for (int j = i + 1; j < 16; j++) {
//				int nn = (16 - i) * i / 2;
//				System.out.println(n++ + " " + nn + ":\t" + i + "\t" + j);
//			}
//		}
	}

	MathTest() {
		int valence = 8;
		top = new Vertex[valence * 2 + 1];
		Vertex[] current = new Vertex[top.length];
		Vertex[] next = new Vertex[top.length];
		for (int i = 0; i < top.length; i++) {
			top[i] = new Vertex(i);
			current[i] = new Vertex(new Vertex[] { top[i] }, new double[] { 1.0 });
		}
		
		for (int level = 0; level < 24; level++) {
//			Vertex[] vv = new Vertex[top.length];
//			double[] ww = new double[top.length];
//			vv[0] = current[0];
//			ww[0] = 9.0/16.0;
//			for (int i = 1; i < top.length; i++) {
//				vv[i] = current[i];
//				if (i % 2 == 1) {
//					ww[i] = 1.0/64.0;
//				} else {
//					ww[i] = 3.0/32.0;
//				}
//			}
//			next[0] = new Vertex(vv, ww);
			next[0] = new Vertex(
					new Vertex[] { current[1], current[0], current[9] },
					new double[] { 1.0/8.0, 3.0/4.0, 1.0/8.0 }
			);
			for (int i = 1; i < top.length; i++) {
				int l = top.length - 1;
				int n = (i % l) + 1;
				int nn = ((i + 1) % l) + 1;
				int p = ((i + l - 2) % l) + 1;
				int pp = ((i + l - 3) % l) + 1;
				if (i % 2 == 0) {
					// even - face point
					next[i] = new Vertex(
							new Vertex[] { current[i], current[n], current[0], current[p] },
							new double[] { 0.25, 0.25, 0.25, 0.25 }
					);
				} else {
					// odd - edge point
					next[i] = new Vertex(
							new Vertex[] { current[pp], current[p], current[0], current[i], current[nn], current[n] },
							new double[] { 1.0/16.0, 1.0/16.0, 3.0/8.0, 3.0/8.0, 1.0/16.0, 1.0/16.0 }
					);
				}
				if (i == 1 || i == 9) {
					next[i] = new Vertex(
							new Vertex[] { current[i], current[0] },
							new double[] { 0.5, 0.5 }
					);
				}
			}
			for (int i = 0; i < top.length; i++) {
				current[i] = next[i];
			}
			double f = Math.pow(1.636363636363636363636363636363, level);
			Vertex tangent1 = new Vertex(new Vertex[] { current[1], current[9] }, new double[] { f, -f });
//			Vertex tangent2 = new Vertex(new Vertex[] { current[4], current[8] }, new double[] { f, -f });
			Vertex tangent2 = new Vertex(new Vertex[] { current[5], current[0] }, new double[] { f, -f });
			System.out.println("\nlevel " + level + ":");
			System.out.println("limit=" + current[0]);
			System.out.println("tangent1=" + tangent1);
			System.out.println("tangent2=" + tangent2);
		}
		
	}
	
	class Vertex {
		double[] w = new double[top.length];
		Vertex(int number) {
			w[number] = 1.0;
		}
		
		Vertex(Vertex[] vertices, double[] weights) {
			if (vertices.length != weights.length) {
				throw new IllegalArgumentException();
			}
			for (int i = 0; i < vertices.length; i++) {
				for (int j = 0; j < top.length; j++) {
					w[j] += vertices[i].w[j] * weights[i];
				}
			}
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			boolean one = false;
			for (int i = 0; i < top.length; i++) {
				String f = fraction(w[i]);
				if (!f.equals("0")) {
					if (one) {
						if (w[i] > 0)
							sb.append(" + ");
						else
							sb.append(" - ");
					}
					if (w[i] == 1.0) {
						sb.append("v" + i);
					} else {
						sb.append(fraction(w[i])).append(" ").append("v" + i);
					}
					one = true;
				}
			}
			return sb.toString();
		}
		
		String fraction(double d) {
			double epsilon = 0.000001;
			for (int n = 0; n <= 100; n++) {
				for (int over = 1; over <= 100; over++) {
					double test = (double) n / (double) over;
					if ((test > (d - epsilon)) && (test < (d + epsilon))) {
						return n == 0 ? "0" : n + "/" + over;
					}
				}
			}
			for (int n = -1; n >= -100; n--) {
				for (int over = 1; over <= 100; over++) {
					double test = (double) n / (double) over;
					if ((test > (d - epsilon)) && (test < (d + epsilon))) {
						return n == 0 ? "0" : -n + "/" + over;
					}
				}
			}
			return Double.toString(Math.abs(d));
		}
	}
}
