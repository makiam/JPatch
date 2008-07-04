package simplex;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;


public class Test {
	public static void main(String[] args) {
		Simplex simplex = new Simplex(new Vector[] {
				new Vector(-1, -1),
				new Vector(5, -1),
				new Vector(-1, 5)
		});
		final Delaunay d = new Delaunay(simplex);
		
		d.add(new Vector(1, 0));
		d.add(new Vector(0, 0));
		d.add(new Vector(0, 1));
//		d.add(new Vector(0, 0));
//		d.add(new Vector(1, 1));
//		d.delaunayPlace(new Pnt(0, 1));
//		dt.delaunayPlace(new Pnt(0, 0));
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
		
		JPanel panel = new JPanel() {
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.scale(50, -50);
				g2.translate(2, -5);
				g2.setStroke(new BasicStroke(0));
				Line2D.Double line = new Line2D.Double();
				
				for (Simplex s : d.getSimplices()) {
					Vector[] pts = s.getVertices();
					for (int i = 0; i < pts.length - 1; i++) {
						for (int j = i + 1; j < pts.length; j++) {
							line.setLine(
									pts[i].getElement(0),// + pts[i].coord(2) * 0.25,
									pts[i].getElement(1),// + pts[i].coord(2) * 0.25,
									pts[j].getElement(0),// + pts[j].coord(2) * 0.25,
									pts[j].getElement(1)// + pts[j].coord(2) * 0.25
							);
							g2.draw(line);
						}
					}
				}
			}
		};
		panel.setPreferredSize(new Dimension(400, 400));
		JFrame frame = new JFrame("Delaunay test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
//	private static void printSimplex(Simplex<Pnt> simplex) {
//		System.out.print(simplex + ":");
//		for (Pnt p : simplex) {
//			System.out.print(p + ", ");
//		}
//		System.out.println();
//	}
	
	
}
