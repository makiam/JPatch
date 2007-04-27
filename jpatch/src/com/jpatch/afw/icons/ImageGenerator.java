package com.jpatch.afw.icons;

import com.jpatch.afw.ui.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import javax.swing.JFrame;
import org.apache.batik.ext.awt.LinearGradientPaint;

public class ImageGenerator {
	enum Style { GLOSSY, FROSTED, BRUSHED, DARK }
	enum Type { SINGLE, RND_SINGLE, LARGE_SINGLE, GRP_LEFT, GRP_CENTER, GRP_RIGHT, RND_GRP_LEFT, RND_GRP_CENTER, RND_GRP_RIGHT }
	enum Mode { NORMAL, ROLLOVER, PRESSED, SELECTED, PRESSED_SELECTED }
	
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
		AffineTransform at = g.getTransform();
		g.translate(8, 8);
		drawGroupButtons(Style.GLOSSY, 28, 28, 22, g, false);
		g.translate(28 * 3 + 8, 0);
		drawButton(Style.GLOSSY, 28, 28, g, true);
		g.translate(28 + 8, 0);
		drawGroupButtons(Style.FROSTED, 28, 28, 22, g, false);
		g.translate(28 * 3 + 8, 0);
		drawButton(Style.FROSTED, 28, 28, g, true);
		g.translate(28 + 8, 0);
		drawGroupButtons(Style.BRUSHED, 28, 28, 22, g, false);
		g.translate(28 * 3 + 8, 0);
		drawButton(Style.BRUSHED, 28, 22, g, false);
		g.translate(28 + 8, 0);
		drawGroupButtons(Style.DARK, 28, 28, 28, g, true);
		g.translate(28 * 3 + 8 + 10, 0);
		drawButton(Style.DARK, 28, 28, g, true);
		g.translate(28 + 8, -4);
		drawSwitcher(48, 42, g);
		g.setTransform(at);
		
		Image im = makeIcon(7);
		g.drawImage(im, 16, 12, null);
		im = makeIcon(8);
		g.drawImage(im, 398, 14, null);
		im = makeIcon(9);
		g.drawImage(im, 398 + 28 + 6, 14, null);
		
		frame.add(imagePanel.getComponent());
		frame.pack();
		frame.setVisible(true);
	}
	
	void drawSwitcher(int width, int height, Graphics2D g) {
		int outerWidth = width;
		int innerWidth = outerWidth - 2;
		int innerHeight = height - 2;
		RoundRectangle2D outerRect, innerRect, ooRect;
		outerRect = new RoundRectangle2D.Float(0, 0, outerWidth, height, height / 2.3f, height / 2.3f);
		innerRect = new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight, innerHeight / 2.5f, innerHeight / 2.5f);
		ooRect = new RoundRectangle2D.Float(-1, -1, outerWidth + 2, height + 2, (height + 2) / 2.1f, (height + 2) / 2.1f);
		
		g.setPaint(new GradientPaint(0, 0, new Color(0x20000000, true), 0, height, new Color(0x80ffffff, true)));
		g.fill(ooRect);
		g.setColor(new Color(0x80000000, true));
		g.fill(outerRect);
		
		float halfHeight = innerHeight / 4.0f;
		Area area1 = new Area();
		area1.add(new Area(new Arc2D.Float(1, 1 + halfHeight, halfHeight * 2, halfHeight * 2, 90, 90, Arc2D.PIE)));
		area1.add(new Area(new Rectangle2D.Float(1 + halfHeight, 1 + halfHeight, innerWidth / 2.0f, halfHeight)));
		Area area2 = new Area();
		area2.add(new Area(new Rectangle2D.Float(1 + innerWidth / 2.0f, 1, innerWidth / 2.0f, innerHeight)));
		area2.subtract(new Area(new Ellipse2D.Float(1 + innerWidth - halfHeight * 2, 1 - halfHeight, halfHeight * 2, halfHeight * 2)));
		area2.subtract(new Area(new Rectangle2D.Float(1 + innerWidth / 2.0f, 1, innerWidth / 2.0f - halfHeight, halfHeight)));
		area1.add(area2);
		Area area3 = new Area(area1);
		area3.transform(new AffineTransform(1, 0, 0, 1, 0, halfHeight * 2));
		area1.intersect(new Area(innerRect));
		area1.intersect(new Area(new Rectangle2D.Float(0, 0, width, height / 2.0f)));
		area3.intersect(new Area(innerRect));
		
//		g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.25f, 0.5f, 0.5f, 0.75f, 1.0f }, new Color[] { new Color(1.00f, 1.00f, 1.00f), new Color(0.80f, 0.80f, 0.80f), new Color(0.85f, 0.85f, 0.85f), new Color(1.00f, 1.00f, 1.00f), new Color(0.80f, 0.80f, 0.80f), new Color(0.85f, 0.85f, 0.85f) } ));
//		g.fill(innerRect);
		
		g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.5f, 0.5f, 1.0f }, new Color[] { new Color(0xe4e4e4), new Color(0xffffff), new Color(0xe4e4e4), new Color(0xffffff) } ));
		g.fill(innerRect);
		g.setPaint(new GradientPaint(0, 1, new Color(0xb8b8b8), 0, 1 + halfHeight * 2, new Color(0xffffff)));
		g.fill(area1);
		g.setPaint(new GradientPaint(0, halfHeight * 2 + 1, new Color(0xb8b8b8), 0, halfHeight * 2 + 1 + halfHeight * 2, new Color(0xffffff)));
		g.fill(area3);
		
		g.setColor(new Color(0x40000000, true));
		g.drawLine(width / 2 - 1, 1, width / 2 - 1, innerHeight);
		g.drawLine(1, height / 2 - 1, width - 2, height / 2 - 1);
		g.setColor(new Color(0x40ffffff, true));
		g.drawLine(width / 2, 1, width / 2, innerHeight);
		g.drawLine(1, height / 2, width - 2, height / 2);
		
		Font font = new Font("monospaced", Font.BOLD, 20);
		g.drawImage(ImageUtils.createTextIcon(font, new Color(0x30000000, true), "1"), width / 4 - 6, height / 4 - 10, null);
		g.drawImage(ImageUtils.createTextIcon(font, new Color(0x30000000, true), "2"), width * 3 / 4 - 8, height / 4 - 10, null);
		g.drawImage(ImageUtils.createTextIcon(font, new Color(0x30000000, true), "3"), width / 4 - 5, height * 3 / 4 - 11, null);
		g.drawImage(ImageUtils.createTextIcon(font, new Color(0x30000000, true), "4"), width * 3 / 4 - 9, height * 3 / 4 - 11, null);
	}
	
	void drawButton(Style style, int width, int height, Graphics2D g, boolean round) {
		int outerWidth = width;
		int innerWidth = outerWidth - 2;
		int innerHeight = height - 2;
		RoundRectangle2D outerRect, innerRect, ooRect;
		if (round) {
			outerRect = new RoundRectangle2D.Float(0, 0, outerWidth, height, height, height);
			innerRect = new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight, innerHeight, innerHeight);
			ooRect = new RoundRectangle2D.Float(-1, -1, outerWidth + 2, height + 2, height + 2, height + 2);
		} else {
			ooRect = new RoundRectangle2D.Float(-1, -1, outerWidth + 2, height + 2, (height + 2) / 2.1f, (height + 2) / 2.1f);
			outerRect = new RoundRectangle2D.Float(0, 0, outerWidth, height, height / 2.3f, height / 2.3f);
			innerRect = new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight, innerHeight / 2.5f, innerHeight / 2.5f);
		}
		
		g.setPaint(new GradientPaint(0, 0, new Color(0x20000000, true), 0, height, new Color(0x80ffffff, true)));
		g.fill(ooRect);
		g.setColor(new Color(0x80000000, true));
		g.fill(outerRect);
		switch (style) {
		case GLOSSY:
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
		case FROSTED:
//			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.3f, 0.7f, 1.0f }, new Color[] { new Color(1.0f, 1.0f, 1.0f), new Color(0.85f, 0.85f, 0.85f), new Color(0.7f, 0.7f, 0.7f), new Color(0.50f, 0.50f, 0.50f) } ));
			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.5f, 1.0f }, new Color[] { new Color(1.00f, 1.00f, 1.00f), new Color(0.80f, 0.80f, 0.80f), new Color(0.85f, 0.85f, 0.85f) } ));
//			g.setPaint(new GradientPaint(0, 1, new Color(0xffffff), 0, 1 + innerHeight, new Color(0xa0a0a0)));
			g.fill(innerRect);
			break;
		case BRUSHED:
//			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.3f, 0.7f, 1.0f }, new Color[] { new Color(1.0f, 1.0f, 1.0f), new Color(0.85f, 0.85f, 0.85f), new Color(0.7f, 0.7f, 0.7f), new Color(0.50f, 0.50f, 0.50f) } ));
//			g.setPaint(new LinearGradientPaint(0, 1, 0, 1 + innerHeight,new float[] { 0.0f, 0.5f, 1.0f }, new Color[] { new Color(0.95f, 0.95f, 0.95f), new Color(0.75f, 0.75f, 0.75f), new Color(0.80f, 0.80f, 0.80f) } ));
			g.setPaint(new GradientPaint(0, 1, new Color(0xffffff), 0, 1 + innerHeight, new Color(0xa0a0a0)));
			g.fill(innerRect);
			break;
		case DARK:
			g.setPaint(new GradientPaint(0, 1, new Color(0x545454), 0, 1 + innerHeight, new Color(0x444444)));
			g.fill(innerRect);
			g.setPaint(new GradientPaint(0, 1, new Color(0xa0a0a0), 0, 1 + innerHeight * 0.5f, new Color(0x505050)));
			Area area = new Area(new RoundRectangle2D.Float(1, 1, innerWidth, innerHeight * 0.5f, innerHeight * 0.5f, innerHeight * 0.5f));
			area.intersect(new Area(innerRect));
			g.fill(area);
			if (round) {
				g.setPaint(new GradientPaint(0, 1, new Color(0xffffffff, true), 0, 1 + innerHeight * 0.25f, new Color(0x00ffffff, true)));
			} else {
				g.setPaint(new GradientPaint(0, 1, new Color(0x80ffffff, true), 0, 1 + innerHeight * 0.25f, new Color(0x00ffffff, true)));
			}
			g.draw(innerRect);
			break;
	}
		
	}
	
	void drawGroupButtons(Style style, int borderWidth, int centerWidth, int height, Graphics2D g, boolean round) {
		int outerWidth = 2 * borderWidth + centerWidth;
		int innerHeight = height - 2;
		
		if (round) {
			outerWidth = 3 * height + 10;
			RoundRectangle2D.Float outerRect = new RoundRectangle2D.Float(-3, -3, outerWidth + 6, height + 6, height + 6, height + 6);
			RoundRectangle2D.Float innerRect = new RoundRectangle2D.Float(-2, -2, outerWidth + 4, height + 4, height + 4, height + 6);
			g.setPaint(new GradientPaint(0, -3, new Color(0x40000000, true), 0, height + 6, new Color(0x80ffffff, true)));
			g.fill(outerRect);
//			g.setPaint(new GradientPaint(0, -2, new Color(0xb0b0b0), 0, height + 4, new Color(0xd0d0d0)));
//			g.fill(innerRect);
			g.translate(1, 0);
			drawButton(style, height, height, g, true);
			g.translate(height + 4, 0);
			drawButton(style, height, height, g, true);
			g.translate(height + 4, 0);
			drawButton(style, height, height, g, true);
			g.translate(-2 * (height + 2) - 1, 0);
		} else {
			drawButton(style, outerWidth, height, g, false);
			g.setColor(new Color(0x40000000, true));
			g.drawLine(borderWidth - 1, 1, borderWidth - 1, innerHeight);
			g.drawLine(outerWidth - borderWidth - 1, 1, outerWidth - borderWidth - 1, innerHeight);
			g.setColor(new Color(0x40ffffff, true));
			g.drawLine(borderWidth, 1, borderWidth, innerHeight);
			g.drawLine(outerWidth - borderWidth, 1, outerWidth - borderWidth, innerHeight);
		}
	}
	
	void configureGraphics(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
	}
	
	void drawBackground(Graphics2D g) {
		g.setPaint(new GradientPaint(0, 0, new Color(0xcccccc), 0, 50, new Color(0x999999)));
		g.fillRect(0, 0, 600, 50);
	}
	
	Image makeIcon(int num) {
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		configureGraphics(g);
		drawIcon(num, g);
		return image;
	}
	
	void drawIcon(int num, Graphics2D g) {
		g.setComposite(AlphaComposite.DstAtop);
		switch(num) {
		case 1:	// vertex mode
			g.setColor(new Color(0x40000000, true));
			g.drawLine(1, 3, 7, 1);
			g.drawLine(7, 1, 13, 3);
			g.drawLine(13, 3, 7, 5);
			g.drawLine(7, 5, 1, 3);
			g.drawLine(13, 11, 7, 13);
			g.drawLine(7, 13, 1, 11);
			g.drawLine(1, 3, 1, 11);
			g.drawLine(7, 5, 7, 13);
			g.drawLine(13, 3, 13, 11);	
			g.setColor(new Color(0xc0000000, true));
			g.fill(new Ellipse2D.Float(0, 2, 3, 3));
			g.fill(new Ellipse2D.Float(6, 4, 3, 3));
			g.fill(new Ellipse2D.Float(0, 10, 3, 3));
			g.fill(new Ellipse2D.Float(6, 12, 3, 3));
			break;
		case 2:	// edge mode
			g.setColor(new Color(0x40000000, true));
			g.drawLine(1, 3, 7, 1);
			g.drawLine(7, 1, 13, 3);
			g.drawLine(13, 3, 7, 5);
			g.drawLine(13, 11, 7, 13);
			g.drawLine(13, 3, 13, 11);	
			g.setColor(new Color(0xc0000000, true));
			g.drawLine(7, 5, 1, 3);
			g.drawLine(7, 13, 1, 11);
			g.drawLine(1, 3, 1, 11);
			g.drawLine(7, 5, 7, 13);
			break;
		case 3:	// face mode
//			g.setComposite(AlphaComposite.Src);
			g.setColor(new Color(0x40000000, true));
			g.drawLine(1, 3, 7, 1);
			g.drawLine(7, 1, 13, 3);
			g.drawLine(13, 3, 7, 5);
			g.drawLine(7, 5, 1, 3);
			g.drawLine(13, 11, 7, 13);
			g.drawLine(7, 13, 1, 11);
			g.drawLine(1, 3, 1, 11);
			g.drawLine(7, 5, 7, 13);
			g.drawLine(13, 3, 13, 11);	
//			g.setComposite(AlphaComposite.Xor);
			g.setColor(new Color(0x40000000, true));
			GeneralPath p = new GeneralPath();
			p.moveTo(1, 3);
			p.lineTo(8, 5);
			p.lineTo(8, 14);
			p.lineTo(1, 12);
			p.closePath();
			g.fill(p);
			g.setColor(new Color(0xc0000000, true));
			g.drawLine(7, 5, 1, 3);
			g.drawLine(7, 13, 1, 11);
			g.drawLine(1, 3, 1, 11);
			g.drawLine(7, 5, 7, 13);
			break;
		case 4:	// object mode
//			g.setComposite(AlphaComposite.Src);
			g.setColor(new Color(0xc0000000, true));
			g.drawLine(1, 3, 7, 1);
			g.drawLine(7, 1, 13, 3);
			g.drawLine(13, 3, 7, 5);
			g.drawLine(7, 5, 1, 3);
			g.drawLine(13, 11, 7, 13);
			g.drawLine(7, 13, 1, 11);
			g.drawLine(1, 3, 1, 11);
			g.drawLine(7, 5, 7, 13);
			g.drawLine(13, 3, 13, 11);
			g.setComposite(AlphaComposite.Xor);
			g.setColor(new Color(0x40000000, true));
			p = new GeneralPath();
			p.moveTo(1, 3);
			p.lineTo(7, 1);
			p.lineTo(14, 3);
			p.lineTo(14, 11);
			p.lineTo(7, 14);
			p.lineTo(1, 12);
			p.closePath();
			g.fill(p);
			break;
		case 5: // zoom view
			g.setColor(new Color(0xc0000000, true));
			g.draw(new Ellipse2D.Float(0, 0, 10, 10));
			g.setStroke(new BasicStroke(1.5f));
			g.drawLine(9, 9, 13, 13);
			g.setStroke(new BasicStroke(2.5f));
			g.drawLine(11, 11, 13, 13);
			break;
		case 6: // move view
			g.setColor(new Color(0xc0000000, true));
			g.drawLine(7, 4, 7, 1);
			g.drawLine(10, 7, 13, 7);
			g.drawLine(7, 10, 7, 13);
			g.drawLine(4, 7, 1, 7);
			g.draw(new Polygon(new int[] { 7, 9, 5 }, new int[] { 0, 2, 2 }, 3));
			g.draw(new Polygon(new int[] { 0, 2, 2 }, new int[] { 7, 9, 5 }, 3));
			g.draw(new Polygon(new int[] { 7, 9, 5 }, new int[] { 14, 12, 12 }, 3));
			g.draw(new Polygon(new int[] { 14, 12, 12 }, new int[] { 7, 9, 5 }, 3));
			break;
		case 7: // rotate view
			g.setColor(new Color(0x20000000, true));
			g.drawArc(2, 2, 12, 11, 45, 300);
			g.setColor(new Color(0x40000000, true));
			g.drawArc(2, 2, 11, 11, 45, 270);
			g.setColor(new Color(0x60000000, true));
			g.drawArc(2, 2, 11, 11, 45, 240);
			g.setColor(new Color(0x80000000, true));
			g.drawArc(2, 2, 11, 11, 45, 210);
			g.setColor(new Color(0xa0000000, true));
			g.drawArc(2, 2, 11, 11, 45, 180);
			g.setColor(new Color(0xc0000000, true));
			g.drawArc(2, 2, 11, 11, 45, 150);
			g.fill(new Polygon(new int[] { 14, 9, 14 }, new int[] { 6, 6, 1 }, 3));
			break;
		case 8: // undo
			g.setPaint(new GradientPaint(0, 0, new Color(0xfffff8f8, true), 0, 8, new Color(0x00fff8f8, true)));
			g.setStroke(new BasicStroke(4));
			g.drawArc(-2, 2, 13, 13, 300, 110);
			g.setPaint(new GradientPaint(0, 0, new Color(0xfffff0f0, true), 0, 12, new Color(0x00fff0f0, true)));
			g.setStroke(new BasicStroke(3));
			g.drawArc(-1, 2, 12, 13, 300, 110);
			g.setPaint(new GradientPaint(0, 0, new Color(0xffffe8e8, true), 0, 16, new Color(0x00ffe8e8, true)));
			g.setStroke(new BasicStroke(2));
			g.drawArc(0, 2, 11, 13, 300, 110);
			g.setPaint(new GradientPaint(0, 0, new Color(0xffffffff, true), 0, 12, new Color(0x00ffffff, true)));
			g.fill(new Polygon(new int[] { 5, 13, 5 }, new int[] { 0, 1, 7 }, 3));
			break;
		case 9: // redo
			g.setTransform(new AffineTransform(-1, 0, 0, 1, 16, 0));
			g.setPaint(new GradientPaint(0, 0, new Color(0xfff8f8ff, true), 0, 8, new Color(0x00f8f8ff, true)));
			g.setStroke(new BasicStroke(4));
			g.drawArc(-2, 2, 13, 13, 300, 110);
			g.setPaint(new GradientPaint(0, 0, new Color(0xfff0f0ff, true), 0, 12, new Color(0x00f0f0ff, true)));
			g.setStroke(new BasicStroke(3));
			g.drawArc(-1, 2, 12, 13, 300, 110);
			g.setPaint(new GradientPaint(0, 0, new Color(0xffe8e8ff, true), 0, 16, new Color(0x00e8e8ff, true)));
			g.setStroke(new BasicStroke(2));
			g.drawArc(0, 2, 11, 13, 300, 110);
			g.setPaint(new GradientPaint(0, 0, new Color(0xffffffff, true), 0, 12, new Color(0x00ffffff, true)));
			g.fill(new Polygon(new int[] { 5, 13, 5 }, new int[] { 0, 1, 7 }, 3));
			break;
		}
	}
}
