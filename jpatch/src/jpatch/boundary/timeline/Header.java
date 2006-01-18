/*
 * $Id: Header.java,v 1.3 2006/01/18 16:05:02 sascha_l Exp $
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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Header extends JComponent implements MouseListener, MouseMotionListener {
		/**
		 * 
		 */
		private final TimelineEditor timeLineEditor;
		int width = 128;
		private Dimension dim = new Dimension(width, 16 * 20);
		private Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		private Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
		private boolean bResizeCursor = false;
		private boolean bResizing = false;
		private static final Icon[] iconDownArrow = new Icon[] {createIcon(0, Color.BLACK), createIcon(1, Color.LIGHT_GRAY), createIcon(0, UIManager.getColor("Button.focus")) };
		private static final Icon[] iconUpArrow = new Icon[] {createIcon(1, Color.BLACK), createIcon(1, Color.LIGHT_GRAY), createIcon(1, UIManager.getColor("Button.focus")) };
		private JButton[] downButton;
		private JButton[] upButton;
		
		public Header(TimelineEditor tle) {
			timeLineEditor = tle;
			addMouseListener(this);
			addMouseMotionListener(this);
			setLayout(null);
			downButton = new JButton[timeLineEditor.getTracks().size()];
			upButton = new JButton[timeLineEditor.getTracks().size()];
			for (int i = 0; i < timeLineEditor.getTracks().size(); i++) {
				final Track track = timeLineEditor.getTracks().get(i);
				downButton[i] = new JButton(iconDownArrow[0]);
				downButton[i].setDisabledIcon(iconDownArrow[1]);
				downButton[i].setRolloverIcon(iconDownArrow[2]);
				downButton[i].setBorderPainted(false);
				downButton[i].setContentAreaFilled(false);
				downButton[i].setFocusable(false);
				downButton[i].setOpaque(false);
				downButton[i].setToolTipText("expand track");
				downButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						expandTrack(track, true);
					}
				});
				upButton[i] = new JButton(iconUpArrow[0]);
				upButton[i].setDisabledIcon(iconUpArrow[1]);
				upButton[i].setRolloverIcon(iconUpArrow[2]);
				upButton[i].setBorderPainted(false);
				upButton[i].setContentAreaFilled(false);
				upButton[i].setFocusable(false);
				upButton[i].setOpaque(false);
				upButton[i].setToolTipText("collapse track");
				upButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						expandTrack(track, false);
					}
				});
				add(downButton[i]);
				add(upButton[i]);
			}
		}
		
		public void doLayout() {
			super.doLayout();
			layoutButtons();
		}
		
		private void layoutButtons() {
			int y = 0;
			
			for (int i = 0; i < timeLineEditor.getTracks().size(); i++) {
				Track track = timeLineEditor.getTracks().get(i);
				if (track.isExpanded()) {
					upButton[i].setVisible(true);
					downButton[i].setVisible(false);
					upButton[i].setBounds(width - 16, y + 3, 11, 6);
				} else {
					upButton[i].setVisible(false);
					downButton[i].setVisible(true);
					downButton[i].setBounds(width - 16, y + 3, 11, 6);
				}
				y += track.getHeight();
			}
		}
		
		private void setResizeCursor(boolean enable) {
			if (enable == bResizeCursor)
				return;
			bResizeCursor = enable;
			if (bResizeCursor)
				setCursor(resizeCursor);
			else
				setCursor(defaultCursor);
		}
		public Dimension getPreferredSize() {
			dim.setSize(width, timeLineEditor.getTracksHeight());
			return dim;
		}
		
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}
		
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Rectangle clip = g.getClipBounds();
			//int frame = start / TimelineEditor.this.iFrameWidth - 1;
//			g.setColor(Color.WHITE);
//			for (int x = -TimelineEditor.this.iFrameWidth ; x <= clip.width + TimelineEditor.this.iFrameWidth; x += TimelineEditor.this.iFrameWidth) {
//				g.drawLine(x + start, clip.y, x + start, clip.y + clip.height);
//			}
			
//			g.setColor(getBackground().darker());
//			g.drawLine(width - 5, clip.y, width - 5, clip.y + clip.height - 1);
//			g.drawLine(width - 1, clip.y, width - 1, clip.y + clip.height - 1);
			
			int y = 0;
			for (Track track : timeLineEditor.getTracks()) {
				g.setColor(Color.BLACK);
				g.drawString(((AvarTrack) track).getName(), 8, y + 12);
				if (track.isExpanded()) {
					g.setColor(getBackground().darker());
					g.drawLine(width - 5, y, width - 5, y + track.getHeight() - 5);
					g.drawLine(width - 1, y, width - 1, y + track.getHeight() - 5);
					g.drawLine(clip.x, y + track.getHeight() - 5, clip.x + clip.width - 1, y + track.getHeight() - 5);
					g.drawLine(clip.x, y + track.getHeight() - 1, clip.x + clip.width - 1, y + track.getHeight() - 1);
					g.setColor(getBackground());
					g.fillRect(width - 4, y, 3, track.getHeight());
				} else {
					g.setColor(getBackground());
					g.fillRect(width - 5, y, 5, track.getHeight());
					g.setColor(getBackground().darker());
					g.drawLine(width - 5, y, width - 5, y + track.getHeight() - 1);
					g.drawLine(width - 1, y, width - 1, y + track.getHeight() - 1);
					g.drawLine(clip.x, y + track.getHeight() - 1, clip.x + clip.width - 5, y + track.getHeight() - 1);
				}
				y += track.getHeight();
			}
			g.setColor(getBackground().darker());
			g.drawLine(width - 5, y, width - 5, getHeight() - 1);
			g.drawLine(width - 1, y, width - 1, getHeight() - 1);
//			if (bResizing)
//				g.setColor(getBackground().darker());
//			if (bResizeCursor)
//				g.setColor(getBackground().brighter());
//			else
			
//			g.setColor(Color.BLACK);
//			g.drawLine(x + 1, clip.y, x + 1, clip.height);
		}
		
		private void expandTrack(Track track, boolean expand) {
			if (track.isExpanded() == expand)
				return;
			track.expand(expand);
			timeLineEditor.revalidate();
			((JComponent) timeLineEditor.getRowHeader().getView()).revalidate();
			((JComponent) timeLineEditor.getViewport().getView()).revalidate();
			timeLineEditor.repaint();
			return;
		}
		
		private static Icon createIcon(final int type, final Color color) {
			return new Icon() {
				public void paintIcon(Component c, Graphics g, int x, int y) {
					g.setColor(color);
					switch (type) {
					case 0:
						g.fillPolygon(new int[] { 0, 9, 4 }, new int[] { 1, 1, 6}, 3);
						break;
					case 1:
						g.fillPolygon(new int[] { -1, 9, 4 }, new int[] { 5, 5, -1}, 3);
						break;
					}
				}

				public int getIconWidth() {
					return 6;
				}

				public int getIconHeight() {
					return 4;
				}
			};
		}
		
		public void mouseMoved(MouseEvent e) {
			setResizeCursor(e.getX() > width - 5);
		}
		
		public void mouseDragged(MouseEvent e) {
			if (bResizing) {
				width = e.getX() + 5;
				if (width < 16 + 5)
					width = 16 + 5;
				if (width > timeLineEditor.getWidth() - 32)
					width = timeLineEditor.getWidth() - 32;
				setSize(getPreferredSize());
				timeLineEditor.doLayout();
			}
		}
		
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() == 2 && e.getX() > width - 5) {
				width = 128;
				setSize(getPreferredSize());
				timeLineEditor.doLayout();
				return;
			}
			bResizing = e.getX() > width - 5;
			setResizeCursor(bResizing);
			if (e.getClickCount() == 2) {
				int y = 0;
				for (Track track : timeLineEditor.getTracks()) {
					if (e.getY() > y && e.getY() < y + track.getHeight()) {
						expandTrack(track, !track.isExpanded());
						return;
					}
					y += track.getHeight();
				}
			}
//			repaint();
		}
		public void mouseReleased(MouseEvent e) {
			if (bResizing) {
				bResizing = false;
//				repaint();
			}
		}
		public void mouseExited(MouseEvent e) {
			if (!bResizing)
				setResizeCursor(false);
		}

		public void mouseClicked(MouseEvent e) { }

		public void mouseEntered(MouseEvent e) { }
	}