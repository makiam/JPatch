/*
 * $Id: TrackView.java,v 1.1 2006/01/17 21:06:39 sascha_l Exp $
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
class TrackView extends JComponent implements Scrollable {
		/**
		 * 
		 */
		private final TimelineEditor timelineEditor;
		private Dimension dim = new Dimension();
		
		public TrackView(TimelineEditor tle) {
			timelineEditor = tle;
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent event) {
					if (event.getClickCount() == 2) {
						int y = 0;
						for (Track track : timelineEditor.getTracks()) {
							if (event.getY() > y && event.getY() < y + track.getHeight()) {
								track.expand(!track.isExpanded());
								revalidate();
								((JComponent) timelineEditor.getRowHeader().getView()).revalidate();
								timelineEditor.repaint();
								return;
							}
							y += track.getHeight();
						}
					}
				}
			});
		}
		
		public Dimension getPreferredSize() {
			dim.setSize(timelineEditor.getFrameWidth() * 200, timelineEditor.getTracksHeight()); // FIXME: use animation length
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
			int fw = timelineEditor.getFrameWidth();
			int start = clip.x - clip.x % fw;
			//int frame = start / TimelineEditor.this.iFrameWidth - 1;
//			g.setColor(Color.WHITE);
//			for (int x = -TimelineEditor.this.iFrameWidth ; x <= clip.width + TimelineEditor.this.iFrameWidth; x += TimelineEditor.this.iFrameWidth) {
//				g.drawLine(x + start, clip.y, x + start, clip.y + clip.height);
//			}
			int y = 0;
			for (Track track : timelineEditor.getTracks()) {
				track.paint(g, y);
				y += track.getHeight();
			}
			int x = timelineEditor.getCurrentFrame() * fw + fw / 2;
			g.setColor(Color.BLACK);
			g.drawLine(x, clip.y, x, clip.y + clip.height);
//			g.setColor(Color.BLACK);
//			g.drawLine(x + 1, clip.y, x + 1, clip.height);
		}
		
		public Dimension getPreferredScrollableViewportSize() {
			return new Dimension(600, 200);
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			switch (orientation) {
			case SwingConstants.HORIZONTAL:
				int x = visibleRect.x % timelineEditor.getFrameWidth();
				if (direction > 0)
					return timelineEditor.getFrameWidth() - x;
				else
					return x == 0 ? timelineEditor.getFrameWidth() : x;
			case SwingConstants.VERTICAL:
				int y= visibleRect.y % 16;
				if (direction > 0)
					return 16 - y;
				else
					return y == 0 ? 16 : y;
			}
			throw new IllegalArgumentException();
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			switch (orientation) {
			case SwingConstants.HORIZONTAL:
				return getScrollableUnitIncrement(visibleRect, orientation, direction) + timelineEditor.getFrameWidth() * 9;
			case SwingConstants.VERTICAL:
				return getScrollableUnitIncrement(visibleRect, orientation, direction) + 16 * 4;
			}
			throw new IllegalArgumentException();
		}

		public boolean getScrollableTracksViewportWidth() {
			return false;
		}

		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}