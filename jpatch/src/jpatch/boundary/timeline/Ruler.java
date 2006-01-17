/*
 * $Id: Ruler.java,v 1.1 2006/01/17 21:06:39 sascha_l Exp $
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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

class Ruler extends JComponent {
		/**
		 * 
		 */
		private final TimelineEditor timelineEditor;
		
		/**
		 * @param editor
		 */
		Ruler(TimelineEditor timelineEditor) {
			this.timelineEditor = timelineEditor;
		}

		private Dimension dim = new Dimension();
		private Font font = new Font("Monospaced", Font.PLAIN, 10);
		
		public Dimension getPreferredSize() {
			dim.setSize(timelineEditor.getFrameWidth() * 200, 16); // FIXME: use animation length
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
			g.setFont(font);
			Rectangle clip = g.getClipBounds();
			int fw = timelineEditor.getFrameWidth();
			int start = clip.x - clip.x % fw + fw / 2;
			int frame = start / fw - 1;
//			((Graphics2D) g).setPaint(new GradientPaint(0, 0, getBackground(), 0, 20, getBackground().brighter()));
//			((Graphics2D) g).fill(clip);
//			g.fillRect(clip.x, clip.y, clip.width, clip.height);
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
	}