/*
 * $Id: AvarTrack.java,v 1.21 2006/06/03 20:08:59 sascha_l Exp $
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicScrollBarUI;

import jpatch.boundary.MainFrame;
import jpatch.entity.*;

public class AvarTrack extends Track<MotionCurve.Float> {
	
	private static final int TANGENT_LENGTH = 1;
	private Stroke normalStroke = new BasicStroke();
	private Stroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1, new float[] { 3, 3 }, 0);
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
			int index = motionCurve.getIndexAt(frame);
			int end = frame + clip.width / fw + 1;
			float[] d = new float[] { 0, 0 };
			int offset = (int) MainFrame.getInstance().getAnimation().getStart() * fw + fw / 2;
			for (int n = motionCurve.getKeyCount(); index < n; index++) {
				MotionKey.Float key = (MotionKey.Float) motionCurve.getKey(index);
				frame = (int) key.getPosition();
				if (frame > end)
					break;
				int vThis = off - Math.round(size / scale * key.getFloat());
				motionCurve.getDerivatives(index, d, false);
				int vPref = off - (int) Math.round(size / scale * (key.getFloat() - d[0] * TANGENT_LENGTH));
				int vNext = off - (int) Math.round(size / scale * (key.getFloat() + d[1] * TANGENT_LENGTH));
				
				/* draw tangents */
				g.setColor(Color.GRAY);
				int x = frame * fw + offset;
				if (key.getTangentMode() != MotionKey.TangentMode.MANUAL)
					((Graphics2D) g).setStroke(dashedStroke);
				g.drawLine(x - fw * TANGENT_LENGTH, y + vPref, x, y + vThis);
				if (key.isSmooth())
					((Graphics2D) g).setStroke(dashedStroke);
				g.drawLine(x, y + vThis, x + fw * TANGENT_LENGTH, y + vNext);
				((Graphics2D) g).setStroke(normalStroke);
				
				/* draw key */
				Color fillColor;
				if (keyHit(key, hitKeys))
					fillColor = TimelineEditor.HIT_KEY;
				else if (selection.containsKey(key))
					fillColor = TimelineEditor.SELECTED_KEY;
				else
					fillColor = Color.GRAY;
				drawKey(g, key, x - 3, y + vThis - 3, fillColor, Color.BLACK);
				
				/* draw tangent handles */
				g.setColor(Color.GRAY);
				if (key.getTangentMode() == MotionKey.TangentMode.MANUAL) {
					g.fillRect(x - fw * TANGENT_LENGTH - 2, y + vPref - 2, 5, 5);
					g.fillRect(x + fw * TANGENT_LENGTH - 2, y + vNext - 2, 5, 5);
				}
			}
			
//			for (int x = -fw ; x <= clip.width + fw; x += fw) {
//				int keyIndex = motionCurve.getIndexAt(frame) - 1;
//				if (keyIndex < 0)
//					continue;
//				if (keyIndex >= motionCurve.getKeyCount())
//					break;
//				MotionKey.Float key = (MotionKey.Float) motionCurve.getKey(keyIndex);
//				System.out.println(frame + " " + key.getPosition());
//				if (key != null && key.getPosition() == frame) {
//					int vThis = off - (int) Math.round(size / scale * key.getFloat());
//					motionCurve.getDerivatives(keyIndex, d);
//					int vPref = off - (int) Math.round(size / scale * (key.getFloat() - d[0]));
//					int vNext = off - (int) Math.round(size / scale * (key.getFloat() + d[1]));
////					if (keyHit(key, hitKeys))
////						g.setColor(TimelineEditor.HIT_KEY);
////					else if (selection.containsKey(key))
////						g.setColor(TimelineEditor.SELECTED_KEY);
////					else
////						g.setColor(Color.GRAY);
////					g.fillOval(x + start - 3, y + vThis - 3, 6, 6);
////					g.setColor(Color.BLACK);
////					g.drawOval(x + start - 3, y + vThis - 3, 6, 6);
//					
//					Color fillColor;
//					if (keyHit(key, hitKeys))
//						fillColor = TimelineEditor.HIT_KEY;
//					else if (selection.containsKey(key))
//						fillColor = TimelineEditor.SELECTED_KEY;
//					else
//						fillColor = Color.GRAY;
//					drawKey(g, key, x + start - 3, y + vThis - 3, fillColor, Color.BLACK);
//					g.drawLine(x + start - fw, y + vPref, x + start, y + vThis);
//					g.drawLine(x + start, y + vThis, x + start + fw, y + vNext);
//				}
//				frame++;
//			}
			g.setClip(clip);
			return;
		}
		super.paint(g, y, selection, hitKeys);
	}
	
	public TangentHandle getTangentHandleAt(int mx, int my) {
		System.out.println("getTangentHandleAt(" + mx + ", " + my + ") called");
		if (!bExpanded)
			return null;
		
		float min = motionCurve.getMin();
		float max = motionCurve.getMax();
		float scale = max - min;
		int size = iExpandedHeight - 4;
		int off = iExpandedHeight - 4 + Math.round(size * min / scale);
		
		int frame = mx / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
		int hyIn = 0;
		int hyOut = 0;
		TangentHandle.Float handleIn = null;
		TangentHandle.Float handleOut = null;
		
		System.out.println("frame = " + frame);
		MotionKey.Float keyIn = (MotionKey.Float) motionCurve.getKeyAt(frame + TANGENT_LENGTH);
		if (keyIn != null) {
			handleIn = new TangentHandle.Float(keyIn, TangentHandle.Side.IN);
			hyIn = off - Math.round(size / scale * handleIn.getValue());
			System.out.println("hyIn = " + hyIn);
		}
		
		MotionKey.Float keyOut = (MotionKey.Float) motionCurve.getKeyAt(frame - TANGENT_LENGTH);
		if (keyOut != null) {
			handleOut = new TangentHandle.Float(keyOut, TangentHandle.Side.OUT);
			hyOut = off - Math.round(size / scale * handleOut.getValue());
			System.out.println("hyOut = " + hyOut);
		}
		
		int dyIn = Math.abs(my - hyIn);
		int dyOut = Math.abs(my - hyOut);
		
		TangentHandle.Float handle = null;
		if (handleIn != null && dyIn < 5 && dyIn < dyOut)
			handle = handleIn;
		else if (handleOut != null && dyOut < 5)
			handle = handleOut;
		return handle;
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
		float min = motionCurve.getMin();
		float max = motionCurve.getMax();
		float scale = max - min;
		int size = iExpandedHeight - 4;
		int off = iExpandedHeight - 4 + (int) Math.round(size * min / scale);
		float f = (off - y) * scale / size;
		if (object instanceof MotionKey.Float) {
			if (f < min)
				f = min;
			if (f > max)
				f = max;
			((MotionKey.Float) object).setFloat(f);
		} else if (object instanceof TangentHandle.Float) {
			((TangentHandle.Float) object).setValue(f);
		}
	}
}