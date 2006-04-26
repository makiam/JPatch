/*
 * $Id$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package utilities;

import com.sun.org.apache.bcel.internal.util.ClassLoader;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

import jpatch.VersionInfo;

/**
 * @author sascha
 *
 */
public class LogoMaker {
	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		frame.setUndecorated(true);
		final BufferedImage image = createSpashImage();
		final Dimension dim = new Dimension(image.getWidth(), image.getHeight());
		JPanel panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				g.drawImage(image, 0, 0, null);
			}
			@Override
			public Dimension getPreferredSize() {
				return dim;
			}
		};
		frame.add(panel);
		frame.pack();
		frame.setLocation(200, 200);
		frame.setVisible(true);
		ImageIO.write(image, "png", new File("src/jpatch/images/title.png"));
	}
	
	public static BufferedImage createSpashImage() {
		BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2.setPaint(new GradientPaint(0, 0, new Color(0x6688ff), 50, 299, new Color(0x0000cc)));
		g2.setPaint(new GradientPaint(0, 0, new Color(0xaaccbb), 50, 299, new Color(0x668877)));
		g2.fillRect(0, 0, 400, 300);
		
		g2.setPaint(new GradientPaint(0, 50, new Color(0x11cccccc, true), 399, 0, new Color(0x55cccccc, true)));
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		for (int i = 0; i < 800; i+=16)
			g2.drawLine(i - 400, 300, i - 100, 0);
		
		GeneralPath path;
		path = new GeneralPath();
		path.moveTo(0, 200);
		path.curveTo(150, 220, 250, 120, 399, 100);
		path.lineTo(399, 299);
		path.lineTo(0, 299);
		path.closePath();
		g2.setPaint(new GradientPaint(0, 0, new Color(0x40ffffff, true), 0, 299, new Color(0x00ffffff, true)));
		g2.fill(path);
		
		path = new GeneralPath();
		path.moveTo(100, 299);
		path.curveTo(150, 100, 250, 100, 300, 0);
		path.lineTo(399, 0);
		path.lineTo(399, 299);
		path.closePath();
		g2.setPaint(new GradientPaint(0, 0, new Color(0x00000000, true), 399, 0, new Color(0x20000000, true)));
		g2.fill(path);
		
		g2.setPaint(new GradientPaint(0, 0, new Color(0x80ffffff, true), 100, 100, new Color(0x00ffffff, true)));
		g2.fillRoundRect(8, 8, 384, 284, 64, 64);
		
//		g2.setPaint(new GradientPaint(399, 299, new Color(0x40ffffff, true), 300, 200, new Color(0x00ffffff, true)));
//		g2.fillRoundRect(10, 10, 380, 280, 100, 100);
		
		/*
		 * JPatch
		 */
		FontRenderContext frc = g2.getFontRenderContext();
		Font font;
		GlyphVector gv;
		Shape shape;
		font = new Font("MgOpen Modata", Font.BOLD, 110);
		
		gv = font.createGlyphVector(frc, "JPatch");
		shape = gv.getOutline();
		Rectangle bounds = shape.getBounds();
		
//		for (Font f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
//			System.out.println(f.getFontName());
		
		AffineTransform at;
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2 + 2, bounds.height + 10 + 4);
		g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setColor(new Color(0x33000000, true));
		shape = at.createTransformedShape(gv.getOutline());
		g2.draw(shape);
		g2.fill(shape);
		
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2 + 1, bounds.height + 10 + 2);
		g2.setColor(new Color(0x33000000, true));
		shape = at.createTransformedShape(gv.getOutline());
		g2.draw(shape);
		g2.fill(shape);
		
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2, bounds.height + 10);
		g2.setPaint(new GradientPaint(0, 45, new Color(0xffffff), 0, 100, new Color(0xcccce4), true));
		shape = at.createTransformedShape(gv.getOutline());
		g2.fill(shape);
		
//		g2.setPaint(new GradientPaint(0, 50, new Color(0x44ffffff, true), 1, 55, new Color(0x00ffffff, true), true));
//		g2.fill(shape);
		
		g2.setPaint(new GradientPaint(1, 50, new Color(0x04000000, true), 0.95f, 51, new Color(0x00000000, true), true));
		g2.fill(shape);
		g2.setPaint(new GradientPaint(1, 50, new Color(0x04000000, true), 0.9f, 50.6f, new Color(0x00000000, true), true));
		g2.fill(shape);
		g2.setPaint(new GradientPaint(1, 50, new Color(0x04000000, true), 1.1f, 51.1f, new Color(0x00000000, true), true));
		g2.fill(shape);
		g2.setPaint(new GradientPaint(1, 50, new Color(0x04000000, true), 1.12f, 51.3f, new Color(0x00000000, true), true));
		g2.fill(shape);
		g2.setPaint(new GradientPaint(1, 50, new Color(0x04000000, true), 1.05f, 52.2f, new Color(0x00000000, true), true));
		g2.fill(shape);
		g2.setPaint(new GradientPaint(1, 50, new Color(0x04000000, true), 1.03f, 53.5f, new Color(0x00000000, true), true));
		g2.fill(shape);
		
		g2.setPaint(new GradientPaint(0.6f, 50, new Color(0x08ffffff, true), 0.7f, 52, new Color(0x00ffffff, true), true));
		g2.fill(shape);
		g2.setPaint(new GradientPaint(0.6f, 50, new Color(0x08ffffff, true), 0.8f, 51.4f, new Color(0x00ffffff, true), true));
		g2.fill(shape);
		g2.setPaint(new GradientPaint(0.6f, 50, new Color(0x08ffffff, true), 0.4f, 52.1f, new Color(0x00ffffff, true), true));
		g2.fill(shape);
		g2.setPaint(new GradientPaint(0.6f, 50, new Color(0x08ffffff, true), 0.5f, 52.3f, new Color(0x00ffffff, true), true));
		g2.fill(shape);
		g2.setPaint(new GradientPaint(0.6f, 50, new Color(0x08ffffff, true), 0.45f, 53.7f, new Color(0x00ffffff, true), true));
		g2.fill(shape);
		g2.setPaint(new GradientPaint(0.6f, 50, new Color(0x08ffffff, true), 0.55f, 54.6f, new Color(0x00ffffff, true), true));
		g2.fill(shape);
		
		
		g2.setPaint(new GradientPaint(0, 20, new Color(0xffffff), 0, 80, new Color(0xbbbbc0)));
		g2.draw(shape);
		
		
		
		
		
		/*
		 * Version
		 */
		font = new Font("MgOpen Modata", Font.BOLD, 30);
		gv = font.createGlyphVector(frc, "Version " + VersionInfo.ver);
		shape = gv.getOutline();
		bounds = shape.getBounds();
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2, bounds.height + 105);
		g2.setColor(new Color(0xffff00));
		shape = at.createTransformedShape(gv.getOutline());
		g2.fill(shape);
		
		/*
		 * Written by Sascha Ledinsky
		 */
		font = new Font("Sans Serif", Font.PLAIN, 14);
		gv = font.createGlyphVector(frc, "written by Sascha Ledinsky");
		shape = gv.getOutline();
		bounds = shape.getBounds();
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2, bounds.height + 135);
		g2.setColor(new Color(0xffffff));
		shape = at.createTransformedShape(gv.getOutline());
		g2.fill(shape);
		
		/*
		 * Copyright
		 */
		font = new Font("Sans Serif", Font.PLAIN, 12);
		gv = font.createGlyphVector(frc, "© Copyright 2002-2006");
		shape = gv.getOutline();
		bounds = shape.getBounds();
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2, bounds.height + 155);
		g2.setColor(new Color(0xffffff));
		shape = at.createTransformedShape(gv.getOutline());
		g2.fill(shape);
		
		
		/*
		 * url
		 */
		font = new Font("Sans Serif", Font.BOLD, 12);
		gv = font.createGlyphVector(frc, "http://www.jpatch.com");
		shape = gv.getOutline();
		bounds = shape.getBounds();
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2, bounds.height + 175);
		g2.setColor(new Color(0xcccccc));
		shape = at.createTransformedShape(gv.getOutline());
		g2.fill(shape);
		
		int y = -20;
		g2.setColor(new Color(0xcccccc));
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		g2.setFont(new Font("Sans Serif", Font.PLAIN, 10));
		drawCenteredString(g2, "This program is free software. You can redistribute it and/or modify it under the", 230 + y);
		drawCenteredString(g2, "terms of the GNU General Public License as published by the Free Software", 240 + y);
		drawCenteredString(g2, "Foundation; either version 2 of the License, or (at your option) any later version.", 250 + y);
		drawCenteredString(g2, "This program is distributed in the hope that it will be useful, but WITHOUT ANY", 264 + y);
		drawCenteredString(g2, "WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS", 274 + y);
		drawCenteredString(g2, "FOR A PARTICULAR PURPOSE. See the GNU General Public License for more", 284 + y);
		drawCenteredString(g2, "details.", 294 + y);
//		/*
//		 * Copyright
//		 */
//		font = new Font("Sans Serif", Font.PLAIN, 10);
//		gv = font.createGlyphVector(frc, "This program is free software. You can redistribute it and/or modify it under the");
//		shape = gv.getOutline();
//		bounds = shape.getBounds();
//		at = new AffineTransform(1, 0, 0, 1, 5, bounds.height + 200);
//		g2.setColor(new Color(0xffffff));
//		shape = at.createTransformedShape(gv.getOutline());
//		g2.fill(shape);
//		/*
//		 * Copyright
//		 */
//		font = new Font("Sans Serif", Font.PLAIN, 10);
//		gv = font.createGlyphVector(frc, "terms of the GNU General Public License as published by the Free Software");
//		shape = gv.getOutline();
//		bounds = shape.getBounds();
//		at = new AffineTransform(1, 0, 0, 1, 5, bounds.height + 212);
//		g2.setColor(new Color(0xffffff));
//		shape = at.createTransformedShape(gv.getOutline());
//		g2.fill(shape);
//		/*
//		 * Copyright
//		 */
//		font = new Font("Sans Serif", Font.PLAIN, 10);
//		gv = font.createGlyphVector(frc, "Foundation; either version 2 of the License, or (at your option) any later version.");
//		shape = gv.getOutline();
//		bounds = shape.getBounds();
//		at = new AffineTransform(1, 0, 0, 1, 5, bounds.height + 224);
//		g2.setColor(new Color(0xffffff));
//		shape = at.createTransformedShape(gv.getOutline());
//		g2.fill(shape);
//		/*
//		 * Copyright
//		 */
//		font = new Font("Sans Serif", Font.PLAIN, 10);
//		gv = font.createGlyphVector(frc, "This program is distributed in the hope that it will be useful, but WITHOUT ANY");
//		shape = gv.getOutline();
//		bounds = shape.getBounds();
//		at = new AffineTransform(1, 0, 0, 1, 5, bounds.height + 236);
//		g2.setColor(new Color(0xffffff));
//		shape = at.createTransformedShape(gv.getOutline());
//		g2.fill(shape);
//		/*
//		 * Copyright
//		 */
//		font = new Font("Sans Serif", Font.PLAIN, 10);
//		gv = font.createGlyphVector(frc, "WARRANTY; without even the implied warranty of");
//		shape = gv.getOutline();
//		bounds = shape.getBounds();
//		at = new AffineTransform(1, 0, 0, 1, 5, bounds.height + 248);
//		g2.setColor(new Color(0xffffff));
//		shape = at.createTransformedShape(gv.getOutline());
//		g2.fill(shape);
		
		
		
		if (!VersionInfo.release) {
			/*
			 * Development Version
			 */
			font = new Font("FreeMono", Font.BOLD, 60);
			gv = font.createGlyphVector(frc, "DEVELOPMENT");
			shape = gv.getOutline();
			bounds = shape.getBounds();
			double s = Math.sin(33.0 / 180.0 * Math.PI);
			double c = Math.cos(33.0 / 180.0 * Math.PI);
			System.out.println(s + " " + c);
			at = new AffineTransform(c, -s, s, c, (400 - bounds.width) / 2 + 40 + 1, bounds.height + 230 + 1);
			g2.setColor(new Color(0x66000000, true));
			g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			shape = at.createTransformedShape(gv.getOutline());
			g2.draw(shape);
			gv = font.createGlyphVector(frc, "VERSION");
			shape = gv.getOutline();
			bounds = shape.getBounds();
			at = new AffineTransform(c, -s, s, c, (400 - bounds.width) / 2 + 40 + 10 * c + 1, bounds.height + 230 + 10 * s + 1);
			shape = at.createTransformedShape(gv.getOutline());
			g2.draw(shape);
			
			gv = font.createGlyphVector(frc, "DEVELOPMENT");
			shape = gv.getOutline();
			bounds = shape.getBounds();
			System.out.println(s + " " + c);
			at = new AffineTransform(c, -s, s, c, (400 - bounds.width) / 2 + 40 - 1, bounds.height + 230 - 1);
			g2.setColor(new Color(0x66ffffff, true));
			g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			shape = at.createTransformedShape(gv.getOutline());
			g2.draw(shape);
			gv = font.createGlyphVector(frc, "VERSION");
			shape = gv.getOutline();
			bounds = shape.getBounds();
			at = new AffineTransform(c, -s, s, c, (400 - bounds.width) / 2 + 40 + 10 * c - 1, bounds.height + 230 + 10 * s - 1);
			shape = at.createTransformedShape(gv.getOutline());
			g2.draw(shape);
			
			gv = font.createGlyphVector(frc, "DEVELOPMENT");
			shape = gv.getOutline();
			bounds = shape.getBounds();
			System.out.println(s + " " + c);
			at = new AffineTransform(c, -s, s, c, (400 - bounds.width) / 2 + 40, bounds.height + 230);
			g2.setColor(new Color(0x66ff0000, true));
			g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			shape = at.createTransformedShape(gv.getOutline());
			g2.draw(shape);
			gv = font.createGlyphVector(frc, "VERSION");
			shape = gv.getOutline();
			bounds = shape.getBounds();
			at = new AffineTransform(c, -s, s, c, (400 - bounds.width) / 2 + 40 + 10 * c, bounds.height + 230 + 10 * s);
			shape = at.createTransformedShape(gv.getOutline());
			g2.draw(shape);
		}
		
		g2.setPaint(new GradientPaint(0, 0, new Color(0x11000000, true), 399, 0, new Color(0x33000000, true)));
		g2.fillRect(0, 280, 400, 20);
		
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setPaint(new GradientPaint(0, 0, new Color(0xcceedd), 0, 299, new Color(0xaaccbb)));
		g2.drawLine(0, 0, 399, 0);
		g2.drawLine(0, 0, 0, 299);
		g2.setPaint(new GradientPaint(0, 0, new Color(0xddffee), 0, 299, new Color(0xbbeecc)));
		g2.drawLine(1, 1, 398, 1);
		g2.drawLine(1, 1, 1, 298);
		g2.setPaint(new GradientPaint(0, 0, new Color(0x446655), 0, 299, new Color(0x224433)));
		g2.drawLine(0, 299, 399, 299);
		g2.drawLine(399, 0, 399, 299);
		g2.setPaint(new GradientPaint(0, 0, new Color(0x557766), 0, 299, new Color(0x335544)));
		g2.drawLine(1, 298, 398, 298);
		g2.drawLine(398, 1, 398, 298);
		return image;
	}
	
	private static void drawCenteredString(Graphics2D g2, String text, int y) {
		int w = g2.getFontMetrics().charsWidth(text.toCharArray(), 0, text.length()) >> 1;
		g2.drawString(text, 200 - w, y);
	}
}
