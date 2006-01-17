/*
 * $Id: Header.java,v 1.1 2006/01/17 21:06:39 sascha_l Exp $
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

import javax.swing.JComponent;

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
		
		public Header(TimelineEditor tle) {
			timeLineEditor = tle;
			addMouseListener(this);
			addMouseMotionListener(this);	
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
			
			int y = 0;
			for (Track track : timeLineEditor.getTracks()) {
				
				g.setColor(Color.WHITE);
				g.drawLine(clip.x, y + track.getHeight() - 1, clip.x + clip.width, y + track.getHeight() - 1);
				g.setColor(Color.BLACK);
				g.drawString(((AvarTrack) track).getName(), 8, y + 12);
				y += track.getHeight();
			}
//			if (bResizing)
//				g.setColor(getBackground().darker());
//			if (bResizeCursor)
//				g.setColor(getBackground().brighter());
//			else
				g.setColor(getBackground());
			g.fill3DRect(width - 6, 0, 6, dim.height, true);
//			g.setColor(Color.BLACK);
//			g.drawLine(x + 1, clip.y, x + 1, clip.height);
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
						track.expand(!track.isExpanded());
						timeLineEditor.revalidate();
						((JComponent) timeLineEditor.getRowHeader().getView()).revalidate();
						timeLineEditor.repaint();
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