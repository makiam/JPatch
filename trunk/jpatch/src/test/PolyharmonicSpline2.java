package test;

import com.jpatch.afw.vecmath.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

public class PolyharmonicSpline2 {

	double[][] c = new double[][] {
			{0, 0},
			{0, 1},
			{1, 0},
			{1, 1},
	};
	
	double[] w = new double[c.length];
	double[] y = new double[] { 1, 0, 0, 1 };
	double[] v = new double[c[0].length + 1];
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PolyharmonicSpline2();
	}
	
	PolyharmonicSpline2() {
		int num = 100;
		c = new double[num][2];
		w = new double[num];
		y = new double[num];
		v = new double[3];
		for (int i = 0; i < num; i++) {
			c[i][0] = Math.random();
			c[i][1] = Math.random();
			y[i] = Math.random();
			double dx = (c[i][0] - 0.5) * 1.414;
			double dy = (c[i][1] - 0.5) * 1.414;
			y[i] = 1 - (dx * dx + dy * dy);
//			y[i] = (c[i][0] + c[i][1]) * 0.5;
		}
////		c[0][0] = 0.5;
////		c[0][1] = 0.5;
////		y[0] = 0.0;
		
		int dim = c.length + 1 + c[0].length;
		// fill matrix;
		double[] matrix = new double[dim * dim];
		for (int i = 0; i < c.length; i++) {		// row
			for (int j = 0; j < c.length; j++) {	// column
				matrix[i * dim + j] = d(i, j);
			}
			matrix[c.length + i * dim] = 1;
			matrix[c.length * dim + i] = 1;
			for (int k = 0; k < c[0].length; k++) {
				matrix[i * dim + c.length + 1 + k] = c[i][k];
				matrix[(c.length + 1 + k) * dim + i] = c[i][k];
			}
		}
		
		double[] b = new double[dim];
		System.arraycopy(y, 0, b, 0, y.length);
		
//		Utils3d.printMatrix(matrix);
		
//		double[] matrix = new double[] {
//				d(0, 0), d(0, 1), d(0, 2), d(0, 3), 1, c[0],
//				d(1, 0), d(1, 1), d(1, 2), d(1, 3), 1, c[1], 
//				d(2, 0), d(2, 1), d(2, 2), d(2, 3), 1, c[2], 
//				d(3, 0), d(3, 1), d(3, 2), d(3, 3), 1, c[3], 
//				1,       1,       1,       1,       0, 0, 
//				c[0],    c[1],    c[2],    c[3],    0, 0
//		};
		Utils3d.solve(matrix, b);
		System.out.println(Arrays.toString(b));
		System.arraycopy(b, 0, w, 0, y.length);
		System.arraycopy(b, y.length, v, 0, v.length);
		System.out.println(Arrays.toString(w));
		System.out.println(Arrays.toString(v));
//		vv = b[4];
//		v = b[5];
		
		for (int i = 0; i < c.length; i++) {
			System.out.println(i + ": " + y[i] + " = " + y(c[i]));
		}
		final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		double[] p = new double[2];
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				p[0] = (double) x / image.getWidth();
				p[1] = (double) y / image.getHeight();
				float value = (float) Math.max(0, Math.min(y(p), 1));
//				System.out.println(x + "," + y + "=" + value);
//				Color col = new Color(value, value, value);
//				System.out.println(col);
				g2.setColor(new Color(value, value, value));
				g2.fillRect(x, y, 1, 1);
			}
		}
		g2.setColor(Color.RED);
		for (int i = 0; i < c.length; i++) {
			g2.drawOval((int) (c[i][0] * 400) - 2, (int) (c[i][1] * 400) - 2, 5, 5);
		}
		try {
			ImageIO.write(image, "png", new File("/home/sascha/Desktop/polyharmonic_spline_2d_cone.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JPanel panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				g.drawImage(image, 0, 0, null);
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
		return phi(c[i], c[j]);
	}
	
	private double y(double[] x) {
		double y = 0;
		for (int i = 0; i < c.length; i++) {
			y += w[i] * phi(x, c[i]);
		}
		y += v[0];
		for (int i = 1; i < v.length; i++) {
			y += v[i] * x[i - 1];
		}
		return y;
	}
	
	private double phi(double[] a, double[] b) {
		double r = distance(a, b);
		return r;
		
//		return r * r * r;
		
//		int k = 2;
//		if (r < 1) {
//			return Math.pow(r, k - 1) * Math.log(Math.pow(r, r));
//		} else {
//			return Math.pow(r, k) * Math.log(r);
//		}
	}
	
	double distance(double[] a, double[] b) {
		double dsq = 0;
		for (int i = 0; i < a.length; i++) {
			double d = a[i] - b[i];
			dsq += d * d;
		}
		return Math.sqrt(dsq);
	}

}
