/*
 * $Id: Track.java,v 1.22 2006/06/07 20:07:44 sascha_l Exp $
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
import java.util.Arrays;
import java.util.Map;

import javax.swing.UIManager;

import jpatch.boundary.MainFrame;
import jpatch.control.edit.AtomicAddMotionKey;
import jpatch.control.edit.AtomicModifyMotionCurve;
import jpatch.control.edit.AtomicMoveMotionKey;
import jpatch.control.edit.JPatchUndoableEdit;
import jpatch.entity.*;

public class Track<M extends MotionCurve> {
		
	static final int TRACK_HEIGHT = 13;
	static final int EXPANDED_HEIGHT = 92;
	static final int TOP = 4;
	
	int iExpandedHeight = EXPANDED_HEIGHT;
	boolean bHidden = false;
	boolean bExpanded = false;
	boolean bExpandable = false;
	TimelineEditor timelineEditor;
	M motionCurve;
	
	Track() { }
	
	public Track(TimelineEditor timelineEditor, M motionCurve) {
		this.timelineEditor = timelineEditor;
		this.motionCurve = motionCurve;
	}
	
	public int getHeight() {
		if (isHidden())
			return 0;
		return isExpanded() ? iExpandedHeight : TRACK_HEIGHT;
	}
	
	public String getName() {
		return motionCurve.getName();
	}
	
	public int getIndent() {
		return 0;
	}
	
	public void expand(boolean expand) {
		if (bExpandable)
			bExpanded = expand;
	}
	
	public boolean isExpandable() {
		return bExpandable;
	}
	
	public void setExpandedHeight(int height) {
		iExpandedHeight = height;
	}
	
	public boolean isExpanded() {
		return bExpanded;
	}
	
	public boolean isHidden() {
		return bHidden;
	}
	
	public void setHidden(boolean hidden) {
		bHidden = hidden;
	}
	
	public MotionKey[] getKeysAt(int mx, int my) {
//		System.out.println("*");
		int frame = mx / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
		MotionKey key = motionCurve.getKeyAt(frame);
//		boolean contains = selection.containsKey(key);
		if (key != null)
			return new MotionKey[] { key };
		else
			return null;
//		return contains;
	}
	
//	public MotionCurve getMotionCurve(MotionKey key) {
//		return motionCurve;
//	}
	
	public MotionCurve[] getMotionCurves() {
		return new MotionCurve[] { motionCurve };
	}
	
	public void moveKey(Object key, int y) {
//		throw new UnsupportedOperationException(getClass().getName() + " doesn't support this method");
	}
	
	public void shiftKey(Object key, int frame) {
		motionCurve.moveKey((MotionKey) key, frame);
	}
	
	public JPatchUndoableEdit insertKeyAt(int frame) {
		if (motionCurve.hasKeyAt(frame))
			return null;
		if (motionCurve instanceof MotionCurve.Float) {
			return new AtomicAddMotionKey(motionCurve, new MotionKey.Float(frame, ((MotionCurve.Float) motionCurve).getFloatAt(frame)));
		}
		if (motionCurve instanceof MotionCurve.Point3d) {
			return new AtomicAddMotionKey(motionCurve, new MotionKey.Point3d(frame, ((MotionCurve.Point3d) motionCurve).getPoint3dAt(frame)));
		}
		if (motionCurve instanceof MotionCurve.Quat4f) {
			return new AtomicAddMotionKey(motionCurve, new MotionKey.Quat4f(frame, ((MotionCurve.Quat4f) motionCurve).getQuat4fAt(frame)));
			}
		if (motionCurve instanceof MotionCurve.Color3f) {
			return new AtomicAddMotionKey(motionCurve, new MotionKey.Color3f(frame, ((MotionCurve.Color3f) motionCurve).getColor3fAt(frame)));
		}
		if (motionCurve instanceof MotionCurve.Object) {
			return null;
		}
		throw new UnsupportedOperationException(); // can't handle this case - needs subclassing
	}
	
	/*
	 * paint the track
	 */
	public void paint(Graphics g, int y, Map<MotionKey, TrackView.KeyData> selection, MotionKey[] hitKeys) {	
		/*
		 * get clip bounds, compute start frame.
		 */
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
		
		/*
		 * setup colors
		 */
		Color background, track;
		if (timelineEditor.getHeader().getSelectedTracks().contains(this)) {
			background = TimelineEditor.SELECTED_BACKGROUND;
			track = TimelineEditor.SHADOW;
		} else {
			background = TimelineEditor.BACKGROUND;
			track = TimelineEditor.LIGHT_SHADOW;
		}
		
		/*
		 * paint the track
		 */
		g.setColor(background);
		g.drawLine(clip.x, y + TOP - 2, clip.x + clip.width, y + TOP - 2);
		g.drawLine(clip.x, y + TOP - 1, clip.x + clip.width, y + TOP - 1);
		g.drawLine(clip.x, y + TOP + 0, clip.x + clip.width, y + TOP + 0);
		g.drawLine(clip.x, y + TOP + 4, clip.x + clip.width, y + TOP + 4);
		g.drawLine(clip.x, y + TOP + 5, clip.x + clip.width, y + TOP + 5);
		g.drawLine(clip.x, y + TOP + 6, clip.x + clip.width, y + TOP + 6);
		g.setColor(track);
		g.drawLine(clip.x, y + TOP + 1, clip.x + clip.width, y + TOP + 1);
		g.drawLine(clip.x, y + TOP + 2, clip.x + clip.width, y + TOP + 2);
		g.drawLine(clip.x, y + TOP + 3, clip.x + clip.width, y + TOP + 3);
		
		/*
		 * draw the motion-keys
		 */
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			MotionKey key = motionCurve.getKeyAt(frame);
			if (key != null) {
				Color fillColor;
				if (keyHit(key, hitKeys))
					fillColor = TimelineEditor.HIT_KEY;
				else if (selection.containsKey(key))
					fillColor = TimelineEditor.SELECTED_KEY;
				else
					fillColor = Color.GRAY;
				drawKey(g, key, x + start - 3, y + TOP - 1, fillColor, Color.BLACK);
			}
			frame++;
		}
	}
	
	boolean keyHit(MotionKey key, MotionKey[] hitKeys) {
		if (hitKeys == null)
			return false;
		for (MotionKey hitKey : hitKeys)
			if (key.equals(hitKey))
				return true;
		return false;
	}
	
	void drawKey(Graphics g, MotionKey key, int x, int y, Color fill, Color outline) {
		switch (key.getInterpolation()) {
		case CUBIC:
			g.setColor(fill);
			g.fillOval(x, y, 6, 6);
			g.setColor(outline);
			g.drawOval(x, y, 6, 6);
			break;
		case LINEAR:
			Polygon polygon = new Polygon(new int[] { x + 3, x + 6, x + 3, x }, new int[] { y, y + 3, y + 6, y + 3 }, 4);
			g.setColor(fill);
			g.fillPolygon(polygon);
			g.setColor(outline);
			g.drawPolygon(polygon);
			break;
		case DISCRETE:
			g.setColor(fill);
			g.fillRect(x, y, 6, 6);
			g.setColor(outline);
			g.drawRect(x, y, 6, 6);
			break;
		}
	}
}