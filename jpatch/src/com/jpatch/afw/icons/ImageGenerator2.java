package com.jpatch.afw.icons;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageGenerator2 {
	static String[] iconNames = {
		"COLLAPSED",
		"EXPANDED",
		"SET_UPPER_LIMIT",
		"SET_LOWER_LIMIT",
		"CLEAR_LIMIT"
	};
	
	public static void main(String[] args) throws Exception {
		ImageGenerator2 ig = new ImageGenerator2();
		for (int i = 0; i < iconNames.length; i++) {
			ig.generateIcon(i);
		}
	}
	void generateIcon(int num) throws IOException {
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		configureGraphics(g);
		drawIcon(num + 1, g);
		ImageIO.write(image, "png", new File("src/com/jpatch/afw/icons/" + iconNames[num] + ".png"));
	}
	
	void configureGraphics(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
	}
	
	void drawIcon(int num, Graphics2D g) {
		g.setComposite(AlphaComposite.DstAtop);
		switch(num) {
		case 0: // test
			g.setColor(new Color(0xff000000, true));
			g.drawRect(0, 0, 15, 15);
			break;
		case 1:	// collapsed
			g.setColor(new Color(0x80000000, true));
			g.fill(new Polygon(new int[] { 5, 13, 5 }, new int[] { 5, 9, 13 }, 3));
			break;
		case 2:	// expanded
			g.setColor(new Color(0x80000000, true));
			g.fill(new Polygon(new int[] { 5, 9, 13 }, new int[] { 5, 13, 5 }, 3));
			break;
		case 3:	// set upper limit
			g.setColor(new Color(0x80000000, true));
			g.fill(new Polygon(new int[] { 3, 8, 13 }, new int[] { 13, 2, 13 }, 3));
			break;
		case 4:	// set lower limit
			g.setColor(new Color(0x80000000, true));
			g.fill(new Polygon(new int[] { 3, 8, 13 }, new int[] { 2, 13, 2 }, 3));
			break;
		case 5:	// clear limit
			g.setColor(new Color(0x80800000, true));
			g.setStroke(new BasicStroke(3));
			g.drawLine(3, 3, 12, 12);
			g.drawLine(3, 12, 12, 3);
			break;
		}
	}
}
