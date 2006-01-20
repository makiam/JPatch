/*
 * $Id: Corner.java,v 1.2 2006/01/20 20:28:23 sascha_l Exp $
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
package jpatch.boundary.timeline;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;


/**
 * @author sascha
 *
 */
public class Corner extends JComponent {

	private JButton expandAllButton;
	private JButton collapseAllButton;
	private JButton centerButton;
	private static final Icon[] iconDownArrow = new Icon[] {createIcon(0, Color.BLACK), createIcon(0, Color.WHITE), createIcon(0, UIManager.getColor("Button.focus")) };
	private static final Icon[] iconUpArrow = new Icon[] {createIcon(1, Color.BLACK), createIcon(1, Color.WHITE), createIcon(1, UIManager.getColor("Button.focus")) };
	private static final Icon[] iconCenter = new Icon[] {createIcon(2, Color.BLACK), createIcon(2, Color.WHITE), createIcon(2, UIManager.getColor("Button.focus")) };
	
	
	public Corner(final TimelineEditor timelineEditor) {
		expandAllButton = new JButton(iconDownArrow[0]);
		expandAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timelineEditor.expandAll(true);
			}
		});
		expandAllButton.setPressedIcon(iconDownArrow[1]);
		expandAllButton.setRolloverIcon(iconDownArrow[2]);
		expandAllButton.setBorderPainted(false);
		expandAllButton.setContentAreaFilled(false);
		expandAllButton.setFocusable(false);
		expandAllButton.setOpaque(false);
		expandAllButton.setToolTipText("expand all tracks");
		collapseAllButton = new JButton(iconUpArrow[0]);
		collapseAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timelineEditor.expandAll(false);
			}
		});
		collapseAllButton.setPressedIcon(iconUpArrow[1]);
		collapseAllButton.setRolloverIcon(iconUpArrow[2]);
		collapseAllButton.setBorderPainted(false);
		collapseAllButton.setContentAreaFilled(false);
		collapseAllButton.setFocusable(false);
		collapseAllButton.setOpaque(false);
		collapseAllButton.setToolTipText("collapse all tracks");
		centerButton = new JButton(iconCenter[0]);
		centerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timelineEditor.resetTracks();
			}
		});
		centerButton.setPressedIcon(iconCenter[1]);
		centerButton.setRolloverIcon(iconCenter[2]);
		centerButton.setBorderPainted(false);
		centerButton.setContentAreaFilled(false);
		centerButton.setFocusable(false);
		centerButton.setOpaque(false);
		centerButton.setToolTipText("reset all expanded tracks to their default height");
		add(collapseAllButton);
		add(expandAllButton);
		add(centerButton);
		setLayout(null);
	}
	
	public void doLayout() {
		super.doLayout();
		layoutButtons();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.BLACK);
		g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
		g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
		g.drawLine(getWidth() - 2, 0, getWidth() - 2, getHeight() - 2);
		g.setColor(UIManager.getColor("ScrollBar.shadow"));
		g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 2);
	}
	
	private void layoutButtons() {
		expandAllButton.setBounds(3, 2, 10, 12);
		collapseAllButton.setBounds(3 + 16, 2, 10, 12);
		centerButton.setBounds(3 + 32, 2, 10, 12);
	}
	
	private static Icon createIcon(final int type, final Color color) {
		return new Icon() {
			public void paintIcon(Component c, Graphics g, int x, int y) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				g.setColor(color);
				switch (type) {
				case 0:
					g.fillPolygon(new int[] { 0, 9, 4 }, new int[] { 0, 0, 5}, 3);
					g.fillPolygon(new int[] { 0, 9, 4 }, new int[] { 6, 6, 11}, 3);
					break;
				case 1:
					g.fillPolygon(new int[] { -1, 9, 4 }, new int[] { 5, 5, -1}, 3);
					g.fillPolygon(new int[] { -1, 9, 4 }, new int[] { 11, 11, 5}, 3);
					break;
				case 2:
					g.fillPolygon(new int[] { 0, 9, 4 }, new int[] { 0, 0, 5}, 3);
					g.fillPolygon(new int[] { -1, 9, 4 }, new int[] { 11, 11, 5}, 3);
					g.drawLine(0, 5, 9, 5);
					break;
				}
				
			}

			public int getIconWidth() {
				return 6;
			}

			public int getIconHeight() {
				return 6;
			}
		};
	}
}
