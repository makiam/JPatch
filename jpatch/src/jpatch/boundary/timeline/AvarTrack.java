/*
 * $Id: AvarTrack.java,v 1.20 2006/05/30 14:20:22 sascha_l Exp $
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
import java.util.Map;

import javax.swing.UIManager;

import jpatch.boundary.MainFrame;
import jpatch.entity.*;

public class AvarTrack extends Track<MotionCurve.Float> {
	
	public AvarTrack(TimelineEditor timelineEditor, MotionCurve.Float motionCurve) {
		super(timelineEditor, motionCurve);
		bExpandable = true;
	}
	
	@Override
	public void paint(Graphics g, int y, Map<MotionKey, TrackView.KeyData> selection, MotionKey[] hitKeys) {
		int bottom = getHeight() - 4;
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
		
		if (bExpanded) {
			float min = motionCurve.getMin();
			float max = motionCurve.getMax();
			
			float scale = max - min;
			int size = iExpandedHeight - 4;
			int off = iExpandedHeight - 4 + (int) Math.round(size * min / scale);
			if (timelineEditor.getHeader().getSelectedTracks().contains(this)) 
				g.setColor(TimelineEditor.SELECTED_BACKGROUND);
			else
				g.setColor(TimelineEditor.TRACK);
			g.fillRect(clip.x, y + 1, clip.width, size);

			frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
			g.setColor(TimelineEditor.BACKGROUND);
//			g.fillRect(clip.x, y - 3, clip.width, 3);
			g.fillRect(clip.x, y + bottom + 1, clip.width, 3);
			g.setColor(TimelineEditor.SHADOW);
			g.drawLine(clip.x, y, clip.x + clip.width, y);
			g.drawLine(clip.x, y + bottom, clip.x + clip.width, y + bottom);
			g.setColor(TimelineEditor.LIGHT_SHADOW);
			g.drawLine(clip.x, y + 1, clip.x + clip.width, y + 1);
			g.setClip(clip.intersection(new Rectangle(clip.x, y + 1, clip.width, bottom - 1)));
			g.setColor(TimelineEditor.DARK_TICK);
			for (int x = -fw ; x <= clip.width + fw; x += fw) {
				if (frame % 6 == 0)
					g.drawLine(x + start, y + 2, x + start, y + size - 1);
				else
					g.drawLine(x + start, y + off - 5, x + start, y + off + 5);
				frame++;
			}
			g.drawLine(clip.x, y + off, clip.x + clip.width, y + off);

			frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
			
			int vPrev = off - (int) Math.round(size / scale * motionCurve.getFloatAt(frame));
			g.setColor(Color.BLACK);
			for (int x = -fw ; x <= clip.width + fw; x++) {
				float f = (float) (start + x - fw / 2) / fw;
				int vThis = off - (int) Math.round(size / scale * motionCurve.getFloatAt(f));
				g.drawLine(x + start - 1, y + vPrev, x + start, y + vThis);
				frame++;
				vPrev = vThis;
			}
			
			frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
			for (int x = -fw ; x <= clip.width + fw; x += fw) {
				int vThis = off - (int) Math.round(size / scale * motionCurve.getFloatAt(frame));
				MotionKey key = motionCurve.getKeyAt(frame);
				if (key != null) {
//					if (keyHit(key, hitKeys))
//						g.setColor(TimelineEditor.HIT_KEY);
//					else if (selection.containsKey(key))
//						g.setColor(TimelineEditor.SELECTED_KEY);
//					else
//						g.setColor(Color.GRAY);
//					g.fillOval(x + start - 3, y + vThis - 3, 6, 6);
//					g.setColor(Color.BLACK);
//					g.drawOval(x + start - 3, y + vThis - 3, 6, 6);
					Color fillColor;
					if (keyHit(key, hitKeys))
						fillColor = TimelineEditor.HIT_KEY;
					else if (selection.containsKey(key))
						fillColor = TimelineEditor.SELECTED_KEY;
					else
						fillColor = Color.GRAY;
					drawKey(g, key, x + start - 3, y + vThis - 3, fillColor, Color.BLACK);
				}
				frame++;
			}
			g.setClip(clip);
			return;
		}
		super.paint(g, y, selection, hitKeys);
	}
	
	@Override
	public MotionKey[] getKeysAt(int mx, int my) {
		if (!bExpanded) {
			return super.getKeysAt(mx, my);
		}
		int frame = mx / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
		MotionKey.Float key = (MotionKey.Float) motionCurve.getKeyAt(frame);
		if (key == null)
			return null;
		float min = motionCurve.getMin();
		float max = motionCurve.getMax();
		float scale = max - min;
		int size = iExpandedHeight - 4;
		int off = iExpandedHeight - 4 + (int) Math.round(size * min / scale);
		int ky = off - (int) Math.round(size / scale * key.getFloat());
		if (my > ky - 5 && my < ky + 5)
			return new MotionKey[] { key };
		return null;
	}
	
	public void moveKey(Object object, int y) {
		MotionKey.Float key = (MotionKey.Float) object;
		float min = motionCurve.getMin();
		float max = motionCurve.getMax();
		float scale = max - min;
		int size = iExpandedHeight - 4;
		int off = iExpandedHeight - 4 + (int) Math.round(size * min / scale);
		float f = (off - y) * scale / size;
		if (f < min)
			f = min;
		if (f > max)
			f = max;
		key.setFloat(f);
	}
}