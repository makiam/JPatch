package javaview_test;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;

import simplex.*;

public class Test {
	static Simplex<Pnt> hit;
	
	public static void main(String[] args) {
		Simplex simplex = new Simplex<Pnt>(
				new Pnt(-1, -1),
				new Pnt(5, -1),
				new Pnt(-1, 5)
//				new Pnt(-1, -1, 5)
		);
		final DelaunayTriangulation dt = new DelaunayTriangulation(simplex);
		
		dt.delaunayPlace(new Pnt(1, 0));
		dt.delaunayPlace(new Pnt(0, 1));
		dt.delaunayPlace(new Pnt(0, 0));
//		dt.delaunayPlace(new Pnt(1, 1));
//		dt.delaunayPlace(new Pnt(0, 0.5));
//		dt.delaunayPlace(new Pnt(1, 0.5));
//		dt.delaunayPlace(new Pnt(0.5, 0));
//		dt.delaunayPlace(new Pnt(0.5, 1));
//		dt.delaunayPlace(new Pnt(0.5, 0.5));
//		dt.delaunayPlace(new Pnt(1, 1, 0));
//		dt.delaunayPlace(new Pnt(0, 1, 1));
//		dt.delaunayPlace(new Pnt(1, 0, 1));
//		dt.delaunayPlace(new Pnt(1, 1, 1));
		
		final AffineTransform at = new AffineTransform();
		at.scale(50, -50);
		at.translate(2, -5);
		final JPanel panel = new JPanel() {
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
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
					hit = dt.locate(new Pnt(p.x, p.y));
					panel.repaint();
				} catch (NoninvertibleTransformException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.setPreferredSize(new Dimension(400, 400));
		JFrame frame = new JFrame("Delaunay test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	private static void printSimplex(Simplex<Pnt> simplex) {
		System.out.print(simplex + ":");
		for (Pnt p : simplex) {
			System.out.print(p + ", ");
		}
		System.out.println();
	}
	
	
}
