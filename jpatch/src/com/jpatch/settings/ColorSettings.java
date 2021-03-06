/*
 * $Id: ColorSettings.java 420 2007-03-06 17:42:55Z sascha_l $
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
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.vecmath.*;

public class ColorSettings extends AbstractSettings {
	Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/colors.png"));
	public Icon getIcon() {
		return icon;
	}
	public Color3f background = new Color3f(new Color(0x38,0x38,0x38));
	public Color3f activeBackground = new Color3f(new Color(0x40,0x40,0x40));
	public Color3f curves = new Color3f(new Color(255,255,255));
	public Color3f points = new Color3f(new Color(255,255,0));
	public Color3f headPoints = new Color3f(new Color(255,0,0));
	public Color3f multiPoints = new Color3f(new Color(255,128,0));
	public Color3f selectedPoints = new Color3f(new Color(0,255,0));
	public Color3f hotObject = new Color3f(new Color(0,255,255));
	public Color3f tangents = new Color3f(new Color(255,255,0));
	public Color3f selection = new Color3f(new Color(255,255,0));
	public Color3f text = new Color3f(new Color(0x80,0x80,0x80));
	public Color3f majorGrid = new Color3f(new Color(0x18,0x18,0x18));
	public Color3f minorGrid = new Color3f(new Color(0x28,0x28,0x28));
	public Color3f xAxis = new Color3f(new Color(255,64,0));
	public Color3f yAxis = new Color3f(new Color(0,255,0));
	public Color3f zAxis = new Color3f(new Color(128,128,255));
	public Color3f grey = new Color3f(new Color(0x50,0x60,0x70));
	public Color3f backfacingPatches = new Color3f(new Color(255,0,0));
	public Color3f passiveCamera = new Color3f(new Color(0x38,0x48,0x58));
	public Color3f activeCamera = new Color3f(new Color(0x58,0x68,0x78));
	public float ghostFactor = 0.33f;
}