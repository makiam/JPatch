package com.jpatch.boundary.headupdisplay;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

public class Borders {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		JFrame frame = new JFrame("Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel label = new JLabel(new ImageIcon(createImage()));
		frame.add(label);
		frame.pack();
		frame.setVisible(true);
		ImageIO.write(createImage(), "png", new File("src/com/jpatch/boundary/headupdisplay/windowBorders.png"));
	}

	
	static BufferedImage createImage() {
		final BufferedImage image = new BufferedImage(80, 60, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		for (int j = 0; j < 3; j++) {
			if (j == 0) {
				g2.setPaint(new GradientPaint(0, 0, new Color(0.6f, 0.6f, 0.6f, 0.5f), 0, 20, new Color(0.5f, 0.5f, 0.5f, 0.5f)));
			} else {
				g2.setPaint(new GradientPaint(0, 0, new Color(0.6f, 0.6f, 0.6f, 0.6f), 0, 20, new Color(0.5f, 0.5f, 0.5f, 0.5f)));
			}
			g2.setClip(0, 0, 80, 20);
			g2.fillRoundRect(0, 0, 80, 40, 19, 19);
			g2.setClip(null);
			
//			if (j > 0) {
				
			
				for (int i = 0; i < 3; i++) {
					if (j > 1) {
						g2.setPaint(new GradientPaint(0, 0, new Color(0x777777 + 0x333333 * j), 0, 20, new Color(0x888888 + 0x333333 * j)));
					} else {
						g2.setPaint(new GradientPaint(0, 0, new Color(0x77333333, true), 0, 20, new Color(0x77333333, true)));
					}
					g2.fill(new Ellipse2D.Double(30 + i * 16, 3, 15, 15));
					if (j > 1) {
						g2.setPaint(new GradientPaint(0, 0, new Color(0x888888 + 0x333333 * j), 0, 10, new Color(0x333333 + 0x000000 * j)));
						g2.fill(new Ellipse2D.Double(31.2 + i * 16, 4.2, 12.6, 12.6));
					}
				}
				
				g2.setColor(new Color(0xaaaaaa + 0x222222 * j));
				g2.setStroke(new BasicStroke(j == 2 ? 1.7f : 1.5f));
				
				g2.draw(new Line2D.Double(66.5, 7, 72, 13));
				g2.draw(new Line2D.Double(66.5, 13, 72, 7));
				
				g2.draw(new Line2D.Double(50.5, 10, 56, 10));
				g2.draw(new Line2D.Double(53.25, 7, 53.25, 13));
				
				g2.draw(new Line2D.Double(34.5, 10, 40, 10));
//			}
			g2.translate(0, 20);
		}
		return image;
	}
}
