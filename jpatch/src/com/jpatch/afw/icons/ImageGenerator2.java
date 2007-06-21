package com.jpatch.afw.icons;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.batik.ext.awt.LinearGradientPaint;

public class ImageGenerator2 {
	static String[] iconNames = {
		"COLLAPSED",
		"EXPANDED",
		"SET_UPPER_LIMIT",
		"SET_LOWER_LIMIT",
		"CLEAR_LIMIT",
		"CHECKBOX_UNCHECKED",
		"CHECKBOX_CHECKED",
		"CHECKBOX_UNCHECKED_PRESSED",
		"CHECKBOX_CHECKED_PRESSED",
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
			g.fill(new Polygon(new int[] { 4, 9, 14 }, new int[] { 13, 2, 13 }, 3));
			break;
		case 4:	// set lower limit
			g.setColor(new Color(0x80000000, true));
			g.fill(new Polygon(new int[] { 4, 9, 14 }, new int[] { 3, 14, 3 }, 3));
			break;
		case 5:	// clear limit
			g.setColor(new Color(0x80800000, true));
			g.setStroke(new BasicStroke(3));
			g.drawLine(3, 3, 12, 12);
			g.drawLine(3, 12, 12, 3);
			break;
		case 6:	// CHECKBOX_UNCHECKED
			g.translate(0, -1);
			g.setComposite(AlphaComposite.Src);
			g.setColor(new Color(0x30000000, true));
			for (int y = 0; y < 2; y++) {
				for (int x = 0; x < 2; x++) {
					g.fillRoundRect(x + 0, y + 1, 14, 14, 8, 8);
				}
			}
			g.setColor(new Color(0x66000000, true));
			g.fillRoundRect(0, 1, 14, 14, 6, 6);
			g.setPaint(new GradientPaint(0, 8, new Color(0xcccccc), 0, 14, new Color(0xffffff)));
			g.fillRoundRect(1, 2, 12, 12, 6, 6);
			g.setComposite(AlphaComposite.SrcAtop);
			g.setPaint(new GradientPaint(0, 0, new Color(0x00ffffff, true), 0, 8, new Color(0x88ffffff, true)));
			g.fillRoundRect(1, 2, 12, 6, 6, 6);
			break;
		case 7: // CHECKBOX_CHECKED
			g.translate(0, -1);
			g.setComposite(AlphaComposite.Src);
			g.setColor(new Color(0x30000000, true));
			for (int y = 0; y < 2; y++) {
				for (int x = 0; x < 2; x++) {
					g.fillRoundRect(x + 0, y + 1, 14, 14, 8, 8);
				}
			}
			g.setColor(new Color(0x88000000, true));
			g.fillRoundRect(0, 1, 14, 14, 6, 6);
			g.setPaint(new GradientPaint(0, 8, new Color(0xacacb4), 0, 14, new Color(0xcfcfe7)));
			g.fillRoundRect(1, 2, 12, 12, 6, 6);
			g.setComposite(AlphaComposite.SrcAtop);
			g.setPaint(new GradientPaint(0, 0, new Color(0x00ffffff, true), 0, 8, new Color(0xccffffff, true)));
			g.fillRoundRect(1, 2, 12, 6, 6, 6);
			g.setComposite(AlphaComposite.Src);
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.drawLine(11, 1, 6, 11);
			g.drawLine(6, 11, 3, 7);
			break;
		case 8: // CHECKBOX_UNCHECKED_PRESSED
			g.translate(0, -1);
			g.setComposite(AlphaComposite.Src);
			g.setColor(new Color(0x30000000, true));
			for (int y = 0; y < 2; y++) {
				for (int x = 0; x < 2; x++) {
					g.fillRoundRect(x + 0, y + 1, 14, 14, 8, 8);
				}
			}
			g.setColor(new Color(0x88000000, true));
			g.fillRoundRect(0, 1, 14, 14, 6, 6);
			g.setPaint(new GradientPaint(0, 8, new Color(0xacacb4), 0, 14, new Color(0xcfcfe7)));
			g.fillRoundRect(1, 2, 12, 12, 6, 6);
			g.setComposite(AlphaComposite.SrcAtop);
			g.setPaint(new GradientPaint(0, 0, new Color(0x00ffffff, true), 0, 8, new Color(0xccffffff, true)));
			g.fillRoundRect(1, 2, 12, 6, 6, 6);
			break;
		case 9: // CHECKBOX_CHECKED_PRESSED
			g.translate(0, -1);
			g.setComposite(AlphaComposite.Src);
			g.setColor(new Color(0x30000000, true));
			for (int y = 0; y < 2; y++) {
				for (int x = 0; x < 2; x++) {
					g.fillRoundRect(x + 0, y + 1, 14, 14, 8, 8);
				}
			}
			g.setColor(new Color(0x66000000, true));
			g.fillRoundRect(0, 1, 14, 14, 6, 6);
			g.setPaint(new GradientPaint(0, 8, new Color(0xcccccc), 0, 14, new Color(0xffffff)));
			g.fillRoundRect(1, 2, 12, 12, 6, 6);
			g.setComposite(AlphaComposite.SrcAtop);
			g.setPaint(new GradientPaint(0, 0, new Color(0x00ffffff, true), 0, 8, new Color(0x88ffffff, true)));
			g.fillRoundRect(1, 2, 12, 6, 6, 6);
			g.setComposite(AlphaComposite.Src);
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.drawLine(11, 1, 6, 11);
			g.drawLine(6, 11, 3, 7);
			break;
		}
	}
}
