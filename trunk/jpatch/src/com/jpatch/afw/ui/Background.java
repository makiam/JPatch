package com.jpatch.afw.ui;

import java.awt.*;
import java.awt.geom.*;

public class Background {
	private static final Color HUB_COLOR = new Color(0xffffff);
	private static final Color SPOKE_COLOR = new Color(0xa0a0a0);
//	private static final Color HUB_COLOR = Color.WHITE;
//	private static final Color SPOKE_COLOR = Color.BLACK;
	
	public static void fillComponent(Container c, Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Component root = c;
		while (root.getParent() != null) {
			root = root.getParent();
		}
		double rootWidth = root.getWidth();
		double rootHeight = root.getHeight();
		double size = Math.sqrt(rootWidth * rootWidth + rootHeight * rootHeight);
		Point2D.Double hub = new Point2D.Double(rootWidth / 2.0, -rootWidth / 4.0);
		Point2D.Double spoke0 = new Point2D.Double();
		Point2D.Double spoke = new Point2D.Double();
		int step = (int) size / 16;
		for (Container parent = c; parent != root; parent = parent.getParent()) {
			Point p = parent.getLocation();
			hub.x -= p.x;
			hub.y -= p.y;
		}
		for (int y = 0; y < c.getHeight(); y += step) {
			for (int x = 0; x < c.getWidth(); x += step) {
				double dx = (x + step / 2.0) - hub.x;
				double dy = (y + step / 2.0) - hub.y;
				double l =  size / Math.sqrt(dx * dx + dy * dy);
				spoke.x = hub.x + dx * l * 0.5;
				spoke.y = hub.y + dy * l * 0.5;
				spoke0.x = hub.x + dx * l * 0.0;
				spoke0.y = hub.y + dy * l * 0.0;
				g2.setPaint(new GradientPaint(spoke0, HUB_COLOR, spoke, SPOKE_COLOR));
				g.fillRect(x, y, step, step);
			}
		}
	}
}
