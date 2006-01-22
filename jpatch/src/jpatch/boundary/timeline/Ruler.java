/*
 * $Id: Ruler.java,v 1.6 2006/01/22 21:14:45 sascha_l Exp $
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

class Ruler extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener {
		/**
		 * 
		 */
		private final TimelineEditor timelineEditor;
		
		/**
		 * @param editor
		 */
		Ruler(TimelineEditor timelineEditor) {
			this.timelineEditor = timelineEditor;
			addMouseListener(this);
			addMouseMotionListener(this);
			addMouseWheelListener(this);
			setCursor(TimelineEditor.horizontalResizeCursor);
		}

		private Dimension dim = new Dimension();
		private Font font = new Font("Monospaced", Font.PLAIN, 10);
		private int mouseX;
		
		public Dimension getPreferredSize() {
			dim.setSize(timelineEditor.getFrameWidth() * 24 * 60, 16); // FIXME: use animation length
			return dim;
		}
		
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}
		
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}
		
		public void paintComponent(Graphics g) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			super.paintComponent(g);
			g.setFont(font);
			Rectangle clip = g.getClipBounds();
			
			int fw = timelineEditor.getFrameWidth();
			int start = clip.x - clip.x % fw + fw / 2;
			int frame = start / fw - 1;
			//((Graphics2D) g).setPaint(new GradientPaint(0, 0, new Color(255, 255, 128), 0, 16, getBackground().brighter()));
			g.setColor(new Color(255, 255, 224));
			((Graphics2D) g).fill(clip);
			g.fillRect(clip.x, clip.y, clip.width, clip.height);
			g.setColor(Color.BLACK);
			g.drawLine(clip.x, 15, clip.x + clip.width, 15);
			for (int x = -fw ; x <= clip.width + fw; x += fw) {
				if (frame % 6 == 0)
					g.drawLine(x + start, getHeight() - 6, x + start, getHeight() - 1);
				else
					g.drawLine(x + start, getHeight() - 4, x + start, getHeight() - 3);
				if (frame % 12 == 0) {
					if (fw > 2 || frame % 24 == 0) {
						String num = String.valueOf(frame);
						g.drawString(num, x + start - num.length() * 3 + 1, 9);
					}
				}
				if (frame == timelineEditor.getCurrentFrame()) {
//					g.setColor(Color.BLACK);
//					g.fillPolygon(new int[] { x + start - 4, x + start + 7, x + start + 1}, new int[] { clip.y + clip.height - 6, clip.y + clip.height - 6, clip.y + clip.height - 0}, 3);
//					g.setColor(Color.RED);
					g.fillPolygon(new int[] { x + start - 5, x + start + 6, x + start }, new int[] { getHeight() - 6, getHeight() - 6, getHeight() - 0}, 3);
//					g.setColor(Color.BLACK);
				}
				frame++;
			}
			
//			g.setColor(Color.WHITE);
//			g.draw3DRect(clip.x, clip.y, clip.width - 1, clip.height - 1, false);
//			g.draw3DRect(clip.x + 1, clip.y + 1, clip.width - 3, clip.height - 3, true);
		}

		public void mouseWheelMoved(MouseWheelEvent e) {
			setFrameWidth(timelineEditor.getFrameWidth() - e.getWheelRotation());
		}
		
		private void setFrameWidth(int newWidth) {
			if (newWidth < 6)
				newWidth = 6;
			if (newWidth > 32)
				newWidth = 32;
			Rectangle r = timelineEditor.getViewport().getViewRect();
			r.x = r.x * newWidth / timelineEditor.getFrameWidth();
			timelineEditor.setFrameWidth(newWidth);
			timelineEditor.getHorizontalScrollBar().setValue(r.x);
			revalidate();
			((JComponent) timelineEditor.getViewport().getView()).revalidate();
			((JComponent) timelineEditor.getRowHeader().getView()).revalidate();
		}
		
		public void mousePressed(MouseEvent e) {
			mouseX = (e.getX() - timelineEditor.getViewport().getViewRect().x) / timelineEditor.getFrameWidth();
		}
		
		public void mouseDragged(MouseEvent e) {
			if (mouseX == 0)
				return;
			int x = e.getX() - timelineEditor.getViewport().getViewRect().x;
			int nf = x / mouseX;
			setFrameWidth(nf);
		}

		public void mouseClicked(MouseEvent e) { }

		public void mouseReleased(MouseEvent e) { }

		public void mouseEntered(MouseEvent e) { }

		public void mouseExited(MouseEvent e) { }

		public void mouseMoved(MouseEvent e) { }
		
		
	}