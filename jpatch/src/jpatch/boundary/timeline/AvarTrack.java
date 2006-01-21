/*
 * $Id: AvarTrack.java,v 1.9 2006/01/21 15:15:55 sascha_l Exp $
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
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.UIManager;

import jpatch.entity.Morph;
import jpatch.entity.MotionCurve;

public class AvarTrack extends Track<MotionCurve.Float> {
	
	public AvarTrack(TimelineEditor timelineEditor, MotionCurve.Float motionCurve) {
		super(timelineEditor, motionCurve);
		bExpandable = true;
	}
	
	public void paint(Graphics g, int y) {	
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1;
		
		if (bExpanded) {
			g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
			g.drawLine(clip.x, y + getHeight() - 2, clip.x + clip.width, y + getHeight() - 2);
			g.drawLine(clip.x, y + getHeight() - 6, clip.x + clip.width, y + getHeight() - 6);
			g.setColor(UIManager.getColor("ScrollBar.shadow"));
			g.drawLine(clip.x, y + getHeight() - 1, clip.x + clip.width, y + getHeight() - 1);
			float scale = motionCurve.getMax() - motionCurve.getMin();
			int size = iExpandedHeight - 17;
			int off = iExpandedHeight - 12 + (int) Math.round(size * motionCurve.getMin() / scale);
			g.setColor(TRACK);
			g.fillRect(clip.x, y + 5, clip.width, size + 1);
//			g.setColor(TRACK.darker());
//			g.drawLine(clip.x, y + 1, clip.x + clip.width, y + 1);
//			g.setColor(TRACK.brighter());
//			g.drawLine(clip.x, y + 61, clip.x + clip.width, y + 61);
			frame = start / fw - 1;
			g.setColor(TICK);
			for (int x = -fw ; x <= clip.width + fw; x += fw) {
				if (frame % 6 == 0) {
					g.setColor(ZERO);
					g.drawLine(x + start, y + 5, x + start, y + iExpandedHeight - 12);
					g.setColor(TICK);
				} else {
					g.drawLine(x + start, y + 5, x + start, y + iExpandedHeight - 12);
				}
				frame++;
			}
			g.setColor(ZERO);
			g.drawLine(clip.x, y + off, clip.x + clip.width, y + off);
			g.setClip(clip.intersection(new Rectangle(clip.x, y + 2, clip.width, size + 7)));
			int vPrev = off - (int) Math.round(size / scale * motionCurve.getFloatAt(frame));
			g.setColor(CURVE);
			frame = start / fw - 1;
			for (int x = -fw ; x <= clip.width + fw; x ++) {
				float f = (float) (start + x - fw / 2) / fw;
				int vThis = off - (int) Math.round(size / scale * motionCurve.getFloatAt(f));
//				g.setColor(Color.BLACK);
				g.drawLine(x + start - 1, y + vPrev, x + start, y + vThis);
				frame++;
				vPrev = vThis;
			}
			g.setColor(KEY);
			frame = start / fw - 1;
			for (int x = -fw ; x <= clip.width + fw; x += fw) {
				int vThis = off - (int) Math.round(size / scale * motionCurve.getFloatAt(frame));
				if (motionCurve.hasKeyAt(frame)) {
					g.fillOval(x + start - 3, y + vThis - 3, 6, 6);
					g.setColor(Color.BLACK);
					g.drawOval(x + start - 3, y + vThis - 3, 6, 6);
					g.setColor(KEY);
				} else {
//					g.fillRect(x + start - 1, y + vThis - 1, 3, 3);
				}
				frame++;
			}
			g.setClip(clip);
			return;
		}
		super.paint(g, y);
	}
}