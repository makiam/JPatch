/*
 * $Id: Header.java 283 2006-06-10 18:50:21 +0000 (Sat, 10 Jun 2006) sascha_l $
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

import com.sun.org.apache.bcel.internal.util.ClassLoader;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

import java.util.*;
import java.util.List;
import javax.swing.*;

import jpatch.entity.*;

public class Header2 extends Header implements MouseListener, MouseMotionListener {
	
	private ImageIcon showIcon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/eye.png"));
	private ImageIcon dontshowIcon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/eye_invisible.png"));
	private ImageIcon scaleIcon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/trackscale.png"));
	
	private Color[] colors;
	private AbstractButton[][] buttons = new AbstractButton[3][0];
	
	public Header2(TimelineEditor tle) {
		super(tle, false);
		addMouseListener(this);
		addMouseMotionListener(this);
		setLayout(null);
		createButtons();
	}
	
	
	public void createButtons() {
		/* create curve colors */
		colors = new Color[timelineEditor.getTracks().size()];
		int[] c = new int[] { 0xff, 0xff, 0x00, 0x00, 0x00, 0xff };
		for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			int j = i;
			int r = c[j % 6];
			int g = c[(j + 2) % 6];
			int b = c[(j + 4) % 6];
			if ((j / 6) % 3 == 1) {
				r = (r * 2) / 3;
				g = (g * 2) / 3;
				b = (b * 2) / 3;
			} else if ((j / 6) % 3 == 2) {
				r = 0xc0 + (r >> 2);
				g = 0xc0 + (g >> 2);
				b = 0xc0 + (b >> 2);
			}
			System.out.println(r + " " + g + " " + b);
			colors[i] = new Color(r, g, b);
		}
		
		/* create the buttons */
		for (int i = 0; i < buttons.length; i++) {
			for (AbstractButton button : buttons[i])
				if (button != null)
					remove(button);
			buttons[i] = new AbstractButton[timelineEditor.getTracks().size()];
			for (int j = 0; j < timelineEditor.getTracks().size(); j++) {
				final Track track = timelineEditor.getTracks().get(j);
				switch (i) {
				case 0:
					buttons[i][j] = new JToggleButton(dontshowIcon);
					buttons[i][j].setSelectedIcon(showIcon);
					buttons[i][j].setToolTipText("show curve");
					break;
				case 1:
					buttons[i][j] = new JButton(createColorIcon(j));
					buttons[i][j].setToolTipText("set curve color");
					break;
				case 2:
					buttons[i][j] = new JButton(scaleIcon);
					buttons[i][j].setToolTipText("set curve scale and offset");
					break;
				}
				buttons[i][j].setBorderPainted(false);
				buttons[i][j].setContentAreaFilled(false);
				buttons[i][j].setFocusable(false);
				buttons[i][j].setOpaque(false);
				buttons[i][j].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
				add(buttons[i][j]);
			}
		}
	}
	
	@Override
	void layoutButtons() {
		int y = 0;
		
		for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			Track track = timelineEditor.getTracks().get(i);
//			showButton[i].setVisible(!track.isHidden());
//			if (track.isHidden())
//				continue;
			if (track.isExpandable()) {
				buttons[0][i].setBounds(2, y + 1, 13, 13);
				buttons[1][i].setBounds(16, y + 1, 13, 13);
				buttons[2][i].setBounds(30, y + 1, 13, 13);
//				showButton[i].setSelected(track.isExpanded());
			}
			y += track.getHeight();
		}
	}
	
	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
		Rectangle clip = g.getClipBounds();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		int y = 0;
		Track prev = null;
		for (Track track : timelineEditor.getTracks()) {
			if (y + Track.TRACK_HEIGHT > clip.y && y < clip.y + clip.height) {
				
				/* if track is selected, paint blue background */
				if (setSelectedTracks.contains(track)) {
					g.setColor(TimelineEditor.SELECTED_BACKGROUND);
					g.fillRect(0, y, width, Track.TRACK_HEIGHT);
				}
				
				if (track instanceof HeaderTrack) {
					
					/* draw header name in groove style */
					g.setColor(TimelineEditor.HIGHLIGHT);
					g.drawString(track.getName(), 48 + track.getIndent(), y + 11);
					g.setColor(TimelineEditor.SHADOW);
					g.drawString(track.getName(), 47 + track.getIndent(), y + 10);					
				} else {
					
					/* draw track name */
					g.setColor(Color.BLACK);
					g.setFont(plain);
					g.drawString(track.getName(), 48 + track.getIndent(), y + 11);
				}
			}
			y += Track.TRACK_HEIGHT;
		}
		g.setClip(clip);
		g.setColor(TimelineEditor.BACKGROUND);
		g.fillRect(width - 5, clip.y, 3, clip.height);
		
		g.setColor(TimelineEditor.SHADOW);
		g.drawLine(width - 6, y, width - 6, y + 5);
		g.drawLine(width - 2, y, width - 2, y + 5);
		g.setColor(TimelineEditor.LIGHT_SHADOW);
		g.drawLine(width - 1, y, width - 1, y + 5);
		g.setColor(Color.BLACK);
		g.drawLine(0, y + 6, width - 1, y + 6);
		g.setColor(TimelineEditor.SHADOW);
		Rectangle r = timelineEditor.getRowHeader().getViewRect();
		g.fillRect(0, y + 7, width, r.height);
	}
	
	public Icon createColorIcon(final int i) {
		return new Icon() {
			public void paintIcon(Component c, Graphics g, int x, int y) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				g.setColor(colors[i]);
				g.fillRect(2, 2, 8, 8);
				g.setColor(Color.BLACK);
				g.drawRect(2, 2, 8, 8);
			}
			
			public int getIconWidth() {
				return 13;
			}
			
			public int getIconHeight() {
				return 13;
			}
		};
	}
}
		
	
