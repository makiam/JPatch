/*
 * $Id: Theme.java,v 1.9 2006/05/22 10:46:19 sascha_l Exp $
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
package jpatch.boundary.laf;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import javax.swing.*;

/**
 * @author sascha
 *
 */
public class Theme {
	public static final Color rolloverBorderColor = new Color(0.6f, 0.6f, 1.0f);
	
	public static void paintButtonBackground(AbstractButton button, Graphics2D g2) {
		
		float buttonRed = (float) button.getBackground().getRed() / 255 * 0.9f;
		float buttonGreen = (float) button.getBackground().getRed() / 255 * 0.9f;
		float buttonBlue = (float) button.getBackground().getRed() / 255 * 1.0f;
		Color buttonBackground = new Color(buttonRed, buttonGreen, buttonBlue);
		
		ButtonModel model = button.getModel();
		float width = button.getWidth();
		float height = button.getHeight();
		
		if (model.isRollover()) {
			if (model.isPressed()) {
				g2.setPaint(new GradientPaint(0, 0, buttonBackground, 0, height, Color.WHITE));
				g2.fill(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, 9, 9));
			} else {
				g2.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, height, buttonBackground));
				g2.fill(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, 9, 9));
			}
		} else if (model.isSelected()) {
			g2.setPaint(new GradientPaint(0, 0, new Color(0,0,0.2f,0.3f), 0, height * 0.75f, new Color(0,0,0.2f,0.0f)));
			g2.setClip(0, 0, (int) width, (int) (height * 0.75f));
			g2.fill(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, 9, 9));
			g2.setPaint(new GradientPaint(0, height * 0.75f - 1, new Color(1,1,1,0.0f), 0, height - 2, new Color(1,1,1,1f)));
			g2.setClip(0, (int) (height * 0.75f), (int) width, (int) (height * 0.25f));
			g2.fill(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, 9, 9));
			g2.setClip(0, 0, (int) width, (int) height);
		} else if (!(button.getParent() instanceof JToolBar)) {
			g2.setPaint(new GradientPaint(0, 0, new Color(1,1,1,1f), 0, height/2, new Color(1,1,1,0.0f)));
			g2.setClip(0, 0, (int) width, (int) (height /2));
			g2.fill(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, 9, 9));
			g2.setPaint(new GradientPaint(0, height/2 - 1, new Color(0,0,0.3f,0.0f), 0, height - 2, new Color(0,0,0.3f,0.1f)));
			g2.setClip(0, (int) (height /2), (int) width, (int) (height /2));
			g2.fill(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, 9, 9));
			g2.setClip(0, 0, (int) width, (int) height);
		}
	}
	
//	public static void paintButtonBackground(AbstractButton button, Graphics2D g2) {
//		
//		float buttonRed = (float) button.getBackground().getRed() / 255 * 0.9f;
//		float buttonGreen = (float) button.getBackground().getRed() / 255 * 0.9f;
//		float buttonBlue = (float) button.getBackground().getRed() / 255 * 1.0f;
//		Color buttonBackground = new Color(buttonRed, buttonGreen, buttonBlue);
//		
//		ButtonModel model = button.getModel();
//		int width = button.getWidth();
//		int height = button.getHeight();
//		
//		Color c1 = new Color(1,1,1,0.5f);
//		Color c2 = new Color(1,1,1,1.0f);
//		Color c3 = new Color(0,0,0.0f,0.1f);
//		Color c4 = new Color(0,0,0.0f,0.0f);
//		
////		paint(g2, width, height, height / 2, c1, c2, c3, c4);
//		
//		if (button.getParent() instanceof JToolBar) {
//			if (model.isPressed())
//				paintSplit(g2, width, height, height / 2, c1, c2, c2, c1);
//			else if (model.isSelected())
//				paintSplit(g2, width, height, height / 2, c3, c4, c4, c3);
//			else if (model.isRollover())
//				paintFull(g2, width, height, c2, c1);
//		} else {
//			if (model.isPressed())
//				paintSplit(g2, width, height, height / 2, c1, c2, c2, c1);
//			else if (model.isSelected())
//				paintSplit(g2, width, height, height / 2, c3, c4, c4, c3);
//			else
//				paintSplit(g2, width, height, height / 2, c1, c2, c3, c4);
//		}
//		if (model.isRollover()) {
//			if (model.isPressed()) {
//				paintFull(g2, width, height, c1, c2);
//			} else {
//				if (model.isSelected()) {
//					paintSplit(g2, width, height, height / 2, c3, c4, c1, c2);
//				} else {
//					paintFull(g2, width, height, c2, c1);
//				}
//			}
//		} else if (model.isSelected()) {
//			paintSplit(g2, width, height, height / 2, c3, c4, c1, c2);
//		} else if (!(button.getParent() instanceof JToolBar)) {
//			paintSplit(g2, width, height, height / 2, c1, c2, c3, c4);
//		}
	
//		if (model.isRollover()) {
//			if (model.isPressed()) {
//				g2.setPaint(new GradientPaint(0, 0, buttonBackground, 0, height, Color.WHITE));
//				g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//			} else {
//				g2.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, height, buttonBackground));
//				g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//			}
//		} else if (model.isSelected()) {
//			g2.setPaint(new GradientPaint(0, 0, new Color(0,0,0.2f,0.3f), 0, height / 2, new Color(0,0,0.2f,0.0f)));
//			g2.setClip(0, 0, (int) width, (int) (height / 2));
//			g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//			g2.setPaint(new GradientPaint(0, height / 2 - 1, new Color(1,1,1,1f), 0, height - 2, new Color(1,1,1,0.0f)));
//			g2.setClip(0, (int) (height / 2), (int) width, (int) (height / 2));
//			g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//			g2.setClip(0, 0, (int) width, (int) height);
//		} else if (!(button.getParent() instanceof JToolBar)) {
//			g2.setPaint(new GradientPaint(0, 0, new Color(1,1,1,1f), 0, height/2, new Color(1,1,1,0.0f)));
//			g2.setClip(0, 0, (int) width, (int) (height /2));
//			g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//			g2.setPaint(new GradientPaint(0, height/2 - 1, new Color(0,0,0.3f,0.1f), 0, height - 2, new Color(0,0,0.3f,0.0f)));
//			g2.setClip(0, (int) (height /2), (int) width, (int) (height /2));
//			g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//			g2.setClip(0, 0, (int) width, (int) height);
//			g2.setPaint(new GradientPaint(0, 0, new Color(0x00, 0x00, 0x00), 0, height / 2, new Color(0xff, 0xff, 0xff)));
//			g2.setClip(0, 0, (int) width, (int) (height /2));
//			g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//			g2.setPaint(new GradientPaint(0, height / 2, new Color(0x00, 0x00, 0x00), 0, height, new Color(0xff, 0xff, 0xff)));
//			g2.setClip(0, (int) (height * 0.75f), (int) width, (int) (height * 0.25f));
//			g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//		}
//	}
//	
//	static void paintSplit(Graphics2D g2, int width, int height, int split, Color c1, Color c2, Color c3, Color c4) {
//		g2.setPaint(new GradientPaint(0, 0, c1, 0, split, c2));
//		g2.setClip(0, 0, width, split);
//		g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//		g2.setPaint(new GradientPaint(0, split - 1, c3, 0, height - 2, c4));
//		g2.setClip(0, split, width, height - split);
//		g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//		g2.setClip(0, 0, (int) width, (int) height);
//	}
//	
//	static void paintFull(Graphics2D g2, int width, int height, Color c1, Color c2) {
//		g2.setPaint(new GradientPaint(0, 0, c1, 0, height - 2, c2));
//		g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
//	}
}
