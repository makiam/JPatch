package javaview_test;

import com.jpatch.afw.vecmath.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import javax.swing.*;

import simplex.*;

public class Test {
	static Simplex<Pnt> hit;
	
	public static void main(String[] args) {
//		Simplex simplex = new Simplex<Pnt>(
//				new Pnt(-1, -1),
//				new Pnt(5, -1),
//				new Pnt(-1, 5)
//		);
//		final DelaunayTriangulation dt = new DelaunayTriangulation(simplex);
		Simplex<Pnt> simplex1 = new Simplex<Pnt>(
				new Pnt(0, 0),
				new Pnt(0, 1),
				new Pnt(1, 0)
		);
		Simplex<Pnt> simplex2 = new Simplex<Pnt>(
				new Pnt(1, 1),
				new Pnt(1, 0),
				new Pnt(0, 1)
		);
		final DelaunayTriangulation dt = new DelaunayTriangulation(simplex1, simplex2);
		final Map<Pnt, Color> colors = new HashMap<Pnt, Color>();
		
//		dt.delaunayPlace(new Pnt(1, 0));
//		dt.delaunayPlace(new Pnt(0, 1));
//		dt.delaunayPlace(new Pnt(0, 0));
//		dt.delaunayPlace(new Pnt(1, 1));
		
//		for (int j = 1; j < 5; j++) {
//			for (int i = 0, n = j + 4; i < n; i++) {
//				dt.delaunayPlace(new Pnt(0.5 + j * 1.0 / 10 * Math.cos(i * 2 * Math.PI / n), 0.5 + j * 1.0 / 10 * Math.sin(i * 2 * Math.PI / n)));
//			}
//		}
//		dt.delaunayPlace(new Pnt(0.25, 0.25));
//		dt.delaunayPlace(new Pnt(0.25, 0.50));
//		dt.delaunayPlace(new Pnt(0.25, 0.75));
//		dt.delaunayPlace(new Pnt(0.50, 0.25));
//		dt.delaunayPlace(new Pnt(0.50, 0.50));
//		dt.delaunayPlace(new Pnt(0.50, 0.75));
//		dt.delaunayPlace(new Pnt(0.75, 0.25));
//		dt.delaunayPlace(new Pnt(0.75, 0.50));
//		dt.delaunayPlace(new Pnt(0.75, 0.75));
		
//		dt.delaunayPlace(new Pnt(0, 0.5));
//		dt.delaunayPlace(new Pnt(1, 0.5));
//		dt.delaunayPlace(new Pnt(0.5, 0));
//		dt.delaunayPlace(new Pnt(0.5, 1));
//		dt.delaunayPlace(new Pnt(0.5, 0.5));
//		dt.delaunayPlace(new Pnt(1, 1, 0));
//		dt.delaunayPlace(new Pnt(0, 1, 1));
//		dt.delaunayPlace(new Pnt(1, 0, 1));
//		dt.delaunayPlace(new Pnt(1, 1, 1));
		for (int i = 0; i < 10; i++) {
			dt.delaunayPlace(new Pnt(Math.random(), Math.random()));
		}
		dt.delaunayPlace(new Pnt(0.00, 0.5));
		for (Simplex<Pnt> s : dt) {
			for (Pnt p : s) {
				Color c = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
				colors.put(p, c);
			}
		}
		
//		colors.put(new Pnt(0, 0), Color.BLACK);
//		colors.put(new Pnt(0, 1), Color.WHITE);
//		colors.put(new Pnt(1, 0), Color.WHITE);
//		colors.put(new Pnt(1, 1), Color.BLACK);
//		colors.put(new Pnt(0.5, 0.5), new Color(0.5f, 0.5f, 0.5f));
		
		final BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();
		for (int y = 0; y < 600; y += 2) {
			double py = y / 600.0;
			for (int x = 0; x < 600; x += 2) {
				double px = x / 600.0;
				double[] point = new double[] { px, py };
				hit = dt.locate(new Pnt(point));
				if (hit != null) {
					double[][] splx = new double[hit.size()][point.length];
					int n = 0;
					for (Pnt pt : hit) {
						for (int i = 0; i < pt.dimension(); i++) {
							splx[n][i] = pt.coord(i);
						}
						n++;
					}
					double[] weights = getWeights(point, splx);
					int i = 0;
					double r = 0, g = 0, b = 0;
					for (Pnt p : hit) {
						Color c = colors.get(p);
						r += weights[i] * c.getRed();
						g += weights[i] * c.getGreen();
						b += weights[i] * c.getBlue();
						i++;
					}
//					System.out.println(r + "," + g + "," + b);
					g2.setColor(new Color((int) r, (int) g, (int) b));
					g2.fillRect(x, y, 2, 2);
				}
			}
			System.out.println(y);
		}
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for (Simplex<Pnt> s : dt) {
			for (Pnt p : s) {
				Color c = colors.get(p);
				int x = (int) (p.coord(0) * 600);
				int y = (int) (p.coord(1) * 600);
				
				g2.setColor(Color.WHITE);
				g2.fillOval(x - 5, y - 5, 11, 11);
				g2.setColor(c);
				g2.fillOval(x - 4, y - 4, 9, 9);
			}
		}
		final AffineTransform at = new AffineTransform();
		at.scale(600, 600);
		at.translate(0.1, 0.1);
		final JPanel panel = new JPanel() {
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setStroke(new BasicStroke(0));
				g2.setColor(Color.WHITE);
				g2.drawImage(img, 60, 60, null);
//				if (true) return;
				g2.setTransform(at);
				g2.setStroke(new BasicStroke(0));
				Pnt[] pts = new Pnt[3];
				Line2D.Double line = new Line2D.Double();
				for (Simplex<Pnt> s : dt) {
					s.toArray(pts);
					for (int i = 0; i < pts.length - 1; i++) {
						for (int j = i + 1; j < pts.length; j++) {
							line.setLine(
									pts[i].coord(0),// + pts[i].coord(2) * 0.25,
									pts[i].coord(1),// + pts[i].coord(2) * 0.25,
									pts[j].coord(0),// + pts[j].coord(2) * 0.25,
									pts[j].coord(1)// + pts[j].coord(2) * 0.25
							);
							g2.draw(line);
						}
					}
				}
				g2.setColor(Color.RED);
				if (hit != null) {
					hit.toArray(pts);
					for (int i = 0; i < pts.length - 1; i++) {
						for (int j = i + 1; j < pts.length; j++) {
							line.setLine(
									pts[i].coord(0),// + pts[i].coord(2) * 0.25,
									pts[i].coord(1),// + pts[i].coord(2) * 0.25,
									pts[j].coord(0),// + pts[j].coord(2) * 0.25,
									pts[j].coord(1)// + pts[j].coord(2) * 0.25
							);
							g2.draw(line);
						}
					}
				}
			}
		};
		panel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point2D.Double p = new Point2D.Double(e.getX(), e.getY());
				try {
					at.inverseTransform(p, p);
					double[] point = new double[] { p.x, p.y };
					hit = dt.locate(new Pnt(point));
					if (hit != null) {
						double[][] simplex = new double[hit.size()][point.length];
						int n = 0;
						for (Pnt pt : hit) {
							for (int i = 0; i < pt.dimension(); i++) {
								simplex[n][i] = pt.coord(i);
							}
							n++;
						}
						getWeights(point, simplex);
					}
					panel.repaint();
				} catch (NoninvertibleTransformException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.setPreferredSize(new Dimension(700, 700));
		JFrame frame = new JFrame("Delaunay test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	private static void printSimplex(double[][] simplex) {
		for (double[] p : simplex) {
			for (double c : p) {
				System.out.print(c);
				System.out.print(" ");
			}
			System.out.println();
		}
	}
	
	private static Color col(double x, double y, double z) {
		return new Color(
				(float) Math.max(0, Math.min(x, 1)),
				(float) Math.max(0, Math.min(y, 1)),
				(float) Math.max(0, Math.min(z, 1))
		);
	}
	private static double[] getWeights(double[] p, double[][] simplex) {
//		printSimplex(simplex);
		int n = p.length;
		double[] matrix = new double[n * n];
		
		for (int i = 1; i < simplex.length; i++) {
			double[] corner = simplex[i];
			for (int j = 0; j < n; j++) {
				matrix[j * n + i - 1] = corner[j] - simplex[0][j];
			}
		}
		for (int i = 0; i < p.length; i++) {
			p[i] -= simplex[0][i];
		}
		double[] inv = Utils3d.invert(matrix, new double[matrix.length]);
//		System.out.println("p= " + Arrays.toString(p));
		double[] p1 = Utils3d.transform(inv, p, new double[n]);
//		System.out.println("p1=" + Arrays.toString(p1));
		double w0 = 1;
		for (double d : p1) {
			w0 -= d;
		}
		double[] w = new double[p1.length + 1];
		w[0] = w0;
		System.arraycopy(p1, 0, w, 1, p1.length);
		return w;
	}
	
	private static void printSimplex(Simplex<Pnt> simplex) {
		System.out.print(simplex + ":");
		for (Pnt p : simplex) {
			System.out.print(p + ", ");
		}
		System.out.println();
	}
	
	
}
