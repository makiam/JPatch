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
package com.jpatch.afw.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

class Switcher extends JComponent {
	private static final Icon ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/SWITCH_LEFT.png"));
	private static final Icon SELECTED_ICON = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/SWITCH_RIGHT.png"));
	
	private final JLabel offLabel;
	private final JLabel onLabel;
	private final JToggleButton button;
	private final Dimension preferredSize;
		
	public Switcher(String offText, String onText) {
		offLabel = new JLabel(offText);
		onLabel = new JLabel(onText);
		
		button = new JToggleButton();
		button.setBorderPainted(false);
		button.setBorder(null);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setIcon(ICON);
		button.setSelectedIcon(SELECTED_ICON);
		
		setLayout(null);
		add(offLabel);
		add(button);
		add(onLabel);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				button.setSelected(e.getX() > getWidth() / 2);
			}
		});
		
		int w = Math.max(onLabel.getPreferredSize().width, offLabel.getPreferredSize().width);
		int h = onLabel.getPreferredSize().height;
		preferredSize = new Dimension(2 * w + 40, h);
	}
	
	public AbstractButton asAbstractButton() {
		return button;
	}
	
	@Override
	public void doLayout() {
		int width = getWidth();
		int offWidth = offLabel.getPreferredSize().width;
		int onWidth = onLabel.getPreferredSize().width;
		int height = onLabel.getPreferredSize().height;
		offLabel.setBounds(width / 2 - 20 - offWidth, 0, offWidth, height);
		onLabel.setBounds(width / 2 + 20, 0, onWidth, height);
		button.setBounds(width / 2 - 16, 0, 33, 16);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return preferredSize;
	}
}