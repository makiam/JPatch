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

public class CheckBoxUI extends MetalCheckBoxUI {
	private static final Icon UNCHECKED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/CHECKBOX_UNCHECKED.png"));
	private static final Icon CHECKED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/CHECKBOX_CHECKED.png"));
	private static final Icon UNCHECKED_PRESSED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/CHECKBOX_UNCHECKED_PRESSED.png"));
	private static final Icon CHECKED_PRESSED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/CHECKBOX_CHECKED_PRESSED.png"));
	
	public static ComponentUI createUI(JComponent jcomponent) {
		return new CheckBoxUI();
	}


	@Override
	public void paint(Graphics g, JComponent c) {
		// TODO Auto-generated method stub
//		Graphics2D g2 = (Graphics2D) g;
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		super.paint(g, c);
//		g.drawRect(0, 0, 10, 10);
		if (((AbstractButton) c).isSelected()) {
			if (((AbstractButton) c).getModel().isPressed()) {
				CHECKED_PRESSED_ICON.paintIcon(c, g, 0, 1);
			} else {
				CHECKED_ICON.paintIcon(c, g, 0, 1);
			}
		} else {
			if (((AbstractButton) c).getModel().isPressed()) {
				UNCHECKED_PRESSED_ICON.paintIcon(c, g, 0, 1);
			} else {
				UNCHECKED_ICON.paintIcon(c, g, 0, 1);
			}
		}
	}
}