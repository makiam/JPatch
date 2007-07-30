/*
 * $Id:$
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
package trashcan;

import com.jpatch.boundary.Viewport;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;

/**
 * @author sascha
 *
 */
public abstract class OrthoViewDirection implements ViewDirection {	
	public static final OrthoViewDirection FRONT = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setTuple(0, 0);
		}
		public String toString() {
			return "front";
		}
	};
	public static final OrthoViewDirection BACK = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setTuple(0, 180);
		}
		public String toString() {
			return "back";
		}
	};
	public static final OrthoViewDirection TOP = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setTuple(90, 0);
		}
		public String toString() {
			return "top";
		}
	};
	public static final OrthoViewDirection BOTTOM = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setTuple(-90, 0);
		}
		public String toString() {
			return "bottom";
		}
	};
	public static final OrthoViewDirection LEFT = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setTuple(0, 90);
		}
		public String toString() {
			return "left";
		}
	};
	public static final OrthoViewDirection RIGHT = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setTuple(0, -90);
		}
		public String toString() {
			return "right";
		}
	};
	public static final ViewDirection[] STANDARD_VIEW_DIRECTIONS = new ViewDirection[] {
		OrthoViewDirection.FRONT,
		OrthoViewDirection.BACK,
		OrthoViewDirection.TOP,
		OrthoViewDirection.BOTTOM,
		OrthoViewDirection.LEFT,
		OrthoViewDirection.RIGHT,
		new OrthoViewDirection.BirdsEye()
	};
	
	public static class BirdsEye extends OrthoViewDirection {
		private final Map<Viewport, Point2d> viewportMap = new HashMap<Viewport, Point2d>();
		public void bindTo(Viewport viewport) {
			Point2d point = viewportMap.get(viewport);
			if (point == null) {
				point = new Point2d(45, -45);
				viewportMap.put(viewport, point);
			}
			viewport.viewRotation.setTuple(point);
		}
		public void unbind(Viewport viewport) {
			Point2d point = viewportMap.get(viewport);
			if (point == null) {
				throw new IllegalStateException();
			}
			viewport.getViewRotationAttribute().getTuple(point);
		}
		public String toString() {
			return "bird's eye";
		}
	};
	
	public void unbind(Viewport viewport) {
		;	// do nothing
	}

	public int compareTo(Object o) {
		if (o == this) {
			return 0;
		}
		if (o instanceof OrthoViewDirection) {
			OrthoViewDirection otherDir = (OrthoViewDirection) o;
			for (int i = 0; i < STANDARD_VIEW_DIRECTIONS.length; i++) {
				if (STANDARD_VIEW_DIRECTIONS[i] == this) {
					return -1;
				} else if (STANDARD_VIEW_DIRECTIONS[i] == otherDir) {
					return 1;
				}
			}
		}
		return -1;
	}
}
