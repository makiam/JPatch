package test;

import com.jpatch.afw.vecmath.*;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

public class PolyharmonicSpline2 {
	int dd = 3;
	double[][] c = new double[][] {
			{0, 0},
			{0, 1},
			{1, 0}
	};
	double[] y = new double[] {
			0, 0, 0,
			0, 1, 0,
			1, 0, 0
	};
//	double[] w = new double[c.length * 2];
//	double[] v = new double[(c[0].length + 1) * 2];
	double[] x;
	
	double py;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PolyharmonicSpline2();
	}
	
	PolyharmonicSpline2() {
		int num = 100;
		c = new double[num][2];
		y = new double[num * dd];
		for (int i = 0; i < num; i++) {
//			c[i][0] = Math.random();
//			c[i][1] = Math.random();
			double r = Math.random() * 0.5;
			double a = Math.random() * Math.PI * 2;
			c[i][0] = 0.5 + Math.cos(a) * r;
			c[i][1] = 0.5 + Math.sin(a) * r;
			y[i * dd] = c[i][0];
			y[i * dd + 1] = c[i][1];
			double dx = c[i][0] - 0.5;
			double dy = c[i][1] - 0.5;
//			double r = Math.sqrt(dx * dx + dy * dy);
			y[i * dd + 2] = 0.5 + 0.5 * Math.cos(r * 40) * (0.5 - r);
//			y[i * dd + 2] = 0.5 + 0.5 * Math.cos(r * 40) * (0.5 - r);
		}

		
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
		
		double[] b = new double[dim * dd];
		x = new double[dim * dd];
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
		long t = System.currentTimeMillis();
//		for (int i = 0; i < 100; i++) {
			Utils3d.solve(matrix, b, x);
//		}
		System.out.println((double) (System.currentTimeMillis() - t));
//		System.out.println(Arrays.toString(b));
//		System.arraycopy(b, 0, w, 0, y.length);
//		System.arraycopy(b, y.length, v, 0, v.length);
//		System.out.println(Arrays.toString(w));
//		System.out.println(Arrays.toString(v));
//		vv = b[4];
//		v = b[5];
//		
//		for (int i = 0; i < c.length; i++) {
//			System.out.println(i + ": " + y[i] + " = " + y(c[i]));
//		}
		final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		double[] p = new double[2];
		double[] value = new double[dd];
		float[] rgb = new float[3];
		t = System.currentTimeMillis();
		for (int iy = 0; iy < image.getHeight(); iy += 4) {
			System.out.println(iy);
			for (int ix = 0; ix < image.getWidth(); ix += 4) {
				p[0] = (double) ix / image.getWidth();
				p[1] = (double) iy / image.getHeight();
				evaluate(p, value);
				for (int i = 0; i < 3; i++) {
					rgb[i] = (float) Math.max(0, Math.min(value[i], 1));
				}
				g2.setColor(new Color(rgb[0], rgb[1], rgb[2]));
				g2.fillRect(ix, iy, 4, 4);
			}
		}
		System.out.println((double) (System.currentTimeMillis() - t) / 100 / 100);
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
		final JPanel panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				double[] p = new double[2];
				double[] value = new double[dd];
				super.paint(g);
				g.drawImage(image, 0, 0, null);
				p[1] = py;
				g.setColor(Color.WHITE);
				for (int ix = 0; ix < image.getWidth(); ix++) {
					p[0] = (double) ix / image.getWidth();
					evaluate(p, value);
					g.fillRect(ix, 300 - (int) (200.0 * value[2]), 1, 1);
				}
			}
		};
		JFrame frame = new JFrame("test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel.setPreferredSize(new Dimension(400, 400));
		panel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				py = 1.0 - e.getY() / 400.0;
				panel.repaint();
			}
		});
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	private double d(int i, int j) {
		return phi(c[i], c[j]);
	}
	
//	private double y(double[] x) {
//		double y = 0;
//		for (int i = 0; i < c.length; i++) {
//			y += w[i] * phi(x, c[i]);
//		}
//		y += v[0];
//		for (int i = 1; i < v.length; i++) {
//			y += v[i] * x[i - 1];
//		}
//		return y;
//	}
	
	private void evaluate(double[] pos, double[] y) {
		for (int i = 0; i < y.length; i++) {
			y[i] = x[c.length * dd + i];
		}
		for (int i = 0; i < c.length; i++) {
			double phi = phi(pos, c[i]);
			int row = i * dd;
			for (int j = 0; j < y.length; j++) {
				y[j] += x[row + j] * phi;
			}
			
		}
		for (int i = 0; i < c[0].length; i++) {
			int row = (c.length + 1 + i) * dd;
			for (int j = 0; j < y.length; j++) {
				y[j] += x[row + j] * pos[i];
			}
		}
	}
	
	private double phi(double[] a, double[] b) {
		double r = distance(a, b);
//		return r;
		
//		return r * r * r;
		
//		int k = 2;
//		if (r < 1) {
//			return Math.pow(r, k - 1) * Math.log(Math.pow(r, r));
//		} else {
//			return Math.pow(r, k) * Math.log(r);
//		}
		
		if (r < 1) {
			return r * Math.log(Math.pow(r, r));
		} else {
			return r * r * Math.log(r);
		}
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
