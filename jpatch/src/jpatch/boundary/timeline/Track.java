/*
 * $Id: Track.java,v 1.10 2006/02/06 16:40:41 sascha_l Exp $
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

import javax.swing.UIManager;

import jpatch.control.edit.AtomicModifyMotionCurve;
import jpatch.control.edit.JPatchUndoableEdit;
import jpatch.entity.*;

public class Track<M extends MotionCurve> {
	
//	static final Color SEPARATOR = new Color(164, 164, 164);
//	static final Color TRACK = new Color(208, 216, 200);
	static final Color KEY = new Color(128, 128, 128);
//	static final Color TICK = new Color(200, 192, 186);
//	static final Color ZERO = new Color(178, 170, 162);
//	static final Color CURVE = new Color(0, 0, 0);
	
	static final int TRACK_HEIGHT = 13;
	static final int EXPANDED_HEIGHT = 92;
	static final int TOP = 4;
	
	int iExpandedHeight = EXPANDED_HEIGHT;
	boolean bHidden = false;
	boolean bExpanded = false;
	boolean bExpandable = false;
	TimelineEditor timelineEditor;
	M motionCurve;
	
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
	
//	public void setDefaultExpandedHeight() {
//		iExpandedHeight = EXPANDED_HEIGHT;
//	}
	
	public boolean isExpanded() {
		return bExpanded;
	}
	
	public boolean isHidden() {
		return bHidden;
	}
	
	public void setHidden(boolean hidden) {
		bHidden = hidden;
	}
	
	public Object getKeyAt(int mx, int my) {
		System.out.println("*");
		int frame = mx / timelineEditor.getFrameWidth();
		return motionCurve.getKeyAt(frame);
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
			return new AtomicModifyMotionCurve.Float((MotionCurve.Float) motionCurve, frame, ((MotionCurve.Float) motionCurve).getFloatAt(frame));
		}
		if (motionCurve instanceof MotionCurve.Point3d) {
			return new AtomicModifyMotionCurve.Point3d((MotionCurve.Point3d) motionCurve, frame, ((MotionCurve.Point3d) motionCurve).getPoint3dAt(frame));
		}
		if (motionCurve instanceof MotionCurve.Quat4f) {
			return new AtomicModifyMotionCurve.Quat4f((MotionCurve.Quat4f) motionCurve, frame, ((MotionCurve.Quat4f) motionCurve).getQuat4fAt(frame));
			}
		if (motionCurve instanceof MotionCurve.Color3f) {
			return new AtomicModifyMotionCurve.Color3f((MotionCurve.Color3f) motionCurve, frame, ((MotionCurve.Color3f) motionCurve).getColor3fAt(frame));
		}
		// can't handle this case - needs subclassing
		throw new UnsupportedOperationException();
	}
	
	public void paint(Graphics g, int y, Object selectedKey) {	
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1;
		Color background, track;
		if (timelineEditor.getHeader().getSelectedTracks().contains(this)) {
			background = TimelineEditor.SELECTED_BACKGROUND;
			track = TimelineEditor.SHADOW;
		} else {
			background = TimelineEditor.BACKGROUND;
			track = TimelineEditor.LIGHT_SHADOW;
		}
//		g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
//		g.drawLine(clip.x, y + getHeight() - 1, clip.x + clip.width, y + getHeight() - 1);
//		g.setColor(TRACK);
//		g.fillRect(clip.x, y + 3, clip.width, 5);
		g.setColor(background);
		g.drawLine(clip.x, y + TOP - 2, clip.x + clip.width, y + TOP - 2);
		g.drawLine(clip.x, y + TOP - 1, clip.x + clip.width, y + TOP - 1);
		g.drawLine(clip.x, y + TOP + 0, clip.x + clip.width, y + TOP + 0);
		g.drawLine(clip.x, y + TOP + 4, clip.x + clip.width, y + TOP + 4);
		g.drawLine(clip.x, y + TOP + 5, clip.x + clip.width, y + TOP + 5);
		g.drawLine(clip.x, y + TOP + 6, clip.x + clip.width, y + TOP + 6);
		g.setColor(track);
		g.drawLine(clip.x, y + TOP + 1, clip.x + clip.width, y + TOP + 1);
//		g.setColor(TimelineEditor.SHADOW);
//		g.drawLine(clip.x, y + TOP + 4, clip.x + clip.width, y + TOP + 4);
//		g.setColor(TimelineEditor.SHADOW);
		g.drawLine(clip.x, y + TOP + 2, clip.x + clip.width, y + TOP + 2);
		g.drawLine(clip.x, y + TOP + 3, clip.x + clip.width, y + TOP + 3);
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			MotionKey key = motionCurve.getKeyAt(frame);
			if (key != null) {
				if (key == selectedKey)
					g.setColor(TimelineEditor.SELECTED_KEY);
				else
					g.setColor(Color.GRAY);
				g.fillOval(x + start - 3, y + TOP - 1, 6, 6);
				g.setColor(Color.BLACK);
				g.drawOval(x + start - 3, y + TOP - 1, 6, 6);
			}
			
//			else {
//				if (frame % 6 == 0) {
//					g.setColor(ZERO);
//					g.drawLine(x + start, y + 3, x + start, y + 6);
//				} else {
//					g.setColor(TICK);
//					g.drawLine(x + start, y + 3, x + start, y + 6);
//				}
//			}
			frame++;
		}
	}
}