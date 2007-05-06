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
package com.jpatch.boundary;

/**
 * @author sascha
 *
 */
public abstract class OrthoViewDirection implements ViewDirection {
	public static OrthoViewDirection FRONT = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setX(0);
			viewport.viewRotation.setY(0);
		}
		public String toString() {
			return "front";
		}
	};
	public static OrthoViewDirection BACK = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setX(0);
			viewport.viewRotation.setY(180);
		}
		public String toString() {
			return "back";
		}
	};
	public static OrthoViewDirection TOP = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setX(90);
			viewport.viewRotation.setY(0);
		}
		public String toString() {
			return "top";
		}
	};
	public static OrthoViewDirection BOTTOM = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setX(-90);
			viewport.viewRotation.setY(0);
		}
		public String toString() {
			return "bottom";
		}
	};
	public static OrthoViewDirection LEFT = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setX(0);
			viewport.viewRotation.setY(90);
		}
		public String toString() {
			return "left";
		}
	};
	public static OrthoViewDirection RIGHT = new OrthoViewDirection() {
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setX(0);
			viewport.viewRotation.setY(-90);
		}
		public String toString() {
			return "right";
		}
	};
	
	public static class BirdsEye extends OrthoViewDirection {
		private double x = 45;
		private double y = 45;
		public void bindTo(Viewport viewport) {
			viewport.viewRotation.setX(x);
			viewport.viewRotation.setY(y);
		}
		public void unbind(Viewport viewport) {
			x = viewport.viewRotation.getX();
			y = viewport.viewRotation.getY();
		}
		public String toString() {
			return "bird's eye";
		}
	};
	
	public void unbind(Viewport viewport) {
		;	// do nothing
	}
}
