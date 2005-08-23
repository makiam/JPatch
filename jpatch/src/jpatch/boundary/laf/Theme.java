/*
 * $Id: Theme.java,v 1.3 2005/08/23 22:14:42 sascha_l Exp $
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
	public static final Color rolloverBorderColor = Color.ORANGE;
	
	public static void paintButtonBackground(AbstractButton button, Graphics2D g2) {
		ButtonModel model = button.getModel();
		float width = button.getWidth();
		float height = button.getHeight();
		
		if (model.isRollover()) {
			if (model.isPressed()) {
				g2.setPaint(new GradientPaint(0, 0, button.getBackground(), 0, height, Color.WHITE));
				g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
			} else {
				g2.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, height, button.getBackground()));
				g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
			}
		} else if (model.isSelected()) {
			g2.setPaint(new GradientPaint(0, 0, new Color(0,0,0,0.3f), 0, height * 0.75f, new Color(0,0,0,0.0f)));
			g2.setClip(0, 0, (int) width, (int) (height * 0.75f));
			g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
			g2.setPaint(new GradientPaint(0, height * 0.75f - 1, new Color(1,1,1,0.0f), 0, height - 2, new Color(1,1,1,1f)));
			g2.setClip(0, (int) (height * 0.75f), (int) width, (int) (height * 0.25f));
			g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
			g2.setClip(0, 0, (int) width, (int) height);
		} else if (!(button.getParent() instanceof JToolBar)) {
			g2.setPaint(new GradientPaint(0, 0, new Color(1,1,1,1f), 0, height/2, new Color(1,1,1,0.0f)));
			g2.setClip(0, 0, (int) width, (int) (height /2));
			g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
			g2.setPaint(new GradientPaint(0, height/2 - 1, new Color(0,0,0,0.0f), 0, height - 2, new Color(0,0,0,0.1f)));
			g2.setClip(0, (int) (height /2), (int) width, (int) (height /2));
			g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 9, 9));
			g2.setClip(0, 0, (int) width, (int) height);
		}
	}
}
