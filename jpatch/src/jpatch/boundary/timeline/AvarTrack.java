/*
 * $Id: AvarTrack.java,v 1.24 2006/06/07 20:07:44 sascha_l Exp $
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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Map;

import jpatch.boundary.MainFrame;
import jpatch.entity.*;

public class AvarTrack extends Track<MotionCurve.Float> {
	
	private static final int TANGENT_LENGTH = 1;
	private Stroke normalStroke = new BasicStroke();
	private Stroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1, new float[] { 3, 3 }, 0);
	
	float scale;
	int offset;
	
	AvarTrack() { }
	
	public AvarTrack(TimelineEditor timelineEditor, MotionCurve.Float motionCurve) {
		super(timelineEditor, motionCurve);
		bExpandable = true;
		setExpandedHeight(iExpandedHeight);
	}
	
	@Override
	public void setExpandedHeight(int height) {
		iExpandedHeight = height;
		float min = motionCurve.getMin();
		float max = motionCurve.getMax();
		int size = iExpandedHeight - 4;
		scale = (size - 2) / (max - min);
		offset = size + Math.round(min * scale) - 1;
	}
	
	void drawCurve(Graphics g, boolean drawTangents, int y, Color curveColor, Color keyColor, MotionCurve.Float motionCurve, Map<MotionKey, TrackView.KeyData> selection, MotionKey[] hitKeys) {
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		
		/*
		 * draw the curve
		 */
		int yPrev = 0;
		g.setColor(curveColor);
		for (int x = -fw ; x <= clip.width + fw; x++) {
			float f = (float) (start + x - fw / 2) / fw;
			int vThis = y + offset - Math.round(scale * motionCurve.getFloatAt(f));
			if (yPrev < vThis)
				yPrev++;
			else if (yPrev > vThis)
				yPrev--;
			g.drawLine(x + start, vThis, x + start, yPrev);
			yPrev = vThis;
		}
		
		int frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
		int index = motionCurve.getIndexAt(frame);
		int end = frame + clip.width / fw + 1;
		int xOffset = (int) MainFrame.getInstance().getAnimation().getStart() * fw + fw / 2;
		TangentHandle.Float inTangent;
		TangentHandle.Float outTangent;
		for (int n = motionCurve.getKeyCount(); index < n; index++) {
			MotionKey.Float key = (MotionKey.Float) motionCurve.getKey(index);
			frame = (int) key.getPosition();
			if (frame > end)
				break;
			int vThis = y + offset - Math.round(scale * key.getFloat());
			int vPrev = 0;
			int vNext = 0;
			
			inTangent = getInTangent(key);
			if (inTangent != null)
				vPrev = y + offset - Math.round(scale * inTangent.getValue());
			
			outTangent = getOutTangent(key);
			if (outTangent != null)
				vNext = y + offset - Math.round(scale * outTangent.getValue());
			
			int x = frame * fw + xOffset;
			
			if (drawTangents) {
			
				/*
				 *  draw tangents
				 */
				g.setColor(curveColor);
				if (key.getTangentMode() != MotionKey.TangentMode.MANUAL)
					((Graphics2D) g).setStroke(dashedStroke);
				if (inTangent != null)
					g.drawLine(x - fw * TANGENT_LENGTH, vPrev, x, vThis);
				if (outTangent != null)
					g.drawLine(x, vThis, x + fw * TANGENT_LENGTH, vNext);
				((Graphics2D) g).setStroke(normalStroke);
				
				/* 
				 * draw tangent handles
				 */
	//			if (key.getTangentMode() == MotionKey.TangentMode.MANUAL)
	//				g.setColor(curveColor);
	//			else
	//				g.setColor(Color.GRAY);
				
				int xIn = x - fw * TANGENT_LENGTH;
				int xOut = x + fw * TANGENT_LENGTH;
				if (key.getTangentMode() == MotionKey.TangentMode.OVERSHOOT) {
					if (inTangent != null) {
						g.fillRect(xIn - 2, vPrev - 2, 5, 5);
					}
					if (outTangent != null) {
						g.fillRect(xOut - 2, vNext - 2, 5, 5);
					}
				} else if (key.getTangentMode() == MotionKey.TangentMode.AUTO) {
					if (inTangent != null) {
						g.drawOval(xIn - 2, vPrev - 2, 4, 4);
						g.fillOval(xIn - 2, vPrev - 2, 4, 4);
					}
					if (outTangent != null) {
						g.drawOval(xOut - 2, vNext - 2, 4, 4);
						g.fillOval(xOut - 2, vNext - 2, 4, 4);
					}
				} else {
					if (key.isSmooth()) {
						if (inTangent != null) {
							g.drawOval(xIn - 2, vPrev - 2, 4, 4);
							g.fillOval(xIn - 2, vPrev - 2, 4, 4);
						}
						if (outTangent != null) {
							g.drawOval(xOut - 2, vNext - 2, 4, 4);
							g.fillOval(xOut - 2, vNext - 2, 4, 4);
						}
					} else {
						Polygon p;
						if (inTangent != null) {
							p = new Polygon(new int[] { xIn - 3, xIn + 3, xIn + 0 }, new int[] { vPrev + 2, vPrev + 2, vPrev - 4 }, 3);
							g.drawPolygon(p);
							g.fillPolygon(p);
						}
						if (outTangent != null) {
							p = new Polygon(new int[] { xOut - 3, xOut + 3, xOut + 0 }, new int[] { vNext + 2, vNext + 2, vNext - 4 }, 3);
							g.drawPolygon(p);
							g.fillPolygon(p);
						}
					}
				}
			}
			
			/*
			 * draw key
			 */
			Color fillColor;
			if (keyHit(key, hitKeys))
				fillColor = TimelineEditor.HIT_KEY;
			else if (selection.containsKey(key))
				fillColor = TimelineEditor.SELECTED_KEY;
			else
				fillColor = keyColor;
			drawKey(g, key, x - 3, vThis - 3, fillColor, Color.BLACK);
		}
	}
	
	void drawTrack(Graphics g, int y, int offset) {
		int bottom = iExpandedHeight - 4;
		Rectangle clip = g.getClipBounds();
		
		/*
		 * draw track background
		 */
		if (timelineEditor.getHeader().getSelectedTracks().contains(this)) 
			g.setColor(TimelineEditor.SELECTED_BACKGROUND);
		else
			g.setColor(TimelineEditor.TRACK);
		g.fillRect(clip.x, y + 1, clip.width, bottom);
		g.setColor(TimelineEditor.BACKGROUND);
		g.fillRect(clip.x, y + bottom + 1, clip.width, 3);
		g.setColor(TimelineEditor.SHADOW);
		g.drawLine(clip.x, y, clip.x + clip.width, y);
		g.drawLine(clip.x, y + bottom, clip.x + clip.width, y + bottom);
		g.setColor(TimelineEditor.LIGHT_SHADOW);
		g.drawLine(clip.x, y + 1, clip.x + clip.width, y + 1);
		g.setClip(clip.intersection(new Rectangle(clip.x, y + 1, clip.width, bottom - 1)));
		
		/*
		 * draw ticks
		 */
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
		g.setColor(TimelineEditor.DARK_TICK);
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			if (frame % 6 == 0)
				g.drawLine(x + start, y + 2, x + start, y + bottom - 1);
			else
				g.drawLine(x + start, y + offset - 5, x + start, y + offset + 5);
			frame++;
		}
		
		/*
		 * draw zero-line
		 */
		g.drawLine(clip.x, y + offset, clip.x + clip.width, y + offset);
		
	}
	
	@Override
	public void paint(Graphics g, int y, Map<MotionKey, TrackView.KeyData> selection, MotionKey[] hitKeys) {
		if (!bExpanded) {
			super.paint(g, y, selection, hitKeys);
			return;
		}
		Rectangle clip = g.getClipBounds();
		float min = motionCurve.getMin();
		float max = motionCurve.getMax();
		float scale = max - min;
		int size = iExpandedHeight - 4;
		int off = iExpandedHeight - 4 + Math.round(size * min / scale);
		
		/*
		 * draw track
		 */
		drawTrack(g, y, off);
		
		/*
		 * draw curve
		 */
		drawCurve(g, true, y, Color.BLACK, Color.GRAY, motionCurve, selection, hitKeys);
		
		g.setClip(clip);
	}
	
	private TangentHandle.Float getInTangent(MotionKey key) {
		int index = key.getIndex();
		/* check if the previous key is cubic */
		if (index > 0 && key.getMotionCurve().getKey(index - 1).getInterpolation() == MotionKey.Interpolation.CUBIC) 
			return new TangentHandle.Float(key, TangentHandle.Side.IN);
		else
			return null;
	}
	
	private TangentHandle.Float getOutTangent(MotionKey key) {
		/* check if this key is cubic */
		if (key.getInterpolation() == MotionKey.Interpolation.CUBIC)	
			return new TangentHandle.Float(key, TangentHandle.Side.OUT);
		else
			return null;
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
		MotionKey key;
		
		System.out.println("frame = " + frame);
		
		key = motionCurve.getKeyAt(frame + TANGENT_LENGTH);
		if (key != null) {
			handleIn = getInTangent(key);
			hyIn = off - Math.round(size / scale * handleIn.getValue());
		}
		
		key = motionCurve.getKeyAt(frame - TANGENT_LENGTH);
		if (key != null) {
			handleOut = getOutTangent(key);
			hyOut = off - Math.round(size / scale * handleOut.getValue());
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
		int off = iExpandedHeight - 4 + Math.round(size * min / scale);
		int ky = off - Math.round(size / scale * key.getFloat());
		if (my > ky - 5 && my < ky + 5)
			return new MotionKey[] { key };
		return null;
	}
	
	@Override
	public void moveKey(Object object, int y) {
		float min = motionCurve.getMin();
		float max = motionCurve.getMax();
		float scale = max - min;
		int size = iExpandedHeight - 4;
		int off = iExpandedHeight - 4 + Math.round(size * min / scale);
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