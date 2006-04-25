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

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import javax.swing.*;

import jpatch.VersionInfo;

/**
 * @author sascha
 *
 */
public class LogoMaker {
	public static void main(String[] args) {
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
	}
	
	public static BufferedImage createSpashImage() {
		BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(new GradientPaint(0, 0, new Color(0x6688ff), 50, 299, new Color(0x0000cc)));
		g2.fillRect(0, 0, 400, 300);
		
		g2.setPaint(new GradientPaint(0, 50, new Color(0x11cccccc, true), 399, 0, new Color(0x88cccccc, true)));
		g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		for (int i = 0; i < 800; i+=16)
			g2.drawLine(i - 400, 300, i - 100, 0);
		
		
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
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2 + 2, bounds.height + 20 + 4);
		g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setColor(new Color(0x33000000, true));
		shape = at.createTransformedShape(gv.getOutline());
		g2.draw(shape);
		g2.fill(shape);
		
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2 + 1, bounds.height + 20 + 2);
		g2.setColor(new Color(0x33000000, true));
		shape = at.createTransformedShape(gv.getOutline());
		g2.draw(shape);
		g2.fill(shape);
		
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2, bounds.height + 20);
		g2.setPaint(new GradientPaint(0, 50, new Color(0xffffff), 0, 100, new Color(0xcccccc), true));
		shape = at.createTransformedShape(gv.getOutline());
		g2.fill(shape);
		g2.setPaint(new GradientPaint(0, 20, new Color(0xffffff), 0, 80, new Color(0xccccff)));
		g2.draw(shape);
		
		
		
		
		
		/*
		 * Version
		 */
		font = new Font("MgOpen Modata", Font.BOLD, 30);
		gv = font.createGlyphVector(frc, "Version " + VersionInfo.ver);
		shape = gv.getOutline();
		bounds = shape.getBounds();
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2, bounds.height + 120);
		g2.setColor(new Color(0xffffff));
		shape = at.createTransformedShape(gv.getOutline());
		g2.fill(shape);
		
		/*
		 * Written by Sascha Ledinsky
		 */
		font = new Font("Sans Serif", Font.PLAIN, 16);
		gv = font.createGlyphVector(frc, "written by Sascha Ledinsky");
		shape = gv.getOutline();
		bounds = shape.getBounds();
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2, bounds.height + 155);
		g2.setColor(new Color(0xffffff));
		shape = at.createTransformedShape(gv.getOutline());
		g2.fill(shape);
		
		/*
		 * Copyright
		 */
		font = new Font("Sans Serif", Font.PLAIN, 16);
		gv = font.createGlyphVector(frc, "© Copyright 2002-2006");
		shape = gv.getOutline();
		bounds = shape.getBounds();
		at = new AffineTransform(1, 0, 0, 1, (400 - bounds.width) / 2, bounds.height + 175);
		g2.setColor(new Color(0xffffff));
		shape = at.createTransformedShape(gv.getOutline());
		g2.fill(shape);
		
		g2.setColor(new Color(0xcccccc));
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		g2.setFont(new Font("Sans Serif", Font.PLAIN, 10));
		g2.drawString("This program is free software. You can redistribute it and/or modify it under the", 5, 230);
		g2.drawString("terms of the GNU General Public License as published by the Free Software", 20, 240);
		g2.drawString("Foundation; either version 2 of the License, or (at your option) any later version.", 9, 250);
		g2.drawString("This program is distributed in the hope that it will be useful, but WITHOUT ANY", 8, 264);
		g2.drawString("WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS", 7, 274);
		g2.drawString("FOR A PARTICULAR PURPOSE. See the GNU General Public License for more", 20, 284);
		g2.drawString("details.", 180, 294);
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
		at = new AffineTransform(c, -s, s, c, (400 - bounds.width) / 2 + 40 + 1, bounds.height + 230 + 2);
		g2.setColor(new Color(0x33000000, true));
		g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		shape = at.createTransformedShape(gv.getOutline());
		g2.draw(shape);
		gv = font.createGlyphVector(frc, "VERSION");
		shape = gv.getOutline();
		bounds = shape.getBounds();
		at = new AffineTransform(c, -s, s, c, (400 - bounds.width) / 2 + 40 + 10 * c + 1, bounds.height + 230 + 10 * s + 2);
		shape = at.createTransformedShape(gv.getOutline());
		g2.draw(shape);
		
		gv = font.createGlyphVector(frc, "DEVELOPMENT");
		shape = gv.getOutline();
		bounds = shape.getBounds();
		System.out.println(s + " " + c);
		at = new AffineTransform(c, -s, s, c, (400 - bounds.width) / 2 + 40, bounds.height + 230);
		g2.setColor(new Color(0xbbffff00, true));
		g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		shape = at.createTransformedShape(gv.getOutline());
		g2.draw(shape);
		gv = font.createGlyphVector(frc, "VERSION");
		shape = gv.getOutline();
		bounds = shape.getBounds();
		at = new AffineTransform(c, -s, s, c, (400 - bounds.width) / 2 + 40 + 10 * c, bounds.height + 230 + 10 * s);
		shape = at.createTransformedShape(gv.getOutline());
		g2.draw(shape);
		
		
		
		g2.setPaint(new GradientPaint(0, 0, new Color(0xbbbbbb), 0, 299, new Color(0x999999)));
		g2.drawLine(0, 0, 399, 0);
		g2.drawLine(0, 0, 0, 299);
		g2.setPaint(new GradientPaint(0, 0, new Color(0xcccccc), 0, 299, new Color(0xaaaaaa)));
		g2.drawLine(1, 1, 398, 1);
		g2.drawLine(1, 1, 1, 298);
		g2.setPaint(new GradientPaint(0, 0, new Color(0x999999), 0, 299, new Color(0x777777)));
		g2.drawLine(0, 299, 399, 299);
		g2.drawLine(399, 0, 399, 299);
		g2.setPaint(new GradientPaint(0, 0, new Color(0xaaaaaa), 0, 299, new Color(0x888888)));
		g2.drawLine(1, 298, 398, 298);
		g2.drawLine(398, 1, 398, 298);
		return image;
	}
}
