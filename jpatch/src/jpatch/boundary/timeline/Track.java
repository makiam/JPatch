/*
 * $Id: Track.java,v 1.1 2006/01/17 21:06:39 sascha_l Exp $
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

import java.awt.Graphics;

public abstract class Track {
	public final static int TRACK_HEIGHT = 16;
	boolean bExpanded = false;
	
	public int getHeight() {
		return TRACK_HEIGHT;
	}
	
	public abstract void paint(Graphics g, int y);
	
	public abstract String getName();
	
	public void expand(boolean expand) {
		bExpanded = expand;
	}
	
	public boolean isExpanded() {
		return bExpanded;
	}
}