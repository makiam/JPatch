/*
 * $Id: Header.java,v 1.5 2006/01/19 16:26:29 sascha_l Exp $
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
		private Cursor horizontalResizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
		private Cursor verticalResizeCursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
		private Cursor cornerResizeCursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
		private boolean bHorizontalResizeCursor = false;
		private boolean bVerticalResizeCursor = false;
		private boolean bHorizontalResize = false;
		private int iVerticalResize = -1;
		private static final Icon[] iconDownArrow = new Icon[] {createIcon(0, Color.BLACK), createIcon(0, Color.WHITE), createIcon(0, UIManager.getColor("Button.focus")) };
		private static final Icon[] iconUpArrow = new Icon[] {createIcon(1, Color.BLACK), createIcon(1, Color.WHITE), createIcon(1, UIManager.getColor("Button.focus")) };
		private JToggleButton[] expandButton;
		
		public Header(TimelineEditor tle) {
			timeLineEditor = tle;
			addMouseListener(this);
			addMouseMotionListener(this);
			setLayout(null);
			expandButton = new JToggleButton[timeLineEditor.getTracks().size()];
			for (int i = 0; i < timeLineEditor.getTracks().size(); i++) {
				final Track track = timeLineEditor.getTracks().get(i);
				expandButton[i] = new JToggleButton(iconDownArrow[0]);
				expandButton[i].setRolloverIcon(iconDownArrow[2]);
				expandButton[i].setPressedIcon(iconDownArrow[1]);
				expandButton[i].setSelectedIcon(iconUpArrow[0]);
				//expandButton[i].set(iconDownArrow[1]);
				expandButton[i].setRolloverSelectedIcon(iconUpArrow[2]);
				expandButton[i].setBorderPainted(false);
				expandButton[i].setContentAreaFilled(false);
				expandButton[i].setFocusable(false);
				expandButton[i].setOpaque(false);
				expandButton[i].setToolTipText("expand track");
				expandButton[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						boolean expanded = !track.isExpanded();
						expandTrack(track, expanded);
						((JToggleButton) e.getSource()).setToolTipText(expanded ? "collapse track" : "expand track");
						((JToggleButton) e.getSource()).setPressedIcon(expanded ? iconUpArrow[1] : iconDownArrow[1]);
					}
				});
				add(expandButton[i]);
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
				//if (track.isExpanded()) {
				//	upButton[i].setVisible(true);
				//	downButton[i].setVisible(false);
				//	upButton[i].setBounds(width - 26, y + 3, 11, 6);
				//} else {
				//	upButton[i].setVisible(false);
				//	downButton[i].setVisible(true);
					expandButton[i].setBounds(width - 16, y + 3, 11, 6);
					expandButton[i].setSelected(track.isExpanded());
				//}
				y += track.getHeight();
			}
		}
		
		private void setHorizontalResizeCursor(boolean enable) {
			if (enable == bHorizontalResizeCursor)
				return;
			bHorizontalResizeCursor = enable;
			setCursor(bHorizontalResizeCursor ? horizontalResizeCursor : defaultCursor);
		}
		
		private void setVerticalResizeCursor(boolean enable) {
			if (enable == bVerticalResizeCursor)
				return;
			bVerticalResizeCursor = enable;
			if (!bHorizontalResizeCursor)
				setCursor(bVerticalResizeCursor ? verticalResizeCursor : defaultCursor);
			else
				setCursor(bVerticalResizeCursor ? cornerResizeCursor : horizontalResizeCursor);
		}
		
		public Dimension getPreferredSize() {
			dim.setSize(width, timeLineEditor.getTracksHeight() + 4);
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
			revalidate();
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
			setHorizontalResizeCursor(e.getX() > width - 5);
			int y = 0;
			boolean vResize = false;
			for (int i = 0; i < timeLineEditor.getTracks().size(); i++) {
			Track track = timeLineEditor.getTracks().get(i);
				if (!bHorizontalResize && track.isExpanded() && e.getY() > y + track.getHeight() - 5 && e.getY() <= y + track.getHeight()) {
					vResize = true;
					break;
				}
				y += track.getHeight();
			}
			setVerticalResizeCursor(vResize);
		}
		
		public void mouseDragged(MouseEvent e) {
			if (bHorizontalResize) {
				width = e.getX() + 3;
				if (width < 16 + 5)
					width = 16 + 5;
				if (width > timeLineEditor.getWidth() - 32)
					width = timeLineEditor.getWidth() - 32;
				setSize(getPreferredSize());
				timeLineEditor.doLayout();
			}
			if (iVerticalResize > -1) {
				int y = 0;
				for (int i = 0; i < iVerticalResize; i++)
					y += timeLineEditor.getTracks().get(i).getHeight();
				int h = e.getY() - y + 3;
				if (h < 32)
					h = 32;
				if (h > 512)
					h = 512;
				((AvarTrack) timeLineEditor.getTracks().get(iVerticalResize)).setExpandedHeight(h);
				timeLineEditor.revalidate();
				revalidate();
				((JComponent) timeLineEditor.getViewport().getView()).revalidate();
				timeLineEditor.repaint();
			}
		}
		
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() == 2 && e.getX() > width - 5) {
				width = 128;
				setSize(getPreferredSize());
				timeLineEditor.doLayout();
				setHorizontalResizeCursor(e.getX() > width - 5);
				return;
			}
			bHorizontalResize = e.getX() > width - 5;
			iVerticalResize = -1;
			//setHorizontalResizeCursor(bHorizontalResize);
			
				int y = 0;
				for (int i = 0; i < timeLineEditor.getTracks().size(); i++) {
				Track track = timeLineEditor.getTracks().get(i);
					if (e.getClickCount() == 2 && e.getY() > y && e.getY() < y + track.getHeight() - 5) {
						expandTrack(track, !track.isExpanded());
						return;
					} else if (track.isExpanded() && e.getY() > y + track.getHeight() - 5 && e.getY() <= y + track.getHeight()) {
						if (e.getClickCount() == 2) {
							((AvarTrack) track).setDefaultExpandedHeight();
							timeLineEditor.revalidate();
							revalidate();
							((JComponent) timeLineEditor.getViewport().getView()).revalidate();
							timeLineEditor.repaint();
							setVerticalResizeCursor(e.getY() > y + track.getHeight() - 5 && e.getY() <= y + track.getHeight());
						} else {
							iVerticalResize = i;
						}
					}
					y += track.getHeight();
				}
			
//			repaint();
		}
		
		public void mouseReleased(MouseEvent e) {
			if (bHorizontalResize)
				bHorizontalResize = false;
			if (iVerticalResize > -1)
				iVerticalResize = -1;
			setHorizontalResizeCursor(e.getX() > width - 5);
			int y = 0;
			boolean vResize = false;
			for (int i = 0; i < timeLineEditor.getTracks().size(); i++) {
			Track track = timeLineEditor.getTracks().get(i);
				if (!bHorizontalResize && track.isExpanded() && e.getY() > y + track.getHeight() - 5 && e.getY() <= y + track.getHeight()) {
					vResize = true;
					break;
				}
				y += track.getHeight();
			}
			setVerticalResizeCursor(vResize);
		}
		
		public void mouseExited(MouseEvent e) {
			if (!bHorizontalResize)
				setHorizontalResizeCursor(false);
			if (iVerticalResize < 0)
				setVerticalResizeCursor(false);
		}

		public void mouseClicked(MouseEvent e) { }

		public void mouseEntered(MouseEvent e) { }
	}