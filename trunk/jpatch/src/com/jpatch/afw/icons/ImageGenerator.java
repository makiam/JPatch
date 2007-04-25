package com.jpatch.afw.icons;

import com.jpatch.afw.ui.ImagePanel;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import org.apache.batik.ext.awt.LinearGradientPaint;

public class ImageGenerator {
	enum Style { GLOSSY_GLASS, FROSTED_GLASS, BRUSHED_METAL }
	
	public static void main(String[] args) {
		new ImageGenerator().test();
	}
	
	void test() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
		ImagePanel imagePanel = new ImagePanel(image);
		Graphics2D g = image.createGraphics();
		configureGraphics(g);
		drawBackground(g);
		g.translate(8, 8);
		drawGroupButtons(Style.GLOSSY_GLASS, 28, 28, 22, g);
		g.translate(28 * 3 + 8, 0);
		drawButton(Style.GLOSSY_GLASS, 28, 28, g, true);
		g.translate(28 + 8, 0);
		drawGroupButtons(Style.FROSTED_GLASS, 28, 28, 22, g);
		g.translate(28 * 3 + 8, 0);
		drawButton(Style.FROSTED_GLASS, 28, 28, g, true);
		g.translate(28 + 8, 0);
		drawGroupButtons(Style.BRUSHED_METAL, 28, 28, 22, g);
		g.translate(28 * 3 + 8, 0);
		drawButton(Style.BRUSHED_METAL, 28, 22, g, false);
		g.translate(28 + 8, -4);
		drawSwitcher(48, 42, g);
		frame.add(imagePanel.getComponent());
		frame.pack();
		frame.setVisible(true);
	}
	
	void drawSwitcher(int width, int height, Graphics2D g) {
		int outerWidth = width;
		int innerWidth = outerWidth - 2;
		int innerHeight = height - 2;
		RoundRectangle2D outerRect, innerRect;
		outerRect = new RoundRectangle2D.Float(0, 0, outerWidth, height, height / 2.3f, height / 2.3f);
		innerRect = new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight, innerHeight / 2.5f, innerHeight / 2.5f);
		
		g.setColor(new Color(0x80ffffff, true));
		g.translate(0, 1);
		g.fill(outerRect);
		g.setColor(new Color(0x80000000, true));
		g.translate(0, -1);
		g.fill(outerRect);
		
		g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.25f, 0.5f, 0.5f, 0.75f, 1.0f }, new Color[] { new Color(1.00f, 1.00f, 1.00f), new Color(0.80f, 0.80f, 0.80f), new Color(0.85f, 0.85f, 0.85f), new Color(1.00f, 1.00f, 1.00f), new Color(0.80f, 0.80f, 0.80f), new Color(0.85f, 0.85f, 0.85f) } ));
		g.fill(innerRect);
		
		g.setColor(new Color(0x20000000, true));
		g.drawLine(width / 2 - 1, 1, width / 2 - 1, innerHeight);
		g.drawLine(1, height / 2 - 1, width - 2, height / 2 - 1);
		g.setColor(new Color(0x40ffffff, true));
		g.drawLine(width / 2, 1, width / 2, innerHeight);
		g.drawLine(1, height / 2, width - 2, height / 2);
	}
	
	void drawButton(Style style, int width, int height, Graphics2D g, boolean round) {
		int outerWidth = width;
		int innerWidth = outerWidth - 2;
		int innerHeight = height - 2;
		RoundRectangle2D outerRect, innerRect;
		if (round) {
			outerRect = new RoundRectangle2D.Float(0, 0, outerWidth, height, height, height);
			innerRect = new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight, innerHeight, innerHeight);
		} else {
			outerRect = new RoundRectangle2D.Float(0, 0, outerWidth, height, height / 2.3f, height / 2.3f);
			innerRect = new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight, innerHeight / 2.5f, innerHeight / 2.5f);
		}
		

		g.setColor(new Color(0x80ffffff, true));
		g.translate(0, 1);
		g.fill(outerRect);
		g.setColor(new Color(0x80000000, true));
		g.translate(0, -1);
		g.fill(outerRect);
		switch (style) {
		case GLOSSY_GLASS:
			float halfHeight = innerHeight / 2.0f;
			Area area1 = new Area();
			area1.add(new Area(new Arc2D.Float(1, 1 + halfHeight, halfHeight * 2, halfHeight * 2, 90, 90, Arc2D.PIE)));
			area1.add(new Area(new Rectangle2D.Float(1 + halfHeight, 1 + halfHeight, innerWidth / 2.0f, halfHeight)));
			Area area2 = new Area();
			area2.add(new Area(new Rectangle2D.Float(1 + innerWidth / 2.0f, 1, innerWidth / 2.0f, innerHeight)));
			area2.subtract(new Area(new Ellipse2D.Float(1 + innerWidth - innerHeight, 1 - halfHeight, halfHeight * 2, halfHeight * 2)));
			area2.subtract(new Area(new Rectangle2D.Float(1 + innerWidth / 2.0f, 1, innerWidth / 2.0f - halfHeight, halfHeight)));
			area1.add(area2);
			area1.intersect(new Area(innerRect));
			g.setPaint(new GradientPaint(0, 1, new Color(0xe4e4e4), 0, 1 + innerHeight, new Color(0xffffff)));
			g.fill(innerRect);
			g.setPaint(new GradientPaint(0, 1, new Color(0xb8b8b8), 0, 1 + innerHeight, new Color(0xffffff)));
			g.fill(area1);
			break;
		case FROSTED_GLASS:
//			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.3f, 0.7f, 1.0f }, new Color[] { new Color(1.0f, 1.0f, 1.0f), new Color(0.85f, 0.85f, 0.85f), new Color(0.7f, 0.7f, 0.7f), new Color(0.50f, 0.50f, 0.50f) } ));
			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.5f, 1.0f }, new Color[] { new Color(1.00f, 1.00f, 1.00f), new Color(0.80f, 0.80f, 0.80f), new Color(0.85f, 0.85f, 0.85f) } ));
//			g.setPaint(new GradientPaint(0, 1, new Color(0xffffff), 0, 1 + innerHeight, new Color(0xa0a0a0)));
			g.fill(innerRect);
			break;
		case BRUSHED_METAL:
//			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.3f, 0.7f, 1.0f }, new Color[] { new Color(1.0f, 1.0f, 1.0f), new Color(0.85f, 0.85f, 0.85f), new Color(0.7f, 0.7f, 0.7f), new Color(0.50f, 0.50f, 0.50f) } ));
//			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.5f, 1.0f }, new Color[] { new Color(0.95f, 0.95f, 0.95f), new Color(0.75f, 0.75f, 0.75f), new Color(0.80f, 0.80f, 0.80f) } ));
			g.setPaint(new GradientPaint(0, 1, new Color(0xffffff), 0, 1 + innerHeight, new Color(0xa0a0a0)));
			g.fill(innerRect);
		break;
	}
		
	}
	
	void drawGroupButtons(Style style, int borderWidth, int centerWidth, int height, Graphics2D g) {
		int outerWidth = 2 * borderWidth + centerWidth;
		int innerWidth = outerWidth - 2;
		int innerHeight = height - 2;
		
		drawButton(style, outerWidth, height, g, false);
		
		g.setColor(new Color(0x40000000, true));
		g.drawLine(borderWidth - 1, 1, borderWidth - 1, innerHeight);
		g.drawLine(outerWidth - borderWidth - 1, 1, outerWidth - borderWidth - 1, innerHeight);
		g.setColor(new Color(0x40ffffff, true));
		g.drawLine(borderWidth, 1, borderWidth, innerHeight);
		g.drawLine(outerWidth - borderWidth, 1, outerWidth - borderWidth, innerHeight);
	}
	
	void configureGraphics(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
	}
	
	void drawBackground(Graphics2D g) {
		g.setPaint(new GradientPaint(0, 0, new Color(0xcccccc), 0, 50, new Color(0x999999)));
		g.fillRect(0, 0, 600, 50);
	}
}
