package test;

import com.jpatch.afw.vecmath.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class PolyharmonicSpline {

	double[] c = new double[] { 0, 0.4, 1.4, 2 };
	double[] w = new double[4];
	double[] y = new double[] { 1, -5, 3, 0 };
	double vv, v;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PolyharmonicSpline();
	}
	
	PolyharmonicSpline() {
		final double startX = -1;
		final double endX = 3;
		final double startY = -10;
		final double endY = 10;
		
		double[] matrix = new double[] {
				d(0, 0), d(0, 1), d(0, 2), d(0, 3), 1, c[0],
				d(1, 0), d(1, 1), d(1, 2), d(1, 3), 1, c[1], 
				d(2, 0), d(2, 1), d(2, 2), d(2, 3), 1, c[2], 
				d(3, 0), d(3, 1), d(3, 2), d(3, 3), 1, c[3], 
				1,       1,       1,       1,       0, 0, 
				c[0],    c[1],    c[2],    c[3],    0, 0
		};
		double[] b = new double[] { y[0], y[1], y[2], y[3], 0, 0 };
		Utils3d.solve(matrix, b);
		System.out.println(Arrays.toString(b));
		w[0] = b[0];
		w[1] = b[1];
		w[2] = b[2];
		w[3] = b[3];
		vv = b[4];
		v = b[5];
		
		JPanel panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				for (int i = 0; i < 400; i++) {
					double x = startX + (i / 400.0) * (endX - startX);
					double y = y(x);
//					System.out.println(x + ": " + y);
					int s = (int) ((y - startY) / (endY - startY) * 400);
					g.drawRect(i, s, 0, 0);
				}
				for (int i = 0; i < c.length; i++) {
					int x = (int) ((c[i] - startX) / (endX - startX) * 400);
					int s = (int) ((y[i] - startY) / (endY - startY) * 400);
					g.drawRect(x - 2, s - 2, 5, 5);
				}
			}
		};
		JFrame frame = new JFrame("test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.setPreferredSize(new Dimension(400, 400));
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	private double d(int i, int j) {
//		return Math.abs(c[i] - c[j]);
		return phi(c[i], c[j]);
	}
	
	private double y(double x) {
		double y = 0;
		for (int i = 0; i < c.length; i++) {
			y += w[i] * phi(x, c[i]);
		}
		return y + vv + v * x;
	}
	
	private double phi(double i, double j) {
//		return Math.abs(i - j);
		double r = Math.abs(i - j);
		return r * r * r;
//		int k = 2;
//		if (r < 1) {
//			return Math.pow(r, k - 1) * Math.log(Math.pow(r, r));
//		} else {
//			return Math.pow(r, k) * Math.log(r);
//		}
	}

}
