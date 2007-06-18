/*
 * $Id:$
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
package com.jpatch.afw.ui.laf;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;



public class SliderUI extends MetalSliderUI {

	public static ComponentUI createUI(JComponent jcomponent) {
		return new SliderUI();
	}


	@Override
	public void paint(Graphics g, JComponent c) {
		// TODO Auto-generated method stub
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g, c);
	}


	@Override
	public void paintThumb(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int cx = thumbRect.x + thumbRect.width / 2;
		int cy = thumbRect.y + thumbRect.height / 2;
		g2.setColor(new Color(0x666666));
		g2.fillOval(cx - 5, cy - 5, 10, 10);
		g2.setPaint(new GradientPaint(0, cy - 3, new Color(0xffffff), 0, cy + 3, new Color(0x888888)));
		g2.fillOval(cx - 4, cy - 4, 8, 8);
	}

	@Override
	public void paintTrack(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		int c = trackRect.height / 2;
		g2.setPaint(new GradientPaint(0, trackRect.y + c - 2, new Color(0x70000000, true), 0, trackRect.y + c + 2, new Color(0x08000000, true)));
		g2.fillRoundRect(trackRect.x, trackRect.y + c - 2, trackRect.width, 4, 4, 4);
	}
}