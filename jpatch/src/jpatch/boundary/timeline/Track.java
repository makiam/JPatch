/*
 * $Id: Track.java,v 1.2 2006/01/21 15:15:55 sascha_l Exp $
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

import jpatch.entity.*;

public class Track<M extends MotionCurve> {
	
	static final Color SEPARATOR = new Color(255, 255, 255);
	static final Color TRACK = new Color(208, 216, 200);
	static final Color KEY = new Color(136, 128, 144);
	static final Color TICK = new Color(200, 192, 186);
	static final Color ZERO = new Color(178, 170, 162);
	static final Color CURVE = new Color(0, 0, 0);
	
	static final int TRACK_HEIGHT = 16;
	static final int EXPANDED_HEIGHT = 92;
	
	int iExpandedHeight = EXPANDED_HEIGHT;
	boolean bExpanded = false;
	boolean bExpandable = false;
	TimelineEditor timelineEditor;
	M motionCurve;
	
	public Track(TimelineEditor timelineEditor, M motionCurve) {
		this.timelineEditor = timelineEditor;
		this.motionCurve = motionCurve;
	}
	
	public int getHeight() {
		return bExpanded ? iExpandedHeight : TRACK_HEIGHT;
	}
	
	public String getName() {
		return motionCurve.getName();
	}
	
	public int getInlay() {
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
	
	public void setDefaultExpandedHeight() {
		iExpandedHeight = EXPANDED_HEIGHT;
	}
	
	public boolean isExpanded() {
		return bExpanded;
	}
	
	public void paint(Graphics g, int y) {	
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1;
		
		g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
		g.drawLine(clip.x, y + getHeight() - 1, clip.x + clip.width, y + getHeight() - 1);
		g.setColor(TRACK);
		g.fillRect(clip.x, y + 5, clip.width, 5);
		
		g.setColor(KEY);
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			if (motionCurve.hasKeyAt(frame)) {
				//g.fill3DRect(x + start - iFrameWidth / 2, y + 2, iFrameWidth, 11, true);
				g.setColor(KEY);
				g.fillOval(x + start - 3, y + 4, 6, 6);
				g.setColor(Color.BLACK);
				g.drawOval(x + start - 3, y + 4, 6, 6);
				
			} else {
				if (frame % 6 == 0) {
					g.setColor(ZERO);
					g.drawLine(x + start, y + 5, x + start, y + 9);
				} else {
					g.setColor(TICK);
					g.drawLine(x + start, y + 5, x + start, y + 9);
				}
			}
			frame++;
		}
	}
}