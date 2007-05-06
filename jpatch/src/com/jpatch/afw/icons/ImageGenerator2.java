package com.jpatch.afw.icons;

import java.awt.AlphaComposite;
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
		"EXPANDED"
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
			g.fill(new Polygon(new int[] { 2, 14, 2 }, new int[] { 2, 8, 14 }, 3));
			break;
		case 2:	// expanded
			g.setColor(new Color(0x80000000, true));
			g.fill(new Polygon(new int[] { 2, 8, 14 }, new int[] { 2, 14, 2 }, 3));
			break;
		}
	}
}
