package com.jpatch.afw.test;

import java.awt.*;
import java.awt.geom.Point2D;

import javax.swing.*;

public class FillTest {
	
	public static void main(String[] args) throws Exception {
		new FillTest();
	}
	
	FillTest() throws Exception {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setLayout(null);
		JComponent c = new JComponent() {
			public void paintComponent(Graphics g) {
				fillComponent(this, g);
			}
		};
		frame.add(c);
		int x = 0;
		while (true) {
			c.setBounds(x++, 100, 400, 300);
			Thread.sleep(20);
		}
	}
	
	private void fillComponent(JComponent c, Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		double rootWidth = c.getRootPane().getWidth();
		double rootHeight = c.getRootPane().getHeight();
		double size = Math.sqrt(rootWidth * rootWidth + rootHeight * rootHeight);
		Point2D.Double hub = new Point2D.Double(rootWidth / 2.0, -size / 4.0);
		Point2D.Double spoke = new Point2D.Double();
		
		int step = (int) size / 9;
		
		int xoff = 0, yoff = 0;
		for (Container parent = c; parent != null; parent = parent.getParent()) {
			Point p = parent.getLocation();
			xoff += p.x;
			yoff += p.y;
		}
		System.out.println(xoff + "," + yoff);
		
		hub.x -= xoff;
		hub.y -= yoff;
		
		for (int y = 0; y < c.getHeight(); y += step) {
			for (int x = 0; x < c.getWidth(); x += step) {
				double dx = (x + step / 2.0) - hub.x;
				double dy = (y + step / 2.0) - hub.y;
				double l =  size / Math.sqrt(dx * dx + dy * dy);
				spoke.x = hub.x + dx * l;
				spoke.y = hub.y + dy * l;
				g2.setPaint(new GradientPaint(hub, Color.WHITE, spoke, Color.GRAY));
				g.fillRect(x, y, step, step);
			}
		}
	}
}
