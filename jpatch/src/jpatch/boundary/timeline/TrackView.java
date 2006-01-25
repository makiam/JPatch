/*
 * $Id: TrackView.java,v 1.10 2006/01/25 16:56:29 sascha_l Exp $
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

@SuppressWarnings("serial")
class TrackView extends JComponent implements Scrollable, MouseListener, MouseMotionListener {
		/**
		 * 
		 */
		private final TimelineEditor timelineEditor;
		private Dimension dim = new Dimension();
		private int iVerticalResize = -1;
		private int mx, my;
		
		public TrackView(TimelineEditor tle) {
			timelineEditor = tle;
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		public Dimension getPreferredSize() {
			dim.setSize(timelineEditor.getFrameWidth() * 24 * 60 + 1, timelineEditor.getTracksHeight() + 7); // FIXME: use animation length
			return dim;
		}
		
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}
		
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}
		
		public void paintComponent(Graphics g) {
//			long t = System.currentTimeMillis();
			super.paintComponent(g);
			Rectangle clip = g.getClipBounds();
//			System.out.println(clip);
			int fw = timelineEditor.getFrameWidth();
			int start = clip.x - clip.x % fw + fw / 2;
			//int frame = start / TimelineEditor.this.iFrameWidth - 1;
//			g.setColor(Color.WHITE);
//			for (int x = -TimelineEditor.this.iFrameWidth ; x <= clip.width + TimelineEditor.this.iFrameWidth; x += TimelineEditor.this.iFrameWidth) {
//				g.drawLine(x + start, clip.y, x + start, clip.y + clip.height);
//			}
			g.setColor(TimelineEditor.TICK);
			int frame = start / fw - 1;
			for (int x = -fw ; x <= clip.width + fw; x += fw) {
				if (frame % 6 == 0) {
//					g.setColor(TimelineEditor.TICK);
					g.drawLine(x + start, clip.y, x + start, clip.y + clip.height);
//					g.setColor(TimelineEditor.HIGHLIGHT);
//					g.drawLine(x + start + 1, clip.y, x + start + 1, clip.y + clip.height);
				} else {
//					g.drawLine(x + start, y + 3, x + start, y + iExpandedHeight - 3);
				}
				frame++;
			}
			
			int y = 0;
			for (Track track : timelineEditor.getTracks()) {
				if (y + track.getHeight() > clip.y && y < clip.y + clip.height)
					track.paint(g, y);
				y += track.getHeight();
			}
//			if (timelineEditor.getTracks().size() > 0 && timelineEditor.getTracks().get(timelineEditor.getTracks().size() - 1).isExpanded())
//				y -= 1;
//			g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
//			g.drawLine(clip.x, y - 1, clip.x + clip.width - 1, y - 1);
//			g.setColor(UIManager.getColor("ScrollBar.shadow"));
//			g.drawLine(clip.x, y, clip.x + clip.width - 1, y);
			//timelineEditor.getHeight();
//			if (timelineEditor.getViewport().getHeight() > clip.height)
//				g.setClip(clip.x, clip.y, clip.width, timelineEditor.getViewport().getHeight());
//			g.fillRect(clip.x, y, clip.x + clip.width - 1, timelineEditor.getHeight() - y);
			
			int x = timelineEditor.getCurrentFrame() * fw + fw / 2;
			g.setColor(Color.BLACK);
			g.drawLine(x, clip.y, x, clip.y + clip.height - 1);
			g.fillPolygon(new int[] { x - 6, x + 6, x }, new int[] { getHeight() - 1, getHeight() - 1, getHeight() - 8}, 3);
			
			g.setColor(Color.BLACK);
			g.drawLine(clip.x, y + 6, clip.x + clip.width - 1, y + 6);
//			
//			g.setColor(Color.BLACK);
//			g.drawLine(x + 1, clip.y, x + 1, clip.height);
//			System.out.println((System.currentTimeMillis() - t) + " ms");
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
		
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
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
							((JComponent) timelineEditor.getRowHeader().getView()).revalidate();
							timelineEditor.repaint();
	//						setVerticalResizeCursor(e.getY() > y + track.getHeight() - 5 && e.getY() <= y + track.getHeight());
						} else {
							iVerticalResize = i;
						}
					}
					y += track.getHeight();
				}
			} else if (e.getButton() == MouseEvent.BUTTON2) {
				mx = e.getX();
				my = e.getY();
				System.out.println(mx);
			}
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
			if (iVerticalResize > -1)
				iVerticalResize = -1;
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
			if (iVerticalResize < 0)
				timelineEditor.setCursor(TimelineEditor.defaultCursor);
		}

		public void mouseDragged(MouseEvent e) {
			System.out.println(e.getButton());
			if (iVerticalResize > -1) {
				int y = 0;
				for (int i = 0; i < iVerticalResize; i++)
					y += timelineEditor.getTracks().get(i).getHeight();
				int h = e.getY() - y + 3;
				if (h < 32)
					h = 32;
				if (h > 256)
					h = 256;
				timelineEditor.getTracks().get(iVerticalResize).setExpandedHeight(h);
				timelineEditor.revalidate();
				revalidate();
				((JComponent) timelineEditor.getRowHeader().getView()).revalidate();
				timelineEditor.repaint();
			}
			else {
//				int x = timelineEditor.getHorizontalScrollBar().getValue();
//				int y = timelineEditor.getVerticalScrollBar().getValue();
				JScrollBar hsb = timelineEditor.getHorizontalScrollBar();
				JScrollBar vsb = timelineEditor.getVerticalScrollBar();
				int sbx = hsb.getValue();
				int sby = vsb.getValue();
				int dx = e.getX() - mx;
				int dy = e.getY() - my;
				mx = (sbx == 0 || sbx == hsb.getMaximum() - hsb.getVisibleAmount()) ? e.getX() : e.getX() - dx;
				my = (sby == 0 || sby == vsb.getMaximum() - vsb.getVisibleAmount()) ? e.getY() : e.getY() - dy;
//				System.out.println("y=" + e.getY() + "\tmy=" + my + " \tdy=" + dy + " \tsby=" + sby + " max=" + (vsb.getMaximum() - vsb.getVisibleAmount()));
				timelineEditor.getHorizontalScrollBar().setValue(sbx - dx);
				timelineEditor.getVerticalScrollBar().setValue(sby - dy);
//				dx = e.getX() - sbx;
//				dy = e.getY() - sby;
//				revalidate();
//				timelineEditor.revalidate();
//				((JComponent) timelineEditor.getRowHeader().getView()).revalidate();
			}
		}

		public void mouseMoved(MouseEvent e) {
			boolean vResize = false;
			int y = 0;
			for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			Track track = timelineEditor.getTracks().get(i);
				if (track.isExpanded() && e.getY() > y + track.getHeight() - 6 && e.getY() <= y + track.getHeight()) {
					vResize = true;
					break;
				}
				y += track.getHeight();
			}
			if (vResize)
				timelineEditor.setCursor(TimelineEditor.verticalResizeCursor);
			else 
				timelineEditor.setCursor(TimelineEditor.defaultCursor);
		}
	}