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

import javax.vecmath.Tuple2d;
import javax.vecmath.Vector2d;

class OrthoView {
	final Tuple2d translation = new Vector2d(0.0, 0.0);
	final Tuple2d rotation = new Vector2d(0.0, 0.0);
	double scale = 1.0;
	
	OrthoView() { }
	
	OrthoView(OrthoView view) {
		set(view);
	}
	
	void set(OrthoView view) {
		this.translation.set(view.translation);
		this.rotation.set(view.rotation);
		this.scale = view.scale;
	}
	
	void updateViewdef(OrthoViewDef orthoViewDef) {
		orthoViewDef.getTranslationAttribute().setTuple(translation);
		orthoViewDef.getRotationAttribute().setTuple(rotation);
		orthoViewDef.getScaleAttribute().setDouble(scale);
	}
}