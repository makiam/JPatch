/*
 * $Id: ViewportSettings.java 420 2007-03-06 17:42:55Z sascha_l $
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
package com.jpatch.settings;

import com.jpatch.afw.settings.*;
import javax.swing.*;

public class ViewportSettings extends AbstractSettings {
	Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/display.png"));
	public Icon getIcon() {
		return icon;
	}
	public static enum ScreenMode { SINGLE, HORIZONTAL_SPLIT, VERTICAL_SPLIT, QUAD };
	
	public transient ViewportSettings.ScreenMode viewportMode = ScreenMode.SINGLE;
	public transient boolean synchronizeViewports = false;
	public transient boolean snapToGrid = false;
	public float modelerGridSpacing = 1.0f;
	public float animatorGridSpacing = 10.0f;
	public boolean showGroundPlaneInModeler = false;
	public boolean showGroundPlaneInAnimator = true;
	public float groundPlaneSpacing = 10.0f;
	public int groundPlaneSize = 20;
}