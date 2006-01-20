/*
 * $Id: Header.java,v 1.7 2006/01/20 20:28:23 sascha_l Exp $
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
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

import javax.swing.*;

public class Header extends JComponent implements MouseListener, MouseMotionListener {
		/**
		 * 
		 */
		private final TimelineEditor timelineEditor;
		int width = 128;
		private Dimension dim = new Dimension(width, 16 * 20);
		
		private boolean bHorizontalResize = false;
		private int iVerticalResize = -1;
		private static final Icon[] iconDownArrow = new Icon[] {createIcon(0, Color.BLACK), createIcon(0, Color.WHITE), createIcon(0, UIManager.getColor("Button.focus")) };
		private static final Icon[] iconUpArrow = new Icon[] {createIcon(1, Color.BLACK), createIcon(1, Color.WHITE), createIcon(1, UIManager.getColor("Button.focus")) };
		private JToggleButton[] expandButton;
		
		public Header(TimelineEditor tle) {
			timelineEditor = tle;
			addMouseListener(this);
			addMouseMotionListener(this);
			setLayout(null);
			expandButton = new JToggleButton[timelineEditor.getTracks().size()];
			for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
				final Track track = timelineEditor.getTracks().get(i);
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
						timelineEditor.expandTrack(track, expanded);
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
			
			for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
				Track track = timelineEditor.getTracks().get(i);
				//if (track.isExpanded()) {
				//	upButton[i].setVisible(true);
				//	downButton[i].setVisible(false);
				//	upButton[i].setBounds(width - 26, y + 3, 11, 6);
				//} else {
				//	upButton[i].setVisible(false);
				//	downButton[i].setVisible(true);
					expandButton[i].setBounds(3, y + 4, 11, 6);
					expandButton[i].setSelected(track.isExpanded());
				//}
				y += track.getHeight();
			}
		}
		
		public Dimension getPreferredSize() {
			dim.setSize(width, timelineEditor.getTracksHeight() + 4);
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
			for (Track track : timelineEditor.getTracks()) {
				g.setColor(Color.BLACK);
				g.drawString(((AvarTrack) track).getName(), 16, y + 12);
				if (track.isExpanded()) {
					g.setColor(UIManager.getColor("ScrollBar.shadow"));
					g.drawLine(width - 1, y, width - 1, y + track.getHeight() - 5);
					g.drawLine(0, y + track.getHeight() - 1, width - 1, y + track.getHeight() - 1);
					g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
					g.drawLine(width - 6, y, width - 6, y + track.getHeight() - 1);
					g.drawLine(width - 2, y, width - 2, y + track.getHeight() - 1);
					g.drawLine(0, y + track.getHeight() - 6, width - 1, y + track.getHeight() - 6);
					g.drawLine(0, y + track.getHeight() - 2, width - 1, y + track.getHeight() - 2);
					
					g.setColor(getBackground());
					g.fillRect(width - 5, y, 3, track.getHeight());
					g.fillRect(0, y + track.getHeight() - 5, width, 3);
				} else {
					g.setColor(UIManager.getColor("ScrollBar.shadow"));
					g.drawLine(width - 1, y, width - 1, y + track.getHeight() - 1);
					g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
					g.drawLine(width - 6, y, width - 6, y + track.getHeight() - 1);
					g.drawLine(width - 2, y, width - 2, y + track.getHeight() - 1);
					g.drawLine(0, y + track.getHeight() - 1, width - 1, y + track.getHeight() - 1);
					g.setColor(getBackground());
					g.fillRect(width - 5, y, 3, track.getHeight());
				}
				y += track.getHeight();
			}
			if (timelineEditor.getTracks().get(timelineEditor.getTracks().size() - 1).isExpanded())
				y -= 1;
			g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
			g.drawLine(0, y - 1, width - 1, y - 1);
			g.setColor(UIManager.getColor("ScrollBar.shadow"));
			g.drawLine(0, y, width - 1, y);
//			g.setColor(getBackground().darker());
//			g.drawLine(width - 5, 0, width - 5, y - 1);
//			g.drawLine(width - 1, 0, width - 1, y - 1);
//			g.drawLine(width - 5, 0, width - 1, 0);
//			g.drawLine(width - 5, y - 1, width - 1, y - 1);
//			g.setColor(getBackground().darker().darker());
//			g.fillRect(0, y, width, clip.height - y);
//			if (bResizing)
//				g.setColor(getBackground().darker());
//			if (bResizeCursor)
//				g.setColor(getBackground().brighter());
//			else
			
//			g.setColor(Color.BLACK);
//			g.drawLine(x + 1, clip.y, x + 1, clip.height);
		}
		
		private static Icon createIcon(final int type, final Color color) {
			return new Icon() {
				public void paintIcon(Component c, Graphics g, int x, int y) {
					((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
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
			boolean hResize = e.getX() > width - 6 && e.getY() < timelineEditor.getTracksHeight();
			int y = 0;
			boolean vResize = false;
			for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			Track track = timelineEditor.getTracks().get(i);
				if (!bHorizontalResize && track.isExpanded() && e.getY() > y + track.getHeight() - 6 && e.getY() <= y + track.getHeight()) {
					vResize = true;
					break;
				}
				y += track.getHeight();
			}
			if (hResize && vResize)
				timelineEditor.setCursor(TimelineEditor.cornerResizeCursor);
			else if (hResize)
				timelineEditor.setCursor(TimelineEditor.horizontalResizeCursor);
			else if (vResize)
				timelineEditor.setCursor(TimelineEditor.verticalResizeCursor);
			else 
				timelineEditor.setCursor(TimelineEditor.defaultCursor);
		}
		
		public void mouseDragged(MouseEvent e) {
			if (bHorizontalResize) {
				width = e.getX() + 3;
				if (width < 48 + 6)
					width = 48 + 6;
				if (width > timelineEditor.getWidth() - 48)
					width = timelineEditor.getWidth() - 48;
				setSize(getPreferredSize());
				timelineEditor.doLayout();
			}
			if (iVerticalResize > -1) {
				int y = 0;
				for (int i = 0; i < iVerticalResize; i++)
					y += timelineEditor.getTracks().get(i).getHeight();
				int h = e.getY() - y + 3;
				if (h < 32)
					h = 32;
				if (h > 256)
					h = 256;
				((AvarTrack) timelineEditor.getTracks().get(iVerticalResize)).setExpandedHeight(h);
				timelineEditor.revalidate();
				revalidate();
				((JComponent) timelineEditor.getViewport().getView()).revalidate();
				timelineEditor.repaint();
			}
		}
		
		public void mousePressed(MouseEvent e) {
			if (e.getClickCount() == 2 && e.getX() > width - 6) {
				width = 128;
				setSize(getPreferredSize());
				timelineEditor.doLayout();
//				setHorizontalResizeCursor(e.getX() > width - 5);
				return;
			}
			bHorizontalResize = e.getX() > width - 6;
			iVerticalResize = -1;
			//setHorizontalResizeCursor(bHorizontalResize);
			
				int y = 0;
				for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
				Track track = timelineEditor.getTracks().get(i);
					if (e.getClickCount() == 2 && e.getY() > y && e.getY() < y + track.getHeight() - 6) {
						timelineEditor.expandTrack(track, !track.isExpanded());
						return;
					} else if (track.isExpanded() && e.getY() > y + track.getHeight() - 6 && e.getY() <= y + track.getHeight()) {
						if (e.getClickCount() == 2) {
							((AvarTrack) track).setDefaultExpandedHeight();
							timelineEditor.revalidate();
							revalidate();
							((JComponent) timelineEditor.getViewport().getView()).revalidate();
							timelineEditor.repaint();
//							setVerticalResizeCursor(e.getY() > y + track.getHeight() - 5 && e.getY() <= y + track.getHeight());
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
//			setHorizontalResizeCursor(e.getX() > width - 5);
//			int y = 0;
//			boolean vResize = false;
//			for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
//			Track track = timelineEditor.getTracks().get(i);
//				if (!bHorizontalResize && track.isExpanded() && e.getY() > y + track.getHeight() - 5 && e.getY() <= y + track.getHeight()) {
//					vResize = true;
//					break;
//				}
//				y += track.getHeight();
//			}
//			setVerticalResizeCursor(vResize);
		}
		
		public void mouseExited(MouseEvent e) {
			timelineEditor.setCursor(TimelineEditor.defaultCursor);
		}

		public void mouseClicked(MouseEvent e) { }

		public void mouseEntered(MouseEvent e) { }
	}