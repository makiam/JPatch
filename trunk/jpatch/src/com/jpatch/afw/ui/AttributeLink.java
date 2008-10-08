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
package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.*;

public final class AttributeLink implements AttributePostChangeListener {
	private final DoubleAttr controller;
	private final DoubleAttr target;
	
	public static AttributeLink createLink(DoubleAttr controller, DoubleAttr target) {
		return new AttributeLink(controller, target);
	}
	
	private AttributeLink(DoubleAttr controller, DoubleAttr target) {
		this.controller = controller;
		this.target = target;
		controller.addAttributePostChangeListener(this);
	}
	
	public void attributeHasChanged(Attribute source) {
		target.setDouble(controller.getDouble());
	}
	
	public void unlink() {
		controller.removeAttributePostChangeListener(this);
	}
}